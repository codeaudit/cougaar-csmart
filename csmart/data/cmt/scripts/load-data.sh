#!/bin/sh
#
# Load all the V3_ASB, V3_ EXPT, and V3_LIB data in the data directory using the control files in the ctl directory
#USERID= must be defined as "username/password"
cd data
sqlload userid=$USERID control=../ctl/V3_ASB_AGENT_PG_ATTR.ctl
sqlload userid=$USERID control=../ctl/V3_ASB_AGENT_RELATION.ctl
sqlload userid=$USERID control=../ctl/V3_ASB_ALPLOC.ctl
sqlload userid=$USERID control=../ctl/V3_ASB_ASSEMBLY.ctl
sqlload userid=$USERID control=../ctl/V3_ASB_COMPONENT.ctl
sqlload userid=$USERID control=../ctl/V3_ASB_COMPONENT_ARG.ctl
sqlload userid=$USERID control=../ctl/V3_ASB_COMPONENT_NODE.ctl
sqlload userid=$USERID control=../ctl/V3_ASB_NODE.ctl
sqlload userid=$USERID control=../ctl/V3_ASB_OPLAN.ctl
sqlload userid=$USERID control=../ctl/V3_ASB_OPLAN_AGENT_ATTR.ctl
sqlload userid=$USERID control=../ctl/V3_EXPT_ASSEMBLY.ctl
sqlload userid=$USERID control=../ctl/V3_EXPT_EXPERIMENT.ctl
sqlload userid=$USERID control=../ctl/V3_LIB_ACTIVITY_TYPE_REF.ctl
sqlload userid=$USERID control=../ctl/V3_LIB_AGENT_ORG.ctl
sqlload userid=$USERID control=../ctl/V3_LIB_COMPONENT.ctl
sqlload userid=$USERID control=../ctl/V3_LIB_COMPONENT_ARG.ctl
sqlload userid=$USERID control=../ctl/V3_LIB_OPLAN_AGENT_ATTR_REF.ctl
sqlload userid=$USERID control=../ctl/V3_LIB_OPTEMPO_REF.ctl
sqlload userid=$USERID control=../ctl/V3_LIB_PG_ATTRIBUTE.ctl
cd ..
