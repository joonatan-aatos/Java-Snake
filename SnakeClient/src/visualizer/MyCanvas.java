package visualizer;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;

import game.Direction;
import state.GameInfo;
import world_state.WorldState;
import world_state.WorldState.FruitState;
import world_state.WorldState.PlayerState;
import world_state.WorldState.SpriteState;

public class MyCanvas extends Canvas {

	protected MyCanvas() {
		
	}
	
	protected void drawGame(WorldState world, int clientId, boolean drawDeathMessage, boolean paused, boolean drawPlayerNames) {
		
		Graphics graphics = getGraphics();
		if(graphics == null) {
			//System.out.println("Canvas.getGraphics() returned null!");
			return;
		}
		
		Image dbImage = createImage(1000, 1000);			// Create a 1000 wide and 1000 tall image to draw on
		Graphics2D g = (Graphics2D) dbImage.getGraphics();	// The graphics for the dbImage
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		// Draw here
		drawBackground(g);
		drawSprites(g, world, drawPlayerNames);
		if(drawDeathMessage)
			drawDeathMessage(g);
		if(!world.gameStarted && !drawDeathMessage)
			drawWaitingScreen(g, world.countDownString);
		if(paused)
			drawPausedScreen(g);
		drawGameInfo(g, world.sprites, clientId);
		
		
		((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_RENDERING,
	            RenderingHints.VALUE_RENDER_QUALITY);
		((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		            RenderingHints.VALUE_ANTIALIAS_ON);

		graphics.drawImage(dbImage, 0, 0, this.getSize().width, this.getSize().height, this);

	}
	
	protected void drawConnectingWindow(Color colorBoxColor) {
		
		Graphics graphics = getGraphics();
		if(graphics == null) {
			return;
		}
		
		Image dbImage = createImage(1000, 1000);			// Create a 1000 wide and 1000 tall image to draw on
		Graphics2D g = (Graphics2D) dbImage.getGraphics();	// The graphics for the dbImage
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		// Draw here
		drawBackground(g);
		drawLogo(g);
		drawGameInfo(g);
		if(colorBoxColor != null)
			drawColorBox(g, colorBoxColor);
		
		((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_RENDERING,
	            RenderingHints.VALUE_RENDER_QUALITY);
		((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		            RenderingHints.VALUE_ANTIALIAS_ON);
		
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
	
	private void drawLogo(Graphics2D g) {
		
		//g.drawImage(Visualizer.snakeLogoImage, 100, 50, 800, 450, null);
		g.setFont(Visualizer.snakeGameFont.deriveFont(250f));
		g.setColor(GameInfo.DEFAULT_SNAKE_COLOR);
		
		drawCenteredString(g, "Snake", 500, 300);
		
		g.setFont(Visualizer.snakeGameFont.deriveFont(30f));
		
		g.drawString("Made by: Joonatan", 750, 985);
		
	}
	
	private void drawColorBox(Graphics2D g, Color color) {
		
		g.setColor(color);
		g.fillRect(20, 810, 100, 100);
		
		g.setColor(Color.GRAY);
		g.setStroke(new BasicStroke(5));
		g.drawRect(20, 810, 100, 100);
		
	}
	
	private void drawSprites(Graphics2D g, WorldState world, boolean drawPlayerNames) {
		
		ArrayList<SpriteState> sprites = world.sprites;
		
		for(SpriteState sprite : sprites) {
			
			if(sprite instanceof PlayerState)
				drawPlayer(g, (PlayerState) sprite, world, drawPlayerNames);
			
			else if(sprite instanceof FruitState) {
				FruitState fruit = (FruitState) sprite;
				
				int[] fruitCoords = Visualizer.convertSpriteCoords(fruit.xPos*10, fruit.yPos*10);
				if(fruit.type == 0) {
					g.drawImage(Visualizer.appleImage, fruitCoords[0], fruitCoords[1], fruitCoords[2], fruitCoords[3], null);
				}
				else if(fruit.type == 1){
					g.drawImage(Visualizer.orangeImage, fruitCoords[0], fruitCoords[1], fruitCoords[2], fruitCoords[3], null);				
				}
				else if(fruit.type == 2) {
					g.drawImage(Visualizer.bananaImage, fruitCoords[0], fruitCoords[1], fruitCoords[2], fruitCoords[3], null);								
				}
				else {
					int[] spriteCoords = Visualizer.convertSpriteCoords(sprite.xPos, sprite.yPos);
					g.setColor(new Color(255, 0, 0));
					g.fillRect(spriteCoords[0], spriteCoords[1], spriteCoords[2], spriteCoords[3]);					
				}
			}
			else {
				int[] spriteCoords = Visualizer.convertSpriteCoords(sprite.xPos, sprite.yPos);
				g.setColor(new Color(255, 0, 0));
				g.fillRect(spriteCoords[0], spriteCoords[1], spriteCoords[2], spriteCoords[3]);
			}
		}
	}

	private void drawPlayer(Graphics2D g, PlayerState player, WorldState world, boolean drawNames) {
		
		g.setColor(player.color);
		
		//Draw the body of the snake
		for(int i = 0; i < player.tail.size(); i++) {
			if(i == player.tail.size()-1)
				break;
			int[] tailPoint = player.tail.get(i);
			int[] tailCoords = Visualizer.convertTailCoords(tailPoint[0], tailPoint[1]);
			g.fillRect(tailCoords[0], tailCoords[1], tailCoords[2], tailCoords[3]);
		}
		
		// Draw the front of the snake
		int[] playerCoords = Visualizer.convertSpriteCoords(player.xPos, player.yPos);
		g.fillRect(playerCoords[0], playerCoords[1], playerCoords[2], playerCoords[3]);
		
		// Draw the tail of the snake
		int d = Visualizer.calculateTailPosition(player);
		
		int tailEndX = player.tail.get(0)[0] * 10;
		int tailEndY = player.tail.get(0)[1] * 10;
		
		if(Direction.getDirectionFromString(player.tailDirection).isHorizontal())
			tailEndX += d;
		else if(Direction.getDirectionFromString(player.tailDirection).isVertical())
			tailEndY += d;
		
		int[] tailEndCoords = Visualizer.convertSpriteCoords(tailEndX, tailEndY);
		g.fillRect(tailEndCoords[0], tailEndCoords[1], tailEndCoords[2], tailEndCoords[3]);
		
		// Draw other things
		if(!world.gameStarted && player.readyToStart) {
			
			g.setFont(Visualizer.snakeGameFont.deriveFont(35f));
			drawCenteredString(g, "(READY)", (player.xPos+5)*100/25, (25 - (player.yPos) / 10)*1000/25 - 60);
		}
		if(drawNames) {
			
			g.setFont(Visualizer.snakeGameFont.deriveFont(30f));
			drawCenteredString(g, player.name, (player.xPos+5)*100/25, (250 - player.yPos)*100/25 + 25);
		}
	}
	
	private void drawGameInfo(Graphics2D g) {
		
		/*
		g.setColor(Color.BLACK);
		g.setFont(new Font("", Font.PLAIN, 30));
		g.drawString("FPS: "+Integer.toString(GameInfo.currentFPS), 10, 35);
		*/
		
	}
	
	private int playerLength = 0;
	
	private void drawGameInfo(Graphics2D g, ArrayList<SpriteState> sprites, int clientId) {
		
		drawGameInfo(g);
		
		g.setColor(Color.BLACK);
		g.setFont(new Font("", Font.PLAIN, 30));
		
		PlayerState player = null;
		
		for(SpriteState ss : sprites) {
			if(ss instanceof PlayerState) {
				PlayerState ps = (PlayerState) ss;
				if(ps.clientId == clientId) {
					player = ps;
				}
			}
		}
		
		if(player == null) {			
			g.drawString("Length: "+Integer.toString(playerLength), 810, 35);
		}
		else {
			playerLength = player.tail.size();
			g.drawString("Length: "+Integer.toString(playerLength), 810, 35);
		}

	}
	
	private void drawDeathMessage(Graphics2D g) {
		
		g.setColor(Color.RED);
		g.setFont(Visualizer.snakeGameFont.deriveFont(180f));
		drawCenteredString(g, "You Died!", 500, 450);
		g.setFont(Visualizer.snakeGameFont.deriveFont(70f));
		drawCenteredString(g, "Press enter to leave or", 500, 600);
		drawCenteredString(g, "space to rejoin", 500, 670);
	}
	
	private void drawWaitingScreen(Graphics2D g, String countDownString) {
		
		g.setColor(Color.RED);
		
		if(countDownString == null)
			return;
		
		if(countDownString.isEmpty()) {
			g.setFont(Visualizer.snakeGameFont.deriveFont(80f));
			g.drawString(Visualizer.waitingForPlayers.getString(), 160, 450);
			g.setFont(Visualizer.snakeGameFont.deriveFont(60f));
			drawCenteredString(g, "Press space to get ready / unready", 500, 540);
		}
		else {
			g.setFont(Visualizer.snakeGameFont.deriveFont(200f));
			drawCenteredString(g, countDownString, 500, 500);
		}
	}
	
	private void drawPausedScreen(Graphics2D g) {
		
		g.setColor(new Color(80, 80, 80, 100));
		g.fillRect(350, 370, 300, 360);
		
		g.setColor(new Color(80, 80, 80));
		g.setFont(Visualizer.snakeGameFont.deriveFont(90f));
		drawCenteredString(g, "Options", 500, 320);
		
		g.setColor(new Color(40, 40, 40, 100));
		g.setStroke(new BasicStroke(5));
		g.drawRect(350, 370, 300, 360);
	}
	
	private void drawCenteredString(Graphics2D g, String text, int x, int y) {
	    // Get the FontMetrics
	    FontMetrics metrics = g.getFontMetrics(g.getFont());
	    // Check for null pointer
	    if(metrics == null)
	    	return;
	    // Determine the X coordinate for the text
	    int xPos = x - metrics.stringWidth(text) / 2;
	    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
	    int yPos = y - metrics.getHeight() / 2 + metrics.getAscent();
	    // Draw the String
	    g.drawString(text, xPos, yPos);
	}
}
