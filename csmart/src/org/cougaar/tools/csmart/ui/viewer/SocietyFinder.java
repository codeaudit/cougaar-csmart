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

package org.cougaar.tools.csmart.ui.viewer;

import org.cougaar.tools.csmart.util.FileParseUtil;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.StringUtility;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Most of this was taken from ConfigFinder.  ConfigFinder is used
 * to find the society file that the user chooses, so we want to open
 * a file chooser on the first path that ConfigFinder will use.
 * @see org.cougaar.util.ConfigFinder
 */
public final class SocietyFinder {
  private static final String defaultConfigPath = 
    "$CWD;$HOME/.alp;$INSTALL/configs/$CONFIG;$INSTALL/configs/common";
  private static List configPath = new ArrayList();
  private static Map defaultProperties;
  private static Map properties = null;
  private static SocietyFinder defaultSocietyFinder;

  static {
    Map m = new HashMap();
    defaultProperties = m;

    File ipf = new File(System.getProperty("org.cougaar.install.path", "."));
    try { ipf = ipf.getCanonicalFile(); } catch (IOException ioe) {}
    String ipath = ipf.toString();
    m.put("INSTALL", ipath);

    m.put("HOME", System.getProperty("user.home"));
    m.put("CWD", System.getProperty("user.dir"));

    File csf = new File(ipath, "configs");
    try { csf = csf.getCanonicalFile(); } catch (IOException ioe) {}
    String cspath = csf.toString();
    m.put("CONFIGS", cspath);

    String cs = System.getProperty("org.cougaar.config", "common");
    if (cs != null)
      m.put("CONFIG", cs);

    defaultSocietyFinder = 
      new SocietyFinder(System.getProperty("org.cougaar.config.path"), 
                        defaultProperties);
  }
 
  /**
   * Use SocietyFinder.getInstance instead of constructor.
   * @param s configuration path
   * @param p properties
   */
  public SocietyFinder(String s, Map p) {
    properties = p;
    if (s == null) {
      s = defaultConfigPath;
    } else {
      s = s.replace('\\', '/'); // Make sure its a URL and not a file path
    }

    // append the default if we end with a ';'
    if (s.endsWith(";")) s += defaultConfigPath;

    Vector v = StringUtility.parseCSV(s, ';');
    int l = v.size();
    for (int i = 0; i < l; i++) {
      appendPathElement((String) v.elementAt(i));
    }
  }

  /**
   * Return an instance of <code>SocietyFinder</code>.
   */
  public static SocietyFinder getInstance() {
    return defaultSocietyFinder;
  }

  private void appendPathElement(String el) {
    String s = el;
    try {
      s = substituteProperties(el);
      s = s.replace('\\', '/').replace('\\', '/'); // These should be URL-like
      try {
        if (!s.endsWith("/")) s += "/";
        appendPathElement(new URL(s));
      }
      catch (MalformedURLException mue) {
        File f = new File(s);
        if (f.isDirectory()) {
          appendPathElement(new File(s).getCanonicalFile().toURL());
        } // else skip it.
      }
    } 
    catch (Exception e) {
      System.err.println("Failed to interpret " + el + " as url: " + e);
    }
  }

  private void appendPathElement(URL url) {
    configPath.add(url);
  }

  private int indexOfNonAlpha(String s, int i) {
    int l = s.length();
    for (int j = i; j<l; j++) {
      char c = s.charAt(j);
      if (!Character.isLetterOrDigit(c)) return j;
    }
    return -1;
  }

  private String substituteProperties(String s) {
    int i = s.indexOf('$');
    if (i >= 0) {
      int j = indexOfNonAlpha(s,i+1);
      String s0 = s.substring(0,i);
      String s2 = (j<0)?"":s.substring(j);
      String k = s.substring(i+1,(j<0)?s.length():j);
      Object o = properties.get(k);
      if (o == null) {
        throw new IllegalArgumentException("No such path property \""+k+"\"");
      }
      return substituteProperties(s0+o.toString()+s2);
    }
    return s;
  }

