package org.j4me.examples.ui;

import java.util.*;
import javax.microedition.lcdui.*;
import org.j4me.ui.*;

/**
 * Makes the screen into an Etch A Sketch.  Moving the joystick causes
 * lines to be drawn.  The drawing can be erased with the "Shake" button.
 * <p>
 * This class demonstrates how to use a canvas class.  Canvas classes
 * are useful when the entire screen needs to be manually drawn.
 */
public class EtchASketch
	extends DeviceScreen
{
	/**
	 * The screen to return to once the user is done with this one.
	 */
	private final DeviceScreen parent;
	
	/**
	 * The list of points that have been sketched.  It is filled with
	 * <code>int[2]</code> arrays for each point.
	 */
	private Vector points = new Vector();
	
	/**
	 * The current point being drawn.  The first element is the X-coordinate
	 * and the second is the Y-coordinate.
	 */
	private int[] current = new int[2];
	
	/**
	 * Constructs the Etch A Sketch screen.
	 * 
	 * @param parent is the screen to return to when the user
	 *  hits the "Back" menu button.
	 */
	public EtchASketch (DeviceScreen parent)
	{
		this.parent = parent;
		
		// Adds the title bar to the canvas.
		// If title is null there won't be a title bar. 
		setTitle( "Etch A Sketch" );
		
		// Adds the menu options to the canvas.
		// These are invoked by the declineNotify() and
		// acceptNotify() methods respectively.
		setMenuText( "Back", "Shake" );
	}

	/**
	 * Called when this screen is made visible.  It sets the
	 * position of the cursor to the center of the screen.
	 * 
	 * @see DeviceScreen#showNotify()
	 */
	public void showNotify ()
	{
		current[0] = getWidth() / 2;
		current[1] = getHeight() / 2;
	}

	/**
	 * Called when the user presses the left menu button.
	 * This goes back to the previous screen.
	 * 
	 * @see DeviceScreen#declineNotify()
	 */
	protected void declineNotify ()
	{
		parent.show();
	}

	/**
	 * Called when the user presses the right menu button.
	 * This clears the screen.
	 * 
	 * @see DeviceScreen#acceptNotify()
	 */
	protected void acceptNotify ()
	{
		points = new Vector();
		repaint();
	}

	/**
	 * Called when the user presses a key.
	 * 
	 * @see DeviceScreen#keyPressed(int)
	 */
	protected void keyPressed (int key)
	{
		// Move the drawing point.
		move( key );
		
		// Continue processing the key event.
		super.keyPressed( key );
	}

	/**
	 * Called when the user holds down a key.
	 * 
	 * @see Canvas#keyRepeated(int)
	 */
	protected void keyRepeated (int key)
	{
		// Move the drawing point.
		move( key );
		
		// Continue processing the key event.
		super.keyRepeated( key );
	}
	
	/**
	 * Moves the current drawing point if <code>key</code> is a joystick
	 * moving event.
	 * 
	 * @param key is the key code activated by a key event.
	 */
	private void move (int key)
	{
		if ( key == LEFT || key == RIGHT || key == UP || key == DOWN )
		{
			// Record the current point to draw.
			points.addElement( new int[] { current[0], current[1] } );
			
			// Move the point.
			if ( (key == LEFT) && (current[0] > 0) )
			{
				current[0] -= 3;
			}
			else if ( (key == RIGHT) && (current[0] < getWidth() - 1) )
			{
				current[0] += 3;
			}
			else if ( (key == UP) && (current[1] > 0) )
			{
				current[1] -= 3;
			}
			else if ( (key == DOWN) && (current[1] < getScreenHeight() - 1) )
			{
				current[1] += 3;
			}
			
			// Repaint the screen to reflect the drawing.
			repaint();
		}
	}

	/**
	 * Moves the drawing point to where the stylus was pressed.
	 * 
	 * @see org.j4me.ui.DeviceScreen#pointerPressed(int, int)
	 */
	protected void pointerPressed (int x, int y)
	{
		move( x, y );
	}

	/**
	 * Moves the drawing point to where the stylus was dragged.
	 * 
	 * @see org.j4me.ui.DeviceScreen#pointerDragged(int, int)
	 */
	protected void pointerDragged (int x, int y)
	{
		move( x, y );
	}

	/**
	 * Moves the current drawing point for stylus events.
	 * 
	 * @param x is the horizontal position of the pointer.
	 * @param y is the vertical position of the pointer.
	 */
	private void move (int x, int y)
	{
		// Record the new point as the current.
		current[0] = x;
		current[1] = y;
		
		// Add the new point.
		points.addElement( new int[] { x, y } );
		
		// Repaint the screen to reflect the drawing.
		repaint();
	}

	/**
	 * Paints the current Etch A Sketch drawing.
	 * 
	 * @see DeviceScreen#paint(Graphics)
	 */
	protected void paint (Graphics g)
	{
		// Set the color of the line.
		Theme theme = UIManager.getTheme();
		int color = theme.getHighlightColor();
		g.setColor( color );
		
		// Draw lines between all points. 
		int[] lastPoint = null;
		Enumeration e = points.elements();
		
		while ( e.hasMoreElements() )
		{
			int[] newPoint = (int[])e.nextElement();
			
			if ( lastPoint != null )
			{
				g.drawLine( lastPoint[0], lastPoint[1], newPoint[0], newPoint[1] );
			}
			
			lastPoint = newPoint;
		}
	}
}
