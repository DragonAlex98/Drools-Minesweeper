package it.unicam.cs.view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.kie.api.runtime.rule.FactHandle;

import it.unicam.cs.controller.DroolsUtils;
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
			private boolean mousePressed = false;
			private Location squareLocation = null;
			
			@Override
			public void mousePressed(MouseEvent e) {
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
						DroolsUtils.getInstance().getKSession().delete(fH);
						DroolsUtils.getInstance().getKSession().fireAllRules();
						panel.repaint();
						System.out.println(grid);
					}
				}
				mousePressed = false;
				squareLocation = null;
			}
		});
		

		/*@Override
		public void mouseReleased(MouseEvent e) {
			if (!mousePressed) {
				return;
			}
			if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isRightMouseButton(e)) {
				if (panel.getSquareLocation(e.getPoint()).equals(squareLocation)) {
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
			}
			mousePressed = false;
			squareLocation = null;
		}*/
		this.getContentPane().add(panel, null);
		this.setVisible(true);
	}
}
