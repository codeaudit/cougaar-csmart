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

	
