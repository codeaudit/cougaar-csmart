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

package org.cougaar.tools.csmart.ui.monitor.viewer;


import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import javax.swing.JOptionPane;
import org.cougaar.tools.csmart.ui.monitor.PropertyNames;
import org.cougaar.tools.csmart.ui.util.ClientServletUtil;
import org.cougaar.tools.csmart.ui.util.ServletResponse;
import org.cougaar.tools.csmart.ui.util.ServletResult;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.PropertyTree;
import org.cougaar.util.log.Logger;

/**
 * Utility <tt>getFullThread(..)<tt> methods for multi-agent interaction
 * and aggregation with the "Threads" servlet.
 * <p>
 * The "getFullThread(..)" method makes multiple calls to the "Threads" 
 * servlets on different Agents to create a cross-agent "thread" of the 
 * separate Agent Blackboard Objects.
 * <p>
 * Note that the name "thread" here refers to chains of Objects, not
 * to <code>java.lang.thread</code>!
 */
public final class ThreadUtils {

  private ThreadUtils() {
  }

  /**
   * Enable/disable debug messages.
   */
  public static final boolean VERBOSE = false;

  /**
   * getFullThread starting with a single UID.
   * <p>
   * Note that the UID prefix ("xyz/123") does <u>not</u> necessarily
   * indicate the agent name!  In particular, transfered Tasks will
   * keep the UID prefix of their source Agent, not their destination,
   * and there are other special-cases like this.
   *
   * @see #getFullThread(AgentMapping,boolean,int,Map)
   */
  public static List getFullThread(
      AgentMapping am,
      boolean isDown,
      int limit,
      String startAgentName,
      String startUID) {
    List startUIDs = new ArrayList(1);
    startUIDs.add(startUID);
    return 
      getFullThread(
          am,
          isDown,
          limit,
          startAgentName,
          startUIDs);
  }

  /**
   * getFullThread for a List of UIDs on a single agent.
   * <p>
   * Note that the UID prefix ("xyz/123") does <u>not</u> necessarily
   * indicate the agent name!  In particular, transfered Tasks will
   * keep the UID prefix of their source Agent, not their destination,
   * and there are other special-cases like this.
   *
   * @see #getFullThread(AgentMapping,boolean,int,Map)
   */
  public static List getFullThread(
      AgentMapping am,
      boolean isDown,
      int limit,
      String startAgentName,
      List startUIDs) {
    Map agentToUIDs = new HashMap(1);
    agentToUIDs.put(startAgentName, startUIDs);
    return
      getFullThread(
          am,
          isDown,
          limit,
          agentToUIDs);
  }

