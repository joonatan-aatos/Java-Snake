package visualizer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EventListener;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import game.Direction;
import state.GameInfo;
import state.State;
import world_state.WorldState;
import world_state.WorldState.PlayerState;

public class Visualizer {
	
	private MyCanvas canvas;
	private MyFrame frame;
	private VisualizerToEngineInterface engineInterface;
	
	private ArrayList<Component> defaultMenuComponents;
	private ArrayList<Component> settingsMenuComponents;
	private ArrayList<Component> pausedMenuComponents;
	private JLabel invalidServerInfoLabel;
	private JTextField playerNameTextField;
	private JTextField playerColorTextField;
	private JCheckBox muteCheckBox;
	
	protected static AnimatedString waitingForPlayers;
	protected static Image appleImage;
	protected static Image orangeImage;
	protected static Image bananaImage;
	protected static Font snakeGameFont;
	
	public Visualizer(VisualizerToEngineInterface engineInterface) {
		
		frame = new MyFrame();
		canvas = new MyCanvas();
		this.engineInterface = engineInterface;
		
		init();
		try {
			loadResources();
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (FontFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void update(WorldState world, int clientId, State state, boolean paused, boolean drawPlayerNames) {
		
		if(state.isConnected()) {
			
			canvas.drawGame(world, clientId, state == State.Dead, paused, drawPlayerNames);
			
		}
		else {
			if(state == State.Default)
				canvas.drawConnectingWindow(getPlayerColor());
			else
				canvas.drawConnectingWindow(null);
		}
		
		updateVisibleComponents(state, paused);
		updateAnimations(state);
		
	}
	
	private void loadResources() throws IOException, FontFormatException {
		
		InputStream appleImageStream = this.getClass().getResourceAsStream("/images/Apple.png");
		appleImage = ImageIO.read(appleImageStream);
		
		InputStream orangeImageStream = this.getClass().getResourceAsStream("/images/Orange.png");
		orangeImage = ImageIO.read(orangeImageStream);
		
		InputStream bananaImageStream = this.getClass().getResourceAsStream("/images/Banana.png");
		bananaImage = ImageIO.read(bananaImageStream);
		
		InputStream snakeFontStream = this.getClass().getResourceAsStream("/fonts/DINOMOUSE-REGULAR.OTF");
		snakeGameFont = Font.createFont(Font.TRUETYPE_FONT, snakeFontStream);
		
		
		String[] waitingForPlayersStrings = {
				"Waiting for players.  ",
				"Waiting for players.. ",
				"Waiting for players..."
		};
		waitingForPlayers = new AnimatedString(waitingForPlayersStrings, 1000);
	}
	
	private void init() {
		
		// Initialize all components that are visible in the default menu
		JTextField nameTextField;
		JTextField ipTextField;
		JTextField portTextField;
		JTextField colorTextField;
		JButton connectButton;
		JButton settingsButton;
		JCheckBox useLocalHostCheckBox;
		
		nameTextField = new HintTextField("Name");
		nameTextField.setBounds(GameInfo.WIDTH/2-80, GameInfo.HEIGHT/2-50, 160, 30);
		nameTextField.setDocument(new JTextFieldLimit(12));
		
		ipTextField = new HintTextField("Server IP address");
		ipTextField.setBounds(GameInfo.WIDTH/2-80, GameInfo.HEIGHT/2+10, 160, 30);
		
		portTextField = new HintTextField("Server port");
		portTextField.setBounds(GameInfo.WIDTH/2-80, GameInfo.HEIGHT/2+45, 160, 30);
		
		colorTextField = new HintTextField("Snake Color HEX");
		colorTextField.setBounds(10, GameInfo.HEIGHT - 70, 120, 30);
		
		useLocalHostCheckBox = new JCheckBox("Use local host");
		useLocalHostCheckBox.setBounds(GameInfo.WIDTH/2-80, GameInfo.HEIGHT/2-15, 160, 20);
		useLocalHostCheckBox.addActionListener(new SelectSoundPlayer());
		
		invalidServerInfoLabel = new JLabel(" Invalid ip address or port!");
		invalidServerInfoLabel.setBounds(GameInfo.WIDTH/2-100, GameInfo.HEIGHT/2+125, 200, 20);
		
		connectButton = new JButton("Connect to server");
		connectButton.setBounds(GameInfo.WIDTH/2-80, GameInfo.HEIGHT/2+80, 160, 40);
		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(useLocalHostCheckBox.isSelected()) {
					if(!engineInterface.connectButtonPressed(GameInfo.LOCAL_HOST_STRING, portTextField.getText()))
						invalidServerInfoLabel.setVisible(true);
					else {
						invalidServerInfoLabel.setVisible(false);
						canvas.requestFocus();
					}
				} else {
					if(!engineInterface.connectButtonPressed(ipTextField.getText(), portTextField.getText()))
						invalidServerInfoLabel.setVisible(true);
					else {
						invalidServerInfoLabel.setVisible(false);
						canvas.requestFocus();
					}
				}
			}
		});
		connectButton.addActionListener(new SelectSoundPlayer());
		
		settingsButton = new JButton("Settings");
		settingsButton.setBounds(GameInfo.WIDTH - 120, 5, 100, 30);
		settingsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				engineInterface.settingsButtonPressed();
			}
		});
		settingsButton.addActionListener(new SelectSoundPlayer());
		
		defaultMenuComponents = new ArrayList<Component>();
		
		defaultMenuComponents.add(nameTextField);
		defaultMenuComponents.add(ipTextField);
		defaultMenuComponents.add(portTextField);
		defaultMenuComponents.add(colorTextField);
		defaultMenuComponents.add(connectButton);
		defaultMenuComponents.add(settingsButton);
		defaultMenuComponents.add(useLocalHostCheckBox);
		defaultMenuComponents.add(invalidServerInfoLabel);
		
		playerNameTextField = nameTextField;
		playerColorTextField = colorTextField;
		
		
		// Initialize all components that are visible in the settings menu
		JButton goBackButton;
		JComboBox<String> resolutionSelect;
		JLabel resolutionLabel;
		
		goBackButton = new JButton("Done");
		goBackButton.setBounds(GameInfo.WIDTH/2-50, GameInfo.HEIGHT/2+100, 100, 30);
		goBackButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				engineInterface.goBackButtonPressed();
			}
		});
		goBackButton.addActionListener(new SelectSoundPlayer());
		goBackButton.setVisible(false);
		
		resolutionSelect = new JComboBox<String>(new String[] {"400px", "500px", "600px", "700px", "800px"});
		resolutionSelect.setSelectedItem(GameInfo.WIDTH+"px");
		resolutionSelect.setBounds(GameInfo.WIDTH/2-80, GameInfo.HEIGHT/2+10, 160, 30);
		resolutionSelect.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getSource() == resolutionSelect) {
					String resolution = (String) resolutionSelect.getSelectedItem();
					int resolutionValue = Integer.valueOf(resolution.split("px")[0]);
					if(GameInfo.WIDTH != resolutionValue && GameInfo.HEIGHT != resolutionValue) {
						GameInfo.WIDTH = GameInfo.HEIGHT = resolutionValue;
						resolutionChanged();
					}
				}
			}
		});
		resolutionSelect.addActionListener(new SelectSoundPlayer());
		resolutionSelect.setVisible(false);
		
		resolutionLabel = new JLabel("Resolution");
		resolutionLabel.setBounds(GameInfo.WIDTH/2-80, GameInfo.HEIGHT/2-15, 160, 20);
		resolutionLabel.setVisible(false);
		
		settingsMenuComponents = new ArrayList<Component>();
		
		settingsMenuComponents.add(goBackButton);
		settingsMenuComponents.add(resolutionSelect);
		settingsMenuComponents.add(resolutionLabel);
		
		
		// Initialize all components that are visible in the paused menu
		JButton leaveGameButton;

		leaveGameButton = new JButton("Leave game");
		leaveGameButton.setBounds(GameInfo.WIDTH/2-60, (int) ( (double) GameInfo.HEIGHT / 2d + 70d * (double)GameInfo.HEIGHT / 600d), 120, 30);
		leaveGameButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				engineInterface.leaveGameButtonPressed();
			}
		});
		leaveGameButton.addActionListener(new SelectSoundPlayer());
		leaveGameButton.setVisible(false);
				
		muteCheckBox = new JCheckBox("Mute Audio");
		muteCheckBox.setBounds(GameInfo.WIDTH/2-60,(int) ( (double) GameInfo.HEIGHT / 2d + 40d * (double)GameInfo.HEIGHT / 600d), 120, 20);
		muteCheckBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				engineInterface.mute(muteCheckBox.isSelected());
			}
		});
		muteCheckBox.addActionListener(new SelectSoundPlayer());
		muteCheckBox.setVisible(false);
		
		pausedMenuComponents = new ArrayList<Component>();
		
		pausedMenuComponents.add(leaveGameButton);
		pausedMenuComponents.add(muteCheckBox);
		
		
		for(Component c : defaultMenuComponents) {
			frame.add(c);
		}
		for(Component c : settingsMenuComponents) {
			frame.add(c);
		}
		for(Component c : pausedMenuComponents) {
			frame.add(c);
		}
		// When implementing a new list of components remember to also add it to the resolutionChanged() function
				
		frame.add(canvas);
		
		invalidServerInfoLabel.setVisible(false);
	}
	
	private void updateVisibleComponents(State s, boolean paused) {
		
		if(s.isConnected()) {
			if(defaultMenuComponents.get(0).isVisible()) {
				for(Component c : defaultMenuComponents) {
					c.setVisible(false);
				}
			}
			if(paused) {
				if(!pausedMenuComponents.get(0).isVisible()) {
					for(Component c : pausedMenuComponents) {
						c.setVisible(true);
					}					
				}
			}
			else {
				if(pausedMenuComponents.get(0).isVisible()) {
					for(Component c : pausedMenuComponents) {
						c.setVisible(false);
					}					
				}
			}
		}
		else {
			if(s == State.Default) {
				if(!defaultMenuComponents.get(0).isVisible()) {
					for(Component c : defaultMenuComponents) {
						c.setVisible(true);
					}
					invalidServerInfoLabel.setVisible(false);
				}
				if(settingsMenuComponents.get(0).isVisible()) {
					for(Component c : settingsMenuComponents) {
						c.setVisible(false);
					}
				}
			}
			else if(s == State.Settings) {
				if(defaultMenuComponents.get(0).isVisible()) {
					for(Component c : defaultMenuComponents) {
						c.setVisible(false);
					}
				}
				if(!settingsMenuComponents.get(0).isVisible()) {
					for(Component c : settingsMenuComponents) {
						c.setVisible(true);
					}
				}
			}
			
			if(pausedMenuComponents.get(0).isVisible()) {
				for(Component c : pausedMenuComponents) {
					c.setVisible(false);
				}
			}
		}
	}
	
	private void updateAnimations(State state) {
		
		if(state.isConnected()) {
			
			if(!waitingForPlayers.isOn())
				waitingForPlayers.turnOn();
		}
		else {
			
			if(waitingForPlayers.isOn())
				waitingForPlayers.turnOff();			
		}
	}
	
	private void resolutionChanged() {
		
		for(Component c : defaultMenuComponents) {
			c.setVisible(false);
			frame.remove(c);
		}
		for(Component c : settingsMenuComponents) {
			c.setVisible(false);
			frame.remove(c);
		}
		for(Component c : pausedMenuComponents) {
			c.setVisible(false);
			frame.remove(c);
		}
		init();
		frame.refreshSize();
	}
	
	public void muteStateChanged(boolean mute) {
		
		if(mute && !muteCheckBox.isSelected()) {
			muteCheckBox.setSelected(true);
		}
		else if(!mute && muteCheckBox.isSelected()) {			
			muteCheckBox.setSelected(false);
		}
		
	}
	
	public void addEventListener(EventListener e) {
		
		if(e instanceof KeyListener) {
			canvas.addKeyListener((KeyListener) e);
		}
		canvas.requestFocus();
	}
	
	public void start() {
		
		frame.setVisible(true);
	}
	
	public void stop() {
		
		frame.setVisible(false);
	}
	
	public void showErrorDialog(String message) {
		
		JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public String getPlayerName() {
		
		return playerNameTextField.getText().equals("") ? "Snake" : playerNameTextField.getText();
	}
	
	public Color getPlayerColor() {
		
		String colorHex = playerColorTextField.getText();
		if(colorHex.length() == 6) {
			
			try {
				Color playerColor = Color.decode("#"+colorHex);
				return playerColor;
				
			} catch(NumberFormatException e) {
				
				return GameInfo.DEFAULT_SNAKE_COLOR;
			}
		}
		return GameInfo.DEFAULT_SNAKE_COLOR;
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
	
	protected static int calculateTailPosition(PlayerState player) {
		
		Direction tailDirection = Direction.getDirectionFromString(player.tailDirection);

		if(!player.animateTail) {
			if(tailDirection == Direction.Left || tailDirection == Direction.Down) {
				return 10;
			}
			else {
				return -10;
			}
		}

		int d = player.xPos - (int) (Math.floor((double) player.xPos / 10d) * 10d) +
				player.yPos - (int) (Math.floor((double) player.yPos / 10d) * 10d);
		
		if(!player.animateTail) {
			d = 0;
		}
		
		if((Direction.getDirectionFromString(player.currentDirection) == Direction.Left || 
				Direction.getDirectionFromString(player.currentDirection) == Direction.Down) &&
				(tailDirection == Direction.Right || tailDirection == Direction.Up))
			d = 10 - d;
		if((Direction.getDirectionFromString(player.currentDirection) == Direction.Right || 
				Direction.getDirectionFromString(player.currentDirection) == Direction.Up) &&
				(tailDirection == Direction.Left || tailDirection == Direction.Down))
			d = 10 - d;

		if(d == 10 && player.animateTail)
			d = 0;
		if((tailDirection == Direction.Right || tailDirection == Direction.Up)) {
			d -= 10;
			if(d == -10)
				d = 0;
		}
		return d;
	}
	
	private class SelectSoundPlayer implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			engineInterface.playSelectSound();
		}
		
	}
}
