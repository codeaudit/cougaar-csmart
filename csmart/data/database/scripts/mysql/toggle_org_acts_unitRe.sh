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
    echo "Usage: toggle_org_acts_unitRe.sh [1AD_Test] or [UA]"
    echo "   "
    echo "     Specify whether you want the oplan_agent_attr.csv file"
    echo "            to be the 'test' version (all 1AD) or 'final' version (UA). "
    exit
fi


if [ "$1" = "1AD_Test" ]; then
    echo "Copying oplan_agent_attr.csv to 1AD only test version."

    if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr_1ADTest.csv" ]; then
        echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr_1ADTest.csv"
        exit
    fi
    cp $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr.csv $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr_orig.csv
    cp $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr_1ADTest.csv $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr.csv
    
    echo "oplan_agent_attr.csv is now in its 1AD test Unit Re-Affiliation use case format."
else
    echo "Copying oplan_agent_attr.csv to the final UA use case version."
    if [ ! -e "$COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr_orig.csv" ]; then
        echo "Cannot find $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr_orig.csv"
        exit
    fi 
    cp $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr_orig.csv $COUGAAR_INSTALL_PATH/csmart/data/database/csv/oplan_agent_attr.csv
    
    echo "oplan_agent_attr.csv is now in final UA, Unit Re-Affiliation use case format."
fi