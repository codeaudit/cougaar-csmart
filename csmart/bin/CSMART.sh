#!/bin/sh -f

# <copyright>
#  Copyright 2001 BBNT Solutions, LLC
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


# Main script to run CSMART on Unix
# Note that CSMART writes some files to its working directory.

os=`uname`
SEP=";"
if [ $os = "Linux" -o $os = "SunOS" ]; then SEP=":"; fi

# Next section lists Jar files required for CSMART. Note that by default CSMART uses the Cougaar
# Bootstrapper, which should find the jar files automatically (searching CIP/lib, plugins, sys, etc).
# One need only include the bootstrap jar file

# COUGAAR bootstrapping classpath will be:
#  $COUGAAR_INSTALL_PATH/lib/core.jar
#
# Plus any explicitly added entries - for us, the CSMART Jar file
    
# once running, jar files will be searched for in (in order):
#  -Dorg.cougaar.class.path 	like classpath
#  $COUGAAR_INSTALL_PATH/lib/*
#  $COUGAAR_INSTALL_PATH/plugins/*
#  -Dorg.cougaar.system.path=$COUGAAR3RDPARTY
#  $COUGAAR_INSTALL_PATH/sys/*
#  $CLASSPATH		(Cougaar bootstrapping path from above)
#
    
# To run without the Bootstrapper, set org.cougaar.useBootstrapper=false

MYCLASSPATH="${COUGAAR_INSTALL_PATH}/lib/bootstrap.jar"

DEVPATH=""
if [ "$COUGAAR_DEV_PATH" != "" ] ; then
    MYCLASSPATH="${COUGAAR_DEV_PATH}${SEP}${MYCLASSPATH}"
    DEVPATH="-Dorg.cougaar.class.path=${COUGAAR_DEV_PATH}"
fi
    
# The performance analyzer uses Excel. To use it or an equivalent,
# edit the following property:
MYEXCEL=-Dorg.cougaar.tools.csmart.excelpath=""
#if [ $SEP=";" ]; then
# I can't make this work
#    MYEXCEL=-Dorg.cougaar.tools.csmart.excelpath=\"C:/Program\ Files/Microsoft\ Office/Office/excel.exe\"; 
#fi

MYMEMORY="-Xms100m -Xmx300m"

# The delay between starting nodes, in milliseconds
MYDELAY="-Dorg.cougaar.tools.csmart.startdelay=0"

MYPROPERTIES="-Dorg.cougaar.install.path=$COUGAAR_INSTALL_PATH $MYEXCEL $MYDELAY $DEVPATH"

# It is possible to disable use of the workspace file: Your work
# will only be saved to the database when you explicitly save,
# and will not be restored from the workspace file. You will have to
# reload all experiments from the database every time you restart.
# To do so, uncomment the following line.
# set MYPROPERTIES="-Dorg.cougaar.tools.csmart.doWorkspace=false $MYPROPERTIES"

# Set the config path to include the basic CSMART config files first
MYCONFIGPATH="-Dorg.cougaar.config.path=$COUGAAR_INSTALL_PATH/csmart/data/common/\;"

javaargs="$MYPROPERTIES $MYMEMORY $MYCONFIGPATH -cp $MYCLASSPATH"

if [ "$COUGAAR_DEV_PATH" != "" ]; then
   echo java $javaargs org.cougaar.bootstrap.Bootstrapper org.cougaar.tools.csmart.ui.viewer.CSMART
fi

exec java $javaargs org.cougaar.bootstrap.Bootstrapper org.cougaar.tools.csmart.ui.viewer.CSMART
