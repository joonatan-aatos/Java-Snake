package engine;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import game.Player;
import game.World;
import game.WorldToEngineInterface;
import protocol.ClientHandler;
import protocol.ProtocolToEngineInterface;
import protocol.ClientHandler.Info;
import server.SocketServer;
import server.SocketServerToEngineInterface;
import state.GameInfo;
import state.State;
import timer.Timer;
import visualizer.MyCanvas;
import visualizer.Visualizer;
import world_state.WorldState;

public class Engine implements SocketServerToEngineInterface, ProtocolToEngineInterface, WorldToEngineInterface {
	
	private boolean running;
	private int desiredFPS = 60;		//how often the game renders
	private int desiredTicksPS = 30;	//how often the game updates
	private int timeOutTimer = 0;

	private Visualizer visualizer;
	private World world;
	private SocketServer server;
	private State state;
	
	private int playerIdCounter;
	private Map<Integer, ClientHandler> clientHandlerMap;
	private Map<Integer, Player> playerMap;
	
	public Engine() {
		
//		visualizer = new Visualizer();
		world = new World(this);
		
		clientHandlerMap = Collections.synchronizedMap(new HashMap<>());
		playerMap = Collections.synchronizedMap(new HashMap<>());
		playerIdCounter = 0;
		
		server = new SocketServer(this, 5555);
		
		server.start();
		
		if(GameInfo.DRAW_GAME)
			visualizer.start();
	}
	
