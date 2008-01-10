package org.j4me.bluetoothgps;

import java.io.*;
import javax.microedition.io.*;
import org.j4me.logging.*;
import org.j4me.util.*;

/**
 * Main class for communication with GPS receiver. Use this class to access GPS
 * receiver from other classes.
 */
class BluetoothGPS implements Runnable {

    /**
     * The timeout value for Bluetooth connections in milliseconds.
     * Since this tries to connect on 10 Bluetooth channels, the
     * total timeout for connecting is 10x this number.
     * <p>
     * The emulator's default timeout is 10,000 ms.
     */
    private static final short BLUETOOTH_TIMEOUT = 3000;

    /**
     * Time in ms to wait until resume to receive.
     */
    private static final short BREAK = 500;

    /**
     * Wait after calling disconnect
     */
    private static final short DISCONNECT_WAIT = 1000;

    /**
     * Time in ms to sleep before each read. This seems to solve the problem or
     * read hangs.
     */
    public static final short SLEEP_BEFORE_READ = 100;

    /**
     * How long to wait before we just kill the read. We add the sleep value
     * since this sleep is performed before every read and we start the timer
     * before the pre-read sleep.
     */
    public static final short READ_TIMEOUT = SLEEP_BEFORE_READ + 1000;

    /**
     * How long to wait to initialize the bluetooth connection
     */
    public static final short BLUETOOTH_CONNECTION_INIT_SLEEP = 200;
    
    /**
     * Conversion constant to convert between knots and meters per second (m/s).
     */
    private static final float MS_PER_KNOT = 0.514444444444444f;
    
	/**
	 * The number of days since January 1 for the start of a month.  This does not
	 * include leap year days.
	 * 
	 * @see #convertUTCTime(String, String)
	 */
	private static final int MONTH_OFFSET[] =
			{ 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334 };
	
    /**
     * Connection to bluetooth device.
     */
    private StreamConnection connection;

    /**
     * The input stream from the GPS device.  Location data is received through it.
     */
    private InputStream inputStream;

    /**
     * The output stream to the GPS device.  Configuration commands are sent through it.
     */
    private OutputStream outputStream;
    
    /**
     * Receiving happens in separate thread.
     */
    private Thread runner;

    /**
     * Flag to indicate that the runner thread should stop
     */
    private boolean stop = false;

    /**
     * URL used to connect to bluetooth device.
     */
    private String url;

    /**
     * Listener to notify of location events.  If this is <code>null</code> then
     * don't notify anything of location events.
     */
    private LocationListener locationListener;

    /**
     * The source of location information.
     */
    private final BluetoothLocationProvider locationProvider;

    /**
     * The interval, in milliseconds, between calls made to the registered
     * <code>LocationListener.locationUpdated</code>.
     * 
     * @see #setLocationListener(LocationListener, BluetoothLocationProvider)
     */
    private long locationUpdateInterval;
    
    /**
     * The system time for when <code>LocationListener.locationUpdated</code>
     * was last raised.
     * 
     * @see #locationUpdateInterval
     */
    private long lastLocationUpdateTime;

    /**
     * The last <code>Location</code> obtained from the GPS.  This is the provider's
     * best guess about the current location.  It is usually less than a second old.
     */
    private Location location;
    
    /**
     * Creates new receiver. Does not start automatically, use start() instead.
     *
     * @param url -
     *            URL of bluetooth device to connect to.
     */
    public BluetoothGPS(BluetoothLocationProvider provider, String url) {
    	this.locationProvider = provider;
        this.url = url;
    }
    
    /**
     * @return The last <code>Location</code> obtained from the GPS or <code>null</code>
     *  if no location has yet been obtained.
     */
    public Location getLastKnownLocation ()
    {
    	return location;
    }

