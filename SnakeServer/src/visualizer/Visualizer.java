package visualizer;

import java.awt.event.KeyListener;
import java.util.EventListener;

import game.Direction;
import game.Player;
import game.World;
import state.GameInfo;

public class Visualizer {

	private MyCanvas canvas;
	private MyFrame frame;
	
	public Visualizer() {
		
		canvas = new MyCanvas();
		frame = new MyFrame();
		
		frame.add(canvas);
	}
	
	public void start() {
		
		frame.setVisible(true);
	}
	
	public void update(World world) {
		
		canvas.drawGame(world);
	}
	
	public void addEventListener(EventListener e) {
		
		if(e instanceof KeyListener) {
			canvas.addKeyListener((KeyListener) e);
		}
		canvas.requestFocus();
	}
	
	protected static int[] convertSpriteCoords(int x, int y) {
		
		int[] coords = {
				x * 1000 / GameInfo.WORLD_SIZE / 10,
				1000 - (y+10) * 1000 / GameInfo.WORLD_SIZE / 10,
				1000 / GameInfo.WORLD_SIZE,
				1000 / GameInfo.WORLD_SIZE
		};
		return coords;
	}
	
	protected static int[] convertTailCoords(int x, int y) {
		
		int[] coords = {
				x * 1000 / GameInfo.WORLD_SIZE,
				1000 - (y+1) * 1000 / GameInfo.WORLD_SIZE,
				1000 / GameInfo.WORLD_SIZE,
				1000 / GameInfo.WORLD_SIZE
		};
		return coords;
	}
	
	protected static int calculateTailPosition(Player player) {
		
		Direction tailDirection = player.getTailDirection();

		int d = player.getxPos() - (int) (Math.floor((double) player.getxPos() / 10d) * 10d) +
				player.getyPos() - (int) (Math.floor((double) player.getyPos() / 10d) * 10d);
		
		if((player.getCurrentDirection() == Direction.Left || player.getCurrentDirection() == Direction.Down) &&
				(tailDirection == Direction.Right || tailDirection == Direction.Up))
			d = 10 - d;
		if((player.getCurrentDirection() == Direction.Right || player.getCurrentDirection() == Direction.Up) &&
				(tailDirection == Direction.Left || tailDirection == Direction.Down))
			d = 10 - d;

		if(d == 10)
			d = 0;		
		if(tailDirection == Direction.Right || tailDirection == Direction.Up) {
			d -= 10;
			if(d == -10)
				d = 0;
		}
		return d;
	}
}
