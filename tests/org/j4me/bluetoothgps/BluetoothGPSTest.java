package org.j4me.bluetoothgps;

import j2meunit.framework.*;
import org.j4me.*;

/**
 * Tests the <code>BluetoothGPS</code> class.  It is responsible for all communication
 * with the Bluetooth GPS device.  Other classes parse the sentences and map
 * information to JSR 179.
 * 
 * @see org.j4me.bluetoothgps.BluetoothGPS
 */
public class BluetoothGPSTest
	extends J4METestCase
{
	public BluetoothGPSTest ()
	{
		super();
	}
	
	public BluetoothGPSTest (String name, TestMethod method)
	{
		super( name, method );
	}
	
	public Test suite ()
	{
		TestSuite suite = new TestSuite();
		
		suite.addTest(new BluetoothGPSTest("testCreateSentence", new TestMethod() 
				{ public void run(TestCase tc) {((BluetoothGPSTest) tc).testCreateSentence(); } }));
		
		return suite;
	}
	
	/**
	 * Tests the <code>createSentence</code> method.
	 */
	public void testCreateSentence ()
	{
		// Test that a known sentence is converted to a known result.
		String sentence = "PSRF103,01,00,00,01";
		byte[] result = BluetoothGPS.createSentence( sentence );
		byte[] expected = "$PSRF103,01,00,00,01*25\r\n".getBytes();
		
		assertEquals("NMEA sentence lengths", expected.length, result.length );
		for ( int i = 0; i < expected.length; i++ )
		{
			assertEquals("NMEA sentence character " + i, expected[i], result[i]);
		}
		
	}
}
