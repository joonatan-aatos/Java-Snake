package visualizer;

public interface VisualizerToEngineInterface {
	
	public boolean connectButtonPressed(String ipAddressString, String portString);
	public void settingsButtonPressed();
	public void goBackButtonPressed();
	public void leaveGameButtonPressed();
	public void playSelectSound();
	public void mute(boolean mute);
}
