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

import java.net.URL;
import java.net.URLConnection;

import java.util.Collection;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

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
import org.cougaar.planning.ldm.asset.LocationSchedulePG;

import org.cougaar.planning.ldm.plan.*;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.PropertyTree;

import org.cougaar.tools.csmart.ui.monitor.PropertyNames;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * This Servlet gathers information about agents and their relationships.
 * The information is encoded in name/value pairs stored in PropertyTree
 * objects which are serialized to the client. <br>
 *
 * <p>
 * Can be loaded manually by including this line in an agent's .ini configuration file: <pre/>
 *   plugin = org.cougaar.core.servlet.SimpleServletComponent(org.cougaar.tools.csmart.ui.servlet.AgentInfoServlet, 
 *   /CSMART_AgentInfoServlet) </pre>
 *
 * <p>
 * Is loaded from a URL on a CSMART machine, on agent 'Agent': <br>
 *   http://localhost:port/$Agent/CSMART_AgentInfoServlet
 */
public class AgentInfoServlet 
  extends HttpServlet 
{
  public static final Role SELF_ROLE = Role.getRole("Self");
  
  private SimpleServletSupport support;
  
  public AgentInfoServlet(SimpleServletSupport support) {
    super();
    this.support = support;
  }
  
  public void doGet(
		    HttpServletRequest request,
		    HttpServletResponse response) throws IOException, ServletException
  {
    // create a new "AgentInfo" context per request
    AgentInfo ai = new AgentInfo(support);
    ai.execute(request, response);  
  }
  
  public void doPut(
		     HttpServletRequest request,
		     HttpServletResponse response) throws IOException, ServletException
  {
    // create a new "AgentInfo" context per request
    AgentInfo ai = new AgentInfo(support);
    ai.execute(request, response);  
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
  private static class AgentInfo {
    
    /*
     * parameters from the URL:
     */
    ServletOutputStream out; 
    
    /* since "AgentInfo" is a static inner class, here
     * we hold onto the support API.
     *
     * this makes it clear that AgentInfo only uses
     * the "support" from the outer class.
     */      
    private SimpleServletSupport support;
    
    public AgentInfo(SimpleServletSupport support) {
      this.support = support;
    }
    
    /**
     * This is the main Servlet method called by the infrastructure in response
     * to receiving a request from a client.
     * Get information about this agent and its relationships
     * and return these to the client in a serialized PropertyTree.
     */
    public void execute(  HttpServletRequest request, 
			  HttpServletResponse response) throws IOException, ServletException 
    {  
      
      //this.out = response.getWriter();
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
      
      try {
	Vector collection = getSelfInformation();
	if (collection!=null)
	  {
	    ObjectOutputStream p = new ObjectOutputStream(out);
	    p.writeObject(collection);
	  }
      } catch (Exception e) {
	System.out.println("CSMART_AgentInfoServlet Exception: " + e);
	e.printStackTrace();
      }
    }
    
    
    /**
     * Get information about this agent by getting assets that:
     * are instances of HasRelationships and
     * have a community property group and
     * have a cluster property group
     * and isLocal
     */
    private static UnaryPredicate getSelfPred() {
      return new UnaryPredicate() {
	  public boolean execute(Object o) {
	    if (o instanceof Asset) {
	      Asset asset = (Asset)o;
	      if ((asset instanceof HasRelationships) &&
		  ((HasRelationships)asset).isLocal() &&
		  (asset.hasClusterPG()))
		return true;
	    }
	    return false;
	  }
	};
    }
    
    /**
     * Get information about this agent including community name,
     * roles and relationships.
     * Returns a vector which contains a single PropertyTree which contains the
     * properties for this agent.
     */
    private Vector getSelfInformation() { 
      Collection container = 
	support.queryBlackboard(getSelfPred());
      Iterator iter = container.iterator();
      Vector results = new Vector(1);
      int n = 0; // unique index for relationships
      while (iter.hasNext()) {
	Asset asset = (Asset)iter.next();
	PropertyTree properties = new PropertyTree();
	// treat this like an organization at the client end
	properties.put(PropertyNames.OBJECT_TYPE, 
		       PropertyNames.ORGANIZATION_OBJECT);
	properties.put(PropertyNames.UID_ATTR,
		       getUIDAsString(asset.getUID()));
	String assetKeyName = asset.getKey().toString();
	if (assetKeyName == null)
	  return results;
	properties.put(PropertyNames.ORGANIZATION_KEY_NAME, TranslateUtils.trimAngles(assetKeyName));
	properties.put(PropertyNames.ORGANIZATION_NAME,
		       asset.getItemIdentificationPG().getNomenclature());
	RelationshipSchedule schedule = 
	  ((HasRelationships)asset).getRelationshipSchedule();
	Iterator schedIter = new ArrayList(schedule).iterator();
	PropertyTree uniqueRelationships = new PropertyTree();
	while (schedIter.hasNext()) {
	  Relationship relationship = (Relationship)schedIter.next();
	  // skip relationships with self
	  if ((relationship.getRoleA().equals(SELF_ROLE)) ||
	      (relationship.getRoleB().equals(SELF_ROLE)))
	    continue;
	  Object otherObject = schedule.getOther(relationship);
	  // skip relationships with non-assets; does this occur???
	  if (!(otherObject instanceof Asset))
	    continue;
	  // get the name of the organization this one is related to;
	  // on the client side, this is compared to organization name
	  // so use the same getter
	  String relatedTo = ((Asset)otherObject).getKey().toString();
	  if (relatedTo == null)
	    continue;
	  // filter duplicates
	  relatedTo = TranslateUtils.trimAngles(relatedTo);
	  String rel = schedule.getOtherRole(relationship).getName();
	  String entry = (String)uniqueRelationships.get(rel+relatedTo);
	  if (entry != null && entry.equals(relatedTo))
	    continue;
	  uniqueRelationships.put(rel+relatedTo, relatedTo);
	  // property is related_to_n_name
	  properties.put(PropertyNames.ORGANIZATION_RELATED_TO + 
			 "_" + n++ + "_" + rel,
			 relatedTo);
	}
	// add location schedule
	LocationSchedulePG locSchedPG;
	Schedule locSched;
	if (((locSchedPG = asset.getLocationSchedulePG()) != null) &&
	    ((locSched = locSchedPG.getSchedule()) != null) &&
	    (!(locSched.isEmpty()))) {
	  List locPTs = new ArrayList();
	  Enumeration locSchedEn = locSched.getAllScheduleElements();
	  boolean includeStartEndTime = false;
	  int locNumber = 0;
	  while (locSchedEn.hasMoreElements()) {
	    Object oi = locSchedEn.nextElement();
	    if (!(oi instanceof LocationScheduleElement)) {
	      continue;
	    }
	    LocationScheduleElement lse = (LocationScheduleElement)oi;
	    Location loc = lse.getLocation();
	    if (!(loc instanceof LatLonPoint)) {
	      continue;
	    }
	    LatLonPoint lseLoc = (LatLonPoint)loc;
	    if ((lseLoc.getLatitude() == null) ||
		(lseLoc.getLongitude() == null)) {
	      continue;
	    }
	    properties.put(PropertyNames.ORGANIZATION_LOCATION_ELEMENT_START_TIME + "_" + locNumber,
			   new Long(lse.getStartTime()));
	    properties.put(PropertyNames.ORGANIZATION_LOCATION_ELEMENT_END_TIME + "_" + locNumber,
			   new Long(lse.getEndTime()));
	    properties.put(PropertyNames.ORGANIZATION_LOCATION_ELEMENT_LATITUDE + "_" + locNumber,
			   new Double(lseLoc.getLatitude().getDegrees()));
	    properties.put(PropertyNames.ORGANIZATION_LOCATION_ELEMENT_LONGITUDE + "_" + locNumber,
			   new Double(lseLoc.getLongitude().getDegrees()));
	    properties.put(PropertyNames.ORGANIZATION_LOCATION_ELEMENT_VERBOSE + "_" + locNumber,
			   lseLoc.toString());
	    includeStartEndTime = true;
	    locNumber++;
	  }
	  if (includeStartEndTime) {
	    properties.put(PropertyNames.ORGANIZATION_LOCATION_START_TIME,
			   new Long(locSched.getStartTime()));
	    properties.put(PropertyNames.ORGANIZATION_LOCATION_END_TIME,
			   new Long(locSched.getEndTime()));
	    includeStartEndTime = false;
	  }
	}
	int nProperties = properties.size();
	results.add(properties);
      }
      return results;
    }
    
    private String getUIDAsString(UID uid) {
      if (uid == null)
	return "null";
      return uid.getOwner() + "/" + uid.getId();
    }
  }
}
