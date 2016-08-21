package main;

import java.util.*;

public class WavObj {
	private short channels, bitspersample;
	private int samplerate;
	private double[] buf;
	
	WavObj (short channels, short bitspersample, int samplerate, double[] buf) {
		this.channels = channels;
		this.bitspersample = bitspersample;
		this.samplerate = samplerate;
		this.buf = buf;
	}
	
	public double value(int i) {
		if (i < 0 || i >= buf.length) return -1;
		return buf[i];
	}
	
	public int getDataSize() {
		return this.buf.length;
	}
	
	public short getBitsPerSample() {
		return this.bitspersample;
	}
	
	public int getBytesPerSample() {
		return this.bitspersample / 8;
	}
	
	public short getChannels() {
		return channels;
	}
	
	public int getSampleRate() {
		return samplerate;
	}
	
	public void diff(int order) {
		System.out.println("---EFFECT: DIFFERENTIATOR---");
		if (order < 1) return;
		System.out.print("Processing:\t\t");
		double amp1 = amplitude(buf.length/2,buf.length);
		
		double[] temp = new double[buf.length-order];
		for (int i = 1; i <= order; i++) 
			for (int j = 0; j < buf.length-i; j++) {
				WavDataHandler.perc((buf.length-i)*(i-1)+j, buf.length*order, 10);
				buf[j] = buf[j+1] - buf[j];
			}
		
		double amp2 = amplitude(buf.length/2,buf.length);
		
		for (int i = 0; i < buf.length-order; i++)
			temp[i] = buf[i]*amp1/amp2;
		
		buf = temp;
        System.out.println("100.00%");
	}
	
	public void pitch(double factor) {
		System.out.println("---EFFECT: PITCH SHIFT---");
		if (factor == 1.0) return;
		if (factor > 1.0) squish(factor);
		else if (factor < 1.0) stretch(1.0 / factor);
        System.out.println("100.00%");
	}
	
	private void stretch(double factor) {
		System.out.printf("Stretching by x%.1f: ",factor);
		System.out.print("\t");
		double[] temp = new double[(int)((buf.length-1)*factor)+1];
		
		for (int i = 0; i < temp.length; i++) {
			WavDataHandler.perc(i, temp.length, 10);
			if (i % factor == 0) temp[i] = buf[(int)(i/factor)];
			else temp[i] = ((i%factor)*buf[(int)(i/factor)+1] + (factor-(i%factor))*buf[(int)(i/factor)]) / factor;
		}
		
		buf = temp;
	}
	
	private void squish(double factor) {
		System.out.printf("Squishing by x%.1f:",factor);
		System.out.print("\t");
		double[] temp = new double[(int)((buf.length-1)/factor)+1];
		
		for (int i = 0; i < temp.length; i++) {
			WavDataHandler.perc(i, temp.length, 10);
			temp[i] = buf[(int)(i*factor)];
		}
		
		buf = temp;
	}
	
	public void reverse() {
		reverse(0, buf.length-1);
	}
	
	public void reverse(int l, int r) {
		System.out.println("---EFFECT: REVERSE---");
		if (l >= buf.length || l < 0 || r >= buf.length || r < 0 || l >= r) return;
		System.out.print("Processing:\t\t");
		int start = l, end = r;
		while (l < r) {
			WavDataHandler.perc((l-start)*2, end-start, 10);
			double temp = buf[l];
			buf[l] = buf[r];
			buf[r] = temp;
			l++;
			r--;
		}
		System.out.println("100.00%");
	}
	
	public void delay(int freq, double decay, double wetvol) {
        System.out.println("---EFFECT: DELAY---");
		if (freq > buf.length || freq <= 0 || decay >= 1 || decay <= 0 || wetvol > 1 || wetvol < 0) return;
		double[] wet = new double[buf.length];
        System.out.print("Processing:\t\t");
		for (int i = 0; i < freq; i++) {
			WavDataHandler.perc(i, wet.length, 10);
			wet[i] = buf[i];
		}
		for (int i = freq; i < buf.length; i++) {
			WavDataHandler.perc(i, wet.length, 10);
			wet[i] = buf[i] + wet[i-freq]*decay; 
			wet[i] = wet[i]*wetvol + buf[i]*(1-wetvol);
		}
		buf = wet;
        System.out.println("100.00%");
	}
	
	public void extend(int times) {
        System.out.println("---EFFECT: EXTEND WITH SILENCE---");
		double[] temp = new double[buf.length*times];
        System.out.print("Processing:\t\t");
		for (int i = 0; i < buf.length; i++) {
			WavDataHandler.perc(i, buf.length, 10);
			temp[i] = buf[i];
		}
        System.out.println("100.00%");
		buf = temp;
	}
	
	public void repeat(int times) {
        System.out.println("---EFFECT: REPEAT---");
		double[] temp = new double[buf.length*times];
        System.out.print("Processing:\t\t");
		for (int i = 0; i < buf.length; i++) {
			WavDataHandler.perc(i, buf.length, 10);
			for (int j = 0; j < times; j++)
				temp[i+buf.length*j] = buf[i];
		}
		buf = temp;
        System.out.println("100.00%");
	}
	
	public void volume(double factor) {
        System.out.println("---EFFECT: VOLUME---");
        System.out.print("Processing:\t\t");
		for (int i = 0; i < buf.length; i++) {
			WavDataHandler.perc(i, buf.length, 10);
			buf[i] *= factor;
		} 
        System.out.println("100.00%");
	}
	
	public double amplitude(int time, int duration) {
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
}
