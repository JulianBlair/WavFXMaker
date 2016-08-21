package main;

import java.io.IOException;

public class test {
	
	public static void main(String[] args) throws IOException {
		
		String INPUT_FILE1 = "C:\\Users\\Julian\\Music\\~Sound Packs\\Fantom Claps\\fantclaps101(01).wav";
		String INPUT_FILE2 = "C:\\Users\\Julian\\Desktop\\wavtest.wav";
		String OUTPUT_FILE = "C:\\Users\\Julian\\Desktop\\output.wav";

        WavObj wave1 = WavDataHandler.read(INPUT_FILE1);
        WavObj wave2 = WavDataHandler.read(INPUT_FILE2);
        
        //wave2.extend(0.1);
        wave1.repeat(10);
        wave2.mix(wave1, 0.5, 0.5);

        //wave.repeat(10);
        //wave.delay(0.1, 0.6, 1);
        //wave.visual(50, 100);
        //wave.reverse();
        //wave.pitch(0.3);
        //wave.diff(2);
        //wave.volume(2);
        //wave2.trim(1, 3);
        
        //wave2.convertSamplerate(44100);
        
        WavDataHandler.write(wave2, OUTPUT_FILE);
	}
}
