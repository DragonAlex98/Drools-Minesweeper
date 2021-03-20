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

import javax.swing.JPanel;

import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.model.SquareImages;
import lombok.Getter;

public class MainPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Grid grid;
	@Getter
	private float squareWidth;
	@Getter
	private float squareHeight;

	public MainPanel(Grid grid) {
		this.grid = grid;
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				squareWidth = (float)getWidth() / grid.getConfig().getN_COLUMNS();
				squareHeight = (float)getHeight() / grid.getConfig().getN_ROWS();
			}
		});		
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
			BufferedImage im = SquareImages.getInstance().getSquareImage(s);
			Image coveredImageResized = im.getScaledInstance((int)squareWidth, (int)squareHeight, Image.SCALE_AREA_AVERAGING);
			g.drawImage(coveredImageResized, (int)(squareWidth*s.getLocation().getColumn()), (int)(squareHeight*s.getLocation().getRow()), null);
			return;
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