    /**
     * Establishes a bluetooth serial connection (specified in GPS_BT_URL) and
     * opens an input stream.
     *
     * @see #isConnected()
     * @see #disconnect()
     *
     * @throws ConnectionNotFoundException - If the target of the name cannot be found, or if the requested protocol type is not supported. 
     * @throws IOException - If error occurs while establishing bluetooth connection or opening input stream. 
     * @throws SecurityException - May be thrown if access to the protocol handler is prohibited.
     */
    private synchronized void connect()
    	throws ConnectionNotFoundException, IOException, SecurityException {
    	
        if (!isConnected()) {
        	// Connect to the GPS device.
            Log.info("Connecting to Bluetooth device at " + url);

            connection = (StreamConnection) ConnectorHelper.open(
            		url, Connector.READ_WRITE, BLUETOOTH_TIMEOUT );

            Log.debug("Bluetooth connection established");

            // Record the connection.
            configureBluetoothGPSSettings(connection);

            inputStream = connection.openInputStream();
            outputStream = connection.openOutputStream();
        }
    }

    /**
     * Configure the GPS device.
     * <p>
     * This sends NMEA input sentences to the Bluetooth GPS device.  The type and
     * frequency of output sentences sent back from the device should be altered
     * such that we do not receive messages we do not care about (to avoid the
     * overhead of processing them) and we receive the messages we do care about
     * once a second (maximum rate so that if any are corrupt we get our data as
     * fast as possible).
     * <p>
     * On a few devices it is possible to adjust the baud rate.  However, this is
     * not a good option.  It isn't the speed data is transmitted (actually faster
     * is better), it is the amount of quality data that comes through.
     * <p>
     * A good list of the proprietary input sentences is here:
     * http://www.gpsinformation.org/dale/nmea.htm
     * 
     * @param connection
     *            is the connection object to the Bluetooth GPS unit. The
     *            connection must be {@link Connector#READ} or
     *            {@link Connector#READ_WRITE}.
     */
    private void configureBluetoothGPSSettings(StreamConnection connection)
    {
        if (outputStream != null) {
        	try {

        		// Send SiRF configuration.
            	//  These are specific to the SiRF chipset and are ignored by others.
            	//  They are also ignored if the GPS device is in SiRF mode which it
            	//  should not be.
	        	//
	        	//  The sentences are discussed here:
	        	//     http://www.gpsinformation.org/dale/nmea.htm#sirf
	        	//     http://www.elgps.com/public_ftp/Documentos/SIRF_Protocol.pdf
	        	//  They can be tested using the SiRFDemo application:
	        	//     http://www.gpspassion.com/forumsen/topic.asp?TOPIC_ID=25575
        		//
            	// The Bluetooth modems in SiRF devices do not allow the baud rate
            	// to be changed.  It is usually 38400 or higher.
            	// The SiRF input sentence that controls output sentences is:
            	//     $PSRF103,05,00,01,01*20
				//	where
				//	   $PSRF103
				//	   05         00=GGA
				//	              01=GLL
				//	              02=GSA
				//	              03=GSV
				//	              04=RMC
				//	              05=VTG
            	//                ... through 10
				//	   00         mode, 0=set rate, 1=query
				//	   01         rate in seconds, 0-255
				//	   01         checksum 0=no, 1=yes
				//	   *20        checksum
            	
            	// Send the NMEA sentences we care about every second.
        		outputStream.write( createSentence("PSRF103,00,00,01,01") );  // GPGGA
        		outputStream.write( createSentence("PSRF103,02,00,01,01") );  // GPGSA
        		outputStream.write( createSentence("PSRF103,04,00,01,01") );  // GPRMC

            	// Disable the remaining sentences.
            	//     This avoids the overhead of receiving them, processing them, and
            	//   throwing them away.  They can be very frequent and cause problems
            	//   for slow processors such as on the Motorola SLVR.
            	//     Not all of these are NMEA sentences.  Some are SiRF specific
            	//   PSRFTXT sentences which are the most frequent and therefore most
            	//   problematic.
        		outputStream.write( createSentence("PSRF103,01,00,00,01") );  // GPGLL
        		outputStream.write( createSentence("PSRF103,03,00,00,01") );  // GPGSV
        		outputStream.write( createSentence("PSRF103,05,00,00,01") );  // GPVTG
        		outputStream.write( createSentence("PSRF103,06,00,00,01") );  // GPMSS
        		outputStream.write( createSentence("PSRF103,07,00,00,01") );  // ? (Untested but might be the PSRFTXT we turn off)
        		outputStream.write( createSentence("PSRF103,08,00,00,01") );  // ? (Untested but might be the PSRFTXT we turn off)
        		outputStream.write( createSentence("PSRF103,09,00,00,01") );  // GPZDA
        		outputStream.write( createSentence("PSRF103,10,00,00,01") );  // ? (Untested but might be the PSRFTXT we turn off)
                
	            
	            // Send Garmin configuration. 
				//	Garmin device programming (see section 3.1.5)
				//	http://www.garmin.com/manuals/GPS10_TechnicalSpecifications.pdf
            	//
            	//  Output sentence enable/disable (PGRMO).  The format is:
            	//     $PGRMO,<1>,<2>*hh\r\n
				//	where
            	//     <1> Target sentence description (e.g. GPGSV)
            	//     <2> Mode where:
            	//          0 = disable specific sentence
            	//          1 = enable specific sentence
            	//          2 = disable all output sentences
            	//          3 = enable all output sentences (except GPALM)
            	//          4 = restore factory default output sentences
        		outputStream.write( createSentence("PGRMO,,2") );  // Turn off all sentences
        		outputStream.write( createSentence("PGRMO,GPGGA,1") );  // Turn on GPGGA
            	outputStream.write( createSentence("PGRMO,GPGSA,1") );  // Turn on GPGSA
            	outputStream.write( createSentence("PGRMO,GPRMC,1") );  // Turn on GPRMC

            	
            	// Note the following doesn't work.  The iBlue is apparently locked
            	// at the factory presets according to a note on a discussion board.
            	//
            	// Send Martech (MTK) configuration.  It is the chipset used in the i-Blue 747.
            	//  MTK is produced by Transystem, a company in Taiwan.  It is a chipset
            	//  comparable to the SiRF III.  See the programming guide:
            	//  http://www.transystem.com.tw/driver_manual/EB-230-Data-Sheet-V1.2.pdf
            	//
            	//  Output sentence to enable frequency of input sentences is:
            	//     $PMTK314,<0>,...,<18>*hh\r\n
            	//  where each field
            	//     0=off, 1=every second, 2=every two seconds, etc.
            	//  and the field numbers are
				//	   0 NMEA_SEN_GLL, // GPGLL interval - Geographic Position - Latitude longitude
				//	   1 NMEA_SEN_RMC, // GPRMC interval - Recommended Min. specific GNSS sentence
				//	   2 NMEA_SEN_VTG, // GPVTG interval - Course Over Ground and Ground Speed
				//	   3 NMEA_SEN_GGA, // GPGGA interval - GPS Fix Data
				//	   4 NMEA_SEN_GSA, // GPGSA interval - GNSS DOPS and Active Satellites
				//	   5 NMEA_SEN_GSV, // GPGSV interval - GNSS Satellites in View
				//	   6 NMEA_SEN_GRS, // GPGRS interval - GNSS Range Residuals
				//	   7 NMEA_SEN_GST, // GPGST interval - GNSS Pseudorange Error Statistics
				//	   13 NMEA_SEN_MALM, // PMTKALM interval - GPS almanac information
				//	   14 NMEA_SEN_MEPH, // PMTKEPH interval - GPS ephemeris information
				//	   15 NMEA_SEN_MDGP, // PMTKDGP interval - GPS differential correction information
				//	   16 NMEA_SEN_MDBG, // PMTKDBG interval – MTK debug information
				//	   17 NMEA_SEN_ZDA, // GPZDA interval – Time & Date
				//	   18 NMEA_SEN_MCHN, // PMTKCHN interval – GPS channel status
            	//outputStream.write( createSentence("PMTK314,0,1,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0") );
            	
            	
            	outputStream.flush();
                Log.debug("Configured GPS device settings");
            } catch (IOException e) {
                Log.warn("Could not send configuration sentences to Bluetooth GPS", e);
            }
        }
    }
    
