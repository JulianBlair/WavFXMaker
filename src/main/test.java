package main;

import java.io.IOException;

public class test {
	
	public static void main(String[] args) throws IOException {
		
		//String INPUT_FILE = "C:\\Users\\Julian\\Music\\~Sound Packs\\Fantom Claps\\fantclaps101(01).wav";
		String INPUT_FILE = "C:\\Users\\Julian\\Desktop\\wavtest_small.wav";
		String OUTPUT_FILE = "C:\\Users\\Julian\\Desktop\\output.wav";
		
        WavObj wave = WavDataHandler.read(INPUT_FILE);

        //wave.volume(0.2);
        //wave.repeat(10);
        //wave.extend(50);
        //wave.delay(2000, 0.8, 1);
        //wave.visual(50);
        
        WavDataHandler.write(wave, INPUT_FILE, OUTPUT_FILE);
	}
}
