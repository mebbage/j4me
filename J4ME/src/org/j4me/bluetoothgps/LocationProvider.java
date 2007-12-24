package org.j4me.bluetoothgps;

import java.io.*;
import org.j4me.logging.*;

/**
 * This is the starting point for applications using this API and represents a
 * source of the location information. A <code>LocationProvider</code> represents a
 * location-providing module, generating <code>Location</code>s.
 * <p>
 * Applications obtain <code>LocationProvider</code> instances (classes implementing
 * the actual functionality by extending this abstract class) by calling the
 * factory method. It is the responsibility of the implementation to return the
 * correct <code>LocationProvider</code>-derived object.
 * <p>
 * Applications that need to specify criteria for the location provider
 * selection, must first create a <code>Criteria</code> object, and pass it to the
 * factory method. The methods that access the location related information
 * shall throw <code>SecurityException</code> if the application does not have the
 * relevant permission to access the location information.
 * <p>
 * A typical implementation will get a list of Bluetooth devices and let
 * the user select their GPS device.  Then it will persist that address using
 * the J2ME Record Management System (RMS) for the future.  Once the address
 * is known, we can use that as criteria for selecting the GPS device.  The
 * following demonstrates how to do this (note it is a lengthy operation and
 * should be handled appropriately such as on a different thread):
 * <code><pre>
 * 	// Create our location criteria assuming the hosting device has it.
 * 	Criteria criteria = new Criteria();
 * 	criteria.setHorizontalAccuracy( 1 );  // +/- 1 meter 68.27% of the time
 * 	// [Set other criteria here]
 * 
 * 	// See if anything matches on the local device.
 * 	LocationProvider provider = LocationProvider.getInstance( criteria );
 * 
 * 	// Is there a LBS on the local device?
 * 	if ( provider == null )  // then nothing on local device
 * 	{
 * 		// Get the Bluetooth devices within 10 meters that are accepting
 *		// connections.  This can take 10 seconds or more.
 * 		String[][] remoteDevices = LocationProvider.discoverRemoteDevices();
 * 
 * 		if ( remoteDevices == null )
 * 		{
 * 			return;
 * 		}
 * 
 * 		// Show the user a list of devices by the human readable names.
 * 		String[] deviceNames = new String[remoteDevices.length];
 * 
 * 		for ( int i = 0; i < remoteDevices.length; i++ )
 * 		{
 * 			deviceNames[i] = remoteDevices[i][0];
 * 		}
 * 
 * 		// [Show the list here and set an integer deviceIndex to it]
 * 
 * 		// Get the Bluetooth address of the device.
 * 		String address = remoteDevices[deviceIndex][1];
 * 
 *		// [Store the device's name and address for next time]
 *
 *		// See if the remote device will work.
 *		criteria.setRemoteDeviceAddress( address );
 *		provider = LocationProvider.getInstance( criteria );
 *	}
 * </pre></code>
 */
public abstract class LocationProvider
{
	/**
	 * Availability status code: the location provider is available.
	 */
	public static final int AVAILABLE = 1;

	/**
	 * Availability status code: the location provider is temporarily
	 * unavailable. Temporary unavailability means that the method is
	 * unavailable due to reasons that can be expected to possibly change in the
	 * future and the provider to become available. An example is not being able
	 * to receive the signal because the signal used by the location method is
	 * currently being obstructed, e.g. when deep inside a building for
	 * satellite based methods. However, a very short transient obstruction of
	 * the signal should not cause the provider to toggle quickly between
	 * <code>TEMPORARILY_UNAVAILABLE</code> and <code>AVAILABLE</code>.
	 */
	public static final int TEMPORARILY_UNAVAILABLE = 2;

	/**
	 * Availability status code: the location provider is out of service. Being
	 * out of service means that the method is unavailable and the
	 * implementation is not able to expect that this situation would change in
	 * the near future. An example is when using a location method implemented
	 * in an external device and the external device is detached.
	 */
	public static final int OUT_OF_SERVICE = 3;

	/**
	 * The last instance of a location provider.  It is obtained when the user
	 * calls <code>getInstance</code>.
	 */
	private static LocationProvider instance;
	
