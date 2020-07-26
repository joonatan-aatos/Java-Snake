package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import state.GameInfo;
import state.State;
import timer.Timer;
import timer.TimerInterface;
import world_state.WorldState;

public class World implements TimerInterface {
	
	private Random random = new Random();
	
	private List<Sprite> sprites;
	private List<Sprite> spritesToBeRemoved;
	private WorldToEngineInterface engineInterface;
	
	private String countDownString;
	
	private final int[][] spawnLocations = {
			{ 6, GameInfo.WORLD_SIZE-1 - 6 },
			{ GameInfo.WORLD_SIZE-1 - 6, GameInfo.WORLD_SIZE-1 - 6 },
			{ 6, 6 },
			{ GameInfo.WORLD_SIZE-1 - 6, 6 }
	};
	
	private boolean gameStarted;
	private boolean allPlayersAreReady;
	
	public World(WorldToEngineInterface engine) {
		
		this.engineInterface = engine;
		init();
	}
	
	private void init() {
		
		sprites = Collections.synchronizedList(new ArrayList<Sprite>());
		spritesToBeRemoved = Collections.synchronizedList(new ArrayList<Sprite>());
		gameStarted = false;
		allPlayersAreReady = false;
		countDownString = "";
	}
	
	public void tick() {
		
		checkForSpectators();
		
		if(!allPlayersAreReady && checkIfAllPlayersAreReady()) {
			
			allPlayersAreReady = true;
			engineInterface.allPlayersAreReady();	
		}
		else if(allPlayersAreReady && !checkIfAllPlayersAreReady()) {
			
			allPlayersAreReady = false;
			engineInterface.allPlayersAreNoLongerReady();
		}
		
		if(gameStarted) {
			for(Sprite sprite : sprites) {
				
				sprite.tick(sprites, spritesToBeRemoved);
			}
			// SpritesToBeRemoved is used to avoid java.util.ConcurrentModificationException 
			// when a sprite is removed from sprites while it is being iterated over. 
			for(Sprite sprite : spritesToBeRemoved) {
				
				if(sprite instanceof Fruit) {
					engineInterface.fruitEaten();
				}
				sprites.remove(sprite);
			}
			spritesToBeRemoved.clear();
			
			checkForSpriteDeath();
			if(random.nextInt(80) == 0 && getAppleCount() < 5)
				sprites.add(createFruit());
		}
	}
	
	public void startGame() {
		
		gameStarted = true;
		
		for(Sprite s : sprites) {
			if(s instanceof Player) {
				Player player = (Player) s;
				if(player.getDesiredDirection() == Direction.None)
					player.setDesiredDirection(Direction.Up);
			}
		}
	}
	
	private boolean checkIfAllPlayersAreReady() {
		
		if(sprites.size() == 0) {
			stopGame();
			engineInterface.stopGame();
			return false;
		}
		else {
			boolean playersInList = false;
			for(Sprite s : sprites) {
				if(s instanceof Player) {
					playersInList = true;
					break;
				}
			}
			if(!playersInList) {
				stopGame();
				return false;
			}
		}
		if(GameInfo.currentState == State.WaitingForPlayers || !countDownString.equals("0")) {
			for(Sprite s : sprites) {
				if(s instanceof Player) {
					Player player = (Player) s;
					if(!player.readyToStart() && !player.isSpectating() && !player.isDead())
						return false;
				}
			}
		}
		return true;
	}
	
	private void stopGame() {
		
		if(gameStarted) {
			gameStarted = false;
			
			boolean playersInList = false;
			for(Sprite s : sprites) {
				if(s instanceof Player) {
					playersInList = true;
					break;
				}
			}
			if(!playersInList) {
				sprites.clear();
			}
		}
	}
	
	private void checkForSpriteDeath() {
		
		for(int i = 0; i < sprites.size(); i++) {
			
			if(sprites.get(i).isDead()) {
				killSprite(sprites.get(i));
			}
		}
	}
	
	private Fruit createFruit() {
		
		
		int[] fruitCoords;
		
		ArrayList<int[]> fruitCoordsOptions = new ArrayList<int[]>();
		for(int i = 0; i < GameInfo.WORLD_SIZE; i++) {
			for(int j = 0; j < GameInfo.WORLD_SIZE; j++) {
				
				if(!spriteAt(j, i)) {
					int[] _coords = {j, i};
					fruitCoordsOptions.add(_coords);
				}
			}
		}
		
		fruitCoords = fruitCoordsOptions.get(random.nextInt(fruitCoordsOptions.size()));
		Fruit fruit;
		
		int rand = random.nextInt(10);
		if(rand > 3) {
			// 6/10 chance
			fruit = new Fruit(fruitCoords[0], fruitCoords[1], Fruit.Type.Apple);
		}
		else if(rand > 1) {
			// 2/10 chance
			fruit = new Fruit(fruitCoords[0], fruitCoords[1], Fruit.Type.Orange);
		}
		else {
			// 2/10 chance
			fruit = new Fruit(fruitCoords[0], fruitCoords[1], Fruit.Type.Banana);			
		}
		
		return fruit;
	}
	
	private void checkForSpectators() {
		
		for(int i = 0; i < sprites.size(); i++) {
			
			if(sprites.get(i) instanceof Player) {
				
				Player player = (Player) sprites.get(i);
				if(player.getxPos() == -1 && player.getyPos() == -1 && !player.isDead) {
					
					killSprite(player);
				}
			}
		}
	}
	