  /**
   * Do a "thread" search for multiple agents, starting with the
   * (agent, UIDs) Map specified by <tt>agentToUIDs</tt>, and returning
   * a List of PropertyTrees as the result.
   * <p>
   * Note that the UID prefix ("xyz/123") does <u>not</u> necessarily
   * indicate the agent name!  In particular, transfered Tasks will
   * keep the UID prefix of their source Agent, not their destination,
   * and there are other special-cases like this.
   * <p>
   * The <code>AgentMapping</code> is used to find the host and 
   * port for each agent query.
   * <p>
   * "isDown" specifies the direction for the search.  If <tt>true</tt>
   * then serialize-order links are traversed, plus links from an
   * allocation to a remote Agent (analogous to <i>effects</i>).  If 
   * <tt>false</tt> then only "*parent*" links are traversed and Tasks 
   * are followed up to their parents (analogous to <i>causes</i>).
   * <p>
   * <pre>
   * How should an exceeded limit be signalled back to the user?
   *   option 1) overflow the first time for a full agent-quantity
   *   option 2) overflow to (limit+1) -- the user will get a partial result
   *             for the last agent searched, which might be confusing
   *             <i>(this is what is done now!)</i>
   *   option 3) never overflow, quietly return less than the limit
   *             (but then how does the user know it overflowed?)
   *   option 4) append a dummy PropertyTree of ("Overflow", "true")
   *   option 5) append a dummy PropertyTree with a new "agentToUIDs" Map,
   *             which would allowing further "getFullThread" calls with this
   *             "continuation-point"
   * All five could be supported by a parameter, but it's likely that we'll
   * only need one or two options...
   * </pre>
   * <p>
   * The "agentToUIDs" Map specifies the starting points for the search,
   * where the Map contains (String agentName, List of String UIDs).
   * Objects matching these starting points are also returned in the
   * results.
   *
   * @param am maps an agent name to its host and port
   * @param isDown specifies the search direction
   * @param limit maximum number of PropertyTrees to gather (see above notes)
   * @param agentToUIDs a Map of (String agentName, List of String UIDs) for 
   *   the search starting points
   *
   * @return a List of PropertyTrees
   */
  public static List getFullThread(
      AgentMapping am,
      boolean isDown,
      int limit,
      Map agentToUIDs) {

    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.ui.monitor.viewer.ThreadUtils");

    boolean hasLimit = (limit >= 0);
    int remainingLimit = limit;

    // keep a list of PropertyTrees as our result
    List result = new ArrayList();

    // keep a Map of (agentName, List of UIDs)
    Map workMap = new HashMap(13);
    workMap.putAll(agentToUIDs);

    while (workMap.size() > 0) {

      if (VERBOSE) {
        // show the work-map
        if(log.isDebugEnabled()) {
          log.debug("workMap["+workMap.size()+"]:");
          Iterator iter = workMap.entrySet().iterator();
          for (int x = 0; x < workMap.size(); x++) {
            log.debug("   "+x+"  "+iter.next());
          }
        }
      }

      // take first entry of the work-map
      String findAgentName = (String)workMap.keySet().iterator().next();
      List findUIDs = (List)workMap.remove(findAgentName);

      // query the servlet
      if (VERBOSE) {
        if(log.isDebugEnabled()) {
          log.debug("--query "+findAgentName+" ["+findUIDs.size()+"]--");
        }
      }
      List l = 
        getLocalThread(
            am,
            isDown, 
            findAgentName,
            findUIDs,
            remainingLimit);
      int n = ((l != null) ? l.size() : 0);
      if (VERBOSE) {
        if(log.isDebugEnabled()) {
          log.debug("--done["+n+"]--");
        }
      }

      if (n <= 0) {
        // found nothing?
        continue;
      }

      // add these to the results 
      //
      // note that they are already tagged with the AGENT_ATTR
      result.addAll(l);

      // check limit
      if (hasLimit) {
        remainingLimit -= n;
        if (remainingLimit < 0) {
          // exceed our limit!  
          //
          // For now we'll accept this overflow and return.
          break;
        }
      }

      // add the "boundary-case" UIDs to the workMap
      for (int i = 0; i < n; i++) {
        PropertyTree pti = (PropertyTree)l.get(i);
        if (VERBOSE) {
          if(log.isDebugEnabled()) {
            log.debug(
                      "  "+i+"  "+
                      pti.get("Object_Type")+
                      " "+
                      pti.get("UID"));
          }
        }
        // switch on "isDown" -- let HotSpot optimize the loop
        if (isDown) {
          // "down" boundary is an allocation to an agent
          Object o = pti.get(PropertyNames.ALLOCATION_TO_AGENT);
          if (o != null) {
            String allocAgent = (String)o;
            String allocTask = 
              (String)pti.get(PropertyNames.ALLOCATION_TASK_UID);
            if (VERBOSE) {
              if(log.isDebugEnabled()) {
                log.debug("trace agent: "+allocAgent+", uid: "+allocTask);
              }
            }
            List toL = (List)workMap.get(allocAgent);
            if (toL == null) {
              toL = new ArrayList();
              workMap.put(allocAgent, toL);
            }
            toL.add(allocTask);
          }
        } else {
          // "up" boundary is a task where source != findAgentName
          Object o = pti.get(PropertyNames.TASK_SOURCE);
          if ((o != null) &&
              (!(findAgentName.equals(o)))) {
            String parentAgent = (String)o;
            String parentTask = 
              (String)pti.get(PropertyNames.TASK_PARENT_UID);
            if (VERBOSE) {
              if(log.isDebugEnabled()) {
                log.debug("trace agent: "+parentAgent+", uid: "+parentTask);
              }
            }
            List toL = (List)workMap.get(parentAgent);
            if (toL == null) {
              toL = new ArrayList();
              workMap.put(parentAgent, toL);
            }
            toL.add(parentTask);
          }
        }
      }
    }

    // done!
    return result;
  }

  /**
   * Query a single agent for its "thread" information.
   *
   * @param am AgentMapping, to get the host and port for the agent
   * @param agentName name of the agent
   * @param isDown select direction for the search
   * @param uids List of String UIDs for the search
   * @param limit integer limit for number of objects, or -1 if there
   *                is no limit.
   *
   * @return List of PropertyTrees, or null if an error occurred
   */

