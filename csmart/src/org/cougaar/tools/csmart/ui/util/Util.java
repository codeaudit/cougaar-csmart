/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.util;

import org.cougaar.util.ConfigFinder;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JOptionPane;

/**
 * Utility class for connecting to societies via PSPs.
 */

public class Util {
  //  private static final String PSP_PACKAGE = "csmart/monitor";

  // servlet to get the URLs of all clusters in the society
  private static final String PSP_CLUSTERPROVIDER = 
    "CSMART_ClusterProviderServlet";

  /**
   * Contact the PSP: "csmart/monitor/CLUSTER_URLS.PSP" at the specified URL.
   * The PSP_package used in all these methods is "csmart/monitor".
   * Get URLs of all clusters in that society.
   * This may return null or a vector with zero elements;
   * in these cases, appropriate error messages are displayed for the user.
   * @param clusterURL  the URL of the cluster to contact
   * @return            vector of String; URLs of clusters in society
   */

  public static Vector getClusters(String clusterURL) {
    Vector clusterURLs = null;
    //    String PSP_id = "CLUSTER_URLS.PSP";
    //    String URLSpec = clusterURL + PSP_PACKAGE + "/" + PSP_CLUSTERPROVIDER;
    String URLSpec = clusterURL + "/" + PSP_CLUSTERPROVIDER;
    try {
      URL url = new URL(URLSpec);
      URLConnection connection = url.openConnection();
      connection.setDoInput(true);
      connection.setDoOutput(true);
      // get cluster URLs from PSP
      InputStream is = connection.getInputStream();
      ObjectInputStream p = new ObjectInputStream(is);
      clusterURLs = (Vector)p.readObject();
      if (clusterURLs == null)
	JOptionPane.showMessageDialog(null,
				      URLSpec +
				      " returned null; no information to graph.");
      else if (clusterURLs.size() == 0)
	JOptionPane.showMessageDialog(null,
				      URLSpec +
				      " returned no agents; no information to graph.");
    } catch (Exception e) {
      System.out.println("Failed to contact: " + URLSpec + " " + e);
      JOptionPane.showMessageDialog(null,
				    "Failed to contact: " + URLSpec +
				    "; no information to graph.");
    }
    return clusterURLs;
  }

  /**
   * Get information from the PSPs at the specified clusters.
   * There is one entry in the returned vector for each cluster.
   * If the cluster is unreachable or returns null,
   * the returned vector contains a null entry for that cluster.
   * If the PSP returns a vector, then
   * this returns a vector of vectors.
   * @param clusterURLs  URLs of clusters to contact, vector of String
   * @param PSP_id       id of the PSP to contact
   * @return             information from the PSPs; vector of Object
   */

//    public static Vector getDataFromClusters(Vector clusterURLs,
//  					   String PSP_id) {
//      Vector results = new Vector();
//      for (int i = 0; i < clusterURLs.size(); i++) {
//        Object tmp = getDataFromCluster((String)clusterURLs.elementAt(i),
//   				      PSP_id);
//        results.add(tmp);
//      }
//      return results;
//    }

  /**
   * Get information from the specified PSP at the specified cluster.
   * If the cluster or PSP cannot be contacted, this returns null.
   * @param clusterURL  the cluster to contact
   * @param PSP_id      the id of the PSP to contact
   * @return            the object returned by the PSP or null
   */

//    private static Object getDataFromCluster(String clusterURL, String PSP_id) {
//      Object results = null;
//      //    String URLSpec = clusterURL + PSP_PACKAGE + "/" + PSP_id;
//      String URLSpec = clusterURL + "/" + PSP_id;
//      try {
//        URL url = new URL(URLSpec);
//        URLConnection connection = url.openConnection();
//        connection.setDoInput(true);
//        connection.setDoOutput(true);
//        // read events from PSP
//        InputStream is = connection.getInputStream();
//        ObjectInputStream p = new ObjectInputStream(is);
//        results = p.readObject();
//      } catch (Exception e) {
//        System.out.println("Failed to contact: " + URLSpec + " " + e);
//      }
//      return results;
//    }

  /**
   * Equivalent to 
   *  <tt>getCollectionFromClusters(clusterURLs, PSP_id, filter, -1)</tt>.
   */
  public static Collection getCollectionFromClusters(Vector clusterURLs,
						     String PSP_id,
						     String filter) {
    return getCollectionFromClusters(clusterURLs, PSP_id, filter, -1);
  }

  /**
   * Get a filtered collection from specified clusters and PSP.  
   * The PSP must be one that returns a Collection.
   * The filter is an operator predicate which is used by the PSP
   * to determine what objects to include in the collection.
   * Returns null if the cluster of PSP cannot be contacted.
   * @param clusterURLs the clusters to contact; vector of String
   * @param PSP_id      the PSP to contact
   * @param filter      the filter the PSP will use to collect objects
   * @param limit       the limit for the number of objects to return, or -1 if
   *                       there is no limit -- the PSP must accept "?limit=N"
   * @return            the collection from the PSP or null
   */

