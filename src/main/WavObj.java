package main;

public class WavObj {
	short channels, bitspersample;
	int samplerate;
	double[] buf;
	
	WavObj (short channels, short bitspersample, int samplerate, double[] buf) {
		this.channels = channels;
		this.bitspersample = bitspersample;
		this.samplerate = samplerate;
		this.buf = buf;
	}
	
	void delay(int freq, double decay, double wetvol) {
        System.out.println("---EFFECT: DELAY---");
		if (freq > buf.length || freq <= 0 || decay >= 1 || decay <= 0 || wetvol > 1 || wetvol < 0) return;
		double[] wet = new double[buf.length];
        System.out.print("Processing:\t\t");
		for (int i = 0; i < freq; i++) {
	   	 	perc(i, wet.length);
			wet[i] = buf[i];
		}
		for (int i = freq; i < buf.length; i++) {
			perc(i, wet.length);
			wet[i] = buf[i] + wet[i-freq]*decay; 
			wet[i] = wet[i]*wetvol + buf[i]*(1-wetvol);
		}
		buf = wet;
        System.out.println("100.00%");
	}
	
	void extend(int times) {
        System.out.println("---EFFECT: EXTEND WITH SILENCE---");
		double[] temp = new double[buf.length*times];
        System.out.print("Processing:\t\t");
		for (int i = 0; i < buf.length; i++) {
			perc(i, buf.length);
			temp[i] = buf[i];
		}
        System.out.println("100.00%");
		buf = temp;
	}
	
	void repeat(int times) {
        System.out.println("---EFFECT: REPEAT---");
		double[] temp = new double[buf.length*times];
        System.out.print("Processing:\t\t");
		for (int i = 0; i < buf.length; i++) {
			perc(i, buf.length);
			for (int j = 0; j < times; j++)
				temp[i+buf.length*j] = buf[i];
		}
		buf = temp;
        System.out.println("100.00%");
	}
	
	void volume(double factor) {
        System.out.println("---EFFECT: VOLUME---");
        System.out.print("Processing:\t\t");
		for (int i = 0; i < buf.length; i++) {
			perc(i, buf.length);
			buf[i] *= factor;
		} 
        System.out.println("100.00%");
	}
	
	double amplitude(int time, int duration) {
		if (time-duration/2 < 0 || time+duration/2 > buf.length) return -1;
		double avg = 0;
		for (int i = 0; i < duration/2; i++) {
			avg += buf[time-i]*buf[time-i];
			avg += buf[time+i]*buf[time+i];
		}
		avg /= duration;
		return Math.sqrt(avg);
	}
	
	private void perc(int i, int len) {
   	 	double prog = i / (double) len;
   	 	if (i % (len/10) == 0) System.out.printf("%.2f%%...",prog*100);
	}
}
