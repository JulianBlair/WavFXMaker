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
	         System.out.println("Sample rate: "+samplerate);
	         
	         dis.readInt(); //byterate
	         dis.readShort(); //block align
	         
	         short bitspersample = dis.readShort();
	         bitspersample = little2big(bitspersample);
	         short bytespersample = (short) (bitspersample / 8);
	         System.out.println("Bits per sample: " + bitspersample);

	         discard = true;
	         while (discard) {
	        	 int read = dis.readInt();
	        	 if (read == DATA_INTREP) discard = false;
	         }

	         int datasize = dis.readInt();
	         datasize = little2big(datasize) / channels / bytespersample;
	         System.out.println("Number of samples: " + datasize);
	         
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
	
	static void write(WavObj o, String in, String out) throws IOException {
		System.out.println("---WRITING FILE---");
         try {
        // create new data I/O stream
        dis = new DataInputStream(new FileInputStream(in));   
        dos = new DataOutputStream(new FileOutputStream(out)); 
         
        int bytespersample = o.bitspersample / 8;
        System.out.print("Writing header...");
		//Copy first chunk + "fmt "
        boolean discard = true;
        while (discard) {
	       	 int read = dis.readInt();
	       	 if (read == FMT_INTREP) discard = false;
	       	 dos.writeInt(read);
        }
        dos.writeInt(dis.readInt()); //Chunk 1 size
        dos.writeShort(dis.readShort()); //PCM
        
        dos.writeShort(little2big(o.channels)); dis.readShort();
        dos.writeInt(little2big(o.samplerate)); dis.readInt();
        dos.writeInt(little2big(o.samplerate * o.channels * bytespersample)); dis.readInt();
        dos.writeShort(little2big((short) (o.channels * bytespersample))); dis.readShort();
        dos.writeShort(little2big(o.bitspersample)); dis.readShort();
        
        //Copy up to "data"
        discard = true;
        while (discard) {
       		int read = dis.readInt();
       		if (read == DATA_INTREP) discard = false;
       		dos.writeInt(read);
        }
        dos.writeInt(little2big(o.buf.length * o.channels * bytespersample));
        System.out.println("DONE");
        
        System.out.print("Write data progress:\t");
        int i = 0, datasize = o.buf.length;
		for (double f : o.buf) {
       		perc(i,datasize,10);
       		i++;
			byte[] d = null;
			long rnd = Math.round(f);
			switch (bytespersample) {
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
			for (int j = 0; j < bytespersample; j++) {
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
   		 	if (prog < 0.1) System.out.print(" ");
   	 	}
	}
}