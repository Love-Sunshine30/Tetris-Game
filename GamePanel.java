import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GamePanel extends JPanel implements ActionListener {

    
    private static final int CELL = 30;

    private static final int BOARD_W = Board.COLS * CELL;
    private static final int BOARD_H = Board.ROWS * CELL;
    private static final int SIDE_W  = 160;
    private static final int PANEL_W = BOARD_W + SIDE_W;
    private static final int PANEL_H = BOARD_H + 40; 

   
    private static final int[] LINE_SCORES = {0, 100, 300, 500, 800};

    private static final Color BG_COLOR      = new Color(15,  15,  25);
    private static final Color GRID_COLOR    = new Color(40,  40,  60);
    private static final Color BORDER_COLOR  = new Color(80,  80, 120);
    private static final Color GHOST_COLOR   = new Color(255, 255, 255, 50); 
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


    private final Timer gameTimer;
    private int timerDelay;

    
    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_W, PANEL_H));
        setBackground(BG_COLOR);
        setFocusable(true);

        board     = new Board();
        gameTimer = new Timer(500, this); 

        addKeyListener(new InputHandler(this));
    }


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

   
    private void spawnPiece() {
        activePiece = new Tetromino(nextType);
        nextType    = TetrominoType.random();

        if (board.collides(activePiece)) {
            gameOver = true;
            gameTimer.stop();
        }
    }


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


    private void lockAndSpawn() {
        board.lock(activePiece);

        int lines = board.clearLines();
        if (lines > 0) {
            score        += LINE_SCORES[lines] * level;
            linesCleared += lines;


            int newLevel = (linesCleared / 10) + 1;
            if (newLevel != level) {
                level      = newLevel;
                timerDelay = gravityDelay(level);
                gameTimer.setDelay(timerDelay);
            }
        }

        spawnPiece();
    }


    private int gravityDelay(int lvl) {
        return Math.max(100, 1000 - (lvl - 1) * 80);
    }


    public void movePieceLeft() {
        activePiece.moveLeft();
        if (board.collides(activePiece)) activePiece.moveRight(); 
        repaint();
    }

    public void movePieceRight() {
        activePiece.moveRight();
        if (board.collides(activePiece)) activePiece.moveLeft(); 
        repaint();
    }


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

    
    public void rotatePieceClockwise() {
        activePiece.rotateClockwise();
        if (!tryResolveCollisionAfterRotation()) {
            activePiece.rotateCounterClockwise();
        }
        repaint();
    }


    public void rotatePieceCounterClockwise() {
        activePiece.rotateCounterClockwise();
        if (!tryResolveCollisionAfterRotation()) {
            activePiece.rotateClockwise();
        }
        repaint();
    }


    private boolean tryResolveCollisionAfterRotation() {
        if (!board.collides(activePiece)) return true;

        activePiece.moveLeft();
        if (!board.collides(activePiece)) return true;

        activePiece.moveRight(); activePiece.moveRight();
        if (!board.collides(activePiece)) return true;

        activePiece.moveRight();
        if (!board.collides(activePiece)) return true;

        activePiece.moveLeft(); activePiece.moveLeft(); activePiece.moveLeft();
        return false;
    }

    public void togglePause() {
        paused = !paused;
        repaint();
    }

    public boolean isGameOver() { return gameOver; }
    public boolean isPaused()   { return paused;   }


    private int computeGhostRow() {
        int originalRow = activePiece.getRow();

        while (!board.collides(activePiece)) activePiece.moveDown();
        activePiece.moveUp();
        int ghostRow = activePiece.getRow();


        while (activePiece.getRow() > originalRow) activePiece.moveUp();

        return ghostRow;
    }


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
                if (r >= 0) drawCell(g, r, c, activePiece.getColor()); 
            }
        }
    }

    private void drawCell(Graphics2D g, int row, int col, Color color) {
        int x = col * CELL, y = row * CELL;

        g.setColor(color);
        g.fillRect(x + 1, y + 1, CELL - 2, CELL - 2);

        g.setColor(color.brighter());
        g.drawLine(x + 1,        y + 1, x + CELL - 2, y + 1);        
        g.drawLine(x + 1,        y + 1, x + 1,        y + CELL - 2); 

        g.setColor(color.darker());
        g.drawLine(x + 1,        y + CELL - 1, x + CELL - 1, y + CELL - 1); 
        g.drawLine(x + CELL - 1, y + 1,        x + CELL - 1, y + CELL - 1); 
    }

    private void drawBoardBorder(Graphics2D g) {
        g.setColor(BORDER_COLOR);
        g.drawRect(0, 0, BOARD_W - 1, BOARD_H - 1);
    }


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

    private void drawNextPiece(Graphics2D g, int x, int y) {
        int previewCell = 20;
        int[] cells = nextType.getCells(0); 

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
