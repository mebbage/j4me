package org.j4me.collections;

import org.j4me.*;
import j2meunit.framework.*;

/**
 * Tests the <code>CubbyHole</code> class.  It is a thread synchronization
 * helper that stores exactly one object.  A worker thread can get the very
 * latest information stored by a producer.
 * 
 * @see org.j4me.collections.CubbyHole
 */
public class CubbyHoleTest
	extends J4METestCase
{
	public CubbyHoleTest ()
	{
		super();
	}
	
	public CubbyHoleTest (String name, TestMethod method)
	{
		super( name, method );
	}
	
	public Test suite ()
	{
		TestSuite suite = new TestSuite();
		
		suite.addTest(new CubbyHoleTest("testBasics", new TestMethod() 
				{ public void run(TestCase tc) {((CubbyHoleTest) tc).testBasics(); } }));
		suite.addTest(new CubbyHoleTest("testBlocking", new TestMethod() 
				{ public void run(TestCase tc) {((CubbyHoleTest) tc).testBlocking(); } }));
		
		return suite;
	}
	
	/**
	 * Tests that a cubby hole stores exactly one object.  Thread synchronization
	 * is not covered by this test case.
	 */
	public void testBasics ()
	{
		try
		{
			CubbyHole cubby = new CubbyHole();
			
			// Very there is nothing in the empty cubby hole.
			boolean isEmpty = cubby.empty();
			assertTrue("The cubby hole is empty.", isEmpty);
			
			Object peek = cubby.peek();
			assertNull("Nothing comes from peaking into an empty cubby hole.", peek);
			
			// Put something into the cubby hole.
			Integer i = new Integer( 13 );
			cubby.set( i );
			
			isEmpty = cubby.empty();
			assertFalse("The cubby hole has something in it.", isEmpty);
			
			peek = cubby.peek();
			assertSame("The cubby hole correctly stored our object.", i, peek);
	
			Object get = cubby.get();
			assertSame("Got the object stored in the cubby.", i, get);
	
			// The cubby hole should once again be empty.
			isEmpty = cubby.empty();
			assertTrue("The cubby hole is empty again.", isEmpty);
			
			peek = cubby.peek();
			assertNull("Nothing comes from peaking into the empty again cubby hole.", peek);
	
			// Put several objects into the cubby hole before taking one out.
			Integer i1 = new Integer( 1 );
			Integer i2 = new Integer( 2 );
			Integer i3 = new Integer( 3 );
			
			get = cubby.set( i1 );
			assertNull("Nothing returned from empty cubby hole.", get);
	
			get = cubby.set( i2 );
			assertSame("Old data i1 returned from cubby hole.", i1, get);
	
			get = cubby.set( i3 );
			assertSame("Old data i2 returned from cubby hole.", i2, get);
	
			get = cubby.get();
			assertSame("Newest data is in cubby hole.", i3, get);
		}
		catch (InterruptedException e)
		{
			fail( e.toString() );
		}
	}
	
	/**
	 * Tests that a consumer thread blocks waiting for a producer to add
	 * something to the cubby hole.
	 */
	public void testBlocking ()
	{
		final CubbyHole one = new CubbyHole();
		final CubbyHole two = new CubbyHole();
		
		class Consumer extends Thread
		{
			public void run ()
			{
				try
				{
					// Block waiting for something in the first cubby hole.
					Object consume = one.get();
					
					// The producer thread should be blocking waiting for
					// this thread to put something into the second cubby hole.
					two.set( consume );
				}
				catch (Throwable t)
				{
					fail( t.toString() );
				}
			}
		}
		
		try
		{
			// Create a consumer thread.
			Consumer consumer = new Consumer();
			consumer.start();
			
			// Give up the CPU to let the consumer start and block.
			Thread.sleep( 0 );

			// Put some data into the first cubby hole to unblock the consumer.
			Integer data = new Integer( 13 );
			one.set( data );
			
			// Get data from the second cubby hole.  This thread will block
			// until the consumer puts something into it.
			Integer result = (Integer)two.get();
			
			// Verify the consumer thread read our original data from the
			// first cubby hole and put it into the second.
			assertSame("Data integrety verified.", data, result);
		}
		catch (InterruptedException e)
		{
			fail( e.toString() );
		}
	}
}
