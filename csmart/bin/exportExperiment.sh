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

# export a named experiment with all its supporting data
# Note that you must separately copy the necessary recipeQueries
# from recipeQueries.q
# Run this only _after_ running copy-experiment
# Note that multi-word experiment names must be in double quotes
# Also note that this depends on a valid CSMART install, including
# setting COUGAAR_INSTALL_PATH to an install that includes
# util.jar, bootstrap.jar, csmart.jar, and configs/common/CSMART.q
# and a valid cougaar.rc file

if [ "x$4" = "x" ]; then
  echo "Usage: exportExperiment.sh [Experiment to export] [DB Username] [DB Password] [DB name] [Optional: host name]"
  exit
fi

# TODO: Check that the experiment to copy exists, and maybe
# check that its a copy (does not have a base assembly)

if [ "x$5" = "x" ]; then
    ${COUGAAR_INSTALL_PATH}/csmart/bin/copy-experiment.sh "$1" "export" $2 $3 $4 export
else
    ${COUGAAR_INSTALL_PATH}/csmart/bin/copy-experiment.sh "$1" "export" $2 $3 $4 export $5
fi

# Check that named experiment exists
# if this fails, something went wrong in copy
if [ "x$5" = "x" ]; then
  EID=`mysql -s -e "select distinct name from expt_experiment where name = '$1-export'" -u $2 -p$3 tempcopy`
else
  EID=`mysql -s -e "select distinct name from expt_experiment where name = '$1-export'" -u $2 -p$3 -h $5 tempcopy`
fi

if [ "x$EID" = "x" ]; then
 echo $EID
 echo ""
 echo "Problem reading copied experiment.  Exiting."
 echo ""
 # delete the temp db
 ${COUGAAR_INSTALL_PATH}/csmart/bin/delete-temp-db.sh $2 $3 $4 $5
 exit
fi

# Do sed to get SQL with ExptName
# replace copyForExport with copyASBNoMod.sql
# and it supposedly does a complete
# copy without modifying the experiment - does it work?
sed s/:exptName/"$1"/ ${COUGAAR_INSTALL_PATH}/csmart/bin/copyForExport.sql > copyFE-fixed.sql

# run copyForExport
if [ "x$5" = "x" ]; then
  mysql -f -u $2 -p$3 $4 < copyFE-fixed.sql
else
  mysql -f -u $2 -p$3 -h $5 $4 < copyFE-fixed.sql
fi

rm copyFE-fixed.sql

##############
# Now invoke Java to update ALIB IDs
os=`uname`
SEP=";"
if [ $os = "Linux" -o $os = "SunOS" ]; then SEP=":"; fi
MYCLASSPATH="${COUGAAR_INSTALL_PATH}/lib/bootstrap.jar"

DEVPATH=""
if [ "$COUGAAR_DEV_PATH" != "" ] ; then
    #MYCLASSPATH="${COUGAAR_DEV_PATH}${SEP}${MYCLASSPATH}"
    DEVPATH="-Dorg.cougaar.class.path=${COUGAAR_DEV_PATH}"
fi
MYPROPERTIES="-Dorg.cougaar.install.path=$COUGAAR_INSTALL_PATH $DEVPATH"
javaargs="$MYPROPERTIES -cp $MYCLASSPATH"

echo "Updating ALIB IDs."
echo ""
echo "Running java $javaargs org.cougaar.bootstrap.Bootstrapper org.cougaar.tools.csmart.util.ExportExperimentHelper $1"

`java $javaargs org.cougaar.bootstrap.Bootstrapper org.cougaar.tools.csmart.util.ExportExperimentHelper $1`
echo ""
# Done with Java
###############

# do the dump
echo "Exporting experiment $1...."
if [ "x$5" = "x" ]; then
  mysqldump -q -l --add-locks -c -t -n -r "$1-export.sql" -u $2 -p$3 tempcopy
else
  mysqldump -q -l --add-locks -c -t -n -r "$1-export.sql" -u $2 -p$3 -h $5 tempcopy
fi

# Munge the dump file to replace INSERT with REPLACE for the tables
# lib_component and alib_component
sed s/'INSERT INTO lib_component'/'REPLACE INTO lib_component'/ "$1-export.sql" | sed s/'INSERT INTO alib_component'/'REPLACE INTO alib_component'/ | sed s/'INSERT INTO lib_pg_attribute'/'REPLACE INTO lib_pg_attribute'/ | sed s/'INSERT INTO lib_agent_org'/'REPLACE INTO lib_agent_org'/ > exp-export.sql
mv exp-export.sql "$1-export.sql"

echo ""

# tell the user the name of the export file
echo "Experiment has been exported to $1-export.sql, along with all of its recipes. Load into new database with command: mysql -f -u <user> -p<password> <db> < $1-export.sql"
echo "Note the use of the -f option, to avoid errors about duplicate rows, including missing recipes in the resulting experiment."

# get names of recipes to copy, tell user
echo "You must be sure to separately copy the following queries as well:"
if [ "x$5" = "x" ]; then
  echo "select distinct arg_value from tempcopy.lib_mod_recipe_arg where arg_value like 'recipeQuery%'" | mysql -s -u $2 -p$3 $4
else
  echo "select distinct arg_value from tempcopy.lib_mod_recipe_arg where arg_value like 'recipeQuery%'" | mysql -s -u $2 -p$3 -h $5 $4
fi

echo ""
# delete the temp db
${COUGAAR_INSTALL_PATH}/csmart/bin/delete-temp-db.sh $2 $3 $4 $5

echo "Export Done."

