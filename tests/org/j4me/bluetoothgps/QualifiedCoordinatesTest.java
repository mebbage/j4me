package org.j4me.bluetoothgps;

import org.j4me.*;
import j2meunit.framework.*;

/**
 * Tests the coordinates classes that mimic those in JSR 179.
 * 
 * @see org.j4me.bluetoothgps.Coordinates
 * @see org.j4me.bluetoothgps.QualifiedCoordinates
 */
public class QualifiedCoordinatesTest
	extends J4METestCase
{
	public QualifiedCoordinatesTest ()
	{
		super();
	}
	
	public QualifiedCoordinatesTest (String name, TestMethod method)
	{
		super( name, method );
	}
	
	public Test suite ()
	{
		TestSuite suite = new TestSuite();
		
		suite.addTest(new QualifiedCoordinatesTest("testGetSet", new TestMethod() 
				{ public void run(TestCase tc) {((QualifiedCoordinatesTest) tc).testGetSet(); } }));
		suite.addTest(new QualifiedCoordinatesTest("testToString", new TestMethod() 
				{ public void run(TestCase tc) {((QualifiedCoordinatesTest) tc).testToString(); } }));
		suite.addTest(new QualifiedCoordinatesTest("testDistance", new TestMethod() 
				{ public void run(TestCase tc) {((QualifiedCoordinatesTest) tc).testDistance(); } }));
		suite.addTest(new QualifiedCoordinatesTest("testAzimuthTo", new TestMethod() 
				{ public void run(TestCase tc) {((QualifiedCoordinatesTest) tc).testAzimuthTo(); } }));
		
		return suite;
	}

	/**
	 * Simple test of the get and set methods.
	 */
	public void testGetSet ()
	{
		// Test constructor and getters.
		QualifiedCoordinates coordinates = new QualifiedCoordinates( 37.345832, -120.832345, -7.1f, 1.8f, Float.NaN );
		assertEquals("Lattitude", 37.345832, coordinates.getLatitude(), 0.0000005);
		assertEquals("Longitude", -120.832345, coordinates.getLongitude(), 0.0000005);
		assertEquals("Elevation", -7.1F, coordinates.getAltitude(), 0.05);
		assertEquals("Horizontal accuracy", 1.8F, coordinates.getHorizontalAccuracy(), 0.05);
		assertTrue("Vertical accuracy", Float.isNaN( coordinates.getVerticalAccuracy() ));
		
		// Test setters.
		coordinates.setLatitude( 0.0 );  // Equator
		coordinates.setLongitude( 84.17 );
		coordinates.setAltitude( Float.NaN );
		coordinates.setHorizontalAccuracy( Float.NaN );
		coordinates.setVerticalAccuracy( 12.3f );
		
		assertEquals("2 Lattitude", 0.0, coordinates.getLatitude(), 0.0000005);
		assertEquals("2 Longitude", 84.17, coordinates.getLongitude(), 0.0000005);
		assertTrue("2 Elevation", Float.isNaN( coordinates.getAltitude() ));
		assertTrue("2 Horizontal accuracy", Float.isNaN( coordinates.getHorizontalAccuracy() ));
		assertEquals("2 Vertical accuracy", 12.3f, coordinates.getVerticalAccuracy(), 0.05 );
	}

	/**
	 * Verifies the <code>toString</code> methods do not throw exceptions and
	 * have valid contents.
	 */
	public void testToString ()
	{
		// Test Coordinates.toString()
		Coordinates cc = new Coordinates( 37.345832, -120.832345, -7.1f );
		String s = cc.toString();
		assertTrue("Coordinates has latitude", s.indexOf("37.345832") >= 0 );
		assertTrue("Coordinates has latitude as north", s.indexOf("N") >= 0 );
		assertTrue("Coordinates has longitude", s.indexOf("120.832345") >= 0 );
		assertTrue("Coordinates has longitude as west", s.indexOf("W") >= 0 );
		assertTrue("Coordinates has altitude", s.indexOf("-7.1") >= 0 );

		// Test QualifiedCoordinates.toString()
		QualifiedCoordinates cqc = new QualifiedCoordinates( -73.448444, 55.382833, Float.NaN, Float.NaN, Float.NaN );
		s = cqc.toString();
		assertTrue("QualifiedCoordinates has latitude", s.indexOf("73.448444") >= 0 );
		assertTrue("QualifiedCoordinates has latitude as south", s.indexOf("S") >= 0 );
		assertTrue("QualifiedCoordinates has longitude", s.indexOf("55.382833") >= 0 );
		assertTrue("QualifiedCoordinates has longitude as east", s.indexOf("E") >= 0 );
		
		cqc.setHorizontalAccuracy( 3.0f );
		cqc.setAltitude( 835f );
		cqc.setVerticalAccuracy( 1.7f );
		s = cqc.toString();
		assertTrue("QualifiedCoordinates has horizontal accuracy", s.indexOf("3.0") >= 0 );
		assertTrue("QualifiedCoordinates has vertical accuracy", s.indexOf("1.7") >= 0 );
		
		// Test empty QualifiedCoordinates (make sure there are no exceptions).
		cqc = new QualifiedCoordinates( 0, 0, Float.NaN, Float.NaN, Float.NaN );
		s = cqc.toString();
	}

	/**
	 * Tests the <code>distance</code> method.
	 * <p>
	 * This test data came from <a href="http://www.movable-type.co.uk/scripts/LatLong.html">
	 * Calculate distance and bearing between two Latitude/Longitude points</a>.
	 */
	public void testDistance ()
	{
		// Test the distance in meters of the same point.
		Coordinates cc = new Coordinates( 33.448444, 55.382833, Float.NaN );
		
		float distance = cc.distance( cc );
		assertEquals("Distance to self is 0 meters", 0, distance, 0.0000001);


		// Distance between LAX and JFK (from http://williams.best.vwh.net/avform.htm#Math).
		//  Suppose point 1 is LAX: (33deg 57min N, 118deg 24min W)
		//  Suppose point 2 is JFK: (40deg 38min N,  73deg 47min W)
		Coordinates lax = new Coordinates( 33.0 + 57.0/60.0, -1 * (118.0 + 24.0/60.0), Float.NaN );
		Coordinates jfk = new Coordinates( 40.0 + 38.0/60.0, -1 * (73.0 + 47.0/60.0),  Float.NaN );
		
		double expected = 2144.0 * 1852.0;  // 2144 nautical miles which are defined to be 1852 meters.
		
		distance = lax.distance( jfk );
		assertEquals("Distance in meters", expected, distance, 3000.0);  // tolerance of 3 km
		
		distance = jfk.distance( lax );
		assertEquals("Reflexive test", expected, distance, 3000.0);
	
		
		// Test something a known distance apart that crosses a hemisphere.
		//  Default example from:  http://www.movable-type.co.uk/scripts/LatLong.html
		//  Can use that website to generate more tests.
		Coordinates pt1 = new Coordinates( 53.0 + 9.0/60.0 + 2.0/3600.0, -1 * (1.0 + 50.0/60.0 + 40.0/3600.0), Float.NaN );
		Coordinates pt2 = new Coordinates( 52.0 + 12.0/60.0 + 17.0/3600.0, 0.0 + 8.0/60.0 + 26.0/3600.0, Float.NaN );
		
		distance = pt1.distance( pt2 );
		assertEquals("Distance in meters", 170.2 * 1000, distance, 10.0);
		
		distance = pt2.distance( pt1 );
		assertEquals("Reflexive test", 170.2 * 1000, distance, 10.0);
		
		
		// Test two close points.
		Coordinates close1 = new Coordinates( 37.72468665475846, -122.49742881485966, Float.NaN );
		Coordinates close2 = new Coordinates( 37.72574332138882, -122.49371100087049, Float.NaN );
		
		distance = close1.distance( close2 );
		assertEquals("Distance in meters", 347.9799, distance, 1.0);
	}
	
	/**
	 * Tests the <code>azimuthTo</code> method used for getting a bearing,
	 * or course, between two points.
	 */
	public void testAzimuthTo ()
	{
		// Initial course out of LAX to JFK (from http://williams.best.vwh.net/avform.htm#Math).
		Coordinates lax = new Coordinates( 33.0 + 57.0/60.0, -1 * (118.0 + 24.0/60.0), Float.NaN );
		Coordinates jfk = new Coordinates( 40.0 + 38.0/60.0, -1 * (73.0 + 47.0/60.0),  Float.NaN );
		
		double expected = 66.0;  // 66 degrees
		
		double course = lax.azimuthTo( jfk );
		assertEquals("Initial azimuth out of LAX to JFK", expected, course, 0.5);  // nearest degree
	}
}
