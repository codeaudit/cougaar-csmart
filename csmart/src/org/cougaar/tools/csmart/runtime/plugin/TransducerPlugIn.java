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
package org.cougaar.tools.csmart.runtime.plugin;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Vector;
import java.util.Iterator;
import java.util.Enumeration;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.EmptyIterator;

import org.cougaar.core.blackboard.IncrementalSubscription;

import org.cougaar.tools.csmart.runtime.ldm.plugin.transducer.Society;
import org.cougaar.tools.csmart.runtime.ldm.plugin.transducer.Agent;
import org.cougaar.tools.csmart.runtime.ldm.event.RealWorldEvent;
import org.cougaar.tools.csmart.runtime.ldm.event.ImpactModel;
import org.cougaar.tools.csmart.runtime.ldm.event.InfrastructureEvent;
import org.cougaar.tools.csmart.runtime.ldm.event.NewInfrastructureEvent;
import org.cougaar.tools.csmart.runtime.ldm.event.IEFactory;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

/**
 * Translate <code>RealWorldEvent</code>s into <code>InfrastructureEvent</code>s<br>
 * Said another way, calculate the impact on the modeled society,
 * of the real world events it sees.
 * Each RWE provides a model, which is given a representation of the modeled
 * society.  From that information, the Event calculates all of the resulting
 * InfEs which result.
 * This PlugIn then publishes each of those events.<br>
 * <br>
 * The society model is built from a configuration file, whose name
 * is a parameter to this plugin.
 * The initialization file has the format: <pre>
 * AgentName, LatInDecimalDegrees, LonInDecimalDegrees[, other fields]
 * </pre><br>
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 * @see CSMARTPlugIn
 * @see ImpactModel
 * @see InfrastructureEvent
 */
public class TransducerPlugIn extends CSMARTPlugIn {
  private IncrementalSubscription rweSub;

  private transient Logger log;

  private Society world = null;
  
  // Subscribe to all RealWorldEvents
  private UnaryPredicate rweP = new UnaryPredicate() {
      public boolean execute(Object o) {
	return (o instanceof RealWorldEvent);
      }
    };
  
  private IEFactory theIEF; // Special factory that only creates InfEvts
  
  /**
   * Create the society from data given on initialization
   * and subscribe to RealWorldEvents
   */
  public void setupSubscriptions() {
    log = CSMART.createLogger(this.getClass().getName());

    world = new Society();
    theIEF = new IEFactory(theCSMARTF);
    
    // This reads in the data file and fills in the world
    parseParameters();

    // Subscribe to RealWorldEvents
    rweSub = (IncrementalSubscription) subscribe(rweP);
  } // end of setupSubscritions()

  /**
   * For each new <code>RealWorldEvent</code>, give its model the society
   * representation, and the special limited factory.<br>
   * For each <code>InfrastructureEvent</code> that it gives back, publish it.
   */
  public void execute() {
    if(log.isDebugEnabled()) {
      String msg = "execute: " + this;
      log.debug(msg);
    }
    long currentTime = currentTimeMillis();

    Enumeration addedRWEs = rweSub.getAddedList();
    while(addedRWEs.hasMoreElements()) {
      RealWorldEvent rwe = (RealWorldEvent)addedRWEs.nextElement();
      if (rwe == null) {
	// error!
	if (log.isDebugEnabled()) {
	  log.error("execute: " + this + " got null RWE on subscription");
	}
	continue;
      }

      ImpactModel model = rwe.getModel();
      if (model == null) {
	// error!
	if (log.isDebugEnabled()) {
	  log.error("execute: " + this + " got null RWE model from event: " + rwe);
	}
	continue;
      }
      if (log.isDebugEnabled()) {
	log.debug("execute: " + this + ": about to get impacts of " + rwe);
      }
      // give the model the world and a factory
      Iterator effects = model.getImpact(world, theIEF);
      // the effects should already have Time set
      // as well as the IE specific fields
      
      if (effects == null) {
	// error!  No impact!
	if (log.isDebugEnabled()) {
	  log.debug("execute: " + this + " null effects on the world for " + rwe);
	}
	continue;
      } else if (effects.equals(EmptyIterator.iterator())) {
	// error!  No impact!
	if (log.isDebugEnabled()) {
	  log.debug("execute: " + this + " empty Iterator effects on the world for " + rwe);
	}
	continue;
      } else if (!(effects.hasNext())) {
	// error!  No impact!
	if (log.isDebugEnabled()) {
	  log.debug("execute: " + this + " iterator wo next effects on the world for " + rwe);
	}
	continue;
      } else {
	if (log.isDebugEnabled()) {
	  log.debug("execute: " + this + " got effects for " + rwe);
	}
	for ( ; effects.hasNext(); ) {
	  // publish each of the events
	  InfrastructureEvent ie = (InfrastructureEvent)effects.next();
	  if (ie != null) {
	    // log something
	    if (log.isDebugEnabled()) {
	      log.debug("execute: " + this + " publishing effect: " + ie);
	    }
	    // Right here, I could not publish ies destined to a Customer Agent
	    // Note that isolating a customer node is OK, just not
	    // effecting the Node itself
	    if (ie.getDestination().toString().indexOf("Customer") != -1) {
	      // This is heading to a customer agent!
	      if (ie.isNodeType()) {
		// to effect the Node (vs the wire)
//  		if (log.isDebugEnabled()) {
//  		  log.debug("execute: " + this + " not publishing effect on Customer agent node: " + ie);
//  		}
//  		continue;
	      }
	    }

	    // Make sure all the fields are filled in:
	    ((NewInfrastructureEvent)ie).setSource(getAgentIdentifier());
	    ((NewInfrastructureEvent)ie).setPublisher(this.toString());
	    // ie.setPlan(???)???
	    
	    // Should this be publishAddAfter(ie, ie.getTime())???
	    //	    publishAdd(ie);
	    publishAddAt(ie, ie.getTime());
	  } else {
	    if (log.isDebugEnabled()) {
	      log.error("execute: " + this + " got null IE as an effect!");
	    }
	  }
	} // end of loop to publish the IEs
      }
    } // end of loop over rweSub
  } // end of execute()

