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

package org.cougaar.tools.csmart.experiment;

import java.io.Serializable;
import java.util.ArrayList;

import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.core.property.Property;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

public class Trial extends ModifiableConfigurableComponent implements Serializable {
  private static final String DESCRIPTION_PROPERTY = "description";
  private static final String PARAMETERS_PROPERTY = "parameters";
  private static final String VALUES_PROPERTY = "values";
  private static final String RESULTS_PROPERTY = "results";
  private ArrayList trialParameters; // array of properties
  private ArrayList trialValues; // array of objects; values of trialParameters
  private ArrayList trialResults; // array of TrialResult

  private boolean editable = false;

  private transient Logger log;

  /**
   * Construct a trial with the given name.
   * @param name
   */

  public Trial(String name) {
    super(name);
    trialParameters = new ArrayList();
    trialValues = new ArrayList();
    trialResults = new ArrayList();
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void initProperties() {
    addProperty(DESCRIPTION_PROPERTY, "");
    addProperty(PARAMETERS_PROPERTY, trialParameters);
    addProperty(VALUES_PROPERTY, trialValues);
    addProperty(RESULTS_PROPERTY, trialResults);
  }

  /**
   * Note that name is inherited from ConfigurableComponent,
   * so there is no need for get name methods here.
   */

  /**
   * Set a text description for this trial.
   * @param description description of the trial
   */
  public void setTextDescription(String description) {
    getProperty(DESCRIPTION_PROPERTY).setValue(description);
  }

  /**
   * Get the text description of this trial.
   * @return description of the trial
   */
  public String getTextDescription() {
    return (String)getProperty(DESCRIPTION_PROPERTY).getValue();
  }

  /**
   * Get results of all runs of this trial.
   * @return array of trial results (timestamp and results location)
   */
  public TrialResult[] getTrialResults() {
    return (TrialResult[])trialResults.toArray(new TrialResult[trialResults.size()]);
  }

  /**
   * Add result of one run of this trial.
   * @param result timestamp (when trial was run) and results location
   */

  public void addTrialResult(TrialResult result) {
    trialResults.add(result);
    getProperty(RESULTS_PROPERTY).setValue(trialResults);
  }

  public void removeTrialResult(TrialResult result) {
    trialResults.remove(result);
    getProperty(RESULTS_PROPERTY).setValue(trialResults);
  }

  /**
   * Add a parameter to this trial.
   * @param parameter a property whose value is being changed in the experiment
   * @param value the value for the property for this trial
   */

  public void addTrialParameter(Property parameter, Object value) {
    trialParameters.add(parameter);
    trialValues.add(value);
    getProperty(PARAMETERS_PROPERTY).setValue(trialParameters);
    getProperty(VALUES_PROPERTY).setValue(trialValues);
  }

  /**
   * Set the value of the property in this trial, or
   * add a new property to this trial.
   * @param name a property whose value is being changed in the experiment
   * @param value the value for the property in this trial
   */

  public void setTrialParameter(Property parameter, Object value) {
    for (int i = 0; i < trialParameters.size(); i++) {
      Property param = (Property)trialParameters.get(i);
      if (param.equals(parameter)) {
	trialValues.set(i, value);
	getProperty(VALUES_PROPERTY).setValue(trialValues);
	return;
      }
    }
    addTrialParameter(parameter, value);
  }

  public void removeTrialParameter(Property parameter) {
    int i = trialParameters.indexOf(parameter);
    trialParameters.remove(i);
    trialValues.remove(i);
    getProperty(PARAMETERS_PROPERTY).setValue(trialParameters);
    getProperty(VALUES_PROPERTY).setValue(trialValues);
  }

  public int getNumberOfParameters() {
    return trialParameters.size();
  }

  public Property[] getParameters() {
    return (Property[])trialParameters.toArray(new Property[trialParameters.size()]);
  }

  public Object[] getValues() {
    return (Object[])trialValues.toArray(new Object[trialValues.size()]);
  }

  public boolean isEditable() {
    return this.editable;
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  public void printParametersAndValues() {
    StringBuffer sb = new StringBuffer(200);
    for (int i = 0; i < trialParameters.size(); i++) {
      sb.append(((Property)trialParameters.get(i)).getName());
      sb.append(",");
    }
    if(log.isDebugEnabled()) {
      log.info(sb.substring(0, sb.length()-1));
    }
    sb.setLength(0);
    for (int i = 0; i < trialValues.size(); i++) {
      sb.append(trialValues.get(i).toString());
      sb.append(",");
    }
    if(log.isDebugEnabled()) {
      log.info(sb.substring(0, sb.length()-1));
    }
  }

}