	/**
	 * Empty constructor to help implementations and extensions. This is not
	 * intended to be used by applications. Applications should not make
	 * subclasses of this class and invoke this constructor from the subclass.
	 */
	protected LocationProvider ()
	{
	}
	
	/**
	 * Uses Bluetooth device discovery to get a list of the nearby (within 10
	 * meters) Bluetooth GPS devices that are turned on.  If more than one
	 * device is returned, the user should select which is their GPS device.
	 * <p>
	 * The address of the Bluetooth device should be set using the
	 * <code>Criteria.setRemoteDeviceAddress</code> method.  That <code>Criteria</code>
	 * object can then be used as the argument to the <code>getInstance</code>
	 * factory method.
	 * <p>
	 * Discovering Bluetooth devices is a lengthy operation.  It usually takes
	 * more than ten seconds.  Therefore this method call normally should
	 * be made from a separate thread to keep the application responsive.
	 * 
	 * @return An array of all the nearby Bluetooth devices that will accept
	 *  a connection, not just GPS devices.  Each array element returns another
	 *  <code>String[2]</code> where the first element is the device's human readable
	 *  name and the second is the address (devices that do not support human
	 *  readable names will be set to the address).  If the operation completed
	 *  successfully, and no devices are nearby, the returned array will have
	 *  length 0.  If the operation terminated, for example because the device
	 *  does not support the Bluetooth API, the returned array will be <code>null</code>.
	 * @throws IOException if any Bluetooth I/O errors occur.  For example if
	 *  another Bluetooth discovery operation is already in progess.
	 * @throws SecurityException if the user did not grant access to Bluetooth
	 *  on the device.
	 */
	public static String[][] discoverBluetoothDevices ()
		throws IOException, SecurityException
	{
		String[][] devices = null;
		
		//#ifndef BLACKBERRY
		
			// If the device doesn't support Bluetooth, just return null.
			if ( supportsBluetoothAPI() == false )
			{
				return null;
			}
			
			// Discover devices.
			BluetoothDeviceDiscovery discoverer = null;
			
			try
			{
				discoverer = (BluetoothDeviceDiscovery)Class.forName( "org.j4me.bluetoothgps.BluetoothDeviceDiscovery" ).newInstance();
			}
			catch (Exception e)
			{
				// Some kind of exception creating the BluetoothDeviceDiscovery object.
				// This can happen on some platforms, such as pre-JSR-82 BlackBerry devices.
				Log.warn("Cannot discover Bluetooth devices", e);
				return null;
			}
	
			devices = discoverer.discoverNearbyDeviceNamesAndAddresses();
			
		//#endif // BlackBerry
			
		return devices;
	}

