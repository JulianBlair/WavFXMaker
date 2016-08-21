package main;

import java.util.*;

public class WavObj {
	private short channels, bitdepth;
	private int samplerate;
	private double[] buf;
	private double amplitude;
	boolean suppress = false;
	
	WavObj (short channels, short bitdepth, int samplerate, double[] buf) {
		this.channels = channels;
		this.bitdepth = bitdepth;
		this.samplerate = samplerate;
		this.update(buf);
	}
	
	public void append(WavObj second, double vol, boolean relvol) {
		if (!suppress) System.out.println("---COMBINING TWO WAVS---");
		if (vol < 0 || this.channels != second.channels || this.bitdepth < second.bitdepth) return;
		double amp1 = 1, amp2 = 1;
		if (relvol) {
			amp1 = this.amplitude;
			amp2 = second.amplitude;
		}
		second.convertSamplerate(this.samplerate);
		double[] temp = new double[this.buf.length + second.getDataSize()];
		for (int i = 0; i < this.buf.length; i++)
			temp[i] = this.buf[i];
		for (int i = 0; i < second.getDataSize(); i++)
			temp[this.buf.length+i] = second.value(i)*amp1/amp2*vol;
		this.update(temp);
		if (!suppress) System.out.println("DONE");
	}
	
	public void mix(WavObj second, double vol, boolean relvol, double start) {
		if (!suppress) System.out.println("---MIXING TWO WAVS---");
		int a = (int) (start*this.samplerate);
		if (a < 0 || a >= this.getDataSize() || vol < 0 || this.channels != second.channels || this.bitdepth < second.bitdepth) return;
		double amp1 = 1, amp2 = 1;
		if (relvol) {
			amp1 = this.amplitude;
			amp2 = second.amplitude;
		}
		second.convertSamplerate(this.samplerate);
		for (int i = 0; (i < second.getDataSize() && a+i < this.getDataSize()); i++)
			buf[a+i] += second.value(i)*amp1/amp2*vol;
		update();
		if (!suppress) System.out.println("DONE");
	}
	
	/**
	 * Trim audio track between two points.
	 * @param start Start point in secs, inclusive.
	 * @param finish End point in secs, exclusive.
	 */
	public void trim(double start, double finish) {
		if (!suppress) System.out.println("---EFFECT: TRIM---");
		int a = (int)(start*this.samplerate), b = (int)(finish*this.samplerate);
		if (a < 0 || a >= buf.length || b < 0 || b >= buf.length || a >= b) return;
		if (!suppress) System.out.print("Processing:\t\t");
		double[] temp = new double[b-a];
		for (int i = a; i < b; i++) {
			if (!suppress) WavDataHandler.perc(i-a, temp.length, 10);
			temp[i-a] = buf[i];
		}
		this.update(temp);
		if (!suppress) System.out.println("100.00%");
	}
	
	public void diff(int order) {
		if (!suppress) System.out.println("---EFFECT: DIFFERENTIATOR---");
		if (order < 1) return;
		if (!suppress) System.out.print("Processing:\t\t");
		double amp1 = amplitude();
		
		double[] temp = new double[buf.length-order];
		for (int i = 1; i <= order; i++) 
			for (int j = 0; j < buf.length-i; j++) {
				if (!suppress) WavDataHandler.perc((buf.length-i)*(i-1)+j, buf.length*order, 10);
				buf[j] = buf[j+1] - buf[j];
			}
		
		double amp2 = amplitude();
		
		for (int i = 0; i < buf.length-order; i++)
			temp[i] = buf[i]*amp1/amp2;
		
		this.update(temp);
		if (!suppress) System.out.println("100.00%");
	}
	
	public void pitch(double factor) {
		if (!suppress) System.out.println("---EFFECT: PITCH SHIFT---");
		if (factor == 1.0) return;
		if (factor > 1.0) squish(factor);
		else if (factor < 1.0) stretch(1.0 / factor);
		if (!suppress) System.out.println("100.00%");
	}
	
	private void stretch(double factor) {
		if (!suppress) System.out.printf("Stretching by x%.1f: ",factor);
		if (!suppress) System.out.print("\t");
		double[] temp = new double[(int)((buf.length-1)*factor)+1];
		
		for (int i = 0; i < temp.length; i++) {
			if (!suppress) WavDataHandler.perc(i, temp.length, 10);
			if ((i/factor)+1 < buf.length) {
				temp[i] = ((i%factor)*buf[(int)(i/factor)+1] + (factor-(i%factor))*buf[(int)(i/factor)]) / factor;
			}
		}
		
		this.update(temp);
	}
	
	private void squish(double factor) {
		if (!suppress) System.out.printf("Squishing by x%.1f:",factor);
		if (!suppress) System.out.print("\t");
		double[] temp = new double[(int)((buf.length-1)/factor)+1];
		
		for (int i = 0; i < temp.length; i++) {
			if (!suppress) WavDataHandler.perc(i, temp.length, 10);
			temp[i] = buf[(int)(i*factor)];
		}
		
		this.update(temp);
	}
	
	public void reverse() {
		reverse(0, buf.length-1);
	}
	
