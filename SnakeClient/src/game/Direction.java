package game;

public enum Direction {
	Up(0, 1, "UP"),
	Down(0, -1, "DOWN"),
	Left(-1, 0, "LEFT"),
	Right(1, 0, "RIGHT"),
	None(0, 0, "NONE");
	
	private int dx, dy;
	private String name;
	
	private Direction(int x, int y, String name) {
		this.dx = x;
		this.dy = y;
		this.name = name;
	}
	
	public int getdx() {
		return dx;
	}
	
	public int getdy() {
		return dy;
	}
	
	public boolean isHorizontal() {
		return dx != 0;
	}
	
	public boolean isVertical() {
		return dy != 0;
	}
	
	public static Direction getDirectionFromString(String direction) {
		if(direction.equalsIgnoreCase("UP"))
			return Direction.Up;
		else if(direction.equalsIgnoreCase("DOWN"))
			return Direction.Down;
		else if(direction.equalsIgnoreCase("LEFT"))
			return Direction.Left;
		else if(direction.equalsIgnoreCase("RIGHT"))
			return Direction.Right;
		return Direction.None;
	}
	@Override
	public String toString() {
		return name;
	}
}
