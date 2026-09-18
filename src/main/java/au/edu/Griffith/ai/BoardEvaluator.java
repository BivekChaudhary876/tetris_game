package au.edu.Griffith.ai;

/**
 * Gives a board a score so the AI can compare placements. Higher is better.
 *
 * <p>The four heuristics from the unit's AI tutorial (height, lines cleared,
 * holes, bumpiness), plus a max-height guard. Works on a plain
 * {@code boolean[row][col]} grid because the AI scores imaginary boards that
 * were never locked into the real field — and it keeps this class free of any
 * model or JavaFX dependency, so it is easy to unit test.</p>
 *
 * <p>Two deliberate changes from the tutorial, which asks us to optimise rather
 * than copy its version:</p>
 * <ul>
 *   <li>Height is the <em>sum</em> of all columns, not the tallest one. Using
 *   only the tallest gives the AI no reason to keep the other columns low.</li>
 *   <li>The weights are the published tuned set. What matters is the ratio:
 *   holes must outweigh height, or the AI buries cells to keep the stack flat.</li>
 * </ul>
 */
public class BoardEvaluator {

    /** Sum of all column heights. Lower stack is better. */
    public static final double WEIGHT_AGGREGATE_HEIGHT = -0.510066;

    /** Rows this placement removes. Clearing lines is the goal. */
    public static final double WEIGHT_LINES_CLEARED = 0.760666;

    /** Covered empty cells. Worst outcome: everything above must clear first. */
    public static final double WEIGHT_HOLES = -0.35663;

    /** Height gaps between neighbouring columns. A flat surface fits more pieces. */
    public static final double WEIGHT_BUMPINESS = -0.184483;

    /** Tallest column — this is what actually triggers game over. */
    public static final double WEIGHT_MAX_HEIGHT = -0.15;

    /**
     * @param grid         board as it would look after the piece locked and full
     *                     rows were removed
     * @param linesCleared rows that placement removed
     */
    public double evaluate(boolean[][] grid, int linesCleared) {
        return WEIGHT_AGGREGATE_HEIGHT * aggregateHeight(grid)
                + WEIGHT_LINES_CLEARED * linesCleared
                + WEIGHT_HOLES * holes(grid)
                + WEIGHT_BUMPINESS * bumpiness(grid)
                + WEIGHT_MAX_HEIGHT * maximumHeight(grid);
    }

    /** Floor to the topmost filled cell in this column, or 0 if the column is empty. */
    public int columnHeight(boolean[][] grid, int col) {
        for (int row = 0; row < grid.length; row++) {
            if (grid[row][col]) {
                return grid.length - row;
            }
        }
        return 0;
    }

    public int aggregateHeight(boolean[][] grid) {
        int total = 0;
        for (int col = 0; col < grid[0].length; col++) {
            total += columnHeight(grid, col);
        }
        return total;
    }

    public int maximumHeight(boolean[][] grid) {
        int tallest = 0;
        for (int col = 0; col < grid[0].length; col++) {
            tallest = Math.max(tallest, columnHeight(grid, col));
        }
        return tallest;
    }

    /** Empty cells with a filled cell somewhere above them in the same column. */
    public int holes(boolean[][] grid) {
        int count = 0;
        for (int col = 0; col < grid[0].length; col++) {
            boolean blockAbove = false;
            for (int row = 0; row < grid.length; row++) {
                if (grid[row][col]) {
                    blockAbove = true;
                } else if (blockAbove) {
                    count++;
                }
            }
        }
        return count;
    }

    /** Total height difference between each neighbouring pair of columns. */
    public int bumpiness(boolean[][] grid) {
        int total = 0;
        for (int col = 0; col < grid[0].length - 1; col++) {
            total += Math.abs(columnHeight(grid, col) - columnHeight(grid, col + 1));
        }
        return total;
    }
}