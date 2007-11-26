package org.j4me.bluetoothgps;


/**
 * Storage data type for parsed GPS data.
 */
final class GPSRecord {
    /**
     * Character that indicates a warning.
     */
    public String altitude;
    public String date;
    public String secondsSinceMidnight;
    public String hdop;
    public String lattitude;
    public char lattitudeDirection;
    public String longitude;
    public char longitudeDirection;
    public String quality;
    public String satelliteCount;
    public String vdop;
    public String speed;
    public String course;

    /**
     * Constructs a record object for the current position
     * calculated by GPS.
     */
    public GPSRecord() {
    }

    /**
     * Creates a deep copy of a GPS record object.
     *
     * @param record is GPS record to make a deep copy of.
     */
    public GPSRecord(GPSRecord record) {
        this.altitude = record.altitude;
        this.date = record.date;
        this.secondsSinceMidnight = record.secondsSinceMidnight;
        this.hdop = record.hdop;
        this.lattitude = record.lattitude;
        this.lattitudeDirection = record.lattitudeDirection;
        this.longitude = record.longitude;
        this.longitudeDirection = record.longitudeDirection;
        this.quality = record.quality;
        this.satelliteCount = record.satelliteCount;
        this.vdop = record.vdop;
        this.speed = record.speed;
        this.course = record.course;
    }
}
