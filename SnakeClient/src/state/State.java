package state;

public enum State {
	
	Default(false),
	Settings(false),
	WaitingForGame(true),
	Playing(true),
	Dead(true),
	None(false);
	
	private boolean connected;
	
	private State(boolean connected) {
		
		this.connected = connected;
	}
	
	public boolean isConnected() {
		return this.connected;
	}
}
