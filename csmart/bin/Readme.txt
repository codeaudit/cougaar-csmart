csmart/bin/Readme.txt

See "csmart/doc/CSMART-README.txt" for an overview of CSMART
and instructions on how to install and configure CSMART.

In particular, see "csmart/doc/InstallAndTest.html" for 
step-by-step instructions.


This directory contains scripts for building and running CSMART.

All scripts in this directory assume that the environment variable
COUGAAR_INSTALL_PATH has been set appropriately, as in:
     COUGAAR_INSTALL_PATH=/opt/alp
     (where this directory is /opt/alp/csmart/bin)

A platform-specific version of "dot.exe" from AT&T is required for
graph layout.  The current Cougaar release comes with both the Win and
Linux/Solaris versions of this file.

Also note: To get access to the CPU related metrics, you will need to
install the Mylib.dll/.so in your path: $COUGAAR_INSTALL_PATH/csmart/bin 
or JDK_HOME/bin work well. This library can be installed from the 
scalability module per its installation instructions 
(scalability/bin/Install.txt) but the society runs fine without it.


Some key parameters that you will want to locally edit:
ABC PlugIns use a custom logger, whose verbosity can be edited (see
setarguments.bat)
Excel location is specified in CMSART / CSMART.bat / CSMART.sh


Contents:

CSMART
CSMART.sh
CSMART.bat - The main script for the CSMART UI. Relies on the
        setarguments from above.
	NOTE: CSMART.bat specifies the location of MS Excel on your
	local machine, and should be editted to reflect your local
	installation.

Monitor
Monitor.bat - Scripts for running the CSMART Society Monitor tool
        standalone, to view the conents of an arbitrary Cougaar
        blackboard. Again, these rely upon having "$COUGAAR_INSTALL_PATH"
        set.

dot.exe
dot-l386
        For the AT&T GraphViz package, for doing graph layout.  "dot.exe" 
        is for DOS/WIN machines, "dot-l386" is for Linux (also works on 
        Solaris).  Used by the CSMART "Society Monitor" UIs. These come 
        with a standard Cougaar install (from the "cougaar.zip"), but are 
        also available from the "csmart-3rdparty.zip" file. The
	usual location is $COUGAAR_INSTALL_PATH/csmart/bin, but alternatives 
        such as $COUGAAR_INSTALL_PATY/sys and $COUGAAR_INSTALL_PATH/bin 
        also work. You can also get these files from the ABC website
        (http://ABCTestBed.bbn.com) or the AT&T GraphViz site.
	
CSMARTNode
CSMARTsetarguments.bat
CSMARTsetlibpath.bat
        Old startup-scripts for running "ABC Societies" from ".INI" files.
        Most users should not run these files.
	
build-all.bat
        Simple script for compiling CSMART sources on a Windows machine.

build.xml
	ANT1.3 build script. ANT is available at http://jakarta.apache.org/ant

[The sample scripts for running the AppServer have been moved to the server 
 module.]
