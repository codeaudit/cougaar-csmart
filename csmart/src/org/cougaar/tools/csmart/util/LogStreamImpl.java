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

package org.cougaar.tools.csmart.util;

import org.cougaar.util.*;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.Field;
import java.text.*;

/**
 * A full implementation of LogStream.  
 * <br><br>
 * <i>Location</i> and <i>severity</i> level of the log can be changed at 
 * runtime using the following command line options.<br>
 * &nbsp;&nbsp;<code>-Dcsmart.log.severity=<i>[Desired Severity Level]</i></code><br>
 * &nbsp;&nbsp;<code>-Dcsmart.log.location=<i>[Desired Log Location]</i></code><br> 
 * <br>
 * Each option has a default:<br>
 * The default severity level is set at: <code>PROBLEM</code><br>
 * The default log location is:
 * <code><i>[COUGAAR_INSTALL_PATH]</i>/[<i>YearMonthDate</i>].log</code><br>
 * <br>
 * Example Usage: <br>
 * &nbsp;<code> % java -Dcsmart.log.severity=DEBUG ... </code>
 * <br>
 * &nbsp;<code> % java -Dcsmart.log.location=/var/adm/log/myapp.log ... </code>
 * <br>
 * &nbsp;<code> % java -Dcsmart.log.severity=VERBOSE -Dcsmart.log.location=/home/mydir/log.txt ... </code>
 * <br><br>
 * If the Log Level is set to VERY_VERBOSE on the commandline, output is sent
 * to both the file and to STDOUT.<br><br>
 * If any errors are encountered with the command line specified values, the 
 * default values are used.<br>
 * If <code>log.location</code> consists of just a path, a default file name of the format 
 * <code><i>YearMonthDate</i>.log</code> is used.<br><br>
 * The log consists of '|' delimited text.  The format of the logfile is:<br>
 * &nbsp;&nbsp;<i>Timestamp | Severity | Message</i><br><br>
 * <i><b> Note: The Timestamp is in Milliseconds. </b></i> 
 * <br>
 * <br>
 * Each log entry starts with a header containing:<br>
 * <ul>
 * <li>TimeStamp of Run Start</li>
 * <li>Log Level</li>
 * <li>Alp Version and build time</li>
 * <li>VM Version</li>
 * <li>OS Version</li>
 * </ul>
 */
