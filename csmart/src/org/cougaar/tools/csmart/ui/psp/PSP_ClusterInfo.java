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

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.cougaar.core.cluster.*;
import org.cougaar.core.society.UID;
import org.cougaar.core.util.*;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.Entity;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.plan.HasRelationships;
import org.cougaar.domain.planning.ldm.plan.Relationship;
import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.lib.planserver.*;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.util.*;


/**
 * This PSP gathers information about clusters and their relationships.
 * The information is encoded in name/value pairs stored in PropertyTree
 * objects which are serialized to the client.
 */

public class PSP_ClusterInfo extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {

  public static final Role SELF_ROLE = Role.getRole("Self");

  public PSP_ClusterInfo() {
    super();
  }

  public PSP_ClusterInfo( String pkg, String id ) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  /**
   * Get information about this cluster by getting assets that:
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
	      //	      (asset.hasCommunityPG(new Date().getTime())) &&
	      (asset.hasClusterPG()))
	    return true;
	}
	return false;
      };
    };
  }

  /**
   * This is the main PSP method called by the infrastructure in response
   * to receiving a request from a client.
   * Get information about this cluster and its relationships
   * and return these to the client in a serialized PropertyTree.
   */

  public void execute(PrintStream out,
		      HttpInput query_parameters,
		      PlanServiceContext psc,
		      PlanServiceUtilities psu) throws Exception {
    try {
      System.out.println("PSP_ClusterInfo received query..........");
      Vector collection = getSelfInformation(psc);
      ObjectOutputStream oos = new ObjectOutputStream(out);
      oos.writeObject(collection);
    } catch (Exception e) {
      System.out.println("PSP_ClusterInfo Exception: " + e);
      e.printStackTrace();
    }
  }

  /**
   * Get information about this cluster including community name,
   * roles and relationships.
   * Returns a vector which contains a single PropertyTree which contains the
   * properties for this cluster.
   */

  private Vector getSelfInformation(PlanServiceContext psc) {
    Collection container = 
      psc.getServerPlugInSupport().queryForSubscriber(getSelfPred());
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
      results.add(properties);
    }
    return results;
  }

  private String getUIDAsString(UID uid) {
    if (uid == null)
      return "null";
    return uid.getOwner() + "/" + uid.getId();
  }

  /**
   * Required by PSP interface.
   */

  public boolean test(HttpInput query_parameters, PlanServiceContext sc)  {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }

  /**
   * A PSP can output either HTML or XML (for now).  The server
   * should be able to ask and find out what type it is.
   **/

  public boolean returnsXML() {
    return false;
  }

  public boolean returnsHTML() {
    return false;
  }

  /**  Any PlanServiceProvider must be able to provide DTD of its
   *  output IFF it is an XML PSP... ie.  returnsXML() == true;
   *  or return null
   **/

  public String getDTD()  {
    return null;
  }

  /**
   * Required by UISubscriber interface.
   */

  public void subscriptionChanged(Subscription subscription) {
  }
}
