#The below commands create each given file with no headings

SELECT COMPONENT_ALIB_ID,COMPONENT_NAME,COMPONENT_LIB_ID,COMPONENT_TYPE,CLONE_SET_ID 
INTO OUTFILE ":cip/csmart/data/database/raw_data/alib_component_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM alib_component;

SELECT ASSEMBLY_ID,COMPONENT_ALIB_ID,COMPONENT_LIB_ID,CLONE_SET_ID,COMPONENT_NAME  
INTO OUTFILE ":cip/csmart/data/database/raw_data/asb_agent_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM asb_agent;

SELECT ASSEMBLY_ID,COMPONENT_ALIB_ID,PG_ATTRIBUTE_LIB_ID,ATTRIBUTE_VALUE,ATTRIBUTE_ORDER,START_DATE,END_DATE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/asb_agent_pg_attr_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM asb_agent_pg_attr;

SELECT ASSEMBLY_ID,ROLE,SUPPORTING_COMPONENT_ALIB_ID,SUPPORTED_COMPONENT_ALIB_ID,START_DATE,END_DATE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/asb_agent_relation_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM asb_agent_relation;

SELECT ASSEMBLY_ID,ALPLOC_CODE,LOCATION_NAME,LATITUDE,LONGITUDE,INSTALLATION_TYPE_CODE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/asb_alploc_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM asb_alploc;

SELECT ASSEMBLY_ID,ASSEMBLY_TYPE,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/asb_assembly_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM asb_assembly;

SELECT ASSEMBLY_ID,COMPONENT_ALIB_ID,ARGUMENT,ARGUMENT_ORDER 
INTO OUTFILE ":cip/csmart/data/database/raw_data/asb_component_arg_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM asb_component_arg;

SELECT ASSEMBLY_ID,COMPONENT_ALIB_ID,PARENT_COMPONENT_ALIB_ID,PRIORITY,INSERTION_ORDER
INTO OUTFILE ":cip/csmart/data/database/raw_data/asb_component_hierarchy_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM asb_component_hierarchy;

SELECT ASSEMBLY_ID,OPLAN_ID,OPERATION_NAME,PRIORITY,C0_DATE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/asb_oplan_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM asb_oplan;

SELECT ASSEMBLY_ID,OPLAN_ID,COMPONENT_ALIB_ID,COMPONENT_ID,START_CDAY,ATTRIBUTE_NAME,END_CDAY,ATTRIBUTE_VALUE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/asb_oplan_agent_attr_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM asb_oplan_agent_attr;

SELECT EXPT_ID,DESCRIPTION,NAME,CFW_GROUP_ID  
INTO OUTFILE ":cip/csmart/data/database/raw_data/expt_experiment_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM expt_experiment;

SELECT TRIAL_ID,EXPT_ID,DESCRIPTION,NAME 
INTO OUTFILE ":cip/csmart/data/database/raw_data/expt_trial_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM expt_trial;

SELECT TRIAL_ID,ASSEMBLY_ID,EXPT_ID,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/expt_trial_assembly_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM expt_trial_assembly;

SELECT TRIAL_ID,ASSEMBLY_ID,EXPT_ID,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/expt_trial_config_assembly_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM expt_trial_config_assembly;

SELECT TRIAL_ID,MOD_RECIPE_LIB_ID,RECIPE_ORDER,EXPT_ID 
INTO OUTFILE ":cip/csmart/data/database/raw_data/expt_trial_mod_recipe_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM expt_trial_mod_recipe;

SELECT TRIAL_ID,CFW_ID,ORG_GROUP_ID,EXPT_ID,MULTIPLIER,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/expt_trial_org_mult_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM expt_trial_org_mult;

SELECT TRIAL_ID,THREAD_ID,EXPT_ID 
INTO OUTFILE ":cip/csmart/data/database/raw_data/expt_trial_thread_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM expt_trial_thread;

SELECT ACTIVITY_TYPE,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_activity_type_ref_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_activity_type_ref;

SELECT COMPONENT_LIB_ID,AGENT_LIB_NAME,AGENT_ORG_CLASS 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_agent_org_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_agent_org;

SELECT CLONE_SET_ID 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_clone_set_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_clone_set;

SELECT COMPONENT_LIB_ID,COMPONENT_TYPE,COMPONENT_CLASS,INSERTION_POINT,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_component_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_component;

SELECT COMPONENT_LIB_ID,ARGUMENT 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_component_arg_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_component_arg;

SELECT MOD_RECIPE_LIB_ID,NAME,JAVA_CLASS,DESCRIPTION  
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_mod_recipe_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_mod_recipe;

SELECT MOD_RECIPE_LIB_ID,ARG_NAME,ARG_ORDER,ARG_VALUE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_mod_recipe_arg_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_mod_recipe_arg;

SELECT PG_ATTRIBUTE_LIB_ID,PG_NAME,ATTRIBUTE_NAME,ATTRIBUTE_TYPE,AGGREGATE_TYPE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_pg_attribute_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_pg_attribute;


