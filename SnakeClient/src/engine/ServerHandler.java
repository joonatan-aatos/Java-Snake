package engine;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import game.Direction;
import world_state.WorldState;

public class ServerHandler {

	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private BufferedReader text_in;
	private DataOutputStream text_out;
	private ObjectInputStream obj_in;
	private ObjectOutputStream obj_out;
	
	private Thread serverListener;
	private boolean connected;
	private WorldState worldState;
	private ServerHandlerToEngineInterface engineInterface;

	public ServerHandler(WorldState worldState, ServerHandlerToEngineInterface engineInterface) {
		
		this.worldState = worldState;
		this.engineInterface = engineInterface;
		socket = null;
		serverListener = new Thread(new ServerListener());
		connected = false;
	}

	public void start() {
		
		if(socket == null || !connected) {
			System.out.println("Error: Socket is not connected to the server!");
			return;
		}
		init();
		serverListener.start();
	}
	
	public boolean connect(InetAddress host, int port) {
		
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(host.getHostName(), port), 2000);
		} catch (UnknownHostException e) {
			// e.printStackTrace();
			return false;
		} catch (ConnectException e) {
			// e.printStackTrace();
			return false;
		} catch (IOException e) {
			//e.printStackTrace();
			return false;
		}
		if(socket.isClosed()) {
			System.exit(1);
		}
			
		System.out.println("Connected to the server");
		connected = true;
		return true;
	}
	
	public void sendDirectionInput(Direction direction) {
		
		if(socket == null)
			return;
		
		if(!socket.isClosed()) {
			try {
				text_out.writeBytes("#" + direction.toString() + "\n");
				text_out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Error: Cannot send direction input because socket is closed!");
		}
			
	}
	
	public void sendKeyInput(String keyCode) {
		
		if(socket == null)
			return;
		
		if(!socket.isClosed()) {
			try {
				text_out.writeBytes("&" + keyCode + "\n");
				text_out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Error: Cannot send direction input because socket is closed!");
		}
	}
	
	private class ServerListener implements Runnable {
				
		private ServerListener() {
			
		}
		
		@Override
		public void run() {
			
			// Listen for messages that the server sends
			Object input = null;
			while(connected) {
				
				try {
					
					input = obj_in.readObject();
					
				} catch(StreamCorruptedException e) {
					
					e.printStackTrace();
				
				} catch(ClassNotFoundException e) {
					
					e.printStackTrace();
					
				} catch(ClassCastException e) {
					
					e.printStackTrace();
					engineInterface.disconnect();
					
				} catch(InvalidClassException e) {
					
					e.printStackTrace();
					engineInterface.disconnect();
					
				} catch(OptionalDataException e) {
					
					e.printStackTrace();
					engineInterface.disconnect();
					
				} catch(SocketException e) {
					
					closeSocket();
					break;
					
				} catch (EOFException e) {
					
					engineInterface.disconnect();
					e.printStackTrace();
					closeSocket();
					break;
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				} catch (Exception e) {
					
					e.printStackTrace();
					engineInterface.disconnect();
				}
				
				if(input == null)
					closeSocket();
				
				if(input instanceof String) {
					
					// Received a String
					String inputString = (String) input;
					
					if(inputString.contains("[ID]")) {
						try {
							String clientIdStr = inputString.split("]")[1];
							int clientId = Integer.valueOf(clientIdStr);
							engineInterface.setClientId(clientId);
							
						} catch(Exception e) {
							e.printStackTrace();
							System.out.println("Failed to parse snake index in ServerListener.run()");
							engineInterface.setClientId(-1);
							
						}
					}
					else if(inputString.equals("PLAYER_DIED")) {
						engineInterface.playerDied();
					}
					else if(inputString.equals("PLAYER_REJOINED")) {
						engineInterface.playerRejoined();
					}
					else if(inputString.equals("GET_NAME")) {
						
						try {
							obj_out.writeObject(engineInterface.getPlayerName());
						} catch (IOException e) {
							e.printStackTrace();
							engineInterface.disconnect();
						}
					}
					else if(inputString.equals("GET_COLOR")) {
						
						try {
							obj_out.writeObject(engineInterface.getPlayerColor());
						} catch (IOException e) {
							e.printStackTrace();
							engineInterface.disconnect();
						}
					}
					else if(inputString.equals("GAME_STARTED")) {
						
						engineInterface.gameStarted();
					}
					else if(inputString.equals("FRUIT_EATEN")) {
						
						engineInterface.fruitEaten();
					}
					else if(inputString.equals("GAME_STARTING")) {
						
						engineInterface.gameStarting();
					}
					else if(inputString.equals("GAME_CANCELLED")) {
						
						engineInterface.gameCancelled();
					}
				}
				else if(input instanceof WorldState) {
					
					WorldState state = (WorldState) input;
					ServerHandler.this.worldState = state;
					
				}
				else if(input instanceof Integer) {
					
					int inputInt = (int) input;
					try {
						obj_out.writeObject(inputInt*1729);
					} catch(SocketException e) {
						
						e.printStackTrace();
						engineInterface.disconnect();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
				}
				
				input = null;
				
			}
		}
	}
	
	private void init() {
		
		// Initializing everything
		try {
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			text_in = new BufferedReader(new InputStreamReader(inputStream));
			text_out = new DataOutputStream(outputStream);
			obj_out = new ObjectOutputStream(outputStream);
			obj_in = new ObjectInputStream(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		
		closeSocket();
	}
	
	private void closeSocket() {
		
		System.out.println("Closing Socket");
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			connected = false;
		}
	}
	
	public InetAddress getLocalHost() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	
	public WorldState getWorldState() {
		return this.worldState;
	}
	
	public boolean isConnected() {
		return this.connected;
	}
	
	public InetAddress getIP() {
		
		try {
			return InetAddress.getByName("192.168.1.33");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
}
