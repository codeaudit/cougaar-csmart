/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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
import java.io.IOException;
import java.net.URL;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;

import org.cougaar.core.servlet.ServletUtil;
import org.cougaar.core.servlet.SimpleServletSupport;
import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.asset.CommunityPG;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.HasRelationships;

import org.cougaar.tools.csmart.ui.monitor.PropertyNames; 
import org.cougaar.util.PropertyTree;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * Expects no input
 * Returns agent name and community name from Entity object
 */
public class CommunityProviderServlet 
  extends HttpServlet
{
  private SimpleServletSupport support;
  private transient Logger log;

  public CommunityProviderServlet(SimpleServletSupport support) {
    super();
    this.support = support;
    log = CSMART.createLogger("org.cougaar.tools.csmart.ui.servlet");
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
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.ui.servlet");

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
        if(log.isDebugEnabled()) {
          log.debug("CSMART_CommunityProviderServlet received query..........");
        }
	StringBuffer buf = HttpUtils.getRequestURL(request);
	Vector collection = getSelfInformation(buf);
	//out.print(collection);
	
	ObjectOutputStream p = new ObjectOutputStream(out);
	p.writeObject(collection);
        if(log.isDebugEnabled()) {
          log.debug("Sent cluster urls");
        }
      } catch (Exception e) {
        if(log.isDebugEnabled()) {
          log.error("CSMART_CommunityProviderServlet Exception", e);
          e.printStackTrace();
        }
      }
    }

    /**
     * Get object which describes this agent and its community.
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
     * Get agent and community name for this agent.
     * Returns a vector which contains a single PropertyTree which contains the
     * properties for this agent.
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
	// THIS METHOD OF ACCESSING AGENT NAME MUST MATCH HOW OTHER ServletS
	// (ESPECIALLY CSMART_PlanServlet) ACCESS THE AGENT NAME SO COMPARISONS
	// CAN BE MADE AT THE CLIENT
	String name = support.getEncodedAgentName();
	properties.put(PropertyNames.AGENT_NAME, name);
	CommunityPG communityPG = asset.getCommunityPG(new Date().getTime());
	String communityName = null;
	if (communityPG != null) {
	  Collection communities = communityPG.getCommunities();
	  if (communities.size() > 1) 
            if(log.isDebugEnabled()) {
              log.warn("CSMART_CommunityProviderServlet: WARNING: " + 
                       "handling agents in multiple communities is not implemented.");
            }
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
  }
}

