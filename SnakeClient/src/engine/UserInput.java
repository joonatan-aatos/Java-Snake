package engine;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import game.Direction;

public class UserInput implements KeyListener {

	private ServerHandler serverHandler;
	
	public UserInput(ServerHandler server) {
		
		this.serverHandler = server;
	}
	
	public void setServerHandler(ServerHandler serverHandler) {
		
		this.serverHandler = serverHandler;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		
		Direction direction = Direction.None;
		
		if(e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP) {
			direction = Direction.Up;
		} else if(e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN) {
			direction = Direction.Down;
		} else if(e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT) {
			direction = Direction.Left;
		} else if(e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) {
			direction = Direction.Right;
		}
		
		if(direction != Direction.None) {
			serverHandler.sendDirectionInput(direction);
		}
		
		if(e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_SPACE) {
			serverHandler.sendKeyInput(Integer.toString(e.getKeyCode()));
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}
}
