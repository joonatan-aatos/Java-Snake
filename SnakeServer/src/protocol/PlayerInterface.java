package protocol;

import java.awt.Color;

public interface PlayerInterface {
	
	public void directionInputReceived(String input);
	public void keyInputReceived(String input);
	public void setName(String name);
	public void setColor(Color color);
}
