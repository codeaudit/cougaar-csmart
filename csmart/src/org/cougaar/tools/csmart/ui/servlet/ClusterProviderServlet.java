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

import org.cougaar.util.UnaryPredicate;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.http.HttpUtils;

import org.cougaar.core.servlet.ServletUtil;
import org.cougaar.core.servlet.SimpleServletSupport;
import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;

import java.io.*;
import java.util.Collections;
import java.util.Vector;

import java.io.ObjectOutputStream;
import java.io.PrintStream;

/**
 * This PSP expects no input, and returns the URLs of all the clusters
 * as a serialized object which is a Vector of Strings.
 * It is used by the CSMARTUL applications to obtain the URLs of the clusters.
 * <pre>
 * Invoke as: CLUSTER_URLS.PSP
 * Returns: URLs, serialized Vector of Strings
 * </pre>
 */

public class ClusterProviderServlet 
extends HttpServlet
{
  private SimpleServletSupport support;
  
  public ClusterProviderServlet(SimpleServletSupport support) {
    super();
    this.support = support;
  }

  public void doGet(
		    HttpServletRequest request,
		    HttpServletResponse response) throws IOException, ServletException
  {
    // create a new "ClusterProvider" context per request
    ClusterProvider cp = new ClusterProvider(support);
    cp.execute(request, response);  
  }
  
  public void doPost(
		     HttpServletRequest request,
		     HttpServletResponse response) throws IOException, ServletException
  {
    // create a new "ClusterViewer" context per request
    ClusterProvider cp = new ClusterProvider(support);
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
  private static class ClusterProvider {
    
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
    
    public ClusterProvider(SimpleServletSupport support) {
      this.support = support;
    }
    
    /**
     * Main method.
     *
     * Called when a request is received from a client.
     * Get the POST data; parse the request; get the objects
     * that match the request; send them to the client.
     */
    public void execute(  HttpServletRequest request, 
			  HttpServletResponse response) throws IOException, ServletException 
    {
      
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
      
      // get the list of urls and return them 
      try {
	Vector urls = new Vector();
	Vector names = new Vector();

	StringBuffer buf = HttpUtils.getRequestURL(request);
	String url = buf.toString();
	System.out.println("URL: " + url);
	
	// get all agent names; reconstruct the urls and return
	getAllNamesAndUrls(urls, names, url, false);
	
	System.out.println("Got urls: " + urls);
	// send the urls to the client
	ObjectOutputStream p = new ObjectOutputStream(out);
	p.writeObject(urls);
	System.out.println("Sent cluster urls");
      } catch (Exception e) {
	System.out.println("Exception: " + e);
      }
    }
    
    /**
     * helper methods
     **/
    
    public void getAllNamesAndUrls(Vector urls, Vector names, String url, boolean sortByName) {
      if (urls == null) {
	return;
      }
      
      // get http://localhost:port/$
      url = url.substring(0, url.indexOf('$')+1); 
      
      support.getAllEncodedAgentNames(names);
      if(sortByName)
	Collections.sort(names);
      
      for(int i = 0; i < names.size(); i++) {
	String alias = (String)names.elementAt(i);
	String u = url+alias+'/';
	urls.addElement(u);
      }
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
}

