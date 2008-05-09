package org.j4me.bluetoothgps;

import org.j4me.logging.*;

/**
 * Parses chunks of data from a GPS device.
 */
class NMEAParser {
    private static final String DOLLAR_SIGN_GPGSA = "$GPGSA";
    private static final String DOLLAR_SIGN_GPGGA = "$GPGGA";
    private static final String DOLLAR_SIGN_GPRMC = "$GPRMC";

    /**
     * Size of the string buffer. This should be a little more than the size of
     * the byte array plus 80 (the max size of an NMEA sentence).
     */
    public static final short OUTPUT_BUFFER_MAX_SIZE = 2048;

    /**
     * The maximum size of a sentence according to the NMEA standards is 82. We
     * will use 128 to be safe.
     */
    private static final short MAX_SENTENCE_SIZE = 128;

    /**
     * Sentence characters
     */
    private static final byte SENTENCE_START = '$';
    private static final byte CHECKSUM_START = '*';
    private static final byte SENTENCE_END = '\n';
    private static final byte DELIMITER = ',';

    /**
     * There was not enough to process
     */
    public static final short TYPE_NOTHING_TO_PROCESS = -1;

    /**
     * No type
     */
    public static final short TYPE_NONE = 0;

    /**
     * Type GPRMC.
     */
    public static final short TYPE_GPRMC = 1;

    /**
     * Type GPGGA.
     */
    public static final short TYPE_GPGGA = 2;

    /**
     * Type GPGSA.
     */
    public static final short TYPE_GPGSA = 4;

    /**
     * TYPE_GPRMC | TYPE_GPGGA | TYPE_GPGSA
     */
    public static final short ALL_TYPES_MASK = 7;

    /**
     * The current data read from the GPS device
     */
    private byte[] data = new byte[OUTPUT_BUFFER_MAX_SIZE];

    /**
     * The length of <code>data</code>
     */
    private int dataLength = 0;
    
    /**
     * The record being built
     */
    private GPSRecord record = null;

    /**
     * Holds a record to be processed
     */
    private GPSRecord recordBuffer = null;

    /**
     * Cosntructor. Initialize the output buffer and record
     *
     */
    public NMEAParser() {
        record = new GPSRecord();
    }

    /**
     * Append the output to the output buffer. The size of the output must
     * always be less than {@link #OUTPUT_BUFFER_MAX_SIZE}.
     *
     * @param output - the output in bytes
     * @param size - the size of the output
     */
    private void append(byte[] output, int size)
    {
    	// Allocate a new buffer if we got so much data that it will
    	// contain all our information and the buffer from before can
    	// be discarded to save the overhead of processing it.
        if ( dataLength + size >= data.length )
        {
        	flush();
        }
        
        // Append output to data left over from a past append().
        int start = 0;
        int length = size;
        
        if ( size > data.length )
        {
        	start = size - data.length;
        	length = size - start;
        }
        
        System.arraycopy( output, start, data, dataLength, length );
        dataLength += size;
    }

    /**
     * Flush the buffer.
     */
    public void flush()
    {
    	dataLength = 0;
    }

    /**
     * Append the output and parse it. The size of the output must always be
     * less than {@link #OUTPUT_BUFFER_MAX_SIZE}.
     *
     * @param output - the output to parse in bytes
     * @param size - the size of the output
     * @return a integer to indicate which sentences were parsed
     */
    public int parse(byte[] output, int size) {
        append(output, size);
        return doParse();
    }

