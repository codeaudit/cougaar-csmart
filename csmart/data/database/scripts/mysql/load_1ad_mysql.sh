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

# Load the Configuration data contained in csmart/data/database/csv
# This data comes with the distribution.
# You may also move the original data aside, and "dump" the data from a database
# for sharing, or editing and reloading
# Note that MySQL must be installed on the local machine, and
# Cougaar Install Path must be set

if [ "x$3" = "x" ]; then
  echo "Usage: load_1ad_mysql.sh [Config DB Username] [Password] [MySQL Config DB database name] "
  exit
fi

if [ "x$COUGAAR_INSTALL_PATH" = "x" ] ; then
  echo "You must set COUGAAR_INSTALL_PATH to the root of your Cougaar install."
  echo "Usage: load_1ad_mysql.sh [Config DB Username] [Password] [MySQL Config DB database name] "
  exit
fi


#Change potential backslashes in COUGAAR_INSTALL_PATH to forward slashes
echo $COUGAAR_INSTALL_PATH | tr '\\' '/' > newcip.txt

#Replace variable in sql script with CIP
sed s/:cip/$(cat newcip.txt | sed 's/\//\\\//g')/ $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/load_db.sql > load_mysql_db_new.sql
rm newcip.txt

if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sedscr.sh" ]; then
    echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sedscr.sh"
    exit
else
    #Replace potential '\r\n' combo in all .csv files with only '\n' to match load script
    $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sedscr.sh $COUGAAR_INSTALL_PATH/csmart/data/database/csv/*.csv
fi

echo "Loading '.csv' files to database."
mysql -u$1 -p$2 $3 < load_mysql_db_new.sql

rm load_mysql_db_new.sql
rm $COUGAAR_INSTALL_PATH/csmart/data/database/csv/*.tmp

echo "Done."
