LOAD DATA INFILE ':cip/csmart/data/database/csv/alib_component.csv.tmp'
    INTO TABLE alib_component
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (COMPONENT_ALIB_ID,COMPONENT_NAME,COMPONENT_LIB_ID,COMPONENT_TYPE,CLONE_SET_ID);

LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_agent.csv.tmp'
    INTO TABLE asb_agent
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,COMPONENT_ALIB_ID,COMPONENT_LIB_ID,CLONE_SET_ID,COMPONENT_NAME);

LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_agent_pg_attr.csv.tmp'
    INTO TABLE asb_agent_pg_attr
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,COMPONENT_ALIB_ID,PG_ATTRIBUTE_LIB_ID,ATTRIBUTE_VALUE,ATTRIBUTE_ORDER,START_DATE,END_DATE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_agent_relation.csv.tmp'
    INTO TABLE asb_agent_relation
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,ROLE,SUPPORTING_COMPONENT_ALIB_ID,SUPPORTED_COMPONENT_ALIB_ID,START_DATE,END_DATE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_alploc.csv.tmp'
    INTO TABLE asb_alploc
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,ALPLOC_CODE,LOCATION_NAME,LATITUDE,LONGITUDE,INSTALLATION_TYPE_CODE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_assembly.csv.tmp'
    INTO TABLE asb_assembly
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,ASSEMBLY_TYPE,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_component_arg.csv.tmp'
    INTO TABLE asb_component_arg
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,COMPONENT_ALIB_ID,ARGUMENT,ARGUMENT_ORDER);

LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_component_hierarchy.csv.tmp'
    INTO TABLE asb_component_hierarchy
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,COMPONENT_ALIB_ID,PARENT_COMPONENT_ALIB_ID,PRIORITY,INSERTION_ORDER);

LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_oplan.csv.tmp'
    INTO TABLE asb_oplan
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,OPLAN_ID,OPERATION_NAME,PRIORITY,C0_DATE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_oplan_agent_attr.csv.tmp'
    INTO TABLE asb_oplan_agent_attr
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,OPLAN_ID,COMPONENT_ALIB_ID,COMPONENT_ID,START_CDAY,ATTRIBUTE_NAME,END_CDAY,ATTRIBUTE_VALUE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_alploc.csv.tmp'
    INTO TABLE cfw_alploc
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ALPLOC_CODE,LOCATION_NAME,LATITUDE,LONGITUDE,INSTALLATION_TYPE_CODE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_context_plugin_arg.csv.tmp'
    INTO TABLE cfw_context_plugin_arg
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ORG_CONTEXT,PLUGIN_ARG_ID);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_group.csv.tmp'
    INTO TABLE cfw_group
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_GROUP_ID,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_group_member.csv.tmp'
    INTO TABLE cfw_group_member
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,CFW_GROUP_ID);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_group_org.csv.tmp'
    INTO TABLE cfw_group_org
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_GROUP_ID,ORG_ID);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_instance.csv.tmp'
    INTO TABLE cfw_instance
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_oplan.csv.tmp'
    INTO TABLE cfw_oplan
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,OPLAN_ID,OPERATION_NAME,PRIORITY,C0_DATE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_oplan_activity.csv.tmp'
    INTO TABLE cfw_oplan_activity
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,OPLAN_ID,ORG_GROUP_ID,START_CDAY,END_CDAY,OPTEMPO,ACTIVITY_TYPE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_oplan_loc.csv.tmp'
    INTO TABLE cfw_oplan_loc
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,OPLAN_ID,ORG_GROUP_ID,START_CDAY,END_CDAY,LOCATION_CODE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_oplan_og_attr.csv.tmp'
    INTO TABLE cfw_oplan_og_attr
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,OPLAN_ID,ORG_GROUP_ID,START_CDAY,ATTRIBUTE_NAME,END_CDAY,ATTRIBUTE_VALUE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_org_group_org_member.csv.tmp'
    INTO TABLE cfw_org_group_org_member
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ORG_GROUP_ID,ORG_ID);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_org_hierarchy.csv.tmp'
    INTO TABLE cfw_org_hierarchy
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ORG_ID,SUPERIOR_ORG_ID);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_org_list.csv.tmp'
    INTO TABLE cfw_org_list
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ORG_ID);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_org_og_relation.csv.tmp'
    INTO TABLE cfw_org_og_relation
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ROLE,ORG_ID,ORG_GROUP_ID,START_DATE,END_DATE,RELATION_ORDER);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_org_orgtype.csv.tmp'
    INTO TABLE cfw_org_orgtype
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ORG_ID,ORGTYPE_ID);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_org_pg_attr.csv.tmp'
    INTO TABLE cfw_org_pg_attr
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ORG_ID,PG_ATTRIBUTE_LIB_ID,ATTRIBUTE_VALUE,ATTRIBUTE_ORDER,START_DATE,END_DATE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_orgtype_plugin_grp.csv.tmp'
    INTO TABLE cfw_orgtype_plugin_grp
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ORGTYPE_ID,PLUGIN_GROUP_ID);

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_plugin_group_member.csv.tmp'
    INTO TABLE cfw_plugin_group_member
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,PLUGIN_GROUP_ID,PLUGIN_ID,PLUGIN_CLASS_ORDER);

