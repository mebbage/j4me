package org.j4me.examples.ui.components;

import org.j4me.ui.*;
import org.j4me.ui.components.*;

/**
 * Example of a <code>CheckBox</code> component.
 */
public class CheckBoxExample
	extends Dialog
{
	/**
	 * The previous screen.
	 */
	private DeviceScreen previous;
	
	/**
	 * The check box.
	 */
	private CheckBox checkbox;
	
	/**
	 * Constructs a screen that shows a <code>CheckBox</code> component in action.
	 * 
	 * @param previous is the screen to return to once this done.
	 */
	public CheckBoxExample (DeviceScreen previous)
	{
		this.previous = previous;
		
		// Set the title and menu.
		setTitle( "CheckBox Example" );
		setMenuText( "Back", null );

		// Add the phone number box.
		checkbox = new CheckBox();
		checkbox.setLabel( "I agree to the terms." );
		checkbox.setChecked( true );
		append( checkbox );
	}

	/**
	 * Takes the user to the previous screen.
	 */
	protected void declineNotify ()
	{
		previous.show();
	}
}
