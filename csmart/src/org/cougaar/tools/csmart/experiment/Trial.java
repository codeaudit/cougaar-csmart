/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.tools.csmart.experiment;

import org.cougaar.tools.csmart.core.property.ModifiableConfigurableComponent;
import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A trial represents a run of an Experiment. In future it
 * may represent a particular configuration variation.
 * Currently it is a holder for TrialResults.
 **/
public class Trial extends ModifiableConfigurableComponent implements Serializable {
  private static final String DESCRIPTION_PROPERTY = "description";
  private static final String RESULTS_PROPERTY = "results";
  private ArrayList trialResults; // array of TrialResult

  private boolean editable = false;

  private transient Logger log;

  /**
   * Construct a trial with the given name.
   * @param name
   */
  public Trial(String name) {
    super(name);
    trialResults = new ArrayList();
    createLogger();
  }

  private void createLogger() {
    log = CSMART.createLogger(this.getClass().getName());
  }

  public void initProperties() {
    addProperty(DESCRIPTION_PROPERTY, "");
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

  public boolean isEditable() {
    return this.editable;
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }
}
