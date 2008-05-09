package org.j4me.bluetoothgps;

import java.io.*;
import javax.microedition.io.*;
import org.j4me.logging.*;

/**
 * Maps the communications with a Bluetooth GPS device to the <code>LocationProvider</code>
 * interface.
 */
class BluetoothLocationProvider extends LocationProvider {

	/**
	 * The protocol portion of the URL for Bluetooth addresses.
	 */
	private static final String BLUETOOTH_PROTOCOL = "btspp://";
	
	/**
	 * The Bluetooth connection string options for communicating with a
	 * GPS device.
	 * <ul>
	 *  <li>master is false because the current device is the master
	 *  <li>encryption is false because no sensitive data is transmitted
	 *  <li>authentication is false because the data is not personalized
	 * </ul>
	 */
	private static final String BLUETOOTH_GPS_OPTIONS = ";master=false;encrypt=false;authenticate=false";
	
    /**
     * The instance of this class
     */
    private static BluetoothLocationProvider instance = null;

    /**
     * Holds the connection to the GPS device.  Used to get coordinates from the GPS device.
     */
    private BluetoothGPS gps = null;

    /**
     * URL used to connect
     */
    private String bluetoothURL = null;

    /**
     * The state of the location provider
     */
    private int state = TEMPORARILY_UNAVAILABLE;

    /**
     * Returns a <code>LocationProvider</code> for the GPS device connected to
     * via Bluetooth.
     */
	public static LocationProvider getInstance (Criteria criteria)
		throws LocationException, IOException
	{
		// Make sure we haven't given out our one Bluetooth GPS provider.
		//  Bluetooth will only support a connection to a single other GPS
		//  device so we cap out at one provider.
		if ( instance != null )
		{
			throw new LocationException("Bluetooth GPS socket already in use.");
		}
		
		// See if we meet the criteria.
		if ( matchesCriteria(criteria) )
		{
			String url = criteria.getRemoteDeviceAddress();
			return getInstance( url );
		}
		else
		{
			// Bluetooth GPS doesn't meet the criteria.
			return null;
		}
	}
    
    /**
     * Construct the instance of this class.  If the <code>channelId</code> is <code>null</code>
     * this method will attempt to guess at the channel id.
     *
     * @param remoteDeviceBTAddress - The remote GPS device bluetooth address
     * @param channelId - The channel id for the remote device.  This may be <code>null</code>.  If this
     *  is the case we will simply guess at the channel ID for the device.
     * @throws ConnectionNotFoundException - If the target of the name cannot be found, or if the requested protocol type is not supported. 
     * @throws IOException - If error occurs while establishing bluetooth connection or opening input stream. 
     * @throws SecurityException - May be thrown if access to the protocol handler is prohibited.
     */
    private BluetoothLocationProvider(String remoteDeviceBTAddress,
        String channelId) throws ConnectionNotFoundException, IOException, SecurityException {
        
    	// The number of channels to try connecting on.
    	//  Bluetooth address have channels 1-9 typically.  However, GPS
    	//  devices seem to only have 1 channel.  We'll use two just to
    	//  be safe in case the Bluetooth GPS device allows multiple
    	//  connections.
    	final int maxTries = 2;
    	
        // If the channel id is null, we need to guess at the channel id
        if (channelId == null) {
            // Try a few channels
            for (int i = 1; i <= maxTries; i++) {
                try {
                    bluetoothURL = constructBTURL(remoteDeviceBTAddress,
                            Integer.toString(i));
                    gps = connect(bluetoothURL);
                    break;
                } catch (IOException e) {
                    if (Log.isDebugEnabled()) {
                        Log.debug("Channel ID = " + i + " failed:  " + e.toString());
                    }

                    // If there are still more to try, then try them
                    if (i == maxTries) {
                        throw e;
                    }
                }
            }
        } else {
            // Connect to the remote GPS device
            bluetoothURL = constructBTURL(remoteDeviceBTAddress, channelId);
            gps = connect(bluetoothURL);
        }
    }
    
