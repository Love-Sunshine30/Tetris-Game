import java.awt.Color;


public class Board {


    public static final int COLS = 10;

    public static final int ROWS = 20;


    private final Color[][] grid;

   
    public Board() {
        grid = new Color[ROWS][COLS];

    }


    public Color getCell(int row, int col) {
        return grid[row][col];
    }

    public boolean isOccupied(int row, int col) {
       
        if (row < 0) return false;
        
        if (row >= ROWS || col < 0 || col >= COLS) return true;

        return grid[row][col] != null;
    }

 
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


    public void lock(Tetromino piece) {
        for (int i = 0; i < 16; i++) {
            if (piece.isFilled(i)) {
                int r = piece.cellRow(i);
                int c = piece.cellCol(i);

                if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
                    grid[r][c] = piece.getColor();
                }
            }
        }
    }

 
    public int clearLines() {
        int linesCleared = 0;
        int writeRow = ROWS - 1; 

        
        for (int readRow = ROWS - 1; readRow >= 0; readRow--) {
            if (isRowComplete(readRow)) {
                linesCleared++;
                
            } else {
                
                if (writeRow != readRow) {
                    System.arraycopy(grid[readRow], 0, grid[writeRow], 0, COLS);
                }
                writeRow--;
            }
        }

        
        for (int r = writeRow; r >= 0; r--) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = null;
            }
        }

        return linesCleared;
    }

    private boolean isRowComplete(int row) {
        for (int c = 0; c < COLS; c++) {
            if (grid[row][c] == null) return false;
        }
        return true;
    }


    public boolean isTopReached() {
        for (int c = 0; c < COLS; c++) {
            if (grid[0][c] != null) return true;
        }
        return false;
    }

    public void clear() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = null;
            }
        }
    }
}
