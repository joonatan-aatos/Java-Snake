package world_state;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;

public class WorldState implements Serializable {
	
	public boolean gameStarted;
	public String countDownString;
	
	public ArrayList<SpriteState> sprites;
	
	public WorldState() {
		sprites = new ArrayList<SpriteState>();
	}
	
	public static class PlayerState extends SpriteState {
		
		public boolean readyToStart;
		public boolean animateTail;
		public ArrayList<int[]> tail;
		public String currentDirection;
		public String tailDirection;
		public String name;
		public int clientId;
		public Color color;
		
		public PlayerState(int xPos, int yPos) {	
			super(xPos, yPos);
		}
	}
	
	public static class FruitState extends SpriteState {
		
		public int type;
		
		public FruitState(int xPos, int yPos) {
			super(xPos, yPos);
		}
	}
	
	public static class SpriteState implements Serializable {
		
		public int xPos;
		public int yPos;
		
		public SpriteState(int xPos, int yPos) {
			this.xPos = xPos;
			this.yPos = yPos;
		}
	}
}
