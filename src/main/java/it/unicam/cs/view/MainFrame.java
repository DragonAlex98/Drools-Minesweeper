package it.unicam.cs.view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import it.unicam.cs.controller.GridController;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	public MainFrame(String title, Grid grid, GridController controller) {
		super(title);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(800, 600);
		this.setResizable(true);
		this.setLocationRelativeTo(null);

		MainPanel panel = new MainPanel(grid);
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					Location location = panel.getSquareLocation(e.getPoint());
					if (e.getClickCount() == 1) {
						controller.uncoverSquare(location);						
					} else if (e.getClickCount() == 2) {
						controller.chordSquare(location);
					}
					panel.repaint();
					System.out.println(grid);
				}
				if (SwingUtilities.isRightMouseButton(e)) {
					Location location = panel.getSquareLocation(e.getPoint());
					controller.flagSquare(location);
					panel.repaint();
					System.out.println(grid);
				}
			}
		});
		
		this.getContentPane().add(panel, null);
		this.setVisible(true);
	}
}
