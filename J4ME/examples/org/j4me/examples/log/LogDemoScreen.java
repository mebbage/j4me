package org.j4me.examples.log;

import org.j4me.logging.*;

/**
 * Shows properties about the phone.  On startup it scans the device for
 * its properties and logs them.  Actually showing the logs is the job
 * of the base class <code>LogScreen</code>.
 * <p>
 * For a more complete overview of a phone's specifications use the open
 * source <a href="http://mobiledevtools.sourceforge.net/">
 * Mobile Device Tools</a> application.  For a description of each JSR see
 * the <a href="http://java.sun.com/javame/reference/docs/msa_datasheet.pdf">
 * J2ME Mobile Services Architecture Datasheet</a>.
 */
public class LogDemoScreen
	extends LogScreen
{
	/**
	 * Constructs the demo screen for the log package.
	 */
	public LogDemoScreen ()
	{
		super( null );
		
		// Remove "cancel" button from the menu.
		setMenuText( null, getRightMenuText() );
	}
	
	/**
	 * Populates the log with details about the current device.
	 * 
	 * @see org.j4me.ui.DeviceScreen#showNotify()
	 */
	public void showNotify ()
	{
		Log.error( "System properties" );
		Log.warn( "Screen: " + this.getScreenWidth() + "w " + this.getScreenHeight() + "h" );
		Log.warn( "Total memory: " + Runtime.getRuntime().totalMemory() );
		Log.warn( "Platform: " + System.getProperty("microedition.platform") );
		Log.warn( 
				"Configuration: " + System.getProperty("microedition.configuration") + "\n" +
				"Profiles: " + System.getProperty("microedition.profiles") );
		Log.warn(
				"Locale: " + System.getProperty("microedition.locale") + "\n" +
				"Char encoding: " + System.getProperty("microedition.encoding") + "\n" +
				"i18n (JSR 238): " + hasClass("javax.microedition.global.Formatter") );
		
		Log.error( "Security and Commerce APIs" );
		logJSR( "Payment", 229, "javax.microedition.payment.TransactionRecord" );
		logJSR( "Security and Trust Services", 177, "java.security.Signature" );

		Log.error( "Graphics APIs" );
		logJSR( "Mobile-media Supplement", 234, "javax.microedition.amms.GlobalManager" );
		logJSR( "SVG (Scalable Vector Graphics)", 226, "javax.microedition.m2g.ScalableGraphics" );
		logJSR( "3D Graphics", 184, "javax.microedition.m3g.Node" );
		logJSR( "Mobile Media", 135, "javax.microedition.media.Manager" );
		// Video portion of 135 not always supported is "javax.microedition.media.TimeBase"
		
		Log.error( "Communications" );
		logJSR( "SIP", 180, "javax.microedition.sip.SipConnection" );
		logJSR( "MMS Messaging", 205, "javax.wireless.messaging.MessagePart" );
		logJSR( "Bluetooth", 82, "javax.bluetooth.LocalDevice" );
		// OBEX portion of 82 not always supported is "javax.obex.HeaderSet"
		logJSR( "SMS Messaging", 120, "javax.wireless.messaging.Message" );
		
		Log.error( "Personal warnrmation" );
		logJSR( "Location", 179, "javax.microedition.location.Location" );
		logJSR( "PIM and File", 75, "javax.microedition.pim.PIM" );

		Log.error( "Application Connectivity" );
		logJSR( "Content Handler", 211, "javax.microedition.content.Invocation" );
		logJSR( "Web Services", 172, "javax.xml.parsers.SAXParser" );
		
		// Continue processing the event.
		super.showNotify();
	}
	
	/**
	 * Logs if a JSR is present on the phone or not.
	 * 
	 * @param name is the JSR name.
	 * @param number is the JSR number.
	 * @param clazz is a fully qualified class name of something specific
	 *  to the JSR.
	 */
	private void logJSR (String name, int number, String clazz)
	{
		boolean supports = hasClass( clazz );
		
		if ( supports )
		{
			String message =
				"Supports " +
				name +
				" (JSR " + number + ")";
			Log.warn( message );
		}
		else
		{
			// Protect against expensive string creation when Info-logging is disabled.
			if ( Log.isInfoEnabled() )  
			{
				String message =
					"Does not support " +
					name +
					" (JSR " + number + ")";
				Log.info( message );
			}
		}
	}
	
	/**
	 * Determines if a class is available or not.  This can be used to see
	 * what libraries are running on the system.
	 *
	 * @param name is the fullly qualified name of a class.
	 * @return <code>true</code if the class exists and <code>false</code> if it
	 *  does not.
	 */
	private static boolean hasClass (String name)
	{
		try
		{
			Class.forName( name );
			return true;
		}
		catch (ClassNotFoundException e)
		{
			return false;
		}
	}
}
