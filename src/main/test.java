package main;

import java.io.IOException;

public class test {
	
	public static void main(String[] args) throws IOException {
		
		String INPUT_FILE1 = "C:\\Users\\Julian\\Music\\~Sound Packs\\Fantom Claps\\fantclaps101(01).wav";
		String INPUT_FILE2 = "C:\\Users\\Julian\\Desktop\\wavtest.wav";
		String OUTPUT_FILE = "C:\\Users\\Julian\\Desktop\\output.wav";

        WavObj wave1 = WavDataHandler.read(INPUT_FILE1);
        WavObj wave2 = WavDataHandler.read(INPUT_FILE1);
        
        WavGen sinegen = new WavGen(wave2);
        WavObj wave3 = sinegen.siren(900, 10, 10);
        
        wave1.extend(3);
        wave1.trim(0, 0.1);
        wave1.repeat(40);
        wave1.delay(0.12, 0.5, 1);
        wave1.reverse();
        wave1.pitch(0.3);
        
        //wave2.volume(0.3);

        //wave.visual(50, 100);
        //wave.diff(2);

        wave2.trim(0, 0.1);
        wave2.repeat(50);
        
        wave2.mix(wave1, 0.4, true, 0);
        
        WavDataHandler.write(wave3, OUTPUT_FILE);
	}
}
