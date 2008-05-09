package org.j4me.bluetoothgps;

/**
 * An implementation of the <code>Location</code> interface.
 */
class LocationImpl
	implements Location
{
	/**
	 * Qualified coordinates
	 */
	private final QualifiedCoordinates qualifiedCoordinates;

	/**
	 * The ground speed
	 */
	private final float speed;

	/**
	 * The bearing
	 */
	private final float course;
	
	/**
	 * Whether this object is valid or not
	 */
	private final boolean valid;
	
	/**
	 * The time this location was obtained using the same time definition as
	 * <code>System.currentTimeMillis</code>.
	 */
	private final long timestamp;
	
	/**
	 * Constructor for a valid location.
	 * 
	 * @param qualifiedCoordinates must not be <code>null</code> if <code>valid</code>
	 *  is <code>true</code>.
	 * @param speed may be <code>Float.NaN</code>.
	 * @param course may be <code>Float.NaN</code>.
	 * @param timestamp is the time the location was taken relative to
	 *  <code>System.currentTimeMillis</code>.
	 */
	public LocationImpl (QualifiedCoordinates qualifiedCoordinates, float speed, float course, long timestamp)
	{
		this.valid = true;
		this.qualifiedCoordinates = (QualifiedCoordinates) qualifiedCoordinates;
		this.speed = speed;
		this.course = course;
		this.timestamp = timestamp;
	}

	/**
	 * Constructor for a invalid location.  Invalid locations have no location
	 * data.  They are dummy objects raised to <code>LocationListener</code>s
	 * when no location data is available.
	 */
	public LocationImpl ()
	{
		this.valid = false;
		this.timestamp = System.currentTimeMillis();
		
		this.qualifiedCoordinates = null;
		this.speed = 0.0f;
		this.course = 0.0f;
	}
	
	/**
	 * @see org.j4me.bluetoothgps.Location#getQualifiedCoordinates()
	 */
	public QualifiedCoordinates getQualifiedCoordinates ()
	{
		return qualifiedCoordinates;
	}

	/**
	 * @see org.j4me.bluetoothgps.Location#getSpeed()
	 */
	public float getSpeed ()
	{
		return speed;
	}

	/**
	 * @see org.j4me.bluetoothgps.Location#getCourse()
	 */
	public float getCourse ()
	{
		return course;
	}

	/**
	 * @see org.j4me.bluetoothgps.Location#getTimestamp()
	 */
	public long getTimestamp ()
	{
		return timestamp;
	}

	/**
	 * @see org.j4me.bluetoothgps.Location#isValid()
	 */
	public boolean isValid ()
	{
		return valid;
	}
	
	/**
	 * @see Object#toString()
	 */
	public String toString ()
	{
		if ( valid )
		{
			return
				qualifiedCoordinates.toString() +
				"\nSpeed=" + speed +
				"\nCourse=" + course +
				"\nTimestamp=" + timestamp;
		}
		else
		{
			return "invalid";
		}
	}
}
