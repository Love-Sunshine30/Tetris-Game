import javax.swing.*;

public class GameWindow extends JFrame {

    public GameWindow() {
        super("Tetris");

        // Create the game panel 
        GamePanel gamePanel = new GamePanel();

        // Add the panel to the content pane 
	add(gamePanel);

        // EXIT_ON_CLOSE kills the process when the window is closed 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // prevents grid distortion  
        setResizable(false);

        // sizes the frame to exactly fit the panel's preferredSize
        pack();

        // centre the window on the primary display
        setLocationRelativeTo(null);

        // make the window visible before starting the game 
        setVisible(true);

        // keyboard is focused from the beginning 
        gamePanel.requestFocusInWindow();

        // start the game loop
        gamePanel.startNewGame();
    }
}
