package game;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import protocol.PlayerInterface;
import state.GameInfo;
import state.State;

public class Player extends Sprite implements PlayerInterface {

	public static final int DEFAULT_SPEED = 20;
	
	private ArrayList<int[]> tail;		// Stores all tail coordinates
	private int length;					// Determines how long the tail should be
	private int[] lastTailPoint;		// Stores the coordinates of the last point that was removed from the tail
	
	private boolean readyToStart;
	private boolean spectating;
	private Direction currentDirection;
	private Direction desiredDirection;
	private int speed;
	private int speedCounter = 0;
	private int speedChangeCounter = 0;
	private int id;
	private Color color;
	private String name;

	public Player(int x, int y, int id) {
		
		super(x, y);
		
		this.id = id;
		tail = new ArrayList<int[]>();
		lastTailPoint = null;
		length = 10;
		grow();
		
		currentDirection = desiredDirection = Direction.None;
		speed = DEFAULT_SPEED;
		
		readyToStart = false;
		spectating = false;
	}

	@Override
	public void tick(List<Sprite> sprites, List<Sprite> spritesToBeRemoved) {
		
		if(speedChangeCounter > 0)
			speedChangeCounter--;
		if(speedChangeCounter == 1) {
			speed = DEFAULT_SPEED;
		}
		
		if(isSpectating())
			die();
		
		if(isDead)
			return;
		
		speedCounter += speed;
		while(speedCounter >= 10) {
			
			move();
			speedCounter -= 10;
			
			if(xPos % GameInfo.TILE_SIZE == 0 && yPos % GameInfo.TILE_SIZE == 0) {
				currentDirection = desiredDirection;
			}
			
			if(tail.size() < length) {
				grow();
			}
			else if(tail.size() > 0) {
				if(grow())
					shrink();
			}
			
			checkForPlayerDeath(sprites);
			checkIfOnFruit(sprites, spritesToBeRemoved);			
		}
	}
	
	private void move() {
		
		xPos += currentDirection.getdx();
		yPos += currentDirection.getdy();
	}
	
	private void checkForPlayerDeath(List<Sprite> sprites) {
		
		if(xPos < 0 || xPos > (GameInfo.WORLD_SIZE-1) * GameInfo.TILE_SIZE ||
				yPos < 0 || yPos > (GameInfo.WORLD_SIZE-1)*GameInfo.TILE_SIZE) {
			isDead = true;
		}
		
		for(Sprite s : sprites) {
			if(tail.size() == 0)
				break;
			
			if(s instanceof Player) {
				Player p = (Player) s;
				for(int[] spriteCoords : p.getTail()) {
					if(currentDirection == Direction.Left || currentDirection == Direction.Down) {
						if(spriteCoords[0] == Math.floor(xPos / GameInfo.TILE_SIZE) && spriteCoords[1] == Math.floor(yPos / GameInfo.TILE_SIZE) 
								&& !spriteCoords.equals(tail.get(tail.size()-1))) {
							die();
							break;
						}
					} else if(currentDirection == Direction.Right || currentDirection == Direction.Up) {
						if(spriteCoords[0] == Math.floor((xPos+9) / GameInfo.TILE_SIZE) && spriteCoords[1] == Math.floor((yPos+9) / GameInfo.TILE_SIZE) 
								&& !spriteCoords.equals(tail.get(tail.size()-1))) {
							die();
							break;
						}
					}
				}
			}
			if(isDead)
				break;
		}
	}
	
	private void checkIfOnFruit(List<Sprite> sprites, List<Sprite> spritesToBeRemoved) {
		
		for(Sprite s : sprites) {
			if(s instanceof Fruit && !spritesToBeRemoved.contains(s)) {
				
				Fruit fruit = (Fruit) s;
				
				if(currentDirection == Direction.Left || currentDirection == Direction.Down) {
					if(fruit.getxPos() == Math.floor(xPos / GameInfo.TILE_SIZE) && fruit.getyPos() == Math.floor(yPos / GameInfo.TILE_SIZE)) {
						// Apple at players position
						eatFruit(fruit, spritesToBeRemoved);
						break;
					}
				} else if(currentDirection == Direction.Right || currentDirection == Direction.Up) {
					if(fruit.getxPos() == Math.floor((xPos+9) / GameInfo.TILE_SIZE) && fruit.getyPos() == Math.floor((yPos+9) / GameInfo.TILE_SIZE)) {
						// Apple at players position
						eatFruit(fruit, spritesToBeRemoved);
						break;
					}
				}
			}
		}
	}
	