    /**
     * Parse the data from the Bluetooth GPS device into NMEA sentences.
     *
     * @return a integer to indicate which sentences were parsed
     */
    private int doParse() {
        int parsedSentenceTypes = TYPE_NONE;

        // If there is hardly anything in the buffer, there won't be a
        // NMEA sentence, so don't bother processing it.
        if (dataLength < 40) {
            return TYPE_NONE;
        }

        // Set the current index to be the
        int currentIndex = dataLength - 1;

        // True if the current sentence is the last sentence in the buffer
        boolean isLastSentence = true;

        // Index of the start of the last sentence
        int lastSentenceStart = -1;

        // While there are characters left to process
        while (currentIndex > 0)
        {
            // Find the start of the last NMEA sentence.
            int sentenceStart = lastIndexOf( data, SENTENCE_START, currentIndex );

            // Did we find the start of a sentence?
            if (sentenceStart != -1)
            {
                // We found the start of a sentence, look for the end.
                int sentenceEnd = indexOf( data, SENTENCE_END, sentenceStart, dataLength );
            	
                // Did we find the sentence end?
                if (sentenceEnd != -1)
                {
                    // Look for the first delimitter to get the sentence type.
                	// (i.e. String.indexOf(DELIMITER, sentenceStart))
                    int sentenceTypeEnd = indexOf( data, DELIMITER, sentenceStart, sentenceEnd );

                    // If we found the type end and the sentence end is within
                    // this sentence, then process the sentence. By checking that the
                    // sentence end is less than the current index then we
                    // handle the the case that we have a buffer left of
                    // "$GPRMC,45667,V,4354.788"
                    // and the first chunch of the new chars does not end the
                    // same sentence
                    // but instead starts a new one.
                    if ((sentenceTypeEnd != -1) && (sentenceEnd <= currentIndex)) {
                        try {
                            String type = new String(data, sentenceStart, sentenceTypeEnd - sentenceStart);
                            
                            if ((type.equals(DOLLAR_SIGN_GPRMC)) &&
                                    ((parsedSentenceTypes & TYPE_GPRMC) == 0)) {
                                parsedSentenceTypes = parsedSentenceTypes |
                                    processSentence(data, sentenceStart, sentenceEnd, TYPE_GPRMC);
                            } else if ((type.equals(DOLLAR_SIGN_GPGGA)) &&
                                    ((parsedSentenceTypes & TYPE_GPGGA) == 0)) {
                                parsedSentenceTypes = parsedSentenceTypes |
                                	processSentence(data, sentenceStart, sentenceEnd, TYPE_GPGGA);
                            } else if ((type.equals(DOLLAR_SIGN_GPGSA)) &&
                                    ((parsedSentenceTypes & TYPE_GPGSA) == 0)) {
                                parsedSentenceTypes = parsedSentenceTypes |
                                	processSentence(data, sentenceStart, sentenceEnd, TYPE_GPGSA);
                            }
                        } catch (Throwable t) {
                            Log.warn("processSentence: dataLength=" + dataLength +
                            		 ", Start=" + sentenceStart + 
                            		 ", End=" + sentenceEnd, t);

                            // We are kind of screwed at this point so just return
                            // what we have and flush the buffer.
                            flush();

                            return parsedSentenceTypes;
                        }

                        // move the current position
                        currentIndex = sentenceStart - 1;

                        // Check if we have a complete record. If so we do
                        // not need to keep working with this buffer
                        if (parsedSentenceTypes == ALL_TYPES_MASK) {
                            break;
                        }
                    } else {
                        // This sentence is bunk, so just skip it
                        currentIndex = sentenceStart - 1;
                    }
                } else {
                    // If this is the last sentence in the buffer, then keep the
                    // index of the start so that we do not delete it.
                    if (isLastSentence) {
                        lastSentenceStart = sentenceStart;
                    }

                    currentIndex = sentenceStart - 1;
                }
            } else {
                break;
            }

            // Once we have completed an iteration, set the last sentence flag
            // to false
            isLastSentence = false;
        } // while

        // Throw away everything that has already been parsed.
        if ( lastSentenceStart < 0 )
        {
        	// Processed everything.  No partial sentence left at the end.
        	flush();
        }
        else
        {
        	// Keep the partial last sentence.
        	dataLength -= lastSentenceStart;
            System.arraycopy( data, lastSentenceStart, data, 0, dataLength );
        }

        // If we parsed any of the sentences that we care about, put the record
        // in the buffer.
        if (parsedSentenceTypes != 0) {
            // Put the record in the record buffer
        	setRecordBuffer( record );

            // Create a new record from the existing record
            record = new GPSRecord(record);
        }

        return parsedSentenceTypes;
    }

