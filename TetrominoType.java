import java.awt.Color;

/**
 * TetrominoType.java — Enum representing the 7 classic Tetris pieces.
 *
 * <h2>Design: Rotation tables stored as data, not computed at runtime</h2>
 * <p>Each rotation state is encoded as a flat {@code int[]} of length 16,
 * representing a 4×4 bitmask (row-major, top-left = index 0).  A {@code 1}
 * means the cell is filled; {@code 0} means empty.</p>
 *
 * <p>Alternative approaches considered:</p>
 * <ul>
 *   <li><b>Matrix rotation (transpose + flip):</b> Elegant for square pieces
 *       but the I-piece is 4×1, so a 4×4 transpose doesn't produce the
 *       canonical Tetris rotation.  Hard-coding is simpler and matches the
 *       Tetris Guideline exactly.</li>
 *   <li><b>Wall-kick tables (SRS):</b> The Super-Rotation System allows a
 *       piece to "kick" off walls/floor when rotating near edges.  We keep a
 *       simplified version: try the rotation, if it collides try shifting left
 *       or right by 1.  Full SRS would add per-piece kick offset tables — a
 *       good extension exercise.</li>
 * </ul>
 *
 * <p>The 4 rotations for each piece are stored in order: 0°, 90°, 180°, 270°.
 * Rotating clockwise increments the rotation index mod 4; counter-clockwise
 * decrements it.</p>
 */
public enum TetrominoType {

    // -------------------------------------------------------------------------
    // I — cyan, 4×1 bar
    // -------------------------------------------------------------------------
    I(new Color(0, 240, 240), new int[][]{
        {
            0,0,0,0,
            1,1,1,1,
            0,0,0,0,
            0,0,0,0
        },
        {
            0,0,1,0,
            0,0,1,0,
            0,0,1,0,
            0,0,1,0
        },
        {
            0,0,0,0,
            0,0,0,0,
            1,1,1,1,
            0,0,0,0
        },
        {
            0,1,0,0,
            0,1,0,0,
            0,1,0,0,
            0,1,0,0
        }
    }),

    // -------------------------------------------------------------------------
    // O — yellow, 2×2 square (only 1 unique rotation; we store 4 identical)
    // -------------------------------------------------------------------------
    O(new Color(240, 240, 0), new int[][]{
        {
            0,1,1,0,
            0,1,1,0,
            0,0,0,0,
            0,0,0,0
        },
        {
            0,1,1,0,
            0,1,1,0,
            0,0,0,0,
            0,0,0,0
        },
        {
            0,1,1,0,
            0,1,1,0,
            0,0,0,0,
            0,0,0,0
        },
        {
            0,1,1,0,
            0,1,1,0,
            0,0,0,0,
            0,0,0,0
        }
    }),

    // -------------------------------------------------------------------------
    // T — purple, T-shape
    // -------------------------------------------------------------------------
    T(new Color(160, 0, 240), new int[][]{
        {
            0,1,0,0,
            1,1,1,0,
            0,0,0,0,
            0,0,0,0
        },
        {
            0,1,0,0,
            0,1,1,0,
            0,1,0,0,
            0,0,0,0
        },
        {
            0,0,0,0,
            1,1,1,0,
            0,1,0,0,
            0,0,0,0
        },
        {
            0,1,0,0,
            1,1,0,0,
            0,1,0,0,
            0,0,0,0
        }
    }),

    // -------------------------------------------------------------------------
    // S — green, S-skew
    // -------------------------------------------------------------------------
    S(new Color(0, 240, 0), new int[][]{
        {
            0,1,1,0,
            1,1,0,0,
            0,0,0,0,
            0,0,0,0
        },
        {
            0,1,0,0,
            0,1,1,0,
            0,0,1,0,
            0,0,0,0
        },
        {
            0,0,0,0,
            0,1,1,0,
            1,1,0,0,
            0,0,0,0
        },
        {
            1,0,0,0,
            1,1,0,0,
            0,1,0,0,
            0,0,0,0
        }
    }),

    // -------------------------------------------------------------------------
    // Z — red, Z-skew
    // -------------------------------------------------------------------------
    Z(new Color(240, 0, 0), new int[][]{
        {
            1,1,0,0,
            0,1,1,0,
            0,0,0,0,
            0,0,0,0
        },
        {
            0,0,1,0,
            0,1,1,0,
            0,1,0,0,
            0,0,0,0
        },
        {
            0,0,0,0,
            1,1,0,0,
            0,1,1,0,
            0,0,0,0
        },
        {
            0,1,0,0,
            1,1,0,0,
            1,0,0,0,
            0,0,0,0
        }
    }),

    // -------------------------------------------------------------------------
    // J — blue, J-shape
    // -------------------------------------------------------------------------
    J(new Color(0, 0, 240), new int[][]{
        {
            1,0,0,0,
            1,1,1,0,
            0,0,0,0,
            0,0,0,0
        },
        {
            0,1,1,0,
            0,1,0,0,
            0,1,0,0,
            0,0,0,0
        },
        {
            0,0,0,0,
            1,1,1,0,
            0,0,1,0,
            0,0,0,0
        },
        {
            0,1,0,0,
            0,1,0,0,
            1,1,0,0,
            0,0,0,0
        }
    }),

    // -------------------------------------------------------------------------
    // L — orange, L-shape
    // -------------------------------------------------------------------------
    L(new Color(240, 160, 0), new int[][]{
        {
            0,0,1,0,
            1,1,1,0,
            0,0,0,0,
            0,0,0,0
        },
        {
            0,1,0,0,
            0,1,0,0,
            0,1,1,0,
            0,0,0,0
        },
        {
            0,0,0,0,
            1,1,1,0,
            1,0,0,0,
            0,0,0,0
        },
        {
            1,1,0,0,
            0,1,0,0,
            0,1,0,0,
            0,0,0,0
        }
    });

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** The render colour for this piece type. */
    final Color color;

    /**
     * rotations[r][i] — cell i (0..15) is filled in rotation state r (0..3).
     * Index mapping: row = i/4, col = i%4.
     */
    final int[][] rotations;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    TetrominoType(Color color, int[][] rotations) {
        this.color     = color;
        this.rotations = rotations;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the cells for a given rotation state.
     *
     * @param rotation 0=spawn, 1=CW, 2=180°, 3=CCW
     * @return 16-element array (4×4 bitmask, row-major)
     */
    public int[] getCells(int rotation) {
        return rotations[rotation & 3]; // & 3 is equivalent to % 4 for safety
    }

    /**
     * Returns a random TetrominoType using a simple uniform distribution.
     *
     * <p>Design note: A proper Tetris implementation uses a "bag" randomiser
     * (7-bag): shuffle all 7 types, deal them out, refill — guaranteeing you
     * see each piece at least once per 7 pieces.  For simplicity we use
     * {@link Math#random()} here, which can produce long droughts of any
     * single piece.  Extending to a bag randomiser is a good exercise.</p>
     */
    public static TetrominoType random() {
        TetrominoType[] values = values();
        return values[(int) (Math.random() * values.length)];
    }
}
