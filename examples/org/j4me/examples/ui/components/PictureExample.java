package org.j4me.examples.ui.components;

import java.io.*;
import javax.microedition.lcdui.*;
import org.j4me.ui.*;
import org.j4me.ui.components.*;

/**
 * Shows a <code>Picture</code> component which displays image resources
 * within the Jar.
 */
public class PictureExample
	extends Dialog
{
	/**
	 * The location of the image within the Jar file.
	 */
	private static final String IMAGE_LOCATION = "/J4ME.png";
	
	/**
	 * The previous screen.
	 */
	private DeviceScreen previous;
	
	/**
	 * The label demonstrated by this example.
	 */
	private Picture picture = new Picture();
	
	/**
	 * Constructs a screen that shows a <code>Picture</code> component in action.
	 * 
	 * @param previous is the screen to return to once this done.
	 */
	public PictureExample (DeviceScreen previous)
	{
		this.previous = previous;
		
		// Set the title and menu for this screen.
		setTitle( "Picture Example" );
		setMenuText( "Back", null );
		
		try
		{
			// Center the picture.
			picture.setHorizontalAlignment( Graphics.HCENTER );
			
			// The image location within the Jar.
			picture.setImage( IMAGE_LOCATION );
			
			// Add the picture to this screen.
			append( picture );
		}
		catch (IOException e)
		{
			// Show an error message instead of the picture.
			Label error = new Label( "Error:  Could not find image " + IMAGE_LOCATION );
			append( error );
		}
	}

	/**
	 * Takes the user to the previous screen.
	 */
	protected void declineNotify ()
	{
		previous.show();
	}
}
