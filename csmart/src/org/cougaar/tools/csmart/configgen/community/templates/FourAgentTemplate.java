/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2001 BBNT Solutions LLC
 * </copyright>
 */


package org.cougaar.tools.csmart.configgen.community.templates;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple Community template with 4 agents: 2 Customers requesting MREs,
 * and 2 Providers.  Provider2 can send requests remotely to other
 * Provider2's.
 * <p>
 * This is the template used for the March demo (4/2001)<br>
 */

public class FourAgentTemplate extends GenericAgentTemplate {

  /** Assets **/
  private static String A1_NAME       = "ClassOneDepot";
  private static Object A1_ROLES      = "SubsistenceProvider";

  /** Tasks **/
  private static String TASK1        = "Supply500MREs";
  private static String WORLD_STATE  = "PEACE";

  /** Customer 1, CONSTANT **/
  private static String  C1_NAME         = "Customer1";
  // Multiply Community's demand by this to get demand
  // level for this agent
  // Total demand for an agent is the number of tasks
  // to generate over simulation
  // Make the baseline 1, and the intermediate file can
  // completely control the number
  // Larger than 1, and the customer is high-demand
  private static long    C1_CUSTOMER_FACTOR = 1;
  // relative importance of each these tasks to the customer's
  // happiness
  private static double  C1_VITAL        = 0.6;
  // Level of chaos in number of customer requests
  private static long    C1_CHAOS        = 0;
  // requested completion time of tasks
  // at least 520 + avgTime from below for success in P1
  // at least 1000 + avgTime from below for success in P2
  private static long    C1_DURATION     = 600000;
  private static String  C1_ALLOC_TASK   = TASK1;
  private static Object  C1_ALLOC_ROLES  = A1_ROLES;
  // Set location from center of community
  private static long    C1_DISTANCE     = 6400; // in meters
  private static float   C1_DIRECTION    = 90; // in degrees relative to N

  /** Customer 2, High Demand **/
  private static String  C2_NAME         = "Customer2";
  // Multiply Community's demand by this to get demand
  // level for this agent
  // Total demand for an agent is the number of tasks
  // to generate over simulation
  // Make the baseline 1, and the intermediate file can
  // completely control the number
  private static long    C2_CUSTOMER_FACTOR = 3;
  private static double  C2_VITAL        = 0.5;
  private static long    C2_CHAOS        = 0;
  // at least 520 + avgTime from below for success in P1
  // at least 1000 + avgTime from below for success in P2
  private static long    C2_DURATION     = 1200000;
  private static String  C2_ALLOC_TASK   = TASK1;
  private static Object  C2_ALLOC_ROLES  = A1_ROLES;
  private static long    C2_DISTANCE     = 3640;
  private static float   C2_DIRECTION    = 75;

  /** Provider 1, Local **/
  private static String  P1_NAME            = "Provider1";
  // supports both C1 and C2, no support for external communities
  private static boolean P1_TALK_REMOTE     = false;
  private static Object  P1_SUPPORTS;
  static {
    List l = new ArrayList(2);
    l.add(C1_NAME);
    l.add(C2_NAME);
    P1_SUPPORTS = l;
  }
  private static long    P1_DISTANCE        = 3200; // meters
  private static float   P1_DIRECTION       = 0; // degrees from north

  private static String P1_LA_NAME          = A1_NAME;
  private static Object P1_LA_ROLES         = A1_ROLES;
  // deplete / production (from interm. file) is percent
  // to deplete inventory on each request.
  // Make this number 100, and the interm. file is specifying
  // the number of simultaneous requests it can handle
  //  private static long    P1_LA_DEPLETE_FACTOR  = 1;
  // Make it smaller, and you increase the number
  // of simultaneous requests possible
  // Make this 1000, and the interm. file is specifying the
  // tenths of a request it can handle
  // simultaneously
  private static long    P1_LA_DEPLETE_FACTOR  = 1300;
  // level of chaos in the local asset
  private static long    P1_LA_INV_CHAOS       = 0;
  private static long    P1_LA_TIME_CHAOS      = 0;
  // time to complete this task on avg. Compare to C*_DURATION.
  // This should be substantially shorter
  // Also compare to Allocator.TRY_TIME (currently 40000)
  // If this becomes substantially larger, then lots of secondary
  // allocations will start failing
  private static long    P1_LA_AVG_TIME        = 300000;
  // allocates only based on asset's role
  private static String  P1_ALLOC_TASK      = TASK1;
  private static Object  P1_ALLOC_ROLES     = P1_LA_ROLES;

