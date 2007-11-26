package org.j4me.bluetoothgps;

import org.j4me.logging.*;

/**
 * A <code>LocationProvider</code> for testing MIDlets.  Methods on this class
 * can set locations manually.
 * <p>
 * Make sure the <code>MockLocationProvider</code> is not referenced in
 * anything but your test code.  Then the obfuscator will remove this class
 * from the final Jar.
 * 
 * @see LocationProvider
 */
public class MockLocationProvider
	extends LocationProvider
{
	/**
	 * The state this mock provider is in.
	 */
	private int state = TEMPORARILY_UNAVAILABLE;
	
	/**
	 * The object listening to location events.  If none are set, this
	 * will be <code>null</code>.
	 */
	private LocationListener listener;
	
	/**
	 * The last location set by <code>setLocation</code>
	 */
	private Location location;
	
	/**
	 * Constructs a mock location provider.
	 */
	public MockLocationProvider ()
	{
	}
	
	/**
	 * Sets the state of the mock location provider.  If a listener has
	 * been registered with the <code>setLocationListener</code> method, it
	 * will get notified of the state change.  If <code>state</code> is the
	 * same as <code>getState</code> this method has no effect.
	 * <p>
	 * The initial provider state is <code>OUT_OF_SERVICE</code>.  If
	 * <code>setLocation</code> is called it will automatically make the state
	 * <code>AVAILABLE</code> and call the registered listener's
	 * <code>providerStateChanged</code> method before calling its
	 * <code>locationUpdated<code>.
	 * 
	 * @param state is the new provider state.  It must be one of
	 *  <code>OUT_OF_SERVICE</code>, <code>TEMPORARILY_UNAVAILABLE</code>, or
	 *  <code>AVAILABLE</code>.
	 */
	public void setState (int state)
	{
		// Make sure the state is valid.
		if ( (state != OUT_OF_SERVICE) &&
			 (state != TEMPORARILY_UNAVAILABLE) &&
			 (state != AVAILABLE) )
		{
			throw new IllegalArgumentException("state was invalid.  It must be OUT_OF_SERVICE, TEMPORARILY_UNAVAILABLE, or AVAILABLE.");
		}
		
		// Did the state change?
		if ( this.state != state )
		{
			// Record the new state.
			this.state = state;
			
			// Inform the listener.
			if ( listener != null )
			{
				try
				{
					listener.providerStateChanged( this, state );
				}
				catch (Throwable t)
				{
					// This is a programming error in the user's application.
					Log.warn("Unhandled exception in LocationProvider.providerStateChanged to " + state, t);
				}
			}
		}
	}
	
	/**
	 * Sets a new location.  If a listener has been registered with
	 * the <code>setLocationListener</code> method, it will get notified of the
	 * <code>locationUpdated</code> method.
	 * 
	 * @param coordinates is the new location.
	 * @param speed is the new speed in meters per second.
	 */
	public void setLocation (QualifiedCoordinates coordinates, float speed)
	{
		if ( coordinates == null )
		{
			throw new IllegalArgumentException("coordinates cannot be null.");
		}
		
		// The location provider will now be available.
		setState( AVAILABLE );
		
		// Inform the listener.
		if ( listener != null )
		{
			location = new LocationImpl( coordinates, speed, Float.NaN, System.currentTimeMillis() );
			
			try
			{
				listener.locationUpdated( this, location );
			}
			catch (Throwable t)
			{
				// This is a programming error in the user's application.
				Log.warn("Unhandled exception in LocationProvider.locationUpdated\n" + location, t);
			}
		}
	}
	
	/**
	 * Returns the state of this mock provider.  The state is initially
	 * <code>OUT_OF_SERVICE</code>, but can be set using the <code>setState</code>
	 * method.  After the <code>setLocation</code> method is called this
	 * automatically becomes <code>AVAILABLE</code>
	 * 
	 * @see org.j4me.bluetoothgps.LocationProvider#getState()
	 */
	public int getState ()
	{
		return state;
	}

	/**
	 * Gets the last location set.
	 * 
	 * @see org.j4me.bluetoothgps.LocationProvider#getLocation(int)
	 */
	public Location getLocation (int timeout)
		throws LocationException, InterruptedException
	{
		if ( state == OUT_OF_SERVICE )
		{
			throw new LocationException("Mock provider is out of service");
		}
		
		if ( location == null )
		{
			throw new LocationException("Mock provider has not had location set yet");
		}
		
		return location;
	}
	
	/**
	 * Returns the last known location by the provider.
	 * 
	 * @return a location object. <code>null</code> is returned if the implementation
	 *  doesn't have any previous location information.
	 * @throws SecurityException - if the calling application does not have a
	 *  permission to query the location information
	 * 
	 * @see LocationProvider#getLastKnownLocationToProvider()
	 */
	protected Location getLastKnownLocationToProvider ()
	{
		return location;
	}

	/**
	 * Sets the listener that gets called when the <code>setLocation</code>
	 * and <code>setState</code> methods are called.
	 * 
	 * @see org.j4me.bluetoothgps.LocationProvider#setLocationListener(org.j4me.bluetoothgps.LocationListener, int, int, int)
	 */
	public void setLocationListener (LocationListener locationlistener, int interval, int timeout, int maxAge)
	{
		this.listener = locationlistener;
	}

	/**
	 * @see org.j4me.bluetoothgps.LocationProvider#reset()
	 */
	public void reset ()
	{
		setState( TEMPORARILY_UNAVAILABLE );
		setState( AVAILABLE );
	}

	/**
	 * This is a no-op for the mock location provider.
	 * 
	 * @see org.j4me.bluetoothgps.LocationProvider#close()
	 */
	public void close ()
	{
		setState( OUT_OF_SERVICE );
		location = null;
	}
}
