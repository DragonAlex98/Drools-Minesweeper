package it.unicam.cs.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import it.unicam.cs.model.SquareImages;
import lombok.Getter;

public class MainGlassPane extends JComponent implements KeyListener {
	private static final long serialVersionUID = 1L;

	private static final Color BACKGROUND_COLOR = new Color(255, 255, 255, 96);
	private static final JLabel iconLabel = new JLabel(SquareImages.getInstance().getIcons().get("loading"));
	@Getter
	private JButton stopButton = new JButton("Stop");
	
	public MainGlassPane() {
		setOpaque(false);
		setBackground(BACKGROUND_COLOR);
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridy = 0;
		add(iconLabel, constraints);
		constraints.gridy = 1;
		add(stopButton, constraints);
		stopButton.setForeground(Color.RED);
		stopButton.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		setFocusTraversalKeysEnabled(false);
		addMouseListener(new MouseAdapter() {});
		addMouseMotionListener(new MouseMotionAdapter() {});
		addKeyListener(this);
	}
	
	protected void paintComponent(Graphics g) {
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());
	}
	
	public void setBackground(Color bg) {
		super.setBackground(bg);
		iconLabel.setBackground(bg);
	}
	
	public void activate()
	{
		setVisible(true);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		requestFocusInWindow();
	}
	
	public void deactivate()
	{
		setCursor(null);
		setVisible(false);
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
