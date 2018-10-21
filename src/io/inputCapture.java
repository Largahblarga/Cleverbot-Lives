package io;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

public class inputCapture {
	
	
	public Mixer.Info[] getMixers() {
		return AudioSystem.getMixerInfo();
	}
	
	public Mixer setInput(Mixer.Info info) {
		return AudioSystem.getMixer(info);
	}
	
	public Mixer setOutput(Mixer.Info info) {
		return AudioSystem.getMixer(info);
	}
}
