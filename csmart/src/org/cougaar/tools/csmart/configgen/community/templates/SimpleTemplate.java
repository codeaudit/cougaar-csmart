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

import java.util.*;

/**
 * Template for a simple Community that has one <b>Customer</b> and one
 * <b>Provider</b>.  There is one Asset: <b>REMOTE</b> with with role:
 * <b>ROLE_A</b> and one task <b>TASK_A</b>.
 */

public class SimpleTemplate extends GenericAgentTemplate {

  public SimpleTemplate() {
    buildCommunity();
  }

  protected void buildCommunity() {
    addCustomer(
        "Customer",    // Name
        1,             // Factor
        6400,          // Distance from center of community (meters)
        10f            // Direction in Degrees from center
               );

    addCustomerTask(
        "Customer",  // Agent Name
        "TASK_A",    // Task Name
        "PEACE",     // World State
        0.2,         // How vital this task is
        50,          // Duration of this task
        0            // Chaos Level
                   );

    addAllocation(
        "Customer",  // Name of the Agent to create an allocation for
        "TASK_A",    // Name of task to allocate
        "ROLE_A"     // Roles the provider can complete
                 );

    addProvider(
        "Provider",    // Provider Name
        true,          // Talks to other communities
        "Customer",    // Supports the customer
        1600,          // Distance (meters) from community center
        90f            // Direction (degrees) from center
               );

    addAllocation(
        "Provider", // Agent Name
        "TASK_A",   // Task Name
        "ROLE_A"    // Task Roles
                   ); 

    addLocalAsset(
        "Provider",  // Agent Name
        "REMOTE",    // Asset Name
        "ROLE_A",    // Roles for this asset
        0,           // Inventory Chaos
        10,          // Time Chaos
        50,          // Average Complete Time
        2            // Deplete Factor
                 );

  }

} // SimpleTemplate

