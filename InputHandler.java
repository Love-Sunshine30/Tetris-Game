import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {

    // The game panel that owns the game state.
    private final GamePanel gamePanel;

    public InputHandler(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // If the game is over, only allow restart (Enter).
        if (gamePanel.isGameOver()) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                gamePanel.startNewGame();
            }
            return;
        }

        // If the game is paused, only allow un-pause (P).
        if (gamePanel.isPaused()) {
            if (e.getKeyCode() == KeyEvent.VK_P) {
                gamePanel.togglePause();
            }
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                gamePanel.movePieceLeft();
                break;
            case KeyEvent.VK_RIGHT:
                gamePanel.movePieceRight();
                break;
            case KeyEvent.VK_DOWN:
                // Soft drop — move one row down immediately.
                gamePanel.softDrop();
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_X:
                // Rotate clockwise (UP arrow is the standard Tetris Guideline key).
                gamePanel.rotatePieceClockwise();
                break;
            case KeyEvent.VK_Z:
                // Rotate counter-clockwise.
                gamePanel.rotatePieceCounterClockwise();
                break;
            case KeyEvent.VK_SPACE:
                // Hard drop.
                gamePanel.hardDrop();
                break;
            case KeyEvent.VK_P:
                gamePanel.togglePause();
                break;
            case KeyEvent.VK_ENTER:
                // Re-start when game is already running — ignored (handled above
                // in game-over branch; here it falls through to no-op).
                break;
            default:
                // Unrecognised key — ignore silently.
                break;
        }
    }
}