  /**
   * Get the first path that ConfigFinder will search.
   * @return the path
   */
  public String getPath() {
    for (int i = 0; i < configPath.size(); i++) {
      URL url = (URL) configPath.get(i);
      if (url.getProtocol().equals("file"))
        return url.getFile();
    }
    return ".";
  }


  /**
   * Return names of all agent files in the specified directory.
   * Currently searches all files whose names end with ".ini" for
   * the pattern: [ Cluster ]
   * @param directory to search
   * @return string array of agent filenames
   */
  public String[] getAgentFilenames(File directory) {
    File[] files = directory.listFiles();
    ArrayList agentFilenames = new ArrayList(files.length);
    for (int i = 0; i < files.length; i++) {
      String path = files[i].getPath(); // FileParseUtil needs complete path
      if (!path.endsWith(".ini"))
        continue;
      if (FileParseUtil.containsPattern(path, "\\[ Cluster \\]"))
        agentFilenames.add(path);
    }
    return (String[])agentFilenames.toArray(new String[agentFilenames.size()]);
  }


  /**
   * Locate an actual file in the config path. This will skip over
   * elements of org.cougaar.config.path that are not file: urls.
   **/
  public File locateFile(String aFilename) {
    if (aFilename == null)
      return null;
    File result = new File(aFilename);
    if (result.exists())
      return result;
    for (int i = 0 ; i < configPath.size() ; i++) {
      URL url = (URL) configPath.get(i);
      if (url.getProtocol().equals("file")) {
        try {
          URL fileURL = new URL(url, aFilename);
          result = new File(fileURL.getFile());
          //if (verbose) { System.err.print("Looking for "+result+": "); }
          if (result.exists()) {
            //if (verbose) { System.err.println("Found it. File " + aFilename + 
            //                   " is " + fileURL); }
            return result;
          } else {
            //if (verbose) { System.err.println(); }
          }
        }
        catch (MalformedURLException mue) {
          continue;
        }
      }
    }
    return null;
  }

  /**
   * Opens an InputStream to access the named file. The file is sought
   * in all the places specified in configPath.
   * @throws IOException if the resource cannot be found.
   **/
  public InputStream open(String aURL) throws IOException {
    // First, see if the file can be opened as is
    try {
      File url = new File(aURL);
      //System.out.println("Trying "+url+": ");
      InputStream is = url.toURL().openStream();
      //System.err.println("Found it. File " + aURL + " is " + url);
      if (is != null)
	return is;
    }
//     catch (MalformedURLException mue) {
//       System.out.println("Got exception" + mue);
//     }
    catch (IOException ioe) {
      //System.out.println("Got exception" + ioe);
    }

    // Then try all the things on the ConfigPath
    for (int i = 0 ; i < configPath.size() ; i++) {
      URL base = (URL) configPath.get(i);
      try {
        URL url = new URL(base, aURL);
        //System.out.println("Trying "+url+": ");
        InputStream is = url.openStream();
        if (is == null) continue; // Don't return null
        //System.out.println("Found it. File " + aURL + " is " + url);
        return is;
      }
      catch (MalformedURLException mue) {
        //if (verbose) { System.err.println(); }
	//System.out.println("Got exception" + mue);
        continue;
      }
      catch (IOException ioe) {
        //if (verbose) { System.err.println(); }
	//System.out.println("Got exception" + ioe);
        continue;
      }
    }
    throw new FileNotFoundException("Exhausted options for: " + aURL);
  }

  public static void main(String[] args) {
    JFileChooser chooser =
      new JFileChooser(SocietyFinder.getInstance().getPath());
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    File file = null;
    while (file == null) {
      int result = chooser.showDialog(null, "OK");
      if (result != JFileChooser.APPROVE_OPTION)
        return;
      file = chooser.getSelectedFile();
    }
    String name = "";
    name = file.getName();
    if (name.endsWith(".ini"))
      name = name.substring(0, name.length()-4);
    if (file.isDirectory()) {
      String[] filenames =
        SocietyFinder.getInstance().getAgentFilenames(file);
      if (filenames == null || filenames.length == 0) {
	// Found no Agents
        System.out.println("Found no agent in dir " + file.getPath());
	return;
      }
      for (int i = 0; i < filenames.length; i++)
        System.out.println(filenames[i]);
    }
  }
}
