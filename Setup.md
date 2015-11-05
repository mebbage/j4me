# Introduction #

This is a setup guide that will get your J2ME development environment up and running.  At the end you will run the J4ME examples.

It is assumed you already know Java and use Eclipse.  If you don't know Java you can still follow the setup, but you'll need to look elsewhere to learn the basics of writing J2ME code.  If you don't use Eclipse please post on the discussion board how you setup your environment.

# Sun WTK #

_5 minutes_

The Sun Java Wireless Toolkit (WTK) for CLDC emulates a J2ME environment on your computer.  There are other emulation environments available, but most are built from the Sun WTK.

  1. Visit the [Sun Java Wireless Toolkit](http://java.sun.com/products/sjwtoolkit/) page
  1. Click on the latest toolkit
  1. On the new page hit the "Download" button
  1. On the new page accept the license and click on the link for your platform (e.g. Windows)
  1. Once downloaded, run the installer
    * _Note_ that there has been a bug which requires the installation path to not have spaces.  We recommend accepting the default location.  _Do not put it in `Program Files`._

# Eclipse #

If you do not already have Eclipse [download](http://www.eclipse.org/downloads/) and install it.  The base installation package for Java development is all you need.

## ProGuard ##

_3 minutes_

ProGuard is an obfuscater for Java code.  It is especially helpful for J2ME because it shrinks the final Jar size significantly and optimizes the code.  This means it loads and runs faster on phones.

Part of the obfuscation process is removing unused classes.  If you do not use part of the J4ME library, obfuscation will remove that class from your Jar.

  1. Download [ProGuard](http://proguard.sourceforge.net/) (4.1 or later)
  1. Unzip it and remember where (you'll need this later)

## J2MEUnit ##

_3 minutes_

J2MEUnit is a port of JUnit to the J2ME environment.  J2ME does not support reflection so J2MEUnit is basically the same but with an extra method that specifies your tests.

J4ME uses J2MEUnit for its tests.  Any fixes you submit should include a J2MEUnit test case to make sure it will always behave as you expect.  Also your own project should use J2MEUnit for testing.

  1. Download [J2MEUnit](http://j2meunit.sourceforge.net/) (we recommend downloading ` 	j2meunit-all.zip` because it includes documentation)
  1. Unzip it and remember where (you'll need this later)

## Antenna ##

_(Optional) 3 minutes_

If you use Ant, you'll want Antenna.  It is a collection of Ant tasks for J2ME.  But you will be able to package your J2ME applications from Eclipse without it.

  1. Download [Antenna](http://antenna.sourceforge.net/)
  1. Unzip it and remember where (you'll need this later)

## EclipseME ##

_20 minutes_

EclipseME is _the_ plug-in for J2ME development.  It makes Eclipse properly package J2ME applications and debug them in the emulators.

  1. Open Eclipse
  1. `Help -> Software Updates -> Find and Install...`
  1. In the dialog select "Search for new features to install" and hit Next
  1. In the Install dialog hit the "New Remote Site..." button
  1. In the New Update Site dialog enter:
    * Name as `EclipseME`
    * URL as `http://www.eclipseme.org/updates/`
  1. Highlight EclipseME in the dialog and click Finish.
  1. When it is done you will need to restart Eclipse
  1. Follow the [EclipseME installation instructions](http://eclipseme.org/docs/installation.html)
    * During the installation you will need the install locations of ProGuard, J2MEUnit, and Antenna

# J4ME #

By this point you have a complete J2ME environment.  After this section you'll be running J4ME's examples and be ready to use J4ME in your own applications.

## Install J4ME ##

_2 minutes_

  1. [Download J4ME](http://code.google.com/p/j4me/downloads/list)
  1. Create a `J4ME` directory and unzip the download into it

## Add J4ME to the Eclipse Workspace ##

_5 minutes_

  1. Open Eclipse (if it isn't already)
  1. `File -> Import...`
  1. Choose `Existing Projects into Workspace` as the import source
  1. Hit the Browse button next to `Select root directory`
  1. Navigate to the `J4ME/src` directory where you unzipped the J4ME library
  1. Hit the Finish button
  1. You should see "J4ME" added to the Package Explorer window with an error icon
  1. Click on "J4ME" in the Package Explorer window and then go `Project -> Properties`
  1. Click on "Java Build Path"
  1. On the Libraries tab click the "Add External JARs..." button
  1. Navigate to `j2meunit.jar`
  1. On the Order and Export tab check `j2meunit.jar`
  1. Click OK

## Run the Examples ##

_5 minutes_

  1. In Eclipse go to `Run -> Run...`
  1. Add a new "Wireless Toolkit Emulator"
  1. In the `Name` field enter "J4ME - UI Example"
  1. In the `Project` field hit the Browse button and select "J4ME"
  1. In the `Executable` group hit the Search button next to the Midlet field
  1. Enter "org.j4me.examples.ui.UIDemoMidlet" and hit OK
  1. Hit the Apply button
  1. Repeat steps 2-7 with:
    * "J4ME - Log Example" and "org.j4me.examples.log.LogDemoMidlet"
    * "J4ME - GPS Example" and "org.j4me.examples.bluetoothgps.GPSDemoMidlet"
    * "J4ME - Unit Tests" and "org.j4me.TestsMidlet"
  1. Highlight "J4ME - UI Example" and hit "Run"

# Hello World #

At this point you have setup your J2ME environment and looked at the J4ME library.  This section lets you build your very first J2ME application.

## Creating a J2ME Project ##

_3 minutes_

  1. In Eclipse go to `File -> New -> Project...`
  1. From the New Project dialog select `J2ME Midlet Suite` and hit the Next button
  1. For the `Project Name` type "HelloWorld" and hit the Next button
  1. For the `Device` field make sure it is "DefaultColorPhone" and hit the Next button
  1. On the Projects tab hit the Add button and select "J4ME"
  1. On the Order and Export tab check J4ME
  1. Click the Finish button
  1. This will create a new Project
  1. Notice there is a "HelloWorld" project in Package Explorer with some folders and "HelloWorld.jad"
    * JAD files are unique to J2ME.  They are paired with a .jar file that is your application.  JAD files are used during installation of your application to provide metadata about it.

## J2ME Coding ##

_5 minutes_

  1. Click on the "HelloWorld" project in Project Explorer
  1. `File -> New -> Other...`
  1. From the dialog select `J2ME Midlet` and hit the Next button
  1. In the `Name` field type "HelloWorldMidlet" and hit the Finish button
  1. This will create and open `HelloWorldMidlet.java`, your application's entry point
    * All MIDlets extend the abstract class `MIDlet`
    * The `startApp` method is called by the framework when your application launches
    * The `pauseApp` method is called by the framework when your application is minimized, such as when an incoming phone call is accepted; technically you are supposed to close any connections and save your application's state, but you can leave this empty
    * The `destroyApp` method is called by the framework when your application is closing; close any connections here
  1. `File -> New -> Class`
  1. In the `Name` field type "HelloWorldScreen"
  1. In the `Superclass` field type "org.j4me.ui.Dialog"
  1. Hit the Finish button and it will create and open `HelloWorldScreen.java`
  1. Copy and paste the following code into `HelloWorldScreen.java`
```
import javax.microedition.lcdui.*;
import org.j4me.ui.*;
import org.j4me.ui.components.*;

/**
 * A screen that displays "Hello World".
 */
public class HelloWorldScreen extends Dialog
{
	/**
	 * Constructor.
	 */
	public HelloWorldScreen ()
	{
		// The title across the top of the screen.
		//setTitle( "J4ME" );
		
		// Set the menu text at the bottom of the screen.
		//  "Exit" will appear on the left and when the user presses the phone's
		//  left menu button declineNotify() will be called.  When the user
		//  presses the right menu button acceptNotify() will be called.
		setMenuText( "Exit", null );
		
		// Add a UI component.
		Label lbl = new Label("Hello World!");
		lbl.setHorizontalAlignment( Graphics.HCENTER );
		append( lbl );
	}
	
	/**
	 * Called when the user presses the left menu button to "Exit".
	 */
	public void declineNotify ()
	{
		// Exit the application.
		HelloWorldMidlet.exit();
		
		// Continue processing the event.
		super.declineNotify();
	}
}
```
  1. Copy and paste the following code into `HelloWorldMidlet.java`
```
import javax.microedition.midlet.*;
import org.j4me.ui.*;

/**
 * The entry point for the application.
 */
public class HelloWorldMidlet extends MIDlet
{
	/**
	 * The one and only instance of this class.
	 */
	private static HelloWorldMidlet instance;
	
	/**
	 * Constructs the midlet.  This is called before <code>startApp</code>.
	 */
	public HelloWorldMidlet ()
	{
		instance = this;
	}

	/**
	 * Called when the application is minimized.  For example when their
	 * is an incoming call that is accepted or when the phone's hangup key
	 * is pressed.
	 * 
	 * @see javax.microedition.midlet.MIDlet#pauseApp()
	 */
	protected void pauseApp ()
	{
	}

	/**
	 * Called when the application starts.  Shows the first screen.
	 * 
	 * @see javax.microedition.midlet.MIDlet#startApp()
	 */
	protected void startApp () throws MIDletStateChangeException
	{
		// Initialize the J4ME UI manager.
		UIManager.init( this );
		
		// Change the theme.
		//UIManager.setTheme( new org.j4me.examples.ui.themes.RedTheme() );
		
		// Show the first screen.
		HelloWorldScreen screen = new HelloWorldScreen();
		screen.show();
	}

	/**
	 * Called when the application is exiting.
	 * 
	 * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
	 */
	protected void destroyApp (boolean arg0) throws MIDletStateChangeException
	{
		// Add cleanup code here.
		
		// Exit the application.
		notifyDestroyed();
	}
	
	/**
	 * Programmatically exits the application.
	 */
	public static void exit ()
	{
		try
		{
			instance.destroyApp( true );
		}
		catch (MIDletStateChangeException e)
		{
			// Ignore.
		}
	}
}
```
  1. In Package Explorer click on `HelloWorldMidlet.java`
  1. `Run -> Run As -> Emulated J2ME Midlet`

**Congratulations!  You have written and run your first J2ME application.**