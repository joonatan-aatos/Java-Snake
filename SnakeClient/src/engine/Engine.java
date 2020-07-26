package engine;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import audio.AudioPlayer;
import audio.Sound;
import state.GameInfo;
import state.State;
import timer.Timer;
import visualizer.Visualizer;
import visualizer.VisualizerToEngineInterface;
import world_state.WorldState;

public class Engine implements KeyListener, VisualizerToEngineInterface, ServerHandlerToEngineInterface {
	
	private ArrayList<Integer> keysPressed = new ArrayList<Integer>();
	
	private boolean running;
	private int desiredFPS = 30;		//how often the game renders
	private int desiredTicksPS = 30;	//how often the game updates
	
	private State state;
	private boolean paused;
	private boolean typing;
	private boolean showNames;
	private boolean audioMuted;
	private int clientId;	// Player index in sprites -ArrayList
	
	private Visualizer visualizer;
	private ServerHandler serverHandler;
	private AudioPlayer audioPlayer;
	private UserInput userinput;
	private WorldState worldState;
	
	public Engine() {

		visualizer = new Visualizer(this);
		worldState = new WorldState();
		serverHandler = new ServerHandler(worldState, this);
		userinput = new UserInput(serverHandler);
		audioPlayer = new AudioPlayer();
		
		visualizer.addEventListener(userinput);
		visualizer.addEventListener(this);
		
		state = State.None;	
		paused = false;
		showNames = false;
		audioMuted = false;
	}
	
	public void run() {
		
		running = true;
		visualizer.start();
		state = State.Default;
		
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
				
				if(state.isConnected()) {
					worldState = serverHandler.getWorldState();					
					Timer.updateTimers();
				}
				else {
					
				}
				
				tick++;
				unprocessedTicks--;
			}
			if(unprocessedFrames >= 1) {
				
				visualizer.update(worldState, clientId, state, paused, showNames);
				
				fps++;
				unprocessedFrames -= Math.floor(unprocessedFrames);
			}
			if(System.currentTimeMillis() - fpsTimer >= 1000) {
				if(GameInfo.PRINT_GAMEINFO_TO_CONSOLE) {
					System.out.println("Fps: " + fps + " Ticks: " + tick);
				}
				GameInfo.currentFPS = fps;
				GameInfo.currentTPS = tick;
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
	
	// Return true if a connection was made
	private boolean connectToServer(InetAddress host, int port) {
		
		if(serverHandler.connect(host, port)) {
			state = State.Playing;
			serverHandler.start();
			return true;
		}
		System.out.println("Error: Failed to connect to server!");
		return false;
	}
	
	private void stop() {
		
		running = false;
		visualizer.stop();
	}
	
	private void reset() {
		
		System.out.println("RESET");
		state = State.Default;
		paused = false;
		serverHandler.disconnect();
		serverHandler = new ServerHandler(worldState, this);
		worldState = new WorldState();
		userinput.setServerHandler(serverHandler);
		
		audioPlayer.stopSound(Sound.Main_Theme);
		audioPlayer.stopSound(Sound.Death_Sound);
		audioPlayer.stopSound(Sound.Beep_Sound);
		
	}
	
	// Return false if a connection could not be made
	@Override
	public boolean connectButtonPressed(String ipAddressString, String portString) {
		
		int port = -1;
		if(portString.matches("\\d+"))
			port = Integer.valueOf(portString);
		else
			return false;
			
		if(ipAddressString.equals(GameInfo.LOCAL_HOST_STRING)) {
			if(connectToServer(serverHandler.getLocalHost(), port))
				return true;
			else
				return false;
		}
		try {
			if(connectToServer(InetAddress.getByName(ipAddressString), port))
				return true;
		} catch (UnknownHostException e) {
			
		}
		return false;
		
	}
	
	@Override
	public void settingsButtonPressed() {
		
		state = State.Settings;
	}
	
	@Override
	public void goBackButtonPressed() {
		
		state = State.Default;
	}
	
	@Override
	public void leaveGameButtonPressed() {
		
		reset();
	}
	
	@Override
	public void playerDied() {
		
		if(state != State.Dead) {
			state = State.Dead;
			
			audioPlayer.stopSound(Sound.Main_Theme);
			audioPlayer.playSound(Sound.Death_Sound);
		}
	}
	
	@Override
	public void playerRejoined() {
		
		if(state != State.WaitingForGame) {
			state = State.WaitingForGame;
			audioPlayer.stopSound(Sound.Death_Sound);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		
		if(!keysPressed.contains(e.getKeyCode())) {
			keysPressed.add(e.getKeyCode());
		}
		
		if(e.getKeyCode() == KeyEvent.VK_ENTER && state == State.Dead) {
			reset();
		}
		else if(e.getKeyCode() == KeyEvent.VK_ESCAPE && state.isConnected() && !typing) {
			paused = !paused;
		}
		else if(e.getKeyCode() == KeyEvent.VK_SHIFT && state.isConnected()) {
			showNames = !showNames;
		}
		else if(e.getKeyCode() == KeyEvent.VK_M) {
			this.mute(!audioMuted);
			visualizer.muteStateChanged(audioMuted);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
		if(keysPressed.contains(e.getKeyCode())) {
			keysPressed.remove(Integer.valueOf(e.getKeyCode()));
		}
	}

	@Override
	public void disconnect() {
		
		visualizer.showErrorDialog("The connection to the server was lost unexpectedly");
		reset();
	}

	@Override
	public String getPlayerName() {
		
		return visualizer.getPlayerName();
	}

	@Override
	public Color getPlayerColor() {
		
		return visualizer.getPlayerColor();
	}

	@Override
	public void playSelectSound() {
		
		audioPlayer.playSound(Sound.Select_Sound);
	}

	@Override
	public void gameStarted() {
		
		audioPlayer.loopSound(Sound.Main_Theme);
		this.state = State.Playing;
	}

	@Override
	public void fruitEaten() {
		
		audioPlayer.playSound(Sound.Eat_Sound);

	}
	
	@Override
	public void setClientId(int id) {
		
		this.clientId = id;
	}

	@Override
	public void gameStarting() {
		
		audioPlayer.playSound(Sound.Beep_Sound);
	}

	@Override
	public void gameCancelled() {
		
		audioPlayer.stopSound(Sound.Beep_Sound);
	}

	@Override
	public void mute(boolean mute) {
		
		this.audioMuted = mute;
		audioPlayer.mute(audioMuted);
	}
}
