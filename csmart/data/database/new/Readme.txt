This directory contains files defining and loading tables
defining an OPLAN and military organization OrgAssets supporting
the UL 1AD configuration.

Most users can ignore this directory, focusing on the refconfigdb.zip
file one level up, which is the result of running included scripts, etc.

Users who want to add or edit Organizations (for use when running
from XML files) or OPLANS, may want to look further.

To generate the refconfigdb dump, you must:
1) create the database
2) load the Domain-MySQL.zip contents
3) run new/scripts/load_1ad_mysq.[bat/sh]

This will load the OPLAN/Organizations defined in the included CSV files.

These CSV files were generated from the CSMART configuration database by:
1) Generating a FULL-1AD-TRANS society with threads 1,3,5, and 9
2) Running the included script new/scripts/create_new_db_from_old.[bat/sh]


