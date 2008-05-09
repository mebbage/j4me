package org.j4me.examples.ui.components;

import javax.microedition.lcdui.*;
import org.j4me.ui.*;

/**
 * Example demonstrating the scrollbar's functionality.  This
 * helps test the scrollbar too.
 */
public class ScrollbarExample
	extends DeviceScreen
{
	/**
	 * The previous screen.
	 */
	private DeviceScreen previous;

	/**
	 * The total number of pixels managed by the scrollbar.
	 */
	private int totalHeight;
	
	/**
	 * The number of pixels we've scrolled down.  This can be between 0
	 * for the very top to <code>totalHeight - getHeight()</code>.
	 */
	private int scrolledDown;
	
	/**
	 * How much to scroll the screen up or down with each joystick press.
	 */
	private int scrollIncrement;

	/**
	 * Constructs a screen that shows scrollbars in action.
	 * 
	 * @param previous is the screen to return to once this done.
	 */
	public ScrollbarExample (DeviceScreen previous)
	{
		this.previous = previous;
		
		// Set the title and menu for this screen.
		setTitle( "Scrollbar Example" );
		setMenuText( "Back", null );
		
		// Set the sizes of our scrollable area.
		totalHeight = getHeight() * 5;
		scrolledDown = 0;
		scrollIncrement = (int)( totalHeight / 7.5 );
	}
	
	/**
	 * Takes the user to the previous screen.
	 */
	protected void declineNotify ()
	{
		previous.show();
	}

	/**
	 * Paints the scrollbar on the right edge of the screen.  The
	 * scrollbar attributes are shown on the remainder of the canvas. 
	 * 
	 * @see DeviceScreen#paint(Graphics)
	 */
	protected void paint (Graphics g)
	{
		Theme theme = UIManager.getTheme();
		int width = getWidth();
		int height = getHeight();
		
		// Paint the scrollbar.
		theme.paintVerticalScrollbar(
				g,
				0, 0, width, height,
				scrolledDown, totalHeight );
		
		// Add markers every 10% of the screen.
		int markerSpacing = totalHeight / 10;
		
		int fontHeight = g.getFont().getHeight();
		int fontTop = fontHeight / 2;
		
		int fontX = width - theme.getVerticalScrollbarWidth();
		
		int anchor = Graphics.RIGHT | Graphics.TOP;
		
		for ( int i = 0; i <= 10; i++ )
		{
			// Write the marker.
			String marker = String.valueOf( i * 10 ) + "% ";
			int markerWidth = g.getFont().stringWidth( marker );
			
			int y = i * markerSpacing - scrolledDown;
			int fontY = y - fontTop;
			
			g.drawString( marker, fontX, fontY, anchor );
			
			// Draw a line for the marker.
			g.drawLine( 0, y, fontX - markerWidth - 3, y );
		}
	}

	/**
	 * Moves the scrollbar up and down.
	 */
	protected void keyPressed (int keyCode)
	{
		if ( keyCode == UP )
		{
			scrolledDown -= scrollIncrement;
			
			if ( scrolledDown < 0 )
			{
				scrolledDown = 0;
			}
			
			repaint();
		}
		else if ( keyCode == DOWN )
		{
			scrolledDown += scrollIncrement;
			
			if ( scrolledDown >= totalHeight - getHeight() )
			{
				scrolledDown = totalHeight - getHeight() - 1;
			}
			
			repaint();
		}
		
		super.keyPressed( keyCode );
	}
}
