/*
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.tools.csmart.util;

/**
 * Interface containing constants for doing math on the Earth:
 * Computing distances between points largely
 * Constants here taken from
 * <a href="http://openmap.bbn.com">OpenMap</a><br>
 * Files: <code>com.bbn.openmap.proj.ProjMath, com.bbn.openmap.proj.MoreMath
 * com.bbn.openmap.proj.GreatCircle and com.bbn.openmap.proj.Planet</code>
 * <br>
 * Those files in turn rely in part on John Snyder's
 * <i>Map Projection --A Working Manual</i><br>
 *
 * Some routines taken from <a href="http://www.cougaar.org">Cougaar</a>
 * and the class <code>org.cougaar.glm.util.GeoUtils</code><br>
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 */
public final class EarthConstants {
  // From com.bbn.openmap.proj.MoreMath
  public final static float TWO_PI = (float)Math.PI*2.0f;

  // From com.bbn.openmap.proj.Planet
  // WGS84 / GRS80 datums
  public final static transient float polarRadiusMeters = 6356752.3142f;
  public final static transient float equatorialRadiusMeters = 6378137.0f;
  public final static transient float equatorialCircumferenceMeters = EarthConstants.TWO_PI * EarthConstants.equatorialRadiusMeters;
  
  // from org.cougaar.glm.util, by Ken A:
  // Modified to remove reliance on other Cougaar classes
  /**
   * Compute great-circle distance (expressed as float Kilometers)
   * between two points on globe (expressed as LatLonPoint)
   * @param LatLonPoint of first point
   * @param LatLonPoint of second point
   * @return double distance in KM of great-circle distance between points
   */
  public static float DistanceBetweenLatLonPoints(LatLonPoint position1,
						   LatLonPoint position2)
  {
    // Get distance as KM
    return EarthConstants.DistanceBetweenPoints(position1.getLatitude(),
						position1.getLongitude(),
						position2.getLatitude(),
						position2.getLongitude());
  }
  
  /**
   * Compute great-circle distance in KM between two points on globe
   * expressed as latitude and longitude.
   * @param float latitude of first point (decimal degrees)
   * @param float longitude of first point (decimal degrees)
   * @param float latitude of second point (decimal degrees)
   * @param float longitude of second point (decimal degrees)
   * @return float great-circle distance between two points in KM
   */
  public static float DistanceBetweenPoints(float latitude1,
					     float longitude1, 
					     float latitude2,
					     float longitude2)
  {
    // Convert arguments to Radians
    double lon1_rad = Math.toRadians(longitude1);
    double lat1_rad = Math.toRadians(latitude1);
    double lon2_rad = Math.toRadians(longitude2);
    double lat2_rad = Math.toRadians(latitude2);
    
    // Convert to 3-D Cartesian coordinates (X,Y,Z with earth center at 0,0,0)
    double node_1_x = Math.cos(lat1_rad)*Math.cos(lon1_rad);
    double node_1_y = Math.cos(lat1_rad)*Math.sin(lon1_rad);
    double node_1_z = Math.sin(lat1_rad);
    
    double node_2_x = Math.cos(lat2_rad)*Math.cos(lon2_rad);
    double node_2_y = Math.cos(lat2_rad)*Math.sin(lon2_rad);
    double node_2_z = Math.sin(lat2_rad);
    
    // Calculate Cross-Product
    double cross_x = (node_1_y * node_2_z) - (node_1_z * node_2_y);
    double cross_y = (node_1_z * node_2_x) - (node_1_x * node_2_z);
    double cross_z = (node_1_x * node_2_y) - (node_1_y * node_2_x);
    
    // Calculate the length of the Cross-Product
    double norm_cross = 
      Math.sqrt((cross_x * cross_x) + 
		(cross_y * cross_y) + 
		(cross_z * cross_z));
    
    // Calculate the Dot-Product
    double dot_product = 
      (node_1_x * node_2_x) + (node_1_y * node_2_y) + (node_1_z * node_2_z); 
    
    // Calculate the central angle
    double angle = Math.atan2(norm_cross, dot_product);

    // Calculate the great-circle distance
    float distance = (float)(EarthConstants.equatorialRadiusMeters * angle / 1000.0d);

    return distance;
  }

