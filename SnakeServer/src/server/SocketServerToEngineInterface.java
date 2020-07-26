package server;

import java.net.Socket;

public interface SocketServerToEngineInterface {
	
	public void clientConnected(Socket s);
}
