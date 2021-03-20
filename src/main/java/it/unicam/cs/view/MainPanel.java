package it.unicam.cs.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import lombok.Getter;

public class MainPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Grid grid;
	@Getter
	private float squareWidth;
	@Getter
	private float squareHeight;
	private BufferedImage coveredImage;
	private BufferedImage emptyImage;

	public MainPanel(Grid grid) {
		this.grid = grid;
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				squareWidth = (float)getWidth() / grid.getConfig().getN_COLUMNS();
				squareHeight = (float)getHeight() / grid.getConfig().getN_ROWS();
			}
		});
		try {
			coveredImage = ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/covered.png"));
			emptyImage = ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/empty.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Location getSquareLocation(Point point) {
		return new Location((int) (point.y / squareHeight), (int) (point.x / squareWidth));
	}
	
	@Override
	public void paintComponent(Graphics g) {
		if (squareWidth == 0.0f || squareHeight == 0.0f) {
			return;
		}
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());

		grid.getGridAsStream().forEach(s -> {
			if (s.getState() == SquareState.COVERED) {
				Image coveredImageResized = coveredImage.getScaledInstance((int)squareWidth, (int)squareHeight, Image.SCALE_AREA_AVERAGING);
				g.drawImage(coveredImageResized, (int)(squareWidth*s.getLocation().getColumn()), (int)(squareHeight*s.getLocation().getRow()), null);
				return;
			} else if (s.getState() == SquareState.FLAGGED) {
				g.setColor(Color.ORANGE);
			} else if (s.getState() == SquareState.EXPLODED) {
				g.setColor(Color.RED);
			} else {
				if (s.getType() == SquareType.BOMB) {
					g.setColor(Color.BLACK);
				} else if (s.getType() == SquareType.EMPTY) {
					Image emptyImageResized = emptyImage.getScaledInstance((int)squareWidth, (int)squareHeight, Image.SCALE_AREA_AVERAGING);
					g.drawImage(emptyImageResized, (int)(squareWidth*s.getLocation().getColumn()), (int)(squareHeight*s.getLocation().getRow()), null);
					return;
				} else if (s.getType() == SquareType.NUMBER) {
					g.setColor(Color.BLUE);
				}
			}
			g.fillRect((int)(s.getLocation().getColumn()*squareWidth), (int)(s.getLocation().getRow()*squareHeight), (int)squareWidth, (int)squareHeight);
		});
		
		g.setColor(Color.DARK_GRAY);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(2));
		for (int r = 1; r < grid.getConfig().getN_ROWS(); r++) {
			g2d.drawLine(0, (int)(squareHeight*r), getWidth(), (int)(squareHeight*r));
		}
		for (int c = 1; c < grid.getConfig().getN_COLUMNS(); c++) {
			g2d.drawLine((int)(squareWidth*c), 0, (int)(squareWidth*c), getHeight());
		}
	}
}
