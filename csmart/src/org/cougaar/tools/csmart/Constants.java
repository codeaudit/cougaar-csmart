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

package org.cougaar.tools.csmart;

/**
 * This is a file of Constants for use in CSMART experiments.
 * They are subdivided into several categories.
 * IA specific constants would be in a Constants file in a different package
 *
 * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
 */
public final class Constants {
  /**
   * These are some of the names you can use in ArgValue pairs,
   * in the details ArrayList of the ExperimentBody
   *
   * @author <a href="mailto:wfarrell@bbn.com">Wilson Farrell</a>
   */
  public static interface Expr {

    // value is an Integer indicating the number of attack steps required to
    // complete the attack
    public static final String STEPS_TAKEN = "Steps-Taken";

    // value is a Long indicating the length of time required to complete the
    // attack
    public static final String TIME_TAKEN = "Time-Taken";

    // The current iteration of the attack as an Integer
    public static final String  ITERATION= "Iteration";

    // The name of the current attack step as a String
    public static final String STEP_NAME = "Step-Name";

    // Whether the attack failed or succeeded.  One of Value.SUCCEED or
    // Value.FAILURE
    public static final String END_STATE = "End-State";
  }

  /**
   * These are some of the values you can use in ArgValue pairs
   * Many Values will not be from this list, but some are
   *
   * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
   */
  public static interface Value {
    // use these as the Status in a CommandResponseBody, for example
    public static final String SUCCESS = "Success"; // eventually maybe an Integer(0)
    public static final String FAILURE = "Failure"; // eventually maybe an Integer(1)
    // Use these for the Arg.PW argument for example
    public static final String VALID = "Valid";
    public static final String INVALID = "Invalid";
    // Use this for the Arg.PERMISSION Argument for example
    public static final String EXECUTABLE = "Executable";

    // experiment body status types

    // issued when the attacker starts a new traversal of the same attack tree
    public static final String ITERATION_CHANGE = "Iteration-Change";

    // issued when the attack has completed
    public static final String ATTACK_COMPLETE = "Attack-Complete";

    // issued when the attacker moves to a new step in the attack tree
    public static final String STATE_CHANGE = "State-Change";

    // issued when an attack commences
    public static final String ATTACK_START = "Attack-Start";

  }

  public static interface Rating {
    // Description of failure.
    public static final String SUCCESS         = "Task Was a Success";
    public static final String FAIL_INV_EMPTY  = "The current inventory is empty";
    public static final String FAIL_DEADLINE   = "Deadline exceeded";
    public static final String FAIL_ALLOCATION = "All possible allocations failed";
    public static final String FAIL_NO_RULE    = "No Allocation Rule Exists";
  }

  // Set up some Relationships to ensure the Entity's work out OK
  public static interface RelationshipType {
    public static final String SUPERIOR_SUFFIX = "Superior";
    public static final String SUBORDINATE_SUFFIX = "Subordinate";

    public static final org.cougaar.domain.planning.ldm.plan.RelationshipType SUPERIOR = 
      org.cougaar.domain.planning.ldm.plan.RelationshipType.create(SUPERIOR_SUFFIX, SUBORDINATE_SUFFIX);

    public static final String PROVIDER_SUFFIX = "Provider";
    public static final String CUSTOMER_SUFFIX = "Customer";
    public static final org.cougaar.domain.planning.ldm.plan.RelationshipType PROVIDER = 
      org.cougaar.domain.planning.ldm.plan.RelationshipType.create(PROVIDER_SUFFIX, CUSTOMER_SUFFIX);
  }

  // Pre-define some Roles to ensure our relationships come out OK
  public static class Role {
    /**
     * Insure that Role constants are initialized. Actually does
     * nothing, but the classloader insures that all static
     * initializers have been run before executing any code in this
     * class.
     **/
    public static void init() {
    }

    static {
      org.cougaar.domain.planning.ldm.plan.Role.create("Self", "Self");
      org.cougaar.domain.planning.ldm.plan.Role.create("", RelationshipType.SUPERIOR);
      org.cougaar.domain.planning.ldm.plan.Role.create("Food", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("SubsistenceSupply", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("Subsistence", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("Supply", RelationshipType.PROVIDER);
    }
    
    public static final org.cougaar.domain.planning.ldm.plan.Role SELF = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Self");

    public static final org.cougaar.domain.planning.ldm.plan.Role SUPERIOR = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole(RelationshipType.SUPERIOR_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role SUBORDINATE =
      org.cougaar.domain.planning.ldm.plan.Role.getRole(RelationshipType.SUBORDINATE_SUFFIX);
   public static final org.cougaar.domain.planning.ldm.plan.Role FOODPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Food" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role FOODCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Food" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role SUPPLYPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Supply" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role SUPPLYCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Supply" + 
                                RelationshipType.CUSTOMER_SUFFIX);

   public static final org.cougaar.domain.planning.ldm.plan.Role SUBSISTENCEPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Subsistence" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role SUBSISTENCECUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Subsistence" + 
                                RelationshipType.CUSTOMER_SUFFIX);
   public static final org.cougaar.domain.planning.ldm.plan.Role SUBSISTENCESUPPLYPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("SubsistenceSupply" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role SUBSISTENCESUPPLYCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("SubsistenceSupply" + 
                                RelationshipType.CUSTOMER_SUFFIX);
  }
  
  /**
   * Constants defining types of attacks on a network.
   * @author <a href="mailto:ahelsing@bbn.com">Aaron Helsinger</a>
   */
  public static interface RWEType {
    public static final String DOSNODE = "DoS Node";
    public static final String DOSNET = "DoS Network";
    public static final String KILLNODE = "Kill Node";
    public static final String ISOLATENODE = "Isolate Node";
    public static final String FLOOD = "H2O flood";
    public static final String BOMB = "Bomb blast";
    public static final String EARTHQUAKE = "Earthquake";
  }

  /**
   * Valid <tt>InfrastructureEvent.getType()</tt> values.
   */
  public static interface InfEventType {
    public static final String WIRE_BUSY = "Wire Busy";
    public static final String WIRE_DOWN = "Wire Down";
    public static final String NODE_BUSY = "Node Busy";
    public static final String NODE_DOWN = "Node Down";
  }
} // Constants
