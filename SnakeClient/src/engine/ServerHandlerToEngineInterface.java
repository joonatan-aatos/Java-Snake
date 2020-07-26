package engine;

import java.awt.Color;

public interface ServerHandlerToEngineInterface {
	
	public void playerDied();
	public void disconnect();
	public void playerRejoined();
	public void gameStarted();
	public void setClientId(int id);
	public void fruitEaten();
	public void gameStarting();
	public void gameCancelled();
	public String getPlayerName();
	public Color getPlayerColor();
}
