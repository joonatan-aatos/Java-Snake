import engine.Engine;

public class SnakeServer {

	public static void main(String[] args) {
		
		int port = 5555;
		if(args.length == 1) {
			if(args[0].matches("[0-9]+")) {
				int givenPort = Integer.valueOf(args[0]);
				if(givenPort > 1024 && givenPort < 65536) {
					port = givenPort;
				}
			}
		}
		Engine engine = new Engine(port);
		engine.run();
	}

}