	/**
	 * This factory method is used to get an actual <code>LocationProvider</code>
	 * implementation based on the defined criteria. The implementation chooses
	 * the <code>LocationProvider</code> so that it best fits the defined criteria,
	 * taking into account also possible implementation dependent preferences of
	 * the end user. If no concrete <code>LocationProvider</code> could be created
	 * that typically can match the defined criteria but there are other
	 * location providers not meeting the criteria that could be returned for a
	 * more relaxed criteria, <code>null</code> is returned to indicate this. The
	 * <code>LocationException</code> is thrown, if all supported location providers
	 * are out of service.
	 * <p>
	 * A <code>LocationProvider</code> instance is returned if there is a location
	 * provider meeting the criteria in either the available or temporarily
	 * unavailable state. Implementations should try to select providers in the
	 * available state before providers in temporarily unavailable state, but
	 * this can't be always guaranteed because the implementation may not always
	 * know the state correctly at this point in time. If a <code>LocationProvider</code>
	 * meeting the criteria can be supported but is currently out of service, it
	 * shall not be returned.
	 * <p>
	 * When this method is called with a <code>Criteria</code> that has all fields
	 * set to the default values (i.e. the least restrictive criteria possible),
	 * the implementation shall return a <code>LocationProvider</code> if there is
	 * any provider that isn't in the out of service state. Passing <code>null</code>
	 * as the parameter is equal to passing a <code>Criteria</code> that has all
	 * fields set to the default values, i.e. the least restrictive set of
	 * criteria.
	 * <p>
	 * This method only makes the selection of the provider based on the
	 * criteria and is intended to return it quickly to the application. Any
	 * possible initialization of the provider is done at an implementation
	 * dependent time and MUST NOT block the call to this method.
	 * <p>
	 * This method may, depending on the implementation, return the same
	 * <code>LocationProvider</code> instance as has been returned previously from
	 * this method to the calling application, if the same instance can be used
	 * to fulfil both defined criteria. Note that there can be only one
	 * <code>LocationListener</code> associated with a <code>LocationProvider</code>
	 * instance.
	 * <p>
	 * Obtaining a provider can be a lengthy operation.  For example, connecting
	 * to a remote Bluetooth GPS device requires establishing a Bluetooth
	 * connection.  Although this usually takes less than a second, this
	 * method should usually be performed in a different thread to keep the
	 * application responsive.
	 * 
	 * @param criteria - the criteria for provider selection or <code>null</code> to
	 *        indicate the least restrictive criteria with default values
	 * @return a <code>LocationProvider</code> meeting the defined criteria or
	 *         <code>null</code> if a <code>LocationProvider</code> that meets the defined
	 *         criteria can't be returned but there are other supported
	 *         available or temporarily unavailable providers that do not meet
	 *         the criteria.
	 * @throws LocationException - if all <code>LocationProvider</code>s are
	 *         currently out of service.
	 * @throws IOException - if an I/O error occurs establishing a connection
	 *         to a remote device.
	 * @throws SecurityException - if the user does not give the MIDlet permissions
	 *         to either the location API (for local LBS) or to the Bluetooth
	 *         API (for remote LBS).  <i>Note</i> some JVM implementations use an
	 *         <code>IOException</code> instead.
	 * @see Criteria
	 */
	public static LocationProvider getInstance (Criteria criteria)
		throws LocationException, IOException, SecurityException
	{
		LocationProvider provider = null;
		LocationException localProviderException = null;

		// First check if LBS on the device is allowed.
		if ( criteria.isLocalLBSAllowed() )
		{
			// Now check if the device has LBS.
			if ( supportsLocationAPI() )
			{
				// Is the device's LBS suitable?
				try
				{
					provider = JSR179LocationProvider.getInstance( criteria );
				}
				catch (LocationException e)
				{
					localProviderException = e;
				}
			}
		}
		
		// If there is no on-device provider, try remote Bluetooth GPS.
		if ( provider == null )
		{
			// Is the Bluetooth GPS suitable?
			try
			{
				provider = BluetoothLocationProvider.getInstance( criteria );
			}
			catch (LocationException e)
			{
				if ( localProviderException != null )
				{
					// Use the local LBS exception, we'd prefer local LBS.
					throw localProviderException;
				}
				else
				{
					// No local LBS available and Bluetooth out of commission.
					throw e;
				}
			}
		}
		
		instance = provider;
		return provider;
	}
	
	/**
	 * Returns if the device supports the Location
	 * API (JSR 179).
	 * 
	 * @return <code>true</code> if the device supports JSR 179; <code>false</code>
	 *  otherwise.
	 */
	public static boolean supportsLocationAPI ()
	{
		try
		{
			Class.forName("javax.microedition.location.LocationProvider");
		}
		catch (Throwable e)  // ClassNotFoundException, NoClassDefFoundError
		{
			return false;
		}
		
		return true;
	}

	/**
	 * Returns if the device supports the Bluetooth
	 * API (JSR 82).  Once you know the device supports the API you can
	 * query for other Bluetooth information through the API's
	 * <code>javax.bluetooth.LocalDevice.getProperty</code> method. 
	 * 
	 * @return <code>true</code> if the device supports JSR 82; <code>false</code>
	 *  otherwise.
	 */
	public static boolean supportsBluetoothAPI ()
	{
		try
		{
			Class.forName("javax.bluetooth.LocalDevice");
		}
		catch (Throwable e)  // ClassNotFoundException, NoClassDefFoundError
		{
			return false;
		}
		
		return true;
	}

	/**
	 * Returns the current state of this <code>LocationProvider</code>. The return
	 * value shall be one of the availability status code constants defined in
	 * this class.
	 * 
	 * @return the availability state of this </code>LocationProvider</code>
	 */
	public abstract int getState ();

