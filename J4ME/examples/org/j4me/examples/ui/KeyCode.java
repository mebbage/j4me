package org.j4me.examples.ui;

import javax.microedition.lcdui.*;
import org.j4me.ui.*;
import org.j4me.ui.components.*;

/**
 * Displays the integer key code value for any button pressed
 * on the device.  This helps track down the values for the
 * left and right menu buttons and other special keys.
 */
public class KeyCode
	extends Dialog
{
	/**
	 * The screen to return to once the user is done with this one.
	 */
	private final DeviceScreen parent;
	
	/**
	 * The label that the key code is put into.
	 */
	private final Label code;
	
	/**
	 * Constructs the Key Code screen.
	 * 
	 * @param parent is the screen to return to when the user
	 *  hits the "Back" menu button.
	 */
	public KeyCode (DeviceScreen parent)
	{
		this.parent = parent;
		
		// Adds the title bar to the canvas.
		// If title is null there won't be a title bar. 
		setTitle( "Key Code" );
		
		// Add the menu options to the canvas.
		//   Initially there is no "Back" button on the left.
		//   Use an empty string on the right to show an empty menu bar.
		setMenuText( null, "" );

		// Add help text for this screen.
		Label help = new Label();
		help.setLabel(
				"Press any button to get its integer key code value.  " +
				"This is passed into the keyPressed, keyRepeated, and " +
				"keyReleased methods.\n" +
				"Press the left menu button twice to leave this screen.");
		//append( help );
		
		// Add the label for the key code.
		code = new Label();
		code.setHorizontalAlignment( Graphics.HCENTER );
		code.setFont( Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_LARGE) );
		append( code );
	}

	/**
	 * Called when the user presses the left menu button.
	 * This goes back to the previous screen.
	 * 
	 * @see DeviceScreen#declineNotify()
	 */
	protected void declineNotify ()
	{
		// Was this the first time the left menu button was pressed?
		if ( getLeftMenuText() == null )
		{
			setMenuText( "Back", null );
		}
		
		// The left menu button was already pressed so dismiss the screen.
		else
		{
			parent.show();
		}
	}

	/**
	 * Called when the user presses a key.
	 * 
	 * @see DeviceScreen#keyPressed(int)
	 */
	protected void keyPressed (int keyCode)
	{
		// Display the key code.
		String key = String.valueOf( keyCode );
		code.setLabel( key );
		
		// Update the screen.
		invalidate();
		repaint();
		
		// Continue processing the event.
		super.keyPressed( keyCode );
	}
}
