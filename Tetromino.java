import java.awt.Color;

/**
 * Tetromino.java — Represents the single, currently-falling piece.
 *
 * <h2>Responsibility</h2>
 * <p>A {@code Tetromino} knows its {@link TetrominoType}, its position
 * (boardCol, boardRow) — which is the top-left corner of the 4×4 bounding
 * box — and its current rotation state (0–3).  It does <em>not</em> know
 * about the Board; collision detection is the Board's job.</p>
 *
 * <h2>Coordinate system</h2>
 * <pre>
 *   col →  0  1  2  3  4  5  6  7  8  9
 *   row
 *    0     [ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]
 *    1     [ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]
 *    ...
 *   19     [ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]
 * </pre>
 * <p>The piece spawns with its top-left bounding box at col=3, row=-1
 * (just above the visible board), so that the piece slides into view
 * naturally on the first tick.</p>
 *
 * <h2>Immutability strategy</h2>
 * <p>We deliberately keep Tetromino mutable (its position and rotation
 * change frequently).  The Board stores only colours of locked cells —
 * the Tetromino is the one moving entity.  This is the classic MVC split:
 * Board = model of settled state; Tetromino = transient moving model.</p>
 */
public class Tetromino {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** Width/height of the 4×4 bounding box used by every piece. */
    public static final int GRID = 4;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /** Which of the 7 piece types this is. */
    private final TetrominoType type;

    /**
     * Column of the left edge of the 4×4 bounding box on the board.
     * May be negative (piece partially off the left side).
     */
    private int col;

    /**
     * Row of the top edge of the 4×4 bounding box on the board.
     * Negative during the spawn "slide-in" phase.
     */
    private int row;

    /** Current rotation index: 0=spawn, 1=CW90, 2=180, 3=CCW90. */
    private int rotation;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new Tetromino at the standard spawn position.
     *
     * @param type the piece type to create
     */
    public Tetromino(TetrominoType type) {
        this.type     = type;
        this.rotation = 0;
        // Spawn horizontally centred.  The board is 10 cols wide; the bounding
        // box is 4 wide; (10 - 4) / 2 = 3.
        this.col = 3;
        // Start one row above the visible area so the first downward tick
        // brings it into view at row 0.
        this.row = -1;
    }

    // -------------------------------------------------------------------------
    // Movement — these modify state in place; the caller must validate first
    // -------------------------------------------------------------------------

    /** Shifts the piece one column to the left. */
    public void moveLeft()  { col--; }

    /** Shifts the piece one column to the right. */
    public void moveRight() { col++; }

    /** Drops the piece one row downward. */
    public void moveDown()  { row++; }

    /** Moves the piece upward one row (used to undo an invalid move). */
    public void moveUp()    { row--; }

    /**
     * Rotates clockwise (increments rotation index mod 4).
     * The caller is responsible for checking collision after calling this,
     * and calling {@link #rotateCounterClockwise()} to undo if invalid.
     */
    public void rotateClockwise()        { rotation = (rotation + 1) & 3; }

    /**
     * Rotates counter-clockwise (decrements rotation index mod 4).
     */
    public void rotateCounterClockwise() { rotation = (rotation + 3) & 3; }

    // -------------------------------------------------------------------------
    // Cell enumeration
    // -------------------------------------------------------------------------

    /**
     * Returns the board column of cell {@code i} in the current rotation,
     * where {@code i} ranges 0–15 in the 4×4 bounding box.
     *
     * @param i cell index (0–15)
     * @return board column, may be out of bounds
     */
    public int cellCol(int i) {
        return col + (i % GRID);
    }

    /**
     * Returns the board row of cell {@code i} in the current rotation.
     *
     * @param i cell index (0–15)
     * @return board row, may be out of bounds (negative = above visible area)
     */
    public int cellRow(int i) {
        return row + (i / GRID);
    }

    /**
     * Returns {@code true} if cell {@code i} is filled for the current
     * rotation state.
     *
     * @param i cell index (0–15)
     */
    public boolean isFilled(int i) {
        return type.getCells(rotation)[i] == 1;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /** @return the piece colour (delegated from type). */
    public Color getColor() { return type.color; }

    /** @return the piece type. */
    public TetrominoType getType() { return type; }

    /** @return current top-left column of the bounding box. */
    public int getCol() { return col; }

    /** @return current top-left row of the bounding box. */
    public int getRow() { return row; }

    /** @return current rotation index (0–3). */
    public int getRotation() { return rotation; }
}
