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

# Export the named recipe for import into another MySQL DB
# Note that you must get the appropriate queries
# from the recipeQueries.q file
# Note: multi-word recipe names must be in double quotes

if [ "x$4" = "x" ]; then
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
sed s/'INSERT INTO lib_mod_recipe'/'REPLACE INTO lib_mod_recipe'/ "$1-export.sql" > r-export.sql
mv r-export.sql "$1-export.sql"
sed s/'INSERT INTO alib_component'/'REPLACE INTO alib_component'/ "$1-export.sql" > r-export.sql
mv r-export.sql "$1-export.sql"
sed s/'INSERT INTO lib_component'/'REPLACE INTO lib_component'/ "$1-export.sql" > r-export.sql
mv r-export.sql "$1-export.sql"
sed s/'INSERT INTO lib_pg_attribute'/'REPLACE INTO lib_pg_attribute'/ "$1-export.sql" > r-export.sql
mv r-export.sql "$1-export.sql"

sed s/'INSERT INTO lib_agent_org'/'REPLACE INTO lib_agent_org'/ "$1-export.sql" > r-export.sql
mv r-export.sql "$1-export.sql"

# tell user name of export file, to load with -f option
echo "Recipe has been exported to $1-export.sql. Load into new database with command: mysql -f -u <user> -p<password> <db> < $1-export.sql"
echo "Note the use of the -f option, to ignore errors about duplicate rows"

# get the names of the queries to copy
echo "You must be sure to separately copy the following queries as well:"
if [ "x$5" = "x" ]; then
  echo "select distinct arg_value from tempcopy.lib_mod_recipe_arg where arg_value like 'recipeQuery%';" | mysql -s -u $2 -p$3 $4
else
  echo "select distinct arg_value from tempcopy.lib_mod_recipe_arg where arg_value like 'recipeQuery%';" | mysql -s -u $2 -p$3 -h $5 $4
fi

# delete the temp db
${COUGAAR_INSTALL_PATH}/csmart/bin/delete-temp-db.sh $2 $3 $4 $5

echo "Done."