	public void run() {
		
		running = true;
		state = State.WaitingForPlayers;
		
		int fps = 0;	//how often the game renders
		int tick = 0;	//how often the game updates
		
		double nsPerTick = 1000000000 / desiredTicksPS;
		double nsPerFrame = 1000000000 / desiredFPS;
		double then = System.nanoTime();
		double now;
		double unprocessedTicks = 0;
		double unprocessedFrames = 0;
		double fpsTimer = System.currentTimeMillis();
		
		while(running) {
			
			now = System.nanoTime();
			unprocessedTicks += (now - then) / nsPerTick;
			unprocessedFrames += (now - then) / nsPerFrame;
			then = now;
			while(unprocessedTicks >= 1) {
				
				world.tick();
				Timer.updateTimers();
				
				if(GameInfo.currentState != state)
					GameInfo.currentState = state;
				
				tick++;
				unprocessedTicks--;
			}
			if(unprocessedFrames >= 1) {
				
				if(GameInfo.DRAW_GAME)
					visualizer.update(world);
				
				try {
					sendWorldState();
					
				} catch(ConcurrentModificationException e) {
					
					e.printStackTrace();
				}
				
				fps++;
				unprocessedFrames -= Math.floor(unprocessedFrames);
			}
			if(System.currentTimeMillis() - fpsTimer >= 1000) {
				
				if(clientHandlerMap.values().size() > 0) {
					timeOutTimer = 0;
				}
				else  {
					timeOutTimer++;
				}
				if(timeOutTimer >= 300) {
					// 5 Minutes has passed
					timeOut();
				}
				
				if(GameInfo.PRINT_GAMEINFO_TO_CONSOLE) {
					System.out.println("Fps: " + fps + " Ticks: " + tick);
				}
				fpsTimer = System.currentTimeMillis();
				fps = 0;
				tick = 0;
			}
			
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	private void timeOut() {
		
		System.out.println("Server timed out");
		System.exit(0);
	}
	
	private void sendWorldState() throws ConcurrentModificationException {
		
		WorldState worldState = world.generateWorldState();
		
		for(ClientHandler client : clientHandlerMap.values()) {
			
			try {
				if(!client.getSocket().isClosed())
					client.sendWorldState(worldState);
			} catch (SocketException e) {
				// If this exception is thrown the socket in the client has been closed
				// System.out.println("Error in Engine.sendWorldState(): Client disconnected!");
				
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	@Override
	public void kickPlayer(int playerId) {
		
		try {
			clientHandlerMap.get(playerId).disconnect();
		} catch(NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void clientConnected(Socket s) {
		
		// PlayerIdCounter is a unique key for all players
		Player player = world.createNewPlayer(playerIdCounter, state != State.WaitingForPlayers);
		// Save the player into a map
		playerMap.put(playerIdCounter, player);
		
		// Create a ClientHandler for the player
		ClientHandler clientHandler = new ClientHandler(s, playerIdCounter, player, this);
		// Start the ClientHandler
		new Thread(clientHandler).start();
		// Save the ClientHandler into a map
		clientHandlerMap.put(playerIdCounter, clientHandler);
		
		if(state == State.GameRunning) {
			clientHandler.sendInfo(Info.GameStarted);
		}
		
		// Increase the playerIdCounter so that it's different for the next client that connects
		playerIdCounter++;
	}
	
	@Override
	public void socketClosed(int playerId) {
		
		System.out.printf("%s left the game\n", clientHandlerMap.get(playerId).getPlayerName());
		
		clientHandlerMap.remove(playerId);
		world.removePlayer(playerMap.get(playerId));
		playerMap.remove(playerId);
		
	}
	
	@Override
	public Player rejoin(int playerId) {
		
		if(playerMap.get(playerId) == null)
			return null;
		
		if(playerMap.get(playerId).isSpectating() && world.playerCanJoin()) {
			
			world.removePlayer(playerMap.get(playerId));
			playerMap.remove(playerId);
			
			Player player = world.createNewPlayer(playerId, state != State.WaitingForPlayers);
			playerMap.put(playerId, player);
			clientHandlerMap.get(playerId).sendInfo(Info.PlayerRejoined);
			
			if(state == State.GameRunning) {
				clientHandlerMap.get(playerId).sendInfo(Info.GameStarted);
			}
			
			return player;
		}
		
		return playerMap.get(playerId);
	}
	
	@Override
	public void playerJoinedTheGame(String playerName) {
		
		System.out.printf("%s joined the game.\n", playerName);
	}
	
	@Override
	public void playerDied(int playerKey) {
		
		System.out.printf("%s died\n", playerMap.get(playerKey).getName());
		
		playerMap.get(playerKey).startSpectating();
		world.removePlayer(playerMap.get(playerKey));
		
		ClientHandler ch = clientHandlerMap.get(playerKey);
		ch.sendInfo(Info.PlayerDied);
	}
	
	@Override
	public void allPlayersAreReady() {

		if(state == State.GameRunning)
			return;
		
		System.out.println("Starting game!");
		state = State.StartingGame;
		world.startCountdownTimer();
		
		for(ClientHandler client : clientHandlerMap.values()) {

			if(!client.getSocket().isClosed())
				client.sendInfo(Info.GameStarting);
		}
		
	}

	@Override
	public void allPlayersAreNoLongerReady() {
		
		if(state == State.GameRunning)
			return;
		
		state = State.WaitingForPlayers;
		world.cancelCountdownTimer();

		for(ClientHandler client : clientHandlerMap.values()) {

			if(!client.getSocket().isClosed())
				client.sendInfo(Info.GameCancelled);
		}
	}
	
	@Override
	public void startGame() {
		
		state = State.GameRunning;
		world.startGame();
		
		for(ClientHandler client : clientHandlerMap.values()) {

			if(!client.getSocket().isClosed())
				client.sendInfo(Info.GameStarted);
		}
	}
	
	@Override
	public void stopGame() {
		
		state = State.WaitingForPlayers;
		
		for(ClientHandler client : clientHandlerMap.values()) {

			if(!client.getSocket().isClosed())
				client.sendInfo(Info.GameCancelled);
		}
	}
	
	@Override
	public void fruitEaten() {

		for(ClientHandler client : clientHandlerMap.values()) {

			if(!client.getSocket().isClosed())
				client.sendInfo(Info.FruitEaten);
		}
	}
	
	@Override
	public State getCurrentState() {
		
		return state;
	}

}
