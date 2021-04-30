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
import it.unicam.cs.utils.ImageUtils;
import lombok.NoArgsConstructor;

/**
 * UI Class used to manage the graphic representation of the Grid as JPanel.
 *
 */
@NoArgsConstructor
public class MainPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	/** The actual grid **/
	private Grid grid;
	/** The width of a Square (it depends on the size of this JPanel) **/
	private float squareWidth;
	/** The height of a Square (it depends on the size of this JPanel) **/
	private float squareHeight;
	/** Map containing the images of the Square with the right size **/
	private Map<String, Image> images = new HashMap<String, Image>();

	/**
	 * Method used to update the size of the images, depending on the size of this JPanel.
	 */
	private void updateImages() {
		if (getWidth() / grid.getConfig().getN_COLUMNS() == 0 || getHeight() / grid.getConfig().getN_ROWS() == 0) {
			return;
		}
		float internalSquareWidth = (float)getWidth() / grid.getConfig().getN_COLUMNS();
		float internalSquareHeight = (float)getHeight() / grid.getConfig().getN_ROWS();
		images.clear();
		ImageUtils.getInstance().getImages().forEach((k, v) -> {
			Image coveredImageResized = v.getScaledInstance((int)internalSquareWidth, (int)internalSquareHeight, Image.SCALE_AREA_AVERAGING);					
			MainPanel.this.images.put(k, coveredImageResized);
		});
		squareWidth = internalSquareWidth;
		squareHeight = internalSquareHeight;
	}

	/**
	 * Method used to initialize this JPanel with the grid and the right images.
	 * @param grid The grid used by this JPanel.
	 */
	public void init(Grid grid) {
		this.grid = grid;
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				updateImages();
				revalidate();
				repaint();
			}
		});
		updateImages();
		revalidate();
		repaint();
	}

	/**
	 * Method used to convert a Java AWT Point into a Location.
	 * 
	 * @param point The point to convert.
	 * @return The Location corresponding to the input Point
	 */
	public Location getSquareLocation(Point point) {
		if (grid == null) {
			return new Location(-1, -1);
		}
		return new Location((int) (point.y / squareHeight), (int) (point.x / squareWidth));
	}
	
	@Override
	public void paintComponent(Graphics g) {
		if (grid == null) {
			return;
		}
		if (squareWidth == 0.0f || squareHeight == 0.0f) {
			return;
		}
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());
		// draw the right Square Images if the grid is populated
		if (grid.isPopulated()) {
			grid.getGridAsStream().forEach(s -> {
				String string = ImageUtils.getInstance().getSquareImage(s);
				g.drawImage(images.get(string), (int)(squareWidth*s.getLocation().getColumn()), (int)(squareHeight*s.getLocation().getRow()), null);
			});
		} else { // otherwise draw all covered images
			Image coveredImage = images.get("covered");
			for (int r = 0; r < grid.getConfig().getN_ROWS(); r++) {
				for (int c = 0; c < grid.getConfig().getN_COLUMNS(); c++) {
					g.drawImage(coveredImage, (int)(squareWidth*c), (int)(squareHeight*r), null);
				}
			}
		}
		// draw horizontal and vertical lines to separate the squares
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
