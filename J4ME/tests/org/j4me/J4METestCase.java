package org.j4me;

import j2meunit.framework.*;

/**
 * Extends the standard test case class to add extra helpers.
 */
public class J4METestCase
	extends TestCase
{
	/**
	 * Constructs a J2ME test case.
	 */
	public J4METestCase ()
	{
		super();
	}
	
	/**
	 * Constructs a J2ME test case.
	 * 
	 * @param name is the test name.
	 * @param method is the test that will be executed.
	 */
	public J4METestCase (String name, TestMethod method)
	{
		super( name, method );
	}
	
	/**
	 * Asserts that two floating point numbers are equal within some tolerance.
	 * 
	 * @param message describes what should happen when the test is successful.
	 * @param expected is the desired result.
	 * @param actual is the result from the code being tested.
	 * @param tolerance is how much the <code>actual</code> result can deviate from
	 *  the <code>expected</code>.  This is often <code>1.0</code> meaning within 1 whole
	 *  number.
	 */
	public void assertEquals (String message, double expected, double actual, double tolerance)
	{
		boolean succeeded = false;
		
		if ( Double.isNaN(expected) )
		{
			succeeded = (Double.isNaN(actual) ? true : false);
		}
		else if ( Double.isInfinite(expected) )
		{
			succeeded = (Double.isInfinite(actual) ? true : false);
		}
		else if ( Float.isNaN((float)expected) )
		{
			succeeded = (Float.isNaN((float)actual) ? true : false);
		}
		else if ( Float.isInfinite((float)expected) )
		{
			succeeded = (Float.isInfinite((float)actual) ? true : false);
		}
		else
		{
			succeeded = (expected >= actual - tolerance) && (expected <= actual + tolerance);
		}
		
		if ( succeeded == false )
		{
			message += ":  expected=<" + expected + ">, actual=<" + actual +">, tolerance=<" + tolerance + ">";
			throw new AssertionFailedError( message );
		}
	}
	
	/**
	 * Asserts that two fixed point numbers are equal within some tolerance.
	 * 
	 * @param message describes what should happen when the test is successful.
	 * @param expected is the desired fixed point result.
	 * @param actual is the fixed point result from the code being tested.
	 * @param tolerance is how much the <code>actual</code> result can deviate from
	 *  the <code>expected</code>.  This is often <code>1 << 8</code> meaning within 1 whole
	 *  number.
	 */
	public void assertEquals (String message, int expected, int actual, int tolerance)
	{
		boolean succeeded = (expected >= actual - tolerance) && (expected <= actual + tolerance);
		
		if ( succeeded == false )
		{
			message += ":  expected=<" + expected + ">, actual=<" + actual +">, tolerance=<" + tolerance + ">";
			throw new AssertionFailedError( message );
		}
	}
	
	/**
	 * Asserts that a <code>boolean</code> is false.
	 * 
	 * @param message describes what should happen when the test is successful.
	 * @param actual is the result of the test operation.
	 */
	public void assertFalse (String message, boolean actual)
	{
		assertTrue(message, !actual);
	}
	
	/**
	 * Asserts that two <code>boolean</code> values are the same.
	 * 
	 * @param message describes what should happen when the test is successful.
	 * @param expected is the desired boolean value.
	 * @param actual is the boolean value returned from the code being tested.
	 */
	public void assertEquals (String message, boolean expected, boolean actual)
	{
		if ( expected != actual )
		{
			message += ":  expected=<" + expected + ">, actual=<" + actual +">";
			throw new AssertionFailedError( message );
		}
	}
}