    /**
     * Returns if the Bluetooth GPS implementation matches the required
     * criteria for the application.
     * <p>
     * Getting specific information about the Bluetooth GPS device is not
     * possible without first connecting to it.  We assume it is a NMEA
     * with SBAS (Satellite Based Augmentation System) such as WAAS.  These
     * devices have the following properties:
     * <ul>
     *  <li>Horizontal accuracy within 1 meter (90% within 3).
     *  <li>Altitude is given.
     *  <li>Vertical accuracy within 3 meters (not positive).
     *  <li>Response times of a second.  In actuality these
     *      devices work at 4800 baud or more meaning they send at least
     *      8 sentences a second (480 characters / 80 characters sentence).
     *      Not all of these sentences give us the necessary data, but usually
     *      it comes in a minimum of twice a second.
     *  <li>Power consumption of medium (arguably high).  Bluetooth is a
     *      battery draining medium.  However, just as with a Bluetooth headset for
     *      voice, a phone will work for many hours on a single charge.
     *  <li>No cost associated.  GPS and SBAS are free signals.
     *  <li>Speed and course are given.
     *  <li>Address information is not supplied.  Only the WGS 84 coordinates are
     *      obtained.
     *  <li>A remote URL, typically to a Bluetooth GPS device, must be supplied.
     *      Otherwise only local LBS found on the device running this MIDlet are
     *      allowed. 
     * </ul>
     * 
     * @param criteria defines the applications LBS requirements.
     * @return <code>true</code> if the Bluetooth GPS meets the specified criteria;
     *  <code>false</code> if it does not.
     */
    public static boolean matchesCriteria (Criteria criteria)
    {
    	if ( criteria.getHorizontalAccuracy() < 1 )  // in meters
    	{
    		return false;
    	}
    	
    	if ( criteria.isAltitudeRequired() && criteria.getVerticalAccuracy() < 1 )  // in meters
    	{
    		return false;
    	}
    	
    	if ( (criteria.getPreferredResponseTime() > 500) && (criteria.getPreferredResponseTime() < 500) )  // in milliseconds
    	{
    		return false;
    	}
    	
    	if ( criteria.getPreferredPowerConsumption() == Criteria.POWER_USAGE_LOW )
    	{
    		return false;
    	}
    	
    	if ( criteria.isAddressInfoRequired() )
    	{
    		return false;
    	}
    	
    	if ( criteria.getRemoteDeviceAddress() == null )
    	{
    		return false;
    	}
    	
    	// If we made it here all the criteria were met.
    	return true;
    }

    /**
     * Get the instance of this class when the Bluetooth URL is known
     *
     * @param bturl - The url to the bluetooth device
     * @throws ConnectionNotFoundException - If the target of the name cannot be found, or if the requested protocol type is not supported. 
     * @throws IOException - If error occurs while establishing bluetooth connection or opening input stream. 
     * @throws SecurityException - May be thrown if access to the protocol handler is prohibited.
     */
    public static BluetoothLocationProvider getInstance(String bturl)
        throws ConnectionNotFoundException, IOException, SecurityException {
        if (instance == null) {
            instance = new BluetoothLocationProvider(bturl, null);
        }

        return instance;
    }

    /**
     * Connect to the GPs device.
     *
     * @param bturl - The url to the bluetooth GPS device
     * @throws ConnectionNotFoundException - If the target of the name cannot be found, or if the requested protocol type is not supported. 
     * @throws IOException - If error occurs while establishing bluetooth connection or opening input stream. 
     * @throws SecurityException - May be thrown if access to the protocol handler is prohibited.
     */
    private BluetoothGPS connect(String bturl)
 		throws ConnectionNotFoundException, IOException, SecurityException {

    	// Connect to the Bluetooth GPS device.
        BluetoothGPS gps = new BluetoothGPS(this, bturl);
        gps.start();

        return gps;
    }