    /**
     * Sends a sentence through the output steam.  The sentence is first packaged
     * with a leading '$' and trailing checksum.  For example, to send the sentence
     * "$PSRF103,01,00,00,01*25\r\n" pass in "PSRF103,01,00,00,01".
     * 
     * @param sentence is the NMEA sentence before being packaged.
     * @return <code>sentence</code> converted so it may be sent through an ouput stream.
     */
    public static byte[] createSentence (String sentence)
    {
    	// Calculate the checksum for the sentence.
    	//   A NMEA checksum is calculated as the XOR of the bytes.
    	byte[] input = sentence.getBytes();
    	int checksum = 0;
    	
    	for ( int i = 0; i < input.length; i++ )
    	{
    		checksum ^= input[i];
    	}
    	
    	String hexChecksum = Integer.toHexString( checksum );
    	hexChecksum = hexChecksum.toUpperCase();  // Turn a-f into A-F
    	
    	// Package the sentence.
    	StringBuffer buffer = new StringBuffer();
    	buffer.append( '$' );
    	buffer.append( sentence );
    	buffer.append( '*' );
    	buffer.append( hexChecksum );
    	buffer.append( "\r\n" );
    	String packagedSentence = buffer.toString();
    	
    	// Send the sentence.
    	byte[] data = packagedSentence.getBytes();
    	return data;
    }

