/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.psp;

import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;

import org.cougaar.core.cluster.*;
import org.cougaar.core.society.UID;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.CommunityPG;
import org.cougaar.domain.planning.ldm.asset.Entity;
import org.cougaar.domain.planning.ldm.plan.HasRelationships;
import org.cougaar.lib.planserver.*;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames; 
import org.cougaar.util.PropertyTree;
import org.cougaar.util.UnaryPredicate;

/**
 * Expects no input
 * Returns agent name and community name from Entity object
 */

public class PSP_CommunityProvider extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {

  protected static final boolean DEBUG = false;

  public PSP_CommunityProvider() {
    super();
  }

  public PSP_CommunityProvider(String pkg, String id) throws RuntimePSPException
  {
    setResourceLocation(pkg, id);
  }

  public boolean test(HttpInput query_parameters, PlanServiceContext sc)
  {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
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
	  //	  if ((asset.hasCommunityPG(new Date().getTime())) &&
	  //	      (asset.hasClusterPG()))
	  //	    return true;
	}
	return false;
      };
    };
  };


  /**
   * Called when a request is received from a client.
   * Get the POST data; parse the request; get the objects
   * that match the request; send them to the client.
   */

  public void execute(PrintStream out, 
		      HttpInput query_parameters, 
		      PlanServiceContext psc, 
		      PlanServiceUtilities psu) throws Exception {
    // need try/catch here or caller sends exceptions to client as html
    try {
      System.out.println("PSP_CommunityProvider received query..........");
      Vector collection = getSelfInformation(psc);
      ObjectOutputStream oos = new ObjectOutputStream(out);
      oos.writeObject(collection);
    } catch (Exception e) {
      System.out.println("PSP_CommunityProvider Exception: " + e);
      e.printStackTrace();
    }
  }

  /**
   * Get cluster and community name for this cluster.
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
      properties.put(PropertyNames.UID_ATTR, getUIDAsString(asset.getUID()));
      String name = asset.getItemIdentificationPG().getNomenclature();
      properties.put(PropertyNames.AGENT_NAME, name);
      CommunityPG communityPG = asset.getCommunityPG(new Date().getTime());
      Collection communities = communityPG.getCommunities();
      if (communities.size() > 1) 
	System.out.println("PSP_CommunityProvider: WARNING: handling agents in multiple communities is not implemented.");
      Iterator i = communities.iterator();
      String communityName = null;
      while (i.hasNext()) {
	communityName = (String)i.next();
	break;
      }
      if (communityName != null)
	properties.put(PropertyNames.AGENT_COMMUNITY_NAME, communityName);
      URL url = psc.lookupURL(psc.getServerPlugInSupport().getClusterIDAsString());
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

  protected static class MyPSPState extends PSPState {
    public MyPSPState(
		      UISubscriber xsubscriber,
		      HttpInput query_parameters,
		      PlanServiceContext xpsc) {
      super(xsubscriber, query_parameters, xpsc);
      super.clusterID = super.clusterID.intern();
    }

    public void setParam(String name, String value) {
    }    
  }

}

