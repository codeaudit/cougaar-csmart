#!/bin/sh

# 
# <copyright>
# Copyright 2001,2002 BBNT Solutions, LLC
# under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

# This program is free software; you can redistribute it and/or modify
# it under the terms of the Cougaar Open Source License as published by
# DARPA on the Cougaar Open Source Website (www.cougaar.org).

# THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
# PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
# IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
# ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
# HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
# DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
# TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
# PERFORMANCE OF THE COUGAAR SOFTWARE.
# </copyright>

if [ "x$1" = "x" ]; then
  echo "Usage: dump_1ad_mysql.sh [Config DB Username] [Password] [MySQL Config DB database name] "
  exit
fi

# Check to make sure csv directory exists already
DIR="$COUGAAR_INSTALL_PATH/csmart/data/database/csv/"
test -d $DIR || mkdir -p $DIR

# Check to make sure that the '.csv' files do not already exist in directory
# Use below file as test case.
MYFILE="$COUGAAR_INSTALL_PATH/csmart/data/database/csv/alib_component.csv"

if [ ! -e "$MYFILE" ]; then
    mkdir $COUGAAR_INSTALL_PATH/csmart/data/database/raw_data

    #Change potential backslashes in COUGAAR_INSTALL_PATH to forward slashes
    echo $COUGAAR_INSTALL_PATH | tr '\\' '/' > newcip.txt
    #Replace variable in sql script with CIP
    sed s/:cip/$(cat newcip.txt | sed 's/\//\\\//g')/ $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/dump_db.sql > dump_mysql_db_new.sql
    rm newcip.txt

    echo "Dumping database to .csv files."
    mysql -u$1 -p$2 $3 < dump_mysql_db_new.sql
    rm dump_mysql_db_new.sql

    echo "Concatenating files."
    concat_header_data.sh

    rm -rf $COUGAAR_INSTALL_PATH/csmart/data/database/raw_data
    echo "Done.";
else
    echo "'.csv' files already exist in CIP/csmart/data/database/csv/ directory."
    echo "You must either delete them or move them to a new directory.";
fi