public class LogStreamImpl 
     extends GenericStateModelAdapter
     implements LogStream {


  private int severityLevel;
  private String log;
  private PrintWriter pw;
  private boolean started = false;
  private String defaultLogFile;
  private Properties debugProps = new Properties();
  
  
  private static HashMap dbgTable = new HashMap();
  private static String debugAllToken = "all";
   
  private static boolean debugAll = false;  
    
  /** default constructor **/    
  public LogStreamImpl()  {
   
     // Set the default log file.
     // Format: 'YearMonthDate.log'
     Calendar c = Calendar.getInstance();
     SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        
     defaultLogFile = sdf.format(c.getTime()) + ".log";


     // Get the default Severity level.  First check the commandline,
     // if nothing is specified set a default.
   
     String tmpSeverity = System.getProperty("csmart.log.severity", "PROBLEM");

     if(tmpSeverity.equalsIgnoreCase("SEVERE")) {
        severityLevel = SEVERE;
     } else if (tmpSeverity.equalsIgnoreCase("PROBLEM")) {
        severityLevel = PROBLEM;
     } else if (tmpSeverity.equalsIgnoreCase("DEBUG")) {
        severityLevel = DEBUG; 
     } else if (tmpSeverity.equalsIgnoreCase("VERBOSE")) {
        severityLevel = VERBOSE;
     } else if (tmpSeverity .equalsIgnoreCase("VERY_VERBOSE")) {
        severityLevel = VERY_VERBOSE;
     } else {
        severityLevel = PROBLEM;
     }   
      
     // Now create the log file and path.
     // If a problem occurs, use the default.
     createLogFile();
     
     // Load the Properties file.  If none, debug all.
     loadProperties();
  }

  /**
   * Creates the logfile if it doesn't already exist.
   * It first trys to create the logfile specified on the
   * commandline.  If that fails, it uses the default, 
   * which should never fail.
   */
  private void createLogFile() {  
     String installPath = System.getProperty("org.cougaar.install.path");
      
     String ld = System.getProperty("csmart.log.location");

     File logFile;

     if( ld == null ) {
        logFile = new File(installPath + File.separatorChar + 
        defaultLogFile.toString());
     } else {
        logFile = new File(ld);
     }   
  
     // First check that it is not just a directory
     if(logFile.isDirectory()) {
        // It is just a directory, just add 'defaultLogFile' to the given path.
        logFile = new File(logFile, defaultLogFile);
     }
   
     // Ensure that the file can be written to.   
     if( logFile.exists() ) {
        if( !logFile.canWrite() ) {
        // Try to create it, maybe it just isn't there.
        try {
           logFile.createNewFile();
        } catch( IOException e ) {
        // That failed, use the default.
        System.err.println("Error, cannot write to logfile: " + 
           logFile.toString() +
           " Using default.");

         logFile = new File(installPath + File.separatorChar + 
           defaultLogFile);
        }         
     }
     } else {
        // Try to create the file.
        try {
           logFile.createNewFile();
        } catch( IOException e ) {
           // That failed, use the default.
           System.err.println("Error, cannot write to logfile: " + 
              logFile.toString() +
              " Using default.");

           logFile = new File(installPath + File.separatorChar + 
              defaultLogFile);     
        }
     }
   
     log = logFile.toString();  
  }
  
  // Loads the properties file containing all the debug properties.
  
  private void loadProperties() {
  
     boolean loadSuccess = false;
     String installPath = System.getProperty("org.cougaar.install.path");     
     
     //     File pFile = new File(installPath + File.separatorChar + "abc-sim" + 
     // File.separatorChar + "configs" + File.separatorChar + "debug.properties");
     // FIXME!!!! This only works in a development environment!!!! FIXME
     File pFile = new File(installPath + File.separatorChar + "csmart" + 
        File.separatorChar + "data" + File.separatorChar + "debug.properties");     


    try {
        debugProps.load(new FileInputStream(pFile));
        loadSuccess = true;
     } catch( IOException e ) {
        loadSuccess = false;
     }
     
     if(loadSuccess) {
        Enumeration e = debugProps.propertyNames();
        
        // Fill Hashtable with all values from the properties file. 
        while(e.hasMoreElements()) {
           String name = (String) e.nextElement();
           
           String key = debugProps.getProperty(name);
           
           if( key.equalsIgnoreCase("true") ) {
              dbgTable.put(name, Boolean.TRUE);              
           }
           
        }    
     } else {
        // For some reason, there is no properties file,
        // turn on debugging for all.
        dbgTable.put(debugAllToken, Boolean.TRUE);
        debugAll = dbgTable.containsKey(debugAllToken);
     }
  }
  
  /** 
   *
   * Checks to see if debugging is turned on for the
   * specified Token.
   *
   * @param token the string token to check.
   * @return true if debugging is on, false otherwise.
   */
  private static boolean isDebugging(String token) {
     return debugAll || dbgTable.containsKey(token);
  }
   
  /**
   * 
   * @see LogStream#isApplicable
   *
   */
  public boolean isApplicable(int severity)  {
     if( severity <= severityLevel )  {
        return true;
     }
     return false;
  }

  /*
   *
   * @see LogStream#log   
   *
   */ 
  public synchronized void log(int severity, String message) {
    
     logText(severity, message);  
     
     // If the log level is set to VERY_VERBOSE, log to STDOUT.
     if(severityLevel == VERY_VERBOSE) {
        logToSTDOUT(severity, message);     
     }
     
     // If the log severity is SEVERE, dump to the console as well.
     if( severity == SEVERE ) {
        System.err.println(message); 
     }          
  }

  /*
   *
   * @see LogStream#log(String, int, String)
   *
   */ 
  public synchronized void log(String className, int severity, String message) {
     
     String pkg = className.substring(0, className.lastIndexOf("."));
     
     if( isDebugging(pkg) || isDebugging(className) ){         
       logText(severity, message);  
     }
     
     // If the log level is set to VERY_VERBOSE, log to STDOUT.
     // regardless of any filtering.
     
     if(severityLevel == VERY_VERBOSE) {
        logToSTDOUT(severity, message);     
     }     
          
     // If the log severity is SEVERE, dump to the console as well.
     if( severity == SEVERE ) {
        System.err.println(message); 
     }     
  }

  /*
   *
   * @see LogStream#log(Object, int, String)
   *
   */ 
  public synchronized void log(Object objRef, int severity, String message) {

     String className = objRef.getClass().getName();
          
     String pkg = className.substring(0, className.lastIndexOf("."));
     
     if( isDebugging(pkg) || isDebugging(className) ){         
       logText(severity, message);  
     }
     
     // If the log level is set to VERY_VERBOSE, log to STDOUT.
     // regardless of any filtering.
     
     if(severityLevel == VERY_VERBOSE) {
        logToSTDOUT(severity, message);     
     }     
          
     // If the log severity is SEVERE, dump to the console as well.
     if( severity == SEVERE ) {
        System.err.println(message); 
     }     
  }



  /**
   *
   * Inserted a Log header containing run information, etc.
   * The header is not displayed to STDOUT.
   */
  private void insertHeader() {

     Calendar c = Calendar.getInstance();

     SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
     String time = sdf.format(c.getTime());        

     pw.println();
     pw.println();     
     pw.println("New Run Started at: " + time +
        " with a log level: " + getSeverityString(severityLevel));
     

     String version = null;
     long buildtime = 0;
     try {
        Class vc = Class.forName("org.cougaar.Version");
        Field vf = vc.getField("version");
        Field bf = vc.getField("buildTime");
        version = (String) vf.get(null);
        buildtime = bf.getLong(null);
     } catch (Exception e) {}
     synchronized (System.err) {
        pw.print("Cougaar ");
        if (version == null) {
           pw.println("(unknown version)");
        } else {
           pw.println(version+" built on "+(new Date(buildtime)));
        }
      
        pw.println("VM: JDK "+ System.getProperty("java.vm.version") +
           " ("+ System.getProperty("java.vm.info") +")");
        pw.println("OS: "+ System.getProperty("os.name") +" ("+ 
           System.getProperty("os.version") +")");
     }
    
     pw.println();
     pw.println();     
     
  }
  
  /**
   * Standard delimited-text log.
   *
   */
  private void logText(int severity, String message)  {

     if( started ) {
        if( isApplicable(severity) ) {
            
           pw.print(System.currentTimeMillis());
           pw.print(" | ");
           pw.print(getSeverityString(severity));         
           pw.print(" | ");
           pw.println(message);
        }
     }   
  }

  /**
   *
   * Returns a string representation of the given severity level.
   * @param severity level convert to string.
   */
  private String getSeverityString(int severity) {

     String sev = null;
     
     switch(severity) {
        case SEVERE:
           sev = "SEVERE";
           break;
        case PROBLEM:
           sev = "PROBLEM";
           break;
        case DEBUG:
           sev = "DEBUG";
           break;
        case VERBOSE:
           sev = "VERBOSE";
           break;
        case VERY_VERBOSE:
           sev = "VERY_VERBOSE";
           break;
        default:
           sev.valueOf(severity);
     }           
     
     return sev;
  }
  
  /**
   * Directs all log output to STDOUT.
   */   
  private void logToSTDOUT(int severity, String message) {
  
     if( started ) {      
        System.out.println( getSeverityString(severity) + " | " + message );
     }        
  }
  
  /** 
   *
   * The current severity level that is the greatest level being logged.
   *
   * @return Greatest severity level being logged
   *
   */
   
  public int getSeverityLevel()  {
     return this.severityLevel;
  }

  /**
   * Initializes the state adapter and creates the stream for logging.
   * <br><b><i>Start must be called before any logging will happen.</i></b>
   *
   */
  public void start() {

     if( !started ) {
        try  {
           super.initialize();
        } catch(StateModelException e) {
           System.err.println("StateModel Exception: " + e);
           e.printStackTrace();
        }

        try {
           super.load();
        } catch(StateModelException e) {
           System.err.println("StateModel Exception: " + e);
           e.printStackTrace();
        }

        try {
           super.start();
        } catch(StateModelException e) {
           System.err.println("StateModel Exception: " + e);
           e.printStackTrace();
        }

        // Open BufferedWriter.
        try  {
           pw = new PrintWriter(new BufferedWriter(new FileWriter(log, true)));
        } catch(FileNotFoundException e) {
           System.err.println("FileNotFound Exception: " + e);
           e.printStackTrace();   
        }
        catch(IOException e) {
           System.err.println("IOException: " + e);
           e.printStackTrace();   
        }
        
        started = true;
        
        // Insert Run Start and basic info header.
        insertHeader();
     }
  }

  /**
   * flushes the LogStream and closes the file.
   *
   */
  public void stop() {
 
     if( started ) {
        try {
           super.suspend();
        } catch(StateModelException e) {
           System.err.println("StateModel Exception: " + e);
           e.printStackTrace();
        }

        pw.close();

        try {
           super.stop();
        } catch(StateModelException e) {
           System.err.println("StateModel Exception: " + e);
           e.printStackTrace();
        }

        try {
           super.unload();
        } catch(StateModelException e) {
           System.err.println("StateModel Exception: " + e);
           e.printStackTrace();
        }

        started = false;
     
     }
  }

  /**
   *
   * @see LogStream#flush
   *
   */
  public void flush() {
 
     if( started) {
        pw.flush();
     }
  }

}