  public static Collection getCollectionFromClusters(
      Vector clusterURLs,
      String PSP_id,
      String filter,
      int limit) {
    Collection results = null;
    boolean hasLimit = (limit >= 0);

    // limit that is decremented for each call
    int remainingLimit = (hasLimit ? limit : -1);

    int nClusterURLs = 
      ((clusterURLs != null)  ?
       (clusterURLs.size()) :
       (-1));

    //
    // Sort clusterURLS by the agent names?  For now we've lost
    //   the agent name and only have the raw URL...
    //
    // This method signature should be fixed to be:
    //   (InetAddress societyAddress, Set agentNames, String PSP_id, ..)
    //

    for (int i = 0; i < nClusterURLs; i++) {
      String urli = (String)clusterURLs.elementAt(i);

      // query
      Collection coli = 
	getCollectionFromCluster(
            urli, PSP_id, filter, remainingLimit);

      int ncoli = ((coli != null) ? coli.size() : -1);
      if (ncoli <= 0) {
        // no results
        //
        // getCollectionFromCluster(..) did the popup warning
        //
        // do we want N popups if every query fails?
        continue;
      }

      // append to our results
      if (results == null) {
        results = coli;
      } else {
        try {
          results.addAll(coli);
        } catch (Exception e) {
          // shouldn't happen
          System.out.println(
              "Unable to add to results: "+e);
        }
      }

      // check our limit
      if (hasLimit) {
        // decrement remaining limit
        remainingLimit -= ncoli;
        if (remainingLimit < 0) {
          // exceeded the limit by one
          //
          // create a popup warning:
          JOptionPane.showMessageDialog(null,
              "Exceeded limit of "+limit+
              " objects; producing a trimmed graph from "+
              (i+1)+" of "+nClusterURLs+" Agents.");
          // can add to message the agent names that were:
          //   - fully-queried agents
          //   - current partially-queried agent
          //   - not-queried agents.
          //
          // also can consider an option to ask the user if they'd 
          //   like to increase the limit.  This would require 
          //   recomputing the current query[i], since it was 
          //   limited.
          break;
        }
      }
    }

    return results;
  }
  
  /**
   * Equivalent to
   *  <tt>getCollectionFromCluster(clusterURL, PSP_id, filter, -1)</tt>.
   */
  public static Collection getCollectionFromCluster(String clusterURL,
						    String PSP_id,
						    String filter) {
    return getCollectionFromCluster(clusterURL, PSP_id, filter, -1);
  }

  /**
   * Get a filtered collection from a single cluster and PSP.  
   * The PSP must be one that returns a Collection.
   * The filter is an operator predicate which is used by the PSP
   * to determine what objects to include in the collection.
   * Returns null if the cluster of PSP cannot be contacted.
   * If a PSP returns null, or can't be contacted,
   * an error message is displayed for the user.
   * @param clusterURL  the cluster to contact
   * @param PSP_id      the PSP to contact
   * @param filter      the filter the PSP will use to collect objects
   * @param limit       the limit for the number of objects to return, or -1 if
   *                       there is no limit -- the PSP must accept "?limit=N"
   * @return            the collection from the PSP or null
   */

  public static Collection getCollectionFromCluster(String clusterURL,
						    String PSP_id,
						    String filter,
						    int limit) {
    System.out.println("Util: getCollectionFromCluster: " + PSP_id);
    Collection results = null;
    //    String URLSpec = clusterURL + PSP_PACKAGE + "/" + PSP_id;
    String URLSpec = clusterURL + "/" + PSP_id;
    String agent = URLSpec;
    if (filter != null)
      URLSpec = "?operatorPredicate=" + URLEncoder.encode(filter);
    if (limit >= 0) {
      URLSpec += "?limit=" + limit;
    }
    //    System.out.println("Util: Connecting to society: " + URLSpec);
    try {
      URL url = new URL(URLSpec);
      URLConnection connection = url.openConnection();
      connection.setDoInput(true);
      connection.setDoOutput(true);
      // read events from PSP
      InputStream is = connection.getInputStream();
      ObjectInputStream p = new ObjectInputStream(is);
      Object o = p.readObject();
      System.out.println("Util: " + o.getClass().getName());
      results = (Collection)o;
      //results = (Collection)p.readObject();
      if (results == null || results.isEmpty()) {
	results = null;
	JOptionPane.showMessageDialog(null,
            agent + " returned null; no information to graph from this agent.");
      }
    } catch (Exception e) {
      System.out.println("Util: Failed to contact: " + URLSpec + " " + e);
      JOptionPane.showMessageDialog(null,
          "Failed to contact: " + agent + 
          "; no information to graph from this agent.");
      return null;
    }
    //    System.out.println("Received events: " + results.size() +
    //		       " from cluster: " + clusterURL);
    return results;
  }

  /**
   * Return a path for the specified filename; the path is determined
   * using ConfigFinder.  If there is any error, null is returned.
   * @param filename the filename for which to get the path
   * @return the pathname or null if an error
   */

  public static String getPath(String filename) {
    ConfigFinder configFinder = ConfigFinder.getInstance();
    File file = configFinder.locateFile(filename);
    String path = null;
    try {
      path = file.getCanonicalPath();
    } catch (Exception e) {
      //      System.out.println("Could not find: " + filename);
    }
    return path;
  }

  /**
   * Get the single NamedFrame object for the CSMARTUL application.
   * The NamedFrame object ensures that all frames in the application
   * have unique names, and it notifies the CSMARTUL application when frames
   * are added/removed so that the menu of existing frames can be updated.
   * This method is pointless.
   * @deprecated The singleton NamedFrame should be obtained directly
   * using NamedFrame.getNamedFrame().
   * @return the NamedFrame object for the application
   **/
  public static NamedFrame getNamedFrame() {
    return NamedFrame.getNamedFrame();
  }

}