    /**
     * Looks for the first occurance of a byte <code>b</code> in <code>array</code> between
     * [<code>fromIndex</code>, <code>stopIndex</code>).
     * 
     * @param array is the data to scan.
     * @param b is the byte to match.
     * @param fromIndex is the first index into array to check.
     * @param stopIndex is one past the last index into array to check.
     * @return The first index where <code>b</code> was found; -1 if it was not
     *  found. 
     */
    protected static int indexOf (byte[] array, byte b, int fromIndex, int stopIndex)
    {
        for ( int position = fromIndex; position < stopIndex; position++ )
        {
        	if ( array[position] == b )
        	{
        		return position;
        	}
        }
        
        // If we made it here, b was not found.
        return -1;
    }
    
    /**
     * Looks for the last occurance of a byte <code>b</code> in <code>array</code> going
     * backwards from <code>fromIndex</code>.
     * 
     * @param array is the data to scan.
     * @param b is the byte to match.
     * @param fromIndex is the first index into array to check.
     * @return The last index where <code>b</code> was found; -1 if it was not
     *  found. 
     */
    protected static int lastIndexOf (byte[] array, byte b, int fromIndex)
    {
        for ( int position = fromIndex; position >= 0; position-- )
        {
        	if ( array[position] == b )
        	{
        		return position;
        	}
        }
        
        // If we made it here, b was not found.
        return -1;
    }
    
    /**
     * @return The lastest location information.
     */
    public synchronized GPSRecord getRecordBuffer() {
        return recordBuffer;
    }

    /**
     * @param record is the latest location information.
     */
    private synchronized void setRecordBuffer(GPSRecord record) {
        this.recordBuffer = record;
    }
    
    /**
     * Process the sentence of the specified type.  The sentence is
     * every ASCII character stored in <code>data</code> between <code>offset</code>
     * and <code>stop</code>.
     *
     * @param data contains the NMEA sentence to process.
     * @param offset is the index that starts the NMEA sentence within <code>data</code>.
     * @param stop is the index of the final character in the sentence.
     * @param type - the sentence type
     * @return the type of the setence processed. If the sentence cannot be
     *         processed this returns 0.
     */
    private short processSentence(byte[] data, int offset, int stop, short type) {
        // The index of the current token
        short tokenIndex = 0;

        // The boundaries of the current token
        int tokenStart = 0;
        
        // The calculated check sum
        int checksum = 0;

        // The check sum read from the sentence
        int sentChecksum = 0;

        // If the sentence is greater than the max size just discard it
        if ( stop - offset <= MAX_SENTENCE_SIZE) {
            SentenceData sentenceData = contructSentenceData(type);
            
            for (int i = offset; i < stop; i++) {
                byte character = data[i];

                if (character == SENTENCE_START) {
                    // Ignore the dollar sign
                } else if (character == CHECKSUM_START) {
                    // First process the remaining token
                    sentenceData.processToken(tokenIndex, data, tokenStart, i - tokenStart);

                    // get the sent check sum
                    try {
                    	String transmittedChecksum = new String( data, i + 1, 2 );
                        sentChecksum = Integer.valueOf(transmittedChecksum, 16).intValue();
                    } catch (NumberFormatException nfe) {
                    	// The check sum was corrupt so discard the sentence.
                        return TYPE_NONE;
                    }

                    // Stop processing
                    break;
                } else {
                    // XOR the checksum with this character's value
                    checksum ^= character;
                    
                    // If this is the delimiter, store the token
                    if (character == DELIMITER) {
                        sentenceData.processToken(tokenIndex, data, tokenStart, i - tokenStart);
                        tokenIndex++;
                        tokenStart = i + 1;
                    }
                }
            }

            // Check if the check sums match
            if (checksum == sentChecksum) {
                // Apply the sentence data to the record
                sentenceData.applySentenceData(record);

                // return the type that we processed
                return type;
            }
        }

        return TYPE_NONE;
    }

