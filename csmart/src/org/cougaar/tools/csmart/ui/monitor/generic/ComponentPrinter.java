/*
 * <copyright>
 *  Copyright 2000-2002 BBNT Solutions, LLC
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

package org.cougaar.tools.csmart.ui.monitor.generic;

import java.awt.print.*;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import org.cougaar.util.log.Logger;
import org.cougaar.tools.csmart.ui.viewer.CSMART;

/**
 * Borrowed from Cove.
 */
public class ComponentPrinter implements Printable, Pageable {
  private JComponent component;
  private PageFormat format;
  private PrinterJob job;
  private Dimension dimension;
  private Dimension pageDimension;
  private boolean debug = false;
  private boolean disableDoubleBuffering = true;
  private double scale;
  private transient Logger log;

  // Sadly, required for dealing with margins in NT
  private static final int x_margin_fudge_factor = 2;


  public ComponentPrinter(JComponent component, String jobName) {
    log = CSMART.createLogger(this.getClass().getName());
    this.component = component;
    job = PrinterJob.getPrinterJob();
    if (job == null) {
      if(log.isErrorEnabled()) {
	log.error("Couldn't find a printer!");
      }
    } else {
      java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
      scale = 72.0/tk.getScreenResolution();
      job.setJobName(jobName);

      // Bug 1917: See if returned object same as that input or null:
      // if so, user cancelled the window
      format = job.pageDialog(job.defaultPage());
      if (format == null || format.equals(job.defaultPage())) {
	if (log.isDebugEnabled())
	  log.debug("User cancelled print.");
	job = null;
	return;
      }
      // Hmmm. That didn't work...

      Dimension size = component.getSize();
      double frameHeight = size.height * scale;
      double frameWidth = size.width * scale;
      double pageHeight = format.getImageableHeight();
      double pageWidth = format.getImageableWidth();
      pageDimension = new Dimension((int) pageWidth, (int) pageHeight);
      dimension = new Dimension();
      dimension.width = (int) (frameWidth/pageWidth);
      if (frameWidth%pageWidth != 0) dimension.width++;
      dimension.height = (int) (frameHeight/pageHeight);
      if (frameHeight%pageHeight != 0) dimension.height++;
      if(log.isDebugEnabled()) {
	log.debug("Page layout: " + 
		  dimension.width + "," + dimension.height);
      }
      job.setPageable(this);
    }
  }


  public void disableDoubleBuffering(boolean flag) {
    disableDoubleBuffering = flag;
  }

  public void setDebug(boolean flag) {
    debug = flag;
  }

  public boolean isReady() {
    return job != null;
  }

  public void printPages() {
    if (job == null) return;
    try {
      job.print();
    } catch (PrinterException e) {
      if(log.isErrorEnabled()) {
	log.error("Print job failed: ", e);
      }
    }
  }

  // Printable

  public int print(Graphics g, PageFormat format, int page) {
    if (debug) System.out.print("Printing page " + page + "...");
    int x = page%dimension.width;
    int y = page/dimension.width;
    int xTrans = -x*pageDimension.width;
    int yTrans = -y*pageDimension.height;

    // Account for imaging margins.
    // Is this dependent on orientation?
    xTrans += x_margin_fudge_factor*format.getImageableX();
    yTrans += format.getImageableY();

    if(log.isDebugEnabled()) {
      log.debug("Translation: " + xTrans + "," + yTrans);
    }

    Graphics2D g2d = (Graphics2D) g;
    g2d.scale(scale, scale);
    g2d.translate(xTrans, yTrans);
	
    if (disableDoubleBuffering) component.setDoubleBuffered(false);
    component.print(g2d);
    if (disableDoubleBuffering) component.setDoubleBuffered(true);

    if(log.isDebugEnabled()) {
      log.debug("done");
    }

    return Printable.PAGE_EXISTS;
  }


  // This global double-buffer hack doesn't work properly with JTables.
  /*
    private void doubleBuffering(boolean flag) {
    RepaintManager currentManager = 
    RepaintManager.currentManager(component);
    currentManager.setDoubleBufferingEnabled(flag);
    }
  */



  // Pageable

  public int getNumberOfPages() {
    return dimension.width*dimension.height;
  }

  public PageFormat getPageFormat(int page) {
    return format;
  }

  public Printable getPrintable(int page) {
    return this;
  }


}

