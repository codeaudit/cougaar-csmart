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

package org.cougaar.tools.csmart.ui.psp;

import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.lib.planserver.HttpInput;
import org.cougaar.lib.planserver.PlanServiceContext;
import org.cougaar.lib.planserver.PlanServiceProvider;
import org.cougaar.lib.planserver.PlanServiceUtilities;
import org.cougaar.lib.planserver.PSP_BaseAdapter;
import org.cougaar.lib.planserver.RuntimePSPException;
import org.cougaar.lib.planserver.UISubscriber;

import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Vector;

/**
 * This PSP expects no input, and returns the URLs of all the clusters
 * as a serialized object which is a Vector of Strings.
 * It is used by the CSMARTUL applications to obtain the URLs of the clusters.
 * <pre>
 * Invoke as: CLUSTER_URLS.PSP
 * Returns: URLs, serialized Vector of Strings
 * </pre>
 */

public class PSP_ClusterProvider extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {

  public PSP_ClusterProvider() {
    super();
  }

  public PSP_ClusterProvider(String pkg, String id) throws RuntimePSPException
  {
    setResourceLocation(pkg, id);
  }

  public boolean test(HttpInput query_parameters, PlanServiceContext sc)
  {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }

  /*
    Called when a request is received from a client.
    Get the POST data; parse the request; get the objects
    that match the request; send them to the client.
  */

  public void execute( PrintStream out,
		       HttpInput query_parameters,
		       PlanServiceContext psc,
		       PlanServiceUtilities psu) throws Exception {

    try {
      Vector urls = new Vector();
      Vector names = new Vector();
      if (psc == null) {
	System.out.println("PSC IS NULL");
	return;
      }
      // get cluster names and urls
      psc.getAllURLsAndNames(urls, names);
      System.out.println("Got urls: " + urls);
      // send the urls to the client
      ObjectOutputStream p = new ObjectOutputStream(out);
      p.writeObject(urls);
      System.out.println("Sent cluster urls");
    } catch (Exception e) {
      System.out.println("Exception: " + e);
    }
  }

  /* The UISubscriber interface.
     This PSP doesn't care if subscriptions change
     because it treats each request as a new request.
  */

  public void subscriptionChanged(Subscription subscription) {
  }

  public boolean returnsXML()
  {
    return false;
  }

  public boolean returnsHTML() 
  {
    return false;
  }

  /**  Any PlanServiceProvider must be able to provide DTD of its
   *  output IFF it is an XML PSP... ie.  returnsXML() == true;
   *  or return null
   **/

  public String getDTD() 
  {
    return null;
  }
}

