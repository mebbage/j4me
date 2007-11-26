package org.j4me.examples.ui.components;

import javax.microedition.lcdui.*;
import org.j4me.ui.*;
import org.j4me.ui.components.*;

/**
 * Shows a <code>Label</code> component in action.  The label displays
 * mutliple paragraphs, line breaks, and text justification.
 */
public class LabelExample
	extends Dialog
{
	/**
	 * The previous screen.
	 */
	private DeviceScreen previous;
	
	/**
	 * The label demonstrated by this example.
	 */
	private Label label = new Label();
	
	/**
	 * Constructs a screen that shows a <code>Label</code> component in action.
	 * 
	 * @param previous is the screen to return to once this done.
	 */
	public LabelExample (DeviceScreen previous)
	{
		this.previous = previous;
		
		// Set the title and menu for this screen.
		setTitle( "Label Example" );
		setMenuText( "Back", null );
		
		// Center the text.
		label.setHorizontalAlignment( Graphics.HCENTER );

		// Make the label be mutliple paragraphs.
		label.setLabel(
				"This is a label component.  It shows text using the theme's font " +
				"and color.  However, that can be overridden using the setFont() " +
				"and setFontColor() methods.\n" +
				"This label is center aligned.  Labels can also be left or right " +
				"aligned.\n" +
				"Labels can span multiple paragraphs using the '\\n' character.\n" +
				"Labels will wrap text if they take up more than one line.  " +
				"Wraps happen at the last space, hyphen, or slash characters.  "  +
				"If a single word is bigger than a line, it will break mid-word." );
		
		// Add the label to this screen.
		append( label );
	}

	/**
	 * Takes the user to the previous screen.
	 */
	protected void declineNotify ()
	{
		previous.show();
	}
}
