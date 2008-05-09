package org.j4me.examples.bluetoothgps;

import java.io.*;
import org.j4me.bluetoothgps.*;
import org.j4me.examples.ui.screens.*;
import org.j4me.logging.*;
import org.j4me.ui.*;

/**
 * The "Finding GPS Devices..." alert screen.  This checks for nearby
 * Bluetooth devices.
 * <p>
 * This screen is shown to the user while the application uses Bluetooth dynamic
 * discovery to create a list of nearby devices.  It disappears to give them
 * a selection once it is done.  This usually takes a few seconds, but
 * sometimes as many as 30.
 */
public class FindingGPSDevicesAlert
	extends ProgressAlert
{
	/**
	 * The location information for this application.
	 */
	private final LocationModel model;
	
	/**
	 * An object to use for locking the Bluetooth discovery process.  Only one can
	 * discovery process can run at a time.  So if this screen is shown
	 * it starts, then if the user goes back and screen and comes back to this one,
	 * a second thread will start.  This <code>bluetoothLock</code> prevents the
	 * second thread from doing work until the first completes.  This is not the
	 * most efficient way of doing things because the first thread's results will
	 * be discarded, but it is easy logic and only requires the user wait a few more
	 * seconds and should almost never happen.
	 * <p>
	 * This lock is declared <c>protected static</c> because it is shared throughout
	 * the package.
	 */
	protected static Object bluetoothLock = new Object();
	
	/**
	 * The screen that came before this one.  If the user cancels this
	 * alert, it will go back to this screen.
	 */
	private final DeviceScreen previous;
	
	/**
	 * Constructs the "Finding GPS Devices..." alert screen.
	 * 
	 * @param model is the application's location data.
	 * @param previous is the screen that came before this one.
	 */
	public FindingGPSDevicesAlert (LocationModel model, DeviceScreen previous)
	{
		super( "Finding GPS...", "Looking for nearby Bluetooth devices." );

		this.model = model;
		this.previous = previous;
	}

	/**
	 * Called when the user presses the alert's dismiss button.
	 */
	public void onCancel ()
	{
		Log.info( "Canceling Bluetooth device discovery." );
		previous.show();
	}
	
	/**
	 * A thread that finds nearby Bluetooth devices and sets them on a select
	 * GPS device screen.
	 */
	protected DeviceScreen doWork ()
	{
		DeviceScreen next;
		String[][] devices = null;
		String errorText = null;
		
		synchronized ( bluetoothLock )
		{
			// Stop any providers in case they were using Bluetooth and therefore have
			// a lock on the Bluetooth socket.
			LocationProvider provider = model.getLocationProvider();
			
			if ( provider != null )
			{
				provider.close();
			}
			
			// Search for Bluetooth devices (this takes several seconds).
			try
			{
				Log.info( "Discovering Bluetooth devices." );
				
				devices = LocationProvider.discoverBluetoothDevices();
					
				if ( devices == null )
				{
					// The operation failed for an unknown Bluetooth reason.
					Log.error( "Problem with Bluetooh device discovery.  Operation returned null." );
					errorText = "Bluetooth GPS device discovery failed.";
				}
			}
			catch (SecurityException e)
			{
				// The user prevented communication with the server.
				// Inform them to allow it.
				Log.error( "User denied Bluetooth access.", e );
				errorText = "You must allow access for the GPS to work.\nPlease restart and allow all connections.";
			}
			catch (IOException e)
			{
				// There was an unknown I/O error preventing us from going farther.
				Log.error( "Problem with Bluetooth device discovery.", e );
				errorText = 
					"Bluetooth GPS device discovery failed.\n" +
					"Exit the application and verify your phone's Bluetooth is on.  " +
					"If it is please restart your phone and GPS device and try again.";
			}
			
			// Go to the next screen.
			if ( errorText != null )
			{
				// Inform the user why device discovery failed.
				next = new ErrorAlert(
						"Discovery Error",
						errorText,
						previous );
			}
			else
			{
				// Successful device discovery.
				Log.info( "Found list of " + devices.length + " available devices and presenting them to the user." );
				
				SelectGPSScreen selectGPS = new SelectGPSScreen( model, previous );
				selectGPS.setAvailableDevices( devices );
				
				if ( devices.length == 0 )
				{
					// No devices were found.
					String message =
						"No devices were found.\n" +
						"Make sure your Bluetooth GPS device is on and within 10 feet of you.";
						
					next = new ErrorAlert(
							"Discovery Error",
							message,
							selectGPS );
				}
				else
				{
					// Let the user select from the devices found.
					next = selectGPS;
				}
			}
			
			return next;
		}
	}
}
