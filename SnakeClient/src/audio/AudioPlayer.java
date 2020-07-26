package audio;

public class AudioPlayer {
	
	public AudioPlayer() {
		
	}
	
	public void playSound(Sound sound) {
		
		sound.play();
	}
	
	public void stopSound(Sound sound) {
		
		sound.stop();
	}
	
	public void loopSound(Sound sound) {
		
		sound.loop();
	}
	
	public void mute(boolean mute) {
		
		Sound.Death_Sound.mute(mute);
		Sound.Main_Theme.mute(mute);
		Sound.Eat_Sound.mute(mute);
		Sound.Select_Sound.mute(mute);
		Sound.Beep_Sound.mute(mute);
	}
}
