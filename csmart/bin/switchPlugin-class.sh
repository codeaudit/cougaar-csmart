#!/bin/sh

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

# Replace one plugin classname with another deep in the DB
# For the named experiment
# Note that this experiment should _not_ be one of the base
# experiments, and that this fix will be lost
# if you modify the set of threads enabled in the
# experiment
# Finally, note that the experiment name should be in double quotes,
# if it is multiple words
    
if [ "x$6" = "x" ]; then
  echo "Usage: switchPlugin-class.sh [Old fully qual'ed plugin class] [New plugin class] [Experiment name to update] [Config DB Username] [Password] [MySQL Config DB database name] [Optional: MySQL DB Host]"
  exit
fi

# TODO:
# It would be really nice to check, then warn the user if the old plugin
# isn't in the DB or the experiment isnt in the DB
# Also, I could check that the experiment they gave doesn't use 
# one of the base Assemblies.

sed s/:newP/"$2"/ ${COUGAAR_INSTALL_PATH}/csmart/bin/switchPlugin-class.sql | sed s/:oldP/"$1"/ | sed s/:newExpt/"$3"/ > fixed-switch.sql

sed s/:newP/"$2"/ ${COUGAAR_INSTALL_PATH}/csmart/bin/get-assembly.sql | sed s/:oldP/"$1"/ | sed s/:newExpt/"$3"/ > fixed-ga.sql

# Warning: if this mysql returns more than one id,
# things will surely break.
# I need to loop over the values somehow
# FIXME!!!!
if [ "x$7" = "x" ]; then 
  ASSID=`mysql -s -u $4 -p$5 $6 < fixed-ga.sql`
  #echo "assid = $ASSID"
else
  ASSID=`mysql -s -u $4 -p$5 -h $7 $6 < fixed-ga.sql`
  #echo "assid on remote = $ASSID"
fi

sed s/:assID/"${ASSID}"/ fixed-switch.sql > fixed-switch-2.sql

if [ "x$7" = "x" ]; then 
  mysql -u $4 -p$5 $6 < fixed-switch-2.sql
else
  mysql -u $4 -p$5 -h $7 $6 < fixed-switch-2.sql
fi

rm fixed-switch.sql
rm fixed-switch-2.sql
rm fixed-ga.sql

# Echo to user message when done: Expt [foo] now uses Plugin [Bar] instead of [zap]
if [ "x$7" = "x" ]; then
  echo "Experiment $3 now uses plugin $2 instead of $1 in local database $6"
else
  echo "Experiment $3 now uses plugin $2 instead of $1 in database $6 on host $7"
fi

