package it.unicam.cs.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import it.unicam.cs.enumeration.Difficulty;
import it.unicam.cs.enumeration.GameState;
import it.unicam.cs.enumeration.SolveStrategy;
import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.model.Configuration;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.solver.SolverManager;
import it.unicam.cs.utils.DroolsUtils;
import it.unicam.cs.utils.ImageUtils;

/**
 * Main Class used to manage the User Interface and its components.
 *
 */
public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	/** Grid used for the Minesweeper game **/
	private Grid grid = new Grid(Difficulty.BEGINNER.getConfiguration());
	/** JPanel representing the grid **/
	private MainPanel panel = null;
	/** Number of elapsed seconds **/
	private int elapsedSeconds = 0;
	/** Timer used to count the elapsed seconds **/
	private Timer elapsedSecondsTimer = null;
	/** Timer used during the solving phase **/
	private Timer solveTimer = null;
	/** JComponent used for the waiting phase **/
	private MainGlassPane glassPane = new MainGlassPane();
	/** Font used to render text **/
	private Font customFont = null;
	/** Whether the left mouse button is pressed **/
	private boolean leftMouseButtonPressed = false;

	public static void main(String[] args) {
		// to make Drools work with Java >= 8
		System.setProperty("java.version", "1.8");
		new MainFrame("Minesweeper");
	}

	/**
	 * Method to load a custom Font, used to render text in a visible way.
	 * 
	 * @return The loaded Font (or Monospaced font if the load fail).
	 */
	private Font loadDigitalFont() {
		try {
			Font customFont = Font.createFont(Font.TRUETYPE_FONT,
					MainFrame.class.getResourceAsStream("/it/unicam/cs/font/digital.ttf")).deriveFont(36f);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(customFont);
			return customFont;
		} catch (Exception e) {
			return new Font(Font.MONOSPACED, Font.BOLD, 24);
		}
	}

	/**
	 * Method to create and return the Game menu as a JMenu (including new Game
	 * button and the choice of difficulty).
	 *
	 */
	private JMenu createGameMenu() {
		JMenu gameMenu = new JMenu("Game");
		// New game button
		JMenuItem newGameMenuItem = new JMenuItem("New Game");
		newGameMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Configuration configuration = grid.getConfig();
				Grid grid = new Grid(configuration);
				newGame(grid);
			}
		});
		newGameMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		gameMenu.add(newGameMenuItem);
		gameMenu.addSeparator();

		// Beginner/Intermediate/Expert button group
		ButtonGroup buttonGroup = new ButtonGroup();
		for (Difficulty difficulty : Difficulty.values()) {
			JCheckBoxMenuItem newDifficultyGameMenuItem = new JCheckBoxMenuItem(
					difficulty.toString().substring(0, 1).toUpperCase()
							+ difficulty.toString().substring(1).toLowerCase());
			buttonGroup.add(newDifficultyGameMenuItem);
			newDifficultyGameMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Configuration configuration = difficulty.getConfiguration();
					Grid grid = new Grid(configuration);
					newGame(grid);
				}
			});
			newDifficultyGameMenuItem
					.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1 + difficulty.ordinal(), KeyEvent.CTRL_MASK));
			if (difficulty == Difficulty.BEGINNER) {
				newDifficultyGameMenuItem.setSelected(true);
			}
			gameMenu.add(newDifficultyGameMenuItem);
		}

		// Custom Configuration button
		JCheckBoxMenuItem customDifficultyGameMenuItem = new JCheckBoxMenuItem("Custom...");
		customDifficultyGameMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JTextField nRowsField = new JTextField("5");
				JTextField nColumnsField = new JTextField("5");
				JTextField nBombsField = new JTextField("5");
				Object[] message = { "N. Rows:", nRowsField, "N. Columns:", nColumnsField, "N. Bombs:", nBombsField };

				int option = JOptionPane.showConfirmDialog(panel, message, "Custom Grid", JOptionPane.OK_CANCEL_OPTION);
				if (option == JOptionPane.OK_OPTION) {
					try {
						int nRows = Integer.parseInt(nRowsField.getText());
						int nColumns = Integer.parseInt(nColumnsField.getText());
						int nBombs = Integer.parseInt(nBombsField.getText());
						Configuration configuration = new Configuration(nRows, nColumns, nBombs);
						if (checkConfigurationValidity(configuration)) {
							Grid grid = new Grid(configuration);
							newGame(grid);
							return;
						} else {
							System.out.println("Wrong configuration!");
						}
					} catch (NumberFormatException ex) {
						System.out.println("Wrong format!");
					}
				} else {
					System.out.println("New configuration aborted!");
				}
			}
		});
		customDifficultyGameMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.CTRL_MASK));
		gameMenu.add(customDifficultyGameMenuItem);
		buttonGroup.add(customDifficultyGameMenuItem);

		return gameMenu;
	}

	/**
	 * Method to create and return the Solver menu as a JMenu depending on the
	 * SolverStrategy (including Solve by step button, Solve complete button and
	 * Solve N Times button).
	 * 
	 * @param strategy The Solver strategy used to create the menu.
	 *
	 */
	private JMenu createSolverMenu(SolveStrategy strategy) {
		JMenu solverMenu = new JMenu(strategy.getName());
		// Solve by step button
		JMenuItem solveByStepMenuItem = new JMenuItem(strategy.getName() + " by step");
		solveByStepMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (grid.getGameState() != GameState.ONGOING) {
					return; // exit if WIN or LOSS
				}
				SolverManager solverManager = new SolverManager(strategy, grid);
				stopTimer();
				solverManager.solveByStep();
				MainFrame.this.repaint();
				fireWinLossRules();
			}
		});
		solveByStepMenuItem.setAccelerator(KeyStroke.getKeyStroke(strategy.getSingleStepKey(), KeyEvent.CTRL_MASK));

		// Solve complete button
		JMenuItem solveMenuItem = new JMenuItem(strategy.getName());
		solveMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (grid.getGameState() != GameState.ONGOING) {
					return; // exit if WIN or LOSS
				}

				SolverManager solverManager = new SolverManager(strategy, grid);

				ActionListener cancelButtonAction = new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						solveTimer.stop();
						stopTimer();
						glassPane.deactivate();
						MainFrame.this.repaint();
						glassPane.getStopButton().removeActionListener(this);
					}
				};

				ActionListener solveSingleStep = new ActionListener() {
					private boolean solved = false;

					@Override
					public void actionPerformed(ActionEvent ae) {
						if (solved) {
							solveTimer.stop();
							stopTimer();
							glassPane.deactivate();
							fireWinLossRules();
							glassPane.getStopButton().removeActionListener(cancelButtonAction);
						} else {
							if (grid.getGameState() != GameState.ONGOING) {
								solved = true;
								panel.paintImmediately(panel.getVisibleRect());
								return;
							}
							solverManager.solveByStep();
							glassPane.getProgressBar()
									.setValue((int) (grid.getGridAsStream()
											.filter(s -> s.getState() == SquareState.UNCOVERED).count() * 100
											/ (grid.getConfig().getN_ROWS() * grid.getConfig().getN_COLUMNS()
													- grid.getConfig().getN_BOMBS())));
							panel.paintImmediately(panel.getVisibleRect());
						}
					}
				};

				glassPane.getProgressBar().setValue(0);
				glassPane.activate();
				glassPane.getStopButton().addActionListener(cancelButtonAction);
				solveTimer = new Timer(500, solveSingleStep);
				solveTimer.setRepeats(true);
				solveTimer.start();
			}
		});
		solveMenuItem.setAccelerator(KeyStroke.getKeyStroke(strategy.getSolveKey(), KeyEvent.CTRL_MASK));

		// Solve N Times button
		JMenuItem solveNTimesMenuItem = new JMenuItem(strategy.getName() + " N Times");
		solveNTimesMenuItem.addActionListener(new ActionListener() {
			private long startTime;

			@Override
			public void actionPerformed(ActionEvent e) {
				String option = JOptionPane.showInputDialog(panel, "N Times (1-1000)", 100);

				try {
					int times = Integer.parseInt(option);
					if (times >= 1 && times <= 1000) {
						ActionListener cancelButtonAction = new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								solveTimer.stop();
								stopTimer();
								glassPane.deactivate();
								MainFrame.this.repaint();
								glassPane.getStopButton().removeActionListener(this);
							}
						};

						SolverManager solverManager = new SolverManager(strategy, grid, true);
						ActionListener solveSingleGrid = new ActionListener() {
							private int n = 0;
							private boolean solved = false;

							@Override
							public void actionPerformed(ActionEvent ae) {
								if (n == times) {
									long endTime = System.currentTimeMillis();
									solveTimer.stop();
									stopTimer();
									glassPane.deactivate();
									MainFrame.this.repaint();
									glassPane.getStopButton().removeActionListener(cancelButtonAction);
									solverManager.getSolverStatistics().setElapsedTime((endTime - startTime) / 1000f);
									solverManager.getSolverStatistics().consolidate();
									JOptionPane.showMessageDialog(panel, solverManager.getSolverStatistics(),
											"Solver Statistics", JOptionPane.INFORMATION_MESSAGE,
											ImageUtils.getInstance().getIcons().get("stat"));
								} else {
									if (solved) {
										n++;
										glassPane.getProgressBar().setValue(n * 100 / times);
										solved = false;
										panel.paintImmediately(panel.getVisibleRect());
										return;
									}
									Grid newGrid = new Grid(grid.getConfig());
									newGame(newGrid);
									solverManager.updateSolver(newGrid);
									solverManager.complete();
									panel.paintImmediately(panel.getVisibleRect());
									solved = true;
								}
							}
						};

						glassPane.getProgressBar().setValue(0);
						glassPane.activate();
						glassPane.getStopButton().addActionListener(cancelButtonAction);
						solveTimer = new Timer(10, solveSingleGrid);
						solveTimer.setRepeats(true);
						solveTimer.start();
						startTime = System.currentTimeMillis();
					} else {
						System.out.println("Wrong configuration!");
						JOptionPane.showMessageDialog(panel, "Out of Range!", "Error", JOptionPane.ERROR_MESSAGE);
					}
				} catch (NumberFormatException ex) {
					System.out.println("Wrong format!");
					if (option != null) {
						JOptionPane.showMessageDialog(panel, "Wrong format!", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		solveNTimesMenuItem.setAccelerator(KeyStroke.getKeyStroke(strategy.getSolveNTimesKey(), KeyEvent.CTRL_MASK));

		solverMenu.add(solveByStepMenuItem);
		solverMenu.add(solveMenuItem);
		solverMenu.add(solveNTimesMenuItem);

		return solverMenu;
	}

	/**
	 * Method to create and return the top panel as a Jpanel (including the bomb
	 * count, the Smile button and the timer)
	 */
	private JPanel createTopPanel() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		// Bomb count
		JLabel bombCountLabel = new JLabel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				int remainingBombsCount = grid.getConfig().getN_BOMBS();
				if (grid.isPopulated()) {
					remainingBombsCount -= grid.getGridAsStream().filter(s -> s.getState() == SquareState.FLAGGED)
							.count();
				}
				setText(String.format("%03d", remainingBombsCount));
				super.paintComponent(g);
			}
		};
		bombCountLabel.setFont(customFont);
		bombCountLabel.setForeground(Color.RED);
		bombCountLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		topPanel.add(bombCountLabel, c);

		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.5;
		c.weighty = 1;
		c.gridx = 1;
		c.gridy = 0;

		// Smile Button
		Icon smileIcon = ImageUtils.getInstance().getIcons().get("smile");
		JButton smileButton = new JButton(smileIcon);
		smileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Configuration configuration = grid.getConfig();
				Grid grid = new Grid(configuration);
				newGame(grid);
			}
		});
		smileButton.setMinimumSize(new Dimension(smileIcon.getIconWidth(), smileIcon.getIconHeight()));
		smileButton.setPreferredSize(new Dimension(smileIcon.getIconWidth(), smileIcon.getIconHeight()));
		topPanel.add(smileButton, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.weighty = 0;
		c.gridx = 2;
		c.gridy = 0;

		// Timer
		JLabel timerLabel = new JLabel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				setText(String.format("%03d", elapsedSeconds));
				super.paintComponent(g);
			}
		};
		timerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		timerLabel.setFont(customFont);
		timerLabel.setForeground(Color.RED);
		timerLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

		elapsedSecondsTimer = new Timer(1000, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (elapsedSeconds < 999) {
					elapsedSeconds++;
				}
				timerLabel.repaint();
			}
		});
		elapsedSecondsTimer.setRepeats(true);

		topPanel.add(timerLabel, c);
		return topPanel;
	}

	/**
	 * Method to create the MainPanel representing the grid.
	 */
	private MainPanel createGUIPanel() {
		MainPanel panel = new MainPanel();

		panel.addMouseMotionListener(new MouseMotionAdapter() {
			// used to always press the current location when the left mouse button is pressed
			@Override
			public void mouseDragged(MouseEvent e) {
				if (grid.getGameState() != GameState.ONGOING) {
					return; // exit if WIN or LOSS
				}
				if (!leftMouseButtonPressed) {
					return;
				}
				if (SwingUtilities.isLeftMouseButton(e)) {
					Location clickedLocation = panel.getSquareLocation(e.getPoint());
					if (!grid.isLocationInsideGrid(clickedLocation)) {
						return;
					}
					if (panel.getPressedLocation() != null && !panel.getPressedLocation().equals(clickedLocation)) {
						panel.setPressedLocation(panel.getSquareLocation(e.getPoint()));
						panel.repaint();
					}
				}
			}
		});

		panel.addMouseListener(new MouseAdapter() {
			// when entering the panel, draw the pressed location, if the left mouse button is pressed
			@Override
			public void mouseEntered(MouseEvent e) {
				if (grid.getGameState() != GameState.ONGOING) {
					return; // exit if WIN or LOSS
				}
				if (!leftMouseButtonPressed) {
					return;
				}
				panel.setPressedLocation(panel.getSquareLocation(e.getPoint()));
				panel.repaint();
			}

			// when exiting the panel, stop drawing the pressed location
			@Override
			public void mouseExited(MouseEvent e) {
				if (grid.getGameState() != GameState.ONGOING) {
					return; // exit if WIN or LOSS
				}
				if (!leftMouseButtonPressed) {
					return;
				}
				panel.setPressedLocation(null);
				panel.repaint();
			}

			// if right mouse button is pressed, flag the location the mouse is on and "consume"
			// eventual left mouse button click. If left mouse button is pressed, set the corresponding
			// boolean variable to true
			@Override
			public void mousePressed(MouseEvent e) {
				if (grid.getGameState() != GameState.ONGOING) {
					return; // exit if WIN or LOSS
				}
				if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isRightMouseButton(e)) {
					if (e.getY() < 0) {
	                    return;
	                }
					Location clickedLocation = panel.getSquareLocation(e.getPoint());
					if (!grid.isLocationInsideGrid(clickedLocation)) {
						return;
					}
					if (SwingUtilities.isRightMouseButton(e)) {
						if (!grid.isPopulated()) { // populate grid if not
							grid.populateSafeGrid(clickedLocation);
							elapsedSecondsTimer.start();
						}
						DroolsUtils.getInstance().insertAndFire("FLAG", clickedLocation);
						leftMouseButtonPressed = false;
						panel.setPressedLocation(null);
						MainFrame.this.repaint();
						return;
					}
					if (SwingUtilities.isLeftMouseButton(e)) {
						leftMouseButtonPressed = true; // if left is pressed, save pressed location
						panel.setPressedLocation(clickedLocation);
						panel.repaint();
					}
				}
			}

			// we only care about the release of the left mouse button (1 or 2 clicks),
			// since the right mouse button is handled in the mousePressed method
			@Override
			public void mouseReleased(MouseEvent e) {
				if (grid.getGameState() != GameState.ONGOING) {
					return; // exit if WIN or LOSS
				}
				if (!leftMouseButtonPressed) {
					return; // exit if mouse is not pressed
				}
				panel.setPressedLocation(null);
				panel.repaint();
				if (SwingUtilities.isLeftMouseButton(e)) { // if left click
					leftMouseButtonPressed = false;
					if (e.getY() < 0) {
	                    return;
	                }
					Location clickedLocation = panel.getSquareLocation(e.getPoint());
					if (!grid.isLocationInsideGrid(clickedLocation)) {
						return;
					}
					if (!grid.isPopulated()) { // populate grid if not
						grid.populateSafeGrid(clickedLocation);
						elapsedSecondsTimer.start();
					}
					if (e.getClickCount() <= 1) {
						// if 0 or 1 left click, activate UNCOVER rules. It must accept also 0 for it to work on Mac/Linux,
						// since releasing a mouse in a different location than where it was pressed doesn't count as a click
						DroolsUtils.getInstance().insertAndFire("UNCOVER", clickedLocation);
					} else if (e.getClickCount() == 2) { // if 2 left click, activate CHORD rules
						DroolsUtils.getInstance().insertAndFire("CHORD", clickedLocation);
					}
					MainFrame.this.repaint();
					if (grid.getGameState() != GameState.ONGOING) {
						elapsedSecondsTimer.stop();
						fireWinLossRules();
					}
				}
			}
		});
		return panel;
	}

	public MainFrame(String title) {
		super(title);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(true);
		this.setGlassPane(glassPane);
		try {
			this.setIconImage(ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/bomb.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.addWindowListener(new WindowAdapter() {

			// stop game timer, if running, when minimizing window
			@Override
			public void windowIconified(WindowEvent e) {
				if (elapsedSecondsTimer != null && grid.isPopulated() && grid.getGameState() == GameState.ONGOING) {
					elapsedSecondsTimer.stop();
				}
				super.windowIconified(e);
			}

			// restart game timer, if running, when restoring window
			@Override
			public void windowDeiconified(WindowEvent e) {
				if (elapsedSecondsTimer != null && grid.isPopulated() && grid.getGameState() == GameState.ONGOING) {
					elapsedSecondsTimer.start();
				}
				super.windowDeiconified(e);
			}
			
			// when the window is deactivated (no longer the active window),
			// stop drawing pressed location
			@Override
			public void windowDeactivated(WindowEvent e) {
				leftMouseButtonPressed = false;
				panel.setPressedLocation(null);
				panel.repaint();
			}
		});

		this.customFont = loadDigitalFont();

		this.glassPane.getStopButton().setFont(this.customFont);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(this.createGameMenu());
		for (SolveStrategy strategy : SolveStrategy.values()) {
			menuBar.add(this.createSolverMenu(strategy));
		}
		this.setJMenuBar(menuBar);

		this.panel = createGUIPanel();

		JPanel topPanel = createTopPanel();

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(topPanel, BorderLayout.PAGE_START);
		this.getContentPane().add(this.panel, BorderLayout.CENTER);

		newGame(grid);

		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	/**
	 * Method to check if a certain Configuration is valid or not.
	 * 
	 * @param configuration The configuration to check.
	 * @return True if the configuration is valid, False otherwise.
	 */
	private boolean checkConfigurationValidity(Configuration configuration) {
		if (configuration.getN_ROWS() <= 0 || configuration.getN_COLUMNS() <= 0 || configuration.getN_BOMBS() <= 0) {
			return false;
		}
		if (configuration.getN_BOMBS() >= configuration.getN_ROWS() * configuration.getN_COLUMNS()) {
			return false;
		}
		return true;
	}

	/**
	 * Method to obtain the preferred dimension for the MainPanel.
	 * 
	 * @return The preferred Dimension.
	 */
	private Dimension determinePreferredDimension() {
		Dimension preferredDimension = new Dimension(grid.getConfig().getN_COLUMNS() * 32,
				grid.getConfig().getN_ROWS() * 32);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (preferredDimension.width > screenSize.width || preferredDimension.height > screenSize.height) {
			preferredDimension = new Dimension(screenSize.width, screenSize.height);
		}
		return preferredDimension;
	}

	/**
	 * Method used to create/reset a game.
	 * 
	 * @param grid The grid used for the game.
	 */
	private void newGame(Grid grid) {
		DroolsUtils.getInstance().clear();
		if (elapsedSecondsTimer != null) {
			elapsedSecondsTimer.stop();
		}
		this.elapsedSeconds = 0;
		this.grid = grid;
		this.panel.setPreferredSize(determinePreferredDimension());
		this.pack();
		this.setLocationRelativeTo(this);
		this.panel.init(grid);
		this.repaint();
	}

	/**
	 * Method to stop the timer
	 */
	private void stopTimer() {
		this.elapsedSeconds = 999;
		this.elapsedSecondsTimer.stop();
	}

	/**
	 * Method to fire the corresponding rules in case of victory or defeat.
	 */
	private void fireWinLossRules() {
		GameState gameState = grid.getGameState();
		DroolsUtils.getInstance().fireGroup(gameState.name());
		this.repaint();
		if (gameState == GameState.LOSS) {
			JOptionPane.showMessageDialog(panel, "Bomb Uncovered, You Lose!", "Message", 1,
					ImageUtils.getInstance().getIcons().get("loss"));
		} else if (gameState == GameState.WIN) {
			JOptionPane.showMessageDialog(panel, "Congratulation, You Win!", "Message", 1,
					ImageUtils.getInstance().getIcons().get("win"));
		}
	}
}
