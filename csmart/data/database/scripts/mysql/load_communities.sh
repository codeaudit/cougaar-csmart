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
# This data comes with the distribution.
# You may also move the original data aside, and "dump" the data from a database
# for sharing, or editing and reloading
# Note that MySQL must be installed on the local machine, and
# Cougaar Install Path must be set

if [ "x$3" = "x" ]; then
  echo "Usage: load_communities.sh [Config DB Username] [Password] [MySQL Config DB database name] [Assembly ID]"
  echo "         -- Deletes all rows for given assembly_id, then loads 2 community csv files to db "
  echo "            under the given assembly_id.  If no Assembly ID is provided, COMM-DEFAULT_CONFIG"
  echo "            is used as the assembly_id.  Will only load csv files that do not have an ASSEMBLY_ID"
  echo "            column."
  exit
fi

if [ "x$COUGAAR_INSTALL_PATH" = "x" ] ; then
  echo "You must set COUGAAR_INSTALL_PATH to the root of your Cougaar install."
  echo "Usage: load_communities.sh [Config DB Username] [Password] [MySQL Config DB database name] [Assembly ID]"
  echo "         -- Deletes all rows for given assembly_id, then loads 2 community csv files to db "
  echo "            under the given assembly_id.  If no Assembly ID is provided, COMM-DEFAULT_CONFIG"
  echo "            is used as the assembly_id.  Will only load csv files that do not have an ASSEMBLY_ID"
  echo "            column."
  exit
fi

if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/community_attribute.csv" ]; then
    echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/community_attribute.csv"
    echo "You must place the community csv files you are trying to load in the"
    echo "$COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql directory."
    exit
fi

if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/community_entity_attribute.csv" ]; then
    echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/community_entity_attribute.csv"
    echo "You must place the community csv files you are trying to load in the "
    echo "$COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql directory."
    exit
fi


if [ "$4" = "" ]; then 
    asbly_id="COMM-DEFAULT_CONFIG"
else
    asbly_id=$4
fi

#Change potential backslashes in COUGAAR_INSTALL_PATH to forward slashes
echo $COUGAAR_INSTALL_PATH | tr '\\' '/' > newcip.txt

#Replace variable in sql script with CIP
sed s/:cip/$(cat newcip.txt | sed 's/\//\\\//g')/ $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/load_comm.sql > load_comm_db_new.sql
rm newcip.txt

if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sedscr.sh" ]; then
    echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sedscr.sh"
    exit
else
    #Replace potential '\r\n' combo in all .csv files with only '\n' to match load script
    $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sedscr.sh $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/community*.csv
fi

#Loading only community csv files with a temporary asb_id
echo "Loading '.csv' files to database."
mysql -f -u$1 -p$2 $3 < load_comm_db_new.sql

rm load_comm_db_new.sql
rm $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/*.tmp

#Replace :asb_id in sql script with given assembly_id and update database
sed s/:asb_id/$asbly_id/g $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/update_comm.sql > update_comm_new.sql
mysql -f -u$1 -p$2 $3 < update_comm_new.sql
rm update_comm_new.sql

echo "Done."

