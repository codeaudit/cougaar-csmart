#!/bin/sh

# 
# <copyright>
# Copyright 2001 BBNT Solutions, LLC
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

# Export the named recipe for import into another MySQL DB
# Note that you must get the appropriate queries
# from the recipeQueries.q file
# Note: multi-word recipe names must be in double quotes

if [ "x$1" = "x" ]; then
  echo "Usage: export-recipe.sh [Recipe Name] [Config DB Username] [Password] [MySQL Config DB database name] [Optional: MySQL DB host name]"
  exit
fi

# TODO: Check and warn if recipe does not exist

# Note sed must be on path, as must MySQL
sed s/:recipeName/"$1"/ ${COUGAAR_INSTALL_PATH}/csmart/bin/copyRecipeForExport.sql > fixed-rec-exp.sql

# This next will cause an error if temp db already exists
# but deleting it would cause an error if it didnt
if [ "x$5" = "x" ]; then
  mysql -f -u $2 -p$3 $4 < fixed-rec-exp.sql
else
  mysql -f -u $2 -p$3 -h $5 $4 < fixed-rec-exp.sql
fi

rm fixed-rec-exp.sql

# do the dump
echo "Exporting recipe $1...."
if [ "x$5" = "x" ]; then
  mysqldump -q -l --add-locks -c -t -n -r "$1-export.sql" -u $2 -p$3 tempcopy 
else
  mysqldump -q -l --add-locks -c -t -n -r "$1-export.sql" -u $2 -p$3 -h $5 tempcopy
fi

# munge export script - replace INSERT with REPLACE
# Do I need to do more than these tables?
sed s/'INSERT INTO v4_lib_mod_recipe'/'REPLACE INTO v4_lib_mod_recipe'/ "$1-export.sql" > r-export.sql
mv r-export.sql "$1-export.sql"

# tell user name of export file, to load with -f option
echo "Recipe has been exported to $1-export.sql. Load into new database with command: mysql -f -u <user> -p<password> <db> $1-export.sql"
echo "Note the use of the -f option, to ignore errors about duplicate rows"

# get the names of the queries to copy
echo "You must be sure to separately copy the following queries as well:"
if [ "x$5" = "x" ]; then
  echo "select distinct arg_value from tempcopy.v4_lib_mod_recipe_arg where arg_value like 'recipeQuery%';" | mysql -s -u $2 -p$3 $4
else
  echo "select distinct arg_value from tempcopy.v4_lib_mod_recipe_arg where arg_value like 'recipeQuery%';" | mysql -s -u $2 -p$3 -h $5 $4
fi

# delete the temp db
${COUGAAR_INSTALL_PATH}/csmart/bin/delete-temp-db.sh $2 $3 $4 $5

echo "Done."

