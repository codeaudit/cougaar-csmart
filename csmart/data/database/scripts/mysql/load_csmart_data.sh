#!/bin/sh

# 
# <copyright>
#  
#  Copyright 2001-2004 BBNT Solutions, LLC
#  under sponsorship of the Defense Advanced Research Projects
#  Agency (DARPA).
# 
#  You can redistribute this software and/or modify it under the
#  terms of the Cougaar Open Source License as published on the
#  Cougaar Open Source Website (www.cougaar.org).
# 
#  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
#  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
#  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
#  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
#  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
#  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
#  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
#  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
#  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
#  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
#  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#  
# </copyright>

# Load the Configuration data contained in csmart/data/database/csv
# This data supplies data for the cougaar.rc entry
# org.cougaar.configuration.database

# Note that MySQL must be installed on the local machine, and
# Cougaar Install Path must be set

# If using networked drives, be sure you have MySQL v3.23.49 or better,
# and use the optional "local" argument.

if [ "x${COUGAAR_INSTALL_PATH}" = "x" ]; then 
    echo "COUGAAR_INSTALL_PATH not defined" 
    exit
fi

if [ "x$3" = "x" ]; then
  echo "Load only the data for controlling CSMART into the named database."
  echo "Usage: load_csmart_data.sh [DB Username] [password] [MySQL database name] [DB host [local]]"
  echo "   "
  echo "     Specify the hostname if the database is not local, or if you "
  echo "            want to specify the 'local' keyword "
  echo "   "
  echo "     Note:  Use the 'local' keyword if running across a networked"
  echo "            drive. You must have MySQL v3.23.49 or better to use"
  echo "            the local option."
  exit
fi

user=$1
pswd=$2
database=$3
host=$4
local=$5

echo "Dropping tables from database."
if [ "x$host" = "x" ]; then
    mysql -u$user -p$pswd $database < $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/csmart-db.drop-mysql-tables.sql
else
    mysql -u$user -p$pswd -h$host $database < $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/csmart-db.drop-mysql-tables.sql
fi

echo "Creating tables in database."
if [ "x$host" = "x" ]; then
    mysql -u$user -p$pswd $database < $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/csmart-db.create-mysql-tables.sql
else
    mysql -u$user -p$pswd -h$host $database < $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/csmart-db.create-mysql-tables.sql
fi

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

if [ "$local" = "local" ]; then
    echo "Adding LOCAL to all lines"
    mv load_mysql_db_new.sql load_mysql_db_new_orig.sql
    sed "s/DATA INFILE/DATA LOCAL INFILE/g" load_mysql_db_new_orig.sql > load_mysql_db_new.sql
fi

echo "Loading '.csv' files to database."
if [ "$local" = "local" ]; then
    mysql --local-infile=1 -u$user -p$pswd -h$host $database < load_mysql_db_new.sql
else
    if [ "x$host" = "x" ]; then
	mysql -u$user -p$pswd $database < load_mysql_db_new.sql
    else
	mysql -u$user -p$pswd -h$host $database < load_mysql_db_new.sql
    fi
fi

echo "Creating indexes in database tables."
if [ "x$host" = "x" ]; then
    mysql -u$user -p$pswd $database < $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/csmart-db.create-mysql-indexes.sql
else
    mysql -u$user -p$pswd -h$host $database < $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/csmart-db.create-mysql-indexes.sql
fi

rm load_mysql_db_new.sql
rm -f load_mysql_db_new_orig.sql
rm $COUGAAR_INSTALL_PATH/csmart/data/database/csv/*.tmp

echo "Done loading csmart config data."

