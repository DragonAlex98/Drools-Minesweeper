package it.unicam.cs.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.kie.api.runtime.rule.FactHandle;

import it.unicam.cs.controller.DroolsUtils;
import it.unicam.cs.controller.GridController;
import it.unicam.cs.enumeration.Difficulty;
import it.unicam.cs.enumeration.GameState;
import it.unicam.cs.model.Configuration;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private Grid grid = null;
	private MainPanel panel = null;
	
	public void createNewGameGUI(Grid grid) {
		MainPanel panel = new MainPanel();
		panel.init(grid);
		panel.addMouseListener(new MouseAdapter() {
			private boolean mousePressed = false;
			private Location squareLocation = null;
			
			@Override
			public void mousePressed(MouseEvent e) {
				if (MainFrame.this.grid.getState() != GameState.ONGOING) {
					return;
				}
				if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isRightMouseButton(e)) {
					mousePressed = true;
					squareLocation = panel.getSquareLocation(e.getPoint());
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (!mousePressed) {
					return;
				}
				if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isRightMouseButton(e)) {
					if (panel.getSquareLocation(e.getPoint()).equals(squareLocation)) {
						if (SwingUtilities.isLeftMouseButton(e)) {
							if (e.getClickCount() == 1) {
								DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup( "UNCOVER" ).setFocus();
							} else if (e.getClickCount() == 2) {
								DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup( "CHORD" ).setFocus();
							}
						}
						if (SwingUtilities.isRightMouseButton(e)) {
							DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup( "FLAG" ).setFocus();
						}
						FactHandle fH = DroolsUtils.getInstance().getKSession().insert(squareLocation);
						DroolsUtils.getInstance().getKSession().fireAllRules();
						//DroolsUtils.getInstance().getKSession().delete(fH);
						//DroolsUtils.getInstance().getKSession().fireAllRules();
						panel.repaint();
						System.out.println(MainFrame.this.grid);
					}
				}
				mousePressed = false;
				squareLocation = null;
			}
		});
		this.panel = panel;
	}
	
	private void newGame(Grid grid) {
		this.grid = grid;
		DroolsUtils.getInstance().clear();
		DroolsUtils.getInstance().getKSession().insert(grid);
		DroolsUtils.getInstance().getKSession().fireAllRules();
		this.setSize(grid.getConfig().getN_COLUMNS()*32, grid.getConfig().getN_ROWS()*32);
	}

	public MainFrame(String title, Grid grid, GridController controller) {
		super(title);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		
		JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGameMenuItem = new JMenuItem("New Game");
        newGameMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Configuration configuration = MainFrame.this.grid.getConfig();
				Grid grid = new Grid(configuration);
				newGame(grid);
				MainFrame.this.panel.init(grid);
			}
		});
        gameMenu.add(newGameMenuItem);
        gameMenu.addSeparator();

        for (Difficulty difficulty : Difficulty.values()) {
        	JMenuItem newDifficultyGameMenuItem = new JMenuItem(difficulty.toString().substring(0, 1).toUpperCase() + difficulty.toString().substring(1).toLowerCase());
            newDifficultyGameMenuItem.addActionListener(new ActionListener() {

    			@Override
    			public void actionPerformed(ActionEvent e) {
    				Configuration configuration = difficulty.getConfiguration();
    				Grid grid = new Grid(configuration);
    				newGame(grid);
    				MainFrame.this.panel.init(grid);
    			}
    		});
            gameMenu.add(newDifficultyGameMenuItem);
        }

        menuBar.add(gameMenu);

        JMenu solverMenu = new JMenu("Solver");
        JMenuItem solveMenuItem = new JMenuItem("Solve");
        solveMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO: add solver UI and/or logic
			}
		});
        
        solverMenu.add(solveMenuItem);
        menuBar.add(solverMenu);
        
        this.setJMenuBar(menuBar);

        newGame(grid);
        createNewGameGUI(grid);

		this.getContentPane().add(this.panel, null);
		this.setVisible(true);
	}
}
