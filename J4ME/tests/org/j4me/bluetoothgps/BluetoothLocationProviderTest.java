package org.j4me.bluetoothgps;

import j2meunit.framework.*;
import org.j4me.*;

/**
 * Tests the <code>BluetoothLocationProvider</code> class.  It is responsible for mapping
 * between JSR 179's <code>LocationProvider</code> interface and <code>BluetoothGPS</code>.
 * 
 * @see org.j4me.bluetoothgps.BluetoothLocationProvider
 */
public class BluetoothLocationProviderTest
	extends J4METestCase
{
	public BluetoothLocationProviderTest ()
	{
		super();
	}
	
	public BluetoothLocationProviderTest (String name, TestMethod method)
	{
		super( name, method );
	}
	
	public Test suite ()
	{
		TestSuite suite = new TestSuite();
		
		suite.addTest(new BluetoothLocationProviderTest("testConstructBTURL", new TestMethod() 
				{ public void run(TestCase tc) {((BluetoothLocationProviderTest) tc).testConstructBTURL(); } }));
		
		return suite;
	}
	
	/**
	 * Tests the <code>constructBTURL</code> method.
	 * <p>
	 * Bluetooth GPS URLs should look like the following example:
	 * <pre><code>
	 *    btspp://0123456789:1;master=false;encrypt=false;authenticate=false
	 * </code></pre>
	 */
	public void testConstructBTURL ()
	{
		String fullURL = "btspp://0123456789:1;master=false;encrypt=false;authenticate=false";
		String channel = "1";
		String addressOnly = "0123456789";
		String withoutChannel = "btspp://0123456789";
		String withoutParams = "btspp://0123456789:1";
		String withoutProtocol = "0123456789:1;master=false;encrypt=false;authenticate=false";
		
		// Test if we put in the address only we get out the whole URL.
		String result = BluetoothLocationProvider.constructBTURL( addressOnly, channel );
		assertEquals("Address only", fullURL, result);
		
		// Test with whole URL.
		result = BluetoothLocationProvider.constructBTURL( fullURL, channel );
		assertEquals("Whole URL", fullURL, result);
		
		// Test without the channel.
		result = BluetoothLocationProvider.constructBTURL( withoutChannel, channel );
		assertEquals("Missing channel and parameters", fullURL, result);
		
		// Test without params.
		result = BluetoothLocationProvider.constructBTURL( withoutParams, channel );
		assertEquals("Missing parameters", fullURL, result);
		
		// Test without the protocol.
		result = BluetoothLocationProvider.constructBTURL( withoutProtocol, channel );
		assertEquals("Missing protocol", fullURL, result);
	}
}
