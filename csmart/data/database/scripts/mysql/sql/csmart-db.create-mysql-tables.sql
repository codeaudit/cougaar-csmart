##
##ER/Studio 5.1 SQL Code Generation
## Company :      BBNT
## Project :      CSMART Database
## Author :       M. Kappler & J. Berliner
##
## Date Created : Thursday, June 27, 2002 11:09:07
## Target DBMS : Oracle 8
##


## 
## TABLE: alib_component 
##

CREATE TABLE alib_component(
    COMPONENT_ALIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    COMPONENT_NAME       VARCHAR(150)    BINARY DEFAULT NULL,
    COMPONENT_LIB_ID     VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    COMPONENT_TYPE       VARCHAR(50)    BINARY DEFAULT NULL,
    CLONE_SET_ID         DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    UNIQUE KEY pk_alib_component (COMPONENT_ALIB_ID)
) TYPE=MyISAM 
;


## 
## TABLE: asb_agent 
##

CREATE TABLE asb_agent(
    ASSEMBLY_ID          VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    COMPONENT_ALIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    COMPONENT_LIB_ID     VARCHAR(150)    BINARY DEFAULT NULL,
    CLONE_SET_ID         DECIMAL(68,30) DEFAULT NULL,
    COMPONENT_NAME       VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_asb_agent (ASSEMBLY_ID, COMPONENT_ALIB_ID)
) TYPE=MyISAM 
;


## 
## TABLE: asb_agent_pg_attr 
##

CREATE TABLE asb_agent_pg_attr(
    ASSEMBLY_ID            VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    COMPONENT_ALIB_ID      VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    PG_ATTRIBUTE_LIB_ID    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_VALUE        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_ORDER        DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    START_DATE             DATETIME             NOT NULL DEFAULT '0000-00-00 00:00:00',
    END_DATE               DATETIME             DEFAULT NULL,
    UNIQUE KEY pk_asb_agent_pg_attr (ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE)
) TYPE=MyISAM 
;


## 
## TABLE: asb_agent_relation 
##

CREATE TABLE asb_agent_relation(
    ASSEMBLY_ID                     VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ROLE                            VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    SUPPORTING_COMPONENT_ALIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    SUPPORTED_COMPONENT_ALIB_ID     VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    START_DATE                      DATETIME             NOT NULL DEFAULT '0000-00-00 00:00:00',
    END_DATE                        DATETIME             DEFAULT NULL,
    UNIQUE KEY pk_asb_agent_relation (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE)
) TYPE=MyISAM 
;


## 
## TABLE: asb_alploc 
##

CREATE TABLE asb_alploc(
    ASSEMBLY_ID               VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ALPLOC_CODE               VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    LOCATION_NAME             VARCHAR(50)    BINARY DEFAULT NULL,
    LATITUDE                  DECIMAL(68,30) DEFAULT NULL,
    LONGITUDE                 DECIMAL(68,30) DEFAULT NULL,
    INSTALLATION_TYPE_CODE    CHAR(3)       BINARY DEFAULT NULL,
    UNIQUE KEY pk_asb_alploc (ASSEMBLY_ID, ALPLOC_CODE)
) TYPE=MyISAM 
;


## 
## TABLE: asb_assembly 
##

CREATE TABLE asb_assembly(
    ASSEMBLY_ID      VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ASSEMBLY_TYPE    VARCHAR(50)    BINARY DEFAULT NULL,
    DESCRIPTION      VARCHAR(200)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_asb_assembly (ASSEMBLY_ID)
) TYPE=MyISAM 
;


## 
## TABLE: asb_component_arg 
##

CREATE TABLE asb_component_arg(
    ASSEMBLY_ID          VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    COMPONENT_ALIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    ARGUMENT             VARCHAR(230)    BINARY NOT NULL DEFAULT '',
    ARGUMENT_ORDER       DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    UNIQUE KEY pk_asb_component_arg (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER)
) TYPE=MyISAM 
;


## 
## TABLE: asb_component_hierarchy 
##

