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
# This data comes with the distribution.
# You may also move the original data aside, and "dump" the data from a database
# for sharing, or editing and reloading
# Note that MySQL must be installed on the local machine, and
# Cougaar Install Path must be set

# If using networked drives, be sure you have MySQL v3.23.49 or better,
# and use the optional "local" argument.

if [ "x$3" = "x" ]; then
  echo "Usage: load_ref_data.sh [RefConfig DB Username] [Password] [MySQL RefConfig DB database name] [local]"
  echo "         -- use the 'local' keyword if running across a networked"
  echo "            drive. You must have MySQL v3.23.49 or better to use"
  echo "            the local option."
  exit
fi

if [ "x$COUGAAR_INSTALL_PATH" = "x" ] ; then
  echo "You must set COUGAAR_INSTALL_PATH to the root of your Cougaar install."
  echo "Usage: load_ref_data.sh [RefConfig DB Username] [Password] [MySQL RefConfig DB database name] [local]"
  echo "         -- use the 'local' keyword if running across a networked"
  echo "            drive. You must have MySQL v3.23.49 or better to use"
  echo "            the local option."
  exit
fi


#Change potential backslashes in COUGAAR_INSTALL_PATH to forward slashes
echo $COUGAAR_INSTALL_PATH | tr '\\' '/' > newcip.txt

#Replace variable in sql script with CIP
sed s/:cip/$(cat newcip.txt | sed 's/\//\\\//g')/ $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sql/load_ref_data.sql > load_ref_data_new.sql
rm newcip.txt

# Copy most files from the normal CSV dir. They are by definition the same
if [ -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/alploc.csv" ]; then
    cp "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/alploc.csv" "$COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv"
else
    echo "CSV files in csmart/data/database/csv missing for reference tables, and required!"
    exit
fi

if [ -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_transportable_item.csv" ]; then
    cp "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_transportable_item.csv" "$COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv"
fi

if [ -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_transportable_item_detail.csv" ]; then
    cp "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_transportable_item_detail.csv" "$COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv"
fi

if [ -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_unfrmd_srvc_occ_rnk_subcat.csv" ]; then
    cp "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_unfrmd_srvc_occ_rnk_subcat.csv" "$COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv"
fi

if [ -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_unfrmd_srvc_occptn.csv" ]; then
    cp "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_unfrmd_srvc_occptn.csv" "$COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv"
fi

if [ -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_unfrmd_srvc_rnk.csv" ]; then
    cp "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_unfrmd_srvc_rnk.csv" "$COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv"
fi

if [ -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_unit.csv" ]; then
    cp "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_unit.csv" "$COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv"
fi

if [ -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_unit_billet.csv" ]; then
    cp "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_unit_billet.csv" "$COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv"
fi

if [ -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_unit_equipment.csv" ]; then
    cp "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/fdm_unit_equipment.csv" "$COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv"
fi

if [ -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/geoloc.csv" ]; then
    cp "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/geoloc.csv" "$COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv"
fi

if [ -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/lib_pg_attribute.csv" ]; then
    cp "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/lib_pg_attribute.csv" "$COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv"
fi

if [ -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan.csv" ]; then
    cp "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan.csv" "$COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv"
fi

if [ -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr.csv" ]; then
    cp "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr.csv" "$COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv"
fi

# Done copying over files

if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sedscr.sh" ]; then
    echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sedscr.sh"
    exit
else
    #Replace potential '\r\n' combo in all .csv files with only '\n' to match load script
    $COUGAAR_INSTALL_PATH/csmart/data/database/scripts/mysql/sedscr.sh $COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv/*.csv
fi

if [ "$4" = "local" ]; then
    echo "Adding LOCAL to all lines"
    mv load_ref_data_new.sql load_ref_data_new_orig.sql
    sed "s/DATA INFILE/DATA LOCAL INFILE/g" load_ref_data_new_orig.sql > load_ref_data_new.sql
fi

echo "Loading '.csv' files to database."
if [ "$4" = "local" ]; then
    mysql --local-infile=1 -u$1 -p$2 $3 < load_ref_data_new.sql
else
    mysql -u$1 -p$2 $3 < load_ref_data_new.sql
fi

rm load_ref_data_new.sql
rm -f load_ref_data_new_orig.sql
rm $COUGAAR_INSTALL_PATH/csmart/data/database/ref-csv/*.tmp

echo "Done."

