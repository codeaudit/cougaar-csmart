csmart/bin/Readme.txt

See "csmart/doc/CSMART-README.txt" for an overview of CSMART
and instructions on how to install and configure CSMART.

In particular, see "csmart/doc/InstallAndTest.html" for 
step-by-step instructions.

Be sure to us the v4_v6_conversion script if necessary.

This directory contains scripts for running CSMART.

All scripts in this directory assume that the environment variable
COUGAAR_INSTALL_PATH has been set appropriately, as in:
     COUGAAR_INSTALL_PATH=/opt/cougaar
     (where this directory is /opt/cougaar/csmart/bin)

A platform-specific version of "dot.exe" from AT&T is required for
graph layout.  The current Cougaar release comes with both the Win and
Linux/Solaris versions of this file.

Also note: To get access to the CPU related metrics, you will need to
install the Mylib.dll/.so in your path: $COUGAAR_INSTALL_PATH/csmart/bin 
or JDK_HOME/bin work well. This library can be installed from the 
scalability module per its installation instructions 
(scalability/bin/Install.txt) but the society runs fine without it.


Some key parameters that you will want to locally edit:
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

CONSOLE
CONSOLE.sh
CONSOLE.bat - This script runs the CSMART Experiment Controller (aka Console)
        standalone. This is particularly useful for running an experiment
        defined in an XML file.

dot.exe
dot-l386
        For the AT&T GraphViz package, for doing graph layout.  "dot.exe" 
        is for DOS/WIN machines, "dot-l386" is for Linux (also works on 
        Solaris).  Used by the CSMART "Society Monitor" UIs. These come 
        with a standard Cougaar install (from the "cougaar.zip")
        The
	usual location is $COUGAAR_INSTALL_PATH/csmart/bin, but alternatives 
        such as $COUGAAR_INSTALL_PATY/sys and $COUGAAR_INSTALL_PATH/bin 
        also work. You can also get these files from the  GraphViz site.
	
copy-experiment.sh
[Helper file: copyASB.sql]
	Script to copy a complete CSMART database experiment, under a
	new name. Requires Unix like shell (ie Cygwin
	on Windows). Use this as precursor to modifying that
	experiment in place (substituting a Plugin class, for
	example), or exporting the experiment for sharing with others.
	Be sure to double-quote the experiment name or suffix if they
	contain spaces.

	Usage: copy-experiment.sh [Old Experiment Name] [Suffix for
	new name] [DB User Name] [DB Password] [Database name]
	[Optional: remote host of MySQL DB]

delete-temp-db.[sh/bat]
	Script to drop the temporary database created above. Use
	this after copying an experiment using the above script, and
	exporting it if desired.

	Usage: delete-temp-db.sh [MySQL User] [Password] [DB Name]
	[Optional: Remote MySQL DB host name]

switchPlugin-class.sh
(Helper files switchPlugin-class.sql, get-assembly.sql]
	Script to replace one plugin with another in all uses in a
	named experiment. Use this after doing a deep copy of an
	experiment, using the above script. DO NOT use this on one of
	the base experiments. Be sure to double-quote the experiment
	name if it contains spaces. If you later alter the thread
	selection in this experiment, the substitution will be lost.

	Usage: switchPlugin-class.sh [Old fully qual'ed plugin class]
	[New plugin class] [Experiment name to update] [Config DB
	Username] [Password] [MySQL Config DB database name]
	[Optional: MySQL DB Host]

	For example: To run a 1AD society, class 3, but use the SRA
	Inventory Plugin instead of the ANTS equivalent.
	1) Use CSMART to load a 1AD society from the database, and
	select the class 3 thread.
	2) Save the new experiment to the database.
	3) Run copy-experiment.sh, giving it the experiment name from
	#2 and a suffix, say, "with-SRA-plugin"
	4) Run switchPlugin-class.sh giving it the new experiment name
	(old-suffix)
	5) (Either export the new experiment for use elsewhere, using
	the exportExperiment script, or delete the temporary database,
	using the delete-temp-db script.)
	6) Load the new experiment from the database, and DO NOT
	change the thread selection. Proceed to otherwise configure
	and run this experiment as normal.
	

exportExperiment.sh
[Helper: copyForExport.sql]
	 Export the named experiment, for use on another database. 
	 Uses the above copy-experiment. Note that it relies on a
	 complete Cougaar install, including cougaar.rc, CSMART.q,
	 csmart.jar, bootstrap.jar, and util.jar
	 This script will copy all
	 included recipes as well, but you must be sure that the
	 needed recipe Queries are moved over as well. Also, in
	 some circumstances, if the other database has an imported
	 experiment with the same name for example, you will
	 get errors. The temporary
	 database created by the above copy script, and used here, is
	 deleted when this script completes. Be sure to quote the
	 experiment name if it contains spaces. 

	 Usage: exportExperiment.sh [Experiment to export] [DB
	 Username] [DB Password] [DB name] [Optional: host name]

export-recipe.[sh/bat]
[Helper: copyRecipeForExport.sql]
	 Export the named recipe for use on another database. If a
	 recipe with the same name exists in the other database, you
	 _will_ get errors. Quote the recipe name if it contains
	 spaces. Note that you should have run the delete temp db
	 script above if you previously copied an experiment, or you
	 will get errors. Note that you will have to copy over the
	 needed recipe queries separately.

	 Usage: export-recipe.sh [Recipe Name] [Config DB Username]
	 [Password] [MySQL Config DB database name] [Optional: MySQL
	 DB host name]

v4_v6_conversion.[sh/bat]
        Removes all occurences of table names in the named file that are 
        prefixed with v4 or v6 and replaces them with non prefixed table 
        names. Used to convert a recipe or experiment export from a 
	pre-9.4.1 database format to the current format.

        Usage: v4_v6_conversion.sh [Filename To Convert]

export-all-recipes.sh
	Create script for exporting all recipes from the given
	database. Result of running this script is another script
	to run (without arguments). Note that recipe names with spaces
	have problems. Therefore you will have to edit the generated
	script if any of your recipe names contain spaces.

Viewer[.bat/.sh]
	Run a simple Society Monitor-like viewer of a military
	organization structure defined in particular database tables.

------------
This directory previously contained an Ant build.xml script. This 
script is now generated by the build module process. It is available 
from CVS or from the csmart-dev.zip file, in the csmart/ directory 
(one up). Targets have not changed. However, class files will now be 
located in csmart/tmp/classes, instead of csmart/tmpdir. Change your 
COUGAAR_DEV_PATH to that new value, as necessary. Additionally,
if you used a ~/.ant.properties file, consider using instead
a COUGAAR_INSTALL_PATH/global.properties file, or a
COUGAAR_INSTALL_PATH/csmart/module.properties for csmart specific
properties. See the build.xml file itself for further details.

