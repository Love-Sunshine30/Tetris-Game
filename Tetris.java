import javax.swing.SwingUtilities;

public class Tetris {

    public static void main(String[] args) {
       // entry point 
	SwingUtilities.invokeLater(GameWindow::new);
    }
}
