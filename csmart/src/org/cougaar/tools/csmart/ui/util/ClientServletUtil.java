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

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import javax.swing.JOptionPane;

/**
 * Utilities for contacting servlets in societies.
 */

public class ClientServletUtil {
  // servlet to get the URLs of all agents in the society
  public static final String AGENT_PROVIDER_SERVLET =
    "CSMART_ClusterProviderServlet";

  /**
   * Contact the agent provider servlet at the specified URL,
   * which returns the URLs of all the agents in the society.
   * This may return null or a vector with zero elements;
   * in these cases, appropriate error messages are displayed for the user.
   * @param URL the URL of the agent to contact
   * @return vector of String; URLs of agents in society
   */

  public static Vector getAgentURLs(String URL) throws Exception {
    String urlSpec = URL + "/" + AGENT_PROVIDER_SERVLET;
    URL url = new URL(urlSpec);
    URLConnection connection = url.openConnection();
    connection.setDoInput(true);
    connection.setDoOutput(true);
    // get agent URLs from servlet
    InputStream is = connection.getInputStream();
    ObjectInputStream p = new ObjectInputStream(is);
    return (Vector)p.readObject();
  }

  /**
   * Get a collection from specified agents and servlet.
   * The servlet must be one that returns a Collection.
   * to determine what objects to include in the collection.
   * Returns null if the agent or servlet cannot be contacted.
   * @param agentURLs   the agents to contact; vector of String
   * @param servletId   the servlet to contact
   * @param limit       the limit for the number of objects to return, or -1 if
   *                       there is no limit -- the servlet must
                           accept a limit=N argument
   * @return            results from servlets
   */

  public static ServletResult getCollectionFromAgents(Vector agentURLs,
                                                      String servletId,
                                                      ArrayList parameterNames,
                                                      ArrayList parameterValues,
                                                      int limit) {
    Collection results = null;
    if (agentURLs == null || agentURLs.size() == 0)
      return null;

    boolean hasLimit = (limit >= 0);
    // limit that is decremented for each call
    int remainingLimit = (hasLimit ? limit : -1);
    int nAgentURLs = agentURLs.size();

    ServletResult result = new ServletResult();

    for (int i = 0; i < nAgentURLs; i++) {
      String url = (String)agentURLs.elementAt(i);
      Collection col = 
        getCollectionFromAgent(url, servletId, parameterNames,
                               parameterValues, remainingLimit);
      result.addCollection(col);
      // check limit, and set flag in result if its exceeded
      if (hasLimit) {
        if (col != null) {
          remainingLimit = remainingLimit - col.size();
          if (remainingLimit < 0) {
            result.setLimitExceeded(true);
            break;
          }
        }
      }
    }
    return result;
  }
  
  /**
   * Get a collection from a single agent and servlet.
   * Same as getCollectionFromAgents for a single agent.
   * @param agentURL  the agent to contact
   * @param servletId the servlet to contact
   * @param limit     max. number of objects to return, or -1 if no limit
   * @return          the collection from the servlet or null
   */

  private static Collection getCollectionFromAgent(String agentURL,
                                                   String servletId,
                                                   ArrayList parameterNames,
                                                   ArrayList parameterValues,
                                                   int limit) {
    URLSpec.setBase(agentURL + servletId);
    if (parameterNames != null)
      URLSpec.addArgs(parameterNames, parameterValues);
    if (limit >= 0)
      URLSpec.addArg("limit", limit);
    String urlSpec = URLSpec.getResult();
    Collection results = null;
    try {
      //      System.out.println("ClientServerUtil: contacting: " + urlSpec);
      URL url = new URL(urlSpec);
      URLConnection connection = url.openConnection();
      connection.setDoInput(true);
      connection.setDoOutput(true);
      InputStream is = connection.getInputStream();
      ObjectInputStream p = new ObjectInputStream(is);
      results = (Collection)p.readObject();
    } catch (Exception e) {
      results = null; // caller handles reporting the error
    }
    return results;
  }

  /**
   * This class handles building a URL.
   * The first parameter is preceeded by a question mark
   * and all other parameters are preceeded by an ampersand.
   */

  static class URLSpec {
    static StringBuffer buf;
    static char parameterPrefix;

    public static void setBase(String base) {
      buf = new StringBuffer(100);
      buf.append(base);
      if (base.indexOf('?') == -1)
        parameterPrefix = '?';
      else
        parameterPrefix = '&';
    }

    public static void addArgs(ArrayList names, ArrayList values) {
      for (int i = 0; i < names.size(); i++) 
        URLSpec.addArg((String)names.get(i), values.get(i));
    }

    public static void addArg(String argName, Object argValue) {
      buf.append(parameterPrefix);
      buf.append(argName);
      buf.append('=');
      buf.append(URLEncoder.encode(argValue.toString()));
      parameterPrefix = '&';
    }

    public static void addArg(String argName, int argValue) {
      URLSpec.addArg(argName, String.valueOf(argValue));
    }

    public static String getResult() {
      return buf.toString();
    }
  }

}




