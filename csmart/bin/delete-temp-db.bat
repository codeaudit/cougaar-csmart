@echo OFF

REM 
REM <copyright>
REM  Copyright 2001-2003 BBNT Solutions, LLC
REM  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
REM 
REM  This program is free software; you can redistribute it and/or modify
REM  it under the terms of the Cougaar Open Source License as published by
REM  DARPA on the Cougaar Open Source Website (www.cougaar.org).
REM 
REM  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
REM  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
REM  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
REM  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
REM  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
REM  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
REM  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
REM  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
REM  PERFORMANCE OF THE COUGAAR SOFTWARE.
REM </copyright>

REM Delete the temporary database created when copying
REM or exporting an experiment or recipe
if [%3] == [] (
  echo Usage: delete-temp-db.bat [Config DB Username] [Password] [MySQL Config DB database name] [Optional: MySQL DB host name]
  GOTO L_END
)

if [%4] == [] (
  mysql -f -e "drop database tempcopy;" -u %1 -p%2 %3
) else (
  mysql -f -e "drop database tempcopy;" -u %1 -p%2 -h %4 %3
)

echo Temporary database removed.

:L_END
