#!/bin/sh
#
# Load all the V5_CFW, V5_ALIB and V5_LIB data in the data directory using the control files in the ctl directory
#USERID= must be defined as "username/password"
# It is necessary to drop the FKEY Constraints first, and reload them after the data is loaded.
cd data
sqlload userid=$USERID control=../ctl/V5_CFW_ALPLOC.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_CONTEXT_PLUGIN_ARG.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_GROUP.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_GROUP_MEMBER.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_GROUP_ORG.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_INSTANCE.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_OPLAN.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_OPLAN_OG_ATTR.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_ORGTYPE_PLUGIN_GRP.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_ORG_GROUP.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_ORG_GROUP_OG_MEMBER.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_ORG_GROUP_ORG_MEMBER.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_ORG_HIERARCHY.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_ORG_LIST.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_ORG_OG_RELATION.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_ORG_ORGTYPE.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_ORG_PG_ATTR.ctl
sqlload userid=$USERID control=../ctl/V5_CFW_PLUGIN_GROUP_MEMBER.ctl
sqlload userid=$USERID control=../ctl/V5_LIB_ORGANIZATION.ctl
sqlload userid=$USERID control=../ctl/V5_LIB_ORGTYPE_REF.ctl
sqlload userid=$USERID control=../ctl/V5_LIB_PLUGIN_ARG.ctl
sqlload userid=$USERID control=../ctl/V5_LIB_PLUGIN_ARG_THREAD.ctl
sqlload userid=$USERID control=../ctl/V5_LIB_PLUGIN_GROUP.ctl
sqlload userid=$USERID control=../ctl/V5_LIB_PLUGIN_REF.ctl
sqlload userid=$USERID control=../ctl/V5_LIB_PLUGIN_THREAD.ctl
sqlload userid=$USERID control=../ctl/V5_LIB_ROLE_REF.ctl
sqlload userid=$USERID control=../ctl/V5_LIB_ROLE_THREAD.ctl
sqlload userid=$USERID control=../ctl/V5_LIB_THREAD.ctl
cd ..