  /** Provider 2, Org **/
  private static String  P2_NAME            = "Provider2";
  // supports P1, plus maybe others since (P2_TALK_REMOTE == true)
  private static boolean P2_TALK_REMOTE     = true;
  private static Object  P2_SUPPORTS        = P1_NAME; 
  private static long    P2_DISTANCE        = 1600;
  private static float   P2_DIRECTION       = 180;

  private static String P2_LA_NAME          = A1_NAME;
  private static Object P2_LA_ROLES         = A1_ROLES;
  // deplete / production (from interm. file) is percent
  // to deplete inventory on each request.
  // Make this number 1000, and the interm. file is specifying
  // the number of simultaneous requests it can handle
  // Make it smaller, and you increase the number
  // of simultaneous requests possible
  private static long  P2_LA_DEPLETE_FACTOR  = 750;
  private static long  P2_LA_INV_CHAOS       = 0;
  private static long  P2_LA_TIME_CHAOS      = 0;
  //  private static long    P2_AVG_TIME        = 20;
  // time to complete this task on avg. Compare to C*_DURATION.
  // This should be substantially shorter
  // Also compare to Allocator.TRY_TIME (currently 40000)
  // If this becomes substantially larger, then lots of secondary
  // allocations will start failing
  private static long    P2_LA_AVG_TIME        = 540000;
  // allocates only based on asset's role
  private static String  P2_ALLOC_TASK      = TASK1;
  private static Object  P2_ALLOC_ROLES     = A1_ROLES;


  public FourAgentTemplate() {    
    buildCommunity();
  }

  /**
   * Adds all Customers, Providers and assets to the community.
   */
  protected void buildCommunity() {

    // Customer 1
    addCustomer(
        C1_NAME,               // Agent Name
        C1_CUSTOMER_FACTOR,    // Factor Applied to Demand
        C1_DISTANCE,           // Distance (meters) from center of comm.
        C1_DIRECTION           // Direction (degrees) from center of comm.
               );

    addCustomerTask(
        C1_NAME,           // Agent Name to assign task to
        C1_ALLOC_TASK,     // Name of the task
        WORLD_STATE,       // World state task applies to
        C1_VITAL,          // How vital this task is for happiness
        C1_DURATION,       // Desired length of time to complete task
        C1_CHAOS           // Level of Chaos for this task
                   );

    addAllocation(
        C1_NAME,             // Name of the agent to create allocation for
        C1_ALLOC_TASK,       // Name of the task
        C1_ALLOC_ROLES       // Roles the provider can complete
                 );


    // Customer 2
    addCustomer(
        C2_NAME, C2_CUSTOMER_FACTOR, C2_DISTANCE, C2_DIRECTION);

    addCustomerTask(
        C2_NAME, C2_ALLOC_TASK, WORLD_STATE,
        C2_VITAL, C2_DURATION, C2_CHAOS);

    addAllocation(C2_NAME, C2_ALLOC_TASK, C2_ALLOC_ROLES);



    // Provider 1
    addProvider(
        P1_NAME,                   // Name of the Agent
        P1_TALK_REMOTE,            // is this a provider to remote comms?
        P1_SUPPORTS,               // Names of agents supported within the comm
        P1_DISTANCE,               // Distance (meters) from center of comm
        P1_DIRECTION               // Direction (degrees) from center
               );

    addLocalAsset(
        P1_NAME,                 // Name of agent to assign asset to
        P1_LA_NAME,              // Name of the Local asset
        P1_ALLOC_ROLES,          // List of roles for this asset 
        P1_LA_INV_CHAOS,         // Level of Chaos for the inventory
        P1_LA_TIME_CHAOS,        // Level of Chaos for the complete time
        P1_LA_AVG_TIME,          // Average time to complete task
        P1_LA_DEPLETE_FACTOR     // How much to deplete inventory per req.
                 );

    addAllocation(
        P1_NAME,               // Name of the Agent
        P1_ALLOC_TASK,         // Name of the task
        P1_ALLOC_ROLES         // Roles for this task
                   );

    // Provider 2
    addProvider(
        P2_NAME, P2_TALK_REMOTE, P2_SUPPORTS, P2_DISTANCE, P2_DIRECTION);

    addLocalAsset(
        P2_NAME, P2_LA_NAME, P2_LA_ROLES, P2_LA_INV_CHAOS, 
        P2_LA_TIME_CHAOS, P2_LA_AVG_TIME, P2_LA_DEPLETE_FACTOR);

    addAllocation(
        P2_NAME, P2_ALLOC_TASK, P2_ALLOC_ROLES);
  }

} // FourAgentTemplate
