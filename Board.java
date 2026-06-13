import java.awt.Color;

/**
 * Board.java — The 10×20 grid of settled (locked) cells.
 *
 * <h2>Responsibilities</h2>
 * <ol>
 *   <li>Store the colour of every locked cell (null = empty).</li>
 *   <li>Perform collision detection for a {@link Tetromino}.</li>
 *   <li>Lock a piece into the grid when it lands.</li>
 *   <li>Detect and clear complete horizontal lines.</li>
 *   <li>Detect a game-over condition (piece spawns into occupied cells).</li>
 * </ol>
 *
 * <h2>Data structure choice</h2>
 * <p>A 2-D {@code Color[][]} (indexed [row][col]) is the simplest
 * representation.  {@code null} represents an empty cell; a non-null
 * {@link Color} represents a locked cell.  We avoid a separate boolean
 * "occupied" array because the colour IS the data — no redundancy.</p>
 *
 * <h2>Why not store the active piece here?</h2>
 * <p>Separation of concerns: the Board is the persistent, settled state.
 * The active {@link Tetromino} is transient and lives in {@link GamePanel}.
 * This makes rendering, ghost-piece calculation, and undo logic cleaner.</p>
 */
public class Board {

    // -------------------------------------------------------------------------
    // Dimensions (classic Tetris standard)
    // -------------------------------------------------------------------------

    /** Number of visible columns. */
    public static final int COLS = 10;

    /**
     * Number of visible rows.  The standard Tetris playfield is 20 rows
     * tall (some implementations add a 2-row hidden "buffer zone" at the
     * top — we omit that for simplicity).
     */
    public static final int ROWS = 20;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /**
     * grid[row][col] — null means empty; non-null holds the colour of a
     * locked Tetromino cell.
     *
     * <p>Using Color directly avoids a second lookup and keeps the rendering
     * code simple: if {@code grid[r][c] != null}, draw a filled rectangle
     * in that colour.</p>
     */
    private final Color[][] grid;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /** Creates an empty board. */
    public Board() {
        grid = new Color[ROWS][COLS];
        // Java initialises reference arrays to null — no explicit fill needed.
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    /**
     * Returns the colour of the cell at (row, col), or {@code null} if empty.
     *
     * @param row 0-based row (0 = top)
     * @param col 0-based column (0 = left)
     */
    public Color getCell(int row, int col) {
        return grid[row][col];
    }

    /**
     * Returns {@code true} if the given board position is occupied or
     * out-of-bounds.
     *
     * <p>Design note: treating out-of-bounds as "occupied" means the caller
     * never needs to range-check separately — one method covers walls, floor,
     * and settled cells simultaneously.</p>
     *
     * @param row board row (may be negative for above-board cells)
     * @param col board column
     */
    public boolean isOccupied(int row, int col) {
        // Above the board: treat as free (the piece spawns above row 0).
        if (row < 0) return false;
        // Below floor or outside side walls: blocked.
        if (row >= ROWS || col < 0 || col >= COLS) return true;
        // Check settled cell.
        return grid[row][col] != null;
    }

    // -------------------------------------------------------------------------
    // Collision detection
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the given {@link Tetromino} (at its current
     * position and rotation) overlaps any settled cell or board boundary.
     *
     * <p>We iterate all 16 cells of the 4×4 bounding box, skip empty cells,
     * and test only the filled ones.  This is O(16) = O(1) per call.</p>
     *
     * @param piece the piece to test (not modified)
     */
    public boolean collides(Tetromino piece) {
        for (int i = 0; i < 16; i++) {
            if (piece.isFilled(i)) {
                int r = piece.cellRow(i);
                int c = piece.cellCol(i);
                if (isOccupied(r, c)) return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Piece locking
    // -------------------------------------------------------------------------

    /**
     * Locks the given piece into the grid.  Called when the piece can no
     * longer move down.
     *
     * <p>After locking, call {@link #clearLines()} to remove complete rows.</p>
     *
     * @param piece the piece to lock (not modified)
     */
    public void lock(Tetromino piece) {
        for (int i = 0; i < 16; i++) {
            if (piece.isFilled(i)) {
                int r = piece.cellRow(i);
                int c = piece.cellCol(i);
                // Cells above the board (row < 0) are discarded; this can
                // happen if the player rotates a piece while it's partly
                // above the top edge.
                if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
                    grid[r][c] = piece.getColor();
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Line clearing
    // -------------------------------------------------------------------------

    /**
     * Scans every row from bottom to top, removes any complete row, and
     * shifts the rows above it downward.
     *
     * <p>Returns the number of lines cleared in this call (0–4).  The
     * caller uses this to compute the score.</p>
     *
     * <h3>Algorithm</h3>
     * <p>We use a "write pointer" approach: {@code writeRow} starts at the
     * bottom and only advances when we keep a row.  Complete rows are
     * skipped (not copied), causing the rows above them to overwrite them
     * naturally.  This is O(ROWS × COLS) — optimal for this structure.</p>
     *
     * @return number of complete lines cleared (0–4)
     */
    public int clearLines() {
        int linesCleared = 0;
        int writeRow = ROWS - 1; // next row to write into (start at bottom)

        // Scan from bottom to top.
        for (int readRow = ROWS - 1; readRow >= 0; readRow--) {
            if (isRowComplete(readRow)) {
                linesCleared++;
                // Skip this row — don't copy it to writeRow.
            } else {
                // Copy readRow to writeRow (may be the same row, which is a
                // harmless self-copy).
                if (writeRow != readRow) {
                    System.arraycopy(grid[readRow], 0, grid[writeRow], 0, COLS);
                }
                writeRow--;
            }
        }

        // Any rows above writeRow were completely "eaten" by line clears;
        // fill them with null (empty).
        for (int r = writeRow; r >= 0; r--) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = null;
            }
        }

        return linesCleared;
    }

    /**
     * Returns {@code true} if every cell in the given row is non-null.
     *
     * @param row the row index to test
     */
    private boolean isRowComplete(int row) {
        for (int c = 0; c < COLS; c++) {
            if (grid[row][c] == null) return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Game-over detection
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if any cell in row 0 (the top visible row) is
     * occupied.  This is the standard "top-out" game-over condition.
     *
     * <p>Note: a more precise check would test the spawn columns of the next
     * piece, but checking the entire top row is simpler and widely accepted.</p>
     */
    public boolean isTopReached() {
        for (int c = 0; c < COLS; c++) {
            if (grid[0][c] != null) return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------

    /**
     * Clears the entire board (used when starting a new game).
     */
    public void clear() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = null;
            }
        }
    }
}
