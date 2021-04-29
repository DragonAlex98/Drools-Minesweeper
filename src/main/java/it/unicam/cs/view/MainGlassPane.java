package it.unicam.cs.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import it.unicam.cs.utils.SquareImages;
import lombok.Getter;

/**
 * UI Class used to manage the graphic representation of a waiting phase as JComponent.
 *
 */
public class MainGlassPane extends JComponent implements KeyListener {

	private static final long serialVersionUID = 1L;
	/** Background color of this component **/
	private static final Color BACKGROUND_COLOR = new Color(255, 255, 255, 96);
	/** Gif to display while waiting **/
	private static final JLabel iconLabel = new JLabel(SquareImages.getInstance().getIcons().get("loading"));
	/** Button used to stop the waiting **/
	@Getter
	private JButton stopButton = new JButton("Stop");
	/** Progress bar to represent remaining time **/
	@Getter
	private JProgressBar progressBar = new JProgressBar(0, 100);

	public MainGlassPane() {
		setOpaque(false);
		setBackground(BACKGROUND_COLOR);
		stopButton.setForeground(Color.RED);
		stopButton.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		progressBar.setForeground(Color.GREEN);
		// split the component into three vertical sections
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 0, 5, 0);
		constraints.gridy = 0;
		add(iconLabel, constraints);
		constraints.gridy = 1;
		add(stopButton, constraints);
		constraints.gridy = 2;
		add(progressBar, constraints);

		setFocusTraversalKeysEnabled(false);
		addMouseListener(new MouseAdapter() {});
		addMouseMotionListener(new MouseMotionAdapter() {});
		addKeyListener(this);
	}

	/**
	 * Method used to activate and display this component.
	 */
	public void activate() {
		setVisible(true);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		requestFocusInWindow();
	}

	/**
	 * Method used to deactivate this component
	 */
	public void deactivate() {
		setCursor(null);
		setVisible(false);
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		iconLabel.setBackground(bg);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		e.consume();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		e.consume();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		e.consume();
	}
}