  // Great circle calculations
  // From Openmap: com.bbn.openmap.proj.GreatCircle:
  // uses LatLonPoint
  /**
   * Calculate spherical arc distance between two points.
   * <p>
   * Computes arc distance `c' on the sphere. equation
   * (5-3a). (0 &lt;= c &lt;= PI)
   * <p>
   * @param phi1 latitude in radians of start point
   * @param lambda0 longitude in radians of start point
   * @param phi latitude in radians of end point
   * @param lambda longitude in radians of end point
   * @return float arc distance `c' in radians
   */
  final public static float spherical_distance (float phi1,
						float lambda0,
						float phi,
						float lambda)
  {
    float pdiff = (float)Math.sin(((phi-phi1)/2));
    float ldiff = (float)Math.sin((lambda-lambda0)/2);
    float rval = (float)Math.sqrt((pdiff*pdiff) +
				  (float)Math.cos(phi1)*(float)Math.cos(phi)*(ldiff*ldiff));
    
    return 2.0f * (float)Math.asin(rval);
  }

  /**
   * Calculate spherical arc distance between two points.
   * <p>
   * Computes arc distance `c' on the sphere. equation
   * (5-3a). (0 &lt;= c &lt;= PI)
   * <p>
   * @param p1 <code>LatLonPoint</code> of first point
   * @param p2 <code>LatLonPoint</code> of second point
   * @return float arc distance `c' in radians
   */
  public final static float spherical_distance (LatLonPoint p1, LatLonPoint p2) {
    return spherical_distance(p1.radlat_, p1.radlon_, p2.radlat_, p2.radlon_);
  }

  /**
   * Calculate spherical arc distance between two points.
   * <p>
   * Computes arc distance `c' on the sphere. equation
   * (5-3a). (0 &lt;= c &lt;= PI)
   * <p>
   * @param lat1 latitude in degrees of start point
   * @param lon1 longitude in degrees of start point
   * @param lat2 latitude in degrees of end point
   * @param lon2 longitude in degrees of end point
   * @return float arc distance `c' in radians
   */
  public final static float spherical_distance_deg (float lat1, float lon1,
						    float lat2, float lon2) {
    return spherical_distance(EarthConstants.degToRad(lat1),
			      EarthConstants.degToRad(lon1),
			      EarthConstants.degToRad(lat2),
			      EarthConstants.degToRad(lon2));
  }
  
  /**
   * Calculate point at azimuth and distance from another point.
   * <p>
   * Returns a LatLonPoint at arc distance `c' in direction `Az'
   * from start point.
   * <p>
   * @param phi1 latitude in radians of start point
   * @param lambda0 longitude in radians of start point
   * @param c arc radius in radians (0 &lt; c &lt;= PI)
   * @param Az azimuth (direction) east of north (-PI &lt;= Az &lt; PI)
   * @return LatLonPoint
   */
  public final static LatLonPoint spherical_between(float phi1,
						    float lambda0,
						    float c,
						    float Az)
  {
    float cosphi1 = (float)Math.cos(phi1);
    float sinphi1 = (float)Math.sin(phi1);
    float cosAz = (float)Math.cos(Az);
    float sinAz = (float)Math.sin(Az);
    float sinc = (float)Math.sin(c);
    float cosc = (float)Math.cos(c);
    
    return new LatLonPoint(EarthConstants.radToDeg(
					(float)Math.asin(
						   sinphi1*cosc + cosphi1*sinc*cosAz)),
			   EarthConstants.radToDeg(
					(float)Math.atan2(
						   sinc*sinAz, cosphi1*cosc - sinphi1*sinc*cosAz)
					+ lambda0));
  }

