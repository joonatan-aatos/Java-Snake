package state;

public class GameInfo {

	// General info
	public static final String NAME = "Snake Server";
	public static final int WIDTH = 700;
	public static final int HEIGHT = 700;
	
	// State info
	public static volatile int currentFPS;
	public static volatile int currentTPS;
	public static volatile int latencyMS;
	public static volatile State currentState;
	public static final boolean PRINT_GAMEINFO_TO_CONSOLE = false;
	public static final boolean DRAW_GAME = false;
	
	// Constant variables
	public static final int WORLD_SIZE = 25;
	public static final int TILE_SIZE = 10;
	
}
