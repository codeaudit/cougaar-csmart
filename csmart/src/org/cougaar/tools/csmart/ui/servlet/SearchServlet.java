/*
 * <copyright>
 *  Copyright 1997-2002 BBNT Solutions, LLC
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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.servlet.ServletUtil;
import org.cougaar.core.servlet.SimpleServletSupport;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AssetGroup;
import org.cougaar.planning.ldm.plan.Aggregation;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AssetTransfer;
import org.cougaar.planning.ldm.plan.Composition;
import org.cougaar.planning.ldm.plan.Disposition;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.MPTask;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Workflow;
import org.cougaar.tools.csmart.ui.servlet.TranslateUtils;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;

/**
 * Servlet that searches the Blackboard for all <code>UniqueObjects</code> that
 * are connected to a given <code>Set</code> of <code>UID</code>s.
 * <p>
 * The user specifies:<ul>
 *   <li>a "?limit=.." is an integer limit for the number of object
 *       to find.  FIXME over-limit action</li>
 *   <li>a "?format=.." of "html" or "data" (the default is html)</li>
 *   <li>a "?find=.." of "up" or "down" (the default is "down").  "Up" links
 *       are all the "parent" Objects (<i>causes</i>), and "down" links are
 *       all the "child" Objects (<i>effects</i>)</li>
 *   <li>a starting Set of UID Strings (either POSTed as a 
 *       serialized Java <code>List</code> or, if not POSTed, sent as a 
 *       single "?uid=.." URL parameter)</li>
 * </ul>
 * The result is a List of <code>PropertyTree</code>s for all the
 * related UniqueObjects.
 * 
 * <p>
 * Can be loaded manually by including this line in an agent's .ini configuration file: <br>
 *   plugin = org.cougaar.core.servlet.SimpleServletComponent(org.cougaar.tools.csmart.ui.servlet.SearchServlet, 
 *   /CSMART_SearchServlet)
 *
 * <p>
 * Is loaded from a URL on a CSMART machine, on agent 'Agent': <br>
 *   http://localhost:port/$Agent/CSMART_SearchServlet
 *
 * @see TranslateUtils
 */