CREATE TABLE asb_component_hierarchy(
    ASSEMBLY_ID                 VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    COMPONENT_ALIB_ID           VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    PARENT_COMPONENT_ALIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    PRIORITY                    VARCHAR(20)    BINARY DEFAULT NULL,
    INSERTION_ORDER             DECIMAL(68,30) DEFAULT NULL,
    UNIQUE KEY pk_asb_component_hierarchy (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID)
) TYPE=MyISAM 
;


## 
## TABLE: asb_oplan 
##

CREATE TABLE asb_oplan(
    ASSEMBLY_ID       VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    OPLAN_ID          VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    OPERATION_NAME    VARCHAR(50)    BINARY DEFAULT NULL,
    PRIORITY          VARCHAR(50)    BINARY DEFAULT NULL,
    C0_DATE           DATETIME             DEFAULT NULL,
    UNIQUE KEY pk_asb_oplan (ASSEMBLY_ID, OPLAN_ID)
) TYPE=MyISAM 
;


## 
## TABLE: asb_oplan_agent_attr 
##

CREATE TABLE asb_oplan_agent_attr(
    ASSEMBLY_ID          VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    OPLAN_ID             VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    COMPONENT_ALIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    COMPONENT_ID         VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    START_CDAY           DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    ATTRIBUTE_NAME       VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    END_CDAY             DECIMAL(68,30) DEFAULT NULL,
    ATTRIBUTE_VALUE      VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_asb_oplan_agent_attr (ASSEMBLY_ID, OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_alploc 
##

CREATE TABLE cfw_alploc(
    CFW_ID                    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ALPLOC_CODE               VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    LOCATION_NAME             VARCHAR(50)    BINARY DEFAULT NULL,
    LATITUDE                  DECIMAL(68,30) DEFAULT NULL,
    LONGITUDE                 DECIMAL(68,30) DEFAULT NULL,
    INSTALLATION_TYPE_CODE    CHAR(3)       BINARY DEFAULT NULL,
    UNIQUE KEY pk_cfw_alploc (CFW_ID, ALPLOC_CODE)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_context_plugin_arg 
##

CREATE TABLE cfw_context_plugin_arg(
    CFW_ID           VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ORG_CONTEXT      VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    PLUGIN_ARG_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_context_plugin_arg (CFW_ID, ORG_CONTEXT, PLUGIN_ARG_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_group 
##

CREATE TABLE cfw_group(
    CFW_GROUP_ID    VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION     VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_cfw_group (CFW_GROUP_ID)
) TYPE=MyISAM 
;


## COMMENT ON TABLE cfw_group IS 'CFW_GROUP_ID defines a "Society Template"'

## 
## TABLE: cfw_group_member 
##

CREATE TABLE cfw_group_member(
    CFW_ID          VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    CFW_GROUP_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_group_member (CFW_ID, CFW_GROUP_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_group_org 
##

CREATE TABLE cfw_group_org(
    CFW_GROUP_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_ID          VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_group_org (CFW_GROUP_ID, ORG_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_instance 
##

CREATE TABLE cfw_instance(
    CFW_ID         VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(200)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_cfw_instance (CFW_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_oplan 
##

CREATE TABLE cfw_oplan(
    CFW_ID            VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    OPLAN_ID          VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    OPERATION_NAME    VARCHAR(50)    BINARY DEFAULT NULL,
    PRIORITY          VARCHAR(50)    BINARY DEFAULT NULL,
    C0_DATE           DATETIME             DEFAULT NULL,
    UNIQUE KEY pk_cfw_oplan (CFW_ID, OPLAN_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_oplan_activity 
##

CREATE TABLE cfw_oplan_activity(
    CFW_ID           VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    OPLAN_ID         VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_GROUP_ID     VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    START_CDAY       DECIMAL(68,30)         NOT NULL DEFAULT '0.000000000000000000000000000000',
    END_CDAY         DECIMAL(68,30) DEFAULT NULL,
    OPTEMPO          VARCHAR(50)    BINARY DEFAULT NULL,
    ACTIVITY_TYPE    VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_cfw_oplan_activity (CFW_ID, OPLAN_ID, ORG_GROUP_ID, START_CDAY)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_oplan_loc 
##

CREATE TABLE cfw_oplan_loc(
    CFW_ID           VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    OPLAN_ID         VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_GROUP_ID     VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    START_CDAY       DECIMAL(68,30)         NOT NULL DEFAULT '0.000000000000000000000000000000',
    END_CDAY         DECIMAL(68,30) DEFAULT NULL,
    LOCATION_CODE    VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_cfw_oplan_loc (CFW_ID, OPLAN_ID, ORG_GROUP_ID, START_CDAY)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_oplan_og_attr 
##

CREATE TABLE cfw_oplan_og_attr(
    CFW_ID             VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    OPLAN_ID           VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_GROUP_ID       VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    START_CDAY         DECIMAL(68,30)         NOT NULL DEFAULT '0.000000000000000000000000000000',
    ATTRIBUTE_NAME     VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    END_CDAY           DECIMAL(68,30) DEFAULT NULL,
    ATTRIBUTE_VALUE    VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_cfw_oplan_og_attr (CFW_ID, OPLAN_ID, ORG_GROUP_ID, START_CDAY, ATTRIBUTE_NAME)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_org_group_org_member 
##

CREATE TABLE cfw_org_group_org_member(
    CFW_ID          VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_GROUP_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_ID          VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_org_group_org_member (CFW_ID, ORG_GROUP_ID, ORG_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_org_hierarchy 
##

CREATE TABLE cfw_org_hierarchy(
    CFW_ID             VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_ID             VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    SUPERIOR_ORG_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_org_hierarchy (CFW_ID, ORG_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_org_list 
##

CREATE TABLE cfw_org_list(
    CFW_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_org_list (CFW_ID, ORG_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_org_og_relation 
##

CREATE TABLE cfw_org_og_relation(
    CFW_ID            VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ROLE              VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_ID            VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_GROUP_ID      VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    START_DATE        DATETIME            NOT NULL DEFAULT '0000-00-00 00:00:00',
    END_DATE          DATETIME             DEFAULT NULL,
    RELATION_ORDER    DECIMAL(68,30)         NOT NULL DEFAULT '0.000000000000000000000000000000',
    UNIQUE KEY pk_cfw_org_og_relation (CFW_ID, ROLE, ORG_ID, ORG_GROUP_ID, START_DATE)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_org_orgtype 
##

CREATE TABLE cfw_org_orgtype(
    CFW_ID        VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_ID        VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORGTYPE_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_org_orgtype (CFW_ID, ORG_ID, ORGTYPE_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_org_pg_attr 
##

CREATE TABLE cfw_org_pg_attr(
    CFW_ID                 VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ORG_ID                 VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    PG_ATTRIBUTE_LIB_ID    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_VALUE        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_ORDER        DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    START_DATE             DATETIME             NOT NULL DEFAULT '0000-00-00 00:00:00',
    END_DATE               DATETIME             DEFAULT NULL,
    UNIQUE KEY pk_cfw_org_pg_attr (CFW_ID, ORG_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_orgtype_plugin_grp 
##

CREATE TABLE cfw_orgtype_plugin_grp(
    CFW_ID             VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORGTYPE_ID         VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    PLUGIN_GROUP_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_orgtype_plugin_grp (CFW_ID, ORGTYPE_ID, PLUGIN_GROUP_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_plugin_group_member 
##

CREATE TABLE cfw_plugin_group_member(
    CFW_ID                VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    PLUGIN_GROUP_ID       VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    PLUGIN_ID             VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    PLUGIN_CLASS_ORDER    DECIMAL(68,30) DEFAULT NULL,
    UNIQUE KEY pk_cfw_plugin_group_member (CFW_ID, PLUGIN_GROUP_ID, PLUGIN_ID)
) TYPE=MyISAM 
;


## 
## TABLE: community_attribute 
##

CREATE TABLE community_attribute(
    ASSEMBLY_ID        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    COMMUNITY_ID       VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_ID       VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_VALUE    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_community_attribute (ASSEMBLY_ID, COMMUNITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE)
) TYPE=MyISAM 
;


## 
## TABLE: community_entity_attribute 
##

CREATE TABLE community_entity_attribute(
    ASSEMBLY_ID        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    COMMUNITY_ID       VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ENTITY_ID          VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_ID       VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_VALUE    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_community_entity_attribute (ASSEMBLY_ID, COMMUNITY_ID, ENTITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE)
) TYPE=MyISAM 
;


## 
## TABLE: expt_experiment 
##

CREATE TABLE expt_experiment(
    EXPT_ID         VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION     VARCHAR(200)    BINARY DEFAULT NULL,
    NAME            VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    CFW_GROUP_ID    VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_expt_experiment (EXPT_ID)
) TYPE=MyISAM 
;


## 
## TABLE: expt_trial 
##

CREATE TABLE expt_trial(
    TRIAL_ID       VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    EXPT_ID        VARCHAR(50)    BINARY DEFAULT NULL,
    DESCRIPTION    VARCHAR(100)    BINARY DEFAULT NULL,
    NAME           VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_expt_trial (TRIAL_ID)
) TYPE=MyISAM 
;


## 
## TABLE: expt_trial_assembly 
##

CREATE TABLE expt_trial_assembly(
    TRIAL_ID       VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ASSEMBLY_ID    VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    EXPT_ID        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(200)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_expt_trial_assembly (TRIAL_ID, ASSEMBLY_ID)
) TYPE=MyISAM 
;


## 
## TABLE: expt_trial_config_assembly 
##

CREATE TABLE expt_trial_config_assembly(
    TRIAL_ID       VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ASSEMBLY_ID    VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    EXPT_ID        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(200)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_expt_trial_config_assembly (TRIAL_ID, ASSEMBLY_ID)
) TYPE=MyISAM 
;


## 
## TABLE: expt_trial_mod_recipe 
##

CREATE TABLE expt_trial_mod_recipe(
    TRIAL_ID             VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    MOD_RECIPE_LIB_ID    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    RECIPE_ORDER         DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    EXPT_ID              VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_expt_trial_mod_recipe (TRIAL_ID, MOD_RECIPE_LIB_ID, RECIPE_ORDER)
) TYPE=MyISAM 
;


## 
## TABLE: expt_trial_org_mult 
##

CREATE TABLE expt_trial_org_mult(
    TRIAL_ID        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    CFW_ID          VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ORG_GROUP_ID    VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    EXPT_ID         VARCHAR(50)    BINARY DEFAULT NULL,
    MULTIPLIER      DECIMAL(68,30) DEFAULT NULL,
    DESCRIPTION     VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_expt_trial_org_mult (TRIAL_ID, CFW_ID, ORG_GROUP_ID)
) TYPE=MyISAM 
;


## 
## TABLE: expt_trial_thread 
##

CREATE TABLE expt_trial_thread(
    TRIAL_ID     VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    THREAD_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    EXPT_ID      VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_expt_trial_thread (TRIAL_ID, THREAD_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_activity_type_ref 
##

CREATE TABLE lib_activity_type_ref(
    ACTIVITY_TYPE    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    DESCRIPTION      VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_activity_type_ref (ACTIVITY_TYPE)
) TYPE=MyISAM 
;


## 
## TABLE: lib_agent_org 
##

CREATE TABLE lib_agent_org(
    COMPONENT_LIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    AGENT_LIB_NAME      VARCHAR(50)    BINARY DEFAULT NULL,
    AGENT_ORG_CLASS     VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_agent_org (COMPONENT_LIB_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_clone_set 
##

CREATE TABLE lib_clone_set(
    CLONE_SET_ID    DECIMAL(68,30)   NOT NULL DEFAULT '0.000000000000000000000000000000',
    UNIQUE KEY pk_lib_clone_set (CLONE_SET_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_component 
##

CREATE TABLE lib_component(
    COMPONENT_LIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    COMPONENT_TYPE      VARCHAR(50)    BINARY DEFAULT NULL,
    COMPONENT_CLASS     VARCHAR(100)    BINARY DEFAULT NULL,
    INSERTION_POINT     VARCHAR(50)    BINARY DEFAULT NULL,
    DESCRIPTION         VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_component (COMPONENT_LIB_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_component_arg 
##

CREATE TABLE lib_component_arg(
    COMPONENT_LIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    ARGUMENT            VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_lib_component_arg (COMPONENT_LIB_ID, ARGUMENT)
) TYPE=MyISAM 
;


## 
## TABLE: lib_mod_recipe 
##

CREATE TABLE lib_mod_recipe(
    MOD_RECIPE_LIB_ID    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    NAME                 VARCHAR(50)    BINARY DEFAULT NULL,
    JAVA_CLASS           VARCHAR(100)    BINARY DEFAULT NULL,
    DESCRIPTION          VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_mod_recipe (MOD_RECIPE_LIB_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_mod_recipe_arg 
##

CREATE TABLE lib_mod_recipe_arg(
    MOD_RECIPE_LIB_ID    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ARG_NAME             VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ARG_ORDER            DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    ARG_VALUE            VARCHAR(255)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_mod_recipe_arg (MOD_RECIPE_LIB_ID, ARG_NAME, ARG_ORDER)
) TYPE=MyISAM 
;


## 
## TABLE: lib_org_group 
##

CREATE TABLE lib_org_group(
    ORG_GROUP_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    DESCRIPTION     VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_org_group (ORG_GROUP_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_organization 
##

CREATE TABLE lib_organization(
    ORG_ID      VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_NAME    VARCHAR(50)    BINARY DEFAULT NULL,
    UIC         VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_organization (ORG_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_orgtype_ref 
##

CREATE TABLE lib_orgtype_ref(
    ORGTYPE_ID     VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_orgtype_ref (ORGTYPE_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_pg_attribute 
##

CREATE TABLE lib_pg_attribute(
    PG_ATTRIBUTE_LIB_ID    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    PG_NAME                VARCHAR(50)    BINARY DEFAULT NULL,
    ATTRIBUTE_NAME         VARCHAR(50)    BINARY DEFAULT NULL,
    ATTRIBUTE_TYPE         VARCHAR(50)    BINARY DEFAULT NULL,
    AGGREGATE_TYPE         VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_pg_attribute (PG_ATTRIBUTE_LIB_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_plugin_arg 
##

CREATE TABLE lib_plugin_arg(
    PLUGIN_ARG_ID     VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    ARGUMENT_ORDER    DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    PLUGIN_ID         VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ARGUMENT          VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ARGUMENT_TYPE     VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_plugin_arg (PLUGIN_ARG_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_plugin_arg_thread 
##

CREATE TABLE lib_plugin_arg_thread(
    PLUGIN_ARG_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    THREAD_ID        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_lib_plugin_arg_thread (PLUGIN_ARG_ID, THREAD_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_plugin_group 
##

CREATE TABLE lib_plugin_group(
    PLUGIN_GROUP_ID       VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    PLUGIN_GROUP_ORDER    DECIMAL(68,30) DEFAULT NULL,
    DESCRIPTION           VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_plugin_group (PLUGIN_GROUP_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_plugin_ref 
##

CREATE TABLE lib_plugin_ref(
    PLUGIN_ID       VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    PLUGIN_CLASS    VARCHAR(100)    BINARY DEFAULT NULL,
    DESCRIPTION     VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_plugin_ref (PLUGIN_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_plugin_thread 
##

CREATE TABLE lib_plugin_thread(
    PLUGIN_ID      VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    THREAD_ID      VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_plugin_thread (PLUGIN_ID, THREAD_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_role_ref 
##

CREATE TABLE lib_role_ref(
    ROLE           VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_role_ref (ROLE)
) TYPE=MyISAM 
;


## 
## TABLE: lib_role_thread 
##

CREATE TABLE lib_role_thread(
    ROLE           VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    THREAD_ID      VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_role_thread (ROLE, THREAD_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_thread 
##

CREATE TABLE lib_thread(
    THREAD_ID      VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_thread (THREAD_ID)
) TYPE=MyISAM 
;


## 
