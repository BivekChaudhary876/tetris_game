package au.edu.Griffith.ai;

import au.edu.Griffith.model.Board;
import au.edu.Griffith.model.Position;
import au.edu.Griffith.model.tetromino.AbstractTetromino;
import au.edu.Griffith.model.tetromino.TetrominoType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Picks where to put the current piece: tries every rotation in every column,
 * scores each result with {@link BoardEvaluator}, returns the best.
 *
 * <p><b>Pass snapshots, not live objects.</b> The main
 * {@link #findBestMove(boolean[][], List, List)} takes a copied grid and a
 * copied cell list. AIPlayer runs this on a background thread while the game
 * mutates its piece and board on the JavaFX thread, so reading them live here
 * throws — and the executor hides the exception, leaving the AI silently
 * inert.</p>
 *
 * <p><b>Lookahead.</b> Each candidate is scored as "this board, plus the best
 * the next piece could then do". Scoring only the current piece makes the AI
 * pick spots that leave nowhere good to go next, and it dies within a few lines.
 * The next-piece preview is already on screen, so this costs us nothing.</p>
 *
 * <p><b>Rotation copies the game's own rule.</b> The game turns a piece about
 * whatever cell is at index 1 (see {@link AbstractTetromino#projectRotated()}),
 * not by rotating a shape matrix. {@link #rotateOnce} repeats that same maths so
 * we plan for shapes the game will actually produce.</p>
 */
public class TetrisAI {

    private static final int MAX_ROTATIONS = 4;

    /** Score for a placement the next piece cannot follow — a dead end, not just a bad move. */
    private static final double DEAD_END = -1000.0;

    private final BoardEvaluator evaluator;

    public TetrisAI() {
        this(new BoardEvaluator());
    }

    public TetrisAI(BoardEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * @param grid       board snapshot, {@code [row][col]}
     * @param pieceCells the active piece's cells, copied by the caller
     * @param nextCells  the previewed next piece, or null to skip lookahead
     * @return best placement, or null if the piece fits nowhere (board is full)
     */
    public Move findBestMove(boolean[][] grid, List<Position> pieceCells, List<Position> nextCells) {
        Move best = null;

        List<List<Position>> rotations = distinctRotations(pieceCells);
        for (int rotation = 0; rotation < rotations.size(); rotation++) {
            List<Position> shape = rotations.get(rotation);
            int shapeWidth = width(shape);

            for (int col = 0; col <= grid[0].length - shapeWidth; col++) {
                boolean[][] after = simulateDrop(grid, shape, col);
                if (after == null) {
                    continue; // piece does not fit in this column
                }

                double score = evaluator.evaluate(after, clearFullRows(after));

                if (nextCells != null) {
                    Double followUp = bestFollowUpScore(after, nextCells);
                    score += followUp != null ? followUp : DEAD_END;
                }

                if (best == null || score > best.score()) {
                    best = new Move(col, rotation, score);
                }
            }
        }
        return best;
    }

    /** Convenience for tests. Do not call from a background thread — see class comment. */
    public Move findBestMove(Board board, AbstractTetromino piece) {
        return findBestMove(toGrid(board), piece.getCells(), null);
    }

    /** Spawn cells of a shape, safe to hand to another thread. */
    public static List<Position> spawnCells(TetrominoType type) {
        List<Position> cells = new ArrayList<>(4);
        for (int[] offset : type.spawnOffsets()) {
            cells.add(new Position(offset[0], offset[1]));
        }
        return cells;
    }

    /** Copies the locked cells. The AI only needs filled vs. empty, not colour. */
    public static boolean[][] toGrid(Board board) {
        boolean[][] grid = new boolean[board.getHeight()][board.getWidth()];
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                grid[row][col] = board.cellAt(col, row) != null;
            }
        }
        return grid;
    }

    /** Best score the next piece could reach on this board, or null if it fits nowhere. */
    private Double bestFollowUpScore(boolean[][] grid, List<Position> pieceCells) {
        Double best = null;

        for (List<Position> shape : distinctRotations(pieceCells)) {
            int shapeWidth = width(shape);
            for (int col = 0; col <= grid[0].length - shapeWidth; col++) {
                boolean[][] after = simulateDrop(grid, shape, col);
                if (after == null) {
                    continue;
                }
                double score = evaluator.evaluate(after, clearFullRows(after));
                if (best == null || score > best) {
                    best = score;
                }
            }
        }
        return best;
    }

    /**
     * The piece's different orientations, in rotation order — index 0 is as given,
     * index 1 is one ROTATE later, and so on.
     *
     * <p>Repeats are dropped, so O gives 1 entry and S/Z give 2. That is the
     * tutorial's "don't test all four rotations" optimisation, done by comparing
     * shapes instead of hardcoding a per-piece table.</p>
     */
    public List<List<Position>> distinctRotations(List<Position> pieceCells) {
        List<List<Position>> rotations = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        List<Position> cells = pieceCells;
        for (int turn = 0; turn < MAX_ROTATIONS; turn++) {
            if (turn > 0) {
                cells = rotateOnce(cells);
            }
            if (!seen.add(orientationSignature(cells))) {
                break; // seen this shape already, so all later turns repeat too
            }
            rotations.add(cells);
        }
        return rotations;
    }

    /** One clockwise turn about the cell at index 1 — same maths as the game uses. */
    private List<Position> rotateOnce(List<Position> cells) {
        Position pivot = cells.get(1);
        List<Position> rotated = new ArrayList<>(cells.size());
        for (Position cell : cells) {
            int relativeCol = cell.col() - pivot.col();
            int relativeRow = cell.row() - pivot.row();
            rotated.add(new Position(
                    pivot.col() - relativeRow,
                    pivot.row() + relativeCol));
        }
        return rotated;
    }

    /**
     * Drops the shape straight down column {@code targetCol}.
     *
     * @return the resulting grid, or null if the shape does not fit there.
     *         No tucks or slides under overhangs — a plain vertical drop.
     */
    public boolean[][] simulateDrop(boolean[][] grid, List<Position> shape, int targetCol) {
        List<Position> cells = normalise(shape, targetCol);
        if (!fits(grid, cells)) {
            return null;
        }

        int drop = 0;
        while (fits(grid, translate(cells, 0, drop + 1))) {
            drop++;
        }

        boolean[][] after = copy(grid);
        for (Position cell : translate(cells, 0, drop)) {
            after[cell.row()][cell.col()] = true;
        }
        return after;
    }

    /** Removes full rows in place and drops the rows above down. Returns how many went. */
    public int clearFullRows(boolean[][] grid) {
        int width = grid[0].length;
        int writeRow = grid.length - 1;
        int cleared = 0;

        for (int readRow = grid.length - 1; readRow >= 0; readRow--) {
            boolean full = true;
            for (int col = 0; col < width; col++) {
                if (!grid[readRow][col]) {
                    full = false;
                    break;
                }
            }

            if (full) {
                cleared++;
            } else {
                System.arraycopy(grid[readRow], 0, grid[writeRow], 0, width);
                writeRow--;
            }
        }

        while (writeRow >= 0) {
            java.util.Arrays.fill(grid[writeRow], false);
            writeRow--;
        }
        return cleared;
    }

    /**
     * Identifies a shape regardless of where it sits on the board.
     *
     * <p>AIPlayer uses this to check whether a ROTATE was actually accepted,
     * rather than assuming it was.</p>
     */
    public static String orientationSignature(List<Position> shape) {
        int minCol = Integer.MAX_VALUE;
        int minRow = Integer.MAX_VALUE;
        for (Position cell : shape) {
            minCol = Math.min(minCol, cell.col());
            minRow = Math.min(minRow, cell.row());
        }

        final int colShift = minCol;
        final int rowShift = minRow;
        return shape.stream()
                .map(cell -> (cell.col() - colShift) + ":" + (cell.row() - rowShift))
                .sorted()
                .reduce("", (a, b) -> a + "," + b);
    }

    /** True if every cell is inside the grid and empty. */
    private boolean fits(boolean[][] grid, List<Position> cells) {
        for (Position cell : cells) {
            if (cell.col() < 0 || cell.col() >= grid[0].length
                    || cell.row() < 0 || cell.row() >= grid.length) {
                return false;
            }
            if (grid[cell.row()][cell.col()]) {
                return false;
            }
        }
        return true;
    }

    /** Moves the shape so its left edge is at targetCol and its top edge at row 0. */
    private List<Position> normalise(List<Position> shape, int targetCol) {
        int minCol = Integer.MAX_VALUE;
        int minRow = Integer.MAX_VALUE;
        for (Position cell : shape) {
            minCol = Math.min(minCol, cell.col());
            minRow = Math.min(minRow, cell.row());
        }
        return translate(shape, targetCol - minCol, -minRow);
    }

    private List<Position> translate(List<Position> cells, int dCol, int dRow) {
        List<Position> moved = new ArrayList<>(cells.size());
        for (Position cell : cells) {
            moved.add(cell.translate(dCol, dRow));
        }
        return moved;
    }

    private int width(List<Position> shape) {
        int minCol = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;
        for (Position cell : shape) {
            minCol = Math.min(minCol, cell.col());
            maxCol = Math.max(maxCol, cell.col());
        }
        return maxCol - minCol + 1;
    }

    private boolean[][] copy(boolean[][] grid) {
        boolean[][] copy = new boolean[grid.length][];
        for (int row = 0; row < grid.length; row++) {
            copy[row] = grid[row].clone();
        }
        return copy;
    }
}