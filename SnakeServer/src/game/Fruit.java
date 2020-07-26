package game;

import java.util.List;

public class Fruit extends Sprite {
	
	public enum Type {
		
		Apple(0, 1, Player.DEFAULT_SPEED),
		Orange(1, 1, Player.DEFAULT_SPEED + 5),
		Banana(2, 3, Player.DEFAULT_SPEED - 5);
		
		private int typeIndex;
		private int growAmount;
		private int speedChange;
		
		private Type(int typeIndex, int growAmount, int speedChange) {
			
			this.typeIndex = typeIndex;
			this.growAmount = growAmount;
			this.speedChange = speedChange;
		}
		
		public int getTypeIndex() {
			return typeIndex;
		}
		
		public int getGrowAmount() {
			return growAmount;
		}
		
		public int getSpeedChange() {
			return speedChange;
		}
	}
	
	private Type type;
	
	public Fruit(int x, int y, Type type) {
		super(x, y);
		
		this.type = type;
	}
	
	@Override
	public void tick(List<Sprite> sprites, List<Sprite> spritesToBeRemoved) {
		return;
	}
	
	public Type getType() {
		return type;
	}

}
