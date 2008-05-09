package org.j4me.examples.bluetoothgps;

import javax.microedition.midlet.*;
import org.j4me.examples.ui.themes.*;
import org.j4me.ui.*;

/**
 * A demonstration MIDlet for the J4ME BluetoothGPS package.
 */
public class GPSDemoMidlet
	extends MIDlet
{
	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#startApp()
	 */
	protected void startApp () throws MIDletStateChangeException
	{
		// Initialize the J4ME UI manager.
		UIManager.init( this );
		UIManager.setTheme( new ConsoleTheme() );

		// Show the first screen.
		LocationModel model = new LocationModel();
		CriteriaSelectionScreen next = new CriteriaSelectionScreen( model );
		next.show();
	}

	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#pauseApp()
	 */
	protected void pauseApp ()
	{
		// The application has no state so ignore pauses.
	}

	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
	 */
	protected void destroyApp (boolean cleanup) throws MIDletStateChangeException
	{
		// Exit the MIDlet.
		notifyDestroyed();
	}
}
