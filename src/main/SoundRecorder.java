package main;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.SilenceDetector;

public class SoundRecorder extends SilenceDetector {
	
	public SoundRecorder(double sound_threshold) {
		super(sound_threshold, false);
		System.out.println("SoundRecorder started.");
	}
	
	@Override
	public boolean process(AudioEvent audioEvent) {
		boolean isSilence = isSilence(audioEvent.getFloatBuffer());
		if (!isSilence) {
			System.out.println("I  C A N  H E A R  Y O U");
		}
		return true;
	}

}
