#!/bin/sh -f

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

MYPROPERTIES="-Dorg.cougaar.install.path=$COUGAAR_INSTALL_PATH $MYEXCEL $MYDELAY -Dorg.cougaar.system.path=$COUGAAR3RDPARTY $DEVPATH"

# It is possible to disable use of the workspace file: Your work
# will only be saved to the database when you explicitly save,
# and will not be restored from the workspace file. You will have to
# reload all experiments from the database every time you restart.
# To do so, uncomment the following line.
# MYPROPERTIES="-Dorg.cougaar.tools.csmart.doWorkspace=false $MYPROPERTIES"


# By default, CSMART only permits Recipe target queries to look at
# the basic society definition, the communities, and the Agent,
# Nodes, and Hosts. In particular, you should not depend on the
# particular Plugins, Binder, or parameters within Agents. These items
# may be changed by a recipe, and those changes will not be available to 
# later recipes in deciding whether the recipe is applicable in that
# case. If however, you have a recipe that needs this added
# complexity (for example, wants to look at Agent relationships), then
# un-comment the following line.
# set MYPROPERTIES="-Dorg.cougaar.tools.csmart.allowComplexRecipeQueries=true $MYPROPERTIES"

# Set the config path to include the basic CSMART config files first
MYCONFIGPATH="-Dorg.cougaar.config.path=$COUGAAR_INSTALL_PATH/csmart/data/common/\;"

javaargs="$MYPROPERTIES $MYMEMORY $MYCONFIGPATH -cp $MYCLASSPATH"

if [ "$COUGAAR_DEV_PATH" != "" ]; then
   echo java $javaargs org.cougaar.bootstrap.Bootstrapper org.cougaar.tools.csmart.ui.viewer.CSMART
fi

exec java $javaargs org.cougaar.bootstrap.Bootstrapper org.cougaar.tools.csmart.ui.viewer.CSMART
