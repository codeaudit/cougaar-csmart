csmart/data/database/README

This file describes the directory structure containing scripts and data
files necessary for creating a MySQL configuration database used by CSMART.
See csmart/doc/InstallandTest.html or the User's Guide for more information.

Summary:
After creating a database per the instructions referenced above, you load
it with data from files contained here.
Most users will simply load all required data into a single MySQL
database. To do so, run ./scripts/mysql/load_xml_and_csmart.[sh/bat]

If you will only be running from XML files (no CSMART), you may
instead choose to use the load_xml_db.[sh/bat] script

If you will only be running from CSMART (not from XML files), you may
instead choose to use the load_csmart_db.[sh/bat] script.

Be sure to create / update your cougaar.rc file to match the database
name used in loading the above databases (see
doc/ReleaseNotes/10.4/DataAccess.html).

As a guide, many Cougaar users will require the following cougaar.rc
database entries (with guide indicating which scripts will load that
data):
org.cougaar.database 
blackjack.database
icis.database
--- Domain Plugin data. Filled by data in dbms directory.
--- Loaded by scripts load_csmart_db, load_xml_db, and
load_xml_and_csmart

org.cougaar.oplan.database
--- This entry was added for 10.4.1!
--- Data for reading Oplan from a database. Tables defined in this
directory.
--- Loaded by scripts load_csmart_db, load_xml_db, and
load_xml_and_csmart, all by calling the script
load_oplan_data.[sh/bat]

org.cougaar.configuration.database
--- Data for running CSMART
--- Loaded by scripts load_csmart_db and load_xml_and_csmart -- NOT
by load_xml_db

org.cougaar.refconfig.database
--- Data for running societies from XML files. See the ref-csf
directory.
--- Loaded by load_xml_db and load_xml_and_csmart. NOT by load_csmart_db

To edit the contents of these tables directly (not recommended), edit
the csv files in ./csv

csmart/data/database directory description:

