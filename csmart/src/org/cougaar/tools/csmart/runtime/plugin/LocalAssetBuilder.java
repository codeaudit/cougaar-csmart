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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.tools.csmart.runtime.ldm.asset.*;
import org.cougaar.tools.csmart.util.parser.SimpleParser;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * The <code>LocalAssetBuilder</code> constructs <code>LocalAsset</code>s
 * from a configuration file.
 * <br>
 * The asset builder is responsible for reading in the config file and
 * creating and publishing new Local Assets from the data 
 * in the config file.  The config file is a comma-delimited file.
 * The order of the values is very important!  Data file format is
 * detailed below.
 * <pre>
 * The format for the LocalAsset config file is:
 * 
 * [ItemId], [Dec Amt], [Avg Time], [Inv Dev], [Time Dev], [Roles...]
 * 
 * [ItemId] -- The ItemIdentification of the local Asset.
 * [Dec Amt] -- The amount to decrement the inventory level.
 * [Avg Time] -- The Average time it takes to satisify a request.
 * [Inv Dev] -- The standard-deviation from the inventory rate.
 * [Time Dev] -- The standard-deviation from the Time Rate.
 * [Roles...] -- A comma-separated list of Roles for the asset.
 *
 * Multiple lines can be specified to create multiple LocalAssets.
 *
 * Example:
 *   id-123, 5, 90, 3,  9, FooRole, BarRole
 *   id-xyz, 7, 10, 2, 14, X
 * will create two LocalAssets.  The first asset, with IdemId "id-123",
 * has two roles in it's "RolesPG" -- "FooRole" and "BarRole".
 * </pre>
 */
public class LocalAssetBuilder
    extends CSMARTPlugIn {

  /**
   * Name of the prototype for LocalAssets.
   * <p>
   * Note that all LocalAssets in a Cluster should have unique ItemIds,
   * as defined in the configuration file.
   */
  private static final String PROTO_NAME = "LOCAL_ASSET";

  private transient Logger log;
  //
  // Could override <tt>load(..)</tt> and get the logger, like 
  // <tt>CSMARTPlugIn.load(..)</tt>, but for now it's not required.
  //

  /**
   * Read params and build the LocalAssets for this Cluster -- This PlugIn 
   * must be listed <u>FIRST</u> in the "*.INI" file!.
   */
  public void setupSubscriptions() {
    log = CSMART.createLogger(this.getClass().getName());
    List pv = getParameters() != null ? new ArrayList(getParameters()) : null;
    int pvSize = ((pv != null) ? pv.size() : 0);
    if (pvSize > 0) {
      createLocalAssets((String)pv.get(0));
    }
  }

  public void execute() {
    // never execute
  }

  /**
   * Parses the file "filename" and creates/publishes new LocalAssets.
   * 
   * @param filename name of file/URL that defines the assets
   */
  private void createLocalAssets(String filename) {
    SimpleParser sp = new SimpleParser();

    try {
      sp.load(filename);
      sp.parse();
    } catch(IOException e) {
    }

    List list = sp.getList();
    int n = ((list != null) ? list.size() : 0);
    if (n <= 0) {
      // zero local assets
      return;
    }

    // create the local asset prototype
    Asset proto = 
      theLDMF.createPrototype(
          "org.cougaar.tools.csmart.runtime.ldm.asset.LocalAsset",
          PROTO_NAME);

    // create the local asset instances
    //
    // could keep a Set of itemIds and make sure they are unique...
    for (int i = 0; i < list.size(); i++) {
      List entry = (List)list.get(i);

      String itemId;
      List roles;
      double decrementRate;
      long averageTime;
      double inventoryDeviation;
      double timeDeviation;

      try {
        itemId = (String)entry.get(0);
        decrementRate = Double.parseDouble((String)entry.get(1));
        averageTime = Long.parseLong((String)entry.get(2));
        inventoryDeviation = Double.parseDouble((String)entry.get(3));
        timeDeviation = Double.parseDouble((String)entry.get(4));
        roles = new ArrayList(entry.size());
        for (int j = 5; j < entry.size(); j++) {
          roles.add(Role.getRole((String)entry.get(j)));
        }
      } catch (Exception e) {
        // should log!
	if (log.isDebugEnabled()) {
	  log.debug("Invalid LocalAsset specification["+i+"]: "+entry);
	}
        continue;
      }

      LocalAsset la = 
        createLocalAsset(
            proto,
            itemId,
            roles,
            decrementRate,
            averageTime,
            inventoryDeviation,
            timeDeviation);

      publishAdd(la);
    }
  }

  private LocalAsset createLocalAsset(
      Asset proto,
      String itemId,
      List roles,
      double decrementRate,
      long averageTime,
      double inventoryDeviation,
      double timeDeviation) {

    // create a ThermalResourceModelBG
    ThermalResourceModelBG trmBG = 
      new ThermalResourceModelBG();
    trmBG.setDecrementRate(decrementRate);
    trmBG.setAverageTime(averageTime);
    trmBG.setInventoryDeviation(inventoryDeviation);
    trmBG.setTimeDeviation(timeDeviation);

    // initialize at full-capacity
    trmBG.setInventoryLevelAt(
        currentTimeMillis(), 
        ThermalResourceModelBG.MAX_VALUE);

    // the local asset will use the thermal model for inventory
    return
      createLocalAsset(
          proto,
          itemId, 
          roles,
          trmBG);
  }

  private LocalAsset createLocalAsset(
      Asset proto,
      String itemId,
      List roles,
      SimpleInventoryBG siBG) {

    // create the instance
    LocalAsset la = (LocalAsset)
      theLDMF.createInstance(
          proto,
          itemId);

    // set the inventory model
    NewSimpleInventoryPG nsiPG = (NewSimpleInventoryPG)
      theLDMF.createPropertyGroup(
          SimpleInventoryPG.class);
    nsiPG.setInvBG(siBG);
    la.setSimpleInventoryPG(nsiPG);

    // set the roles
    NewRolesPG nrPG = (NewRolesPG)
      theLDMF.createPropertyGroup(
          RolesPG.class);
    nrPG.setRoles(roles);
    la.setRolesPG(nrPG);

    return la;
  }

}
