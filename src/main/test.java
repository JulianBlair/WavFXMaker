package main;

import java.io.IOException;

public class test {

	private static String INPUT_FILE;
	private static String OUTPUT_FILE;
	
	public static void main(String[] args) throws IOException {
		
		//INPUT_FILE = "C:\\Users\\Julian\\Music\\~Sound Packs\\Fantom Claps\\fantclaps101(01).wav";
		INPUT_FILE = "C:\\Users\\Julian\\Desktop\\wavtest_small.wav";
		OUTPUT_FILE = "C:\\Users\\Julian\\Desktop\\output.wav";
		
        WavObj wave = WavDataHandler.read(INPUT_FILE);

        //wave.repeat(10);
        wave.extend(3.5);
        wave.delay(0.5, 0.5, 1);
        //wave.visual(50, 100);
        //wave.reverse();
        //wave.pitch(0.2);
        //wave.diff(2);
        //wave.volume(2);
        
        WavDataHandler.write(wave, OUTPUT_FILE);
	}
}
