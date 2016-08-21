package main;

public class WavGen {
	
	private short channels, bitdepth;
	private int samplerate;
	
	public WavGen(WavObj settings) {
		this.bitdepth = settings.getBitsPerSample();
		this.channels = settings.getChannels();
		this.samplerate = settings.getSampleRate();
	}
	
	public WavObj silence(double secs) {
		int size = (int) (secs * this.samplerate);
		double[] buf = new double[size];
		return new WavObj(channels, bitdepth, samplerate, buf);
	}
	
	public WavObj sine(int freq, double secs) {
		int size = (int) (secs * this.samplerate);
		double[] buf = new double[size];
		double period = this.samplerate/(double)freq;
		for (int i = 0; i < size; i++) {
			buf[i] = 20000*Math.sin(i/period*2*Math.PI);
		}
		return new WavObj(channels, bitdepth, samplerate, buf);
	}
	
	public WavObj square(int freq, double secs) {
		int size = (int) (secs * this.samplerate);
		double[] buf = new double[size];
		double period = this.samplerate/(double)freq;
		int sign = 1;
		for (int i = 0; i < size; i++) {
			if (2*i % (int) period == 0) sign *= -1;
			buf[i] = 20000*sign;
		}
		return new WavObj(channels, bitdepth, samplerate, buf);
	}
	
	public WavObj saw(int freq, double secs) {
		int size = (int) (secs * this.samplerate);
		double[] buf = new double[size];
		double period = this.samplerate/(double)freq;
		double curr = -1;
		for (int i = 0; i < size; i++) {
			if (i % (int) period == 0) curr = -1;
			else curr += 1/period;
			buf[i] = 20000*curr;
		}
		return new WavObj(channels, bitdepth, samplerate, buf);
	}
	
	public WavObj metronome(int bpm, double secs, int timesig) {
		WavObj met = this.silence(secs);
		double beeplength = 0.1;
		int freq = 900;
		double period = 60 / (double) bpm;
		for (int i = 0; i*period*this.samplerate < met.getDataSize(); i++) {
			if (i % timesig == 0) met.mix(sine(freq, beeplength), 1, false, i*period);
			else met.mix(sine(freq/2, beeplength), 1, false, i*period);
		}
		
		return met;
	}
}