    /**
	 * Construct the Bluetooth URL
	 * 
	 * @param deviceBluetoothAddress - The address of the remote device
	 * @param channelId - The channel ID to use
	 */
	protected static String constructBTURL (String deviceBluetoothAddress, String channelId)
	{
		if ( (channelId == null) || (deviceBluetoothAddress == null) )
		{
			return null;
		}
		
		StringBuffer url = new StringBuffer();
		
		// Add the "btspp://" prefix (if not already there).
		if ( deviceBluetoothAddress.substring(0, BLUETOOTH_PROTOCOL.length())
				.equalsIgnoreCase(BLUETOOTH_PROTOCOL) == false )
		{
			url.append( BLUETOOTH_PROTOCOL );
		}
		
		// Add the address.
		url.append( deviceBluetoothAddress );
		
		// Add the channel ID (if not already there).
		if ( deviceBluetoothAddress.indexOf(':', BLUETOOTH_PROTOCOL.length() + 1) < 0 )
		{
			url.append( ':' );
			url.append( channelId );
		}
		
		// Add the Bluetooth options (if not already there).
		if ( deviceBluetoothAddress.indexOf(';') < 0 )
		{
			url.append( BLUETOOTH_GPS_OPTIONS );
		}
		
		String bturl = url.toString();
		return bturl;
	}

    /**
	 * @see org.j4me.bluetoothgps.LocationProvider#getState()
	 */
    public int getState() {
        return state;
    }
    
    /**
     * Set the state of the location provider
     * 
     * @param state the location provider's state
     */
    public void setState(int state) {
    	this.state = state;
    }
    
	/**
	 * @see org.j4me.bluetoothgps.LocationProvider#getLocation(int)
	 */
	public Location getLocation (int timeout)
		throws LocationException, InterruptedException
	{
		if ( (timeout == 0) || (timeout < -1) )
		{
			throw new IllegalArgumentException();
		}
		
		long start = System.currentTimeMillis();
		timeout *= 1000;  // Convert seconds to milliseconds.
		
		// Poll for the timeout period trying to get a location.
		Location loc;
		
		do
		{
			if ( (state == OUT_OF_SERVICE) || (gps == null) )
			{
				throw new LocationException("Bluetooth location provider is out of service");
			}
			
			// Get the last known location.
			loc = gps.getLastKnownLocation();
			
			if ( loc == null )
			{
				Thread.sleep( 250 );
				
				if ( System.currentTimeMillis() - start > timeout)
				{
					throw new LocationException("Timed out getting location from Bluetooth location provider");
				}
			}
		} while ( loc == null );
		
		return loc;
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
		return gps.getLastKnownLocation();
	}

    /**
     * @see org.j4me.bluetoothgps.LocationProvider#setLocationListener(org.j4me.bluetoothgps.LocationListener, int, int, int)
     */
    public void setLocationListener(LocationListener locationlistener,
        int interval, int timeout, int maxAge) {
        gps.setLocationListener(locationlistener, interval, timeout, maxAge);
    }

    /**
     * @return The address of the Bluetooth GPS device.
     */
    public String getBluetoothURL() {
        return bluetoothURL;
    }

	/**
	 * Forces the Bluetooth GPS to re-acquire its location fix (which may take
	 * almost 40 seconds) and resets the Bluetooth connection.  This is helpful
	 * in making sure the connection is both properly working and getting an
	 * accurate reading.
	 * 
	 * @see org.j4me.bluetoothgps.LocationProvider#reset()
	 */
	public void reset ()
		throws IOException
	{
		if ( gps != null )
		{
			// Perform a warm-start on the GPS receiver so that it re-acquires a fix.
			gps.reacquireFix();
			
			// Mark the provider as unavailable.
			gps.setProviderState( TEMPORARILY_UNAVAILABLE );
		}
	}
	
    /**
     * @see org.j4me.bluetoothgps.LocationProvider#close()
     */
    public void close() {
        if (gps != null) {
    		gps.stop();
            
			// Record we are no longer connected.
            state = OUT_OF_SERVICE;
            
            // Stop sending notifications.
            gps.setLocationListener( null, -1, -1, -1 );
        }
    }
}