Sub-directories:

   ref-csv/:
	Contains source files to define
	the "refconfig" database Organizations tables.
	Users of CSMART need not edit or use these files.
        These table definitions are used only when running from XML
        files. You must fill in content appropriate to your application.
	See the load_ref_data script for loading.

   csv/:
        Contains a '.csv' file for each table to be created in the 
        configuration database.  These files can be loaded into a
        MySQL database 'as is' or may be modified (eg, in excel) as necessary
        before loading. (Note the trailing comma on every row which
        helps avoid load errors.)

   db_src/:
        Contains the database schema source files that are produced from ERStudio.
        Perl scripts can be run against the csmart-db.sql to generate either 
        oracle or mysql specific versions of sql for database loading.

   doc/:
        Contains jpg representations of the entity-relationship diagrams produced
        in ERStudio.  Pictoral representations are provided for the 'cfw' as well
        as 'assembly' tables in the database.

   perl/:
        Contains the perl scripts which are run against the single sql
        file produced from ERStudio to generate either oracle or mysql
        specific sql files as well as to split the original file into components
        for dropping and creating indexes, foreign keys (Oracle) and tables.

   scripts/:
        Contains Dos-version GNU sed v3.02

        Contains the subdirectory - 
           mysql/:

            Contains the subdirectory - 
               sql/:
		  cluster-id-removal.sql - SQL script intented
		     to be run by hand by users, to replace
		     ClusterIdentifier (obsolete as of 10.0) with
		     MessageAddress. Note that CSMART will replace these
		     on a case-by-case basis if you load your experiment
		     and re-save it.
		   
                     Note: These scripts are called by the shell/bat scripts
                       and should not be invoked directly.  
                       See csmart/data/database/doc/db-scripts-guide.html 
                       for further information.
              
                   csmart-db.load-mysql-tables.sql -Loads the configuration database
                        from '.csv' files in the
                        csmart/data/database/csv directory.
                        It should not be called directly by 
                        the user.

                   csmart-db.drop.mysql-indexes.sql - Drops indexes.

                   csmart-db.drop-mysql-tables.sql - Drops all tables.

                   csmart-db.create-mysql-indexes.sql - Creates indexes.

                   csmart-db.create-mysql-tables.sql - Creates all tables.

                   load_oplan_data.sql - Loads oplan tables.

                   load_ref_data.sql - Loads the reference data required to run from xml

                   load_comm.sql - SQL script called by
                          load_communities.sh to load data from csv files. 
                          Should not be called directly by the user.

                   update_comm.sql - SQL script called by
		          load_communities.sh to delete existing rows
		          under the provided ASSEMBLY_ID and associate
		          the provided ASSEMBLY_ID with the data
		          loaded from the csv file.  Should not be
		          called directly by the user. 
                        
           Mysql dir also contains the scripts - 

		     [Note that .bat versions require working JDK
		     distribution.]

                load_xml_and_csmart.sh[bat] - This script loads any required domain and oplan data,
                   the CSMART configuration data and the xml-refconfig data into the database.
		   It uses the helper scripts load_domain_data.sh[bat] (if necessary),
                   load_oplan_data.sh[bat], load_csmart_data.sh[bat]and load_ref_data.sh[bat]. 

                load_csmart_db.sh[bat] - This script loads any required domain and oplan data,
		   and the CSMART configuration data files into the database.
		   It uses the helper scripts load_domain_data.sh[bat] (if necessary),
                   load_oplan_data.sh[bat], and load_csmart_data.sh[bat].

                load_csmart_data.sh[bat] - This script loads the contents of the
		   CSMART configuration data files into the database.
		   It uses the helper SQL files csmart-db.*.sql

                load_xml_db.sh[bat] - This script loads any required
		   domain and oplan data and the ref_csv data files into the database.
		   It uses the helper SQL files load_domain_data.sh[bat] (if necessary),
                   load_oplan_data.sh[bat], and load_ref_data.sh[bat].

                load_ref_data.sh[bat] - This script loads the contents of the
		   ref_csv data files into the database.
		   It uses the helper SQL file load_ref_data.sql

                load_oplan_data.sh[bat] - This script loads the contents of the
		   oplan data files into the oplan database.
		   It uses the helper SQL file load_oplan_data.sql

                sedscr.sh - This script is called by load_1ad_mysql.sh in order to handle 
                   conversion between unix style line endings that may be present
		   in the csv files and windows style line endings.  The script
                   will convert all csv to a standard line ending format to match the
                   load script's requirements.

                load_communities.sh - This script loads community csv files to the database
                   (community_attribute.csv, community_entity_attribute.csv).
                   The script only works for community csv files that do not have
                   an ASSEMBLY_ID column - (i.e. original 9.2 format). 

                   Script usage is:
                   ./load_communities.sh [db user] [db passwd] [db name] [assembly id]

                   All rows for the given ASSEMBLY_ID are deleted from the existing 
                   community tables, then the script loads the supplied csv files 
                   into the 2 respective community tables under the given ASSEMBLY_ID.
 
                   If no assembly id is provided the script will default to using
                   COMM-DEFAULT_CONFIG.  In this case, only newly created experiments
                   will get this community information. 
 
                   To change an existing experiment, or to avoid making this the 
                   default for all future experiments, locate the ASSEMBLY_ID in the
                   expt_trial_assembly table for your experiment whose ID begins with
                   "COMM-" and give that ASSEMBLY_ID as the argument to this script.

Editing of configuration database:

To edit data contained in the database, a user may directly edit any of the '.csv' files
contained in the csmart/data/database/csv directory and then reload the database. 

For developers:
To edit the structure of any tables contained in the configuration database, a user may
need to edit the following scripts to ensure their continued function:

csmart-db.drop.mysql-indexes.sql
csmart-db.drop-mysql-tables.sql
csmart-db.create-mysql-indexes.sql
csmart-db.create-mysql-tables.sql
csmart-db.load-mysql-tables.sql

If modifying or eliminating columns for a given table,  be sure to edit the 
corresponding .q files in csmart/data/common, and possibly the code that uses them.
