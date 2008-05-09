package org.j4me.util;

import org.j4me.*;
import j2meunit.framework.*;

/**
 * Tests the <code>MathFunc</code> class.  It adds the missing static methods
 * in J2ME's implemenation of <code>java.lang.Math</code>.
 * 
 * @see org.j4me.util.MathFunc
 * @see java.lang.Math
 */
public class MathFuncTest
	extends J4METestCase
{
	private static final double TOLERANCE = 0.00000001;
	
	public MathFuncTest ()
	{
		super();
	}
	
	public MathFuncTest (String name, TestMethod method)
	{
		super( name, method );
	}
	
	public Test suite ()
	{
		TestSuite suite = new TestSuite();
		
		suite.addTest(new MathFuncTest("testInverseTrig", new TestMethod() 
				{ public void run(TestCase tc) {((MathFuncTest) tc).testInverseTrig(); } }));
		suite.addTest(new MathFuncTest("testRound", new TestMethod() 
				{ public void run(TestCase tc) {((MathFuncTest) tc).testRound(); } }));
		
		return suite;
	}

	/**
	 * Tests the inverse trig methods <code>atan</code>, <code>atan2</code>,
	 * <code>acos</code>, and <code>asin</code>.
	 * <p>
	 * Get values by putting them into Google's search box like "atan(-30.5)".
	 */
	public void testInverseTrig ()
	{
		double answer;
		
		
		// atan()
		answer = MathFunc.atan( Double.NaN );
		assertTrue("atan() special case of NaN param", Double.isNaN(answer));
		
		answer = MathFunc.atan( -0.0 );
		assertTrue("atan() special case of 0.0 param", answer == -0.0);

		answer = MathFunc.atan( -0.0 );
		assertTrue("atan() special case of 0.0 param", answer == -0.0);

		answer = MathFunc.atan( 1.0 );
		assertEquals("atan() of 1.0 is PI/4", Math.PI / 4, answer, TOLERANCE);

		answer = MathFunc.atan( -1.0 );
		assertEquals("atan() of -1.0 is -PI/4", -Math.PI / 4, answer, TOLERANCE);
		
		answer = MathFunc.atan( -30.5 );
		assertEquals("atan() of -30.5 is -PI/4", -1.53802118, answer, TOLERANCE);
		
		
		// atan2()
		answer = MathFunc.atan2( Double.NaN, Double.NaN );
		assertTrue("atan2() special case 1", Double.isNaN(answer));
		
		answer = MathFunc.atan2( 1.0, Double.NaN );
		assertTrue("atan2() special case 2", Double.isNaN(answer));
		
		answer = MathFunc.atan2( 0.0, 1.0 );
		assertTrue("atan2() special case 3", answer == 0.0);
		
		answer = MathFunc.atan2( 1.0, Double.POSITIVE_INFINITY );
		assertTrue("atan2() special case 4", answer == 0.0);
		
		answer = MathFunc.atan2( -0.0, 1.0 );
		assertTrue("atan2() special case 5", answer == -0.0);
		
		answer = MathFunc.atan2( -1.0, Double.POSITIVE_INFINITY );
		assertTrue("atan2() special case 6", answer == -0.0);
		
		answer = MathFunc.atan2( 0.0, -1.0 );
		assertEquals("atan2() special case 7", Math.PI, answer, TOLERANCE);
		
		answer = MathFunc.atan2( 1.0, Double.NEGATIVE_INFINITY );
		assertEquals("atan2() special case 8", Math.PI, answer, TOLERANCE);
		
		// The JavaDoc calls for this to be -PI/2.  But there is no way to
		// differentiate the sign of the -0.0 from a 0.0.  Other specifications
		// say that if y is 0 at all, this should be PI/2 (which is what this
		// implemenatation returns).
		//answer = MathFunc.atan2( -0.0, -1.0 );
		//assertEquals("atan2() special case 9", -Math.PI, answer, TOLERANCE);
		
		answer = MathFunc.atan2( -1.0, Double.NEGATIVE_INFINITY );
		assertEquals("atan2() special case 10", -Math.PI, answer, TOLERANCE);
		
		answer = MathFunc.atan2( 1.0, 0.0 );
		assertEquals("atan2() special case 11", Math.PI / 2.0, answer, TOLERANCE);
		
		answer = MathFunc.atan2( 1.0, -0.0 );
		assertEquals("atan2() special case 12", Math.PI / 2.0, answer, TOLERANCE);
		
		answer = MathFunc.atan2( Double.POSITIVE_INFINITY, 1.0 );
		assertEquals("atan2() special case 13", Math.PI / 2.0, answer, TOLERANCE);
		
		answer = MathFunc.atan2( Double.POSITIVE_INFINITY, -1.0 );
		assertEquals("atan2() special case 14", Math.PI / 2.0, answer, TOLERANCE);
		
		answer = MathFunc.atan2( -1.0, 0.0 );
		assertEquals("atan2() special case 15", -Math.PI / 2.0, answer, TOLERANCE);
		
		answer = MathFunc.atan2( -1.0, -0.0 );
		assertEquals("atan2() special case 16", -Math.PI / 2.0, answer, TOLERANCE);
		
		answer = MathFunc.atan2( Double.NEGATIVE_INFINITY, 1.0 );
		assertEquals("atan2() special case 17", -Math.PI / 2.0, answer, TOLERANCE);
		
		answer = MathFunc.atan2( Double.NEGATIVE_INFINITY, -1.0 );
		assertEquals("atan2() special case 18", -Math.PI / 2.0, answer, TOLERANCE);
		
		answer = MathFunc.atan2( Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY );
		assertEquals("atan2() special case 19", Math.PI / 4.0, answer, TOLERANCE);
		
		answer = MathFunc.atan2( Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY );
		assertEquals("atan2() special case 20", 3.0 * Math.PI / 4.0, answer, TOLERANCE);
		
		answer = MathFunc.atan2( Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY );
		assertEquals("atan2() special case 21", -Math.PI / 4.0, answer, TOLERANCE);
		
		answer = MathFunc.atan2( Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY );
		assertEquals("atan2() special case 22", -3.0 * Math.PI / 4.0, answer, TOLERANCE);
		
		answer = MathFunc.atan2( 10.0, -10.0 );
		assertEquals("atan2() of x=-10.0, y=10.0 is 135.0 degrees", Math.toRadians(135.0), answer, TOLERANCE);

		answer = MathFunc.atan2( -862.420000, 78.514900 );
		assertEquals("atan2() of y=-862.420000, x=78.514900", -1.4800063943825, answer, TOLERANCE);


		// asin()
		answer = MathFunc.asin( 1.1 );
		assertTrue("asin() special case of number greater than 1", Double.isNaN(answer));
		
		answer = MathFunc.asin( -0.0 );
		assertTrue("asin() special case of 0", answer == -0.0);

		answer = MathFunc.asin( 0.7 );
		assertEquals("asin() of 0.7", 0.775397497, answer, TOLERANCE);

		answer = MathFunc.asin( -0.7 );
		assertEquals("asin() of -0.7", -0.775397497, answer, TOLERANCE);


		// acos()
		answer = MathFunc.acos( 1.1 );
		assertTrue("acos() special case of number greater than 1", Double.isNaN(answer));
		
		answer = MathFunc.acos( -0.0 );
		assertEquals("acos() of 0", Math.PI / 2, answer, TOLERANCE);

		answer = MathFunc.acos( 0.7 );
		assertEquals("acos() of 0.7", 0.79539883, answer, TOLERANCE);

		answer = MathFunc.acos( -0.7 );
		assertEquals("acos() of -0.7", 2.34619382, answer, TOLERANCE);
	}
	
	/**
	 * Tests <code>round</code>.
	 */
	public void testRound ()
	{
		long round;
		
		
		// Test rounding of double.
		round = MathFunc.round( 0.0 );
		assertEquals( 0, round );
		
		round = MathFunc.round( 0.5 );
		assertEquals( 1, round );

		round = MathFunc.round( 1.5 );
		assertEquals( 2, round );

		round = MathFunc.round( 1.49999 );
		assertEquals( 1, round );

		round = MathFunc.round( 13.0002 );
		assertEquals( 13, round );
		
		round = MathFunc.round( 38532.99 );
		assertEquals( 38533, round );
		
		round = MathFunc.round( -0.3 );
		assertEquals( 0, round );
		
		round = MathFunc.round( -38332.835 );
		assertEquals( -38333, round );

		
		// Test rounding of float.
		round = MathFunc.round( 0.0F );
		assertEquals( 0, round );
		
		round = MathFunc.round( 0.5F );
		assertEquals( 1, round );

		round = MathFunc.round( 1.5F );
		assertEquals( 2, round );

		round = MathFunc.round( 1.49999F );
		assertEquals( 1, round );

		round = MathFunc.round( 13.0002F );
		assertEquals( 13, round );
		
		round = MathFunc.round( 38532.99F );
		assertEquals( 38533, round );
		
		round = MathFunc.round( -0.3F );
		assertEquals( 0, round );
		
		round = MathFunc.round( -38332.835F );
		assertEquals( -38333, round );
	}
}
