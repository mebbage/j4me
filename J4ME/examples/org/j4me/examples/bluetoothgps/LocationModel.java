package org.j4me.examples.bluetoothgps;

import org.j4me.bluetoothgps.*;
import org.j4me.logging.*;

/**
 * Stores data about the LBS (Location Based Services) configuration.
 */
public class LocationModel
{
	/**
	 * The <code>Criteria</code> used to select the best location provider.
	 */
	private Criteria criteria;
	
	/**
	 * Has three states:  <code>null</code> if GPS information hasn't been set,
	 * <code>Boolean.TRUE</code> if GPS is on the device, or <code>Boolean.FALSE</code>
	 * if GPS is done through Bluetooth.
	 */
	private Boolean gpsOnDevice;
	
	/**
	 * The friendly name of the Bluetooth GPS device.  This is a human-readable
	 * string that can be shown to the user.
	 */
	private String gpsBluetoothName;

	/**
	 * The URL for communicating with the user's Bluetooth GPS device.
	 * <p>
	 * A separate Bluetooth GPS device is only used when the current device
	 * does not have accurate enough GPS.  When the device's GPS is used
	 * this will be <code>null</code>.
	 */
	private String gpsBluetoothURL;

	/**
	 * The location provider for the application.  This is <code>null</code> if
	 * location services have not yet been initialized.
	 */
	private LocationProvider locationProvider;
	
	/**
	 * @return The criteria used to select the location provider.
	 */
	public Criteria getCriteria ()
	{
		return criteria;
	}
	
	/**
	 * @param criteria is the requirements for the location provider.
	 */
	public void setCriteria (Criteria criteria)
	{
		this.criteria = criteria;
	}
	
	/**
	 * Returns if the device running this MIDlet has the GPS built in
	 * or if GPS comes from a separate Bluetooth device.
	 * 
	 * @return <code>true</code> if GPS comes from this device or <code>false</code>
	 *  if it comes from a separate Bluetooth device.
	 */
	public Boolean isGPSOnDevice ()
	{
		return gpsOnDevice;
	}
	
	/**
	 * Sets that GPS is available on the current device.  Bluetooth will not
	 * be used.
	 */
	public void setGPSOnDevice ()
	{
		gpsOnDevice = Boolean.TRUE;
		
		Log.info("GPS set to on local device");
	}
	
	/**
	 * Records the URL of the Bluetooth GPS device so that on next
	 * startup it can be used again automatically.  If the device supports
	 * GPS with accurate enough resolution (+/- 1 meter) this will be
	 * ignored. 
	 * 
	 * @param name is the friendly name of the Bluetooth GPS device.
	 * @param url is the connection URL to the Bluetooth GPS device.
	 */
	public void setBluetoothGPS (String name, String url)
	{
		// We have GPS information and it isn't on this device.
		this.gpsOnDevice = Boolean.FALSE;
		
		// Record the Bluetooth information.
		this.gpsBluetoothName = name;
		this.gpsBluetoothURL = url;
		
		Log.info("GPS set to Bluetooth " + name);
	}

	/**
	 * Gets the human-readable name of the Bluetooth GPS device.  This can
	 * be displayed on screens to the user.
	 * <p>
	 * Before calling this method you should check that Bluetooth GPS is
	 * being used.  If <code>isGPSOnDevice</code> returns <code>Boolean.FALSE</code>
	 * then you can call this method.  Otherwise GPS information has not
	 * been set or the GPS is on the local device.
	 * 
	 * @return The human-readable name of the Bluetooth GPS device.
	 */
	public String getBluetoothGPSName ()
	{
		return gpsBluetoothName;
	}
	
	/**
	 * Returns the URL of the Bluetooth GPS device.  This can be used to
	 * connect to the remote GPS device through Bluetooth.
	 * <p>
	 * Before calling this method you should check that Bluetooth GPS is
	 * being used.  If <code>isGPSOnDevice</code> returns <code>Boolean.FALSE</code>
	 * then you can call this method.  Otherwise GPS information has not
	 * been set or the GPS is on the local device.
	 * 
	 * @return The URL of the Bluetooth GPS device.
	 */
	public String getBluetoothGPSURL ()
	{
		return gpsBluetoothURL;
	}
	
	/**
	 * Gets the <code>LocationProvider</code> currently used by the application.
	 * If there is no location provider this returns <code>null</code>.
	 * 
	 * @return The <code>LocationProvider</code> in use or <code>null</code> if there
	 *  is not one.
	 */
	public LocationProvider getLocationProvider ()
	{
		return locationProvider;
	}
	
	/**
	 * Sets the <code>LocationProvider</code> used by the application.
	 * 
	 * @param provider is the application's location provider.
	 */
	public void setLocationProvider (LocationProvider provider)
	{
		this.locationProvider = provider;
	}
}