	/**
	 * Retrieves a Location with the constraints given by the <code>Criteria</code>
	 * associated with this class.  If no result could be retrieved, a
	 * <code>LocationException</code> is thrown.  If the location can't be determined
	 * within the <code>timeout</code> period specified in the parameter, the method
	 * shall throw a <code>LocationException</code>.
	 * <p>
	 * If the provider is temporarily unavailable, the implementation shall wait and
	 * try to obtain the location until the timeout expires.  If the provider is out
	 * of service, then the <code>LocationException</code> is thrown immediately.
	 * <p>
	 * Note that the individual <code>Location</code> returned might not fulfil
	 * exactly the criteria used for selecting this <code>LocationProvider</code>. 
	 * The <code>Criteria</code> is used to select a location provider that typically
	 * is able to meet the defined criteria, but not necessarily for every individual
	 * location measurement.
	 * 
	 * @param timeout - a timeout value in seconds. -1 is used to indicate that the 
	 *  implementation shall use its default timeout value for this provider.
	 * @return a <code>Location</code> object 
	 * @throws LocationException - if the location couldn't be retrieved or if the
	 *  timeout period expired
	 * @throws InterruptedException - if the operation is interrupted by calling
	 *  <code>reset()</code> from another thread
	 * @throws SecurityException - if the calling application does not have a 
	 *  permission to query the location information
	 * @throws IllegalArgumentException - if the timeout = 0 or timeout < -1
	 */
	public abstract Location getLocation (int timeout)
		throws LocationException, InterruptedException;

	/**
	 * Adds a <code>LocationListener</code> for updates at the defined interval. The
	 * listener will be called with updated location at the defined interval.
	 * The listener also gets updates when the availablilty state of the
	 * <code>LocationProvider</code> changes.
	 * <p>
	 * Passing in -1 as the interval selects the default interval which is
	 * dependent on the used location method. Passing in 0 as the interval
	 * registers the listener to only receive provider status updates and not
	 * location updates at all.
	 * <p>
	 * Only one listener can be registered with each <code>LocationProvider</code>
	 * instance. Setting the listener replaces any possibly previously set
	 * listener. Setting the listener to <code>null</code> cancels the registration
	 * of any previously set listener.
	 * <p>
	 * The implementation shall initiate obtaining the first location result
	 * immediately when the listener is registered and provide the location to
	 * the listener as soon as it is available. Subsequent location updates will
	 * happen at the defined interval after the first one. If the specified
	 * update interval is smaller than the time it takes to obtain the first
	 * result, the listener shall receive location updates with invalid
	 * Locations at the defined interval until the first location result is
	 * available.
	 * <p>
	 * The timeout parameter determines a timeout that is used if it's not
	 * possible to obtain a new location result when the update is scheduled to
	 * be provided. This timeout value indicates how many seconds the update is
	 * allowed to be provided late compared to the defined interval. If it's not
	 * possible to get a new location result (interval + timeout) seconds after
	 * the previous update, the update will be made and an invalid <code>Location</code>
	 * instance is returned. This is also done if the reason for the inability
	 * to obtain a new location result is due to the provider being temporarily
	 * unavailable or out of service. For example, if the interval is 60 seconds
	 * and the timeout is 10 seconds, the update must be delivered at most 70
	 * seconds after the previous update and if no new location result is
	 * available by that time the update will be made with an invalid
	 * <code>Location</code> instance.
	 * <p>
	 * The <code>maxAge</code> parameter defines how old the location result is
	 * allowed to be provided when the update is made. This allows the
	 * implementation to reuse location results if it has a recent location
	 * result when the update is due to be delivered. This parameter can only be
	 * used to indicate a larger value than the normal time of obtaining a
	 * location result by a location method. The normal time of obtaining the
	 * location result means the time it takes normally to obtain the result
	 * when a request is made. If the application specifies a time value that is
	 * less than what can be realized with the used location method, the
	 * implementation shall provide as recent location results as are possible
	 * with the used location method. For example, if the interval is 60
	 * seconds, the <code>maxAge</code> is 20 seconds and normal time to obtain the
	 * result is 10 seconds, the implementation would normally start obtaining
	 * the result 50 seconds after the previous update. If there is a location
	 * result otherwise available that is more recent than 40 seconds after the
	 * previous update, then the <code>maxAge</code> setting to 20 seconds allows to
	 * return this result and not start obtaining a new one.
	 * 
	 * @param locationlistener - the listener to be registered. If set to <code>null</code>
	 *        the registration of any previously set listener is cancelled.
	 * @param interval - the interval in seconds. -1 is used for the default
	 *        interval of this provider. 0 is used to indicate that the
	 *        application wants to receive only provider status updates and not
	 *        location updates at all.
	 * @param timeout - timeout value in seconds, must be greater than 0. if the
	 *        value is -1, the default timeout for this provider is used. Also,
	 *        if the interval is -1 to indicate the default, the value of this
	 *        parameter has no effect and the default timeout for this provider
	 *        is used. If the interval is 0, this parameter has no effect.
	 * @param maxAge - maximum age of the returned location in seconds, must be
	 *        greater than 0 or equal to -1 to indicate that the default maximum
	 *        age for this provider is used. Also, if the interval is -1 to
	 *        indicate the default, the value of this parameter has no effect
	 *        and the default maximum age for this provider is used. If the
	 *        interval is 0, this parameter has no effect.
	 * @throws java.lang.IllegalArgumentException - if <code>interval</code> &lt; -1,
	 *         or if (<code>interval</code> != -1) and (<code>timeout</code> &gt;
	 *         <code>interval</code> or <code>maxAge</code> &gt; <code>interval</code> or (<code>timeout</code>
	 *         &lt; 1 and <code>timeout</code> != -1) or (<code>maxAge</code> &lt; 1 and
	 *         <code>maxAge</code> != -1))
	 * @throws java.lang.SecurityException - if the calling application does not
	 *         have a permission to query the location information
	 */
	public abstract void setLocationListener (
			LocationListener locationlistener, int interval, int timeout, int maxAge);

