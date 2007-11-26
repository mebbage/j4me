package org.j4me;

import javax.microedition.lcdui.*;
import j2meunit.framework.*;
import j2meunit.midletui.*;

/**
 * Runs all of the unit tests for the <code>Common</code> and <code>Caddie</code>
 * projects.
 */
public class TestsMidlet
	extends TestRunner
	implements CommandListener
{
	private Command exitButton;
	
	private int testCount = 1;
	
	/**
	 * Override J2MEUnit's <code>TestRunner.startApp</code> to add menus.
	 */
	protected void startApp ()
	{
		start( new String[] { "org.j4me.J4METestSuite" } );

		// Add the menu when the tests are running.
		Display display = Display.getDisplay( this );
		Displayable running = display.getCurrent();
		createMenu( running );
		
		// Add the menu when the results are shown.
		Displayable results = getResultsList();
		createMenu( results );
	}
	
	/**
	 * Override the footer.  We don't want it (says J2MEUnit and the authors).
	 */
	public void printFooter ()
	{
		// Automatically exit the program if the "ExitWhenDone" property is set.
		//  To automatically run a Midlet from Ant without manual interaction we
		//  need a way to exit the emulator when the tests are done.  Since this
		//  method is called after the tests have run, we can just exit from here.
		//  We look for a special property added to the JAD by the Ant scripts.
		String exit = getAppProperty("ExitWhenDone");
		
		if ( exit != null )
		{
			notifyDestroyed();
		}
	}
	
	/**
	 * Override the method that prints a "." to the console everytime a test starts.
	 */
	public synchronized void startTest (Test test)
	{
		TestCase tc = (TestCase)test;
		System.out.println("\n=====  (" + testCount++ + ") " + tc.getClass().getName() + "." + tc.getName() + "  =====");
		System.out.flush();
	}
	
	/**
	 * Override the method that prints an "E" to the console everytime a test has an error.
	 */
	public synchronized void addError (Test test, Throwable t)
	{
		System.out.println("ERROR");
	}
	
	/**
	 * Override the method that prints an "F" to the console everytime a test has a failure.
	 */
	public synchronized void addFailure (Test test, AssertionFailedError e)
	{
		System.out.print("FAILURE");
	}
	
	/**
	 * Adds an exit button to the canvas so the user can quit the
	 * tests.
	 * 
	 * @param canvas is the screen to add the exit button to.
	 */
	private void createMenu (Displayable canvas)
	{
		exitButton = new Command( "Exit", Command.EXIT, 1 );
		canvas.addCommand( exitButton );
		canvas.setCommandListener( this );
	}
	
	/**
	 * Called when a menu item is selected.
	 * 
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction (Command c, Displayable d)
	{
		// See if the user is trying to exit.
		if ( c == exitButton )
		{
			notifyDestroyed();
		}
	}
}
