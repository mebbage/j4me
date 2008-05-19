package org.j4me.bluetoothgps;

import org.j4me.collections.*;
import org.j4me.logging.*;

/**
 * Wraps all <code>LocationProvider</code>s returned by the JSR 179 implementation.
 * <p>
 * This class is <i>only</i> used on devices that have JSR 179 on them.  Therefore
 * it can make calls into the <code>javax.microedition.location</code> classes since those
 * files are guaranteed to exist on the device.  Conversely, this file always exists
 * in the J4ME implementation so <code>org.j4me.bluetoothgps.LocationProvider</code> can
 * always reference it.  It can only use it, however, if the JSR 179 implementation
 * is available otherwise the JVM will throw errors.
 */
class JSR179LocationProvider
	extends org.j4me.bluetoothgps.LocationProvider
	implements Runnable
{
	/**
	 * A copy of the actual location provider object
	 */
	private final javax.microedition.location.LocationProvider original;

	/**
	 * Stores the latest location provider update.  It is put there by the JSR 179
	 * implementation.  This class's background thread takes it and passes it off
	 * to <code>locationListener</code>.
	 * <p>
	 * The object stored in the cubby hole will either be an <code>Integer</code>
	 * or a <code>javax.microedition.location.Location</code>.  When the JSR 179
	 * provider notifies a state change to unavailable it will be the <code>Integer</code>
	 * containing the new state code as defined by
	 * <code>javax.microedition.location.LocationProvider</code>.  Otherwise, if
	 * the JSR 179 provider is in service, it will be the last
	 * <code>javax.microedition.location.Location</code> update.
	 * <p>
	 * Some JSR 179 implementations give location events on the main UI thread.
	 * This is dangerous because they can cause the application to become unresponsive
	 * or even crash unless they are immediately handled.  By shuffling the events
	 * to our own background thread the user is free to implement long running
	 * operations.
	 */
	private final CubbyHole update = new CubbyHole();
	
	/**
	 * The application's object registered to listen to location updates.
	 * This can be <code>null</code> meaning the application isn't receiving
	 * events. 
	 */
	private org.j4me.bluetoothgps.LocationListener locationListener;
	
	/**
	 * The worker thread used to raise location events to <code>locationListener</code>.
	 * This thread will only exist so long as <code>locationListener</code> is not
	 * <code>null</code>.
	 */
	private final Thread worker = new Thread( this );
	
	/**
	 * A flag indicating if this provider has ever been in the <code>AVAILABLE</code>
	 * state or not.  If it has, then it becomes <code>TEMPORARILY_UNAVAILABLE</code>
	 * again, we know to reset the provider to try to get a fix again.
	 */
	private boolean hasBeenAvailable = false;
	
	/**
	 * The last known state of the location provider.
	 */
	private int lastState = javax.microedition.location.LocationProvider.TEMPORARILY_UNAVAILABLE;

	/**
	 * Records the location update interval set by the user.
	 */
	private int interval;

	/**
	 * Records the location update timeout set by the user.
	 */
	private int timeout;

	/**
	 * Records the location update maximum age set by the user.
	 */
	private int maxAge;
	
	/**
	 * Returns a JSR 179 <code>LocationProvider</code> wrapped by an object of this
	 * class.
	 */
	public static org.j4me.bluetoothgps.LocationProvider getInstance (org.j4me.bluetoothgps.Criteria criteria)
		throws org.j4me.bluetoothgps.LocationException
	{
		try
		{
			// Change the J4ME Criteria object into a JSR 179 Criteria object.
			javax.microedition.location.Criteria c = convertCriteria( criteria );

			// Call the JSR 179 implementation to see if it has any providers.
			javax.microedition.location.LocationProvider jsr179provider = javax.microedition.location.LocationProvider.getInstance( c );

			if ( jsr179provider != null )
			{
				return new JSR179LocationProvider( jsr179provider );
			}
		}
		catch (javax.microedition.location.LocationException e)
		{
			throw convertLocationException( e );
		}

		// If we made it here, there are no JSR 179 providers that match the criteria.
		return null;
	}

	/**
	 * Create an instance of the actual location provider.
	 */
	private JSR179LocationProvider (javax.microedition.location.LocationProvider provider)
	{
		this.original = provider;
	}

	/**
	 * @see org.j4me.bluetoothgps.LocationProvider#getState()
	 */
	public int getState () 
	{
		// Map the JSR 179 status to one of ours
		int status = original.getState();
		status = convertAvailabilityStatusCode( lastState );
		return status;
	}

	/**
	 * @see org.j4me.bluetoothgps.LocationProvider#getLocation(int)
	 */
	public Location getLocation (int timeout)
		throws LocationException, InterruptedException
	{
		try
		{
			javax.microedition.location.Location location = original.getLocation( timeout );
			org.j4me.bluetoothgps.Location j4me = convertLocation( location );
			return j4me;
		}
		catch (javax.microedition.location.LocationException e)
		{
			throw convertLocationException( e );
		}
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
		javax.microedition.location.Location location = javax.microedition.location.LocationProvider.getLastKnownLocation();
		org.j4me.bluetoothgps.Location j4me = convertLocation( location );
		return j4me;
	}

	/**
	 * @see org.j4me.bluetoothgps.LocationProvider#setLocationListener(org.j4me.bluetoothgps.LocationListener, int, int, int)
	 */
	public void setLocationListener (org.j4me.bluetoothgps.LocationListener locationListener, int interval, int timeout, int maxAge)
	{
		this.locationListener = locationListener; 
		this.interval = interval;
		this.timeout = timeout;
		this.maxAge = maxAge;
		
		// Is the provider working yet?
		if ( original.getState() == javax.microedition.location.LocationProvider.AVAILABLE )
		{
			hasBeenAvailable = true;
		}
		
		// Kill our worker thread to start fresh.
		try
		{
			if ( worker.isAlive() )
			{
				worker.interrupt();
				worker.join();
			}
		}
		catch (Exception e)
		{
			// Ignore.  The worker is dead now.
		}
		
		// Set the new location listner for the JSR 179 implementation
		if ( locationListener == null )
		{
			// Nothing listening for events.
			original.setLocationListener( null, interval, timeout, maxAge );
		}
		else
		{
			// Start listening for events.
			lastState = TEMPORARILY_UNAVAILABLE;
			original.setLocationListener( new JSR179Listener(), interval, timeout, maxAge );

			// Start notifying the user's location listener with a new worker thread.
			worker.start();
		}
	}

	/**
	 * @see org.j4me.bluetoothgps.LocationProvider#reset()
	 */
	public void reset ()
	{
		original.reset();
		
		// Register a new location listener.
		//  Some implementation, like the BlackBerry, will not actually reset until
		//  the location listener is changed.
		if ( locationListener != null )
		{
			original.setLocationListener( new JSR179Listener(), interval, timeout, maxAge );
		}
	}

	/**
	 * @see org.j4me.bluetoothgps.LocationProvider#close()
	 */
	public void close ()
	{
		reset();
		setLocationListener( null, -1, -1, -1 );
		
		try
		{
			if ( worker.isAlive() )
			{
				worker.interrupt();
				worker.join();
			}
		}
		catch (Exception e)
		{
			// Ignore.  The worker is dead now.
		}
	}
	
	/**
	 * Listens to the JSR 179 implementation for location information.
	 * It then hands it off to this class's background thread which
	 * forwards the event to the application's location listener.
	 * 
	 * @see JSR179LocationProvider#update
	 */
	private final class JSR179Listener
		implements javax.microedition.location.LocationListener
	{
		/**
		 * Called whenever the location provider changes state.
		 * 
		 * @see javax.microedition.location.LocationListener#providerStateChanged(javax.microedition.location.LocationProvider, int)
		 */
		public void providerStateChanged (javax.microedition.location.LocationProvider provider, int newState)
		{
			if ( newState != javax.microedition.location.LocationProvider.AVAILABLE )
			{
				// Notify that the provider is unavailable.
				Integer state = new Integer( newState );
				update.set( state );
			}
			
			// Ignore AVAILABLE updates.  We will signal them along with the latest
			// location update.
		}
		
		/**
		 * Called at scheduled intervals with the latest location.
		 * 
		 * @see javax.microedition.location.LocationListener#locationUpdated(javax.microedition.location.LocationProvider, javax.microedition.location.Location)
		 */
		public void locationUpdated (javax.microedition.location.LocationProvider provider, javax.microedition.location.Location location)
		{
			// The provider has been available at one point in time.
			hasBeenAvailable = true;
			
			// Put the latest location information into a cubby hole
			// for JSR179LocationProvider.run() method to forward.  If
			// an older location event is still in the cubby hole it will
			// be replaced by this one.
			update.set( location );
		}
	}

	/**
	 * A background thread that posts location events to the registered
	 * <code>LocationListener</code>.  This keeps the main UI thread free
	 * for JSR 179 implementations that use it, like the BlackBerry.
	 * Implementations therefore can take their time handling the events
	 * without causing the application to become unresponsive or crash.
	 * 
	 * @see Runnable#run()
	 */
	public void run ()
	{
		try
		{
			while ( true )
			{
				// Block until a new event has been raised.
				Object o = update.get();
				
				// Check what kind of update it is.
				if ( o instanceof Integer )
				{
					// The provider is now unavailable.
					Integer i = (Integer)o;
					int newState = i.intValue();
					
					// Forward the state change event to the user's location listener.
					raiseStateChangeEvent( newState );
					
					// Should we reset the provider?
					//  BlackBerry implementations actually become completely unavailable when they
					//  say they are temporarily unavailable.  It is actually a signal saying the
					//  user must try resetting the provider later because it may become available
					//  again.  (To a BlackBerry "Out of Service" means the device does not have LBS.)
					//  This is covered in more detail in the BlackBerry Knowledge Base article at
					//  http://www.blackberry.com/knowledgecenterpublic/livelink.exe/fetch/2000/348583/800332/800703/How_To_-_Detect_when_GPS_is_no_longer_available_and_when_to_reset_the_LocationProvider.html?nodeid=1357467&vernum=0
					if ( hasBeenAvailable && (newState == javax.microedition.location.LocationProvider.TEMPORARILY_UNAVAILABLE) )
					{
						// Pause for a bit so we don't immediately try to get a location.
						//  Since this is happening on our one and only worker thread any events
						//  that may occur by the JSR 179 implementation, such as going back in
						//  service, will still happen.  They will not block either.  This just
						//  means the user's location implementation will not get updated for
						//  at least this long.
						Thread.sleep( interval * 1000 );
						
						// Make sure the provider is still unavailable after the sleep.
						if ( original.getState() == javax.microedition.location.LocationProvider.TEMPORARILY_UNAVAILABLE )
						{
							Log.info("Resetting the location provider to get another fix");
							
							// Reset the location provider so it tries to get another fix.
							reset();
						}
					}
				}
				else // ( o instanceof javax.microedition.location.Location )
				{
					// A new location event has been received;
					javax.microedition.location.Location l = (javax.microedition.location.Location)o;
					
					// Was the provider unavailable and is now available?
					if ( (lastState != javax.microedition.location.LocationProvider.AVAILABLE) && l.isValid() )
					{
						// Now the provider is available again.
						raiseStateChangeEvent( javax.microedition.location.LocationProvider.AVAILABLE );
					}
					
					// Forward the location event to the user's location listener.
					raiseLocationEvent( l );
				}
			}
		}
		catch (InterruptedException e)
		{
			// The application is exiting.
		}
	}
	
	/**
	 * Call when the location provider gives us a new state.
	 * 
	 * @param newState is the <code>javax.microedition.location.LocationProvider</code>
	 *  state code.
	 */
	private void raiseStateChangeEvent (int newState)
	{
		// Record the unavailable state.
		lastState = newState;
		
		// Forward to the application's listener.
		if ( locationListener != null )
		{
			int state = convertAvailabilityStatusCode( newState );
			
			try
			{
				locationListener.providerStateChanged( this, state );
			}
			catch (Throwable t)
			{
				// This is a programming error in the user's application.
				Log.warn("Unhandled exception in LocationProvider.providerStateChanged to " + state, t);
			}
		}
	}
	
	/**
	 * Call when the location provider gives us a new location.
	 * 
	 * @param location is the new location.
	 */
	private void raiseLocationEvent (javax.microedition.location.Location location)
	{
		// Forward to the application's listener.
		if ( locationListener != null )
		{
			org.j4me.bluetoothgps.Location l = convertLocation( location );
			
			try
			{
				locationListener.locationUpdated( this, l );
			}
			catch (Throwable t)
			{
				// This is a programming error in the user's application.
				Log.warn("Unhandled exception in LocationProvider.locationUpdated\n" + l, t);
			}
		}
	}
	
	/**
	 * Converts a JSR 179 <code>LocationException</code> object into a J4ME one.
	 * 
	 * @param jsr179 is the object to convert.
	 * @return The J4ME version of the object.
	 */
	protected static org.j4me.bluetoothgps.LocationException convertLocationException (javax.microedition.location.LocationException jsr179)
	{
		org.j4me.bluetoothgps.LocationException j4me = new org.j4me.bluetoothgps.LocationException( jsr179.getMessage() );
		return j4me;
	}

	/**
	 * Converts a J4ME <code>Criteria</code> object into a JSR 179 one.
	 * 
	 * @param j4me is the object to convert.
	 * @return The JSR 179 version of the object.
	 */
	protected static javax.microedition.location.Criteria convertCriteria (org.j4me.bluetoothgps.Criteria j4me)
	{
		javax.microedition.location.Criteria jsr179 = new javax.microedition.location.Criteria();

		jsr179.setAddressInfoRequired( j4me.isAddressInfoRequired() );
		jsr179.setAltitudeRequired( j4me.isAltitudeRequired() );
		jsr179.setCostAllowed( j4me.isAllowedToCost() );
		jsr179.setHorizontalAccuracy( j4me.getHorizontalAccuracy() );
		jsr179.setPreferredResponseTime( j4me.getPreferredResponseTime() );
		jsr179.setSpeedAndCourseRequired( j4me.isSpeedAndCourseRequired() );
		jsr179.setVerticalAccuracy( j4me.getVerticalAccuracy() );

		// Translate the power level constant.
		int power = j4me.getPreferredPowerConsumption();

		switch ( power )
		{
		case org.j4me.bluetoothgps.Criteria.NO_REQUIREMENT:
			power = javax.microedition.location.Criteria.NO_REQUIREMENT;
			break;
		case org.j4me.bluetoothgps.Criteria.POWER_USAGE_LOW:
			power = javax.microedition.location.Criteria.POWER_USAGE_LOW;
			break;
		case org.j4me.bluetoothgps.Criteria.POWER_USAGE_MEDIUM:
			power = javax.microedition.location.Criteria.POWER_USAGE_MEDIUM;
			break;
		case org.j4me.bluetoothgps.Criteria.POWER_USAGE_HIGH:
			power = javax.microedition.location.Criteria.POWER_USAGE_HIGH;
			break;
		}

		jsr179.setPreferredPowerConsumption( power );

		return jsr179;
	}

	/**
	 * Converts a JSR 179 <code>QualifiedCoordinates</code> object into a J4ME one.
	 * 
	 * @param jsr179 is the object to convert.
	 * @return The J4ME version of the object.
	 */
	protected static org.j4me.bluetoothgps.QualifiedCoordinates convertQualifiedCoordinates (javax.microedition.location.QualifiedCoordinates jsr179)
	{
		if ( jsr179 == null )
		{
			return null;
		}
		
		double latitude = jsr179.getLatitude();
		double longitude = jsr179.getLongitude();
		float altitude = jsr179.getAltitude();
		float horizontalAccuracy = jsr179.getHorizontalAccuracy();
		float verticalAccuracy = jsr179.getVerticalAccuracy();

		org.j4me.bluetoothgps.QualifiedCoordinates j4me = new org.j4me.bluetoothgps.QualifiedCoordinates( latitude, longitude, altitude, horizontalAccuracy, verticalAccuracy );
		return j4me;
	}

	/**
	 * Converts a JSR 179 <code>Location</code> object into a J4ME one.
	 * 
	 * @param jsr179 is the object to convert.
	 * @return The J4ME version of the object.
	 */
	protected static org.j4me.bluetoothgps.Location convertLocation (javax.microedition.location.Location jsr179)
	{
		LocationImpl j4me = null;
		
		if ( jsr179 != null )
		{
			if ( jsr179.isValid() )
			{
				org.j4me.bluetoothgps.QualifiedCoordinates j4meCoordinates = convertQualifiedCoordinates( jsr179.getQualifiedCoordinates() );
				j4me = new LocationImpl( j4meCoordinates, jsr179.getSpeed(), jsr179.getCourse(), jsr179.getTimestamp() );
			}
			else
			{
				j4me = new LocationImpl();
			}
		}
		
		return j4me;
	}

	/**
	 * Converts a JSR 179 availability status code constant into a J4ME one.
	 * The status codes are contstants defined on the <code>LocationProvider</code>
	 * classes.
	 * 
	 * @param jsr179 is the availability status code to convert.
	 * @return The J4ME availability status code.
	 */
	protected static int convertAvailabilityStatusCode (int jsr179)
	{
		int j4me = -1;

		switch (jsr179)
		{
		case javax.microedition.location.LocationProvider.AVAILABLE:
			j4me = org.j4me.bluetoothgps.LocationProvider.AVAILABLE;
			break;
		case javax.microedition.location.LocationProvider.OUT_OF_SERVICE:
			j4me = org.j4me.bluetoothgps.LocationProvider.OUT_OF_SERVICE;
			break;
		case javax.microedition.location.LocationProvider.TEMPORARILY_UNAVAILABLE:
			j4me = org.j4me.bluetoothgps.LocationProvider.TEMPORARILY_UNAVAILABLE;
			break;
		}

		return j4me;
	}
}