    /**
     * Contruct an instance of the sentence data interface that is appropriate
     * for the type passed in.
     *
     * @param type - the type
     * @return the appropriate sentence data implementation instance
     */
    private SentenceData contructSentenceData(int type) {
        switch (type) {
        case TYPE_GPRMC:
            return new GPRMCRecord();

        case TYPE_GPGGA:
            return new GPGGARecord();

        case TYPE_GPGSA:
            return new GPGSARecord();

        default:
            return null;
        }
    }

    /**
     * Interface implemented by classes that know the structure of
     * NMEA sentences.
     */
    private static interface SentenceData {
    	/**
    	 * Processes a token within a NMEA sentence.  Tokens are the data
    	 * between commas.
    	 * 
    	 * @param tokenIndex is the number of commas that have been seen.
	     * @param data contains the NMEA sentence to process.
	     * @param offset is the index that starts the NMEA sentence within <code>data</code>.
	     * @param length is the number of bytes the token is.
    	 */
        public void processToken(short tokenIndex, byte[] data, int offset, int length);

        /**
         * Signals that the location data of the class should be stored in
         * <code>record</code>.
         * 
         * @param record is the most current GPS data.
         */
        public void applySentenceData(GPSRecord record);
    }

    /**
     * $GPGSA NMEA 0183 sentence.  This sentence contains the accuracy (both
     * horizontal and vertical) of the reading.
     */
    private static final class GPGSARecord implements SentenceData {
        public String hdop = null;
        public String vdop = null;

        public void processToken(short tokenIndex, byte[] data, int offset, int length) {
            switch (tokenIndex) {
            case 16:
                hdop = new String( data, offset, length );

                break;

            case 17:
                vdop = new String( data, offset, length );

                break;
            }
        }

        public void applySentenceData(GPSRecord record) {
            record.hdop = hdop;
            record.vdop = vdop;
        }
    }

    /**
     * $GPGGA NMEA 0183 sentence.  This sentence the altitude of coordinates.
     */
    private static final class GPGGARecord implements SentenceData {
        public String quality = null;
        public String satelliteCount = null;
        public String altitude = null;

        public void processToken(short tokenIndex, byte[] data, int offset, int length) {
            switch (tokenIndex) {
            case 6:
                quality = new String( data, offset, length );

                break;

            case 7:
                satelliteCount = new String( data, offset, length );

                break;

            case 9:
            	if ( length > 0 ) {
            		altitude = new String( data, offset, length );
            	} else {
            		altitude = null;
            	}

                break;
            }
        }

        public void applySentenceData(GPSRecord record) {
            record.quality = quality;
            record.satelliteCount = satelliteCount;
            record.altitude = altitude;
        }
    }

    /**
     * $GPRMC NMEA 0183 sentence.  This sentence the latitude, longitude,
     * speed, and course.
     */
    private static final class GPRMCRecord implements SentenceData {
        public String date = null;
        public String secondsSinceMidnight = null;
        public String lattitude = null;
        public char lattitudeDirection;
        public String longitude = null;
        public char longitudeDirection;
        private String speed = null;
        private String course = null;

        public void processToken(short tokenIndex, byte[] data, int offset, int length) {
            switch (tokenIndex) {
            case 1:
            	secondsSinceMidnight = new String( data, offset, length );

                break;

            case 3:
                lattitude = new String( data, offset, length );

                break;

            case 4:

                if (length > 0) {
                    lattitudeDirection = (char)data[offset];
                }

                break;

            case 5:
                longitude = new String( data, offset, length );

                break;

            case 6:

                if (length > 0) {
                	longitudeDirection = (char)data[offset];
                }

                break;

            case 7:
            	// Speed is in knots.
                speed = new String( data, offset, length );

                break;
                
            case 8:
            	if (length > 0) {
            		course = new String( data, offset, length );
            	}
            	
            	break;

            case 9:
            	if (length == 6) {
                    date = new String( data, offset, length );
            	}

                break;
            }
        }

        public void applySentenceData(GPSRecord record) {
            record.date = date;
            record.secondsSinceMidnight = secondsSinceMidnight;
            record.lattitude = lattitude;
            record.lattitudeDirection = lattitudeDirection;
            record.longitude = longitude;
            record.longitudeDirection = longitudeDirection;
            record.speed = speed;
            record.course = course;
        }
    }
}