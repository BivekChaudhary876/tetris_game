/**
 * The Milestone 1 implementation, kept only while its logic is migrated into the
 * MVC layers.
 *
 * <p><strong>Temporary.</strong> Nothing new should be written here and nothing
 * outside this package should import it. It is retained for one reason: the
 * collision, rotation, wall-kick and line-clear code in {@code Tetris} is
 * working and tested by hand, and is the reference the corresponding
 * {@code model} methods are being written from. As each area is ported, delete
 * the class it came from.</p>
 *
 * <p>Migration map:</p>
 * <ul>
 *   <li>{@code Tetris} grid, collision and scoring &rarr; {@link au.edu.Griffith.model.Board}
 *       and {@link au.edu.Griffith.model.Score}</li>
 *   <li>{@code Tetris} timer, key handler and pause flags &rarr;
 *       {@link au.edu.Griffith.controller.GameController} and
 *       {@link au.edu.Griffith.model.state}</li>
 *   <li>{@code Tetris} scene graph and rendering &rarr; {@link au.edu.Griffith.view.GameScreen}
 *       and {@link au.edu.Griffith.view.BoardRenderer}</li>
 *   <li>{@code Main} splash and menu &rarr; {@link au.edu.Griffith.view.SplashScreen},
 *       {@link au.edu.Griffith.view.MainMenuScreen} and {@link au.edu.Griffith.TetrisApp}</li>
 *   <li>{@code Configuration} / {@code HighScores} &rarr; the matching screens and
 *       controllers, backed by {@link au.edu.Griffith.service}</li>
 *   <li>{@code AbstractTetromino} and the seven pieces &rarr;
 *       {@link au.edu.Griffith.model.tetromino} (already ported; the shape data
 *       now lives on {@code TetrominoType})</li>
 * </ul>
 *
 * <p>This package must be removed before the final submission — a marker looking
 * at the Implementation View should see the MVC structure, not two copies of the
 * game.</p>
 */
package au.edu.Griffith.legacy;
