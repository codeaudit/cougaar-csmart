@ECHO OFF

REM "<copyright>"
REM " Copyright 2003 BBNT Solutions, LLC"
REM " under sponsorship of the Defense Advanced Research Projects Agency (DARPA)."
REM ""
REM " This program is free software; you can redistribute it and/or modify"
REM " it under the terms of the Cougaar Open Source License as published by"
REM " DARPA on the Cougaar Open Source Website (www.cougaar.org)."
REM ""
REM " THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS"
REM " PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR"
REM " IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF"
REM " MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT"
REM " ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT"
REM " HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL"
REM " DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,"
REM " TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR"
REM " PERFORMANCE OF THE COUGAAR SOFTWARE."
REM "</copyright>"


COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\new\headers\org_pg_attr_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\new\raw_data\org_pg_attr_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\new\csv\org_pg_attr.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\new\headers\org_relation_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\new\raw_data\org_relation_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\new\csv\org_relation.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\new\headers\lib_pg_attribute_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\new\raw_data\lib_pg_attribute_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\new\csv\lib_pg_attribute.csv

COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\new\headers\lib_organization_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\new\raw_data\lib_organization_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\new\csv\lib_organization.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\new\headers\oplan_agent_attr_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\new\raw_data\oplan_agent_attr_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\new\csv\oplan_agent_attr.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\new\headers\lib_oplan_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\new\raw_data\lib_oplan_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\new\csv\lib_oplan.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\new\headers\alploc_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\new\raw_data\alploc_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\new\csv\alploc.csv