	/**
	 * Closes the location provider.  Removes any <code>LocationListener</code>
	 * from the provider. This should be called when the MIDlet
	 * no longer requires LBS including when <code>MIDlet.destroyApp</code> is called.
	 * <p>
	 * Closing an already closed provider has no effect.
	 * <p>
	 * This method is new to the J4ME implementation (it is not part of the JSR 179
	 * spec).  It is used, for example, to close the Bluetooth connection.
	 */
	public abstract void close ();
	
	/**
	 * Resets the <code>LocationProvider</code>.
	 * <p>
	 * All pending synchronous location requests will be aborted and any blocked
	 * <code>getLocation</code> method calls will terminate with
	 * <code>InterruptedException</code>.
	 * <p>
	 * Applications can use this method e.g. when exiting to have its threads freed
	 * from blocking synchronous operations.
	 * 
	 * @throws IOException - if an I/O error occurs establishing a connection
	 *         to a remote device.
	 */
	public abstract void reset ()
		throws IOException;
	
	/**
	 * Returns the last known location that the implementation has.  This is the
	 * best estimate that the implementation has for the previously known location.
	 * <p>
	 * Applications can use this method to obtain the last known location and check
	 * the timestamp and other fields to determine if this is recent enough and good
	 * enough for the application to use without needing to make a new request for
	 * the current location.
	 * 
	 * @return a location object. <code>null</code> is returned if the implementation
	 *  doesn't have any previous location information.
	 * @throws SecurityException - if the calling application does not have a
	 *  permission to query the location information
	 */
	public static Location getLastKnownLocation ()
	{
		if ( instance == null )
		{
			return null;
		}
		else
		{
			return instance.getLastKnownLocationToProvider();
		}
	}

	/**
	 * Returns the last known location by the provider.  This is a helper method for
	 * JSR-179's <code>static</code> method <code>getLastKnownLocation</code>.
	 * 
	 * @return a location object. <code>null</code> is returned if the implementation
	 *  doesn't have any previous location information.
	 * @throws SecurityException - if the calling application does not have a
	 *  permission to query the location information
	 * 
	 * @see #getLastKnownLocation()
	 */
	protected abstract Location getLastKnownLocationToProvider ();
}
