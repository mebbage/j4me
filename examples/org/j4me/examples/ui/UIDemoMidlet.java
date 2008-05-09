package org.j4me.examples.ui;

import javax.microedition.midlet.*;
import org.j4me.examples.ui.components.*;
import org.j4me.examples.ui.themes.*;
import org.j4me.ui.*;

/**
 * The demonstration MIDlet for the J4ME UI.
 */
public class UIDemoMidlet
	extends MIDlet
{
	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#startApp()
	 */
	protected void startApp () throws MIDletStateChangeException
	{
		// Initialize the J4ME UI manager.
		UIManager.init( this );
		
		// The theme is the default represented <code>Theme</code> class.
		// To change it, create a new <code>Theme</code>-derived object and call
		// <code>UIManager.setTheme</code>.
		
		// The first screen is a menu to choose among the example screens.
		Menu menu = new Menu( "UI Examples", null );
		
		// Create a submenu for showing component example screens.
		Menu componentExampleMenu = new Menu( "Component Examples", menu );
		menu.appendSubmenu( componentExampleMenu );
		
		LabelExample labelExample = new LabelExample( componentExampleMenu );
		componentExampleMenu.appendMenuOption( labelExample );
		
		ProgressBarExample progressBarExample = new ProgressBarExample( componentExampleMenu );
		componentExampleMenu.appendMenuOption( progressBarExample );
		
		TextBoxExample textBoxExample = new TextBoxExample( componentExampleMenu );
		componentExampleMenu.appendMenuOption( textBoxExample );
		
		RadioButtonExample radioButtonExample = new RadioButtonExample( componentExampleMenu );
		componentExampleMenu.appendMenuOption( radioButtonExample );
		
		CheckBoxExample checkBoxExample = new CheckBoxExample( componentExampleMenu );
		componentExampleMenu.appendMenuOption( checkBoxExample );
		
		ScrollbarExample scrollbarExample = new ScrollbarExample( componentExampleMenu );
		componentExampleMenu.appendMenuOption( scrollbarExample );
		
		PictureExample pictureExample = new PictureExample( componentExampleMenu );
		componentExampleMenu.appendMenuOption( pictureExample );
		
		// Create a submenu showing different example themes.
		Menu themesMenu = new Menu( "Themes", menu );
		menu.appendSubmenu( themesMenu );
		
		ThemeMenuItem defaultTheme = new ThemeMenuItem( "Blue (Default)", new Theme() );
		themesMenu.appendMenuOption( defaultTheme );
		
		ThemeMenuItem greenTheme = new ThemeMenuItem( "Green", new GreenTheme() );
		themesMenu.appendMenuOption( greenTheme );
		
		ThemeMenuItem redTheme = new ThemeMenuItem( "Red", new RedTheme() );
		themesMenu.appendMenuOption( redTheme );
		
		ThemeMenuItem consoleTheme = new ThemeMenuItem( "Console", new ConsoleTheme() );
		themesMenu.appendMenuOption( consoleTheme );
		
		// Attach the examples.
		KeyCode keyCode = new KeyCode( menu );
		menu.appendMenuOption( keyCode );
		
		EtchASketch etchAsketch = new EtchASketch( menu );
		menu.appendMenuOption( etchAsketch );
		
		Stopwatch stopwatch = new Stopwatch( menu );
		menu.appendMenuOption( stopwatch );
		
		// Attach an exit option.
		menu.appendMenuOption( new MenuItem()
				{
					public String getText ()
					{
						return "Exit";
					}

					public void onSelection ()
					{
						UIDemoMidlet.this.notifyDestroyed();
					}
				} );
		
		// Show the menu.
		menu.show();
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
		// The application holds no resources that need cleanup.
	}
}

/**
 * Options available from a menu that change the application's theme.
 */
class ThemeMenuItem
	implements MenuItem
{
	private final String name;
	private final Theme theme;
	
	public ThemeMenuItem (String name, Theme theme)
	{
		this.name = name;
		this.theme = theme;
	}

	public String getText ()
	{
		// The name as it appears in the menu.
		return name;
	}

	public void onSelection ()
	{
		// Applies a theme to the example midlet.
		UIManager.setTheme( theme );
		
		// Repaint the screen so the changes take effect.
		UIManager.getScreen().repaint();
	}
}
