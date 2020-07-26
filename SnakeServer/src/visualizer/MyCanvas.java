package visualizer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import game.Direction;
import game.Player;
import game.Sprite;
import game.World;
import state.GameInfo;

public class MyCanvas extends Canvas {

	protected MyCanvas() {
		
	}

	public void drawGame(World world) {
		Graphics graphics = getGraphics();
		if(graphics == null) {
			System.out.println("Canvas.getGraphics() returned null!");
			return;
		}
		
		Image dbImage = createImage(1000, 1000);			// Create a 1000 wide and 1000 tall image to draw on
		Graphics2D g = (Graphics2D) dbImage.getGraphics();	// The graphics for the dbImage
		
		// Draw here
		
		drawBackground(g);
		drawSprites(g, world.getSprites());
		
		graphics.drawImage(dbImage, 0, 0, this.getSize().width, this.getSize().height, this);

	}
	
	private void drawBackground(Graphics2D g) {

		g.setColor(new Color(170, 215, 81));
		g.fillRect(0, 0, 1000, 1000);
		
		g.setColor(new Color(162, 209, 73));
		for(int i = 0; i < GameInfo.WORLD_SIZE; i++) {
			for(int j = 0; j < GameInfo.WORLD_SIZE; j++) {
				if((i % 2 == 0 && j % 2 == 1) || (i % 2 == 1 && j % 2 == 0)) {
					
					g.fillRect(j*1000/GameInfo.WORLD_SIZE, i*1000/GameInfo.WORLD_SIZE, 
							1000/GameInfo.WORLD_SIZE, 1000/GameInfo.WORLD_SIZE);
				}
			}
		}
	}
	
	private void drawSprites(Graphics2D g, List<Sprite> sprites) {
		
		for(Sprite sprite : sprites) {
			
			if(sprite instanceof Player)
				drawPlayer(g, (Player) sprite);
			else {
				int[] spriteCoords = Visualizer.convertSpriteCoords(sprite.getxPos(), sprite.getyPos());
				g.setColor(new Color(255, 0, 0));
				g.fillRect(spriteCoords[0], spriteCoords[1], spriteCoords[2], spriteCoords[3]);
			}
		}
	}

	private void drawPlayer(Graphics2D g, Player player) {
		
		g.setColor(new Color(49, 141, 224));
		
		//Draw the body of the snake
		for(int i = 0; i < player.getTail().size(); i++) {
			if(i == player.getTail().size()-1)
				break;
			int[] tailPoint = player.getTail().get(i);
			int[] tailCoords = Visualizer.convertTailCoords(tailPoint[0], tailPoint[1]);
			g.fillRect(tailCoords[0], tailCoords[1], tailCoords[2], tailCoords[3]);
		}
		
		// Draw the front of the snake
		int[] playerCoords = Visualizer.convertSpriteCoords(player.getxPos(), player.getyPos());
		g.fillRect(playerCoords[0], playerCoords[1], playerCoords[2], playerCoords[3]);
		
		// Draw the tail of the snake
		int d = Visualizer.calculateTailPosition(player);
		
		int tailEndX = player.getTail().get(0)[0] * 10;
		int tailEndY = player.getTail().get(0)[1] * 10;
		if(player.getTailDirection().isHorizontal())
			tailEndX += d;
		else if(player.getTailDirection().isVertical())
			tailEndY += d;
		
		int[] tailEndCoords = Visualizer.convertSpriteCoords(tailEndX, tailEndY);
		g.fillRect(tailEndCoords[0], tailEndCoords[1], tailEndCoords[2], tailEndCoords[3]);
		
		
	}
}