  private static List getLocalThread(
      AgentMapping am,
      boolean isDown,
      String agentName, 
      List uids,
      int limit) {
    // FIXME Use secure mode if necessary!!!
    // Perhaps AgentMapping needs a http/https flag?
    String agentURL = ClientServletUtil.makeURL(am.getHost(agentName),
                                                am.getPort(agentName));
    Logger log = CSMART.createLogger("org.cougaar.tools.csmart.ui");

    try {
      agentURL = agentURL + "/$" + URLEncoder.encode(agentName, "UTF-8") + "/";
    } catch(UnsupportedEncodingException e) {
      if (log.isErrorEnabled())
        log.error("Exception Encoding ", e);
    }

    ArrayList parameterNames = new ArrayList(2);
    ArrayList parameterValues = new ArrayList(2);
    parameterNames.add("format");
    parameterValues.add("data");
    parameterNames.add("find");
    if (isDown)
      parameterValues.add("down");
    else
      parameterValues.add("up");
    ServletResponse response =
      ClientServletUtil.getCollectionFromAgent(agentURL,
                     ClientServletUtil.SEARCH_SERVLET,
                     parameterNames, parameterValues, uids, limit);
    String s = response.getErrorMessage();
    if (s != null)
      JOptionPane.showMessageDialog(null, s);
    return (List)response.getCollection();
  }

//      try {
//        // get host and port
//        String host = am.getHost(agentName);
//        int port = am.getPort(agentName);
//        // create URL
//        String surl = 
//          "http://"+host+":"+port+
//          "/$"+
//          URLEncoder.encode(agentName)+
//          SEARCH_PSP_NAME+
//          "?format=data"+
//          "&find="+
//          (isDown ? "down" : "up") +
//          ((limit >= 0) ? ("&limit="+limit) : "");
//        // connect, upload List of Strings
//        //      System.out.println("ThreadUtils: connecting to: " + surl);
//        URL url = new URL(surl);
//        URLConnection conn = url.openConnection();
//        conn.setDoOutput(true);
//        OutputStream os = conn.getOutputStream();
//        ObjectOutputStream oos = new ObjectOutputStream(os);
//        oos.writeObject(uids);
//        oos.close();
//        // receive result List of PropertyTrees
//        InputStream is = conn.getInputStream();
//        ObjectInputStream ois = new ObjectInputStream(is);
//        Object inObj = ois.readObject();
//        ois.close();
//        // return result
//        return (List)inObj;
//      } catch (Exception e) {
//        System.err.println("Query agent "+agentName+" failed:");
//        System.err.println(e);
//        //      e.printStackTrace();
//        return null;
//      }
  //  }


  /** testing utility */
  public static void main(String[] args) throws NumberFormatException {

    // parse the command line
    String host = "localhost";
    int port = 5555;
    String agentName = "3-69-ARBN";
    int limit = Integer.MAX_VALUE;
    boolean isDown = true;
    List uids = new ArrayList(args.length);
    for (int i = 0; i < args.length; i++) {
      String si = args[i];
      int sep = si.indexOf("=");
      if (sep <= 0) {
        if (si.endsWith(",")) {
          si = si.substring(0, si.length()-1);
        }
        System.out.println("  "+uids.size()+"  "+si);
        uids.add(si);
      } else {
        String name = si.substring(0, sep);
        String value = si.substring(sep+1);
        if (name.equals("agent")) {
          agentName = value;
        } else if (name.equals("down")) {
          isDown = ("true".equals(value));
        } else if (name.equals("limit")) {
          limit = Integer.parseInt(value);
        } else if (name.equals("host")) {
          host = value;
        } else if (name.equals("port")) {
          port = Integer.parseInt(value);
        }
      }
    }

    // create an AgentMapping
    final String finalHost = host;
    final int finalPort = port;
    AgentMapping am = 
      new AgentMapping() {
        public String getHost(String xAgentName) {
          return finalHost;
        }
        public int getPort(String xHost) {
          return finalPort;
        }
      };

    // run the test
    System.out.println("--getFullThread["+uids.size()+"]--");
    List l = getFullThread(am, isDown, limit, agentName, uids);
    int n = ((l != null) ? l.size() : 0);
    System.out.println("--got["+n+"]--");
    for (int i = 0; i < n; i++) {
      PropertyTree pti = (PropertyTree)l.get(i);
      System.out.println(
          "\t"+i+"\t"+
          pti.get(PropertyNames.AGENT_ATTR)+"\t"+
          pti.get(PropertyNames.OBJECT_TYPE)+"\t"+
          pti.get(PropertyNames.UID_ATTR));
    }
  }

}
