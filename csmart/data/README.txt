csmart/data                           

See "csmart/doc/CSMART-README.txt" for an overview of CSMART
and instructions on how to install and configure CSMART.

The most important directory for CSMART users in this directory,
is the database/ directory, which contains data for filling
in the CSMART configuration database. 
See the readme in that directory.
See csmart/doc/InstallandTest.html or the 
User's Guide for usage.

This directory also contains several configuration sub-directories:

  common/:
	LDMDomains and servlets.txt for use with CSMART.  Also
	a logging properties file for use with the CSMART UI.  Finally,
	copies of various config files necessary to run Cougaar nodes.
	
	Note that CSMART loads its own domain plus GLM. Most users should copy the version here into CIP/configs/common.

 debug.properties - contains information for customizing
    the use of the Core logging service, via log4j statements

  packages.txt - A simple description of the different Java packages
        which make up the CSMART code.

  rwe-scripts/
		This includes several XML files specifying RealWorldEvents for
		use with the ABC Impacts. Edit the included files to
		specify actual Agent names to have these
		impacts included in your CSMART experiment.
  cmt/
	Data files and scripts for loading them for the CMT society
	configuration database. These are necessarily only when building
	a _new_ database for running societies from.

  ?/:
    optional society configuration files 

	In general, to run a particular configuration you should run "Node" from that
	directory. However, if you add a full URL to your config path, as decribed in
	bin/setarguments,  then you need not be anywhere in particular.

    For example:

    ul-test/
	This is a small ABC society for testing the external event injection agents.
        When generating your own societies with external impacts, use all of the
	Experiment-* files from here. Also edit the Node
	files to include the two Experiment agents.
    singleCluster/
	A simple ABC Society with only one Agent, but which uses the Metrics collection Plugins
	Note that these MetricsCollection Plugins can be used with any society, not just a CSMART society.
        See MyCluster.ini for comments on using these plugins.

    twoCluster/
	A simple 2 Agent ABC Society.

    threeCluster/
	A slightly richer 3 Agent society, with two different task types, illustrating some of the 
	capabilities of the ABC Plugins
	
