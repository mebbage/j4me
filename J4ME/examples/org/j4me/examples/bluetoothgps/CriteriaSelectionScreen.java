package org.j4me.examples.bluetoothgps;

import org.j4me.bluetoothgps.*;
import org.j4me.ui.*;
import org.j4me.ui.components.*;

/**
 * A configuration screen that determines the user's criteria for a location
 * provider.
 * <p>
 * Because this is an example application we do not allow for some criteria
 * such as "cost allowed".  The example should not use carrier signals that
 * result in user charges.
 */
public class CriteriaSelectionScreen
	extends Dialog
{
	/**
	 * The location information for this application.
	 */
	private final LocationModel model;
	
	/**
	 * Whether to try getting a location provider build into the phone or
	 * over a remote Bluetooth connection.
	 */
	private final RadioButton source;
	
	/**
	 * The required accuracy of the location data in meters.
	 */
	private final TextBox horizontalAccuracy;
	
	/**
	 * If altitude data is required or not.
	 */
	private final CheckBox altitudeRequired;
	
	/**
	 * If the speed and course data is required or not.
	 */
	private final CheckBox speedAndCourseRequired;
	
	/**
	 * Constructs the "Criteria Selection" screen.
	 * 
	 * @param model is the application's location data.
	 */
	public CriteriaSelectionScreen (LocationModel model)
	{
		this.model = model;
		
		// Setup the screen.
		setTitle( "LBS Criteria" );
		setMenuText( null, "OK" );
		
		source = new RadioButton();
		source.setLabel( "LBS source" );
		source.append( "Device" );
		source.append( "Bluetooth" );
		append( source );
		
		horizontalAccuracy = new TextBox();
		horizontalAccuracy.setLabel( "Horizontal accuracy in meters" );
		horizontalAccuracy.setForNumericOnly();
		append( horizontalAccuracy );
		
		altitudeRequired = new CheckBox();
		altitudeRequired.setLabel( "Altitude required" );
		append( altitudeRequired );

		speedAndCourseRequired = new CheckBox();
		speedAndCourseRequired.setLabel( "Speed and course required" );
		append( speedAndCourseRequired );
		
		// Set the initial values for the UI components.
		Criteria c = model.getCriteria();
		
		if ( c == null )
		{
			// No criteria yet so use defaults.
			source.setSelectedIndex( 0 );
			horizontalAccuracy.setString( "10" );
		}
		else
		{
			if ( c.getRemoteDeviceAddress() == null )
			{
				// LBS on the device.
				source.setSelectedIndex( 0 );
			}
			else
			{
				// LBS through Bluetooth.
				source.setSelectedIndex( 1 );
			}

			horizontalAccuracy.setString( Integer.toString(c.getHorizontalAccuracy()) );
			altitudeRequired.setChecked( c.isAltitudeRequired() );
			speedAndCourseRequired.setChecked( c.isSpeedAndCourseRequired() );
		}
	}

	/**
	 * Called when the user is done setting the location provider criteria.
	 * 
	 * @see org.j4me.ui.DeviceScreen#acceptNotify()
	 */
	protected void acceptNotify ()
	{
		// Create a criteria object and go to the next screen.
		Criteria criteria = new Criteria();
		criteria.setCostAllowed( false );  // Don't want cell phone charges for our demo :)
		criteria.setSpeedAndCourseRequired (speedAndCourseRequired.isChecked() );
		criteria.setAltitudeRequired( altitudeRequired.isChecked() );
		criteria.setHorizontalAccuracy( Integer.parseInt(horizontalAccuracy.getString()) );
		
		// If a Bluetooth GPS location provider is going to be used we need to
		// supply its address to the criteria.  However, we don't know the address
		// yet so we'll do Bluetooth device discovery next to present the user with
		// a list of possible Bluetooth devices that are the GPS.
		DeviceScreen next;
		
		if ( source.getSelectedIndex() == 0 )
		{
			// On device LBS.
			criteria.setAllowLocalLBS( true );
			criteria.setRemoteDeviceAddress( null );
			next = new InitializingGPSAlert( model, this );
		}
		else
		{
			// Bluetooth LBS.
			criteria.setAllowLocalLBS( false );
			next = new FindingGPSDevicesAlert( model, this );
		}

		// Store the criteria used for selecting a location provider.
		model.setCriteria( criteria );

		// Go to the next screen.
		next.show();
		
		// Continue processing the event.
		super.acceptNotify();
	}
}
