package it.unicam.cs.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;

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

	private Map<String, Image> images = new HashMap<String, Image>();

	public MainPanel() {}

	private void updateImages() {
		if (getWidth() == 0 || getHeight() == 0) {
			return;
		}
		float internalSquareWidth = (float)getWidth() / grid.getConfig().getN_COLUMNS();
		float internalSquareHeight = (float)getHeight() / grid.getConfig().getN_ROWS();
		images.clear();
		SquareImages.getInstance().getImages().forEach((k, v) -> {
			Image coveredImageResized = v.getScaledInstance((int)internalSquareWidth, (int)internalSquareHeight, Image.SCALE_AREA_AVERAGING);					
			MainPanel.this.images.put(k, coveredImageResized);
		});
		squareWidth = internalSquareWidth;
		squareHeight = internalSquareHeight;
	}

	public void init(Grid grid) {
		this.grid = grid;
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				updateImages();
			}
		});
		updateImages();
		revalidate();
		repaint();
	}

	public Location getSquareLocation(Point point) {
		if (grid == null) {
			return new Location(-1, -1);
		}
		return new Location((int) (point.y / squareHeight), (int) (point.x / squareWidth));
	}
	
	@Override
	public void paintComponent(Graphics g) {
		if (grid == null || !grid.isPopulated()) {
			return;
		}
		if (squareWidth == 0.0f || squareHeight == 0.0f) {
			return;
		}
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());

		grid.getGridAsStream().forEach(s -> {
			String string = SquareImages.getInstance().getSquareImage(s);
			g.drawImage(images.get(string), (int)(squareWidth*s.getLocation().getColumn()), (int)(squareHeight*s.getLocation().getRow()), null);
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
