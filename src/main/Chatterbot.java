package main;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.*;

import javax.sound.sampled.Mixer;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.AudioInputStream;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;

public class Chatterbot {
	private JFrame frame;
	private JLabel inputLabel, outputLabel, volumeLabel;
	private JComboBox<String> inputBox, outputBox;
	private JSlider volumeSlider;
	private JRadioButton powerSwitch;
	
	private Mixer.Info[] devices;
	private String[] deviceNames;
	
	private double silence_threshold = -70D;
	
	AudioFormat targetFormat, sourceFormat;
	
	SourceDataLine sourceDataLine;
	TargetDataLine targetDataLine;
	
	AudioDispatcher dispatcher;
	SoundRecorder processor;
	
	public Chatterbot() {
		initGUI();
	}

	private void initGUI() {
		//Get available audio devices
		devices = AudioSystem.getMixerInfo();
		deviceNames = new String[devices.length];
		for (int i = 0; i < devices.length; i++) {
			deviceNames[i] = devices[i].getName();
		}
		
		//Init window
		frame = new JFrame("Chatterbot");
		frame.setSize(640,480);
		frame.setLayout(new GridLayout(3,3));
		
		//Set up components
		inputLabel = new JLabel("Select input:",JLabel.CENTER);
		outputLabel = new JLabel("Select output:",JLabel.CENTER);
		volumeLabel = new JLabel("Set minimum volume",JLabel.CENTER);
		
		inputBox = new JComboBox<String>(deviceNames);
		inputBox.addActionListener(new BoxListener());
		inputBox.setActionCommand("Input");
		outputBox = new JComboBox<String>(deviceNames);
		outputBox.addActionListener(new BoxListener());
		outputBox.setActionCommand("Output");
		
		volumeSlider = new JSlider(JSlider.HORIZONTAL, -80, 0, -70);
		volumeSlider.addChangeListener(new SliderListener());
		volumeSlider.setMinorTickSpacing(2);
		volumeSlider.setMajorTickSpacing(10);
		volumeSlider.setPaintLabels(true);
		volumeSlider.setPaintTicks(true);
		
		powerSwitch = new JRadioButton("Enable");
		powerSwitch.addActionListener(new ButtonListener());
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				System.exit(0);
			}
		});
		
		//Add components to frame
		frame.add(inputLabel);
		frame.add(outputLabel);
		frame.add(volumeLabel);
		frame.add(inputBox);
		frame.add(outputBox);
		frame.add(volumeSlider);
		frame.add(powerSwitch);
		
		//Show frame
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		//Init stuff
		
		//Start the window
		Chatterbot bot = new Chatterbot();
		bot.start();

	}
	
	private void start() {
		// TODO Auto-generated method stub
		
		
		
	}

	private class BoxListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			//Determine which box was used
			DataLine.Info targetDataLineInfo = null;
			DataLine.Info sourceDataLineInfo = null;
			String command = e.getActionCommand();
			@SuppressWarnings("unchecked")
			JComboBox<String> comboBox = (JComboBox<String>)e.getSource();
			Mixer mixer = AudioSystem.getMixer(devices[comboBox.getSelectedIndex()]);
			
			//Then initialize its Mixer and data line
			if (command == "Input") {
				targetFormat = new AudioFormat(48000.0F, 16, 2, true, true);
				targetDataLineInfo = new DataLine.Info(TargetDataLine.class, targetFormat);
				if (!mixer.isLineSupported(targetDataLineInfo)) {
					System.out.println("ERROR: Selected input could not be acquired!");
					System.exit(0);
				}
			} else if (command == "Output") {
				sourceFormat = new AudioFormat(48000.0F, 16, 2, true, true);
				sourceDataLineInfo = new DataLine.Info(SourceDataLine.class, sourceFormat);
				if (!mixer.isLineSupported(sourceDataLineInfo)) {
					System.out.println("ERROR: Selected output could not be acquired!");
					System.exit(0);
				}
			}
			try {
				if (command == "Input") {
					targetDataLine = (TargetDataLine) mixer.getLine(targetDataLineInfo);
					targetDataLine.open(targetFormat);
				} else if (command == "Output") {
					sourceDataLine = (SourceDataLine) mixer.getLine(sourceDataLineInfo);
					sourceDataLine.open(sourceFormat);
				}
			} catch(LineUnavailableException error) {
				System.out.println(error);
				System.exit(0);
			}
			
		}
	}
	
	private class SliderListener implements ChangeListener{

		@Override
		public void stateChanged(ChangeEvent e) {
			// TODO Auto-generated method stub
			JSlider source = (JSlider)e.getSource();
			if (!source.getValueIsAdjusting()) {
				silence_threshold = Double.valueOf(source.getValue());
			}
			
		}
		
	}
	
	private class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			JRadioButton radioButton = (JRadioButton)e.getSource();
			try {
				if (radioButton.isSelected()) {
					//Turn on audio streams, disable controls
					sourceDataLine.open(sourceFormat);
					targetDataLine.open(targetFormat);
					inputBox.setEnabled(false);
					outputBox.setEnabled(false);
					volumeSlider.setEnabled(false);
					targetDataLine.start();
					//Start processing
					JVMAudioInputStream input = new JVMAudioInputStream(new AudioInputStream(targetDataLine));
					dispatcher = new AudioDispatcher(input,4096,2048);
					processor = new SoundRecorder(silence_threshold);
					dispatcher.addAudioProcessor(processor);
					new Thread(dispatcher, "Starting audio processing thread").start();;
				} else {
					//Turn off audio streams, enable controls
					sourceDataLine.close();
					targetDataLine.close();
					inputBox.setEnabled(true);
					outputBox.setEnabled(true);
					volumeSlider.setEnabled(true);
					dispatcher.removeAudioProcessor(processor);
				}
			} catch(LineUnavailableException error) {
				System.out.println(error);
				System.exit(0);
				
			}
			
		}
		
	}

}
