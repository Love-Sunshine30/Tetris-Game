import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GamePanel extends JPanel implements ActionListener {

    // px per cell
    private static final int CELL = 30;

    private static final int BOARD_W = Board.COLS * CELL;
    private static final int BOARD_H = Board.ROWS * CELL;
    private static final int SIDE_W  = 160;
    private static final int PANEL_W = BOARD_W + SIDE_W;
    private static final int PANEL_H = BOARD_H + 40; // +40 for title bar

    // points for 1/2/3/4 lines cleared
    private static final int[] LINE_SCORES = {0, 100, 300, 500, 800};

    private static final Color BG_COLOR      = new Color(15,  15,  25);
    private static final Color GRID_COLOR    = new Color(40,  40,  60);
    private static final Color BORDER_COLOR  = new Color(80,  80, 120);
    private static final Color GHOST_COLOR   = new Color(255, 255, 255, 50); // translucent
    private static final Color TEXT_COLOR    = new Color(200, 200, 230);
    private static final Color LABEL_COLOR   = new Color(120, 120, 160);
    private static final Color PAUSED_OVERLAY= new Color(0,   0,   0,  160);

    private final Board   board;
    private Tetromino     activePiece;
    private TetrominoType nextType;

    private int  score;
    private int  level;
    private int  linesCleared;

    private boolean gameOver;
    private boolean paused;

    // Gravity timer — restarted on level change to apply new delay
    private final Timer gameTimer;
    private int timerDelay;

    /**
     * Initialises panel and board. Call {@link #startNewGame()} after
     * attaching to a window so repaint() works.
     */
    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_W, PANEL_H));
        setBackground(BG_COLOR);
        setFocusable(true);

        board     = new Board();
        gameTimer = new Timer(500, this); // delay overridden in startNewGame

        addKeyListener(new InputHandler(this));
    }

    /** Resets all state and starts fresh. Safe to call mid-game for restart. */
    public void startNewGame() {
        board.clear();
        score        = 0;
        level        = 1;
        linesCleared = 0;
        gameOver     = false;
        paused       = false;

        nextType   = TetrominoType.random();
        spawnPiece();

        timerDelay = gravityDelay(level);
        gameTimer.setDelay(timerDelay);
        gameTimer.restart();

        repaint();
    }

    /** Spawns next piece at the top; sets gameOver if spawn position is blocked. */
    private void spawnPiece() {
        activePiece = new Tetromino(nextType);
        nextType    = TetrominoType.random();

        if (board.collides(activePiece)) {
            gameOver = true;
            gameTimer.stop();
        }
    }

    /** Gravity tick — drops piece one row, locks if it can't move further. */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver || paused) return;

        activePiece.moveDown();
        if (board.collides(activePiece)) {
            activePiece.moveUp();
            lockAndSpawn();
        }

        repaint();
    }

    /** Locks active piece, clears lines, updates score/level, spawns next piece. */
    private void lockAndSpawn() {
        board.lock(activePiece);

        int lines = board.clearLines();
        if (lines > 0) {
            score        += LINE_SCORES[lines] * level;
            linesCleared += lines;

            // Level up every 10 lines
            int newLevel = (linesCleared / 10) + 1;
            if (newLevel != level) {
                level      = newLevel;
                timerDelay = gravityDelay(level);
                gameTimer.setDelay(timerDelay);
            }
        }

        spawnPiece();
    }

    /** 1000 ms at level 1, -80 ms per level, floor 100 ms. */
    private int gravityDelay(int lvl) {
        return Math.max(100, 1000 - (lvl - 1) * 80);
    }

    // ---- Player commands (called by InputHandler) ----------------------------

    public void movePieceLeft() {
        activePiece.moveLeft();
        if (board.collides(activePiece)) activePiece.moveRight(); // undo
        repaint();
    }

    public void movePieceRight() {
        activePiece.moveRight();
        if (board.collides(activePiece)) activePiece.moveLeft(); // undo
        repaint();
    }

    /** Soft drop: 1 bonus point per row (Tetris Guideline). */
    public void softDrop() {
        activePiece.moveDown();
        if (board.collides(activePiece)) {
            activePiece.moveUp();
            lockAndSpawn();
        } else {
            score++;
        }
        repaint();
    }

    /** Hard drop: teleports to ghost position, 2 pts/row. */
    public void hardDrop() {
        int rowsBefore = activePiece.getRow();
        while (!board.collides(activePiece)) {
            activePiece.moveDown();
        }
        activePiece.moveUp();
        score += (activePiece.getRow() - rowsBefore) * 2;
        lockAndSpawn();
        repaint();
    }

    /** Rotates CW with wall-kick; undoes rotation if no kick resolves it. */
    public void rotatePieceClockwise() {
        activePiece.rotateClockwise();
        if (!tryResolveCollisionAfterRotation()) {
            activePiece.rotateCounterClockwise();
        }
        repaint();
    }

    /** Rotates CCW with wall-kick; undoes rotation if no kick resolves it. */
    public void rotatePieceCounterClockwise() {
        activePiece.rotateCounterClockwise();
        if (!tryResolveCollisionAfterRotation()) {
            activePiece.rotateClockwise();
        }
        repaint();
    }

    /**
     * Tries to resolve a post-rotation collision by nudging ±1 or ±2 columns.
     * @return true if position is valid (possibly after kicking)
     */
    private boolean tryResolveCollisionAfterRotation() {
        if (!board.collides(activePiece)) return true;

        activePiece.moveLeft();
        if (!board.collides(activePiece)) return true;

        activePiece.moveRight(); activePiece.moveRight();
        if (!board.collides(activePiece)) return true;

        // Extra kick right for I-piece near walls
        activePiece.moveRight();
        if (!board.collides(activePiece)) return true;

        // Restore original column
        activePiece.moveLeft(); activePiece.moveLeft(); activePiece.moveLeft();
        return false;
    }

    public void togglePause() {
        paused = !paused;
        repaint();
    }

    public boolean isGameOver() { return gameOver; }
    public boolean isPaused()   { return paused;   }

    // ---- Ghost piece ---------------------------------------------------------

    /**
     * Simulates dropping the active piece to find its landing row.
     * Restores the piece to its original position before returning.
     */
    private int computeGhostRow() {
        int originalRow = activePiece.getRow();

        while (!board.collides(activePiece)) activePiece.moveDown();
        activePiece.moveUp();
        int ghostRow = activePiece.getRow();

        // Move back up — no direct row setter available
        while (activePiece.getRow() > originalRow) activePiece.moveUp();

        return ghostRow;
    }

    // ---- Rendering -----------------------------------------------------------

    /**
     * Painter's-algorithm render order:
     * background → grid → locked cells → ghost → active piece →
     * border → side panel → overlay (pause/game over)
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(g2);
        drawGridLines(g2);
        drawLockedCells(g2);

        if (!gameOver && !paused && activePiece != null) {
            drawGhostPiece(g2);
            drawActivePiece(g2);
        }

        drawBoardBorder(g2);
        drawSidePanel(g2);

        if (paused)   drawPauseOverlay(g2);
        if (gameOver) drawGameOverOverlay(g2);
    }

    private void drawBackground(Graphics2D g) {
        g.setColor(BG_COLOR);
        g.fillRect(0, 0, BOARD_W, BOARD_H);
    }

    private void drawGridLines(Graphics2D g) {
        g.setColor(GRID_COLOR);
        for (int r = 0; r <= Board.ROWS; r++) g.drawLine(0, r * CELL, BOARD_W, r * CELL);
        for (int c = 0; c <= Board.COLS; c++) g.drawLine(c * CELL, 0, c * CELL, BOARD_H);
    }

    private void drawLockedCells(Graphics2D g) {
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Color color = board.getCell(r, c);
                if (color != null) drawCell(g, r, c, color);
            }
        }
    }

    /** Translucent shadow showing where the active piece will land. */
    private void drawGhostPiece(Graphics2D g) {
        int rowOffset = computeGhostRow() - activePiece.getRow();
        if (rowOffset <= 0) return;

        for (int i = 0; i < 16; i++) {
            if (activePiece.isFilled(i)) {
                int r = activePiece.cellRow(i) + rowOffset;
                int c = activePiece.cellCol(i);
                if (r >= 0 && r < Board.ROWS) drawCell(g, r, c, GHOST_COLOR);
            }
        }
    }

    private void drawActivePiece(Graphics2D g) {
        for (int i = 0; i < 16; i++) {
            if (activePiece.isFilled(i)) {
                int r = activePiece.cellRow(i);
                int c = activePiece.cellCol(i);
                if (r >= 0) drawCell(g, r, c, activePiece.getColor()); // clip off-screen rows
            }
        }
    }

    /** Filled rect with bevel: bright top-left, dark bottom-right edges. */
    private void drawCell(Graphics2D g, int row, int col, Color color) {
        int x = col * CELL, y = row * CELL;

        g.setColor(color);
        g.fillRect(x + 1, y + 1, CELL - 2, CELL - 2);

        g.setColor(color.brighter());
        g.drawLine(x + 1,        y + 1, x + CELL - 2, y + 1);        // top
        g.drawLine(x + 1,        y + 1, x + 1,        y + CELL - 2); // left

        g.setColor(color.darker());
        g.drawLine(x + 1,        y + CELL - 1, x + CELL - 1, y + CELL - 1); // bottom
        g.drawLine(x + CELL - 1, y + 1,        x + CELL - 1, y + CELL - 1); // right
    }

    private void drawBoardBorder(Graphics2D g) {
        g.setColor(BORDER_COLOR);
        g.drawRect(0, 0, BOARD_W - 1, BOARD_H - 1);
    }

    // ---- Side panel ----------------------------------------------------------

    private void drawSidePanel(Graphics2D g) {
        int x = BOARD_W + 10;

        g.setColor(new Color(20, 20, 35));
        g.fillRect(BOARD_W, 0, SIDE_W, PANEL_H);

        drawLabel(g, "SCORE", x, 50);  drawValue(g, String.valueOf(score),        x, 72);
        drawLabel(g, "LEVEL", x, 110); drawValue(g, String.valueOf(level),        x, 132);
        drawLabel(g, "LINES", x, 170); drawValue(g, String.valueOf(linesCleared), x, 192);

        drawLabel(g, "NEXT", x, 240);
        if (nextType != null) drawNextPiece(g, x, 260);

        drawHints(g, x, 440);
    }

    private void drawLabel(Graphics2D g, String text, int x, int y) {
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.setColor(LABEL_COLOR);
        g.drawString(text, x, y);
    }

    private void drawValue(Graphics2D g, String text, int x, int y) {
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.setColor(TEXT_COLOR);
        g.drawString(text, x, y);
    }

    /** 4×4 preview using a smaller cell size. */
    private void drawNextPiece(Graphics2D g, int x, int y) {
        int previewCell = 20;
        int[] cells = nextType.getCells(0); // spawn rotation

        for (int i = 0; i < 16; i++) {
            if (cells[i] == 1) {
                int px = x + (i % 4) * previewCell;
                int py = y + (i / 4) * previewCell;

                g.setColor(nextType.color);
                g.fillRect(px + 1, py + 1, previewCell - 2, previewCell - 2);
                g.setColor(nextType.color.brighter());
                g.drawLine(px + 1, py + 1, px + previewCell - 2, py + 1);
                g.drawLine(px + 1, py + 1, px + 1, py + previewCell - 2);
            }
        }
    }

    private void drawHints(Graphics2D g, int x, int y) {
        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.setColor(LABEL_COLOR);
        String[] hints = {"← → Move", "↑ / X Rotate CW", "Z Rotate CCW",
                          "↓ Soft drop", "SPC Hard drop", "P Pause"};
        for (int i = 0; i < hints.length; i++) g.drawString(hints[i], x, y + i * 15);
    }

    // ---- Overlays ------------------------------------------------------------

    private void drawPauseOverlay(Graphics2D g) {
        g.setColor(PAUSED_OVERLAY);
        g.fillRect(0, 0, BOARD_W, BOARD_H);

        g.setFont(new Font("Monospaced", Font.BOLD, 28));
        g.setColor(TEXT_COLOR);
        drawCentredString(g, "PAUSED", BOARD_W / 2, BOARD_H / 2 - 15);

        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g.setColor(LABEL_COLOR);
        drawCentredString(g, "Press P to resume", BOARD_W / 2, BOARD_H / 2 + 20);
    }

    private void drawGameOverOverlay(Graphics2D g) {
        g.setColor(PAUSED_OVERLAY);
        g.fillRect(0, 0, BOARD_W, BOARD_H);

        g.setFont(new Font("Monospaced", Font.BOLD, 28));
        g.setColor(new Color(240, 80, 80));
        drawCentredString(g, "GAME OVER", BOARD_W / 2, BOARD_H / 2 - 30);

        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.setColor(TEXT_COLOR);
        drawCentredString(g, "Score: " + score, BOARD_W / 2, BOARD_H / 2 + 5);

        g.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g.setColor(LABEL_COLOR);
        drawCentredString(g, "Press ENTER to restart", BOARD_W / 2, BOARD_H / 2 + 35);
    }

    /** Centres a string horizontally at (cx, y) using FontMetrics. */
    private void drawCentredString(Graphics2D g, String text, int cx, int y) {
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, cx - textWidth / 2, y);
    }
}