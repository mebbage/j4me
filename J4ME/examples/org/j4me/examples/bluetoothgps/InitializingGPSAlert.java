package org.j4me.examples.bluetoothgps;

import java.io.*;
import org.j4me.bluetoothgps.*;
import org.j4me.examples.ui.screens.*;
import org.j4me.logging.*;
import org.j4me.ui.*;

/**
 * The "Initializing GPS..." alert screen.  This screen is used to get the
 * <code>LocationProvider</code> for the application.  It first tries to get a
 * provider on the device.  But if it cannot it will get a GPS provider
 * through a Bluetooth connection.
 */
public class InitializingGPSAlert
	extends ProgressAlert
{
	/**
	 * The location information for this application.
	 */
	private final LocationModel model;
	
	/**
	 * The screen that came before this one.  If the user cancels the
	 * the process or if it fails it will be returned to.
	 */
	private final DeviceScreen previous;
	
	/**
	 * Constructs the "Initializing GPS..." alert screen.
	 * 
	 * @param model is the application's location data.
	 * @param previous is the screen that came before this one.
	 */
	public InitializingGPSAlert (LocationModel model, DeviceScreen previous)
	{
		super( "Initializing GPS...", "Connecting to the location provider." );
		
		// Append the Bluetooth GPS name to the alert's text.
		Boolean onDevice = model.isGPSOnDevice();
		
		if ( (onDevice != null) && (onDevice.booleanValue() == false) )
		{
			String deviceName = model.getBluetoothGPSName();
			
			String text =
				getText() + "\n" +
				"Using device:  " + deviceName;
			setText( text );
		}

		this.model = model;
		this.previous = previous;
	}

	/**
	 * Called when the user presses the alert's dismiss button.
	 */
	public void onCancel ()
	{
		Log.debug("Canceling GPS initialization.");

		// Go back to the previous screen.
		previous.show();
	}

	/**
	 * A worker thread that get the GPS <c>LocationProvider</c>.
	 * The thread will set the next screen when it is done.
	 */
	protected DeviceScreen doWork ()
	{
		LocationProvider provider = null;
		DeviceScreen next = null;
		
		try
		{
			// Get the GPS provider.
			//  Synchronize on our Bluetooth lock in case the user hits the cancel
			//  button and tries to do Bluetooth device discovery to find new GPS.
			synchronized ( FindingGPSDevicesAlert.bluetoothLock )
			{
				// First close any open provider.
				//  For example if connected to one GPS device and are switching to
				//  another.
				LocationProvider old = model.getLocationProvider();
				
				if ( old != null )
				{
					old.close();
				}
				
				// Get the new provider.
				Criteria criteria = model.getCriteria();
				provider = LocationProvider.getInstance( criteria );
			}
			
			// Set the provider on the model.
			//  Note that if we are using on device GPS (i.e. JSR 179) this is
			//  the method that will throw the SecurityException, not the above
			//  getProvider() method (which would throw it for GPS through Bluetooth).
			model.setLocationProvider( provider );
			
			// Did we get a GPS location provider?
			if ( provider != null )
			{
				// Alert the user we are waiting for a fix from the GPS.
				next = new AcquiringLocationAlert( model, previous ); 
			}
			else
			{
				// There was no location provider that matched the criteria.
				Log.info( "No location provider matched the criteria." );
				next = new ErrorAlert( "GPS Error", "No location provider matched the criteria.", previous );
			}
		}
		catch (LocationException e)
		{
			Log.error( "All the location providers are currently out of service.", e );
			next = new ErrorAlert(
					"GPS Error",
					"The GPS is already in use by another application.  Please shut it down and try again.",
					null );
		}
		catch (SecurityException e)
		{
			Log.error( "The user blocked access to the location provider.", e );
			next = new ErrorAlert(
					"GPS Error",
					"You must allow access for the application to work.\nPlease restart and allow all connections.",
					null );
		}
		catch (IOException e)
		{
			Log.error( "An I/O error occured while connecting to the location provider.", e );
			String errorMessage =
					"A problem occurred connecting to the GPS.\n" +
					"Exit the application and verify your phone's Bluetooth is on.  " +
					"If it is please restart your phone and GPS device and try again.";
			next = new ErrorAlert(
					"GPS Error",
					errorMessage,
					null );
		}

		return next;
	}
}
