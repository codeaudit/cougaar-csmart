csmart/data/database/README

This file describes the directory structure containing scripts and data
files necessary for creating a MySQL configuration database used by CSMART.
See csmart/doc/InstallandTest.html or the User's Guide for more information.

Summary:
After creating a database per the instructions referenced above, you load
it with data from files contained here.
First, load the data in the Domain-MySQL.ZIP file.
Then, to load the configuration information (necessary to "clean" your
installation, and when upgrading to the most recent Cougaar version),
use ./scripts/mysql/load_1ad_mysql.sh[bat] 
(Run without arguments to see usage.)
To edit the contents of these tables directly (not recommended), edit
the csv files in ./csv

csmart/data/database directory description:

This directory contains several subdirectories as well as a zip file.

Zip files:

"Domain-MySQL.ZIP" contains a datafile called "1ad_domain_data_dump.sql"
which is the sql script to be run to load all domain data tables into 
the CSMART configuration database. If you are upgrading from version
9.4 or below of Cougaar, you should re-load this data as an additional
table and new domain data have been added.

refconfigdb.zip:
   This is a dump that includes all of the data in the above data dump,
   PLUS reference OPLAN and Military Organization data for the UltraLog 1AD
   societies. This should define a new "refconfig" database, if you want
   to run a 1AD society strictly from XML files + reference data.

Sub-directories:

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

   headers/:
        Contains a '.csv' file for each corresponding table/'.csv' file 
        with a single row containing the column headers for each table in
        the database.  These headers are used by the database dump script
        'dump_1ad_mysql.sh' to properly generate 'csv' files containing
        headers.

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
		   
                        dump_db.sql - SQL script that generates a '.csv' file
                                      for each table in the configuration database.
                                      Called by the 'dump_1ad_mysql.sh' script.
                                      It should not be called directly by the user.
                        
                        drop_v4_v6.sql - SQL script that drops all old (prior to 9.4.1)
                                         tables that had the v4/v6 prefix. This script 
                                         is called by the load_1ad_mysql.sh/bat script.
                        
                        Note: The following group of 5 sql scripts are generated from a
                              perl script run against sql source code from ER/Studio.  
                              These scripts are called by the load_1ad_mysql.sh/bat scripts
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
                        
                Also contains the scripts - 
 
                dump_1ad_mysql.sh[bat] - Script for generating a '.csv' file 
                                  in the csmart/data/database/csv directory
                                  for each assembly and cfw/lib table in the 
                                  configuration database.  The script will
                                  check to see if '.csv' files already exist
                                  and if so, requires the user to either 
                                  delete the existing files, or rename the
                                  directory before proceeding.  

                                  The script calls the mysql script 
                                  'sql/dump_db.sql' as well as the 
                                  'concat_header_data.sh[bat]' script.

                                  NOTE: The database user must have the 'File'
                                  privilege in the mysql database in order to 
                                  run this script.  See csmart/doc/InstallandTest.html 
                                  for information on setting this privilege.
                

                concat_header_data.sh[bat] - Called by the 'dump_1ad_mysql.sh' 
                                      script and serves to concatenate the raw data
                                      files generated by the 'dump_1ad_mysql.sh' 
                                      script with the existing header '.csv' files
                                      present in the headers/ directory.  This allows
                                      a user to easily edit the generated '.csv' files.
                                      It should not be called directly by the user.

                load_1ad_mysql.sh[bat] - This script will load the configuration database from
                                  the '.csv' files contained in the csmart/data/database/csv
                                  directory.  The script will drop all existing configuration
                                  database tables before loading the new ones.
                                  The script calls several mysql scripts located in 
                                  csmart/data/database/scripts/mysql/sql including:
                                    csmart-db.create-mysql-tables.sql
                                    csmart-db.create-mysql-indexes.sql
                                    csmart-db.drop-mysql-tables.sql
                                    csmart-db.load-mysql-tables.sql 
                                    drop_v4_v6.sql
                                  This script is an easy way to "clean out" your configuration
                                  database.

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

                 load_comm.sql - SQL script called by load_communities.sh to load data from csv files.
                                 Should not be called directly by the user.

                 update_comm.sql - SQL script called by load_communities.sh to delete existing rows
                                   under the provided ASSEMBLY_ID and associate the provided ASSEMBLY_ID
                                   with the data loaded from the csv file.  Should not be called directly
                                   by the user.

      


Editing of configuration database:

To edit data contained in the database, a user may directly edit any of the '.csv' files
contained in the csmart/data/database/csv directory and then reload the database. 

For developers:
To edit the structure of any tables contained in the configuration database, a user may
need to edit the following scripts to ensure their continued function:

dump_db.sql
csmart-db.drop.mysql-indexes.sql
csmart-db.drop-mysql-tables.sql
csmart-db.create-mysql-indexes.sql
csmart-db.create-mysql-tables.sql
csmart-db.load-mysql-tables.sql


If modifying or eliminating columns for a given table,  the user should verify that the 
respective header file contained in csmart/data/database/headers/
remains valid. Be sure to edit the 
corresponding .q files in csmart/data/common, and possibly the code that uses them.




