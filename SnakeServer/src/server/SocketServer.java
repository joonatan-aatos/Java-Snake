package server;

import java.io.IOException;
import java.lang.System.Logger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
	
	private ServerSocket serverSocket;
	private int port;
	private boolean running;

	private ClientListener clientListener;
	private SocketServerToEngineInterface ssi;

	public SocketServer(SocketServerToEngineInterface socketServerInterface, int port) {
		
		clientListener = new ClientListener(socketServerInterface);
		this.ssi = socketServerInterface;
		this.port = port;
	}
	
	public void start() {
		
		init();
		new Thread(clientListener).start();
	}
	
	public void stop() throws IOException {
		
		//TODO Make the function work
		running = false;
		serverSocket.close();
	}
	
	// This class creates a ClientHandler object that communicates with the client
	private class ClientListener implements Runnable {

		private SocketServerToEngineInterface engineInterface;
		
		private ClientListener(SocketServerToEngineInterface socketServerInterface) {
			this.engineInterface = socketServerInterface;
		}
		
		@Override
		public void run() {
			running = true;
						
			// Server is now running
			System.out.println("Server is now running at port "+Integer.toString(serverSocket.getLocalPort()));
			Socket socket = null;
			
			while(running) {
				try {
					socket = serverSocket.accept();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				engineInterface.clientConnected(socket);
				
			}
		}
	}
	
	private void init() {
		
		serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
