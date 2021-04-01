package it.unicam.cs.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
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

import it.unicam.cs.controller.DroolsUtils;
import it.unicam.cs.csp_solver.ChocoCSPSolver;
import it.unicam.cs.csp_solver.MinesweeperSolver;
import it.unicam.cs.enumeration.Difficulty;
import it.unicam.cs.enumeration.GameState;
import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.model.Configuration;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.model.SquareImages;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private Grid grid = null;
	private MainPanel panel = null;
	private ButtonModel selectedDifficultyButtonModel = null;
	private GameState gameState;
	private Timer timer = null;
	private Timer solveTimer = null;
	private int elapsedSeconds = 0;
	private MinesweeperSolver solver = null;
	
	public MainFrame(String title, Grid grid) {
		super(title);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(true);
		this.setLocationRelativeTo(null);
		try {
			this.setIconImage(ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/bomb.png")));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowIconified(WindowEvent e) {
				if (timer != null && MainFrame.this.grid.isPopulated() && MainFrame.this.gameState == GameState.ONGOING) {
					timer.stop();
				}
				super.windowIconified(e);
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				if (timer != null && MainFrame.this.grid.isPopulated() && MainFrame.this.gameState == GameState.ONGOING) {
					timer.start();
				}
				super.windowDeiconified(e);
			}
		});

		Font customFont = null;
		try {
			customFont = Font.createFont(Font.TRUETYPE_FONT, MainFrame.class.getResourceAsStream("/it/unicam/cs/font/digital.ttf")).deriveFont(36f);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(customFont);
		} catch (Exception e) {
			e.printStackTrace();
		}

		JMenuBar menuBar = new JMenuBar();
		
        JMenu gameMenu = new JMenu("Game");
        
        // New game button
        JMenuItem newGameMenuItem = new JMenuItem("New Game");
        newGameMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Configuration configuration = MainFrame.this.grid.getConfig();
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
        	JCheckBoxMenuItem newDifficultyGameMenuItem = new JCheckBoxMenuItem(difficulty.toString().substring(0, 1).toUpperCase() + difficulty.toString().substring(1).toLowerCase());
        	buttonGroup.add(newDifficultyGameMenuItem);
            newDifficultyGameMenuItem.addActionListener(new ActionListener() {

    			@Override
    			public void actionPerformed(ActionEvent e) {
    				selectedDifficultyButtonModel = buttonGroup.getSelection();
    				Configuration configuration = difficulty.getConfiguration();
    				Grid grid = new Grid(configuration);
    				newGame(grid);
    			}
    		});
            newDifficultyGameMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1 + difficulty.ordinal(), KeyEvent.CTRL_MASK));
            if (difficulty == Difficulty.BEGINNER) {
				newDifficultyGameMenuItem.setSelected(true);
				selectedDifficultyButtonModel = buttonGroup.getSelection();
			}
            gameMenu.add(newDifficultyGameMenuItem);
        }
        
        // Custom Configuration button
        JCheckBoxMenuItem customDifficultyGameMenuItem = new JCheckBoxMenuItem("Custom...");
        customDifficultyGameMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JTextField nRowsField = new JTextField();
				JTextField nColumnsField = new JTextField();
				JTextField nBombsField = new JTextField();
				Object[] message = {
				    "N. Rows:", nRowsField,
				    "N. Columns:", nColumnsField,
				    "N. Bombs:", nBombsField
				};

				int option = JOptionPane.showConfirmDialog(null, message, "Custom Grid", JOptionPane.OK_CANCEL_OPTION);
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
				buttonGroup.setSelected(selectedDifficultyButtonModel, true);
			}
		});
        customDifficultyGameMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.CTRL_MASK));
        gameMenu.add(customDifficultyGameMenuItem);
        buttonGroup.add(customDifficultyGameMenuItem);

        menuBar.add(gameMenu);

        JMenu solverMenu = new JMenu("SPSolver");
        
        // Solve complete button
        JMenuItem solveMenuItem = new JMenuItem("SPSolve");
        solveMenuItem.addActionListener(new ActionListener() {
        	
			@Override
			public void actionPerformed(ActionEvent e) {
				JLabel iconLabel = new JLabel(SquareImages.getInstance().getIcons().get("loss"));
				Color backgroundColor = new Color(255, 255, 255, 128);
				JComponent glassPane = new JComponent() {
					private static final long serialVersionUID = 1L;
					
					@Override
					protected void paintComponent(Graphics g) {
						g.setColor(backgroundColor);
						g.fillRect(0, 0, getWidth(), getHeight());
					}

					public void setBackground(Color bg) {
						super.setBackground(bg);
						iconLabel.setBackground(bg);
					};
				};
				glassPane.setOpaque(false);
				glassPane.setBackground(backgroundColor);
				glassPane.setLayout(new BorderLayout());
				glassPane.add(iconLabel, BorderLayout.CENTER);
				MainFrame.this.getRootPane().setGlassPane(glassPane);
				glassPane.setVisible(true);
				glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				glassPane.requestFocusInWindow();
				ActionListener solveSingleStep = new ActionListener() {
					private boolean solved = false;

					@Override
                    public void actionPerformed(ActionEvent ae) {
                        if (solved) {
                        	solveTimer.stop();
                            System.out.println("FINITO!");
                            glassPane.setCursor(null);
            				glassPane.setVisible(false);
                        } else {
                        	solver = new ChocoCSPSolver(grid);
            				solveNTimes(2);
                        	MainFrame.this.panel.paintImmediately(MainFrame.this.panel.getBounds());
                        	solved = true;
                        }
                    }
                };
                solveTimer = new Timer(1000, solveSingleStep);
                solveTimer.start();
			}
		});
        solveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
        
        // SPSolve by step button
        JMenuItem solveByStepMenuItem = new JMenuItem("SPSolve by step");
        solveByStepMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// init spsolver
				solve(true);
			}
		});
        solveByStepMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
        
        // SPSolve N Times button
        JMenuItem solveNTimesMenuItem = new JMenuItem("SPSolve N Times");
        solveNTimesMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// init spsolver
				solveNTimes(5);
			}
		});
        solveNTimesMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
        
        solverMenu.add(solveMenuItem);
        solverMenu.add(solveByStepMenuItem);       
        solverMenu.add(solveNTimesMenuItem);
        menuBar.add(solverMenu);
        
        JMenu cspSolverMenu = new JMenu("CSPSolver");
        
        // CspSolve button
        JMenuItem cspSolveMenuItem = new JMenuItem("CSPSolve");
        cspSolveMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				solver = new ChocoCSPSolver(grid);
				solve(false);
			}
		});
        cspSolveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
        
        // CspSolve by step button
        JMenuItem cspSolveByStepMenuItem = new JMenuItem("CSPSolve by step");
        cspSolveByStepMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				solver = new ChocoCSPSolver(grid);
				solve(true);
			}
		});
        cspSolveByStepMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));
        
        // CspSolve N Times button
        JMenuItem cspSolveNTimesMenuItem = new JMenuItem("CSPSolve N Times");
        cspSolveNTimesMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				String option = JOptionPane.showInputDialog(null, "N Times (0-1000)");
				
				try {
					int times = Integer.parseInt(option);
					if (times > 0 && times < 1000) {
						
						solver = new ChocoCSPSolver(grid);
						solveNTimes(times);
	    				return;
					} else {
						System.out.println("Wrong configuration!");
						JOptionPane.showMessageDialog(panel, "Out of Range!", "Error", JOptionPane.ERROR_MESSAGE);
					}
				} catch (NumberFormatException ex) {
					System.out.println("Wrong format!");
					if(option != null) {
						JOptionPane.showMessageDialog(panel, "Wrong format!", "Error", JOptionPane.ERROR_MESSAGE);						
					}
				}
				
			}
		});
        cspSolveNTimesMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
        
        cspSolverMenu.add(cspSolveMenuItem);
        cspSolverMenu.add(cspSolveByStepMenuItem);
        cspSolverMenu.add(cspSolveNTimesMenuItem);
        menuBar.add(cspSolverMenu);
        
        this.setJMenuBar(menuBar);

        createNewGameGUI();
        newGame(grid);
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
        
        JLabel bombCountLabel = new JLabel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				int remainingBombsCount = MainFrame.this.grid.getConfig().getN_BOMBS();
				if (MainFrame.this.grid.isPopulated()) {
					remainingBombsCount -= MainFrame.this.grid.getGridAsStream().filter(s->s.getState() == SquareState.FLAGGED).count();
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

        Icon smileIcon = new ImageIcon(new ImageIcon(MainFrame.class.getResource("/it/unicam/cs/images/smile.png")).getImage().getScaledInstance(32, 32, Image.SCALE_DEFAULT));
        JButton smileButton = new JButton(smileIcon);
        smileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Configuration configuration = MainFrame.this.grid.getConfig();
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
        
        timer = new Timer(1000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (elapsedSeconds < 999) {
					elapsedSeconds++;
				}
				System.out.println(elapsedSeconds);
				timerLabel.repaint();
			}
		});
		timer.setRepeats(true);
        
        topPanel.add(timerLabel, c);
        
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(topPanel, BorderLayout.PAGE_START);
		this.getContentPane().add(this.panel, BorderLayout.CENTER);
		this.setVisible(true);
		this.pack();
	}
	
	/**
	 * Method to create the MainPanel representing the grid.
	 */
	private void createNewGameGUI() {
		MainPanel panel = new MainPanel();
		panel.addMouseListener(new MouseAdapter() {
			private boolean mousePressed = false;
			private Location squareLocation = null;
			
			@Override
			public void mousePressed(MouseEvent e) {
				if (MainFrame.this.gameState != GameState.ONGOING) {
					return; // exit if WIN or LOSS
				}
				if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isRightMouseButton(e)) {
					mousePressed = true; // if L or R is pressed, save pressed location
					squareLocation = panel.getSquareLocation(e.getPoint());
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (MainFrame.this.gameState != GameState.ONGOING) {
					return; // exit if WIN or LOSS
				}
				if (!mousePressed) {
					return; // exit if mouse is not pressed
				}
				if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isRightMouseButton(e)) { // if L or R click
					if (panel.getSquareLocation(e.getPoint()).equals(squareLocation)) { // if released location == pressed location
						if (!MainFrame.this.grid.isPopulated()) { // populate grid if not
							MainFrame.this.grid.populateSafeGrid(squareLocation);
							timer.start();
						}
						if (SwingUtilities.isLeftMouseButton(e)) {
							if (e.getClickCount() == 1) { // if 1 left click, activate UNCOVER rules
								DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup( "UNCOVER" ).setFocus();
							} else if (e.getClickCount() == 2) { // if 2 left click, activate CHORD rules
								DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup( "CHORD" ).setFocus();
							}
						}
						if (SwingUtilities.isRightMouseButton(e)) { // if right click, activate FLAG rules
							DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup( "FLAG" ).setFocus();
						}
						insertAndFire(squareLocation);
						MainFrame.this.gameState = MainFrame.this.grid.getGameState(); // update state
						MainFrame.this.repaint();
						if (MainFrame.this.gameState != GameState.ONGOING) {
							timer.stop();
							fireWinLossRules();
						}
						System.out.println(MainFrame.this.grid);
						System.out.println(DroolsUtils.getInstance().getKSession().getFactCount());
					}
				}
				mousePressed = false;
				squareLocation = null;
			}
		});
		this.panel = panel;
	}
	
	/**
	 * Thread-safe Method to insert an object into the working memory and fire all related rules.
	 * 
	 * @param object Object to insert into the working memory
	 */
	private synchronized void insertAndFire(Object object) {
		DroolsUtils.getInstance().getKSession().insert(object);
		DroolsUtils.getInstance().getKSession().fireAllRules();
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
		Dimension preferredDimension = new Dimension(grid.getConfig().getN_COLUMNS()*32, grid.getConfig().getN_ROWS()*32);
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
	    if (timer != null) {
	    	timer.stop();
	    }
	    this.elapsedSeconds = 0;
		this.grid = grid;
		this.gameState = GameState.ONGOING;
		this.panel.setPreferredSize(determinePreferredDimension());
		this.pack();
		this.panel.init(grid);
		this.repaint();
	}
	

    /**
     * Method to solve the Minesweeper game using and the current state of the grid.
     * 
     * @param isByStep True if the Solver should perform only one resolution step, False otherwise.
     */
    private void solve(boolean isByStep) {
    	stopTimer();
    	if (isByStep) {
			solver.solveByStep();
    	} else {
			solver.solveComplete();
    	}
		this.gameState = this.grid.getGameState();
		fireWinLossRules();
		this.repaint();
	}
    
    private void solveNTimes(int nTimes) {
    	stopTimer();
		solver.solveCompleteNTimes(nTimes);
		this.repaint();
	}

	private void stopTimer() {
		if (this.gameState == GameState.ONGOING) {
    		this.elapsedSeconds = 999;
    		this.timer.stop();
    	}
	}

	/**
	 * Method to fire the corresponding rules in case of victory or defeat.
	 */
	private void fireWinLossRules() {
		DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup(this.gameState.name()).setFocus();
		DroolsUtils.getInstance().getKSession().fireAllRules();
		this.repaint();
		if (MainFrame.this.gameState == GameState.LOSS) {
			JOptionPane.showMessageDialog(panel, "Bomb Uncovered, You Lose!", "Message", 1, SquareImages.getInstance().getIcons().get("loss"));
		}
		if (MainFrame.this.gameState == GameState.WIN) {
			JOptionPane.showMessageDialog(panel, "Congratulation, You Win!", "Message", 1, SquareImages.getInstance().getIcons().get("win"));
		}
	}
}
