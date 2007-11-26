package org.j4me.bluetoothgps;

import j2meunit.framework.*;
import org.j4me.*;

/**
 * Tests the <code>LocationImpl</code> class.
 * 
 * @see org.j4me.bluetoothgps.LocationImpl
 */
public class LocationImplTest
	extends J4METestCase
{
	public LocationImplTest ()
	{
		super();
	}
	
	public LocationImplTest (String name, TestMethod method)
	{
		super( name, method );
	}
	
	public Test suite ()
	{
		TestSuite suite = new TestSuite();
		
		suite.addTest(new LocationImplTest("testInvalid", new TestMethod() 
				{ public void run(TestCase tc) {((LocationImplTest) tc).testInvalid(); } }));
		suite.addTest(new LocationImplTest("testValid", new TestMethod() 
		{ public void run(TestCase tc) {((LocationImplTest) tc).testValid(); } }));

		return suite;
	}
	
	/**
	 * Tests that an invalid <code>Location</code> object can be constructed and
	 * used.
	 */
	public void testInvalid ()
	{
		// Construct an invalid location.
		Location invalid = new LocationImpl();
		assertTrue("Location is invalid.", invalid.isValid() == false);

		// Make sure "toString()" doesn't null out.
		String s = invalid.toString();
		assertNotNull("Invalid toString", s);
	}
	
	/**
	 * Tests that a valid <code>Location</code> object can be constructed and
	 * used.
	 */
	public void testValid ()
	{
		QualifiedCoordinates coordinates = new QualifiedCoordinates( 37.345832, -120.832345, -7.1f, 1.8f, Float.NaN );
		float speed = 3.2f;
		float course = 270.0f;
		long timestamp = System.currentTimeMillis();

		Location location = new LocationImpl( coordinates, speed, course, timestamp );
		
		// Make sure everything is properly stored.
		assertTrue("Location is valid.", location.isValid());
		assertEquals("Coordinates", coordinates, location.getQualifiedCoordinates());
		assertEquals("Speed", speed, location.getSpeed(), 0.00001);
		assertEquals("Course", course, location.getCourse(), 0.00001);
		assertEquals("Timestamp", timestamp, location.getTimestamp());
		
		// Make sure "toString()" doesn't null out.
		String s = location.toString();
		assertNotNull("Valid toString", s);
	}
}