public class SearchServlet 
  extends HttpServlet
{
  // cougaar hook
  SimpleServletSupport support;

  public void setSimpleServletSupport(SimpleServletSupport support) {
    this.support = support;
  
    if ( !  ( "/CSMART_SearchServlet".equals(support.getPath()) ) ) {
      System.out.println("Error in servlet path: " + support.getPath());
    }
  }
  
  /* Reponds to the GET http method call 
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   */
  public void doGet(
		    HttpServletRequest request,
		    HttpServletResponse response) throws IOException
  {
    // create a new "SearchProvider" context per request
    SearchProvider sp = new SearchProvider(support);
    sp.execute(request, response);
  }
  
  /* Reponds to the PUT http method call 
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   */
  public void doPut( 
		    HttpServletRequest request,
		    HttpServletResponse response) throws IOException
  { 
    // create a new "SearchProvider" context per request
    SearchProvider sp = new SearchProvider(support);
    sp.execute(request, response);
  }
  
  /**
   * Used to parse the URL and set parameters
   */
  private static class SearchProvider 
  {
    
    /**
     * Set to "true" to see debug-level messages.
     */
    public static final boolean VERBOSE = false;
    
    /*
     * Support class
     */
    private SimpleServletSupport support;
    
    /* parameter and method variables */
    private Set startUIDs = null;
    private boolean isDown = true;
    private boolean returnAsData;
    private int limit = Integer.MAX_VALUE;
    private String uid;
    private PrintWriter htmlout;
    
    /* Inner class constructor 
     * @param support Cougaar hook
     */
    public SearchProvider(SimpleServletSupport support) {
      this.support = support;
    }
    
    /**
     * Make the GET and POST passing of parameters transparent to 
     * the user.
     * <p>
     * Determine either GET or POST methods, call with respective 
     * ServletUtil methods.
     * <p>
     * Creates a ParamVisitor instance to set the parameters.
     * Set the parameters, store the map of (name, value) pairs for getting UIDs.
     */
    public void parseParams( 
			    HttpServletRequest req) throws IOException, ClassNotFoundException
    {  
      // always read "format=..", "limit=..", etc
      ServletUtil.ParamVisitor vis = new ServletUtil.ParamVisitor() {
        public void setParam(String name, String value) {
          if ("find".equalsIgnoreCase(name)) {
            if ("down".equalsIgnoreCase(value)) {
              isDown = true;
            } else if ("up".equalsIgnoreCase(value)) {
              isDown = false;
            }
          } else if ("format".equalsIgnoreCase(name)) {
            if ("data".equalsIgnoreCase(value)) {
              returnAsData = true;
            } else if ("html".equalsIgnoreCase(value)) {
              returnAsData = false;
            }
          } else if ("limit".equalsIgnoreCase(name)) {
            try {
              limit = Integer.parseInt(value);
              if (limit < 0) {
                limit = Integer.MAX_VALUE;
              }
            } catch (NumberFormatException nfe) {
            }
          } else if ("uid".equalsIgnoreCase(name)) {
            uid = value;
          }
        }
      };
      ServletUtil.parseParams(vis, req);

      // read attached data if there  (e.g. from PUT)
      int len = req.getContentLength();
      if (len > 0) {
	InputStream in = req.getInputStream();
	// read List of Strings
	try {
	  Object data;
	  try{
	    ObjectInputStream obStream = new ObjectInputStream(in);
	    data = obStream.readObject();
	  } catch (IOException e) {
	    System.out.println("Expecting a POSTED Serialized \"Object\": " + e.getMessage());
	    data = null;
	  }
	  if (data != null) {
	    startUIDs = getSearchUIDs(data);
          }
	} catch (RuntimeException e) {
	  System.out.println("zero UIDs posted?: "+e.getMessage());
	}
      }
    }
    
    /**
     * Main Servlet Method. Parses parameters, searches through them, returning
     * either data or html. 
     * 
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    public void execute(HttpServletRequest request, 
			HttpServletResponse response) throws IOException
    {
      
      // parse the URL parameters and attached (binary) data
      try {
	parseParams(request);
	
	// if zero UIDs were posted then look for a "?uid=" parameter
	if ((startUIDs == null) &&
	    //(optUID != null)) {
	    (uid != null)) {
	  startUIDs = new HashSet(1);
	  //UID uid = UID.toUID(optUID);
	  UID tmpuid = UID.toUID(uid);
	  startUIDs.add(tmpuid);
	}
      } catch (Exception e) {
	System.err.println("SearchServlet: Unable to parse parameters:");
	e.printStackTrace();
	
	// handle as "null" search set
	startUIDs = null;
      }
      
      // search
      List l;
      try {
	l = 
	  search(
		 support,
		 startUIDs,
		 isDown,
		 limit);
      } catch (RuntimeException e) {
	System.err.println("SearchServlet: Unable to search for data:");
	e.printStackTrace();
	
	// send back a null
	l = null;
      }
      
      /* Obtain <code>MessageAddress</code> */
      MessageAddress cid = support.getAgentIdentifier();
      
      // send back the results
      try {
	if (returnAsData) {
          OutputStream dataOut = response.getOutputStream();
	  replyWithData(
			dataOut,
			cid,
			l);
	} else {
          PrintWriter htmlOut = response.getWriter();
	  replyWithHTML(
			htmlOut, 
			cid,
			startUIDs,
			isDown,
			l);
	}
      } catch (Exception e) {
	System.err.println("SearchServlet: Unable to send response back:");
	e.printStackTrace();
      }
      
      // done!
    }
    
    /**
     * Reply with a serialized List of PropertyTrees.
     * @param out OutputStream
     * @param cid MessageAddress
     * @param l List of UIDs 
     */
    private static void replyWithData(
				      OutputStream out, 
				      MessageAddress cid,
				      List l) throws IOException {
      
      // convert to PropertyTrees, all tagged with this agent's name
      List retL = TranslateUtils.toPropertyTrees(l, cid.toString());
      
      try {
	if (retL != null ){
	  // serialize back to the user
	  ObjectOutputStream p = new ObjectOutputStream(out);
	  p.writeObject(retL);
	  //System.out.println("SearchServlet: Sent Objects");  
	}
      } catch (Exception e) {
	System.out.println("SearchServlet Exception: " + e);
	e.printStackTrace(); 
      }
    }
    
    /**
     * Reply with an HTML document.
     * @param out PrintWriter out
     * @param cid MessageAddress
     * @param startUIDs starting set of UIDs
     * @param isDown search guidlines
     * @param l List of UIDs
     */
    private static void replyWithHTML(
				      PrintWriter out, 
				      MessageAddress cid,
				      Set startUIDs,
				      boolean isDown,
				      List l) {
      int nStartUIDs = ((startUIDs != null) ? startUIDs.size() : 0);
      out.print(
		"<html><head><title>Search results</title></head><body>\n"+
		"<h2>Search "+cid+"</h2><p>\n"+
		"<h3>Direction: <b>"+(isDown ? "Down" : "Up")+"</b></h3>\n"+
		"<h3>Start with UIDs["+nStartUIDs+"]:</h3>");
      if (nStartUIDs > 0) {
	out.print("<ol>");
	Iterator iter = startUIDs.iterator();
	for (int i = 0; i < nStartUIDs; i++) {
	  out.print("<li>"+iter.next()+"</li>");
	}
	out.print("</ol>");
      } else {
	out.print(
		  "<i>Use \"?uid=...\" or POST a serialized "+
		  "List of UID Strings</i>");
      }
      int n = ((l != null) ? l.size() : 0);
      out.print("\n<h3>Found UniqueObjects["+n+"]:</h3><p>"+
		"<i>Note: links will appear in the task servlet's lower-left frame</i>"+
		"<p><ol>\n");
      String hrefBase  = null;
      try {
        hrefBase =
          "<a"+
          " target=\"itemFrame\""+
          " href=\"/$"+
          URLEncoder.encode(cid.toString(), "UTF-8")+
          "/tasks?uid=";
        } catch(UnsupportedEncodingException e) {
          // should never happen -- UTF-8 is always valid.
          System.err.println("Exception Encoding: "+e.getMessage());
        }

      // print objs
      for (int i = 0; i < n; i++) {
	UniqueObject ui = (UniqueObject)l.get(i);
	String href;
	UID uid = ui.getUID();
	if (uid != null) {
	  String suid = ui.getUID().toString();
	  // mode must match tasks servlet
	  int mode =
	    ((ui instanceof Task) ? 3 :
	     (ui instanceof PlanElement) ? 5 :
	     (ui instanceof Asset) ? 7 :
	     10);
	  href=
	    hrefBase+
	    suid+
	    "?mode="+
	    mode+
	    "\">"+suid+"</a>";
	} else {
	  href = "<font color=\"red\">null UID</font>";
	}
	out.print(
		  "\n<li>"+
		  href+
		  " == "+ui.getClass().getName()+"</li>");
      }
      
      out.print(
		"\n</ol>\n<h3>As PropertyTrees[");
      // convert to PropertyTrees
      List retL = TranslateUtils.toPropertyTrees(l, cid.toString());
      int nRetL = ((retL != null) ? retL.size() : 0);
      out.print(nRetL+"]:</h3><ol>");
      for (int i = 0; i < nRetL; i++) {
	out.print("\n<li>"+retL.get(i)+"</li>");
      }
      out.print("\n</ol></body></html>");
      
      out.flush();
    }
    
    /** 
     * Get the Set of UIDs specified by the POSTed data.
     * @param postObj List of objects
     */
    private static Set getSearchUIDs(Object postObj) {
    
      // make sure that a List was posted
      if (!(postObj instanceof List)) {
	throw new IllegalArgumentException(
					   "POSTED Object is not a \"java.util.List\": "+
					   ((postObj != null) ? postObj.getClass().getName() : "null"));
      }
    
      // parse vector of params into a set of uids
      List postUIDs = (List)postObj;
      int nPostUIDs = postUIDs.size();
    
      // parse Strings to a Set of UIDs
      Set toSet = new HashSet(nPostUIDs);
      for (int i = 0; i < nPostUIDs; i++) {
	Object oi = postUIDs.get(i);
	if (!(oi instanceof String)) {
	  throw new IllegalArgumentException(
					     "POSTED List["+i+"] contains a non-String:"+
					     ((oi != null) ? oi.getClass().getName() : "null"));
	}
	UID ui = UID.toUID((String)oi);
	toSet.add(ui);
      }
    
      // return the Set of UIDs
      return toSet;
    }
  
    /**
     * Find all the UniqueObjects that have UIDs in the given Set.
     * @param support Cougaar hook
     * @param uids UID set
     */
    private static Collection findUniqueObjects(
						SimpleServletSupport support,
						final Set uids) {
    
      Collection resultCol;
    
      int nuids = ((uids != null) ? uids.size() : 0);
      if (nuids <= 0) {
	// find nothing?
	resultCol = new ArrayList(0);
      } else if (nuids == 1) {
	// get the single UID
	final UID uid = (UID)uids.iterator().next();
	// create a predicate
	UnaryPredicate pred = 
	  new UnaryPredicate() {
	      public boolean execute(Object o) {
		return 
		  ((o instanceof UniqueObject) &&
		   (uid.equals(((UniqueObject)o).getUID())));
	      }
	    };
	// query
	resultCol = support.queryBlackboard(pred);
	//      resultCol = querySupport.query(pred);
      } else {
	// find multiple UniqueObjects
	// create a predicate
	UnaryPredicate pred = 
	  new UnaryPredicate() {
	      public boolean execute(Object o) {
		return 
		  ((o instanceof UniqueObject) &&
		   (uids.contains(((UniqueObject)o).getUID())));
	      }
	    };
	// query
	resultCol = support.queryBlackboard(pred);
      }

      return resultCol;
    }

    /**
     * Search a UID set, to a certain limit
     * @param support Cougaar support
     * @param startUIDset UID set
     * @param isDown search direction
     * @param limit limit of search recursion
     */
    private static List search(
			       SimpleServletSupport support,
			       Set startUIDSet,
			       boolean isDown,
			       int limit) {
      return
	(isDown ?
	 searchDown(support, startUIDSet, limit) :
	 searchUp(support, startUIDSet, limit));
    }
  
    /**
     * Search down a UID set
     * @param support Cougaar hook
     * @param startUIDSet start UID set
     * @param limit limit of search recursion
     */
    private static List searchDown(
				   SimpleServletSupport support,
				   Set startUIDSet,
				   int limit) {
    
      if (VERBOSE) {
	System.out.println(
			   "\n****************************************************"+
			   "\nSEARCH DOWN"+
			   "\nStart with ["+
			   ((startUIDSet != null) ? startUIDSet.size() : 0)+
			   "]: "+startUIDSet+
			   ((limit >= 0) ? ("\nLimit: "+limit) : "\nNo limit"));
      }
    
      // find the starting objects
      Collection startCol =
	findUniqueObjects(
			  support, 
			  startUIDSet);
    
      if (VERBOSE) {
	System.out.println(
			   "Found "+
			   startCol.size()+
			   " of "+
			   ((startUIDSet != null) ? startUIDSet.size() : 0));
      }
    
      // make a list of Objs to search
      WorkStack ws = new WorkStack();
      ws.pushAll(startCol);
    
      while (ws.hasWork()) {
	UniqueObject uo = ws.take();
	//
	int type = getItemType(uo);
	switch (type) {
	case ITEM_TYPE_ALLOCATION:
	  {
	    Allocation alloc = (Allocation)uo;
	    // asset
	    ws.push(alloc.getAsset());
	    // FIXME: what about the task?
	    ws.push(alloc.getTask());
	    // if remote then remote task UID is a property
	  }
	  break;
	case ITEM_TYPE_EXPANSION:
	  {
	    Expansion ex = (Expansion)uo;
	    ws.push(ex.getWorkflow());
	  }
	  break;
	case ITEM_TYPE_AGGREGATION:
	  {
	    Aggregation agg = (Aggregation)uo;
	    Composition comp = agg.getComposition();
	    if (comp != null) {
	      // "^" ignore comp.getParentTasks
	      // child MPTask
	      ws.push(comp.getCombinedTask());
	    }
	  }
	  break;
	case ITEM_TYPE_ASSET_TRANSFER:
	  {
	    AssetTransfer atrans = (AssetTransfer)uo;
	    // asset moved
	    ws.push(atrans.getAsset());
	    // destination asset
	    ws.push(atrans.getAssignee());
	    // task
	    ws.push(atrans.getTask());
	  }
	  break;
	case ITEM_TYPE_TASK:
	  {
	    Task t = (Task)uo;
	    // direct object (Asset)
	    ws.push(t.getDirectObject());
	    // plan element
	    ws.push(t.getPlanElement());
	  }
	  break;
	case ITEM_TYPE_ASSET:
	  {
	    Asset asset = (Asset)uo;
	    // asset group?
	    if (asset instanceof AssetGroup) {
	      // assets
	      ws.pushAll(((AssetGroup)asset).getAssets());
	    }
	  }
	  break;
	case ITEM_TYPE_WORKFLOW:
	  {
	    Workflow wf = (Workflow)uo;
	    // tasks
	    ws.pushAll(wf.getTasks());
	  }
	  break;
	case ITEM_TYPE_DISPOSITION:
	  {
	    // FIXME: What about the Task?
	    PlanElement pe = (PlanElement)uo;
	    // task
	    ws.push(pe.getTask());
	    break;
	  }
	case ITEM_TYPE_OTHER:
	  {
	    // Add in something for HCEs to point to PlanElements?
	    if (uo instanceof org.cougaar.tools.csmart.runtime.ldm.event.HappinessChangeEvent) {
	      ws.push(((org.cougaar.tools.csmart.runtime.ldm.event.HappinessChangeEvent)uo).getRegarding());
	    }
	    break;
	  }
	default:
	  // ignore
	  break;
	}
      
	if (ws.sizePushed() > limit) {
	  // we've exceeded our limit
	  //
	  // FIXME: trim "ws" to just (limit + 1)
	  break;
	}
      }
    
      if (VERBOSE) {
	System.out.println(
			   "done."+
			   "\n**********************************************\n");
      }
    
      List toL = new ArrayList(ws.sizePushed());
      ws.toCollection(toL);
      return toL;
    }
  
    /**
     * search up.
     */
    private static List searchUp(
				 SimpleServletSupport support, 
				 Set startUIDSet,
				 int limit) {
    
      // get this Agent's identifier
      MessageAddress localCID =
	support.getAgentIdentifier();
    
      if (VERBOSE) {
	System.out.println(
			   "\n*****************************************************"+
			   "\nSEARCH UP"+
			   ((limit >= 0) ? ("\nLimit: "+limit) : "\nNo limit"));
      }
    
      // make a list of Objs to search
      WorkStack ws = new WorkStack();
      Set findUIDSet = new HashSet(startUIDSet);
    
      search_up_loop:
      while (!(findUIDSet.isEmpty())) {
      
	if (VERBOSE) {
	  System.out.println(
			     "\nFind ["+findUIDSet.size()+"]: "+findUIDSet);
	}
      
	// find the starting objects
	Collection foundCol =
	  findUniqueObjects(
			    support, 
			    findUIDSet);
	findUIDSet.clear();
      
	if (VERBOSE) {
	  System.out.println(
			     "Found "+foundCol.size()+" of "+findUIDSet.size());
	}
      
	ws.pushAll(foundCol);
      
	while (ws.hasWork()) {
	  UniqueObject uo = ws.take();
	  //
	  if (uo instanceof PlanElement) {
	  
	    PlanElement pe = (PlanElement)uo;
	    // task
	    ws.push(pe.getTask());
	  
	  } else if (uo instanceof Task) {
	  
	    // task parent(s)
	    if (uo instanceof MPTask) {
	      MPTask mpt = (MPTask)uo;
	      // parent tasks
	      ws.pushAll(mpt.getParentTasks());
	    } else {
	      Task t = (Task)uo;
	      // only have a UID for the parent
	      MessageAddress tCID = t.getSource();
	      if ((tCID == null) ||
		  (localCID.equals(tCID))) {
		// local Task, must search by UID
		UID uid = t.getParentTaskUID();
		if ((uid != null) &&
		    (!(ws.hasPushedKey(uid)))) {
		  // add the UID to next search pass.
		  //
		  // note that this "find" Task may be found in
		  // this pass indirectly, which will waste a query,
		  // but it's a rare and harmless situation
		  findUIDSet.add(uid);
		}
	      } else {
		// remote up!
	      }
	    }
	  
	  } else if (uo instanceof Workflow) {
	  
	    Workflow wf = (Workflow)uo;
	    // task
	    ws.push(wf.getParentTask());
	  
	  } else {
	    // ignore
	  }
	
	  if (ws.sizePushed() > limit) {
	    // we've exceeded our limit
	    //
	    // FIXME: trim "ws" to just (limit + 1)
	    break search_up_loop;
	  }
	}
      }

      if (VERBOSE) {
	System.out.println(
			   "done."+
			   "\n****************************************************\n");
      }
    
      List toL = new ArrayList(ws.sizePushed());
      ws.toCollection(toL);
      return toL;
    }
  
    /**
     * Search "worklist" data structure to traverse a UniqueObject graph.
     */
    private static final class WorkStack {
    
      private List l;
      private HashMap m;
    
      public WorkStack() {
	this.l = new ArrayList();
	this.m = new HashMap(89);
      }
    
      public int sizeWork() {
	return l.size();
      }
    
      public int sizePushed() {
	return m.size();
      }
    
      public boolean hasWork() {
	return (!(l.isEmpty()));
      }
    
      public UniqueObject take() {
	UniqueObject uo = (UniqueObject)l.remove(l.size()-1);
	if (VERBOSE) {
	  System.out.println(
			     "----take["+l.size()+"]: "+
			     ((uo != null) ? uo.getClass().getName() : "null"));
	}
	return uo;
      }
    
      public boolean push(UniqueObject uo) {
	if (uo != null) {
	  UID uid = uo.getUID();
	  if (m.put(uid, uo) == null) {
	    // add if new
	    l.add(uo);
	    if (VERBOSE) {
	      System.out.println(
				 "++++pushed UID: "+uid+" for "+uo.getClass().getName());
	    }
	    return true;
	  } else {
	    if (VERBOSE) {
	      System.out.println(
				 "    ignore UID: "+uid+" for "+uo.getClass().getName());
	    }
	  }
	} else {
	  if (VERBOSE) {
	    System.out.println("    ignore null");
	  }
	}
	return false;
      }
    
      public boolean push(Object o) {
	if (o instanceof UniqueObject) {
	  return push((UniqueObject)o);
	} else {
	  if (VERBOSE) {
	    System.out.println("     ignore "+
			       ((o != null) ? o.getClass().getName() : "null"));
	  }
	  return false;
	}
      }
    
      public int pushAll(Collection fromCol) {
	int t = 0;
	int n = ((fromCol != null) ? fromCol.size() : 0);
	if (n > 0) {
	  Iterator iter = fromCol.iterator();
	  do {
	    if (this.push(iter.next())) {
	      t++;
	    }
	  } while (--n > 0);
	}
	return t;
      }
    
      public int pushAll(Enumeration fromEn) {
	int t = 0;
	if (fromEn != null) {
	  while (fromEn.hasMoreElements()) {
	    if (this.push(fromEn.nextElement())) {
	      t++;
	    }
	  }
	}
	return t;
      }
    
      public boolean hasPushed(UniqueObject uo) {
	return 
	  ((uo != null) &&
	   (hasPushedKey(uo.getUID())));
      }

      public boolean hasPushed(Object o) {
	return
	  ((o instanceof UniqueObject) &&
	   (this.hasPushed((UniqueObject)o)));
      }

      public boolean hasPushedKey(UID uid) {
	return
	  ((uid != null) &&
	   (m.containsKey(uid)));
      }

      public void toCollection(Collection toCol) {
	/// add all the values in the Map to the given collection
	toCol.addAll(m.values());
      }

      public void toMap(Map toMap) {
	toMap.putAll(m);
      }

      public String toString() {
	return toString(false);
      }

      public String toString(boolean verbose) {
	String s =
	  "WorkStack {"+
	  "\n  work["+l.size()+"]";
	if (verbose) {
	  s += ": "+l;
	}
	s +=
	  "\n  found["+m.size()+"]";
	if (verbose) {
	  s += ": "+m;
	}
	s +=
	  "\n}";
	return s;
      }
    }

    /* Object Class type variables */
    private static final int ITEM_TYPE_ALLOCATION     = 0;
    private static final int ITEM_TYPE_EXPANSION      = 1;
    private static final int ITEM_TYPE_AGGREGATION    = 2;
    private static final int ITEM_TYPE_DISPOSITION    = 3;
    private static final int ITEM_TYPE_ASSET_TRANSFER = 4;
    private static final int ITEM_TYPE_TASK           = 5;
    private static final int ITEM_TYPE_ASSET          = 6;
    private static final int ITEM_TYPE_WORKFLOW       = 7;
    private static final int ITEM_TYPE_OTHER          = 8;

    /**
     * Big switch statement on Object Class type.
     * <p>
     * Replace with synchronized hashmap lookup on obj.getClass()?
     **/
    private static int getItemType(Object obj) {
      if (obj instanceof PlanElement) {
	if (obj instanceof Allocation) {
	  return ITEM_TYPE_ALLOCATION;
	} else if (obj instanceof Expansion) {
	  return ITEM_TYPE_EXPANSION;
	} else if (obj instanceof Aggregation) {
	  return ITEM_TYPE_AGGREGATION;
	} else if (obj instanceof Disposition) {
	  return ITEM_TYPE_DISPOSITION;
	} else if (obj instanceof AssetTransfer) {
	  return ITEM_TYPE_ASSET_TRANSFER;
	} else {
	  return ITEM_TYPE_OTHER;
	}
      } else if (obj instanceof Task) {
	return ITEM_TYPE_TASK;
      } else if (obj instanceof Asset) {
	return ITEM_TYPE_ASSET;
      } else if (obj instanceof Workflow) {
	return ITEM_TYPE_WORKFLOW;
      } else {
	return ITEM_TYPE_OTHER;
      }
    }
  }
}
