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

if [ "x$1" = "x" ]; then
    echo "Usage: toggle_ext_oplan.sh [compressed] or [extended]"
    echo "   "
    echo "     Specify whether you want the csv files to be in the "
    echo "            'compressed' or 'extended' oplan form. "
    exit
fi


if [ "$1" = "extended" ]; then
    echo "Copying extended oplan versions of csv files to loadable versions."

    if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr_ext.csv" ]; then
        echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr_ext.csv"
        exit
    fi
    if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_stage_ext.csv" ]; then
        echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_stage_ext.csv"
        exit
    fi 
    if [ ! -e "$COUGAAR_INSTALL_PATH/fcsua/data/database/csv/mission_period_3a_ext.csv" ]; then
        echo "Cannot find $COUGAAR_INSTALL_PATH/fcsua/data/database/csv/mission_period_3a_ext.csv"
        exit
    fi 
    cp $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr.csv $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr_orig.csv
    cp $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr_ext.csv $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr.csv
    
    cp $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_stage.csv $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_stage_orig.csv
    cp $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_stage_ext.csv $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_stage.csv
    
    cp $COUGAAR_INSTALL_PATH/fcsua/data/database/csv/mission_period_3a.csv $COUGAAR_INSTALL_PATH/fcsua/data/database/csv/mission_period_3a_orig.csv
    cp $COUGAAR_INSTALL_PATH/fcsua/data/database/csv/mission_period_3a_ext.csv $COUGAAR_INSTALL_PATH/fcsua/data/database/csv/mission_period_3a.csv
    echo "Database is now in extended oplan timeframe format."
else
    echo "Copying compressed oplan versions of csv files to loadable versions."
    if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr_orig.csv" ]; then
        echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr_orig.csv"
        exit
    fi 
    if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_stage_orig.csv" ]; then
        echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_stage_orig.csv"
        exit
    fi 
    if [ ! -e "$COUGAAR_INSTALL_PATH/fcsua/data/database/csv/mission_period_3a_orig.csv" ]; then
        echo "Cannot find $COUGAAR_INSTALL_PATH/fcsua/data/database/csv/mission_period_3a_orig.csv"
        exit
    fi 
    cp $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr_orig.csv $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr.csv
    
    cp $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_stage_orig.csv $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_stage.csv
    
    cp $COUGAAR_INSTALL_PATH/fcsua/data/database/csv/mission_period_3a_orig.csv $COUGAAR_INSTALL_PATH/fcsua/data/database/csv/mission_period_3a.csv
    echo "Database is now in compressed oplan timeframe format."
fi