	public void reverse(int l, int r) {
		if (!suppress) System.out.println("---EFFECT: REVERSE---");
		if (l >= buf.length || l < 0 || r >= buf.length || r < 0 || l >= r) return;
		if (!suppress) System.out.print("Processing:\t\t");
		int start = l, end = r;
		while (l < r) {
			if (!suppress) WavDataHandler.perc((l-start)*2, end-start, 10);
			double temp = buf[l];
			buf[l] = buf[r];
			buf[r] = temp;
			l++;
			r--;
		}
		if (!suppress) System.out.println("100.00%");
	}
	
	/**
	 * Creates delay effect.
	 * @param secs Seconds between feedback loop.
	 * @param decay Feedback % - higher means more feedback.
	 * @param wetvol Wet volume.
	 */
	public void delay(double secs, double decay, double wetvol) {
		if (!suppress) System.out.println("---EFFECT: DELAY---");
        int freq = (int) (secs*this.samplerate);
		if (freq > buf.length || freq <= 0 || decay >= 1 || decay <= 0 || wetvol > 1 || wetvol < 0) return;
		double[] wet = new double[buf.length];
		if (!suppress) System.out.print("Processing:\t\t");
		for (int i = 0; i < freq; i++) {
			if (!suppress) WavDataHandler.perc(i, wet.length, 10);
			wet[i] = buf[i];
		}
		for (int i = freq; i < buf.length; i++) {
			if (!suppress) WavDataHandler.perc(i, wet.length, 10);
			wet[i] = buf[i] + wet[i-freq]*decay; 
			wet[i] = wet[i]*wetvol + buf[i]*(1-wetvol);
		}
		this.update(wet);
		if (!suppress) System.out.println("100.00%");
	}
	
	public void extend(double secs) {
		if (!suppress) System.out.println("---EFFECT: EXTEND WITH SILENCE---");
		double[] temp = new double[(int)(buf.length+secs*this.samplerate)];
		if (!suppress) System.out.print("Processing:\t\t");
		for (int i = 0; i < buf.length; i++) {
			if (!suppress) WavDataHandler.perc(i, buf.length, 10);
			temp[i] = buf[i];
		}
		if (!suppress) System.out.println("100.00%");
        this.update(temp);
	}
	
	public void repeat(int times) {
		if (!suppress) System.out.println("---EFFECT: REPEAT---");
		double[] temp = new double[buf.length*times];
		if (!suppress) System.out.print("Processing:\t\t");
		for (int i = 0; i < buf.length; i++) {
			if (!suppress) WavDataHandler.perc(i, buf.length, 10);
			for (int j = 0; j < times; j++)
				temp[i+buf.length*j] = buf[i];
		}
		this.update(temp);
		if (!suppress) System.out.println("100.00%");
	}
	
	public void volume(double factor) {
		if (!suppress) System.out.println("---EFFECT: VOLUME---");
		if (!suppress) System.out.print("Processing:\t\t");
		for (int i = 0; i < buf.length; i++) {
			if (!suppress) WavDataHandler.perc(i, buf.length, 10);
			buf[i] *= factor;
		}
		update();
		if (!suppress) System.out.println("100.00%");
	}
	
	public double amplitude() {
		return amplitude(buf.length/2,buf.length);
	}
	
	private double amplitude(int time, int duration) {
		if (time-duration/2 < 0 || time+duration/2 > buf.length) return -1;
		double avg = 0;
		for (int i = 0; i < duration/2; i++) {
			avg += buf[time-i]*buf[time-i];
			avg += buf[time+i]*buf[time+i];
		}
		avg /= duration;
		return Math.sqrt(avg);
	}
	
	public void visual(int width) {
		visual(width, buf.length);
	}
	
	public void convertSamplerate(int samplerate) {
		if (this.samplerate == samplerate) return;
		this.suppress = true;
		this.pitch(this.samplerate/(double)samplerate);
		this.suppress = false;
		if (!suppress) System.out.println("---SAMPLE RATE CONVERTED FROM "+this.samplerate+" Hz TO "+samplerate+" Hz---");
		this.samplerate = samplerate;
	}
	
	public void visual(int width, int length) {
		if (width < 1 || length < 1) return;
		double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        Scanner in = new Scanner(System.in);
        System.out.println("---VISUAL PRINTER---");
        System.out.print("This function will print "+length+" lines. Proceed? (y/n) ");
        String response = in.next();
        in.close();
        if (!response.equals("Y") && !response.equals("y")) return;
        
        for (int i = 0; i < length; i++) {
       		if (buf[i] < min) min = buf[i];
       		if (buf[i] > max) max = buf[i];
        }
        
        for (int i = 0; i < length; i++) {
       		int pos = (int) Math.round((buf[i]-min)/(max-min)*width);
       		StringBuffer b = new StringBuffer();
       		for (int k = 0; k < 4; k++) b.append(" ");
       		int j = 0;
       		while (j < pos) {b.append(" "); j++;}
       		b.append("x"); j++;
       		while (j < width) {b.append(" "); j++;}
       		System.out.println(b.toString());
        }
	}
	
	private void update(double[] newar) {
		this.buf = newar;
		update();
	}
	
	private void update() {
		amplitude = this.amplitude();
	}
	
	public double value(int i) {
		if (i < 0 || i >= buf.length) return -1;
		return buf[i];
	}
	
	public int getDataSize() {
		return this.buf.length;
	}
	
	public short getBitsPerSample() {
		return this.bitdepth;
	}
	
	public int getBytesPerSample() {
		return this.bitdepth / 8;
	}
	
	public short getChannels() {
		return channels;
	}
	
	public int getSampleRate() {
		return samplerate;
	}
}
