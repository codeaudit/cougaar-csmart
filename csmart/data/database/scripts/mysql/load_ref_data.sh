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

# Load the RefConfiguration data contained in csmart/data/database/ref-csv
# This script loads data for the cougaar.rc entry org.cougaar.refconfig.database

# Note that MySQL must be installed on the local machine, and
# Cougaar Install Path must be set

# If using networked drives, be sure you have MySQL v3.23.49 or better,
# and use the optional "local" argument.

if [ "x$3" = "x" ]; then
  echo "Load only the tables used to help configure societies running from XML files."
  echo "Usage: load_ref_data.sh [RefConfig DB Username] [Password] [MySQL RefConfig DB database name] [DB host name [local]]"
  echo "     Specify the hostname if the database is not local, or if you "
  echo "            want to specify the 'local' keyword "
  echo "   "
  echo "         -- use the 'local' keyword if running across a networked"
  echo "            drive. You must have MySQL v3.23.49 or better to use"
  echo "            the local option."
  exit
fi

if [ "x$COUGAAR_INSTALL_PATH" = "x" ] ; then
  echo "You must set COUGAAR_INSTALL_PATH to the root of your Cougaar install."
  echo "Usage: load_ref_data.sh [RefConfig DB Username] [Password] [MySQL RefConfig DB database name] [DB host name [local]]"
  echo "     Specify the hostname if the database is not local, or if you "
  echo "            want to specify the 'local' keyword "
  echo "   "
  echo "         -- use the 'local' keyword if running across a networked"
  echo "            drive. You must have MySQL v3.23.49 or better to use"
  echo "            the local option."
  exit
fi

user=$1
pswd=$2
database=$3
host=$4
local=$5

#Change potential backslashes in COUGAAR_INSTALL_PATH to forward slashes
echo $COUGAAR_INSTALL_PATH | tr '\\' '/' > newcip.txt

#Replace variable in sql script with CIP
sed s/:cip/$(cat newcip.txt | sed 's/\//\\\//g')/ $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/load_ref_data.sql > load_ref_data_new.sql
rm newcip.txt

# Copy some lib files from the normal CSV dir. They are by definition the same

if [ -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/lib_pg_attribute.csv" ]; then
    cp "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/lib_pg_attribute.csv" "$COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv"
else
    echo "CSV files in csmart/data/database/csv missing for reference tables, and required!"
    exit
fi

if [ -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/lib_organization.csv" ]; then
    cp "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/lib_organization.csv" "$COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv"
fi

# Done copying over files

if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sedscr.sh" ]; then
    echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sedscr.sh"
    exit
else
    #Replace potential '\r\n' combo in all .csv files with only '\n' to match load script
    $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sedscr.sh $COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv/*.csv
fi

if [ "$local" = "local" ]; then
    echo "Adding LOCAL to all lines"
    mv load_ref_data_new.sql load_ref_data_new_orig.sql
    sed "s/DATA INFILE/DATA LOCAL INFILE/g" load_ref_data_new_orig.sql > load_ref_data_new.sql
fi

echo "Loading '.csv' files to database."
if [ "$local" = "local" ]; then
    mysql --local-infile=1 -u$user -p$pswd -h$host $database < load_ref_data_new.sql
else
    if [ "x$host" = "x" ]; then
	mysql -u$user -p$pswd $database < load_ref_data_new.sql
    else
	mysql -u$user -p$pswd -h$host $database < load_ref_data_new.sql
    fi
fi

rm load_ref_data_new.sql
rm -f load_ref_data_new_orig.sql
rm $COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv/*.tmp
rm $COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv/lib*

echo "Done loading xml ref data."

