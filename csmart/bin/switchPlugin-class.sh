#!/bin/sh

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

