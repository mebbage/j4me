package org.j4me.bluetoothgps;

import j2meunit.framework.*;
import org.j4me.*;

/**
 * Tests the <code>NMEAParser</code> class.  It accepts raw NMEA data coming
 * in from the Bluetooth GPS device.  It then parses it, throws out
 * corrupt data, and takes the parsed sentences and deconstructs them.
 * 
 * @see org.j4me.bluetoothgps.NMEAParser
 */
public class NMEAParserTest
	extends J4METestCase
{
	public NMEAParserTest ()
	{
		super();
	}
	
	public NMEAParserTest (String name, TestMethod method)
	{
		super( name, method );
	}
	
	public Test suite ()
	{
		TestSuite suite = new TestSuite();
		
		suite.addTest(new NMEAParserTest("testByteArrayFinds", new TestMethod() 
				{ public void run(TestCase tc) {((NMEAParserTest) tc).testByteArrayFinds(); } }));
		suite.addTest(new NMEAParserTest("testParseCorruptSentence", new TestMethod() 
				{ public void run(TestCase tc) {((NMEAParserTest) tc).testParseCorruptSentence(); } }));
		suite.addTest(new NMEAParserTest("testThrowAwaySentence", new TestMethod() 
				{ public void run(TestCase tc) {((NMEAParserTest) tc).testThrowAwaySentence(); } }));
		suite.addTest(new NMEAParserTest("testGPRMC", new TestMethod() 
				{ public void run(TestCase tc) {((NMEAParserTest) tc).testGPRMC(); } }));
		suite.addTest(new NMEAParserTest("testGPGGA", new TestMethod() 
				{ public void run(TestCase tc) {((NMEAParserTest) tc).testGPGGA(); } }));
		suite.addTest(new NMEAParserTest("testGPGSA", new TestMethod() 
				{ public void run(TestCase tc) {((NMEAParserTest) tc).testGPGSA(); } }));
		suite.addTest(new NMEAParserTest("testBatchData", new TestMethod() 
				{ public void run(TestCase tc) {((NMEAParserTest) tc).testBatchData(); } }));
		
		return suite;
	}
	
	/**
	 * Tests the <code>indexOf</code> and <code>lastIndexOf</code> methods.  They
	 * scan a byte array similarly to <code>String.indexOf</code> and
	 * <code>String.lastIndexOf</code>.
	 */
	public void testByteArrayFinds ()
	{
		byte[] array = "abcdefghijklmnopqrstuvwxyz".getBytes();
		byte match = 'z';

		// indexOf().
		int index = NMEAParser.indexOf( array, match, 20, array.length );
		assertEquals("indexOf() should find z last", array.length - 1, index);
		
		index = NMEAParser.indexOf( array, match, 0, array.length - 1 );
		assertEquals("indexOf() should stop before z", -1, index);
		
		// lastIndexOf().
		index = NMEAParser.lastIndexOf( array, match, array.length - 1 );
		assertEquals("lastIndexOf() should find z when starting at the end", array.length - 1, index);
		
		index = NMEAParser.lastIndexOf( array, match, array.length - 2 );
		assertEquals("lastIndexOf() should not find z when starting before it", -1, index);
	}
	
	/**
	 * Tests the parser knows how to identify a corrupt NMEA sentence
	 * (this is common) and throws it away.  Sentences are verified by
	 * their length, starting and ending characters, and by checksums.
	 */
	public void testParseCorruptSentence ()
	{
		// Invalid checksum (should be 16, not 15).
		String sentence = "$GPRMC,190350.000,A,3746.0164,N,12226.1176,W,0.45,210.68,031006,,*15\r\n";
		byte[] input = sentence.getBytes();
		
		NMEAParser parser = new NMEAParser();
		int parsedSentences = parser.parse( input, input.length );
		assertEquals("Invalid checksum should have been discarded", NMEAParser.TYPE_NONE, parsedSentences);

		
		// No opening "$" character provided.
		sentence = "GPGGA,190353.000,3746.0164,N,12226.1176,W,1,08,1.0,67.5,M,-25.3,M,,0000*56\r\n";
		input = sentence.getBytes();
		
		parser = new NMEAParser();
		parsedSentences = parser.parse( input, input.length );
		assertEquals("No opening '$' should have been discarded", NMEAParser.TYPE_NONE, parsedSentences);

		
		// Partial sentence.
		sentence = "1171,W,1,08,1.0,68.8,M,-25.3,M,,0000*5C\r\n";
		input = sentence.getBytes();
		
		parser = new NMEAParser();
		parsedSentences = parser.parse( input, input.length );
		assertEquals("Partial sentence should have been discarded", NMEAParser.TYPE_NONE, parsedSentences);

		
		// Two sentences merged together.
		sentence = "$GPGGA,063606.000,3746.0183,N,12226.1274,W,1,05,2.1,55.1,3,23,16,13,27,25,,,,,,,,4.1,2.1,3.6*37\r\n";
		input = sentence.getBytes();
		
		parser = new NMEAParser();
		parsedSentences = parser.parse( input, input.length );
		assertEquals("Merged sentences should have been discarded", NMEAParser.TYPE_NONE, parsedSentences);
	}

	/**
	 * Tests that sentences which provide no useful JSR 179 data are
	 * discarded.
	 */
	public void testThrowAwaySentence ()
	{
		// A legitimate NMEA sentence we don't care about.
		String sentence = "$GPGSV,3,2,09,19,32,069,27,10,25,288,25,03,14,043,30,23,12,143,16*70\r\n";
		byte[] input = sentence.getBytes();

		NMEAParser parser = new NMEAParser();
		int parsedSentences = parser.parse( input, input.length );
		assertEquals("NMEA sentence should have been discarded", NMEAParser.TYPE_NONE, parsedSentences);

		
		// A legitimate propritary SiRF sentence.
		//   It does not contain the standard checksum of NMEA sentences
		//   which makes the test a bit trickier.
		sentence = "$PSRFTXT,Baud rate: 38400  System clock: 1058.797MHz\r\n";
		input = sentence.getBytes();

		parser = new NMEAParser();
		parsedSentences = parser.parse( input, input.length );
		assertEquals("SiRF sentence should have been discarded", NMEAParser.TYPE_NONE, parsedSentences);
	}
	
	/**
	 * Tests a valid $GPRMC sentence is properly parsed.  This sentence
	 * provides latitude, longitude, and speed.
	 */
	public void testGPRMC ()
	{
		String sentence = "$GPRMC,063605.000,A,3746.0188,N,12226.1276,W,0.40,130.67,140207,,*10\r\n";
		byte[] input = sentence.getBytes();

		NMEAParser parser = new NMEAParser();
		int parsedSentences = parser.parse( input, input.length );
		assertEquals("GPRMC sentence should have been parsed", NMEAParser.TYPE_GPRMC, parsedSentences);

		GPSRecord record = parser.getRecordBuffer();
		assertEquals("GPRMC latitude", "3746.0188", record.lattitude);
		assertEquals("GPRMC latitude direction", 'N', record.lattitudeDirection);
		assertEquals("GPRMC longitude", "12226.1276", record.longitude);
		assertEquals("GPRMC longitude direction", 'W', record.longitudeDirection);
		assertEquals("GPRMC speed", "0.40", record.speed);
		assertEquals("GPRMC course", "130.67", record.course);
	}
	
	/**
	 * Tests a valid $GPGGA sentence is properly parsed.  This sentence
	 * provides altitude.
	 */
	public void testGPGGA ()
	{
		String sentence = "$GPGGA,063606.000,3746.0183,N,12226.1274,W,1,05,2.1,55.1,M,-25.3,M,,0000*5C\r\n";
		byte[] input = sentence.getBytes();

		NMEAParser parser = new NMEAParser();
		int parsedSentences = parser.parse( input, input.length );
		assertEquals("GPGGA sentence should have been parsed", NMEAParser.TYPE_GPGGA, parsedSentences);

		GPSRecord record = parser.getRecordBuffer();
		assertEquals("GPGGA altitude", "55.1", record.altitude);
	}
	
	/**
	 * Tests a valid $GPGSA sentence is properly parsed.  This sentence
	 * provides horizontal and veritical accuracy.
	 */
	public void testGPGSA ()
	{
		String sentence = "$GPGSA,A,3,23,16,13,27,25,,,,,,,,4.1,2.1,3.6*37\r\n";
		byte[] input = sentence.getBytes();

		NMEAParser parser = new NMEAParser();
		int parsedSentences = parser.parse( input, input.length );
		assertEquals("GPGSA sentence should have been parsed", NMEAParser.TYPE_GPGSA, parsedSentences);

		GPSRecord record = parser.getRecordBuffer();
		assertEquals("GPGSA horizontal accuracy", "2.1", record.hdop);
		assertEquals("GPGSA vertical accuracy", "3.6", record.vdop);
	}

	/**
	 * Tests a mix of incoming sentences to make sure the outcome is
	 * correct.  The sentences contain all of the above tests and tie
	 * them together. 
	 */
	public void testBatchData ()
	{
		// GPGSV and PSRFTXT sentences should be discarded.
		// First GPRMC should be ovewritten.
		// Last GPRMC has checksum error (should not be "EE") so should be discarded.
		String sentences =
			"$GPGSV,3,1,11,23,67,353,35,20,58,177,26,16,44,074,34,13,41,308,29*76\r\n" +
			"$GPGSV,3,2,11,25,36,049,,27,25,246,32,04,16,292,,01,14,093,*70\r\n" +
			"$GPGSV,3,3,11,31,05,054,,03,02,128,,08,00,236,*4A\r\n" +
			"$GPRMC,063559.998,A,3746.0171,N,12226.1277,W,0.50,149.79,140207,,*15\r\n" +
			"$PSRFTXT,Version:GSW3.1.1TO_3.1.00.07-C23P1.00\r\n" +
			"$PSRFTXT,Version2:F-GPS-03-0510032\r\n" +
			"$PSRFTXT,WAAS Enable\r\n" +
			"$PSRFTXT,TOW:  282973\r\n" +
			"$PSRFTXT,WK:   1414\r\n" +
			"$PSRFTXT,POS:  -2707674 -4260780 3885049\r\n" +
			"$PSRFTXT,CLK:  94976\r\n" +
			"$PSRFTXT,CHNL: 12\r\n" +
			"$PSRFTXT,Baud rate: 38400  System clock: 1058.797MHz\r\n" +
			"$GPGGA,063601.002,3746.0171,N,12226.1278,W,1,05,2.1,59.7,M,-25.3,M,,0000*52\r\n" +
			"$GPGSA,A,3,23,16,13,27,,,,,,,,,6.0,2.8,5.3*39\r\n" +
			"$GPRMC,063601.002,A,3746.0171,N,12226.1278,W,0.47,305.20,140207,,*1E\r\n" +
			"$PSRFTXT,Version:GSW3.1.1TO_3.1.00.07-C23P1.00\r\n" +
			"$GPRMC,063601.002,A,3745.1313,N,12226.6666,W,0.47,305.20,140207,,*EE\r\n";
		byte[] input = sentences.getBytes();

		NMEAParser parser = new NMEAParser();
		int parsedSentences = NMEAParser.TYPE_NONE;
		
		// Loop through the sentences in chunks to
		// make sure they are assembled properly.
		for ( int i = 0; i < input.length; i += 35 )
		{
			// Get the end position.
			int j = i + 35;
			if ( j >= input.length ) j = input.length - 1;
			
			// Chop out our chunk.
			int size = j - i;
			byte[] chunk = new byte[size];
			System.arraycopy( input, i, chunk, 0, size );
			
			// Parse the chunk.
			parsedSentences |= parser.parse( chunk, size );
		}

		assertEquals("All sentences should have been parsed", NMEAParser.ALL_TYPES_MASK, parsedSentences);

		// Verify the contents are correct.
		GPSRecord record = parser.getRecordBuffer();
		assertEquals("GPRMC latitude", "3746.0171", record.lattitude);
		assertEquals("GPRMC latitude direction", 'N', record.lattitudeDirection);
		assertEquals("GPRMC longitude", "12226.1278", record.longitude);
		assertEquals("GPRMC longitude direction", 'W', record.longitudeDirection);
		assertEquals("GPRMC speed", "0.47", record.speed);
		assertEquals("GPRMC course", "305.20", record.course);
		assertEquals("GPGGA altitude", "59.7", record.altitude);
		assertEquals("GPGSA horizontal accuracy", "2.8", record.hdop);
		assertEquals("GPGSA vertical accuracy", "5.3", record.vdop);
	}
}
