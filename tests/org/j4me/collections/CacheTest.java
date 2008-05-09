package org.j4me.collections;

import j2meunit.framework.*;

/**
 * Tests the <code>Cache</code> class.  It is a hashtable with a
 * maximum capacity that removes the LRU element when adding
 * a new one and the cache has reached capacity.
 * 
 * @see org.j4me.collections.Cache
 */
public class CacheTest
	extends TestCase
{
	public CacheTest ()
	{
		super();
	}
	
	public CacheTest (String name, TestMethod method)
	{
		super( name, method );
	}
	
	public Test suite ()
	{
		TestSuite suite = new TestSuite();
		
		suite.addTest(new CacheTest("testBasicAddAndGet", new TestMethod() 
				{ public void run(TestCase tc) {((CacheTest) tc).testBasicAddAndGet(); } }));
		suite.addTest(new CacheTest("testIllegalOperations", new TestMethod()
				{ public void run(TestCase tc) {((CacheTest) tc).testIllegalOperations(); } }));
		suite.addTest(new CacheTest("testAddingTwice", new TestMethod()
				{ public void run(TestCase tc) {((CacheTest) tc).testAddingTwice(); } }));
		suite.addTest(new CacheTest("testLRU", new TestMethod() 
				{ public void run(TestCase tc) {((CacheTest) tc).testLRU(); } }));
		suite.addTest(new CacheTest("testCapacityChange", new TestMethod() 
				{ public void run(TestCase tc) {((CacheTest) tc).testCapacityChange(); } }));
		suite.addTest(new CacheTest("testZeroCapacity", new TestMethod() 
				{ public void run(TestCase tc) {((CacheTest) tc).testZeroCapacity(); } }));
		suite.addTest(new CacheTest("testCapacityOfOne", new TestMethod() 
				{ public void run(TestCase tc) {((CacheTest) tc).testCapacityOfOne(); } }));
		
		return suite;
	}
	
	/**
	 * Tests that an element can be added and retreived from the
	 * cache.  There is no fancy LRU stuff going on.
	 */
	public void testBasicAddAndGet ()
	{
		Cache cache = new Cache(10);
		
		assertEquals("The maximum cache size should be set by the constructor.", 10, cache.getMaxCapacity());
		assertEquals("The cache should initially be empty.", 0, cache.size());
		
		// Add an element.
		int key = 13;
		Integer data = new Integer(42);
		cache.add( new Integer(key), data );
		
		assertEquals("Cache should not be empty now that an element has been added.", 1, cache.size());

		// Get the element back out.
		Object result = cache.get(  new Integer(key) );
		assertTrue("The key should return a reference to the same object that was put in the cache.", data == result);
		
		// Try getting an element that doesn't exit.
		result = cache.get( new Integer(key - 1) );
		assertNull("The key should not return data since it has not been added to the cache.", result);
		
		// Make sure we can clear the cache.
		cache.clear();
		
		assertEquals("Cache should be empty now that it has been cleared.", 0, cache.size());
		
		result = cache.get(  new Integer(key) );
		assertNull("Cache should not contain our key now that is has been cleared.", result);
	}
	
	/**
	 * Tests the cache guards against programming it cannot accept.  This
	 * keeps the cache in a valid state.
	 */
	public void testIllegalOperations ()
	{
		// Test cannot create a cache with no capacity.
		boolean caughtException = false;
		
		try
		{
			new Cache( -1 );
		}
		catch (IllegalArgumentException e)
		{
			caughtException = true;
		}
		catch (Throwable t)
		{
			String actualExceptionName = t.getClass().getName();
			fail( "Expected exception 'IllegalArgumentException' and got '" + actualExceptionName + "'." );
		}
		
		if ( caughtException == false )
		{
			fail( "Expected exception 'IllegalArgumentException' but no exceptions caught." );
		}

		
		// Test cannot change a cache to have have no capacity.
		caughtException = false;
		
		try
		{
			Cache cache = new Cache( 13 );
			cache.setMaxCapacity( -1 );
		}
		catch (IllegalArgumentException e)
		{
			caughtException = true;
		}
		catch (Throwable t)
		{
			String actualExceptionName = t.getClass().getName();
			fail( "Expected exception 'IllegalArgumentException' and got '" + actualExceptionName + "'." );
		}
		
		if ( caughtException == false )
		{
			fail( "Expected exception 'IllegalArgumentException' but no exceptions caught." );
		}

		
		// Test cannot add a null key to a cache.
		caughtException = false;
		
		try
		{
			Cache cache = new Cache( 5 );
			cache.add( null, null );
		}
		catch (IllegalArgumentException e)
		{
			caughtException = true;
		}
		catch (Throwable t)
		{
			String actualExceptionName = t.getClass().getName();
			fail( "Expected exception 'IllegalArgumentException' and got '" + actualExceptionName + "'." );
		}
		
		if ( caughtException == false )
		{
			fail( "Expected exception 'IllegalArgumentException' but no exceptions caught." );
		}

		
		// Test cannot get a null key from a cache.
		caughtException = false;
		
		try
		{
			Cache cache = new Cache( 5 );
			cache.add( new Integer(5), new Integer(5) );
			cache.get( null );
		}
		catch (IllegalArgumentException e)
		{
			caughtException = true;
		}
		catch (Throwable t)
		{
			String actualExceptionName = t.getClass().getName();
			fail( "Expected exception 'IllegalArgumentException' and got '" + actualExceptionName + "'." );
		}
		
		if ( caughtException == false )
		{
			fail( "Expected exception 'IllegalArgumentException' but no exceptions caught." );
		}
	}
	
	/**
	 * Tests adding the same element to the cache twice to make sure there isn't
	 * a duplicate entry.
	 */
	public void testAddingTwice ()
	{
		Integer one = new Integer( 1 );
		Integer two = new Integer( 2 );
		
		int cacheSize = 5;
		Cache cache = new Cache( cacheSize );

		// Add "one" many times.
		for ( int i = 0; i < cacheSize * 2; i++ )
		{
			cache.add( one, one );
		}
		
		assertEquals("one is the only element", 1, cache.size());
		
		// Change the data for "one".
		cache.add( one, two );
		assertEquals("one is still the only element", 1, cache.size());

		Integer data = (Integer)cache.get( one );
		assertEquals("data is two", two, data);
		
		// Just to be sure, add another key.
		cache.add( two, two );
		assertEquals("There are two elements", 2, cache.size());
		
		data = (Integer)cache.get( one );
		assertEquals("key=one and data=two", two, data);
		
		data = (Integer)cache.get( two );
		assertEquals("key=two and data=two", two, data);
	}
	
	/**
	 * Tests that the LRU policy of the cache works as expected.  No resizing
	 * of the maximum cache size is done in this test.
	 */
	public void testLRU ()
	{
		int max = 3;  // Maximum of 3 elements
		Cache cache = new Cache(max);
		
		// Fill the cache, but don't overfill yet.
		cache.add( new Integer(1), new Integer(1) );
		cache.add( new Integer(2), new Integer(2) );
		cache.add( new Integer(3), new Integer(3) );
		
		assertEquals("Cache should be full.", max, cache.size());
		
		// Add another entry and make sure the LRU was ejected.
		cache.add( new Integer(4), new Integer(4) );
		
		assertEquals("Cache should still be full.", max, cache.size());
		
		Object result = cache.get( new Integer(1) );
		assertNull("1 should no longer be in the cache (it was LRU).", result);
		
		// Make sure the cache entries still exist that we expect.
		//  Note must call these in order they were inserted to keep the
		//  same LRU order for later.
		result = cache.get( new Integer(2) );
		assertNotNull("2 should still be in the cache.", result);
		
		result = cache.get( new Integer(3) );
		assertNotNull("3 should still be in the cache.", result);
		
		result = cache.get( new Integer(4) );
		assertNotNull("4 should be in the cache.", result);
		
		// Now try reversing the LRU order and adding more entries.
		result = cache.get( new Integer(3) );
		result = cache.get( new Integer(2) );
		
		cache.add( new Integer(5), new Integer(5) );  // Should kick out 4
		cache.add( new Integer(6), new Integer(6) );  // Should kick out 3
		
		result = cache.get( new Integer(3) );
		assertNull("3 should no longer be in the cache.", result);
		
		result = cache.get( new Integer(4) );
		assertNull("4 should no longer be in the cache.", result);
		
		result = cache.get( new Integer(2) );
		assertNotNull("2 should still be in the cache.", result);
		
		// Order is now:  5, 6, 2.  Get 5, add something, check that 2 and 5 still exist (6 tossed).
		result = cache.get( new Integer(5) );
		assertNotNull("5 should still be in the cache.", result);
		
		cache.add( new Integer(7), new Integer(7) );
		
		result = cache.get( new Integer(2) );
		assertNotNull("2, 5, and 7 should be in the cache.", result);
		
		result = cache.get( new Integer(5) );
		assertNotNull("2, 5, and 7 should be in the cache.", result);
		
		result = cache.get( new Integer(7) );
		assertNotNull("2, 5, and 7 should be in the cache.", result);
		
		// Clear the cache.
		cache.clear();
		
		result = cache.get( new Integer(2) );
		assertNull("2, 5, and 7 should no longer be in the cache.", result);
		
		result = cache.get( new Integer(5) );
		assertNull("2, 5, and 7 should no longer be in the cache.", result);
		
		result = cache.get( new Integer(7) );
		assertNull("2, 5, and 7 should no longer be in the cache.", result);
		
		// Add back in 4 numbers and make sure first is ejected.
		cache.add( new Integer(11), new Integer(11) );
		cache.add( new Integer(12), new Integer(12) );
		cache.add( new Integer(13), new Integer(13) );
		cache.add( new Integer(14), new Integer(14) );
		
		result = cache.get( new Integer(11) );
		assertNull("11 should no longer be in the cache.", result);
		
		result = cache.get( new Integer(12) );
		assertNotNull("12, 13, and 14 should be in the cache.", result);
		
		result = cache.get( new Integer(13) );
		assertNotNull("12, 13, and 14 should be in the cache.", result);
		
		result = cache.get( new Integer(14) );
		assertNotNull("12, 13, and 14 should be in the cache.", result);
	}
	
	/**
	 * Tests the capacity of the cache can be changed dynamically after it is
	 * created.
	 */
	public void testCapacityChange ()
	{
		// Fill a cache.
		Cache cache = new Cache(3);
		cache.add( new Integer(1), new Integer(1) );
		cache.add( new Integer(2), new Integer(2) );
		cache.add( new Integer(3), new Integer(3) );
		
		// Grow the cache.
		cache.setMaxCapacity(5);
		assertEquals("The cache capacity should have grown to 5.", 5, cache.getMaxCapacity());
		
		cache.add( new Integer(4), new Integer(4) );
		cache.add( new Integer(5), new Integer(5) );

		assertNotNull("1, 2, 3, 4, and 5 should be in the cache.", cache.get(new Integer(1)));
		assertNotNull("1, 2, 3, 4, and 5 should be in the cache.", cache.get(new Integer(2)));
		assertNotNull("1, 2, 3, 4, and 5 should be in the cache.", cache.get(new Integer(3)));
		assertNotNull("1, 2, 3, 4, and 5 should be in the cache.", cache.get(new Integer(4)));
		assertNotNull("1, 2, 3, 4, and 5 should be in the cache.", cache.get(new Integer(5)));
		
		// Set the cache to the same size (integer.e. test no-op).
		cache.setMaxCapacity(5);
		
		assertEquals("The cache capacity should remain at 5.", 5, cache.getMaxCapacity());

		assertNotNull("1, 2, 3, 4, and 5 should be in the cache.", cache.get(new Integer(1)));
		assertNotNull("1, 2, 3, 4, and 5 should be in the cache.", cache.get(new Integer(2)));
		assertNotNull("1, 2, 3, 4, and 5 should be in the cache.", cache.get(new Integer(3)));
		assertNotNull("1, 2, 3, 4, and 5 should be in the cache.", cache.get(new Integer(4)));
		assertNotNull("1, 2, 3, 4, and 5 should be in the cache.", cache.get(new Integer(5)));
		
		// Shrink the cache, but not to size 0.
		cache.setMaxCapacity(2);  // Should remove 5 - 2 = 3 elements
		
		assertEquals("The cache capacity should shrink to 2.", 2, cache.getMaxCapacity());
		assertEquals("The cache size should have shrunk to 2.", 2, cache.size());

		assertNull("1, 2, 3 should not be in the cache.", cache.get(new Integer(1)));
		assertNull("1, 2, 3 should not be in the cache.", cache.get(new Integer(2)));
		assertNull("1, 2, 3 should not be in the cache.", cache.get(new Integer(3)));
		
		assertNotNull("4 and 5 should still be in the cache.", cache.get(new Integer(4)));
		assertNotNull("4 and 5 should still be in the cache.", cache.get(new Integer(5)));
	}
	
	/**
	 * Tests that a cache of size 0 doesn't actually cache anything, but lets all
	 * calls execute as normal (i.e. doesn't crash).
	 */
	public void testZeroCapacity ()
	{
		Integer one = new Integer( 1 );
		
		// Create a zero-size cache.
		Cache cache = new Cache( 0 );
		assertEquals("Cache size is 0", 0, cache.getMaxCapacity());
		
		// Make sure add is called without a problem.
		cache.add( one, one );
		assertEquals("one not stored", 0, cache.size());
		
		// Try getting it out just to be sure.
		Object data = cache.get( one );
		assertNull("No data should be in cache", data);
	}
	
	/**
	 * Tests that a cache of size 1  doesn't crash.
	 */
	public void testCapacityOfOne ()
	{
		Integer one = new Integer( 1 );
		Integer two = new Integer( 2 );
		
		// Create the cache.
		Cache cache = new Cache( 1 );
		assertEquals("Cache size is 1", 1, cache.getMaxCapacity());
		
		// Make sure an element can be added.
		cache.add( one, one );
		assertEquals("one stored", 1, cache.size());
		
		Integer data = (Integer)cache.get( one );
		assertEquals("one's data", one, data);
		
		// Add another element.
		cache.add( two, two );
		assertEquals("two only thing stored", 1, cache.size());
		
		data = (Integer)cache.get( one );
		assertNull("one can no longer be retreived", data);
		
		data = (Integer)cache.get( two );
		assertEquals("two's data", two, data);
	}
}
