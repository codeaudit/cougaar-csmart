csmart/data                           

This directory contains several configuration sub-directories:

  common/:
	LDMDomains and default.psps.xml for use with CSMART.  Also
	a properties file for use with the CSMART UI.  Finally,
	copies of various config files necessary to run Cougaar nodes.
	
	Note that CSMART loads its own domain plus GLM.
	Also, default.psps.xml contains the PSPs to copy for using the
	CSMART Society Monitor standalone

 debug.properties - contains one line per package, where "true"
    specifies that debug statements from this package will be logged.

 unix-server-sample.props, win-server-sample.props - sample NodeServer
     properties files, including CSMART specific settings. Use these as
     examples in setting up your own machine to run the server.
 
  rwe-scripts/
		This includes several XML files specifying RealWorldEvents for
		use with the ABC Impacts. Edit the included files to
		specify actual Agent names to have these
		impacts included in your CSMART experiment.

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
	A simple ABC Society with only one Agent, but which uses the Metrics collection PlugIns
	Note that these MetricsCollection PlugIns can be used with any society, not just a CSMART society.
        See MyCluster.ini for comments on using these plugins.

    twoCluster/
	A simple 2 Agent ABC Society.

    threeCluster/
	A slightly richer 3 Agent society, with two different task types, illustrating some of the 
	capabilities of the ABC PlugIns
	