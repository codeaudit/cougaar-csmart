/* 
 * <copyright>
 *  Copyright 2001,2002 BBNT Solutions, LLC
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
package org.cougaar.tools.csmart.ui;

import org.cougaar.tools.csmart.ui.viewer.CSMART;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

public class Browser extends JFrame implements HyperlinkListener {
  private static final Dimension DEFAULT_SIZE = new Dimension(600, 800);
  private static final String BROWSER_TITLE = "CSMART Help Browser";
  private static Browser singleton;
  private Logger log;

  private JTextPane text = new JTextPane() {
      public void scrollToReference(String ref) {
	super.scrollToReference(ref);
	selectReference(ref);
      }
    };
  private JScrollPane scroll = new JScrollPane();

  private Browser() {
    super(BROWSER_TITLE);
    log = CSMART.createLogger(getClass().getName());
    scroll.setViewport(new JViewport() {
	public void scrollRectToVisible(Rectangle r) {
	  Dimension extent = getExtentSize();
	  r = new Rectangle(r.x, r.y, r.width, extent.height);
	  super.scrollRectToVisible(r);
	}
      });
    scroll.setViewportView(text);
    getContentPane().add(scroll);
    addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {
	  dispose();
	}
      });
    text.setEditable(false);
    scroll.setPreferredSize(DEFAULT_SIZE);
    pack();
    show();
  }

  public void dispose() {
    clearSingleton();
    super.dispose();
  }

  private HTMLDocument.Iterator getIterator(HTML.Tag tag) {
    Document doc = text.getDocument();
    if (doc instanceof HTMLDocument) {
      HTMLDocument html = (HTMLDocument) doc;
      return html.getIterator(tag);
    }
    return null;
  }

  private void selectReference(String ref) {
    HTMLDocument.Iterator i = getIterator(HTML.Tag.A);
    if (i != null) {
      for (; i.isValid(); i.next()) {
	AttributeSet a = i.getAttributes();
	String nm = (String) a.getAttribute(HTML.Attribute.NAME);
	if ((nm != null) && nm.equals(ref)) {
	  // found a matching reference in the document.
	  text.select(i.getStartOffset(), i.getEndOffset());
	}
      }
    }
  }

  public void setURL(URL url) {
    try {
      text.setPage(url);
      toFront();
    } catch (IOException ioe) {
      if(log.isErrorEnabled()) {
        log.error("Exception", ioe);
      }
    }
  }

  public void setText(String data) {
    text.setText(data);
    toFront();
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      setURL(e.getURL());
    }
  }

  private static synchronized void clearSingleton() {
    singleton = null;
  }

  public static synchronized void setPage(URL url) {
    if (singleton == null) {
      singleton = new Browser();
    } else {
      singleton.setState(JFrame.NORMAL);
    }
    singleton.setURL(url);
  }

  public static synchronized void setPage(String text) {
    if (singleton == null) {
      singleton = new Browser();
    } else {
      singleton.setState(JFrame.NORMAL);
    }
    singleton.setText(text);
  }
}
