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

if [ "x$3" = "x" ]; then
  echo "Usage: load_1ad_mysql.sh [Config DB Username] [Password] [MySQL Config DB database name] "
  exit
fi

#Change potential backslashes in COUGAAR_INSTALL_PATH to forward slashes
echo $COUGAAR_INSTALL_PATH | tr '\\' '/' > newcip.txt
#Replace variable in sql script with CIP
sed s/:cip/$(cat newcip.txt | sed 's/\//\\\//g')/ load_mysql_db.sql > load_mysql_db_new.sql
rm newcip.txt
#sed s/:cip/$(echo "$COUGAAR_INSTALL_PATH" | sed 's/\\/\\\\\\\\/g')/ load_mysql_db.sql > load_mysql_db_new.sql
#sed s/:cip/$(echo "$COUGAAR_INSTALL_PATH" | sed 's/\//\\\//g')/ load_mysql_db.sql > load_mysql_db_new.sql

echo "Loading '.csv' files to database."
mysql -u$1 -p$2 $3 < load_mysql_db_new.sql

rm load_mysql_db_new.sql

echo "Done."



