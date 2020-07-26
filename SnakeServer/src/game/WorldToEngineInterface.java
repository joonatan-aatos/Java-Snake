package game;

import state.State;

public interface WorldToEngineInterface {
	
	public void playerDied(int playerKey);
	public void allPlayersAreReady();
	public void allPlayersAreNoLongerReady();
	public void startGame();
	public void stopGame();
	public void fruitEaten();
	public State getCurrentState();
}
