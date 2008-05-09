package org.j4me.examples.ui.components;

import org.j4me.ui.*;
import org.j4me.ui.components.*;

/**
 * Shows a <code>ProgressBar</code> component in action.
 */
public class ProgressBarExample
	extends Dialog
{
	/**
	 * The previous screen.
	 */
	private DeviceScreen previous;
	
	/**
	 * The progress bar demonstrated by this example.
	 */
	private ProgressBar bar = new ProgressBar();
	
	/**
	 * Constructs a screen that shows a <code>ProgressBar</code> component in action.
	 * 
	 * @param previous is the screen to return to once this done.
	 */
	public ProgressBarExample (DeviceScreen previous)
	{
		this.previous = previous;
		
		// Set the title and menu for this screen.
		setTitle( "ProgressBar Example" );
		setMenuText( "Back", null );

		// Add a label giving the user instructions.
		Label instructions = new Label();
		append( instructions );
		
		instructions.setLabel( "Move the joystick left and right to advance the progress bar." );
		
		// Add the progress bar to this screen.
		bar.setMaxValue( 10 );
		bar.setLabel( "0 of " + bar.getMaxValue() );
		append( bar );
		
		// Add an indefinate progress bar.
		ProgressBar spinner = new ProgressBar();
		spinner.setLabel( "Max of 0 shows a spinner to indicate an unknown duration." );
		append( spinner );
	}

	/**
	 * Change the value of the progress bar on joystick movements.
	 */
	protected void keyPressed (int key)
	{
		updateProgressBar( key );
		
		// Continue processing the key event.
		super.keyPressed( key );
	}

	/**
	 * Change the value of the progress bar on joystick movements.
	 */
	protected void keyRepeated (int key)
	{
		updateProgressBar( key );
		
		// Continue processing the key event.
		super.keyRepeated( key );
	}

	/**
	 * Moves the progress bar when the joystick has been moved.
	 * 
	 * @param key is the key code for the button pressed by the user.
	 */
	private void updateProgressBar (int key)
	{
		if ( key == LEFT || key == RIGHT )
		{
			int value = bar.getValue();

			if ( key == LEFT )
			{
				value--;
			}
			else if ( key == RIGHT )
			{
				value++;
			}
			
			bar.setValue( value );
			
			bar.setLabel( bar.getValue() + " of " + bar.getMaxValue() );
			bar.repaint();
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
