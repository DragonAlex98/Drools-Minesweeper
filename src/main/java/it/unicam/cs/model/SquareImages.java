package it.unicam.cs.model;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.view.MainFrame;
import it.unicam.cs.view.MainPanel;
import lombok.Getter;

/**
 * Singleton class used to load the images representing the different Squares.
 *
 */
public class SquareImages {
	private static SquareImages instance = null;

	@Getter
	private final Map<String, BufferedImage> images;
	@Getter
	private final Map<String, Icon> icons;

	private SquareImages() {
		this.images = new HashMap<String, BufferedImage>();
		try {
			this.images.put("bomb", ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/bomb.png")));
			this.images.put("covered", ImageIO.read(MainPanel.class.getResource("/it/unicam/cs/images/newcovered.png")));
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
			e.printStackTrace();
		}
		this.icons = new HashMap<String, Icon>();
		this.icons.put("win", new ImageIcon(new ImageIcon(MainFrame.class.getResource("/it/unicam/cs/images/fireworks.gif")).getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT)));
		this.icons.put("loss", new ImageIcon(new ImageIcon(MainFrame.class.getResource("/it/unicam/cs/images/explosion.gif")).getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT)));
		this.icons.put("stat", new ImageIcon(new ImageIcon(MainFrame.class.getResource("/it/unicam/cs/images/stat.png")).getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT)));
	}

	public static SquareImages getInstance() {
		if (instance == null) {
			instance = new SquareImages();
		}
		return instance;
	}

	/**
	 * Method to return the corresponding Image associated to the given Square.
	 * 
	 * @param square The Square to associate the Image with.
	 * @return The Image associated to the given Square.
	 */
	public String getSquareImage(Square square) {
		if (square.getState() == SquareState.COVERED) {
			return "covered";
		} else if (square.getState() == SquareState.FLAGGED) {
			return "flag";
		} else if (square.getState() == SquareState.EXPLODED) {
			return "expbomb";
		} else {
			if (square.getType() == SquareType.BOMB) {
				return "bomb";
			} else if (square.getType() == SquareType.EMPTY) {
				return "empty";
			} else if (square.getType() == SquareType.NUMBER) {
				return ((Number) square).getNumber();
			}
		}
		return null;
	}
}
