/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */
package  org.cougaar.tools.csmart.util;

import  java.io.*;

/**
 * Courtesy of <a href="http://openmap.bbn.com">Openmap</a><br>
 * Written by Don Dietrick, BBNT
 * <br>
 * Encapsulates latitude and longitude coordinates in decimal degrees.
 * Normalizes the internal representation of latitude and longitude.
 * <p>
 * <strong>Normalized Latitude:</strong><br>
 * -90&deg; &lt;= &phi; &lt;= 90&deg;
 * <p>
 * <strong>Normalized Longitude:</strong><br>
 * -180&deg; &lt;= &lambda; &lt;= 180&deg;
 *
 */
public class LatLonPoint
        implements Cloneable, Serializable {
    // SOUTH_POLE <= phi <= NORTH_POLE
    // -DATELINE <= lambda <= DATELINE
    public final static float NORTH_POLE = 90.0f;
    public final static float SOUTH_POLE = -NORTH_POLE;
    public final static float DATELINE = 180.0f;
    public final static float LON_RANGE = 360.0f;
    // initialize to something sane
    protected float lat_ = 0.0f;
    protected float lon_ = 0.0f;

    /**
     * Construct a default LatLonPoint.
     */
    public LatLonPoint () {
    }

    /**
     * Construct a LatLonPoint from raw float lat/lon in decimal degrees.
     *
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     */
    public LatLonPoint (float lat, float lon) {
        lat_ = normalize_latitude(lat);
        lon_ = wrap_longitude(lon);
        radlat_ = EarthConstants.degToRad(lat_);
        radlon_ = EarthConstants.degToRad(lon_);
    }

    /**
     * Construct a LatLonPoint from raw float lat/lon in radians.
     *
     * @param lat latitude in radians
     * @param lon longitude in radians
     * @param isRadian placeholder indicates radians
     */
    public LatLonPoint (float lat, float lon, boolean isRadian) {
        radlat_ = lat;
        radlon_ = lon;
        lat_ = normalize_latitude(EarthConstants.radToDeg(radlat_));
        lon_ = wrap_longitude(EarthConstants.radToDeg(radlon_));
    }

    /**
     * Copy construct a LatLonPoint.
     *
     * @param pt LatLonPoint
     */
    public LatLonPoint (LatLonPoint pt) {
        lat_ = pt.lat_;
        lon_ = pt.lon_;
        radlat_ = pt.radlat_;
        radlon_ = pt.radlon_;
    }

    /**
     * Construct a LatLonPoint from raw double lat/lon.
     *
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     */
    public LatLonPoint (double lat, double lon) {
        this((float)lat, (float)lon);
    }

    /* uncomment to see how many are being used and thrown away...
     protected void finalize() {
     Debug.output("finalized " + this);
     }
     */

    /**
     * Returns a string representation of the object.
     * @return String representation
     */
    public String toString () {
        return  "[Lat.=" + lat_ + ",Lon.=" + lon_ + "]";
    }

    /**
     * Clone the LatLonPoint.
     * @return clone
     */
    public Object clone () {
        try {
            return  super.clone();
        } catch (CloneNotSupportedException e) {
	  // FIXME!!!! - something instead of assert...
	  //Assert.assert(false, "LatLonPoint: internal error!");
            return  null;       // statement not reached
        }
    }

    /**
     * Set latitude.
     * @param lat latitude in decimal degrees
     */
    public void setLatitude (float lat) {
        lat_ = normalize_latitude(lat);
        radlat_ = EarthConstants.degToRad(lat_);
    }

    /**
     * Set longitude.
     * @param lon longitude in decimal degrees
     */
    public void setLongitude (float lon) {
        lon_ = wrap_longitude(lon);
        radlon_ = EarthConstants.degToRad(lon_);
    }

    /**
     * Set latitude and longitude.
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     */
    public void setLatLon (float lat, float lon) {
        lat_ = normalize_latitude(lat);
        lon_ = wrap_longitude(lon);
        radlat_ = EarthConstants.degToRad(lat_);
        radlon_ = EarthConstants.degToRad(lon_);
    }

    /**
     * Set latitude and longitude.
     * @param lat latitude in radians
     * @param lon longitude in radians
     * @param isRadian placeholder indicates radians
     */
    public void setLatLon (float lat, float lon, boolean isRadian) {
        radlat_ = lat;
        radlon_ = lon;
        lat_ = normalize_latitude(EarthConstants.radToDeg(radlat_));
        lon_ = wrap_longitude(EarthConstants.radToDeg(radlon_));
    }

    /**
     * Set LatLonPoint.
     * @param llpt LatLonPoint
     */
    public void setLatLon (LatLonPoint llpt) {
        lat_ = llpt.lat_;
        lon_ = llpt.lon_;
        radlat_ = EarthConstants.degToRad(lat_);
        radlon_ = EarthConstants.degToRad(lon_);
    }

    /**
     * Get normalized latitude.
     * @return float latitude in decimal degrees
     * (-90&deg; &lt;= &phi; &lt;= 90&deg;)
     */
    public float getLatitude () {
        return  lat_;
    }

    /**
     * Get wrapped longitude.
     * @return float longitude in decimal degrees
     * (-180&deg; &lt;= &lambda; &lt;= 180&deg;)
     */
    public float getLongitude () {
        return  lon_;
    }

    /**
     * Determines whether two LatLonPoints are equal.
     * @param obj Object
     * @return Whether the two points are equal up to a tolerance of
     * 10<sup>-5</sup> degrees in latitude and longitude.
     */
    public boolean equals (Object obj) {
        final float TOLERANCE = 0.00001f;
        if (obj instanceof LatLonPoint) {
            LatLonPoint pt = (LatLonPoint)obj;
            return  (EarthConstants.approximately_equal(lat_, pt.lat_, TOLERANCE)
                    && EarthConstants.approximately_equal(lon_, pt.lon_,
                    TOLERANCE));
        }
        return  false;
    }

    /**
     * Hash the lat/lon value.
     * <p>
     * @return int hash value
     */
    public int hashCode () {
        return  EarthConstants.hashLatLon(lat_, lon_);
    }

    /**
     * Write object.
     * @param s DataOutputStream
     */
    public void write (DataOutputStream s) throws IOException {
        // Write my information
        s.writeFloat(lat_);
        s.writeFloat(lon_);
    }

    /**
     * Read object.
     * @param s DataInputStream
     */
    public void read (DataInputStream s) throws IOException {
        // HMMM.  do we really need to be safe here?
        lat_ = normalize_latitude(s.readFloat());
        lon_ = wrap_longitude(s.readFloat());
        radlat_ = EarthConstants.degToRad(lat_);
        radlon_ = EarthConstants.degToRad(lon_);
    }

    /**
     * Sets latitude to something sane.
     * @param lat latitude in decimal degrees
     * @return float normalized latitude in decimal degrees
     * (-90&deg; &lt;= &phi; &lt;= 90&deg;)
     */
    final public static float normalize_latitude (float lat) {
        if (lat > NORTH_POLE) {
            lat = NORTH_POLE;
        }
        if (lat < SOUTH_POLE) {
            lat = SOUTH_POLE;
        }
        return  lat;
    }

    /**
     * Sets longitude to something sane.
     * @param lon longitude in decimal degrees
     * @return float wrapped longitude in decimal degrees
     * (-180&deg; &lt;= &lambda; &lt;= 180&deg;)
     */
    final public static float wrap_longitude (float lon) {
        if ((lon < -DATELINE) || (lon > DATELINE)) {
            //System.out.print("LatLonPoint: wrapping longitude " + lon);
            lon += DATELINE;
            lon = lon%LON_RANGE;
            lon = (lon < 0) ? DATELINE + lon : -DATELINE + lon;
            //Debug.output(" to " + lon);
        }
        return  lon;
    }

    /**
     * Check if latitude is bogus.
     * Latitude is invalid if lat &gt; 90&deg; or if lat &lt; -90&deg;.
     * @param lat latitude in decimal degrees
     * @return boolean true if latitude is invalid
     */
    public static boolean isInvalidLatitude (float lat) {
        return  ((lat > NORTH_POLE) || (lat < SOUTH_POLE));
    }

    /**
     * Check if longitude is bogus.
     * Longitude is invalid if lon &gt; 180&deg; or if lon &lt; -180&deg;.
     * @param lon longitude in decimal degrees
     * @return boolean true if longitude is invalid
     */
    public static boolean isInvalidLongitude (float lon) {
        return  ((lon < -DATELINE) || (lon > DATELINE));
    }

    /**
     * Calculate the <code>radlat_</code> and <code>radlon_</code>
     * instance variables upon deserialization.
     * Also, check <code>lat_</code> and <code>lon_</code> for safety;
     * someone may have tampered with the stream.
     * @param stream Stream to read <code>lat_</code> and <code>lon_</code> from.
     */
    private void readObject (java.io.ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        lat_ = normalize_latitude(lat_);
        lon_ = wrap_longitude(lon_);
        radlat_ = EarthConstants.degToRad(lat_);
        radlon_ = EarthConstants.degToRad(lon_);
    }
    /**
     * Used by the projection code for read-only quick access.
     * This is meant for quick backdoor access by the projection library.
     * Modify at your own risk!
     * @see #lat_
     */
    public transient float radlat_ = 0.0f;
    /**
     * Used by the projection code for read-only quick access.
     * This is meant for quick backdoor access by the projection library.
     * Modify at your own risk!
     * @see #lon_
     */
    public transient float radlon_ = 0.0f;
} // LatLonPoint.java
