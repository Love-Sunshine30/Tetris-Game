import java.awt.Color;

public class Tetromino {


    // Width/height of the 4×4 bounding box used by every piece.
    public static final int GRID = 4;

    // Which of the 7 piece types this is.
    private final TetrominoType type;

    private int col;
    
    private int row;

    private int rotation;

    public Tetromino(TetrominoType type) {
        this.type     = type;
        this.rotation = 0;

        this.col = 3;
     
        this.row = -1;
    }

    //Shifts the piece one column to the left.
    public void moveLeft()  { col--; }

    // Shifts the piece one column to the right. 
    public void moveRight() { col++; }

    // Drops the piece one row downward. 
    public void moveDown()  { row++; }

    //  Moves the piece upward one row (used to undo an invalid move).
    public void moveUp()    { row--; }

    
    public void rotateClockwise()        { rotation = (rotation + 1) & 3; }


    public void rotateCounterClockwise() { rotation = (rotation + 3) & 3; }


    public int cellCol(int i) {
        return col + (i % GRID);
    }


    public int cellRow(int i) {
        return row + (i / GRID);
    }


    public boolean isFilled(int i) {
        return type.getCells(rotation)[i] == 1;
    }

  
    public Color getColor() { return type.color; }


    public int getCol() { return col; }

    public int getRow() { return row; }

    public int getRotation() { return rotation; }
}
