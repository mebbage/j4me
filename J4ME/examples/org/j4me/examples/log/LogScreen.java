package org.j4me.examples.log;

import org.j4me.logging.*;
import org.j4me.ui.*;
import org.j4me.ui.components.*;

/**
 * The "Log" screen.  This shows the contents of the application's
 * log.  It is an advanced screen intended for us to diagnose the
 * application.
 */
public class LogScreen
	extends Dialog
{
	/**
	 * The screen that came before this one.  It is returned to once
	 * this screen exits.
	 */
	private final DeviceScreen previous;
	
	/**
	 * Constructs the "Log" screen.
	 * 
	 * @param previous is the screen that invoked this one.  If this is
	 *  <c>null</c> the application will exit when this screen is dismissed.
	 */
	public LogScreen (DeviceScreen previous)
	{
		// Set the title.
		setTitle( "Log" );
		
		// Record the screens.
		this.previous = previous;
		
		// Add the menu buttons.
		setMenuText( "Back", "Log Menu" );
	}
	
	/**
	 * Called when this screen is going to be displayed.  It populates
	 * the log screen with all of the log messages in memory.
	 * 
	 * @see DeviceScreen#showNotify()
	 */
	public void showNotify ()
	{
		// Clear this form.
		deleteAll();

		HorizontalRule line = new HorizontalRule();

		// Add the log messages.
		LogStatement item = null;
		LogMessage[] logs = Log.getLogMessages();
		
		for ( int i = 0; i < logs.length; i++ )
		{
			// Add a horizontal rule to demarkate it from the last.
			if ( i != 0 )
			{
				append( line );
			}
			
			// Add a log statement component.
			item = new LogStatement( logs[i] );
			append( item );
		}
		
		// Scroll to the last log statement.
		if ( item != null )
		{
			int last = logs.length - 1;
			int position = last * 2;  // Account for horizontal rules
			setSelected( position );
		}
	}
	
	/**
	 * Called when the user presses the "Back" button.
	 * 
	 * @see DeviceScreen#declineNotify()
	 */
	protected void declineNotify ()
	{
		// Go back to the previous screen.
		if ( previous != null )
		{
			previous.show();
		}
	}

	/**
	 * Called when the user presses the "Log Menu" button.
	 * 
	 * @see DeviceScreen#acceptNotify()
	 */
	protected void acceptNotify ()
	{
		// Show the log options.
		LogOptionsScreen options = new LogOptionsScreen( this );
		options.show();
	}
}
