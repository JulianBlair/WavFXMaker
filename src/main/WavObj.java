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
		if (freq > buf.length || freq <= 0 || decay >= 1 || decay <= 0 || wetvol > 1 || wetvol < 0) return;
		double[] wet = new double[buf.length];
		for (int i = 0; i < freq; i++)
			wet[i] = buf[i];
		for (int i = freq; i < buf.length; i++) {
			wet[i] = buf[i] + wet[i-freq]*decay; 
			wet[i] = wet[i]*wetvol + buf[i]*(1-wetvol);
		}
		buf = wet;
	}
	
	void extend(int times) {
		double[] temp = new double[buf.length*times];
		for (int i = 0; i < buf.length; i++)
			temp[i] = buf[i];
		buf = temp;
	}
	
	void repeat(int times) {
		double[] temp = new double[buf.length*times];
		for (int i = 0; i < buf.length; i++) {
			for (int j = 0; j < times; j++)
				temp[i+buf.length*j] = buf[i];
		}
		buf = temp;
	}
	
	void volume(double factor) {
		for (int i = 0; i < buf.length; i++) {
			buf[i] *= factor;
		} 
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
}
