package protocol;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;

import state.GameInfo;
import state.State;
import world_state.WorldState;

public class ClientHandler implements Runnable {
	
	private Random random;
	private Socket socket;
	private int playerId;
	private String playerName;
	private Color playerColor;
	private PlayerInterface playerInterface;
	private ProtocolToEngineInterface engineInterface;
	private boolean running;
	private boolean playerConfirmed;
	
	private InputStream inputStream;
	private OutputStream outputStream;
	private BufferedReader text_in;
	private ObjectInputStream obj_in;
	private DataOutputStream text_out;
	private ObjectOutputStream obj_out;
	
	public ClientHandler(Socket clientSocket, int playerId, PlayerInterface playerInterface, 
			ProtocolToEngineInterface engineInterface) {
		
		this.socket = clientSocket;
		this.playerId = playerId;
		this.playerInterface = playerInterface;
		this.engineInterface = engineInterface;
		
		this.random = new Random();
		
		this.playerConfirmed = false;
	}

	@Override
	public void run() {
		
		try {
			init();
		} catch (IOException e) {
			e.printStackTrace();
		}

		running = true;
		
		if(!getClientInfo()) {
			engineInterface.kickPlayer(playerId);
		}
		
		String input = null;
		
		while(running) {
			
			try {
				input = text_in.readLine();
			} catch(SocketException e) {
				
				closeSocket();
				
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
			
			if(input == null) {
				closeSocket();
				return;
			}
			//TODO parse the input here
						
			if(input.charAt(0) == '#') {
				
				playerInterface.directionInputReceived(input);
			}
			else if(input.charAt(0) == '&') {
				
				playerInterface.keyInputReceived(input);
				
				if(input.equals("&" + Integer.toString(KeyEvent.VK_SPACE))) {

					playerInterface = engineInterface.rejoin(playerId);
					if(playerInterface == null) {
						closeSocket();
						break;
					}
					playerInterface.setColor(playerColor);
					playerInterface.setName(playerName);
				}
			}
		}
	}

	private void init() throws IOException {
		
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
		text_in = new BufferedReader(new InputStreamReader(inputStream));
		text_out = new DataOutputStream(outputStream);
		obj_out = new ObjectOutputStream(outputStream);
		obj_in = new ObjectInputStream(inputStream);
		
		//TODO Do other initialization here
	}
	
	// Return false if this isn't successful
	private boolean getClientInfo() {
		
		try {
			
			// Send a kind of password verification thing
			int randomInt = random.nextInt(1000);
			obj_out.writeObject(randomInt);
			obj_out.flush();

			Object passwordObj = obj_in.readObject();
			if(passwordObj instanceof Integer) {
				int input = (int) passwordObj;
				if(input != randomInt * 1729) {
					System.out.println("Invalid Password");
					return false;
				}
			}
			
			
			obj_out.writeObject("GET_NAME");
			obj_out.flush();

			Object nameObject = obj_in.readObject();
			if(nameObject instanceof String) {
				String input = (String) nameObject;
				
				playerName = input;
			}
			else
				return false;
			
			
			obj_out.writeObject("GET_COLOR");
			obj_out.flush();

			Object colorObject = obj_in.readObject();
			if(colorObject instanceof Color) {
				
				Color color = (Color) colorObject;
				playerColor = color;
			}
			else if(colorObject == null) {
				
				playerColor = new Color(49, 141, 224);
			}
			else
				return false;
			
			
			obj_out.writeObject("[ID]"+Integer.toString(this.playerId));
			obj_out.flush();
			
		} catch(SocketException e) {
			
			e.printStackTrace();
			return false;
			
		} catch(EOFException e) {
			
			e.printStackTrace();
			return false;
			
		} catch(StreamCorruptedException e) {
			
			e.printStackTrace();
			return false;
			
		} catch(ClassNotFoundException e) {
			
			e.printStackTrace();
			System.exit(1);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			System.exit(1);
		}
		
		playerConfirmed = true;
		playerInterface.setColor(playerColor);
		playerInterface.setName(playerName);
		
		engineInterface.playerJoinedTheGame(playerName);
		
		return true;
	}
	
	private void closeSocket() {
		
		if(socket.isClosed())
			return;
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		engineInterface.socketClosed(playerId);
		
		running = false;
		
	}
	
	public enum Info {
		
		PlayerDied("PLAYER_DIED"),
		GameStarted("GAME_STARTED"),
		FruitEaten("FRUIT_EATEN"),
		PlayerRejoined("PLAYER_REJOINED"),
		GameStarting("GAME_STARTING"),
		GameCancelled("GAME_CANCELLED");
		
		private String message;
		
		private Info(String message) {
			
			this.message = message;
		}
		
		public String getMessage() {
			return this.message;
		}
	}
	
	public void sendInfo(Info info) {
		
		int wait = 0;
		while(obj_out == null) {
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			wait++;
			if(wait > 10)
				break;
		}
		
		try {
			obj_out.writeObject(info.getMessage());
			obj_out.flush();
		} catch (IOException e) {
			closeSocket();
		} catch (NullPointerException e) {
			e.printStackTrace();
			closeSocket();
		}
	}
	
	public PlayerInterface getPlayerInterface() {
		return this.playerInterface;
	}
	
	public void sendWorldState(WorldState state) throws IOException {
		
		if(!playerConfirmed)
			return;
		
		if(obj_out == null)
			return;
				
		obj_out.writeObject(state);
		obj_out.flush();
		
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public void disconnect() {
		
		// Make this function better
		
		closeSocket();
	}
}
