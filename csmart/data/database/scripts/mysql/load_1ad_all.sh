#!/bin/sh

# 
# <copyright>
#  Copyright 2001-2003 BBNT Solutions, LLC
#  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
# 
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the Cougaar Open Source License as published by
#  DARPA on the Cougaar Open Source Website (www.cougaar.org).
# 
#  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
#  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
#  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
#  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
#  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
#  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
#  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
#  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
#  PERFORMANCE OF THE COUGAAR SOFTWARE.
# </copyright>

# Load the Configuration data contained in csmart/data/database/csv
# This data comes with the distribution.
# You may also move the original data aside, and "dump" the data from a database
# for sharing, or editing and reloading
# Note that MySQL must be installed on the local machine, and
# Cougaar Install Path must be set

# If using networked drives, be sure you have MySQL v3.23.49 or better,
# and use the optional "local" argument.

if [ "x${COUGAAR_INSTALL_PATH}" = "x" ]; then 
    echo COUGAAR_INSTALL_PATH not defined; 
    exit; 
fi

if [ "x$4" = "x" ]; then
    echo "Usage: load_1ad_all.sh [DB Username] [Password] [domain db] [config db] [local]"
    echo "   "
    echo "      Note: [domain db] and [config db] can be the same but must be listed separately"
    echo "         -- use the 'local' keyword if running across a networked"
    echo "            drive. You must have MySQL v3.23.49 or better to use"
    echo "            the local option."
    exit
fi

user=$1
pswd=$2
domaindb=$3
configdb=$4
local=$5

domain_mysqlcmd="mysql -u $user -p$pswd $domaindb"
dbms_mysql=$COUGAAR_INSTALL_PATH/dbms/data/mysql
csmart_database=$COUGAAR_INSTALL_PATH/csmart/data/database

# The main db (1ADDomainData)
echo "Loading domain database: $domaindb"
#echo "unzip -p $dbms_mysql/1ADDomainData.zip 1ADDomainData.sql | $domain_mysqlcmd"
unzip -p $dbms_mysql/1ADDomainData.zip 1ADDomainData.sql | $domain_mysqlcmd

# Army_spares
#echo "unzip -p $dbms_mysql/army_spares.zip army.txt | $domain_mysqlcmd"
unzip -p $dbms_mysql/army_spares.zip army.txt | $domain_mysqlcmd

# Spares index
#echo "cat $dbms_mysql/spares_index.txt | $domain_mysqlcmd"
cat $dbms_mysql/spares_index.txt | $domain_mysqlcmd


# Config db (csv files)
echo "Loading config database: $configdb"
mysql -u$user -p$pswd $configdb < $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/drop_v4_v6.sql
echo "  - Dropping old tables from config database."
mysql -u$user -p$pswd $configdb < $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/csmart-db.drop-mysql-tables.sql
echo "  - Creating tables in config database."
mysql -u$user -p$pswd $configdb < $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/csmart-db.create-mysql-tables.sql

#Change potential backslashes in COUGAAR_INSTALL_PATH to forward slashes
echo $COUGAAR_INSTALL_PATH | tr '\\' '/' > newcip.txt

#Replace variable in sql script with CIP
sed s/:cip/$(cat newcip.txt | sed 's/\//\\\//g')/ $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/csmart-db.load-mysql-tables.sql > load_mysql_db_new.sql
rm newcip.txt

if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sedscr.sh" ]; then
    echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sedscr.sh"
    exit
else
    #Replace potential '\r\n' combo in all .csv files with only '\n' to match load script
    $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sedscr.sh $COUGAAR_INSTALL_PATH/csmart/data/database/csv/*.csv
fi

if [ "$5" = "local" ]; then
    echo "  - Adding LOCAL to all lines"
    mv load_mysql_db_new.sql load_mysql_db_new_orig.sql
    sed "s/DATA INFILE/DATA LOCAL INFILE/g" load_mysql_db_new_orig.sql > load_mysql_db_new.sql
fi

echo "  - Loading '.csv' files to config database."
if [ "$5" = "local" ]; then
    mysql --local-infile=1 -u$user -p$pswd $configdb < load_mysql_db_new.sql
else
    mysql -u$user -p$pswd $configdb < load_mysql_db_new.sql
fi

echo "  - Creating indexes in config database tables."
mysql -u$user -p$pswd $configdb < $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/csmart-db.create-mysql-indexes.sql

rm load_mysql_db_new.sql
rm -f load_mysql_db_new_orig.sql
rm $COUGAAR_INSTALL_PATH/csmart/data/database/csv/*.tmp

echo "Done."

