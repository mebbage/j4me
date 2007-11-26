package org.j4me.examples.log;

import org.j4me.logging.*;
import org.j4me.ui.*;
import org.j4me.ui.components.*;

/**
 * The "Log Options" screen.  It changes the logging options for the
 * application.
 */
public class LogOptionsScreen
	extends Dialog
{
	/**
	 * The screen that came before this one.  It is returned to once
	 * this screen exits.
	 */
	private final DeviceScreen previous;
	
	/**
	 * The UI component that lets the user select the level at which
	 * the applications logs.
	 */
	private final RadioButton logLevel;
	
	/**
	 * Clears all the logs if checked.
	 */
	private final CheckBox clear;
	
	/**
	 * Constructs the "Log" screen.
	 * 
	 * @param previous is the screen that invoked this one.
	 */
	public LogOptionsScreen (DeviceScreen previous)
	{
		// Record the screens.
		this.previous = previous;
		
		// Set the title.
		setTitle( "Log Options" );
		
		// Add the UI components.
		logLevel = new RadioButton();
		logLevel.setLabel( "Level" );
		logLevel.append( Level.DEBUG.toString() );
		logLevel.append( Level.INFO.toString() );
		logLevel.append( Level.WARN.toString() );
		logLevel.append( Level.ERROR.toString() );
		logLevel.append( Level.OFF.toString() );
		append( logLevel );
		
		clear = new CheckBox();
		clear.setLabel( "Clear log" );
		append( clear );
		
		// Set the current log level.
		Level current = Log.getLogLevel();
		int selection = -1;
		
		if ( current == Level.DEBUG )		selection = 0;
		else if ( current == Level.INFO )	selection = 1;
		else if ( current == Level.WARN )	selection = 2;
		else if ( current == Level.ERROR )	selection = 3;
		else if ( current == Level.OFF )	selection = 4;
		
		logLevel.setSelectedIndex( selection );
	}
	
	/**
	 * Called when this screen is about to be displayed.
	 * 
	 * @see org.j4me.ui.DeviceScreen#showNotify()
	 */
	public void showNotify ()
	{
		// Make sure the checkbox to clear the log is unchecked.
		//  If the user cleared it last time it would retain its state
		//  as checked.  The user might just be changing the log level
		//  and accidentally clear the logs.
		clear.setChecked( false );
		
		// Continue processing the event.
		super.showNotify();
	}

	/**
	 * Called when the user presses the "Cancel" button.
	 * 
	 * @see DeviceScreen#declineNotify()
	 */
	protected void declineNotify ()
	{
		// Go back to the previous screen.
		previous.show();
	}

	/**
	 * Called when the user presses the "OK" button.
	 * 
	 * @see DeviceScreen#acceptNotify()
	 */
	protected void acceptNotify ()
	{
		// Store the new log level.
		Level level = Log.getLogLevel();
		int selection = logLevel.getSelectedIndex();
		
		if ( selection == 0 ) level = Level.DEBUG;
		else if ( selection == 1 ) level = Level.INFO;
		else if ( selection == 2 ) level = Level.WARN;
		else if ( selection == 3 ) level = Level.ERROR;
		else if ( selection == 4 ) level = Level.OFF;
		
		Log.setLevel( level );

		// Clear the log?
		if ( clear.isChecked() )
		{
			Log.clear();
		}
		
		// Go back to the previous screen.
		previous.show();
	}
}
