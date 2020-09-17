package state;

import java.awt.Color;

public class GameInfo {

	// General info
	public static final String NAME = "Snake 1.0.2";
	public static final int WORLD_SIZE = 25;
	public static final boolean PRINT_GAMEINFO_TO_CONSOLE = false;
	public static final String LOCAL_HOST_STRING = "LocalHost";
	public static final Color DEFAULT_SNAKE_COLOR = new Color(49, 141, 224);
	
	// State info
	public static volatile int currentFPS;
	public static volatile int currentTPS;
	public static volatile int WIDTH = 600;
	public static volatile int HEIGHT = 600;
	
}
