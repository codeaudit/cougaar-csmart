#!/bin/sh
#
# Load all the V3_ASB, V3_ EXPT, and V3_LIB data in the data directory using the control files in the ctl directory
#USERID= must be defined as "username/password"
sqlplus $USERID @sql/create-v3-asb-tables.sql

