csmart/data                           

See "csmart/doc/CSMART-README.txt" for an overview of CSMART
and instructions on how to install and configure CSMART.

The most important directory for CSMART users in this directory,
is the database/ directory, which contains data for filling
in the CSMART configuration database. 
See the readme in that directory.
See csmart/doc/InstallandTest.html or the 
User's Guide for usage.

This directory also contains several configuration directories and files:

  common/:
	CSMART.q: Main file of queries used by CSMART
	CMT.q: Queries used by CSMART to generate 1AD societies
	PopulateDb.q: Queries used by CSMART to save experiments
	(optional: recipeQueries.q: User created file of custom recipe queries.)
	servlets.txt for use with CSMART.  Also
	a logging properties file for use with the CSMART UI. 

        debug.properties - contains information for customizing
             the use of the Core logging service, via log4j statements
	
