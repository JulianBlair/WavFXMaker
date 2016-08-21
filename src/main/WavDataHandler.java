package main;
import java.io.*;
import java.nio.*;

public class WavDataHandler {
	
	static int DATA_INTREP = 1684108385;
	static int FMT_INTREP = 1718449184;
	
    static DataInputStream dis = null;
    static DataOutputStream dos = null;
		
	static WavObj read(String in) throws IOException {  
		
		WavObj ret = null;
		
		System.out.println("---READING FILE---");
		
		try {  
	        
	        // create new data input stream
	        dis = new DataInputStream(new FileInputStream(in)); 
	        
	         boolean discard = true;
	         while (discard) {
		       	 int read = dis.readInt();
		       	 if (read == FMT_INTREP) discard = false;
	         }
	         
	         dis.readInt(); //Chunk 1 size
	         dis.readShort(); //PCM
	         
	         short channels = dis.readShort();
	         channels = little2big(channels);
	         System.out.println("Channels: "+channels);

	         int samplerate = dis.readInt();
	         samplerate = little2big(samplerate);
	         System.out.println("Sample rate: "+samplerate+" Hz");
	         
	         dis.readInt(); //byterate
	         dis.readShort(); //block align
	         
	         short bitspersample = dis.readShort();
	         bitspersample = little2big(bitspersample);
	         short bytespersample = (short) (bitspersample / 8);
	         System.out.println("Bits per sample: " + bitspersample+"-bit");

	         discard = true;
	         while (discard) {
	        	 int read = dis.readInt();
	        	 if (read == DATA_INTREP) discard = false;
	         }

	         int datasize = dis.readInt();
	         datasize = little2big(datasize) / channels / bytespersample;
	         //System.out.println("Number of samples: " + datasize);
	         System.out.println();
	         
	         double[] buf = new double[datasize];
	         
	         System.out.print("Read data progress:\t");
	         
	         for (int i = 0; i < datasize; i++) {
	        	 perc(i,datasize,10);
	        	 byte[] d = new byte[bytespersample];
	        	 for (int j = 0; j < bytespersample; j++) {
	        		 d[j] = dis.readByte();
	        	 }
	        	 buf[i] = parse(d, bytespersample);
	         }
	         System.out.println("100.00%");
	         
			ret = new WavObj(channels, bitspersample, samplerate, buf);
	         
	      }catch(Exception e){
	         e.printStackTrace();
	      }finally{
	         // releases all system resources from the streams
	         if(dis!=null)
	            dis.close();
	      }
		
        return ret;
		
		
	}
	
	static void write(WavObj o, String out) throws IOException {
		System.out.println("---WRITING FILE---");
         try {
        // create new data I/O stream
        dos = new DataOutputStream(new FileOutputStream(out)); 
         
        System.out.print("Writing header...");
        
        int subchunk2Size = o.getDataSize() * o.getChannels() * o.getBytesPerSample();
        byte[] chunkID = {'R', 'I', 'F', 'F'};
        int chunkSize = 36 + subchunk2Size;
        byte[] format = {'W', 'A', 'V', 'E'};
        byte[] subchunk1ID = {'f', 'm', 't', ' '};
        int subchunk1Size = 16;
        short audioFormat = 1;
        byte[] subchunk2ID = {'d', 'a', 't', 'a'};
        
        dos.write(chunkID);
        dos.writeInt(little2big(chunkSize));
        dos.write(format);
        dos.write(subchunk1ID);
        dos.writeInt(little2big(subchunk1Size));
        dos.writeShort(little2big(audioFormat));        
        dos.writeShort(little2big(o.getChannels())); 
        dos.writeInt(little2big(o.getSampleRate())); 
        dos.writeInt(little2big(o.getSampleRate() * o.getChannels() * o.getBytesPerSample())); 
        dos.writeShort(little2big((short) (o.getChannels() * o.getBytesPerSample()))); 
        dos.writeShort(little2big(o.getBitsPerSample())); 
        dos.write(subchunk2ID);
        dos.writeInt(little2big(subchunk2Size));
        
        System.out.println("DONE");
        
        System.out.print("Write data progress:\t");
        int datasize = o.getDataSize();
		for (int i = 0; i < datasize; i++) {
       		perc(i,datasize,10);
			byte[] d = null;
			long rnd = Math.round(o.value(i));
			switch (o.getBytesPerSample()) {
			case 4: 
				d = big2little((int) rnd);
				break;
			case 2: 
				d = big2little((short) rnd);
				break;
			case 1: 
				d = big2little((byte) rnd);
				break;
			}
			for (int j = 0; j < o.getBytesPerSample(); j++) {
       		 dos.writeByte(d[j]);
       	 	}
		}
		System.out.println("100.00%");
     	}catch(Exception e){
			e.printStackTrace();
     	}finally{
     		// releases all system resources from the streams
     		if(dis!=null)
     			dis.close();
     		if(dos!=null)
     			dos.close();
     	}
	}
	
	static float parse(byte[] d, int bytespersample) {
		switch (bytespersample) {
			case 4: 
				return ByteBuffer.wrap(d).order(ByteOrder.LITTLE_ENDIAN).getInt();
			case 2: 
				return ByteBuffer.wrap(d).order(ByteOrder.LITTLE_ENDIAN).getShort();
			case 1: 
				return ByteBuffer.wrap(d).order(ByteOrder.LITTLE_ENDIAN).get();
			default: 
				return 0f;
		}
	}
	
	static byte[] big2little(int i) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte)(i & 0xff);
		bytes[1] = (byte)((i >> 8) & 0xff);
		bytes[2] = (byte)((i >> 16) & 0xff);
		bytes[3] = (byte)((i >> 24) & 0xff);
		return bytes;
	}
	
	static byte[] big2little(short i) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte)(i & 0xff);
		bytes[1] = (byte)((i >> 8) & 0xff);
		return bytes;
	}
	
	static byte[] big2little(byte i) {
		byte[] bytes = {i};
		return bytes;
	}
	
	static int little2big(int i) {
	    return i<<24 | i>>8 & 0xff00 | i<<8 & 0xff0000 | i>>>24;
	}
	
	static short little2big(short i) {
	    return (short) (i<<8 | i>>8);
	}
	
	protected static void perc(int i, int len, int freq) {
   	 	double prog = i / (double) len;
   	 	if (len >= freq && i % (len/freq) == 0 && prog < 0.99) {
   	 		System.out.printf("%.2f%%...",prog*100);
   		 	if (Math.round(prog*100*100)/100 < 10) System.out.print(" ");
   	 	}
	}
}