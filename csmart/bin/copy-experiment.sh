#!/bin/sh

# <copyright>
#  Copyright 2001,2002 BBNT Solutions, LLC
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

# Copy a CSMART Experiment in the database doing a deep
# copy of the society, such that you can modify that society
# in place later...
# Note that multi-word experiment names or suffixes must
# be in double quotes

if [ "x$6" = "x" ]; then
  echo "Usage: copy-experiment.sh [Old Experiment Name] [Suffix to add to Exp Name] [Config DB Username] [Password] [MySQL Config DB database name] [export] [Optional: MySQL DB host name]"
  exit
fi

# Check that the Experiment Exists First
# if it doesn't, list all experiments.
if [ "x$7" = "x" ]; then
  EID=`mysql -s -e "select distinct name from v4_expt_experiment where name = '$1'" -u $3 -p$4 $5`
else
  EID=`mysql -s -e "select distinct name from v4_expt_experiment where name = '$1'" -u $3 -p$4 -h $7 $5`
fi

if [ -z "${EID}" ]; then
 echo ""
 echo "The experiment, $1 is not a known experiment."
 echo "All known experiment names:"
 if [ "x$7" = "x" ]; then
     echo "select NAME from v4_expt_experiment" | mysql -s -u $3 -p$4 $5
 else
     echo "select NAME from v4_expt_experiment" | mysql -s -u $3 -p$4 -h $7 $5
 fi
 echo ""
 exit
fi

# Note sed must be on path, as must MySQL
if [ "$6" = "export" ]; then
    sed s/:suffix/"$2"/ ${COUGAAR_INSTALL_PATH}/csmart/bin/copyASBExport.sql | sed s/:oldExpt/"$1"/ > fixed-copy.sql
else
    sed s/:suffix/"$2"/ ${COUGAAR_INSTALL_PATH}/csmart/bin/copyASB.sql | sed s/:oldExpt/"$1"/ > fixed-copy.sql
fi

# Still need to do a replace on the orig expt ID
# FIXME: If the db is screwy this might give more than one ID
# which breaks the later sed
# Need to warn if the number of items is other than 1
if [ "x$7" = "x" ]; then
  OEID=`mysql -s -e "select expt_id from v4_expt_experiment where name = '$1'" -u $3 -p$4 $5`
else
  OEID=`mysql -s -e "select expt_id from v4_expt_experiment where name = '$1'" -u $3 -p$4 -h $7 $5`
fi

sed s/:oldeid/${OEID}/ fixed-copy.sql > fixed-copy-2.sql
mv fixed-copy-2.sql fixed-copy.sql

if [ "x$7" = "x" ]; then
  mysql -f -u $3 -p$4 $5 < fixed-copy.sql
else
  mysql -f -u $3 -p$4 -h $7 $5 < fixed-copy.sql
fi

rm fixed-copy.sql
if [ "$6" != "export" ]; then
    if [ "x$7" = "x" ]; then
	echo "Experiment $1 deep copied as $1-$2 in db $5."
	echo "To swap a plugin, do: ./switchPlugin-class.sh [Old full plugin class] [new full plugin class] \"$1-$2\" $3 $4 $5"
    else
	echo "Experiment $1 deep copied as $1-$2 in db $5 on host $7."
	echo "To swap a plugin, do: ./switchPlugin-class.sh [Old full plugin class] [new full plugin class] \"$1-$2\" $3 $4 $5 $7"
    fi
    # Drop the temporary database
    ${COUGAAR_INSTALL_PATH}/csmart/bin/delete-temp-db.sh $3 $4 $5 $7
fi

echo "Copy Done."
