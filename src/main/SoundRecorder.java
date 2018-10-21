package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.SilenceDetector;

//I'm basically bodging together SilenceDetector and WaveformWriter
//in order to only write a WAV file when speech is detected
//HARDCODING FILENAME TO "input.wav"
public class SoundRecorder extends SilenceDetector {
	private final AudioFormat format;
	private final File rawOutputFile;
	private final String fileName = "input.wav";
	private FileOutputStream rawOutputStream;
	
	private int byteOverlap, byteStepSize;
	int ticks = 60;
	
	public SoundRecorder(final AudioFormat format, double sound_threshold) {
		super(sound_threshold, false);
		this.format = format;
		
		//tempfile
		this.rawOutputFile = new File("C:\\temp\\", new Random().nextInt() + "out.raw");
		System.out.println("SoundRecorder started.");
		
		try {
			this.rawOutputStream = new FileOutputStream(rawOutputFile);
		} catch (FileNotFoundException e) {
			String message;
			message = String.format("Could not write to temporary RAW file %1s: %2s", rawOutputFile.getAbsolutePath(), e.getMessage());
			System.out.println(message);
		}
	}
	
	@Override
	public boolean process(AudioEvent audioEvent) {
		boolean isSilence = isSilence(audioEvent.getFloatBuffer());
		//boolean isSilence = true;
		if (ticks > 0) {
			this.byteOverlap = audioEvent.getOverlap() * format.getFrameSize();
			this.byteStepSize = audioEvent.getBufferSize() * format.getFrameSize() - byteOverlap;
			try {
				//float[] temp = audioEvent.getFloatBuffer();
				byte[] buffer = audioEvent.getByteBuffer();
				//for (byte stuff : buffer) {
					//stuff = stuff / 2;
				//}
				rawOutputStream.write(buffer, byteOverlap, byteStepSize);
			} catch (IOException e) {
				System.out.println(String.format("Could not write to temporary RAW file %1s: %2s", rawOutputFile.getAbsolutePath(), e.getMessage()));
			}
		}
		if (!isSilence) {
			//System.out.println("I  C A N  H E A R  Y O U");
			//Start recording if necessary
			ticks = 30;
			
		} else {
			ticks--;
			System.out.println(ticks);
			if (ticks == 0) {
				export();
				return false;
			}
		}
		return false;
	}
	
	public void export() {
		File out = new File(fileName);
		try {
			//stream the raw file
			final FileInputStream inputStream = new FileInputStream(rawOutputFile);
			long lengthInSamples = rawOutputFile.length() / format.getFrameSize();
			final AudioInputStream audioInputStream;
			//create an audio stream form the raw file in the specified format
			audioInputStream = new AudioInputStream(inputStream, format,lengthInSamples);
			//stream this to the out file
			final FileOutputStream fos = new FileOutputStream(out);
			//stream all the bytes to the output stream
			AudioSystem.write(audioInputStream,AudioFileFormat.Type.WAVE,fos);
			//cleanup
			fos.close();
			audioInputStream.close();
			inputStream.close();
			rawOutputStream.close();
			rawOutputFile.delete();		
			System.out.println(fileName);
			this.rawOutputStream = new FileOutputStream(rawOutputFile);
		} catch (IOException e) {
			String message;
			message = String.format("Error writing the WAV file %1s: %2s", out.getAbsolutePath(), e.getMessage());
			System.out.println(message);
		}
	}

}