	private void eatFruit(Fruit fruit, List<Sprite> spritesToBeRemoved) {
		
		length += fruit.getType().getGrowAmount();
		if(fruit.getType().getSpeedChange() != 20) {
			changeSpeed(fruit.getType().getSpeedChange());
		}
		grow();
		spritesToBeRemoved.add(fruit);
	}
	
	public void die() {
		
		isDead = true;
		
		xPos = (int) Math.round((double)xPos / (double)GameInfo.TILE_SIZE) * GameInfo.TILE_SIZE;
		yPos = (int) Math.round((double)yPos / (double)GameInfo.TILE_SIZE) * GameInfo.TILE_SIZE;
	}
	
	public boolean readyToStart() {
		return readyToStart;
	}
	
	public boolean isSpectating() {
		return spectating;
	}
	
	public void startSpectating() {
		
		if(!spectating) {
			spectating = true;
			isDead = false;
		}
	}
	
	// This function will return true if the snake grew
	/* The reason this function is so large is because
	 * the position of the snake can be between two tiles.
	 * This makes this function quite complicated but you
	 * sholdn't worry because the function does what
	 * it is supposed to do!
	 */
	private boolean grow() {
		
		int[][] gameCoords = {
				{
					(int) Math.floor((double) (xPos) / (double)GameInfo.TILE_SIZE),
					(int) Math.floor((double) (yPos) / (double)GameInfo.TILE_SIZE),
				},
				{
					(int) Math.floor((double) (xPos + 9) / (double)GameInfo.TILE_SIZE),
					(int) Math.floor((double) (yPos + 9) / (double)GameInfo.TILE_SIZE)
				}
		};

		for(int[] point : tail) {
			if(currentDirection == Direction.Right || currentDirection == Direction.Up || currentDirection == Direction.None) {
				if(point[0] == gameCoords[1][0] && point[1] == gameCoords[1][1]) {
					// The tail-ArrayList already contains this point
					return false;
				}
			}
			else if(currentDirection == Direction.Left || currentDirection == Direction.Down) {
				if(point[0] == gameCoords[0][0] && point[1] == gameCoords[0][1]) {
					// The tail-ArrayList already contains this point
					return false;
				}				
			}
		}
		
		if(currentDirection == Direction.Right || currentDirection == Direction.Up || currentDirection == Direction.None)
			tail.add(gameCoords[1]);
		else
			tail.add(gameCoords[0]);
		return true;
	}

	private void shrink() {
		
		lastTailPoint = tail.get(0);
		tail.remove(0);
	}
	
	private void changeSpeed(int newSpeed) {
		
		speed = newSpeed;
		speedChangeCounter = 30*5;
	}
	
	protected Direction getDesiredDirection() {
		
		return this.desiredDirection;
	}
	
	protected void setDesiredDirection(Direction direction) {
		
		this.desiredDirection = direction;
	}
	
	public Direction getTailDirection() {
		
		Direction direction = Direction.None;
		
		if(lastTailPoint != null) {
			
			int dx = tail.get(0)[0] - lastTailPoint[0];
			int dy = tail.get(0)[1] - lastTailPoint[1];
			
			if(dx == 1)
				direction = Direction.Right;
			else if(dx == -1)
				direction = Direction.Left;
			else if(dy == 1)
				direction = Direction.Up;
			else if(dy == -1)
				direction = Direction.Down;
		}
		
		return direction;
	}
	
	public ArrayList<int[]> getTail() {
		return tail;
	}
	
	public int getDesiredTailLength() {
		return this.length;
	}
	
	public Direction getCurrentDirection() {
		return currentDirection;
	}
	
	public int getId() {
		
		return this.id;
	}

	public String getName() {
		
		return this.name;
	}
	
	public Color getColor() {
		
		return this.color;
	}
	
	@Override
	public void directionInputReceived(String input) {
		
		if(input.equals("#UP")) {
			if(currentDirection != Direction.Down)
				desiredDirection = Direction.Up;
		} else if(input.equals("#DOWN")) {
			if(currentDirection != Direction.Up)
				desiredDirection = Direction.Down;
		} else if(input.equals("#LEFT")) {
			if(currentDirection != Direction.Right)
				desiredDirection = Direction.Left;
		} else if(input.equals("#RIGHT")) {
			if(currentDirection != Direction.Left)
				desiredDirection = Direction.Right;
		}
	}
	
	@Override
	public void keyInputReceived(String input) {
		if(input.equals("&" + Integer.toString(KeyEvent.VK_SPACE)) && GameInfo.currentState != State.GameRunning && !isDead && !spectating) {
			readyToStart = !readyToStart;
		}
	}

	@Override
	public void setName(String name) {
		
		this.name = name;
	}

	@Override
	public void setColor(Color color) {
		
		this.color = color;
	}
}
