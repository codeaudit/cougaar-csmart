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

import java.io.File;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
 * A convenience implementation of FileFilter that filters out
 * all files except for those type extensions that it knows about.
 *
 * Extensions are of the type ".foo", which is typically found on
 * Windows and Unix boxes, but not on Macinthosh. Case is ignored.
 *
 * Example - create a new filter that filters out all files
 * but gif and jpg image files:
 *
 *     JFileChooser chooser = new JFileChooser();
 *     ExtensionFileFilter filter = new ExtensionFileFilter(
 *                   new String{"gif", "jpg"}, "JPEG & GIF Images")
 *     chooser.addChoosableFileFilter(filter);
 *     chooser.showOpenDialog(this);
 *
 * From JDK1.2.1 demo.
 */

public class ExtensionFileFilter extends FileFilter {

//     private static String TYPE_UNKNOWN = "Type Unknown";
//     private static String HIDDEN_FILE = "Hidden File";

    private Hashtable filters = null;
    private String description = null;
    private String fullDescription = null;
    private boolean useExtensionsInDescription = true;

    /**
     * Creates a file filter. If no filters are added, then all
     * files are accepted.
     *
     * @see #addExtension
     */
    public ExtensionFileFilter() {
	this.filters = new Hashtable();
    }

    /**
     * Creates a file filter that accepts files with the given extension.
     * Example: new ExtensionFileFilter("jpg");
     *
     * @see #addExtension
     */
    public ExtensionFileFilter(String extension) {
	this(extension,null);
    }

    /**
     * Creates a file filter that accepts the given file type.
     * Example: new ExtensionFileFilter("jpg", "JPEG Image Images");
     *
     * Note that the "." before the extension is not needed. If
     * provided, it will be ignored.
     *
     * @see #addExtension
     */
    public ExtensionFileFilter(String extension, String description) {
	this();
	if(extension!=null) addExtension(extension);
 	if(description!=null) setDescription(description);
    }

    /**
     * Creates a file filter from the given string array.
     * Example: new ExtensionFileFilter(String {"gif", "jpg"});
     *
     * Note that the "." before the extension is not needed and
     * will be ignored.
     *
     * @see #addExtension
     */
    public ExtensionFileFilter(String[] filters) {
	this(filters, null);
    }

    /**
     * Creates a file filter from the given string array and description.
     * Example: new ExtensionFileFilter(String {"gif", "jpg"}, "Gif and JPG Images");
     *
     * Note that the "." before the extension is not needed and will be ignored.
     *
     * @see #addExtension
     */
    public ExtensionFileFilter(String[] filters, String description) {
	this();
	for (int i = 0; i < filters.length; i++) {
	    // add filters one by one
	    addExtension(filters[i]);
	}
 	if(description!=null) setDescription(description);
    }

    /**
     * Return true if this file should be shown in the directory pane,
     * false if it shouldn't.
     *
     * Files that begin with "." are ignored.
     *
     * @see #getExtension
     * @see FileFilter#accept
     */
    public boolean accept(File f) {
	if(f != null) {
	    if(f.isDirectory()) {
		return true;
	    }
	    String extension = getExtension(f);
	    if(extension != null && filters.get(getExtension(f)) != null) {
		return true;
	    };
	}
	return false;
    }

    /**
     * Return the extension portion of the file's name .
     *
     * @see #getExtension
     * @see FileFilter#accept
     */
     public String getExtension(File f) {
	if(f != null) {
	    String filename = f.getName();
	    int i = filename.lastIndexOf('.');
	    if(i>0 && i<filename.length()-1) {
		return filename.substring(i+1).toLowerCase();
	    };
	}
	return null;
    }

    /**
     * Adds a filetype "dot" extension to filter against.
     *
     * For example: the following code will create a filter that filters
     * out all files except those that end in ".jpg" and ".tif":
     *
     *   ExtensionFileFilter filter = new ExtensionFileFilter();
     *   filter.addExtension("jpg");
     *   filter.addExtension("tif");
     *
     * Note that the "." before the extension is not needed and will be ignored.
     */
    public void addExtension(String extension) {
	if(filters == null) {
	    filters = new Hashtable(5);
	}
	filters.put(extension.toLowerCase(), this);
	fullDescription = null;
    }


    /**
     * Returns the human readable description of this filter. For
     * example: "JPEG and GIF Image Files (*.jpg, *.gif)"
     *
     * @see #setDescription
     * @see #setExtensionListInDescription
     * @see #isExtensionListInDescription
     * @see FileFilter#getDescription
     */
    public String getDescription() {
	if(fullDescription == null) {
	    if(description == null || isExtensionListInDescription()) {
 		fullDescription = description==null ? "(" : description + " (";
		// build the description from the extension list
		Enumeration extensions = filters.keys();
		if(extensions != null) {
		  //		    fullDescription += "." + (String) extensions.nextElement();
		  fullDescription += (String) extensions.nextElement();
		    while (extensions.hasMoreElements()) {
			fullDescription += ", " + (String) extensions.nextElement();
		    }
		}
		fullDescription += ")";
	    } else {
		fullDescription = description;
	    }
	}
	return fullDescription;
    }

    /**
     * Sets the human readable description of this filter. For
     * example: filter.setDescription("Gif and JPG Images");
     *
     * @see #setDescription
     * @see #setExtensionListInDescription
     * @see #isExtensionListInDescription
     */
    public void setDescription(String description) {
	this.description = description;
	fullDescription = null;
    }

    /**
     * Determines whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     *
     * Only relevent if a description was provided in the constructor
     * or using setDescription();
     *
     * @see #getDescription
     * @see #setDescription
     * @see #isExtensionListInDescription
     */
    public void setExtensionListInDescription(boolean b) {
	useExtensionsInDescription = b;
	fullDescription = null;
    }

    /**
     * Returns whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     *
     * Only relevent if a description was provided in the constructor
     * or using setDescription();
     *
     * @see #getDescription
     * @see #setDescription
     * @see #setExtensionListInDescription
     */
    public boolean isExtensionListInDescription() {
	return useExtensionsInDescription;
    }
}
