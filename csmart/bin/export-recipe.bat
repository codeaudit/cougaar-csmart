@echo OFF

REM 
REM <copyright>
REM Copyright 2001,2002 BBNT Solutions, LLC
REM under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

REM This program is free software; you can redistribute it and/or modify
REM it under the terms of the Cougaar Open Source License as published by
REM DARPA on the Cougaar Open Source Website (www.cougaar.org).

REM THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
REM PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
REM IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
REM MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
REM ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
REM HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
REM DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
REM TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
REM PERFORMANCE OF THE COUGAAR SOFTWARE.
REM </copyright>

REM Export the named recipe for import into another MySQL DB
REM Note that you must get the appropriate queries
REM from the recipeQueries.q file
REM Note: multi-word recipe names must be in double quotes

REM TODO: Check and warn if recipe does not exist
REM Note sed must be on path, as must MySQL

if [%4] == [] (
  echo Usage: export-recipe.sh [Recipe Name] [Config DB Username] [Password] [MySQL Config DB database name] [Optional: MySQL DB host name]
  GOTO L_END
) 

REM FIXME: Must rename copyRecipeForExport.sql!!!
if exist cpRec.sql del cpRec.sql
copy %COUGAAR_INSTALL_PATH%\csmart\bin\copyRecipeForExport.sql .\cpRec.sql

%COUGAAR_INSTALL_PATH%\\csmart\\data\\database\\scripts\\sed.exe s/":recipeName"/%1/ cpRec.sql > fixrec.sql

if exist cpRec.sql del cpRec.sql

REM This next will cause an error if temp db already exists
REM but deleting it would cause an error if it didnt
if [%5] == [] (
  mysql -f -u %2 -p%3 %4 < fixrec.sql
) else (
  mysql -f -u %2 -p%3 -h %5 %4 < fixrec.sql
)

if exist fixrec.sql del fixrec.sql

REM do the dump
echo Exporting recipe %1....
if [%5] == [] (
  mysqldump -q -l --add-locks -c -t -n -r "rt-exprt.sql" -u %2 -p%3 tempcopy 
) else (
  mysqldump -q -l --add-locks -c -t -n -r "rt-exprt.sql" -u %2 -p%3 -h %5 tempcopy
)


REM munge export script - replace INSERT with REPLACE
REM Do I need to do more than these tables?
call %COUGAAR_INSTALL_PATH%\\csmart\\data\\database\\scripts\\sed.exe s/'INSERT INTO lib_mod_recipe'/'REPLACE INTO lib_mod_recipe'/ rt-exprt.sql > r-export.sql
if exist rt-exprt.sql del rt-exprt.sql
if exist r-export.sql move r-export.sql rt-exprt.sql

call %COUGAAR_INSTALL_PATH%\\csmart\\data\\database\\scripts\\sed.exe s/'INSERT INTO lib_component'/'REPLACE INTO lib_component'/ rt-exprt.sql > r-export.sql
if exist rt-exprt.sql del rt-exprt.sql
if exist r-export.sql move r-export.sql rt-exprt.sql

call %COUGAAR_INSTALL_PATH%\\csmart\\data\\database\\scripts\\sed.exe s/'INSERT INTO lib_pg_attribute'/'REPLACE INTO lib_pg_attribute'/ rt-exprt.sql > r-export.sql
if exist rt-exprt.sql del rt-exprt.sql
if exist r-export.sql move r-export.sql rt-exprt.sql

call %COUGAAR_INSTALL_PATH%\\csmart\\data\\database\\scripts\\sed.exe s/'INSERT INTO lib_agent_org'/'REPLACE INTO lib_agent_org'/ rt-exprt.sql > r-export.sql
if exist rt-exprt.sql del rt-exprt.sql
if exist r-export.sql move r-export.sql rt-exprt.sql

call %COUGAAR_INSTALL_PATH%\\csmart\\data\\database\\scripts\\sed.exe s/'INSERT INTO alib_component'/'REPLACE INTO alib_component'/ rt-exprt.sql > r-export.sql
if exist rt-exprt.sql del rt-exprt.sql
if exist ".\%1-export.sql" del ".\%1-export.sql"
if exist .\%1-export.sql del .\%1-export.sql
if exist r-export.sql move r-export.sql .\%1-export.sql

REM tell user name of export file, to load with -f option
echo Recipe has been exported to %1-export.sql. Load into new database with command: mysql -f -u [user] -p[password] [db] %1-export.sql
echo Note the use of the -f option, to ignore errors about duplicate rows.

REM get the names of the queries to copy
echo You must be sure to separately copy the following queries as well:

if [%5] == [] (
   mysql -s -e "select distinct arg_value from tempcopy.lib_mod_recipe_arg where arg_value like 'recipeQuery%%';" -u %2 -p%3 %4
) else (
   mysql -s -e "select distinct arg_value from tempcopy.lib_mod_recipe_arg where arg_value like 'recipeQuery%%';" -u %2 -p%3 -h %5 %4
)

REM delete the temp db
call %COUGAAR_INSTALL_PATH%\\csmart\\bin\\delete-temp-db.bat %2 %3 %4 %5

echo Done.

:L_END

