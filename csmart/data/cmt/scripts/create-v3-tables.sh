#!/bin/sh
#
# Create all the V3_ASB, V3_ EXPT, and V3_LIB tables
#USERID= must be defined as "username/password"
sqlplus $USERID @sql/create-v3-asb-tables.sql