  /**
   * Parse the single input parameter - the name of the society data file.
   * Then open and read that file
   * The format should be: <pre>
   * AgentName, LatInDecimalDegrees, LonInDecimalDegrees[, other fields]
   * </pre>
   */
  private void parseParameters() {
    Vector pv = getParameters() != null ? new Vector(getParameters()) : null;
    if (pv == null) {
      throw new RuntimeException("TranducerPlugIn expects parameters, got none");
    }
    String fileName = (String)pv.elementAt(0);
    if (fileName == null) {
      throw new RuntimeException("TransducerPlugIn expected param[0] to be a data file name, got none");
    }

    // Finished getting parameter.  Now open the file    
    InputStream input = null;
    try {
      input = getConfigFinder().open(fileName);
    } catch (IOException e) {
      throw new RuntimeException("TransducerPlugIn unable to open file " + fileName);
    }
    
    // Now read the file
    if (input != null) {
      BufferedReader in = new BufferedReader(new InputStreamReader(input));
      try {
	while(in.ready()) {
	  String line = in.readLine();
	  if (line == null) {
	    break;
	  } else {
	    // Skip comment lines
	    try {
	      if (line.charAt(0) != '#') {
		// What about lines of all blanks?
		// create a new Agent
		Agent newAg = processInputLine(line);
		if (newAg != null) {
		  if (log.isDebugEnabled()) {
		    log.debug("parseParameters: " + this + " adding Agent to the world: " + newAg);
		  }
		  world.addAgent(newAg);
		}
	      }
	    } catch(IndexOutOfBoundsException e) {
	      if(line.length() == 0) {
		// Empty line with a CR. This is fin.
	      } else {
                if(log.isDebugEnabled()) {
                  log.error("Got an expection parsing input file.", e);
                }
	      }
	    }
	  }
	}
	in.close();
      } catch (IOException e) {
	// error reading input data!
	if (log.isDebugEnabled()) {
	  log.error("parseParameters: " + this + " failed to read input data.");
	}
      }
    }
  } // end of parseParameters

  // Read one line from the input file, returning the new Agent
  private Agent processInputLine(String line) {
    Vector words = org.cougaar.util.StringUtility.parseCSV(line);
    if (words.size() < 3) {
      // error -- need at least 3 words on the line!

      // log something
      if (log.isDebugEnabled()) {
	log.error("processInputLine: " + this + " got Agent description missing elements: " + line);
      }
      return null;
    }

    String name = (String)words.elementAt(0);
    float lat = 0.0f;
    float lon = 0.0f;
    try {
      lat = Float.parseFloat((String)words.elementAt(1));
      lon = Float.parseFloat((String)words.elementAt(2));
    } catch (NumberFormatException nfe) {
      if (log.isDebugEnabled()) {
	log.error("processInputLine: " + this + " got non-float Lat or Lon: " + (String)words.elementAt(1) + ", " + (String)words.elementAt(2));
      }
      return null;
    }
    // get other fields here
    return new Agent(name, lat, lon);
  } // end of ProcessInputLine
} // end of TransducerPlugIn


