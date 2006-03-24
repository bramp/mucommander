package com.mucommander.ui;

import com.mucommander.ui.icon.IconManager;

import javax.swing.*;

import java.awt.*;


/**
 * Splash screen that gets displayed on muCommander startup.
 *
 * <p>The splash screen is made of a logo image on top of which is displayed muCommander version number (in the top right corner)
 * and a loading message (in the lower left corner) which is updated by {@link #com.mucommander.Launcher} to show startup progress. 
 * It is then closed by {@link #com.mucommander.Launcher} when muCommander is fully started and ready for use.</p> 
 *
 * @author Maxence Bernard
 */
public class SplashScreen extends JWindow {

	/** muCommander version displayed on this splash screen */
	private String version;

	/** Current loading message displayed on this splash screen */
	private String loadingMessage;

	/** Font used to display version and loading message on this splash screen */
	private Font customFont;

	/** Path to the splash screen logo image within the JAR file */
	private final static String LOGO_IMAGE_PATH = "/logo.png";

	/** Name of the font used to display text on this splash screen */
	private final static String FONT_NAME = "Courier";
	/** Style of the font used to display text on this splash screen */
	private final static int FONT_STYLE = Font.PLAIN;
	/** Size of the font used to display text on this splash screen */
	private final static int FONT_SIZE = 11;
	
	/** Color of the text displayed on this splash screen */ 
	private final static Color TEXT_COLOR = new Color(192, 238, 241);

	/** Number of pixels between the loading message and the left side of the splash image */
	private final static int LOADING_MSG_MARGIN_X = 4;
	/** Number of pixels between the loading message and the bottom of the splash image */
	private final static int LOADING_MSG_MARGIN_Y = 6;

	/** Number of pixels between the version information and the right side of the splash image */
	private final static int VERSION_MARGIN_X = 5;
	/** Number of pixels between the version information and the top of the splash image */
	private final static int VERSION_MARGIN_Y = 3;


	/**
	 * Creates and displays a new SplashScreen, with the given version string and initial loading message.
	 *
	 * @param version muCommander version string which will be displayed in the top right corner
	 * @param loadingMessage initial loading message, displayed in the lower left corner
	 */
	public SplashScreen(String version, String loadingMessage) {
		this.version = version;
		this.loadingMessage = loadingMessage;

		// Create a custom font
		this.customFont = new Font(FONT_NAME, FONT_STYLE, FONT_SIZE);

		// Resolves the URL of the splash logo image within the JAR file and create an ImageIcon
		ImageIcon imageIcon = IconManager.getIcon(LOGO_IMAGE_PATH);

		// Wait for the image to be fully loaded
		MediaTracker mediaTracker = new MediaTracker(this);
		mediaTracker.addImage(imageIcon.getImage(), 0);
		try { mediaTracker.waitForID(0); }
		catch(InterruptedException e) {}

		setContentPane(new JLabel(imageIcon));
		
		// Set size manually instead of using pack(), because of a bug under 1.3.1/Win32 which
		// eats a 1-pixel row of the image
//		pack();
		int width = imageIcon.getIconWidth();
		int height = imageIcon.getIconHeight();
		setSize(width, height);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width/2 - width/2,
					     screenSize.height/2 - height/2);

		// Display the splash screen
	    show();
	}


	/**
	 * Repaints this SplashScreen to display the new given loading message, replacing the previous one. 
	 *
	 * @param msg the new loading message to be displayed
	 */
	public void setLoadingMessage(String msg) {
		this.loadingMessage = msg;
		repaint();
	}


	/**
	 * Overridden paint method.
	 */
	public void paint(Graphics g) {
		super.paint(g);

		g.setFont(customFont);
		g.setColor(TEXT_COLOR);
		
		// Get FontRenderContext instance to calculate text width and height
		java.awt.font.FontRenderContext fontRenderContext = ((Graphics2D)g).getFontRenderContext();
		// Display loading message in the lower left corner
		g.drawString(loadingMessage, LOADING_MSG_MARGIN_X, getHeight()-LOADING_MSG_MARGIN_Y);
		// Display version in the top right corner
		java.awt.geom.Rectangle2D textBounds = new java.awt.font.TextLayout(version, customFont, fontRenderContext).getBounds();
		g.drawString(version, getWidth()-(int)textBounds.getWidth()-VERSION_MARGIN_X, (int)textBounds.getHeight()+VERSION_MARGIN_Y);
	}	
}