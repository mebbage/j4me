package org.j4me.examples.bluetoothgps;

import java.io.*;
import java.util.*;
import javax.microedition.lcdui.*;
import org.j4me.bluetoothgps.*;
import org.j4me.examples.log.*;
import org.j4me.logging.*;
import org.j4me.ui.*;
import org.j4me.ui.components.*;

/**
 * Shows the current location information.  At the top of the screen is a
 * pedometer showing the total distance traveled and the average speed.  Note
 * this is a poor pedometer because it does not attempt to remove inaccuracies
 * from the location updates.  The remainder of the screen shows the location
 * data as it is returned from the provider including:
 * <ul>
 *  <li>Coordinates
 *  <li>Altitude
 *  <li>Speed
 *  <li>Course
 *  <li>Timestamp
 * </ul>
 */
public class Pedometer
	extends Dialog
	implements LocationListener
{
	/**
	 * The number of yards in a meter.
	 */
	private static final float YARDS_PER_METER = 1.09361329833771f;

	/**
	 * How many seconds between getting new location information.
	 */
	private static final int INTERVAL = 5;
	
	/**
	 * How many seconds to wait for new location information before
	 * giving up.
	 */
	private static final int TIMEOUT = -1;  // Default
	
	/**
	 * The maximum number of seconds ago a location can be for it to
	 * be valid.  We will never get locations older than this.
	 */
	private static final int MAX_AGE = -1;  // Default

	/**
	 * A large font used for section headings.
	 */
	private static final Font LARGE_FONT = Font.getFont( Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE );
	
	/**
	 * The normal font used for data.
	 */
	private static final Font NORMAL_FONT = Font.getFont( Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM );

	/**
	 * The location information for this application.
	 */
	private final LocationModel model;
	
	/**
	 * The current state of the location provider.  This is "out of service",
	 * "temporarily unavailable", or "available".
	 */
	private Label state = new Label();
	
	/**
	 * The total distance traveled in meters.
	 */
	private FieldValue traveled = new FieldValue( "Traveled (ft)" );
	
	/**
	 * The average speed traveled in meters per second.
	 */
	private FieldValue avgSpeed = new FieldValue( "Avg speed (MPH)" );
	
	/**
	 * The current latitude.
	 */
	private FieldValue latitude = new FieldValue( "Latitude" );
	
	/**
	 * The current longitude.
	 */
	private FieldValue longitude = new FieldValue( "Longitude" );
	
	/**
	 * The current accuracy of the latitude and longitude in meters.
	 */
	private FieldValue horizontalAccuracy = new FieldValue( "Horizontal accuracy (ft)" );
	
	/**
	 * The current altitude in meters.
	 */
	private FieldValue altitude = new FieldValue( "Altitude (ft)" );
	
	/**
	 * The current accuracy of the altitude in meters.
	 */
	private FieldValue verticalAccuracy = new FieldValue( "Vertical accuracy (ft)" );
	
	/**
	 * The current speed in meters per second.
	 */
	private FieldValue speed = new FieldValue( "Speed (MPH)" );
	
	/**
	 * The current compass bearing in degrees where 0.0 is true north.
	 */
	private FieldValue course = new FieldValue( "Course (deg)" );
	
	/**
	 * The time of the last location.
	 */
	private FieldValue time = new FieldValue( "Timestamp" );
	
	/**
	 * The total distance traveled in meters.
	 */
	private float totalDistance;
	
	/**
	 * The time when the first distance was recorded.  Dividing this into
	 * <code>totalDistance</code> gives the average speed.
	 */
	private long startTime;
	
	/**
	 * The last coordinates recorded.
	 */
	private QualifiedCoordinates lastCoordinates;

	/**
	 * Constructs the "Pedometer" screen.
	 * 
	 * @param model is the application's location data.
	 */
	public Pedometer (LocationModel model)
	{
		this.model = model;
		
		// Set the menu bar options.
		setMenuText( null, "Menu" );
		
		// Show the state of the location provider.
		state.setHorizontalAlignment( Graphics.HCENTER );
		setStateLabel( model.getLocationProvider().getState() );
		append( state );
		
		// Create a UI section for pedometer information.
		createNewSection( "Pedometer" );
		append( traveled );
		append( avgSpeed );
		
		// Create a UI section for location information.
		createNewSection( "Location" );
		append( latitude );
		append( longitude );
		append( horizontalAccuracy );
		append( new Label() );  // Blank line
		append( altitude );
		append( verticalAccuracy );
		
		// Create a section for movement information.
		createNewSection( "Movement" );
		append( speed );
		append( course );
		
		// Create a section for the time.
		createNewSection( "Time" );
		append( time );
		
		// Register for location updates.
		LocationProvider provider = model.getLocationProvider();
		provider.setLocationListener( this, INTERVAL, TIMEOUT, MAX_AGE );
	}

	/**
	 * Adds components for a new section of information.
	 * 
	 * @param title is the name of the section.
	 */
	private void createNewSection (String title)
	{
		append( new HorizontalRule() );
		
		Label header = new Label();
		header.setFont( LARGE_FONT );
		header.setLabel( title );
		append( header );
	}
	
	/**
	 * Called when the user presses the "Menu" menu option.
	 * 
	 * @see org.j4me.ui.DeviceScreen#acceptNotify()
	 */
	protected void acceptNotify ()
	{
		Menu menu = new Menu( "Menu", this );
		
		// Choose different location provider criteria.
		menu.appendMenuOption( new CriteriaSelectionScreen(model) );
		
		// Reset the current location provider.
		menu.appendMenuOption( new MenuItem()
			{
				public String getText ()
				{
					return "Reset Location Provider";
				}

				public void onSelection ()
				{
					// Reset the location provider.
					try
					{
						model.getLocationProvider().reset();
					}
					catch (IOException e)
					{
						Log.warn("Could not reset the location provider", e);
					}
					
					show();
				}
			} );
		
		// See the application's log.
		menu.appendMenuOption( new LogScreen(this) );
		
		menu.show();
		
		// Continue processing the event.
		super.acceptNotify();
	}

	/**
	 * Updates the screen whenever a new location is updated.
	 * <p>
	 * This gets called on a different thread from the main UI thread.
	 * 
	 * @see org.j4me.bluetoothgps.LocationListener#locationUpdated(org.j4me.bluetoothgps.LocationProvider, org.j4me.bluetoothgps.Location)
	 */
	public void locationUpdated (LocationProvider provider, Location location)
	{
		// Throw out invalid location updates.
		if ( location.isValid() )
		{
			// Update the pedometer data.
			QualifiedCoordinates coordinates = location.getQualifiedCoordinates();
			
			if ( lastCoordinates == null )
			{
				// Just starting.
				lastCoordinates = coordinates;
				startTime = System.currentTimeMillis();
			}
			else
			{
				// Record another position.
				totalDistance += lastCoordinates.distance( coordinates );
				float averageSpeed = totalDistance / (System.currentTimeMillis() - startTime) * 1000;
				lastCoordinates = coordinates;

				float distance = convertMetersToFeet( totalDistance );
				traveled.setLabel( distance );
				
				averageSpeed = convertMPStoMPH( averageSpeed );
				avgSpeed.setLabel( averageSpeed );
			}
			
			// Update the location data.
			double lat = coordinates.getLatitude();
			latitude.setLabel( lat );
			
			double lon = coordinates.getLongitude();
			longitude.setLabel( lon );
			
			float ha = coordinates.getHorizontalAccuracy();
			ha = convertMetersToFeet( ha );
			horizontalAccuracy.setLabel( ha );
			
			float alt = coordinates.getAltitude();
			alt = convertMetersToFeet( alt );
			altitude.setLabel( alt );
			
			float va = coordinates.getVerticalAccuracy();
			va = convertMetersToFeet( va );
			verticalAccuracy.setLabel( va );
			
			float s = location.getSpeed();
			s = convertMPStoMPH( s );
			speed.setLabel( s );
			
			float c = location.getCourse();
			course.setLabel( c );
			
			long t = location.getTimestamp();
			time.setLabel( t );
			
			// Update this screen.
			repaint();
		}
	}

	/**
	 * Updates the screen whenever the location provider changes state.
	 * <p>
	 * This gets called on a different thread from the main UI thread.
	 * 
	 * @see org.j4me.bluetoothgps.LocationListener#providerStateChanged(org.j4me.bluetoothgps.LocationProvider, int)
	 */
	public void providerStateChanged (LocationProvider provider, int newState)
	{
		// Display the state.
		setStateLabel( newState );
		repaint();
	}
	
	/**
	 * Sets the <code>state</code> label with the location provider's state.
	 * 
	 * @param newState is the location provider's state.
	 */
	private void setStateLabel (int newState)
	{
		switch ( newState )
		{
		case LocationProvider.AVAILABLE:
			state.setLabel("Available");
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			state.setLabel("Temporarily unavailable");
			break;
		case LocationProvider.OUT_OF_SERVICE:
			state.setLabel("Out of service");
			break;
		}
	}
	
	/**
	 * Shows a field and its value such as "Speed (m/s):  5.0".
	 */
	private static final class FieldValue
		extends Label
	{
		private final String name;
		
		public FieldValue (String name)
		{
			this.name = name;
			
			setFont( NORMAL_FONT );
		}
		
		public void setLabel (String label)
		{
			super.setLabel( name + ":  " + label );
		}
		
		public void setLabel (double d)
		{
			String s = Double.toString( d );
			setLabel( s );
		}
		
		public void setLabel (float f)
		{
			String s = Float.toString( f );
			setLabel( s );
		}
		
		public void setLabel (long l)
		{
			Date d = new Date( l );
			String s = d.toString();
			setLabel( s );
		}
	}
	
	/**
	 * Converts between meters and feet.
	 * 
	 * @param meters is the number of meters.
	 * @return The number of feet in <code>meters</code>.
	 */
	public static float convertMetersToFeet (float meters)
	{
		float yards = meters * YARDS_PER_METER;
		float feet = yards * 3.0f;
		return feet;
	}

	/**
	 * Converts meters per second to miles per hour.
	 * 
	 * @param metersPerSecond is a speed in meters/second.
	 * @return MPH.
	 */
	public static float convertMPStoMPH (float metersPerSecond)
	{
		float feetPerSecond = convertMetersToFeet( metersPerSecond );
		float feetPerHour = feetPerSecond * 3600;
		float milesPerHour = feetPerHour / 5280;
		return milesPerHour;
	}
}
