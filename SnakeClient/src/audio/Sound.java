package audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;

public enum Sound {
	
	Select_Sound("/audio/Pop.wav"),
	Eat_Sound("/audio/Snake_Eat_Sound.wav"),
	Death_Sound("/audio/Snake_Death_Sound.wav"),
	Main_Theme("/audio/Snake_Theme.wav"),
	Beep_Sound("/audio/Snake_Beep.wav");
	
	private String path;
	private Clip clip;
	private BooleanControl muteControl;
	
	private Sound(String path) {
		
		this.path = path;

		try {
			
			InputStream stream = this.getClass().getResourceAsStream(path);
			BufferedInputStream bufferedStream = new BufferedInputStream(stream);
			AudioInputStream audioStream;
			audioStream = AudioSystem.getAudioInputStream(bufferedStream);
			clip = AudioSystem.getClip();
			clip.open(audioStream);
			
			muteControl = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
			
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	protected void play() {
		
		if(clip.isActive()) {
			stop();
		}
		clip.setMicrosecondPosition(0);
		clip.start();
	}
	
	protected void stop() {
		
		clip.stop();
		clip.flush();
	}
	
	protected void loop() {
		
		clip.setMicrosecondPosition(0);
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	protected void mute(boolean mute) {
		
		muteControl.setValue(mute);
	}
	
	protected Clip getClip() {
		
		return clip;
	}
}
