/* 
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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
