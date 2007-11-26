package org.j4me.logging;

import j2meunit.framework.*;

/**
 * Tests logging of the <code>org.j4me.logging</code> package.
 * 
 * @see org.j4me.logging.Log
 * @see org.j4me.logging.Level
 * @see org.j4me.logging.LogMessage
 */
public class LogTests
	extends TestCase
{
	public LogTests ()
	{
		super();
	}
	
	public LogTests (String name, TestMethod method)
	{
		super( name, method );
	}
	
	public Test suite ()
	{
		TestSuite suite = new TestSuite();
		
		suite.addTest(new LogTests("testGettingAndSettingLogLevels", new TestMethod() 
				{ public void run(TestCase tc) {((LogTests) tc).testGettingAndSettingLogLevels(); } }));
		suite.addTest(new LogTests("testCircularBuffer", new TestMethod() 
				{ public void run(TestCase tc) {((LogTests) tc).testCircularBuffer(); } }));
		suite.addTest(new LogTests("testLoggingAtAllLevels", new TestMethod() 
				{ public void run(TestCase tc) {((LogTests) tc).testLoggingAtAllLevels(); } }));
		
		return suite;
	}
	
	/**
	 * Tests that the current severity level that is being logged
	 * can be gotten and changed.
	 */
	public void testGettingAndSettingLogLevels ()
	{
		// Get each log level.
		Log.setLevel( Level.DEBUG );  // Everything should be logged
		int debugLevel = Log.getLogLevel().toInt();
		
		Log.setLevel( Level.INFO );  // Info, warn, and error logged
		int infoLevel = Log.getLogLevel().toInt();
		
		Log.setLevel( Level.WARN );  // Warn and error logged
		int warnLevel = Log.getLogLevel().toInt();
		
		Log.setLevel( Level.ERROR );  // Only errors should be logged
		int errorLevel = Log.getLogLevel().toInt();
		
		Log.setLevel( Level.OFF );  // Nothing should be logged
		int offLevel = Log.getLogLevel().toInt();
		
		// Verify each level is different and has the correct priority.
		assertTrue("Debug less than Info", debugLevel < infoLevel);
		assertTrue("Info less than Warn", infoLevel < warnLevel);
		assertTrue("Warn less than Error", warnLevel < errorLevel);
		assertTrue("Error less than Off", errorLevel < offLevel);
		
		// Set the log level using the int values.
		Log.setLevel( debugLevel );
		assertTrue("Level set to debug", Log.getLogLevel() == Level.DEBUG);
		
		Log.setLevel( infoLevel );
		assertTrue("Level set to info", Log.getLogLevel() == Level.INFO);
		
		Log.setLevel( warnLevel );
		assertTrue("Level set to warn", Log.getLogLevel() == Level.WARN);
		
		Log.setLevel( errorLevel );
		assertTrue("Level set to error", Log.getLogLevel() == Level.ERROR);
		
		Log.setLevel( offLevel );
		assertTrue("Level set to off", Log.getLogLevel() == Level.OFF);
		
		// Test that setting a level by int prevents bad data.
		boolean caughtException = false;
		
		try
		{
			Log.setLevel( debugLevel - 1 );
		}
		catch (IllegalArgumentException e)
		{
			caughtException = true;
		}
		
		assertTrue("IllegalArgumentException for invalid log int value", caughtException);
	}

	/**
	 * Tests the logic of the circular buffer used to store log
	 * messages.
	 */
	public void testCircularBuffer ()
	{
		Log.setLevel( Level.DEBUG );
		
		// Make sure no messages are logged already.
		Log.clear();
		LogMessage[] logs = Log.getLogMessages();
		assertEquals("There should be no log messages after clearing it.", 0, logs.length);
		
		// Add a single log message and verify it.
		String simpleTest = "Simple test";
		Log.error( simpleTest );
		logs = Log.getLogMessages();
		
		assertEquals("There should be one log message.", 1, logs.length);
		assertEquals("First log message level", Level.ERROR, logs[0].level);
		assertEquals("First log message text", simpleTest, logs[0].message);
		
		String simpleTestToString = logs[0].toString();
		assertTrue("Message in toString()", simpleTestToString.indexOf(simpleTest) >= 0);
		
		// Add several messages and verify they are returned in descending
		// chronological order.  The buffer should not flip yet though.
		for ( int i = 0; i < 3; i++ )
		{
			String message = String.valueOf( i );
			Log.error( message );
		}
		
		logs = Log.getLogMessages();
		assertEquals("There should be 4 log messages.", 4, logs.length);
		
		assertEquals("Simple log message is oldest and should be first.", simpleTest, logs[0].message);
		
		for ( int i = 0; i < logs.length - 1; i++ )
		{
			long newer = logs[i + 1].time;
			long older = logs[i].time;
			
			assertTrue("Timestamps should not be 0", newer > 0);
			assertTrue("Newer should have a bigger timestamp than older", newer >= older);
		}
		
		// Fill the buffer so it flips and verify it.
		int testMessages = 30;  // Must be bigger than size of circular buffer
		
		for ( int i = 1; i <= testMessages; i++ )
		{
			String message = String.valueOf( i );
			Log.error( message );
		}
		
		logs = Log.getLogMessages();
		assertTrue("Test did not make enough log messages to verify circular buffer", testMessages > logs.length);
		
		for ( int i = 0; i < logs.length; i++ )
		{
			String message = logs[i].message;
			int value = Integer.valueOf( message ).intValue();
			
			assertEquals("Message in circular buffer", 6 + i, value);
		}
	}

	/**
	 * Tests all the methods that log.  Changes the severity at
	 * which messages are kept to be sure only correct messages
	 * get through.
	 */
	public void testLoggingAtAllLevels ()
	{
		// Verify all logging APIs work at different logging levels.
		for ( int i = 0; i < 5; i++ )
		{
			// Set the log level for this loop iteration.
			if ( i == 0 ) Log.setLevel( Level.DEBUG );
			else if ( i == 1 ) Log.setLevel( Level.INFO );
			else if ( i == 2 ) Log.setLevel( Level.WARN );
			else if ( i == 3 ) Log.setLevel( Level.ERROR );
			else Log.setLevel( Level.OFF );
			
			if ( i == 0 )
			{
				assertTrue("isDebugEnabled true", Log.isDebugEnabled());
			}
			else
			{
				assertTrue("isDebugEnabled false", Log.isDebugEnabled() == false);
			}
			
			if ( i <= 1 )
			{
				assertTrue("isInfoEnabled true", Log.isInfoEnabled());
			}
			else
			{
				assertTrue("isInfoEnabled false", Log.isInfoEnabled() == false);
			}
			
			// Clear the log for this loop.
			Log.clear();
			
			// Log using each method.
			Log.debug( "debug" );
			Log.info( "info" );
			Log.warn( "warn" );
			Log.warn( "throwable", new Exception() );
			Log.error( "error" );
			Log.error( null, new Error("error text") );
			
			// Verify the correct things got logged.
			LogMessage[] logs = Log.getLogMessages();
			
			if ( i == 0 )
			{
				assertEquals("Everything should be logged at debug", 6, logs.length);
				assertEquals("Debug log level", Level.DEBUG, logs[0].level);
				assertEquals("Debug log message", "debug", logs[0].message);
			}
			
			if ( i <= 1 )
			{
				assertEquals("Info log level", Level.INFO, logs[1 - i].level);
				assertEquals("Info log message", "info", logs[1 - i].message);
			}
			
			if ( i <= 2 )
			{
				assertEquals("Warn log level", Level.WARN, logs[2 - i].level);
				assertEquals("Warn log message", "warn", logs[2 - i].message);
				
				assertEquals("Warn with exception log level", Level.WARN, logs[3 - i].level);
				assertTrue("Warn with exception log message text", logs[3 - i].message.indexOf("throwable") >= 0);
				assertTrue("Warn with exception log exception type", logs[3 - i].message.indexOf("Exception") >= 0);
			}
			
			if ( i <= 3 )
			{
				int index = (i < 3 ? i : i + 1);

				assertEquals("Error log level", Level.ERROR, logs[4 - index].level);
				assertEquals("Error log message", "error", logs[4 - index].message);
				
				assertEquals("Error with exception log level", Level.ERROR, logs[5 - index].level);
				assertTrue("Error with exception log exception type", logs[5 - index].message.indexOf("Error") >= 0);
				assertTrue("Error with exception log exception message", logs[5 - index].message.indexOf("error text") >= 0);
			}
			
			if ( i == 4 )
			{
				assertEquals("Nothing should be logged at off", 0, logs.length);
			}
		}
	}
}
