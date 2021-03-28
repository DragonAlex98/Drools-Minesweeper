package it.unicam.cs.view;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import it.unicam.cs.controller.DroolsUtils;
import it.unicam.cs.enumeration.Difficulty;
import it.unicam.cs.enumeration.GameState;
import it.unicam.cs.model.Configuration;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.solver.Solver;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private Grid grid = null;
	private MainPanel panel = null;
	private ButtonModel selectedDifficultyButtonModel = null;
	private GameState gameState;
	
	public MainFrame(String title, Grid grid) {
		super(title);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(true);
		this.setLocationRelativeTo(null);

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
        gameMenu.add(customDifficultyGameMenuItem);
        buttonGroup.add(customDifficultyGameMenuItem);

        menuBar.add(gameMenu);

        JMenu solverMenu = new JMenu("Solver");
        // Solve complete button
        JMenuItem solveMenuItem = new JMenuItem("Solve");
        solveMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				solve(false);
			}
		});
        // Solve by step button
        JMenuItem solveByStepMenuItem = new JMenuItem("Solve by step");
        solveByStepMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				solve(true);
			}
		});        
        solverMenu.add(solveMenuItem);
        solverMenu.add(solveByStepMenuItem);
        
        menuBar.add(solverMenu);
        this.setJMenuBar(menuBar);

        createNewGameGUI();
        newGame(grid);

		this.getContentPane().add(this.panel, null);
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
						fireWinLossRules();
						panel.repaint();
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
		this.grid = grid;
		this.gameState = GameState.ONGOING;
		this.panel.setPreferredSize(determinePreferredDimension());
		this.pack();
		this.panel.init(grid);
	}
	

    /**
     * Method to solve the Minesweeper game using a Single Point Solver and the current state of the grid.
     * 
     * @param isByStep True if the Solver should perform only one resolution step, False otherwise.
     */
    private void solve(boolean isByStep) {
		Solver solver = new Solver(MainFrame.this.grid);
		solver.solve(isByStep);
		MainFrame.this.gameState = MainFrame.this.grid.getGameState();
		fireWinLossRules();
		panel.repaint();
	}

	/**
	 * Method to fire the corresponding rules in case of victory or defeat.
	 */
	private void fireWinLossRules() {
		if (MainFrame.this.gameState == GameState.LOSS) {
			DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup( "LOSS" ).setFocus();
			DroolsUtils.getInstance().getKSession().fireAllRules();
		}
		if (MainFrame.this.gameState == GameState.WIN) {
			DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup( "WIN" ).setFocus();
			DroolsUtils.getInstance().getKSession().fireAllRules();
		}
	}
}
