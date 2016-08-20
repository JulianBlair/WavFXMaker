package main;
import java.io.*;
import java.nio.*;

public class WavDataHandler {
	
	static int DATA_INTREP = 1684108385;
	static int FMT_INTREP = 1718449184;
	
    static DataInputStream dis = null;
    static DataOutputStream dos = null;
	static InputStream is = null;
	static OutputStream os = null;
		
	static WavObj read(String in) throws IOException {  
		
		WavObj ret = null;
		
		try {  
			
			// create file input stream
	        is = new FileInputStream(in);
	        
	        // create new data input stream
	        dis = new DataInputStream(is); 
	        
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
	         
	         int byterate = dis.readInt();
	         byterate = little2big(byterate);
	         
	         short blockalign = dis.readShort();
	         blockalign = little2big(blockalign);
	         System.out.println("Block align: "+blockalign);
	         
	         short bitspersample = dis.readShort();
	         short bytespersample = (short) (little2big(bitspersample) / 8);
	         System.out.println("Bits per sample: " + bitspersample);

	         discard = true;
	         while (discard) {
	        	 int read = dis.readInt();
	        	 if (read == DATA_INTREP) discard = false;
	         }

	         int datasize = dis.readInt();
	         datasize = little2big(datasize) / channels / bytespersample;
	         
	         double[] buf = new double[datasize];
	         
	         for (int i = 0; i < datasize; i++) {
	        	 byte[] d = new byte[bytespersample];
	        	 for (int j = 0; j < bytespersample; j++) {
	        		 d[j] = dis.readByte();
	        	 }
	        	 buf[i] = parse(d, bytespersample);
	         }
	         
			ret = new WavObj(channels, blockalign, bitspersample, samplerate, byterate, buf);
	         
	      }catch(Exception e){
	         e.printStackTrace();
	      }finally{
	         // releases all system resources from the streams
	         if(is!=null)
	            is.close();
	         if(dis!=null)
	            dis.close();
	      }
		
        return ret;
		
		
	}
	
	static void write(WavObj o, String in, String out) throws IOException {
		
		// create file input stream
         is = new FileInputStream(in);
         os = new FileOutputStream(out);
         
         // create new data input stream
         dis = new DataInputStream(is);   
         dos = new DataOutputStream(os); 
		
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
        dos.writeInt(little2big(o.byterate)); dis.readInt();
        dos.writeShort(little2big(o.blockalign)); dis.readShort();
        dos.writeShort(o.bitspersample); dis.readShort();
        
        //Copy up to "data"
        discard = true;
        while (discard) {
       	 int read = dis.readInt();
       	 if (read == DATA_INTREP) discard = false;
       	 dos.writeInt(read);
        }
        
        int bytespersample = little2big(o.bitspersample) / 8;
        
        dos.writeInt(little2big(o.buf.length * o.channels * bytespersample));
		for (double f : o.buf) {			
			byte[] d = null;
			switch (bytespersample) {
			case 4: 
				d = big2little((float) f);
				break;
			case 2: 
				d = big2little((short) Math.round(f));
				break;
			case 1: 
				d = big2little((byte) Math.round(f));
				break;
			}
			for (int j = 0; j < bytespersample; j++) {
       		 dos.writeByte(d[j]);
       	 	}
		}
	}
	
	static float parse(byte[] d, int bytespersample) {
		switch (bytespersample) {
			case 4: 
				return ByteBuffer.wrap(d).order(ByteOrder.LITTLE_ENDIAN).getFloat();
			case 2: 
				return ByteBuffer.wrap(d).order(ByteOrder.LITTLE_ENDIAN).getShort();
			case 1: 
				return ByteBuffer.wrap(d).order(ByteOrder.LITTLE_ENDIAN).get();
			default: 
				return 0f;
		}
	}
	
	static byte[] big2little(float i) {
		int bits = Float.floatToIntBits(i);
		byte[] bytes = new byte[4];
		bytes[0] = (byte)(bits & 0xff);
		bytes[1] = (byte)((bits >> 8) & 0xff);
		bytes[2] = (byte)((bits >> 16) & 0xff);
		bytes[3] = (byte)((bits >> 24) & 0xff);
		return bytes;
	}
	
	static byte[] big2little(short i) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte)(i & 0xff);
		bytes[1] = (byte)((i >> 8) & 0xff);
		return bytes;
	}
	
	static int little2big(int i) {
	    return i<<24 | i>>8 & 0xff00 | i<<8 & 0xff0000 | i>>>24;
	}
	
	static short little2big(short i) {
	    return (short) (i<<8 | i>>8);
	}
	
	static void visual(float[] buf, int width, int datasize) {
		float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        
        for (int i = 0; i < datasize; i++) {
       	 if (buf[i] < min) min = buf[i];
       	 if (buf[i] > max) max = buf[i];
        }
        
        for (int i = 0; i < datasize; i++) {
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