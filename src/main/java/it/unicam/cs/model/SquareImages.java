package it.unicam.cs.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.view.MainPanel;

public class SquareImages {
	private static SquareImages instance = null;

	private final Map<String, BufferedImage> images;

	private SquareImages() {
		this.images = new HashMap<String, BufferedImage>();
		try {
			this.images.put("bomb", ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/bomb.png")));
			this.images.put("covered",
					ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/newcovered.png")));
			this.images.put("empty", ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/newempty.png")));
			this.images.put("flag", ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/flag.png")));
			this.images.put("expbomb", ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/expbomb.png")));
			this.images.put("1", ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/number1.png")));
			this.images.put("2", ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/number2.png")));
			this.images.put("3", ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/number3.png")));
			this.images.put("4", ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/number4.png")));
			this.images.put("5", ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/number5.png")));
			this.images.put("6", ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/number6.png")));
			this.images.put("7", ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/number7.png")));
			this.images.put("8", ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/number8.png")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static SquareImages getInstance() {
		if (instance == null) {
			instance = new SquareImages();
		}

		return instance;
	}

	public BufferedImage getSquareImage(Square square) {
		BufferedImage image = null;

		if (square.getState() == SquareState.COVERED) {
			image = images.get("covered");
		} else if (square.getState() == SquareState.FLAGGED) {
			image = images.get("flag");
		} else if (square.getState() == SquareState.EXPLODED) {
			image = images.get("expbomb");
		} else {
			if (square.getType() == SquareType.BOMB) {
				image = images.get("bomb");
			} else if (square.getType() == SquareType.EMPTY) {
				image = images.get("empty");
			} else if (square.getType() == SquareType.NUMBER) {
				switch (((Number) square).getNeighbourBombsCount()) {
				case 1:
					image = images.get("1");
					break;
				case 2:
					image = images.get("2");
					break;
				case 3:
					image = images.get("3");
					break;
				case 4:
					image = images.get("4");
					break;
				case 5:
					image = images.get("5");
					break;
				case 6:
					image = images.get("6");
					break;
				case 7:
					image = images.get("7");
					break;
				case 8:
					image = images.get("8");
					break;
				}
			}
		}

		return image;
	}
}
