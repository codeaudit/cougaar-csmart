/*
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 *
 * © Copyright 2000, 2001 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.tools.csmart.ui.experiment;

import org.cougaar.tools.csmart.ui.viewer.Organizer;

public class MoMetric implements Metric {
    private String name;

    public MoMetric(String name) {
        this.name = name;
    }
    public void setName(String newName) {
        name = newName;
    }
    public String getName() {
        return name;
    }
    public String toString() {
        return name;
    }
    public Metric copy(Organizer organizer, Object context) {
      return organizer.copyMetric(new MoMetric(organizer.generateMetricName(name)),
				  context);
    }
}
