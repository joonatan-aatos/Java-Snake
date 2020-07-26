package game;

import java.util.List;

public abstract class Sprite {

	protected int xPos, yPos;
	
	protected boolean isDead = false;
	
	public Sprite(int x, int y) {
		
		xPos = x;
		yPos = y;
	}

	public abstract void tick(List<Sprite> sprites, List<Sprite> spritesToBeRemoved);

	public int getxPos() {
		return xPos;
	}

	public int getyPos() {
		return yPos;
	}
	
	public boolean isDead() {
		return isDead;
	}
}
