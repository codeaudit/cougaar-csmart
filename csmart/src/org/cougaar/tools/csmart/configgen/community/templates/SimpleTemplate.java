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

