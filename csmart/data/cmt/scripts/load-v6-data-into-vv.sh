#!/bin/sh
#
# Load all the V5_CFW, V5_ALIB and V5_LIB data in the data directory using the control files in the ctl directory
#USERID= must be defined as "username/password"
# It is necessary to drop the FKEY Constraints first, and reload them after the data is loaded.
#
sqlload userid=$USERID data=V6_CFW_ALPLOC.csv control=../ctl/V5_VV_CFW_ALPLOC.ctl
sqlload userid=$USERID data=V6_CFW_CONTEXT_PLUGIN_ARG.csv control=../ctl/V5_VV_CFW_CONTEXT_PLUGIN_ARG.ctl
sqlload userid=$USERID data=V6_CFW_GROUP.csv control=../ctl/V5_VV_CFW_GROUP.ctl
sqlload userid=$USERID data=V6_CFW_GROUP_MEMBER.csv control=../ctl/V5_VV_CFW_GROUP_MEMBER.ctl
sqlload userid=$USERID data=V6_CFW_GROUP_ORG.csv control=../ctl/V5_VV_CFW_GROUP_ORG.ctl
sqlload userid=$USERID data=V6_CFW_INSTANCE.csv control=../ctl/V5_VV_CFW_INSTANCE.ctl
sqlload userid=$USERID data=V6_CFW_OPLAN.csv control=../ctl/V5_VV_CFW_OPLAN.ctl
sqlload userid=$USERID data=V6_CFW_OPLAN_OG_ATTR.csv control=../ctl/V5_VV_CFW_OPLAN_OG_ATTR.ctl
sqlload userid=$USERID data=V6_CFW_ORGTYPE_PLUGIN_GRP.csv control=../ctl/V5_VV_CFW_ORGTYPE_PLUGIN_GRP.ctl
sqlload userid=$USERID data=V6_CFW_ORG_GROUP_OG_MEMBER.csv control=../ctl/V5_VV_CFW_ORG_GROUP_OG_MEMBER.ctl
sqlload userid=$USERID data=V6_CFW_ORG_GROUP_ORG_MEMBER.csv control=../ctl/V5_VV_CFW_ORG_GROUP_ORG_MEMBER.ctl
sqlload userid=$USERID data=V6_CFW_ORG_HIERARCHY.csv control=../ctl/V5_VV_CFW_ORG_HIERARCHY.ctl
sqlload userid=$USERID data=V6_CFW_ORG_LIST.csv control=../ctl/V5_VV_CFW_ORG_LIST.ctl
sqlload userid=$USERID data=V6_CFW_ORG_OG_RELATION.csv control=../ctl/V5_VV_CFW_ORG_OG_RELATION.ctl
sqlload userid=$USERID data=V6_CFW_ORG_ORGTYPE.csv control=../ctl/V5_VV_CFW_ORG_ORGTYPE.ctl
sqlload userid=$USERID data=V6_CFW_ORG_PG_ATTR.csv control=../ctl/V5_VV_CFW_ORG_PG_ATTR.ctl
sqlload userid=$USERID data=V6_CFW_PLUGIN_GROUP_MEMBER.csv control=../ctl/V5_VV_CFW_PLUGIN_GROUP_MEMBER.ctl
sqlload userid=$USERID data=V6_LIB_ORGANIZATION.csv control=../ctl/V5_VV_LIB_ORGANIZATION.ctl
sqlload userid=$USERID data=V6_LIB_ORGTYPE_REF.csv control=../ctl/V5_VV_LIB_ORGTYPE_REF.ctl
sqlload userid=$USERID data=V6_LIB_ORG_GROUP.csv control=../ctl/V5_VV_LIB_ORG_GROUP.ctl
sqlload userid=$USERID data=V6_LIB_PLUGIN_ARG.csv control=../ctl/V5_VV_LIB_PLUGIN_ARG.ctl
sqlload userid=$USERID data=V6_LIB_PLUGIN_ARG_THREAD.csv control=../ctl/V5_VV_LIB_PLUGIN_ARG_THREAD.ctl
sqlload userid=$USERID data=V6_LIB_PLUGIN_GROUP.csv control=../ctl/V5_VV_LIB_PLUGIN_GROUP.ctl
sqlload userid=$USERID data=V6_LIB_PLUGIN_REF.csv control=../ctl/V5_VV_LIB_PLUGIN_REF.ctl
sqlload userid=$USERID data=V6_LIB_PLUGIN_THREAD.csv control=../ctl/V5_VV_LIB_PLUGIN_THREAD.ctl
sqlload userid=$USERID data=V6_LIB_ROLE_REF.csv control=../ctl/V5_VV_LIB_ROLE_REF.ctl
sqlload userid=$USERID data=V6_LIB_ROLE_THREAD.csv control=../ctl/V5_VV_LIB_ROLE_THREAD.ctl
sqlload userid=$USERID data=V6_LIB_THREAD.csv control=../ctl/V5_VV_LIB_THREAD.ctl
