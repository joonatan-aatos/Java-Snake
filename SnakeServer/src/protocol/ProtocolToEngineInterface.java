package protocol;

import game.Player;

public interface ProtocolToEngineInterface {
	
	public void socketClosed(int playerId);
	public void kickPlayer(int playerId);
	public void playerJoinedTheGame(String playerName);
	public Player rejoin(int playerId);
}
