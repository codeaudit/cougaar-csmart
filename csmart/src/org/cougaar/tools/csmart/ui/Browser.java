/* 
 * <copyright>
 * This software is to be used only in accordance with the COUGAAR license
 * agreement. The license agreement and other information can be found at
 * http://www.cougaar.org
 * 
 *       © Copyright 2001 by BBNT Solutions LLC.
 * </copyright>
 */
package org.cougaar.tools.csmart.ui;

import java.net.URL;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

public class Browser extends JFrame implements HyperlinkListener {
    private static final Dimension DEFAULT_SIZE = new Dimension(600, 800);
    private static final String BROWSER_TITLE = "CSMART Help Browser";
    private static Browser singleton;

    private JTextPane text = new JTextPane() {
        protected void scrollToReference(String ref) {
            super.scrollToReference(ref);
            selectReference(ref);
        }
    };
    private JScrollPane scroll = new JScrollPane();

    private Browser() {
        super(BROWSER_TITLE);
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
            ioe.printStackTrace();
        }
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            setURL(e.getURL());
        }
    }

    private synchronized static void clearSingleton() {
        singleton = null;
    }

    public synchronized static void setPage(URL url) {
        if (singleton == null) {
            singleton = new Browser();
        } else {
            singleton.setState(JFrame.NORMAL);
        }
        singleton.setURL(url);
    }
}