    /**
     * Closes input stream and bluetooth connection as well as sets the
     * corresponding objects to null.
     *
     * @see #disconnect()
     */
    private synchronized void disconnect() {
        Log.debug("Disconnecting from GPS device");

        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
            	outputStream.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (IOException e) {
            Log.warn("Problem closing GPS connection", e);
        }

        inputStream = null;
        outputStream = null;
        connection = null;
    }

    /**
     * @return True, if connected and input stream opened
     */
    public synchronized boolean isConnected() {
        return (connection != null) && (inputStream != null);
    }
    
    /**
     * Forces the GPS device to re-acquire its location fix.  This is done
     * through a warm start which takes around 30+ seconds.  It is useful
     * when the GPS location is not considered accurate.
     * <p>
     * Currently only the SiRF chipset is supported.  It uses static
     * navigation to help steady the GPS readings.  However, the technology
     * only works well for driving and forces the locations to be several
     * meters off over time.  If the location does not seem correct, for
     * example it is consistently 10 meters off the road, calling this method
     * will cause the receiver to re-initialize and acquire the correct location.
     */
    public void reacquireFix ()
    {
    	if ( outputStream != null )
    	{
	    	// Send the SiRF sentence.
	    	try
	    	{
	    		// Warm start sentence (takes 30-60 seconds).
	    		String sentence = "PSRF104,0,0,0,0,0,0,12,2";
	    		
	    		// Can we do a hot start (takes < 10 seconds)?
	    		if ( (location != null) && location.isValid() )
	    		{
	    			QualifiedCoordinates coordinates = location.getQualifiedCoordinates();
	    			
	    			// Convert the time of the last reading to GPS weeks and seconds.
	    			//  Week 0 last started at 23:59:47 UTC on 21 August 1999.  In system time it is
	    			//  convertUTCTime( "210899", Long.toString((23 * 3600) + (59 * 60) + 47) );
	    			//  For more information see:  http://tycho.usno.navy.mil/gps_week.html
	    			long time = location.getTimestamp() - 936921587000L;  // Subtract Week 0
	    			final long oneWeek = 604800000L;  // 1 week in milliseconds = 7d * 24h * 60m * 60s * 1000ms
	    			long weeks = time / oneWeek;
	    			long seconds = (time % oneWeek) / 1000;
	    			
	    			// Form the hot start string using our last known location.
	    			StringBuffer sb = new StringBuffer();
	    			sb.append( "PSRF104," );
	    			sb.append( coordinates.getLatitude() );
	    			sb.append( "," );
	    			sb.append( coordinates.getLongitude() );
	    			sb.append( "," );
	    			sb.append( Float.isNaN(coordinates.getAltitude()) ? 0 : coordinates.getAltitude() );
	    			sb.append( ",0," );
	    			sb.append( seconds );
	    			sb.append( "," );
	    			sb.append( weeks );
	    			sb.append( ",12,3" );
	    			sentence = sb.toString();
	    		}
	    		
		    	outputStream.write( createSentence(sentence) );
	    	}
	    	catch (IOException e)
	    	{
	    		Log.warn("Could not re-acquire Bluetooth GPS fix", e);
	    	}
    	}
    }