	public Player createNewPlayer(int playerId, boolean randomSpawnLocation) {
		
		// 4 is the max player count
		
		int[] spawnLocation = null;
		
		if(randomSpawnLocation) {
			ArrayList<int[]> shuffledSpawnLocations = new ArrayList<int[]>(Arrays.asList(spawnLocations));
			Collections.shuffle(shuffledSpawnLocations);
			for(int[] sl : shuffledSpawnLocations) {
				if(!spriteAt(sl[0] * GameInfo.TILE_SIZE, sl[1] * GameInfo.TILE_SIZE)) {				
					spawnLocation = sl;
					break;
				}				
			}
		}
		else {
			for(int i = 0; i < 4; i++) {
				if(!spriteAt(spawnLocations[i][0] * GameInfo.TILE_SIZE, spawnLocations[i][1] * GameInfo.TILE_SIZE)) {				
					spawnLocation = spawnLocations[i];
					break;
				}
			}
		}
		
		Player _player = null;
		
		if(spawnLocation == null) {
			// Create a spectator
			_player = new Player(-1, -1, playerId);
			_player.startSpectating();
		}
		else {
			_player = new Player(spawnLocation[0] * GameInfo.TILE_SIZE, spawnLocation[1] * GameInfo.TILE_SIZE, playerId);
		}
		sprites.add(_player);
		
		return _player;
	}
	
	public int getPlayerIndex(Player player) {
		
		return sprites.indexOf(player);
	}
	
	public boolean playerCanJoin() {
		
		int players = 0;
		
		for(Sprite sprite : sprites) {
			if(sprite instanceof Player) {
				players++;
				if(players >= 4)
					return false;
			}
		}
		return true;
	}
	
	private void killSprite(Sprite sprite) {
		
		if(sprite instanceof Player) {
			Player player = (Player) sprite;
			if(!player.isDead())
				player.die();
			engineInterface.playerDied(player.getId());
		}
		else {
			sprites.remove(sprite);
		}
	}
	
	public int getPlayerCount() {
		int count = 0;
		for(Sprite s : sprites) {
			if(s instanceof Player)
				count++;
		}
		return count;
	}
	
	public int getAppleCount() {
		int count = 0;
		for(Sprite s : sprites) {
			if( s instanceof Fruit)
				count++;
		}
		return count;
	}
	
	private boolean spriteAt(int x, int y) {
		
		for(Sprite s : sprites) {
			
			if(s instanceof Player) {
				
				Player player = (Player) s;
				for(int[] tailCoords : player.getTail()) {
					if(tailCoords[0] == x && tailCoords[1] == y) {
						return true;
					}
				}
			}
			
			if(s.getxPos() == x && s.getyPos() == y)
				return true;
		
		}
		return false;
	}
	
	public WorldState generateWorldState() {
		
		WorldState state = new WorldState();
		
		state.gameStarted = this.gameStarted;
		state.countDownString = this.countDownString;
		
		for(Sprite sprite : sprites) {
			if(sprite instanceof Player) {
				
				Player player = (Player) sprite;
				
				WorldState.PlayerState playerState = new WorldState.PlayerState(player.getxPos(), player.getyPos());
				
				ArrayList<int[]> list = new ArrayList<int[]>();
				for(int j = 0; j < player.getTail().size(); j++) {
					int[] array = new int[2];
					array[0] = player.getTail().get(j)[0];
					array[1] = player.getTail().get(j)[1];
					list.add(array);
				}
				playerState.name = player.getName();
				playerState.clientId = player.getId();
				playerState.color = player.getColor();
				playerState.tail = list;
				playerState.tailDirection = player.getTailDirection().toString();
				playerState.currentDirection = player.getCurrentDirection().toString();
				playerState.readyToStart = player.readyToStart();
				playerState.animateTail = !(player.getDesiredTailLength() > player.getTail().size());
				state.sprites.add(playerState);
			}
			else if(sprite instanceof Fruit) {
				
				Fruit apple = (Fruit) sprite;
				
				WorldState.FruitState appleState = new WorldState.FruitState(apple.xPos, apple.yPos);
				appleState.type = apple.getType().getTypeIndex();
				
				state.sprites.add(appleState);
			}
			else {
				
				state.sprites.add(new WorldState.SpriteState(sprite.getxPos(), sprite.getyPos()));
			}
			
		}
		
		return state;
	}
	
	public List<Sprite> getSprites() {
		return this.sprites;
	}
	
	public void removePlayer(Player player) {
		if(sprites.contains(player))
			sprites.remove(player);
	}
	
	public void startCountdownTimer() {
		
		Timer.startTimer(this, 1000, "Countdown: 3");
		countDownString = "3";
	}
	
	public void cancelCountdownTimer() {
		
		countDownString = "";
		Timer.stopAllTimers();
	}
	
	@Override
	public void timerEnded(Timer t) {
		
		if(engineInterface.getCurrentState() == State.StartingGame) {
			if(t.getMessage() == "Countdown: 3") {
				Timer.startTimer(this, 1000, "Countdown: 2");
				countDownString = "2";
			}
			else if(t.getMessage() == "Countdown: 2") {
				Timer.startTimer(this, 1000, "Countdown: 1");
				countDownString = "1";
			}
			else if(t.getMessage() == "Countdown: 1") {
				countDownString = "";
				engineInterface.startGame();
			}
		}
	}
}
