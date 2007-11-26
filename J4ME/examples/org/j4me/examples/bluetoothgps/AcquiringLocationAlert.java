package org.j4me.examples.bluetoothgps;

import org.j4me.bluetoothgps.*;
import org.j4me.examples.ui.screens.*;
import org.j4me.logging.*;
import org.j4me.ui.*;

/**
 * The "Acquiring Location..." alert screen.  This is shown while the application
 * is waiting on a GPS fix.
 */
public class AcquiringLocationAlert
	extends ProgressAlert
{
	/**
	 * The location information for this application.
	 */
	private final LocationModel model;
	
	/**
	 * The screen that came before this one.  If the user cancels the
	 * alert it will return to the <c>previous</c> screen.
	 */
	private final DeviceScreen previous;
	
	/**
	 * A flag to indicate if the screen was canceled.
	 */
	private boolean canceled = false;
	
	/**
	 * Constructs the "Acquiring Location..." alert screen.
	 * 
	 * @param model is the application's location data.
	 * @param previous is the screen that came before this one and that
	 *  is returned to if the user cancels.
	 */
	public AcquiringLocationAlert (LocationModel model, DeviceScreen previous)
	{
		// Initialize the alert.
		super( "Acquiring Location...", "The GPS is getting a fix on your location." );
		
		// Append the name of the Bluetooth GPS device to the alert's text.
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
	 * 
	 * @see ProgressAlert#onCancel()
	 */
	public void onCancel ()
	{
		Log.debug("Canceling waiting for GPS fix.");
		
		// Go back to the previous screen.
		previous.show();
	}
	
	/**
	 * Cancels this thread.
	 */
	public void cancel ()
	{
		super.cancel();
		
		// This will cause the thread to exit.
		canceled = true;
	}

	/**
	 * Does background work.  It waits until a GPS fix has been acquired and
	 * then returns the next screen to be displayed. 
	 */
	protected DeviceScreen doWork ()
	{
		QualifiedCoordinates coordinates = null;
		
		// Poll until we get a fix.
		while ( (canceled == false) && (coordinates == null) )
		{
			// Has a fix been acquired?
			Location location = LocationProvider.getLastKnownLocation();
			
			if ( location != null )
			{
				coordinates = location.getQualifiedCoordinates();
				
				if ( coordinates == null )
				{
					// Sleep for a bit to wait for a location.
					try
					{
						Thread.sleep( 50 );
					}
					catch (InterruptedException e)
					{
						// Just continue.  "canceled" will probably be true.
					}
				}
			}
		}
		
		// Go to the next screen.
		Pedometer next = new Pedometer( model );
		return next;
	}
}