##################################################################################


SELECT CFW_ID,ALPLOC_CODE,LOCATION_NAME,LATITUDE,LONGITUDE,INSTALLATION_TYPE_CODE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_alploc_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_alploc;

SELECT CFW_ID,ORG_CONTEXT,PLUGIN_ARG_ID 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_context_plugin_arg_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_context_plugin_arg;

SELECT CFW_GROUP_ID,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_group_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_group;

SELECT CFW_ID,CFW_GROUP_ID 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_group_member_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_group_member;

SELECT CFW_GROUP_ID,ORG_ID 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_group_org_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_group_org;

SELECT CFW_ID,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_instance_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_instance;


SELECT CFW_ID,OPLAN_ID,OPERATION_NAME,PRIORITY,C0_DATE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_oplan_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_oplan;

SELECT CFW_ID,OPLAN_ID,ORG_GROUP_ID,START_CDAY,END_CDAY,OPTEMPO,ACTIVITY_TYPE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_oplan_activity_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_oplan_activity;

SELECT CFW_ID,OPLAN_ID,ORG_GROUP_ID,START_CDAY,END_CDAY,LOCATION_CODE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_oplan_loc_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_oplan_loc;

SELECT CFW_ID,OPLAN_ID,ORG_GROUP_ID,START_CDAY,ATTRIBUTE_NAME,END_CDAY,ATTRIBUTE_VALUE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_oplan_og_attr_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_oplan_og_attr;

SELECT CFW_ID,ORG_GROUP_ID,ORG_ID 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_org_group_org_member_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_org_group_org_member;

SELECT cfw_id, org_id, superior_org_id 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_org_hierarchy_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_org_hierarchy;

SELECT CFW_ID,ORG_ID INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_org_list_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_org_list;

SELECT CFW_ID,ROLE,ORG_ID,ORG_GROUP_ID,START_DATE,END_DATE,RELATION_ORDER 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_org_og_relation_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_org_og_relation;

SELECT CFW_ID,ORG_ID,ORGTYPE_ID 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_org_orgtype_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_org_orgtype;

SELECT CFW_ID,ORG_ID,PG_ATTRIBUTE_LIB_ID,ATTRIBUTE_VALUE,ATTRIBUTE_ORDER,START_DATE,END_DATE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_org_pg_attr_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_org_pg_attr;

SELECT CFW_ID,ORGTYPE_ID,PLUGIN_GROUP_ID 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_orgtype_plugin_grp_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_orgtype_plugin_grp;

SELECT CFW_ID,PLUGIN_GROUP_ID,PLUGIN_ID,PLUGIN_CLASS_ORDER 
INTO OUTFILE ":cip/csmart/data/database/raw_data/cfw_plugin_group_member_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM cfw_plugin_group_member;

SELECT ORG_GROUP_ID,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_org_group_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_org_group;

SELECT ORG_ID,ORG_NAME,UIC 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_organization_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_organization;

SELECT ORGTYPE_ID,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_orgtype_ref_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_orgtype_ref;

SELECT PLUGIN_ARG_ID,ARGUMENT_ORDER,PLUGIN_ID,ARGUMENT,ARGUMENT_TYPE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_plugin_arg_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_plugin_arg;

SELECT PLUGIN_ARG_ID,THREAD_ID 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_plugin_arg_thread_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_plugin_arg_thread;

SELECT PLUGIN_GROUP_ID,PLUGIN_GROUP_ORDER,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_plugin_group_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_plugin_group;

SELECT PLUGIN_ID,PLUGIN_CLASS,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_plugin_ref_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_plugin_ref;

SELECT PLUGIN_ID,THREAD_ID,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_plugin_thread_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_plugin_thread;

SELECT ROLE,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_role_ref_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_role_ref;

SELECT ROLE,THREAD_ID,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_role_thread_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_role_thread;

SELECT THREAD_ID,DESCRIPTION 
INTO OUTFILE ":cip/csmart/data/database/raw_data/lib_thread_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM lib_thread;

SELECT ORG_GROUP_ID,ACTIVITY_TYPE,OPTEMPO,START_CDAY,END_CDAY 
INTO OUTFILE ":cip/csmart/data/database/raw_data/oplan_activity_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM oplan_activity;

SELECT ORG_GROUP_ID,LOCATION_CODE,START_CDAY,END_CDAY 
INTO OUTFILE ":cip/csmart/data/database/raw_data/oplan_loc_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM oplan_loc;

SELECT ASSEMBLY_ID,COMMUNITY_ID,ENTITY_ID,ATTRIBUTE_ID,ATTRIBUTE_VALUE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/community_entity_attribute_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM community_entity_attribute;

SELECT ASSEMBLY_ID,COMMUNITY_ID,ATTRIBUTE_ID,ATTRIBUTE_VALUE 
INTO OUTFILE ":cip/csmart/data/database/raw_data/community_attribute_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY "\n" FROM community_attribute;