  /**
   * Calculate point at azimuth and distance from another point.
   * <p>
   * Returns a LatLonPoint at arc distance `c' in direction `Az'
   * from start point.
   * <p>
   * @param point <code>LatLonPoint</code> of start
   * @param c arc radius in radians (0 &lt; c &lt;= PI)
   * @param Az azimuth (direction) east of north (-PI &lt;= Az &lt; PI) in radians
   * @return LatLonPoint
   */
  public final static LatLonPoint spherical_between(LatLonPoint point, float c, float Az) {
    return spherical_between(point.radlat_, point.radlon_, c, Az);
  }

  /**
   * Calculate point at azimuth and distance from another point.
   * <p>
   * Returns a LatLonPoint at arc distance `c' in direction `Az'
   * from start point.
   * <p>
   * @param lat latitude in degrees of start point
   * @param lon longitude in degrees of start point
   * @param c arc radius in radians (0 &lt; c &lt;= PI)
   * @param Az azimuth (direction) east of north (-PI &lt;= Az &lt; PI)
   * @return LatLonPoint
   */
  public final static LatLonPoint spherical_between_degs(float lat, float lon,
							 float c, float Az) {
    return spherical_between(EarthConstants.degToRad(lat),
			     EarthConstants.degToRad(lon), c, Az);
  }
  
  // From Openmaps' com.bbn.openmap.proj.ProjMath:
  /**
   * Convert radians to degrees.
   * @param rad radians
   * @return double decimal degrees
   */
  public final static double radToDeg (double rad) {
    return (rad * (180.0d / Math.PI));
  }  
  
  /**
   * Convert radians to degrees.
   * @param rad radians
   * @return float decimal degrees
   */
  public final static float radToDeg (float rad) {
    return (rad * (180.0f / (float)Math.PI));
  }  
  
  /**
   * Convert degrees to radians.
   * @param deg degrees
   * @return double radians
   */
  public final static double degToRad (double deg) {
    return (deg * (Math.PI / 180.0d));
  }  
  
  /**
   * Convert degrees to radians.
   * @param deg degrees
   * @return float radians
   */
  public final static float degToRad (float deg) {
    return (deg * ((float)Math.PI / 180.0f));
  }  

  /**
   * Convert the meters distance into arcRadius on the earth.
   * @param meters distance, should be <= the Earth's Circumference.
   * @return float radians arc radius (0 &lt; ret &lt;= PI)
   */
  public final static float arcRadius(float meters) {
    float arc = meters / EarthConstants.equatorialRadiusMeters;

    if(arc > Math.PI) {
      arc  = (float)(2 * Math.PI - arc);
    }

    return arc;
  }

  /**
   * Generate a hashCode value for a lat/lon pair.
   * @param lat latitude
   * @param lon longitude
   * @return int hashcode
   */
  public final static int hashLatLon (float lat, float lon) {
    if (lat == -0f) lat = 0f;//handle negative zero (anything else?)
    if (lon == -0f) lon = 0f;
    int tmp = Float.floatToIntBits(lat);
    int hash = (tmp<<5) | (tmp>>27);//rotate the lat bits
    return hash ^ Float.floatToIntBits(lon);//XOR with lon
  }

  
  // From com.bbn.openmap.proj.MoreMath
  /**
   * Checks if a ~= b.
   * Use this to test equality of floating point numbers.
   * <p>
   * @param a double
   * @param b double
   * @param epsilon the allowable error
   * @return boolean
   */
  public static final boolean approximately_equal(double a, double b, double epsilon) {
    return (Math.abs(a-b) <= epsilon);
  }
  
  /**
   * Checks if a ~= b.
   * Use this to test equality of floating point numbers.
   * <p>
   * @param a float
   * @param b float
   * @param epsilon the allowable error
   * @return boolean
   */
  public final static boolean approximately_equal(float a, float b, float epsilon) {
    return (Math.abs(a-b) <= epsilon);
  }
} // EarthConstants.java





