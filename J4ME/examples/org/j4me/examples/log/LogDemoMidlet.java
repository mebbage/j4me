package org.j4me.examples.log;

import javax.microedition.midlet.*;
import org.j4me.examples.ui.themes.*;
import org.j4me.ui.*;

/**
 * A demonstration MIDlet for the J4ME Log package.  It logs properties about
 * the phone it is running on.
 */
public class LogDemoMidlet
	extends MIDlet
{
	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#startApp()
	 */
	protected void startApp () throws MIDletStateChangeException
	{
		// Initialize the J4ME UI manager.
		UIManager.init( this );
		UIManager.setTheme( new RedTheme() );

		// Show the log demo screen.
		LogDemoScreen log = new LogDemoScreen();
		log.show();
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
