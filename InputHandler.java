import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * InputHandler.java — Translates raw key events into game commands.
 *
 * <h2>Design: KeyAdapter over KeyListener</h2>
 * <p>{@link KeyAdapter} is an abstract adapter class that provides empty
 * default implementations for all {@link java.awt.event.KeyListener}
 * methods.  We only override {@code keyPressed}, which is the right hook
 * for game controls (fires immediately; auto-repeats on hold).</p>
 *
 * <h2>Design: Callback (GamePanel reference) vs Event Bus</h2>
 * <p>For a game of this scale, injecting a direct reference to
 * {@link GamePanel} and calling its methods is the simplest correct
 * approach.  An event-bus or command-queue pattern would decouple input
 * from game logic at the cost of more boilerplate — worthwhile in a larger
 * engine but overkill here.</p>
 *
 * <h2>Soft-drop vs hard-drop</h2>
 * <ul>
 *   <li><b>Down arrow</b> — soft drop: moves the piece one row down
 *       immediately (also resets the fall timer to prevent double-drops).</li>
 *   <li><b>Space</b> — hard drop: teleports the piece to its ghost position
 *       and locks it immediately.</li>
 * </ul>
 */
public class InputHandler extends KeyAdapter {

    /** The game panel that owns the game state. */
    private final GamePanel gamePanel;

    /**
     * Constructs an InputHandler that forwards commands to the given panel.
     *
     * @param gamePanel the game panel to control
     */
    public InputHandler(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    /**
     * Fired whenever a key is pressed (and auto-repeated by the OS).
     * We switch on the key code and delegate to the appropriate
     * {@link GamePanel} method.
     *
     * @param e the key event from the Swing event dispatch thread
     */
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