LOAD DATA INFILE ':cip/csmart/data/database/csv/community_attribute.csv.tmp'
    INTO TABLE community_attribute
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,COMMUNITY_ID,ATTRIBUTE_ID,ATTRIBUTE_VALUE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/community_entity_attribute.csv.tmp'
    INTO TABLE community_entity_attribute
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,COMMUNITY_ID,ENTITY_ID,ATTRIBUTE_ID,ATTRIBUTE_VALUE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/expt_experiment.csv.tmp'
    INTO TABLE expt_experiment
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (EXPT_ID,DESCRIPTION,NAME,CFW_GROUP_ID);

LOAD DATA INFILE ':cip/csmart/data/database/csv/expt_trial.csv.tmp'
    INTO TABLE expt_trial
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (TRIAL_ID,EXPT_ID,DESCRIPTION,NAME);

LOAD DATA INFILE ':cip/csmart/data/database/csv/expt_trial_assembly.csv.tmp'
    INTO TABLE expt_trial_assembly
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (TRIAL_ID,ASSEMBLY_ID,EXPT_ID,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/expt_trial_config_assembly.csv.tmp'
    INTO TABLE expt_trial_config_assembly
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (TRIAL_ID,ASSEMBLY_ID,EXPT_ID,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/expt_trial_mod_recipe.csv.tmp'
    INTO TABLE expt_trial_mod_recipe
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (TRIAL_ID,MOD_RECIPE_LIB_ID,RECIPE_ORDER,EXPT_ID);

LOAD DATA INFILE ':cip/csmart/data/database/csv/expt_trial_org_mult.csv.tmp'
    INTO TABLE expt_trial_org_mult
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (TRIAL_ID,CFW_ID,ORG_GROUP_ID,EXPT_ID,MULTIPLIER,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/expt_trial_thread.csv.tmp'
    INTO TABLE expt_trial_thread
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (TRIAL_ID,THREAD_ID,EXPT_ID);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_activity_type_ref.csv.tmp'
    INTO TABLE lib_activity_type_ref
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ACTIVITY_TYPE,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_agent_org.csv.tmp'
    INTO TABLE lib_agent_org
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (COMPONENT_LIB_ID,AGENT_LIB_NAME,AGENT_ORG_CLASS);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_clone_set.csv.tmp'
    INTO TABLE lib_clone_set
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CLONE_SET_ID);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_component.csv.tmp'
    INTO TABLE lib_component
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (COMPONENT_LIB_ID,COMPONENT_TYPE,COMPONENT_CLASS,INSERTION_POINT,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_component_arg.csv.tmp'
    INTO TABLE lib_component_arg
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (COMPONENT_LIB_ID,ARGUMENT);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_mod_recipe.csv.tmp'
    INTO TABLE lib_mod_recipe
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (MOD_RECIPE_LIB_ID,NAME,JAVA_CLASS,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_mod_recipe_arg.csv.tmp'
    INTO TABLE lib_mod_recipe_arg
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (MOD_RECIPE_LIB_ID,ARG_NAME,ARG_ORDER,ARG_VALUE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_org_group.csv.tmp'
    INTO TABLE lib_org_group
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ORG_GROUP_ID,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_organization.csv.tmp'
    INTO TABLE lib_organization
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ORG_ID,ORG_NAME,UIC);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_orgtype_ref.csv.tmp'
    INTO TABLE lib_orgtype_ref
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ORGTYPE_ID,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_pg_attribute.csv.tmp'
    INTO TABLE lib_pg_attribute
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (PG_ATTRIBUTE_LIB_ID,PG_NAME,ATTRIBUTE_NAME,ATTRIBUTE_TYPE,AGGREGATE_TYPE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_plugin_arg.csv.tmp'
    INTO TABLE lib_plugin_arg
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (PLUGIN_ARG_ID,ARGUMENT_ORDER,PLUGIN_ID,ARGUMENT,ARGUMENT_TYPE);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_plugin_arg_thread.csv.tmp'
    INTO TABLE lib_plugin_arg_thread
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (PLUGIN_ARG_ID,THREAD_ID);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_plugin_group.csv.tmp'
    INTO TABLE lib_plugin_group
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (PLUGIN_GROUP_ID,PLUGIN_GROUP_ORDER,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_plugin_ref.csv.tmp'
    INTO TABLE lib_plugin_ref
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (PLUGIN_ID,PLUGIN_CLASS,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_plugin_thread.csv.tmp'
    INTO TABLE lib_plugin_thread
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (PLUGIN_ID,THREAD_ID,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_role_ref.csv.tmp'
    INTO TABLE lib_role_ref
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ROLE,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_role_thread.csv.tmp'
    INTO TABLE lib_role_thread
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ROLE,THREAD_ID,DESCRIPTION);

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_thread.csv.tmp'
    INTO TABLE lib_thread
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (THREAD_ID,DESCRIPTION);

