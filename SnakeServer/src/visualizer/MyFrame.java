package visualizer;

import javax.swing.JFrame;

public class MyFrame extends JFrame {

	public MyFrame() {
		
		super.setSize(state.GameInfo.WIDTH, state.GameInfo.HEIGHT);
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		super.setTitle(state.GameInfo.NAME);
		super.setResizable(false);
		super.setLocationRelativeTo(null);	//centers the screen
	}
}
