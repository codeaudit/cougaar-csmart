#!/bin/sh

# Environment variables
# COUGAAR_INSTALL_PATH = the head of the alp install.
# COUGAAR3RDPARTY = a directory containing 3rd party jar files
#
# COUGAAR bootstrapping classpath will be:
#  $COUGAAR_DEV_PATH	if defined
#  $COUGAAR_INSTALL_PATH/lib/core.jar
#
# once running, jar files will be searched for in (in order):
#  -Dorg.cougaar.class.path 	like classpath
#  $CLASSPATH		(alp bootstrapping path from above)
#  $COUGAAR_INSTALL_PATH/lib/*
#  $COUGAAR_INSTALL_PATH/plugins/*
#  -Dorg.cougaar.system.path=$COUGAAR3RDPARTY
#  $COUGAAR_INSTALL_PATH/sys/*
#

source $COUGAAR_INSTALL_PATH/bin/setlibpath.sh
source $COUGAAR_INSTALL_PATH/bin/setarguments.sh

if [ "$OS" = "Linux" ]; then
    # set some system runtime limits
    limit stacksize 16m    #up from 8m
    limit coredumpsize 0   #down from 1g
    #turn this on to enable inprise JIT
    #setenv JAVA_COMPILER javacomp
fi

LIBPATHS="${LIBPATHS}${SEP}${COUGAAR_INSTALL_PATH}/lib/csmart.jar"

MYPROPFILE=$COUGAAR_INSTALL_PATH/csmart/data/server.props
SERVER_CLASS=org.cougaar.tools.server.NodeServer

javaargs="$MYPROPERTIES $MYMEMORY -classpath $LIBPATHS"

if [ "$COUGAAR_DEV_PATH" != "" ]; then
    echo java $javaargs org.cougaar.core.society.Bootstrapper $MYPROPFILE
fi

exec java $javaargs org.cougaar.core.society.Bootstrapper $SERVER_CLASS $MYPROPFILE
