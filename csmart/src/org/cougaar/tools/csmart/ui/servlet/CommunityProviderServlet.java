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

package org.cougaar.tools.csmart.ui.servlet;

import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;

import java.io.*;
import java.util.Collections;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.http.HttpUtils;

import org.cougaar.core.servlet.ServletUtil;
import org.cougaar.core.servlet.SimpleServletSupport;
import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;

import org.cougaar.tools.csmart.ui.monitor.PropertyNames; 
import org.cougaar.util.PropertyTree;
import org.cougaar.util.UnaryPredicate;

import java.io.ObjectOutputStream;

/**
 * Expects no input
 * Returns agent name and community name from Entity object
 */

public class CommunityProviderServlet 
  extends HttpServlet
{
  private SimpleServletSupport support;
  
  public CommunityProviderServlet(SimpleServletSupport support) {
    super();
    this.support = support;
  }

  public void doGet(
		    HttpServletRequest request,
		    HttpServletResponse response) throws IOException, ServletException
  {
    // create a new "CommunityProvider" context per request
    CommunityProvider cp = new CommunityProvider(support);
    cp.execute(request, response);  
  }
  
  public void doPost(
		     HttpServletRequest request,
		     HttpServletResponse response) throws IOException, ServletException
  {
    // create a new "CommunityProvider" context per request
    CommunityProvider cp = new CommunityProvider(support);
    cp.execute(request, response);  
  }
  
  
  /**
   * This inner class does all the work.
   * <p>
   * A new class is created per request, to keep all the
   * instance fields separate.  If there was only one
   * instance then multiple simultaneous requests would
   * corrupt the instance fields (e.g. the "out" stream).
   * <p>
   * This acts as a <b>context</b> per request.
   */
  private static class CommunityProvider {
    
    /*
     * parameters from the URL:
     */
    
    ServletOutputStream out; 

    /* since "ClusterProvider" is a static inner class, here
     * we hold onto the support API.
     *
     * this makes it clear that ClusterProvider only uses
     * the "support" from the outer class.
     */      
    private SimpleServletSupport support;
    
    // inner class constructor
    public CommunityProvider(SimpleServletSupport support) {
      this.support = support;
    }
    
   
  /**
   * Called when a request is received from a client.
   * Get the POST data; parse the request; get the objects
   * that match the request; send them to the client.
   */
    
    public void execute( HttpServletRequest request, 
			 HttpServletResponse response) throws IOException, ServletException 
    {
      
      // this.out = response.getWriter();
      this.out = response.getOutputStream();

      // create a URL parameter visitor
      ServletUtil.ParamVisitor vis = 
        new ServletUtil.ParamVisitor() {
	    public void setParam(String name, String value) {
	      /* There is no mode, as it only returns the list of agent
	       * names, so do nothing but set up for parameter parsing.   
	       */	  
	    }
	  };
      
      // visit the URL parameters
      ServletUtil.parseParams(vis, request);
      
      
      // need try/catch here or caller sends exceptions to client as html
      try {
	System.out.println("PSP_CommunityProvider received query..........");
	StringBuffer buf = HttpUtils.getRequestURL(request);
	Vector collection = getSelfInformation(buf);
	//out.print(collection);
	
	ObjectOutputStream p = new ObjectOutputStream(out);
	p.writeObject(collection);
	System.out.println("Sent cluster urls");
      } catch (Exception e) {
	System.out.println("PSP_CommunityProvider Exception: " + e);
	e.printStackTrace();
      }
    }

  /**
   * Get object which describes this cluster and its community.
   */
  
  private static UnaryPredicate getSelfPred() {
    return new UnaryPredicate() {
	public boolean execute(Object obj) {
	  if (obj instanceof Asset) {
	    Asset asset = (Asset)obj;
	    if ((asset instanceof HasRelationships) &&
		((HasRelationships)asset).isLocal() &&
		asset.hasClusterPG())
	      return true;
	  }
	  return false;
	}
      };
  }
    
  /**
   * Get cluster and community name for this cluster.
   * Returns a vector which contains a single PropertyTree which contains the
   * properties for this cluster.
   */
  
  private Vector getSelfInformation(StringBuffer buf) {
    Collection container = 
      support.queryBlackboard(getSelfPred());
    Iterator iter = container.iterator();
    Vector results = new Vector(1);
    int n = 0; // unique index for relationships
    while (iter.hasNext()) {
      Asset asset = (Asset)iter.next();
      PropertyTree properties = new PropertyTree();
      properties.put(PropertyNames.UID_ATTR, getUIDAsString(asset.getUID()));
      //      String name = asset.getItemIdentificationPG().getNomenclature();
      // THIS METHOD OF ACCESSING AGENT NAME MUST MATCH HOW OTHER PSPS
      // (ESPECIALLY PSP_PLAN) ACCESS THE AGENT NAME SO COMPARISONS
      // CAN BE MADE AT THE CLIENT
      String name = support.getEncodedAgentName();
      //String name = psc.getServerPlugInSupport().getClusterIDAsString(); 
      properties.put(PropertyNames.AGENT_NAME, name);
      CommunityPG communityPG = asset.getCommunityPG(new Date().getTime());
      String communityName = null;
      if (communityPG != null) {
	Collection communities = communityPG.getCommunities();
	if (communities.size() > 1) 
	  System.out.println("PSP_CommunityProvider: WARNING: handling agents in multiple communities is not implemented.");
	Iterator i = communities.iterator();
	while (i.hasNext()) {
	  communityName = (String)i.next();
	  break;
	}
      } else {
	communityName = "COUGAAR";
      }
      if (communityName != null)
	properties.put(PropertyNames.AGENT_COMMUNITY_NAME, communityName);
      
      // reconstruct url
      String url = buf.toString();
      //URL url = psc.lookupURL(psc.getServerPlugInSupport().getClusterIDAsString());
      if (url != null)
	properties.put(PropertyNames.AGENT_URL, url.toString());
      results.add(properties);
    }
    return results;
  }
  
  private static final String getUIDAsString(final UID uid) {
    return
      ((uid != null) ? uid.toString() : "null");
  }
  
  /**
   * The UISubscriber interface.
   * This PSP doesn't care if subscriptions change
   * because it treats each request as a new request.
   
  public void subscriptionChanged(Subscription subscription) {
  }
  */
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
}

