/*
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.configgen.community;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;

import org.cougaar.tools.csmart.configgen.*;
import org.cougaar.tools.csmart.configgen.community.templates.*;

/**
 * An implementation of <code>SocietyBuilder</code> that uses 
 * community-based templates.
 */
public class SocietyBuilderImpl 
implements SocietyBuilder {

  private String name;

  public SocietyBuilderImpl() {
  }

  public void initialize(String[] args) throws Exception {
    if ((args == null) ||
        (args.length < 1)) {
      throw new IllegalArgumentException(
          "SocietyBuilderImpl expecting a first argument"+
          " for the \"name\"");
    }

    name = args[0];
  }

  /**
   * Starts the config file generation by parsing the XML
   * file and then passing each community element to the
   * community generator.
   */
  public Society build() throws Exception {

    // parse the communities
    Map commMap = new HashMap();
    try {
      XMLParser xp = new XMLParser(name);
      NodeList nlist = xp.getAllNodes();

      List commConfigs = new ArrayList(nlist.getLength());
      for (int i = 0; i < nlist.getLength(); i++) {
        Node n = nlist.item(i);
        if (n.getNodeType() == Node.ELEMENT_NODE) {
          CommunityConfig cc = new CommunityConfig();

          // parse should verify
          cc.parse((Element)n);

          Community comm = loadCommunity(cc);
          if (comm == null) {
            // error
            return null;
          }
          commMap.put(comm.getName(), comm);
        }
      }
    } catch (Exception e) {
      System.err.println(
          "Unable to parse file \""+name+"\":");
      e.printStackTrace();
      return null;
    }

    // use the simple society implementation, even though some properties
    //   are society-wide (e.g. startMillis)
    Society toSoc = new SocietyImpl();

    // expand the community configs to agents
    Map commReps = new HashMap(commMap.size());
    for (Iterator iter = commMap.values().iterator();
        iter.hasNext();
        ) {
      Community ci = (Community)iter.next();

      // create the Agents
      int n = ci.getNumberOfAgents();
      List l = new ArrayList(n);
      int repNum = ci.getRepresentativeAgentIndex();
      for (int j = 0; j < n; j++) {
        Agent newA = new CommunityBasedAgentImpl(ci, j);

        if (j == repNum) {
          commReps.put(ci.getName(), newA);
        }

        l.add(newA);
        toSoc.addAgent(newA);
      }

      toSoc.addNode(ci.getName(), l);
    }

    // join the communities
    for (Iterator iter = commMap.values().iterator();
        iter.hasNext();
        ) {
      Community ci = (Community)iter.next();

      // get all supported communities of ci
      List sup =  ci.getSupportedCommunityNames();
      int n = ((sup != null) ? sup.size() : 0);

      // get the agent that provides the support
      Agent provAgent = (Agent)commReps.get(ci.getName());
      if (provAgent == null) {
        // no representative for the community?
        if (n > 0) {
          System.err.println(
              "Warning: Community \""+ci.getName()+"\" supports "+
              n+" communities ("+sup+"), but lacks a supporting agent.");
        } else {
          // ignore
        }
        continue;
      }

      // for all supported communities
      for (int j = 0; j < n; j++) {
        String supNameJ = (String)sup.get(j);

        // get the agent in cj that receives the support
        Agent recAgent = (Agent)commReps.get(supNameJ);
        if (recAgent == null) {
          if (!(commMap.containsKey(supNameJ))) {
            throw new IllegalArgumentException(
                "Community \""+ci.getName()+"\" supports an unknown "+
                "community \""+supNameJ+"\"");
          } else {
            // nobody needs support?
            System.err.println(
                "Warning: Community \""+ci.getName()+"\" supports "+
                "community \""+supNameJ+"\", but \""+supNameJ+
                "\" lacks a supported agent.");
            continue;
          }
        }

        // match up the agents
        ((CommunityBasedAgentImpl)provAgent).addSupportedAgentName(
            recAgent.getName());
      }
    }

    // create a single-node
    toSoc.addNode("FullSociety", toSoc.getAgents());

    return toSoc;
  }

  /**
   * Dynamically load a community template instance.
   */
  protected Community loadCommunity(CommunityConfig cc) {
    String className = cc.getTemplate();
    if (className.indexOf('.') < 0) {
      // simple built-in templates
      className = 
        "org.cougaar.tools.csmart.configgen.community.templates." + 
        className + 
        "Template";
    }

    // load and create an instance
    Community comm;
    try {
      Class cls = Class.forName(className);
      comm = (Community)cls.newInstance();
    } catch (Exception e) {
      System.err.println(
          "Unable to load Community template ("+
          cc.getTemplate()+") -> class ("+
          className+"):");
      e.printStackTrace();
      return null;
    }

    // attach "commConfig"
    comm.setCommunityConfig(cc);

    return comm;
  }
}