    /**
     * Reads in records sent by the GPS receiver. When a supported record has
     * been received pauses for specified amount of time. Continues on I/O
     * errors.
     */
    public void run() {
    	boolean process = true;
    	
        // Set to true when a GPRMC sentence is processed
        boolean processedGPRMC = false;

        // The ASCII chars read from the buffer
        byte[] outputBytes = null;

        // Keep track of how many times we skip a buffer because it is full.
        short skipCnt = 0;

        // The number of bytes read from the buffer
        int result = 0;

        NMEAParser parser = new NMEAParser();

        // Failures often occur if we try to read from the stream right
        // away. Instead, just wait.
        try {
            Thread.sleep(BLUETOOTH_CONNECTION_INIT_SLEEP);
        } catch (InterruptedException e) {
        }

        // Create the thread to interrupt reads that we believe are hung
        BluetoothReadTimeoutThread btrtt = new BluetoothReadTimeoutThread(this.runner,
                READ_TIMEOUT);

        boolean firstItr = true;

        while (process) {
            try {
                // Check if we should stop
                if (stop) {
                    stop();

                    return;
                }

                // If not connected (e.g. because of explicit disconnect before
                // waiting) try to connect again.
                if (!isConnected()) {
                    // This connect seems to fail a couple of times after a read
                    // timeout
                    connect();

                    // Failures often occur if we try to read from the stream
                    // right
                    // away after a connect. Instead, just sleep.
                    try {
                        Thread.sleep(BLUETOOTH_CONNECTION_INIT_SLEEP);
                    } catch (InterruptedException e) {
                    }
                }

                // reader.read has issues overwriting the characters of an array
                outputBytes = new byte[NMEAParser.OUTPUT_BUFFER_MAX_SIZE];

                // Start a thread that monitors that the read does not hand
                if (firstItr) {
                    // First iteration so just start the thread
                    btrtt.start();
                    firstItr = false;
                } else {
                    // Thread has already been started so just wake it up
                    btrtt.restart();
                }

                // For whatever reason we need to sleep (not wait) before
                // every read.
                try {
                    Thread.sleep(SLEEP_BEFORE_READ);
                } catch (InterruptedException e) {
                }

                // If there is no GPS data result will be -1 and we'll loop.
                result = inputStream.read(outputBytes, 0, NMEAParser.OUTPUT_BUFFER_MAX_SIZE);

                if (result < 0) {
                	// The Bluetooth GPS device closed the connection.
                	throw new IOException("Bluetooth device closed connection");
                }
                
                // Notify the bluetooth read timeout thread that we have
                // completed
                // as successful read
                btrtt.setReadSuccess(true);

                //if (Log.isDebugEnabled()) {
                //    Log.debug("Bytes read: " + result);
                //}

                if ((processedGPRMC) &&
                        (result >= NMEAParser.OUTPUT_BUFFER_MAX_SIZE) &&
                        (skipCnt < 4)) {
                    // If we read the max then just throw out what we have
                    // and read again since there is probably more to read.
                    // Only do this if we just processed at least a GPRMC
                    // sentence
                    // on the last iteration and we have not skipped more than 3
                    // times in a row.
                    // We need to flush the buffer because it contain a sentence
                    // start that should be thrown out also.
                    parser.flush();
                    skipCnt++;
                } else {
                    // We only need to reset the processed flag if we are not
                    // skipping again.
                    // Once we have reached the max skips or do not have a full
                    // buffer then we
                    // will fall into this code we we will not skip again until
                    // we process
                    // a GPRMC sentence
                    processedGPRMC = false;
                    skipCnt = 0;

                    try {
                    	// Uncommenting the next line will log all incoming Bluetooth GPS data.
                    	//if (Log.isDebugEnabled()) Log.debug("Raw NMEA:  " + new String(outputBytes, 0, result));

                        int parseResult = parser.parse(outputBytes, result);
                   	
                        // Append and parse
                        if ((parseResult & NMEAParser.TYPE_GPRMC) != 0) {
                            // We processed a GPRMC sentence (lat/lon
                            // update) so note it so that we can maybe skip
                            processedGPRMC = true;
                            
                            // Process the sentences and update the location listener.
                            //  We only need to do this if we get a lat/lon update.
                            //  For all the other sentences (altitude, accuracy)
                            //  we'll just loop until we get a lat/lon.  That way we
                            //  don't notify the location listener too often.
                            processRecord(parser.getRecordBuffer());

                            // Wait to receive more data over Bluetooth.
                            synchronized (this) {
                                try {
                                    wait(BREAK);
                                } catch (InterruptedException e) {
                                }
                            }
                        } else if (parseResult == NMEAParser.TYPE_NOTHING_TO_PROCESS) {
                            // There was not enough to process so yield the CPU
                            synchronized (this) {
                                try {
                                    wait(BREAK);
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                    }
                    // Error while parsing (supported) record.
                    catch (Throwable t) {
                        Log.warn("Problem parsing GPS data", t);
                    }
                }
            } catch (Throwable t) {
                if (t instanceof InterruptedIOException) {
                    // The read was taking too long so we interrupted it
                    Log.info(
                        "Bluetooth GPS stalled.  Disconnecting and reconnecting.");
                } else if ( t instanceof IOException ) {
                	// Also captures BluetoothConnectionException.
                	Log.info("Bluetooth device dropped connection.  Reconnecting.");
                } else if (t instanceof InterruptedException) {
                	// Closing the application down.
                	process = false;
                } else {
                    // Not sure what happened. Log the error and
                    // disconnect.
                    // IOException : Either
                    // thrown while connecting or while reading.
                    // Wait some time before continuing.
                    Log.warn("Unexpected GPS read error", t);
                }

                // Notify that the location provider is unavailable
                setProviderState( LocationProvider.TEMPORARILY_UNAVAILABLE );

                // Disconnect so that we automatically connect again.
                disconnect();

                synchronized (this) {
                    // Give time to disconnect
                    try {
                        wait(DISCONNECT_WAIT);
                    } catch (InterruptedException e) {
                    }
                }

                parser.flush();
            }
        }
    }

    /**
     * Process the record. Convert the string values to floats and notify the
     * location listener.
     *
     * @param record - the record to process.  If <code>null</code> this method is
     *  a no-op.
     */
    private void processRecord(GPSRecord record) {
    	
    	// Make sure there is something to parse.
    	if ( (record == null) || (record.quality == null) ) {
    		return;
    	}
    	
        // We only need to notify the location listener if we have a good fix.
        // The fix quality
        // may have the following values: 0 = no fix, 1 = GPS or standard
        // positioning service (SPS) fix,
        // 2 = DGPS fix, 3 = Precise positioning service (PPS) fix
        if ((record.quality.equals("1")) ||
                (record.quality.equals("2")) ||
                (record.quality.equals("3"))) {
        	try {
                float altitude = (record.altitude == null ? Float.NaN : Float.parseFloat(record.altitude));

                // Convert the vertical and horizontal accuracy to float
                float horizontalAccuracy = Float.parseFloat(record.hdop);
                float verticalAccuracy = Float.parseFloat(record.vdop);

                // Convert the speed to float
                float speed = Float.parseFloat(record.speed);
                speed *= MS_PER_KNOT;  // Convert knots to meters/second
                
                // Convert the course to a float.
                float course = (record.course == null ? Float.NaN : Float.parseFloat(record.course));	                	

                // The parsed value is in the format ddmmmmmm so we need to
                // convert this to degrees.
                float lattitudeDegrees = convertToDegress(record.lattitude,
                        record.lattitudeDirection);
                float longitudeDegrees = convertToDegress(record.longitude,
                        record.longitudeDirection);

                // If we have a valid lattitude and longitude, notify
                // the location listener
                QualifiedCoordinates qualifiedCoordinates = new QualifiedCoordinates(lattitudeDegrees,
                        longitudeDegrees, altitude, horizontalAccuracy, verticalAccuracy);

                // Convert the timestamp from NMEA's definition to Java's.
                long timestamp = convertUTCTime(record.date, record.secondsSinceMidnight);
                
                // Record the latest location.
                location = new LocationImpl(qualifiedCoordinates, speed, course, timestamp); 
                
                // If we got this far the location provider is available so
                // notify if it was previously unavailable
                setProviderState( LocationProvider.AVAILABLE );

                // Notify the location provider of the new location.
                if (locationListener != null) {
	                long now = System.currentTimeMillis();
	                
	                if ( now - lastLocationUpdateTime >= locationUpdateInterval )
	                {
	                	// The update interval has expired.  Time to raise a new event.
	                	lastLocationUpdateTime = now;
	                	
		                // Raise the location event.
	                	try
	                	{
	                		locationListener.locationUpdated(this.locationProvider, location);
	                	}
	                	catch (Throwable userT)
	                	{
	        				// This is a programming error in the user's application.
	        				Log.warn("Unhandled exception in LocationProvider.locationUpdated\n" + location, userT);
	                	}
	                }
                }
        	} catch (NumberFormatException e) {
        		// Bad float string received from GPS.
        		// Ignore it, the sentence was corrupt.
        	} catch (NullPointerException e) {
        		// Ignore it, we don't have enough data yet.  This occationally
        		// happens immediately when record hasn't been filled out.
        	}
        } else {
            // The fix is 0. Set the state to unavailable and notify
            setProviderState( LocationProvider.TEMPORARILY_UNAVAILABLE );
        }
    }
    
    /**
     * Sets the <code>locationProvider</code> state and notifies any registered
     * <code>locationListener</code>.  This should be called from the reader thread.
     * 
     * @param newState is the new state of the location provider.
     */
    public void setProviderState (int newState)
    {
    	// Has the state changed?
        if ( locationProvider.getState() != newState )
        {
        	// Set the new state.
            locationProvider.setState( newState );
            
            // Inform the registered location listener.
            if ( locationListener != null )
            {
            	try
            	{
                    locationListener.providerStateChanged( locationProvider, newState );
            	}
            	catch (Throwable t)
            	{
    				// This is a programming error in the user's application.
    				Log.warn("Unhandled exception in LocationProvider.providerStateChanged to " + newState, t);
            	}
            }
            
            // Give up the CPU so the UI can be repainted.
            synchronized ( this )
            {
            	try
            	{
            		Thread.sleep( 0 );
            	}
            	catch (InterruptedException e) 
            	{
            		// Ignore and continue.
            	}
            }
        }
    }

    /**
     * Convert from the format Degrees Minutes (DDMM.mmmm) -- note there are
     * no seconds -- to decimal degress(DD.dddd).
     *
     * @param value -
     *            the latitude or longitude value
     * @param direction -
     *            either "S" for south or "W" for west
     * @return the value converted to degrees. If the value direction is "S" or
     *         "W" then the return value will be negative.
     */
    public static float convertToDegress(String value, char direction) {
        if (value != null) {
            int idx = value.indexOf('.');

            if (idx > 0) {
                int mmstart = idx - 2;
                float dd = Float.parseFloat(value.substring(0, mmstart));
                float mmmmmm = Float.parseFloat(value.substring(mmstart));
                float val = mmmmmm / 60;
                float result = dd + val;

                if ((direction == 'S') || (direction == 's')) {
                    return result * -1;
                } else if ((direction == 'W') || (direction == 'w')) {
                    return result * -1;
                } else {
                    return result;
                }
            }
        }

        return 0;
    }
    
    /**
     * Converts a UTC date and time string into Java's time.  Java uses the POSIX standard
     * with 1 January 1970 00:00:00 as the epoch.  It has a precision of milliseconds. 
     * 
     * @param date is the current year, month, and day.  NMEA gives it to us like
     *  "140207" which means February 14, 2007.  If this is <code>null</code> the
     *  current system time is returned.
     * @param time is the number of seconds since midnight.  NMEA gives it to us like
     *  "063559.998" where milliseconds come after the decimal and can be any number of
     *  digits.  If this is <code>null</code> the current system time is returned.
     * @return The Java time in milliseconds.
     * 
     * @see java.lang.System#currentTimeMillis()
     */
    public static long convertUTCTime (String date, String time)
    {
    	if ( (date == null) || (time == null) )
    	{
    		return System.currentTimeMillis();
    	}
    	
    	// Convert time to milliseconds seconds since midnight.
    	double d = Double.parseDouble( time );
    	d *= 1000;  // From seconds to milliseconds
    	int today = (int)d;
    	
    	// Parse the date.
    	int day = Integer.parseInt( date.substring(0, 2) );
    	int month = Integer.parseInt( date.substring(2, 4) );
    	int year = Integer.parseInt( date.substring(4, 6) );
    	
    	// How many days since 1970?
    	long days = 10957;  // Days from 1970 to 2000 including leap years (30 * 365 + 7)
    	days += year * 365;  // Since 2000
    	days += ((year - 1) / 4) + 1; // Include leap years
    	
    	days += MONTH_OFFSET[month - 1];  // Days to the current month
    	if ( (year % 4 == 0) && (month >= 3) ) days += 1;  // Leap day
    	
    	days += day;  // Days so far in the current month
    	
    	// Put it all together.
    	long milliseconds = days * 86400000;  // Number of milliseconds in a day
    	milliseconds += today;
    	
    	return milliseconds;
	}

    /**
     * Starts receving of data (if not yet started).
     *
     */
    synchronized public void start() throws IOException {
    	connect();
    	
        if (runner == null) {
            stop = false;
            runner = new Thread(this);
            runner.start();
        }
    }

    /**
     * Stops receiving of data and disconnects from bluetooth device.
     *
     */
    synchronized public void stop() {
        if (runner != null) {
            // Only stop if this is the current thread, otherwise set the stop flag.
            if (Thread.currentThread() == runner) {
                // runner.notify();
                runner = null;
                disconnect();
                
    			// Go into an unavailable state until we get new locations.
                setProviderState( LocationProvider.TEMPORARILY_UNAVAILABLE );
            } else {
                stop = true;

                // Iterrupt it so it can stop itself
                runner.interrupt();
            }
        } else {
            disconnect();
            
            // No runner thread so already in a temporarily unavailable state.
        }
        
        // Give the worker threads a chance to die.
		synchronized ( this )
		{
			try
			{
				Thread.sleep( 40 );
			}
			catch (InterruptedException e)
			{
				// Ignore.
			}
		}
    }

    /**
     * Set the location listener for this class. The listener will be notified
     * when a record is parsed from the remote GPS device
     *
     * @param locationListener - the location listener defined by the user
     * @param interval is the time in seconds between delivering new locations
     *  to <code>locationListener.locationUpdated</code>.
     * @param timeout is how long it can take to get coordinates.
     * @param maxAge is the oldest a location can be, in seconds, that is delivered
     *  to <code>locationListener.locationUpdated</code>.
     * 
     * @see LocationProvider#setLocationListener(LocationListener, int, int, int)
     */
    public void setLocationListener (
    		LocationListener locationListener,
    		int interval, int timeout, int maxAge)
    {
        this.locationListener = locationListener;
        
        if ( interval < 1 )
        {
        	this.locationUpdateInterval = 1000;  // Deliver every second
        }
        else
        {
        	this.locationUpdateInterval = interval * 1000;  // Convert from seconds to milliseconds
        }
    }
}

/**
 * Waits while reading from the input stream so that if we get wedged reading
 * from the input stream, this will interrupt the read.
 */
final class BluetoothReadTimeoutThread extends Thread {
    /**
     * Timeout in milliseconds. This how long a read from the input stream
     * should take.
     */
    private short timeout;

    /**
     * The thread that is doing the read
     */
    private Thread runner;

    /**
     * Set to true when the read completed successfully
     */
    private boolean readSuccess = false;

    /**
     * Create the read time out thread.
     *
     * @param runner
     *            the thread that does the reading
     * @param timeout
     *            the timeout for reads
     */
    BluetoothReadTimeoutThread(Thread runner, short timeout) {
        this.timeout = timeout;
        this.runner = runner;
    }

    /**
     * Used to restart the thread. Wakes it up.
     */
    public synchronized void restart() {
        this.interrupt();
    }

    /**
     * Wait the specified timeout to kill the read thread
     */
    public synchronized void run() {
        while (true) {
            this.readSuccess = false;

            try {
                // Wait the specified time until for the read
                wait(timeout);
            } catch (InterruptedException e) {
                // If this wait is interrupted it means that the read
                // finished successfully.
                readSuccess = true;
            }

            if (!readSuccess) {
                // The read timed out. Interrupt the read thread so
                // that it will disconnect
                runner.interrupt();
            }

            try {
                // Wait until restart is called
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Let the read timeout thread know that the read finished.
     *
     * @param readSuccess
     *            <code>true</code> if the read completed successfully
     */
    public synchronized void setReadSuccess(boolean readSuccess) {
        this.readSuccess = readSuccess;
        this.interrupt();
    }
}
