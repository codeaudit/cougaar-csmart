--
--ER/Studio 5.1 SQL Code Generation
-- Company :      BBNT
-- Project :      CSMART Database
-- Author :       M. Kappler & J. Berliner
--
-- Date Created : Tuesday, July 09, 2002 16:57:59
-- Target DBMS : Oracle 8
--


-- 
-- TABLE: alib_component 
--

CREATE TABLE alib_component(
    COMPONENT_ALIB_ID    VARCHAR2(150)    NOT NULL,
    COMPONENT_NAME       VARCHAR2(150),
    COMPONENT_LIB_ID     VARCHAR2(150)    NOT NULL,
    COMPONENT_TYPE       VARCHAR2(50),
    CLONE_SET_ID         NUMBER           NOT NULL,
    CONSTRAINT pk_alib_component PRIMARY KEY (COMPONENT_ALIB_ID)
) 
;


-- 
-- TABLE: asb_agent 
--

CREATE TABLE asb_agent(
    ASSEMBLY_ID          VARCHAR2(50)     NOT NULL,
    COMPONENT_ALIB_ID    VARCHAR2(150)    NOT NULL,
    COMPONENT_LIB_ID     VARCHAR2(150),
    CLONE_SET_ID         NUMBER,
    COMPONENT_NAME       VARCHAR2(150),
    CONSTRAINT pk_asb_agent PRIMARY KEY (ASSEMBLY_ID, COMPONENT_ALIB_ID)
) 
;


-- 
-- TABLE: asb_agent_pg_attr 
--

CREATE TABLE asb_agent_pg_attr(
    ASSEMBLY_ID            VARCHAR2(50)     NOT NULL,
    COMPONENT_ALIB_ID      VARCHAR2(150)    NOT NULL,
    PG_ATTRIBUTE_LIB_ID    VARCHAR2(100)    NOT NULL,
    ATTRIBUTE_VALUE        VARCHAR2(50)     NOT NULL,
    ATTRIBUTE_ORDER        NUMBER           NOT NULL,
    START_DATE             DATE             NOT NULL,
    END_DATE               DATE,
    CONSTRAINT pk_asb_agent_pg_attr PRIMARY KEY (ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE)
) 
;


-- 
-- TABLE: asb_agent_relation 
--

CREATE TABLE asb_agent_relation(
    ASSEMBLY_ID                     VARCHAR2(50)     NOT NULL,
    ROLE                            VARCHAR2(50)     NOT NULL,
    SUPPORTING_COMPONENT_ALIB_ID    VARCHAR2(150)    NOT NULL,
    SUPPORTED_COMPONENT_ALIB_ID     VARCHAR2(150)    NOT NULL,
    START_DATE                      DATE             NOT NULL,
    END_DATE                        DATE,
    CONSTRAINT pk_asb_agent_relation PRIMARY KEY (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE)
) 
;


-- 
-- TABLE: asb_alploc 
--

CREATE TABLE asb_alploc(
    ASSEMBLY_ID               VARCHAR2(50)    NOT NULL,
    ALPLOC_CODE               VARCHAR2(50)    NOT NULL,
    LOCATION_NAME             VARCHAR2(50),
    LATITUDE                  NUMBER,
    LONGITUDE                 NUMBER,
    INSTALLATION_TYPE_CODE    CHAR(3),
    CONSTRAINT pk_asb_alploc PRIMARY KEY (ASSEMBLY_ID, ALPLOC_CODE)
) 
;


-- 
-- TABLE: asb_assembly 
--

CREATE TABLE asb_assembly(
    ASSEMBLY_ID      VARCHAR2(50)     NOT NULL,
    ASSEMBLY_TYPE    VARCHAR2(50),
    DESCRIPTION      VARCHAR2(200),
    CONSTRAINT pk_asb_assembly PRIMARY KEY (ASSEMBLY_ID)
) 
;


-- 
-- TABLE: asb_component_arg 
--

CREATE TABLE asb_component_arg(
    ASSEMBLY_ID          VARCHAR2(50)     NOT NULL,
    COMPONENT_ALIB_ID    VARCHAR2(150)    NOT NULL,
    ARGUMENT             VARCHAR2(230)    NOT NULL,
    ARGUMENT_ORDER       NUMBER           NOT NULL,
    CONSTRAINT pk_asb_component_arg PRIMARY KEY (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER)
) 
;


-- 
-- TABLE: asb_component_hierarchy 
--

CREATE TABLE asb_component_hierarchy(
    ASSEMBLY_ID                 VARCHAR2(50)     NOT NULL,
    COMPONENT_ALIB_ID           VARCHAR2(150)    NOT NULL,
    PARENT_COMPONENT_ALIB_ID    VARCHAR2(150)    NOT NULL,
    PRIORITY                    VARCHAR2(20),
    INSERTION_ORDER             NUMBER,
    CONSTRAINT pk_asb_component_hierarchy PRIMARY KEY (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID)
) 
;


-- 
-- TABLE: asb_oplan 
--

CREATE TABLE asb_oplan(
    ASSEMBLY_ID       VARCHAR2(50)    NOT NULL,
    OPLAN_ID          VARCHAR2(50)    NOT NULL,
    OPERATION_NAME    VARCHAR2(50),
    PRIORITY          VARCHAR2(50),
    C0_DATE           DATE,
    CONSTRAINT pk_asb_oplan PRIMARY KEY (ASSEMBLY_ID, OPLAN_ID)
) 
;


-- 
-- TABLE: asb_oplan_agent_attr 
--

CREATE TABLE asb_oplan_agent_attr(
    ASSEMBLY_ID          VARCHAR2(50)     NOT NULL,
    OPLAN_ID             VARCHAR2(50)     NOT NULL,
    COMPONENT_ALIB_ID    VARCHAR2(150)    NOT NULL,
    COMPONENT_ID         VARCHAR2(50)     NOT NULL,
    START_CDAY           NUMBER           NOT NULL,
    ATTRIBUTE_NAME       VARCHAR2(50)     NOT NULL,
    END_CDAY             NUMBER,
    ATTRIBUTE_VALUE      VARCHAR2(50),
    CONSTRAINT pk_asb_oplan_agent_attr PRIMARY KEY (ASSEMBLY_ID, OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME)
) 
;


-- 
-- TABLE: cfw_alploc 
--

CREATE TABLE cfw_alploc(
    CFW_ID                    VARCHAR2(50)    NOT NULL,
    ALPLOC_CODE               VARCHAR2(50)    NOT NULL,
    LOCATION_NAME             VARCHAR2(50),
    LATITUDE                  NUMBER,
    LONGITUDE                 NUMBER,
    INSTALLATION_TYPE_CODE    CHAR(3),
    CONSTRAINT pk_cfw_alploc PRIMARY KEY (CFW_ID, ALPLOC_CODE)
) 
;


-- 
-- TABLE: cfw_context_plugin_arg 
--

CREATE TABLE cfw_context_plugin_arg(
    CFW_ID           VARCHAR2(50)     NOT NULL,
    ORG_CONTEXT      VARCHAR2(50)     NOT NULL,
    PLUGIN_ARG_ID    VARCHAR2(150)    NOT NULL,
    CONSTRAINT pk_cfw_context_plugin_arg PRIMARY KEY (CFW_ID, ORG_CONTEXT, PLUGIN_ARG_ID)
) 
;


-- 
-- TABLE: cfw_group 
--

CREATE TABLE cfw_group(
    CFW_GROUP_ID    VARCHAR2(50)     NOT NULL,
    DESCRIPTION     VARCHAR2(100),
    CONSTRAINT pk_cfw_group PRIMARY KEY (CFW_GROUP_ID)
) 
;


COMMENT ON TABLE cfw_group IS 'CFW_GROUP_ID defines a "Society Template"'
;
-- 
-- TABLE: cfw_group_member 
--

CREATE TABLE cfw_group_member(
    CFW_ID          VARCHAR2(50)    NOT NULL,
    CFW_GROUP_ID    VARCHAR2(50)    NOT NULL,
    CONSTRAINT pk_cfw_group_member PRIMARY KEY (CFW_ID, CFW_GROUP_ID)
) 
;


-- 
-- TABLE: cfw_group_org 
--

CREATE TABLE cfw_group_org(
    CFW_GROUP_ID    VARCHAR2(50)    NOT NULL,
    ORG_ID          VARCHAR2(50)    NOT NULL,
    CONSTRAINT pk_cfw_group_org PRIMARY KEY (CFW_GROUP_ID, ORG_ID)
) 
;


-- 
-- TABLE: cfw_instance 
--

CREATE TABLE cfw_instance(
    CFW_ID         VARCHAR2(50)     NOT NULL,
    DESCRIPTION    VARCHAR2(200),
    CONSTRAINT pk_cfw_instance PRIMARY KEY (CFW_ID)
) 
;


-- 
-- TABLE: cfw_oplan 
--

CREATE TABLE cfw_oplan(
    CFW_ID            VARCHAR2(50)    NOT NULL,
    OPLAN_ID          VARCHAR2(50)    NOT NULL,
    OPERATION_NAME    VARCHAR2(50),
    PRIORITY          VARCHAR2(50),
    C0_DATE           DATE,
    CONSTRAINT pk_cfw_oplan PRIMARY KEY (CFW_ID, OPLAN_ID)
) 
;


-- 
-- TABLE: cfw_oplan_activity 
--

CREATE TABLE cfw_oplan_activity(
    CFW_ID           VARCHAR2(50)    NOT NULL,
    OPLAN_ID         VARCHAR2(50)    NOT NULL,
    ORG_GROUP_ID     VARCHAR2(50)    NOT NULL,
    START_CDAY       NUMBER          NOT NULL,
    END_CDAY         NUMBER,
    OPTEMPO          VARCHAR2(50),
    ACTIVITY_TYPE    VARCHAR2(50),
    CONSTRAINT pk_cfw_oplan_activity PRIMARY KEY (CFW_ID, OPLAN_ID, ORG_GROUP_ID, START_CDAY)
) 
;


-- 
-- TABLE: cfw_oplan_loc 
--

CREATE TABLE cfw_oplan_loc(
    CFW_ID           VARCHAR2(50)    NOT NULL,
    OPLAN_ID         VARCHAR2(50)    NOT NULL,
    ORG_GROUP_ID     VARCHAR2(50)    NOT NULL,
    START_CDAY       NUMBER          NOT NULL,
    END_CDAY         NUMBER,
    LOCATION_CODE    VARCHAR2(50),
    CONSTRAINT pk_cfw_oplan_loc PRIMARY KEY (CFW_ID, OPLAN_ID, ORG_GROUP_ID, START_CDAY)
) 
;


-- 
-- TABLE: cfw_oplan_og_attr 
--

CREATE TABLE cfw_oplan_og_attr(
    CFW_ID             VARCHAR2(50)    NOT NULL,
    OPLAN_ID           VARCHAR2(50)    NOT NULL,
    ORG_GROUP_ID       VARCHAR2(50)    NOT NULL,
    START_CDAY         NUMBER          NOT NULL,
    ATTRIBUTE_NAME     VARCHAR2(50)    NOT NULL,
    END_CDAY           NUMBER,
    ATTRIBUTE_VALUE    VARCHAR2(50),
    CONSTRAINT pk_cfw_oplan_og_attr PRIMARY KEY (CFW_ID, OPLAN_ID, ORG_GROUP_ID, START_CDAY, ATTRIBUTE_NAME)
) 
;


-- 
-- TABLE: cfw_org_group_org_member 
--

CREATE TABLE cfw_org_group_org_member(
    CFW_ID          VARCHAR2(50)    NOT NULL,
    ORG_GROUP_ID    VARCHAR2(50)    NOT NULL,
    ORG_ID          VARCHAR2(50)    NOT NULL,
    CONSTRAINT pk_cfw_org_group_org_member PRIMARY KEY (CFW_ID, ORG_GROUP_ID, ORG_ID)
) 
;


-- 
-- TABLE: cfw_org_hierarchy 
--

CREATE TABLE cfw_org_hierarchy(
    CFW_ID             VARCHAR2(50)    NOT NULL,
    ORG_ID             VARCHAR2(50)    NOT NULL,
    SUPERIOR_ORG_ID    VARCHAR2(50)    NOT NULL,
    CONSTRAINT pk_cfw_org_hierarchy PRIMARY KEY (CFW_ID, ORG_ID)
) 
;


-- 
-- TABLE: cfw_org_list 
--

CREATE TABLE cfw_org_list(
    CFW_ID    VARCHAR2(50)    NOT NULL,
    ORG_ID    VARCHAR2(50)    NOT NULL,
    CONSTRAINT pk_cfw_org_list PRIMARY KEY (CFW_ID, ORG_ID)
) 
;


-- 
-- TABLE: cfw_org_og_relation 
--

CREATE TABLE cfw_org_og_relation(
    CFW_ID            VARCHAR2(50)    NOT NULL,
    ROLE              VARCHAR2(50)    NOT NULL,
    ORG_ID            VARCHAR2(50)    NOT NULL,
    ORG_GROUP_ID      VARCHAR2(50)    NOT NULL,
    START_DATE        DATE            NOT NULL,
    END_DATE          DATE,
    RELATION_ORDER    NUMBER          NOT NULL,
    CONSTRAINT pk_cfw_org_og_relation PRIMARY KEY (CFW_ID, ROLE, ORG_ID, ORG_GROUP_ID, START_DATE)
) 
;


-- 
-- TABLE: cfw_org_orgtype 
--

CREATE TABLE cfw_org_orgtype(
    CFW_ID        VARCHAR2(50)    NOT NULL,
    ORG_ID        VARCHAR2(50)    NOT NULL,
    ORGTYPE_ID    VARCHAR2(50)    NOT NULL,
    CONSTRAINT pk_cfw_org_orgtype PRIMARY KEY (CFW_ID, ORG_ID, ORGTYPE_ID)
) 
;


-- 
-- TABLE: cfw_org_pg_attr 
--

CREATE TABLE cfw_org_pg_attr(
    CFW_ID                 VARCHAR2(50)     NOT NULL,
    ORG_ID                 VARCHAR2(50)     NOT NULL,
    PG_ATTRIBUTE_LIB_ID    VARCHAR2(100)    NOT NULL,
    ATTRIBUTE_VALUE        VARCHAR2(50)     NOT NULL,
    ATTRIBUTE_ORDER        NUMBER           NOT NULL,
    START_DATE             DATE             NOT NULL,
    END_DATE               DATE,
    CONSTRAINT pk_cfw_org_pg_attr PRIMARY KEY (CFW_ID, ORG_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE)
) 
;


-- 
-- TABLE: cfw_orgtype_plugin_grp 
--

CREATE TABLE cfw_orgtype_plugin_grp(
    CFW_ID             VARCHAR2(50)    NOT NULL,
    ORGTYPE_ID         VARCHAR2(50)    NOT NULL,
    PLUGIN_GROUP_ID    VARCHAR2(50)    NOT NULL,
    CONSTRAINT pk_cfw_orgtype_plugin_grp PRIMARY KEY (CFW_ID, ORGTYPE_ID, PLUGIN_GROUP_ID)
) 
;


-- 
-- TABLE: cfw_plugin_group_member 
--

CREATE TABLE cfw_plugin_group_member(
    CFW_ID                VARCHAR2(50)     NOT NULL,
    PLUGIN_GROUP_ID       VARCHAR2(50)     NOT NULL,
    PLUGIN_ID             VARCHAR2(100)    NOT NULL,
    PLUGIN_CLASS_ORDER    NUMBER,
    CONSTRAINT pk_cfw_plugin_group_member PRIMARY KEY (CFW_ID, PLUGIN_GROUP_ID, PLUGIN_ID)
) 
;


-- 
-- TABLE: community_attribute 
--

CREATE TABLE community_attribute(
    ASSEMBLY_ID        VARCHAR2(50)     NOT NULL,
    COMMUNITY_ID       VARCHAR2(100)    NOT NULL,
    ATTRIBUTE_ID       VARCHAR2(100)    NOT NULL,
    ATTRIBUTE_VALUE    VARCHAR2(100)    NOT NULL,
    CONSTRAINT pk_community_attribute PRIMARY KEY (ASSEMBLY_ID, COMMUNITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE)
) 
;


-- 
-- TABLE: community_entity_attribute 
--

CREATE TABLE community_entity_attribute(
    ASSEMBLY_ID        VARCHAR2(50)     NOT NULL,
    COMMUNITY_ID       VARCHAR2(100)    NOT NULL,
    ENTITY_ID          VARCHAR2(100)    NOT NULL,
    ATTRIBUTE_ID       VARCHAR2(100)    NOT NULL,
    ATTRIBUTE_VALUE    VARCHAR2(100)    NOT NULL,
    CONSTRAINT pk_community_entity_attribute PRIMARY KEY (ASSEMBLY_ID, COMMUNITY_ID, ENTITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE)
) 
;


-- 
-- TABLE: expt_experiment 
--

CREATE TABLE expt_experiment(
    EXPT_ID         VARCHAR2(50)     NOT NULL,
    DESCRIPTION     VARCHAR2(200),
    NAME            VARCHAR2(50)     NOT NULL,
    CFW_GROUP_ID    VARCHAR2(50),
    CONSTRAINT pk_expt_experiment PRIMARY KEY (EXPT_ID)
) 
;


-- 
-- TABLE: expt_trial 
--

CREATE TABLE expt_trial(
    TRIAL_ID       VARCHAR2(50)     NOT NULL,
    EXPT_ID        VARCHAR2(50),
    DESCRIPTION    VARCHAR2(100),
    NAME           VARCHAR2(50),
    CONSTRAINT pk_expt_trial PRIMARY KEY (TRIAL_ID)
) 
;


-- 
-- TABLE: expt_trial_assembly 
--

CREATE TABLE expt_trial_assembly(
    TRIAL_ID       VARCHAR2(50)     NOT NULL,
    ASSEMBLY_ID    VARCHAR2(50)     NOT NULL,
    EXPT_ID        VARCHAR2(50)     NOT NULL,
    DESCRIPTION    VARCHAR2(200),
    CONSTRAINT pk_expt_trial_assembly PRIMARY KEY (TRIAL_ID, ASSEMBLY_ID)
) 
;


-- 
-- TABLE: expt_trial_config_assembly 
--

CREATE TABLE expt_trial_config_assembly(
    TRIAL_ID       VARCHAR2(50)     NOT NULL,
    ASSEMBLY_ID    VARCHAR2(50)     NOT NULL,
    EXPT_ID        VARCHAR2(50)     NOT NULL,
    DESCRIPTION    VARCHAR2(200),
    CONSTRAINT pk_expt_trial_config_assembly PRIMARY KEY (TRIAL_ID, ASSEMBLY_ID)
) 
;


-- 
-- TABLE: expt_trial_mod_recipe 
--

CREATE TABLE expt_trial_mod_recipe(
    TRIAL_ID             VARCHAR2(50)     NOT NULL,
    MOD_RECIPE_LIB_ID    VARCHAR2(100)    NOT NULL,
    RECIPE_ORDER         NUMBER           NOT NULL,
    EXPT_ID              VARCHAR2(50),
    CONSTRAINT pk_expt_trial_mod_recipe PRIMARY KEY (TRIAL_ID, MOD_RECIPE_LIB_ID, RECIPE_ORDER)
) 
;


-- 
-- TABLE: expt_trial_org_mult 
--

CREATE TABLE expt_trial_org_mult(
    TRIAL_ID        VARCHAR2(50)     NOT NULL,
    CFW_ID          VARCHAR2(50)     NOT NULL,
    ORG_GROUP_ID    VARCHAR2(50)     NOT NULL,
    EXPT_ID         VARCHAR2(50),
    MULTIPLIER      NUMBER,
    DESCRIPTION     VARCHAR2(100),
    CONSTRAINT pk_expt_trial_org_mult PRIMARY KEY (TRIAL_ID, CFW_ID, ORG_GROUP_ID)
) 
;


-- 
-- TABLE: expt_trial_thread 
--

CREATE TABLE expt_trial_thread(
    TRIAL_ID     VARCHAR2(50)    NOT NULL,
    THREAD_ID    VARCHAR2(50)    NOT NULL,
    EXPT_ID      VARCHAR2(50),
    CONSTRAINT pk_expt_trial_thread PRIMARY KEY (TRIAL_ID, THREAD_ID)
) 
;


-- 
-- TABLE: lib_activity_type_ref 
--

CREATE TABLE lib_activity_type_ref(
    ACTIVITY_TYPE    VARCHAR2(50)    NOT NULL,
    DESCRIPTION      VARCHAR2(50),
    CONSTRAINT pk_lib_activity_type_ref PRIMARY KEY (ACTIVITY_TYPE)
) 
;


-- 
-- TABLE: lib_agent_org 
--

CREATE TABLE lib_agent_org(
    COMPONENT_LIB_ID    VARCHAR2(150)    NOT NULL,
    AGENT_LIB_NAME      VARCHAR2(50),
    AGENT_ORG_CLASS     VARCHAR2(50),
    CONSTRAINT pk_lib_agent_org PRIMARY KEY (COMPONENT_LIB_ID)
) 
;


-- 
-- TABLE: lib_clone_set 
--

CREATE TABLE lib_clone_set(
    CLONE_SET_ID    NUMBER    NOT NULL,
    CONSTRAINT pk_lib_clone_set PRIMARY KEY (CLONE_SET_ID)
) 
;


-- 
-- TABLE: lib_component 
--

CREATE TABLE lib_component(
    COMPONENT_LIB_ID    VARCHAR2(150)    NOT NULL,
    COMPONENT_TYPE      VARCHAR2(50),
    COMPONENT_CLASS     VARCHAR2(100),
    INSERTION_POINT     VARCHAR2(50),
    DESCRIPTION         VARCHAR2(100),
    CONSTRAINT pk_lib_component PRIMARY KEY (COMPONENT_LIB_ID)
) 
;


-- 
-- TABLE: lib_component_arg 
--

CREATE TABLE lib_component_arg(
    COMPONENT_LIB_ID    VARCHAR2(150)    NOT NULL,
    ARGUMENT            VARCHAR2(100)    NOT NULL,
    CONSTRAINT pk_lib_component_arg PRIMARY KEY (COMPONENT_LIB_ID, ARGUMENT)
) 
;


-- 
-- TABLE: lib_mod_recipe 
--

CREATE TABLE lib_mod_recipe(
    MOD_RECIPE_LIB_ID    VARCHAR2(100)    NOT NULL,
    NAME                 VARCHAR2(50),
    JAVA_CLASS           VARCHAR2(100),
    DESCRIPTION          VARCHAR2(100),
    CONSTRAINT pk_lib_mod_recipe PRIMARY KEY (MOD_RECIPE_LIB_ID)
) 
;


-- 
-- TABLE: lib_mod_recipe_arg 
--

CREATE TABLE lib_mod_recipe_arg(
    MOD_RECIPE_LIB_ID    VARCHAR2(100)    NOT NULL,
    ARG_NAME             VARCHAR2(100)    NOT NULL,
    ARG_ORDER            NUMBER           NOT NULL,
    ARG_VALUE            VARCHAR2(255),
    CONSTRAINT pk_lib_mod_recipe_arg PRIMARY KEY (MOD_RECIPE_LIB_ID, ARG_NAME, ARG_ORDER)
) 
;


-- 
-- TABLE: lib_org_group 
--

CREATE TABLE lib_org_group(
    ORG_GROUP_ID    VARCHAR2(50)    NOT NULL,
    DESCRIPTION     VARCHAR2(50),
    CONSTRAINT pk_lib_org_group PRIMARY KEY (ORG_GROUP_ID)
) 
;


-- 
-- TABLE: lib_organization 
--

CREATE TABLE lib_organization(
    ORG_ID      VARCHAR2(50)    NOT NULL,
    ORG_NAME    VARCHAR2(50),
    UIC         VARCHAR2(50),
    CONSTRAINT pk_lib_organization PRIMARY KEY (ORG_ID)
) 
;


-- 
-- TABLE: lib_orgtype_ref 
--

CREATE TABLE lib_orgtype_ref(
    ORGTYPE_ID     VARCHAR2(50)     NOT NULL,
    DESCRIPTION    VARCHAR2(100),
    CONSTRAINT pk_lib_orgtype_ref PRIMARY KEY (ORGTYPE_ID)
) 
;


-- 
-- TABLE: lib_pg_attribute 
--

CREATE TABLE lib_pg_attribute(
    PG_ATTRIBUTE_LIB_ID    VARCHAR2(100)    NOT NULL,
    PG_NAME                VARCHAR2(50),
    ATTRIBUTE_NAME         VARCHAR2(50),
    ATTRIBUTE_TYPE         VARCHAR2(50),
    AGGREGATE_TYPE         VARCHAR2(50),
    CONSTRAINT pk_lib_pg_attribute PRIMARY KEY (PG_ATTRIBUTE_LIB_ID)
) 
;


-- 
-- TABLE: lib_plugin_arg 
--

CREATE TABLE lib_plugin_arg(
    PLUGIN_ARG_ID     VARCHAR2(150)    NOT NULL,
    ARGUMENT_ORDER    NUMBER           NOT NULL,
    PLUGIN_ID         VARCHAR2(100)    NOT NULL,
    ARGUMENT          VARCHAR2(100)    NOT NULL,
    ARGUMENT_TYPE     VARCHAR2(50),
    CONSTRAINT pk_lib_plugin_arg PRIMARY KEY (PLUGIN_ARG_ID)
) 
;


-- 
-- TABLE: lib_plugin_arg_thread 
--

CREATE TABLE lib_plugin_arg_thread(
    PLUGIN_ARG_ID    VARCHAR2(150)    NOT NULL,
    THREAD_ID        VARCHAR2(50)     NOT NULL,
    CONSTRAINT pk_lib_plugin_arg_thread PRIMARY KEY (PLUGIN_ARG_ID, THREAD_ID)
) 
;


-- 
-- TABLE: lib_plugin_group 
--

CREATE TABLE lib_plugin_group(
    PLUGIN_GROUP_ID       VARCHAR2(50)     NOT NULL,
    PLUGIN_GROUP_ORDER    NUMBER,
    DESCRIPTION           VARCHAR2(100),
    CONSTRAINT pk_lib_plugin_group PRIMARY KEY (PLUGIN_GROUP_ID)
) 
;


-- 
-- TABLE: lib_plugin_ref 
--

CREATE TABLE lib_plugin_ref(
    PLUGIN_ID       VARCHAR2(100)    NOT NULL,
    PLUGIN_CLASS    VARCHAR2(100),
    DESCRIPTION     VARCHAR2(100),
    CONSTRAINT pk_lib_plugin_ref PRIMARY KEY (PLUGIN_ID)
) 
;


-- 
-- TABLE: lib_plugin_thread 
--

CREATE TABLE lib_plugin_thread(
    PLUGIN_ID      VARCHAR2(100)    NOT NULL,
    THREAD_ID      VARCHAR2(50)     NOT NULL,
    DESCRIPTION    VARCHAR2(100),
    CONSTRAINT pk_lib_plugin_thread PRIMARY KEY (PLUGIN_ID, THREAD_ID)
) 
;


-- 
-- TABLE: lib_role_ref 
--

CREATE TABLE lib_role_ref(
    ROLE           VARCHAR2(50)     NOT NULL,
    DESCRIPTION    VARCHAR2(100),
    CONSTRAINT pk_lib_role_ref PRIMARY KEY (ROLE)
) 
;


-- 
-- TABLE: lib_role_thread 
--

CREATE TABLE lib_role_thread(
    ROLE           VARCHAR2(50)     NOT NULL,
    THREAD_ID      VARCHAR2(50)     NOT NULL,
    DESCRIPTION    VARCHAR2(100),
    CONSTRAINT pk_lib_role_thread PRIMARY KEY (ROLE, THREAD_ID)
) 
;


-- 
-- TABLE: lib_thread 
--

CREATE TABLE lib_thread(
    THREAD_ID      VARCHAR2(50)     NOT NULL,
    DESCRIPTION    VARCHAR2(100),
    CONSTRAINT pk_lib_thread PRIMARY KEY (THREAD_ID)
) 
;


-- 
-- INDEX: reflib_clone_set38 
--

CREATE INDEX reflib_clone_set38 ON alib_component(CLONE_SET_ID)
;
-- 
-- INDEX: reflib_component13 
--

CREATE INDEX reflib_component13 ON alib_component(COMPONENT_LIB_ID)
;
-- 
-- INDEX: refasb_assembly42 
--

CREATE INDEX refasb_assembly42 ON asb_agent(ASSEMBLY_ID)
;
-- 
-- INDEX: refalib_component43 
--

CREATE INDEX refalib_component43 ON asb_agent(COMPONENT_ALIB_ID)
;
-- 
-- INDEX: reflib_component44 
--

CREATE INDEX reflib_component44 ON asb_agent(COMPONENT_LIB_ID)
;
-- 
-- INDEX: refasb_assembly36 
--

CREATE INDEX refasb_assembly36 ON asb_agent_pg_attr(ASSEMBLY_ID)
;
-- 
-- INDEX: refalib_component12 
--

CREATE INDEX refalib_component12 ON asb_agent_pg_attr(COMPONENT_ALIB_ID)
;
-- 
-- INDEX: reflib_pg_attribute17 
--

CREATE INDEX reflib_pg_attribute17 ON asb_agent_pg_attr(PG_ATTRIBUTE_LIB_ID)
;
-- 
-- INDEX: refasb_assembly4 
--

CREATE INDEX refasb_assembly4 ON asb_agent_relation(ASSEMBLY_ID)
;
-- 
-- INDEX: refalib_component31 
--

CREATE INDEX refalib_component31 ON asb_agent_relation(SUPPORTING_COMPONENT_ALIB_ID)
;
-- 
-- INDEX: refalib_component32 
--

CREATE INDEX refalib_component32 ON asb_agent_relation(SUPPORTED_COMPONENT_ALIB_ID)
;
-- 
-- INDEX: refasb_assembly18 
--

CREATE INDEX refasb_assembly18 ON asb_alploc(ASSEMBLY_ID)
;
-- 
-- INDEX: refalib_component11 
--

CREATE INDEX refalib_component11 ON asb_component_arg(COMPONENT_ALIB_ID)
;
-- 
-- INDEX: refasb_assembly34 
--

CREATE INDEX refasb_assembly34 ON asb_component_arg(ASSEMBLY_ID)
;
-- 
-- INDEX: refalib_component28 
--

CREATE INDEX refalib_component28 ON asb_component_hierarchy(COMPONENT_ALIB_ID)
;
-- 
-- INDEX: refalib_component29 
--

CREATE INDEX refalib_component29 ON asb_component_hierarchy(PARENT_COMPONENT_ALIB_ID)
;
-- 
-- INDEX: refasb_assembly30 
--

CREATE INDEX refasb_assembly30 ON asb_component_hierarchy(ASSEMBLY_ID)
;
-- 
-- INDEX: refasb_assembly19 
--

CREATE INDEX refasb_assembly19 ON asb_oplan(ASSEMBLY_ID)
;
-- 
-- INDEX: refasb_oplan20 
--

CREATE INDEX refasb_oplan20 ON asb_oplan_agent_attr(OPLAN_ID, ASSEMBLY_ID)
;
-- 
-- INDEX: refalib_component21 
--

CREATE INDEX refalib_component21 ON asb_oplan_agent_attr(COMPONENT_ALIB_ID)
;
-- 
-- INDEX: refcfw_instance4 
--

CREATE INDEX refcfw_instance4 ON cfw_alploc(CFW_ID)
;
-- 
-- INDEX: reflib_plugin_arg43 
--

CREATE INDEX reflib_plugin_arg43 ON cfw_context_plugin_arg(PLUGIN_ARG_ID)
;
-- 
-- INDEX: refcfw_instance44 
--

CREATE INDEX refcfw_instance44 ON cfw_context_plugin_arg(CFW_ID)
;
-- 
-- INDEX: refcfw_group2 
--

CREATE INDEX refcfw_group2 ON cfw_group_member(CFW_GROUP_ID)
;
-- 
-- INDEX: refcfw_instance3 
--

CREATE INDEX refcfw_instance3 ON cfw_group_member(CFW_ID)
;
-- 
-- INDEX: refcfw_group32 
--

CREATE INDEX refcfw_group32 ON cfw_group_org(CFW_GROUP_ID)
;
-- 
-- INDEX: reflib_organization33 
--

CREATE INDEX reflib_organization33 ON cfw_group_org(ORG_ID)
;
-- 
-- INDEX: refcfw_instance5 
--

CREATE INDEX refcfw_instance5 ON cfw_oplan(CFW_ID)
;
-- 
-- INDEX: refcfw_oplan62 
--

CREATE INDEX refcfw_oplan62 ON cfw_oplan_activity(OPLAN_ID, CFW_ID)
;
-- 
-- INDEX: reflib_org_group63 
--

CREATE INDEX reflib_org_group63 ON cfw_oplan_activity(ORG_GROUP_ID)
;
-- 
-- INDEX: reflib_org_group67 
--

CREATE INDEX reflib_org_group67 ON cfw_oplan_loc(ORG_GROUP_ID)
;
-- 
-- INDEX: refcfw_oplan64 
--

CREATE INDEX refcfw_oplan64 ON cfw_oplan_loc(OPLAN_ID, CFW_ID)
;
-- 
-- INDEX: reflib_org_group36 
--

CREATE INDEX reflib_org_group36 ON cfw_oplan_og_attr(ORG_GROUP_ID)
;
-- 
-- INDEX: refcfw_oplan7 
--

CREATE INDEX refcfw_oplan7 ON cfw_oplan_og_attr(OPLAN_ID, CFW_ID)
;
-- 
-- INDEX: reflib_org_group11 
--

CREATE INDEX reflib_org_group11 ON cfw_org_group_org_member(ORG_GROUP_ID)
;
-- 
-- INDEX: refcfw_instance60 
--

CREATE INDEX refcfw_instance60 ON cfw_org_group_org_member(CFW_ID)
;
-- 
-- INDEX: reflib_organization61 
--

CREATE INDEX reflib_organization61 ON cfw_org_group_org_member(ORG_ID)
;
-- 
-- INDEX: refcfw_instance56 
--

CREATE INDEX refcfw_instance56 ON cfw_org_hierarchy(CFW_ID)
;
-- 
-- INDEX: reflib_organization57 
--

CREATE INDEX reflib_organization57 ON cfw_org_hierarchy(ORG_ID)
;
-- 
-- INDEX: reflib_organization59 
--

CREATE INDEX reflib_organization59 ON cfw_org_hierarchy(SUPERIOR_ORG_ID)
;
-- 
-- INDEX: reflib_organization16 
--

CREATE INDEX reflib_organization16 ON cfw_org_list(ORG_ID)
;
-- 
-- INDEX: refcfw_instance17 
--

CREATE INDEX refcfw_instance17 ON cfw_org_list(CFW_ID)
;
-- 
-- INDEX: reflib_org_group20 
--

CREATE INDEX reflib_org_group20 ON cfw_org_og_relation(ORG_GROUP_ID)
;
-- 
-- INDEX: reflib_role_ref31 
--

CREATE INDEX reflib_role_ref31 ON cfw_org_og_relation(ROLE)
;
-- 
-- INDEX: reflib_organization52 
--

CREATE INDEX reflib_organization52 ON cfw_org_og_relation(ORG_ID)
;
-- 
-- INDEX: refcfw_instance53 
--

CREATE INDEX refcfw_instance53 ON cfw_org_og_relation(CFW_ID)
;
-- 
-- INDEX: refcfw_org_list47 
--

CREATE INDEX refcfw_org_list47 ON cfw_org_orgtype(ORG_ID, CFW_ID)
;
-- 
-- INDEX: reflib_orgtype_ref48 
--

CREATE INDEX reflib_orgtype_ref48 ON cfw_org_orgtype(ORGTYPE_ID)
;
-- 
-- INDEX: refcfw_org_list37 
--

CREATE INDEX refcfw_org_list37 ON cfw_org_pg_attr(ORG_ID, CFW_ID)
;
-- 
-- INDEX: reflib_orgtype_ref21 
--

CREATE INDEX reflib_orgtype_ref21 ON cfw_orgtype_plugin_grp(ORGTYPE_ID)
;
-- 
-- INDEX: refcfw_instance22 
--

CREATE INDEX refcfw_instance22 ON cfw_orgtype_plugin_grp(CFW_ID)
;
-- 
-- INDEX: reflib_plugin_group23 
--

CREATE INDEX reflib_plugin_group23 ON cfw_orgtype_plugin_grp(PLUGIN_GROUP_ID)
;
-- 
-- INDEX: reflib_plugin_ref26 
--

CREATE INDEX reflib_plugin_ref26 ON cfw_plugin_group_member(PLUGIN_ID)
;
-- 
-- INDEX: reflib_plugin_group27 
--

CREATE INDEX reflib_plugin_group27 ON cfw_plugin_group_member(PLUGIN_GROUP_ID)
;
-- 
-- INDEX: refcfw_instance28 
--

CREATE INDEX refcfw_instance28 ON cfw_plugin_group_member(CFW_ID)
;
-- 
-- INDEX: Ref677 
--

CREATE INDEX Ref677 ON community_attribute(ASSEMBLY_ID)
;
-- 
-- INDEX: Ref676 
--

CREATE INDEX Ref676 ON community_entity_attribute(ASSEMBLY_ID)
;
-- 
-- INDEX: refexpt_experiment26 
--

CREATE INDEX refexpt_experiment26 ON expt_trial(EXPT_ID)
;
-- 
-- INDEX: refexpt_experiment50 
--

CREATE INDEX refexpt_experiment50 ON expt_trial_assembly(EXPT_ID)
;
-- 
-- INDEX: refasb_assembly2 
--

CREATE INDEX refasb_assembly2 ON expt_trial_assembly(ASSEMBLY_ID)
;
-- 
-- INDEX: refexpt_trial27 
--

CREATE INDEX refexpt_trial27 ON expt_trial_assembly(TRIAL_ID)
;
-- 
-- INDEX: Ref1278 
--

CREATE INDEX Ref1278 ON expt_trial_config_assembly(TRIAL_ID)
;
-- 
-- INDEX: Ref1179 
--

CREATE INDEX Ref1179 ON expt_trial_config_assembly(EXPT_ID)
;
-- 
-- INDEX: Ref680 
--

CREATE INDEX Ref680 ON expt_trial_config_assembly(ASSEMBLY_ID)
;
-- 
-- INDEX: refexpt_trial47 
--

CREATE INDEX refexpt_trial47 ON expt_trial_mod_recipe(TRIAL_ID)
;
-- 
-- INDEX: refexpt_experiment51 
--

CREATE INDEX refexpt_experiment51 ON expt_trial_mod_recipe(EXPT_ID)
;
-- 
-- INDEX: reflib_mod_recipe56 
--

CREATE INDEX reflib_mod_recipe56 ON expt_trial_mod_recipe(MOD_RECIPE_LIB_ID)
;
-- 
-- INDEX: refexpt_trial41 
--

CREATE INDEX refexpt_trial41 ON expt_trial_org_mult(TRIAL_ID)
;
-- 
-- INDEX: refexpt_experiment52 
--

CREATE INDEX refexpt_experiment52 ON expt_trial_org_mult(EXPT_ID)
;
-- 
-- INDEX: refexpt_trial40 
--

CREATE INDEX refexpt_trial40 ON expt_trial_thread(TRIAL_ID)
;
-- 
-- INDEX: refexpt_experiment53 
--

CREATE INDEX refexpt_experiment53 ON expt_trial_thread(EXPT_ID)
;
-- 
-- INDEX: reflib_component45 
--

CREATE INDEX reflib_component45 ON lib_agent_org(COMPONENT_LIB_ID)
;
-- 
-- INDEX: reflib_component14 
--

CREATE INDEX reflib_component14 ON lib_component_arg(COMPONENT_LIB_ID)
;
-- 
-- INDEX: reflib_mod_recipe55 
--

CREATE INDEX reflib_mod_recipe55 ON lib_mod_recipe_arg(MOD_RECIPE_LIB_ID)
;
-- 
-- INDEX: Ref5081 
--

CREATE INDEX Ref5081 ON lib_plugin_arg(PLUGIN_ID)
;
-- 
-- INDEX: reflib_thread41 
--

CREATE INDEX reflib_thread41 ON lib_plugin_arg_thread(THREAD_ID)
;
-- 
-- INDEX: reflib_plugin_arg42 
--

CREATE INDEX reflib_plugin_arg42 ON lib_plugin_arg_thread(PLUGIN_ARG_ID)
;
-- 
-- INDEX: reflib_plugin_ref29 
--

CREATE INDEX reflib_plugin_ref29 ON lib_plugin_thread(PLUGIN_ID)
;
-- 
-- INDEX: reflib_thread30 
--

CREATE INDEX reflib_thread30 ON lib_plugin_thread(THREAD_ID)
;
-- 
-- INDEX: reflib_role_ref49 
--

CREATE INDEX reflib_role_ref49 ON lib_role_thread(ROLE)
;
-- 
-- INDEX: reflib_thread50 
--

CREATE INDEX reflib_thread50 ON lib_role_thread(THREAD_ID)
;
-- 
-- TABLE: alib_component 
--

ALTER TABLE alib_component ADD CONSTRAINT reflib_clone_set38 
    FOREIGN KEY (CLONE_SET_ID)
    REFERENCES lib_clone_set(CLONE_SET_ID)
;

ALTER TABLE alib_component ADD CONSTRAINT reflib_component13 
    FOREIGN KEY (COMPONENT_LIB_ID)
    REFERENCES lib_component(COMPONENT_LIB_ID)
;


-- 
-- TABLE: asb_agent 
--

ALTER TABLE asb_agent ADD CONSTRAINT refalib_component43 
    FOREIGN KEY (COMPONENT_ALIB_ID)
    REFERENCES alib_component(COMPONENT_ALIB_ID)
;

ALTER TABLE asb_agent ADD CONSTRAINT refasb_assembly42 
    FOREIGN KEY (ASSEMBLY_ID)
    REFERENCES asb_assembly(ASSEMBLY_ID)
;

ALTER TABLE asb_agent ADD CONSTRAINT reflib_component44 
    FOREIGN KEY (COMPONENT_LIB_ID)
    REFERENCES lib_component(COMPONENT_LIB_ID)
;


-- 
-- TABLE: asb_agent_pg_attr 
--

ALTER TABLE asb_agent_pg_attr ADD CONSTRAINT refalib_component12 
    FOREIGN KEY (COMPONENT_ALIB_ID)
    REFERENCES alib_component(COMPONENT_ALIB_ID)
;

ALTER TABLE asb_agent_pg_attr ADD CONSTRAINT refasb_assembly36 
    FOREIGN KEY (ASSEMBLY_ID)
    REFERENCES asb_assembly(ASSEMBLY_ID)
;

ALTER TABLE asb_agent_pg_attr ADD CONSTRAINT reflib_pg_attribute17 
    FOREIGN KEY (PG_ATTRIBUTE_LIB_ID)
    REFERENCES lib_pg_attribute(PG_ATTRIBUTE_LIB_ID)
;


-- 
-- TABLE: asb_agent_relation 
--

ALTER TABLE asb_agent_relation ADD CONSTRAINT refalib_component31 
    FOREIGN KEY (SUPPORTING_COMPONENT_ALIB_ID)
    REFERENCES alib_component(COMPONENT_ALIB_ID)
;

ALTER TABLE asb_agent_relation ADD CONSTRAINT refalib_component32 
    FOREIGN KEY (SUPPORTED_COMPONENT_ALIB_ID)
    REFERENCES alib_component(COMPONENT_ALIB_ID)
;

ALTER TABLE asb_agent_relation ADD CONSTRAINT refasb_assembly4 
    FOREIGN KEY (ASSEMBLY_ID)
    REFERENCES asb_assembly(ASSEMBLY_ID)
;


-- 
-- TABLE: asb_alploc 
--

ALTER TABLE asb_alploc ADD CONSTRAINT refasb_assembly18 
    FOREIGN KEY (ASSEMBLY_ID)
    REFERENCES asb_assembly(ASSEMBLY_ID)
;


-- 
-- TABLE: asb_component_arg 
--

ALTER TABLE asb_component_arg ADD CONSTRAINT refalib_component11 
    FOREIGN KEY (COMPONENT_ALIB_ID)
    REFERENCES alib_component(COMPONENT_ALIB_ID)
;

ALTER TABLE asb_component_arg ADD CONSTRAINT refasb_assembly34 
    FOREIGN KEY (ASSEMBLY_ID)
    REFERENCES asb_assembly(ASSEMBLY_ID)
;


-- 
-- TABLE: asb_component_hierarchy 
--

ALTER TABLE asb_component_hierarchy ADD CONSTRAINT refalib_component28 
    FOREIGN KEY (COMPONENT_ALIB_ID)
    REFERENCES alib_component(COMPONENT_ALIB_ID)
;

ALTER TABLE asb_component_hierarchy ADD CONSTRAINT refalib_component29 
    FOREIGN KEY (PARENT_COMPONENT_ALIB_ID)
    REFERENCES alib_component(COMPONENT_ALIB_ID)
;

ALTER TABLE asb_component_hierarchy ADD CONSTRAINT refasb_assembly30 
    FOREIGN KEY (ASSEMBLY_ID)
    REFERENCES asb_assembly(ASSEMBLY_ID)
;


-- 
-- TABLE: asb_oplan 
--

ALTER TABLE asb_oplan ADD CONSTRAINT refasb_assembly19 
    FOREIGN KEY (ASSEMBLY_ID)
    REFERENCES asb_assembly(ASSEMBLY_ID)
;


-- 
-- TABLE: asb_oplan_agent_attr 
--

ALTER TABLE asb_oplan_agent_attr ADD CONSTRAINT refalib_component21 
    FOREIGN KEY (COMPONENT_ALIB_ID)
    REFERENCES alib_component(COMPONENT_ALIB_ID)
;

ALTER TABLE asb_oplan_agent_attr ADD CONSTRAINT refasb_oplan20 
    FOREIGN KEY (ASSEMBLY_ID,OPLAN_ID)
    REFERENCES asb_oplan(ASSEMBLY_ID,OPLAN_ID)
;


-- 
-- TABLE: cfw_alploc 
--

ALTER TABLE cfw_alploc ADD CONSTRAINT refcfw_instance4 
    FOREIGN KEY (CFW_ID)
    REFERENCES cfw_instance(CFW_ID)
;


-- 
-- TABLE: cfw_context_plugin_arg 
--

ALTER TABLE cfw_context_plugin_arg ADD CONSTRAINT refcfw_instance44 
    FOREIGN KEY (CFW_ID)
    REFERENCES cfw_instance(CFW_ID)
;

ALTER TABLE cfw_context_plugin_arg ADD CONSTRAINT reflib_plugin_arg43 
    FOREIGN KEY (PLUGIN_ARG_ID)
    REFERENCES lib_plugin_arg(PLUGIN_ARG_ID)
;


-- 
-- TABLE: cfw_group_member 
--

ALTER TABLE cfw_group_member ADD CONSTRAINT refcfw_group2 
    FOREIGN KEY (CFW_GROUP_ID)
    REFERENCES cfw_group(CFW_GROUP_ID)
;

ALTER TABLE cfw_group_member ADD CONSTRAINT refcfw_instance3 
    FOREIGN KEY (CFW_ID)
    REFERENCES cfw_instance(CFW_ID)
;


-- 
-- TABLE: cfw_group_org 
--

ALTER TABLE cfw_group_org ADD CONSTRAINT refcfw_group32 
    FOREIGN KEY (CFW_GROUP_ID)
    REFERENCES cfw_group(CFW_GROUP_ID)
;

ALTER TABLE cfw_group_org ADD CONSTRAINT reflib_organization33 
    FOREIGN KEY (ORG_ID)
    REFERENCES lib_organization(ORG_ID)
;


-- 
-- TABLE: cfw_oplan 
--

ALTER TABLE cfw_oplan ADD CONSTRAINT refcfw_instance5 
    FOREIGN KEY (CFW_ID)
    REFERENCES cfw_instance(CFW_ID)
;


-- 
-- TABLE: cfw_oplan_activity 
--

ALTER TABLE cfw_oplan_activity ADD CONSTRAINT refcfw_oplan62 
    FOREIGN KEY (CFW_ID,OPLAN_ID)
    REFERENCES cfw_oplan(CFW_ID,OPLAN_ID)
;

ALTER TABLE cfw_oplan_activity ADD CONSTRAINT reflib_org_group63 
    FOREIGN KEY (ORG_GROUP_ID)
    REFERENCES lib_org_group(ORG_GROUP_ID)
;


-- 
-- TABLE: cfw_oplan_loc 
--

ALTER TABLE cfw_oplan_loc ADD CONSTRAINT refcfw_oplan64 
    FOREIGN KEY (CFW_ID,OPLAN_ID)
    REFERENCES cfw_oplan(CFW_ID,OPLAN_ID)
;

ALTER TABLE cfw_oplan_loc ADD CONSTRAINT reflib_org_group67 
    FOREIGN KEY (ORG_GROUP_ID)
    REFERENCES lib_org_group(ORG_GROUP_ID)
;


-- 
-- TABLE: cfw_oplan_og_attr 
--

ALTER TABLE cfw_oplan_og_attr ADD CONSTRAINT refcfw_oplan7 
    FOREIGN KEY (CFW_ID,OPLAN_ID)
    REFERENCES cfw_oplan(CFW_ID,OPLAN_ID)
;

ALTER TABLE cfw_oplan_og_attr ADD CONSTRAINT reflib_org_group36 
    FOREIGN KEY (ORG_GROUP_ID)
    REFERENCES lib_org_group(ORG_GROUP_ID)
;


-- 
-- TABLE: cfw_org_group_org_member 
--

ALTER TABLE cfw_org_group_org_member ADD CONSTRAINT refcfw_instance60 
    FOREIGN KEY (CFW_ID)
    REFERENCES cfw_instance(CFW_ID)
;

ALTER TABLE cfw_org_group_org_member ADD CONSTRAINT reflib_org_group11 
    FOREIGN KEY (ORG_GROUP_ID)
    REFERENCES lib_org_group(ORG_GROUP_ID)
;

ALTER TABLE cfw_org_group_org_member ADD CONSTRAINT reflib_organization61 
    FOREIGN KEY (ORG_ID)
    REFERENCES lib_organization(ORG_ID)
;


-- 
-- TABLE: cfw_org_hierarchy 
--

ALTER TABLE cfw_org_hierarchy ADD CONSTRAINT refcfw_instance56 
    FOREIGN KEY (CFW_ID)
    REFERENCES cfw_instance(CFW_ID)
;

ALTER TABLE cfw_org_hierarchy ADD CONSTRAINT reflib_organization57 
    FOREIGN KEY (ORG_ID)
    REFERENCES lib_organization(ORG_ID)
;

ALTER TABLE cfw_org_hierarchy ADD CONSTRAINT reflib_organization59 
    FOREIGN KEY (SUPERIOR_ORG_ID)
    REFERENCES lib_organization(ORG_ID)
;


-- 
-- TABLE: cfw_org_list 
--

ALTER TABLE cfw_org_list ADD CONSTRAINT refcfw_instance17 
    FOREIGN KEY (CFW_ID)
    REFERENCES cfw_instance(CFW_ID)
;

ALTER TABLE cfw_org_list ADD CONSTRAINT reflib_organization16 
    FOREIGN KEY (ORG_ID)
    REFERENCES lib_organization(ORG_ID)
;


-- 
-- TABLE: cfw_org_og_relation 
--

ALTER TABLE cfw_org_og_relation ADD CONSTRAINT refcfw_instance53 
    FOREIGN KEY (CFW_ID)
    REFERENCES cfw_instance(CFW_ID)
;

ALTER TABLE cfw_org_og_relation ADD CONSTRAINT reflib_org_group20 
    FOREIGN KEY (ORG_GROUP_ID)
    REFERENCES lib_org_group(ORG_GROUP_ID)
;

ALTER TABLE cfw_org_og_relation ADD CONSTRAINT reflib_organization52 
    FOREIGN KEY (ORG_ID)
    REFERENCES lib_organization(ORG_ID)
;

ALTER TABLE cfw_org_og_relation ADD CONSTRAINT reflib_role_ref31 
    FOREIGN KEY (ROLE)
    REFERENCES lib_role_ref(ROLE)
;


-- 
-- TABLE: cfw_org_orgtype 
--

ALTER TABLE cfw_org_orgtype ADD CONSTRAINT refcfw_org_list47 
    FOREIGN KEY (CFW_ID,ORG_ID)
    REFERENCES cfw_org_list(CFW_ID,ORG_ID)
;

ALTER TABLE cfw_org_orgtype ADD CONSTRAINT reflib_orgtype_ref48 
    FOREIGN KEY (ORGTYPE_ID)
    REFERENCES lib_orgtype_ref(ORGTYPE_ID)
;


-- 
-- TABLE: cfw_org_pg_attr 
--

ALTER TABLE cfw_org_pg_attr ADD CONSTRAINT refcfw_org_list37 
    FOREIGN KEY (CFW_ID,ORG_ID)
    REFERENCES cfw_org_list(CFW_ID,ORG_ID)
;


-- 
-- TABLE: cfw_orgtype_plugin_grp 
--

ALTER TABLE cfw_orgtype_plugin_grp ADD CONSTRAINT refcfw_instance22 
    FOREIGN KEY (CFW_ID)
    REFERENCES cfw_instance(CFW_ID)
;

ALTER TABLE cfw_orgtype_plugin_grp ADD CONSTRAINT reflib_orgtype_ref21 
    FOREIGN KEY (ORGTYPE_ID)
    REFERENCES lib_orgtype_ref(ORGTYPE_ID)
;

ALTER TABLE cfw_orgtype_plugin_grp ADD CONSTRAINT reflib_plugin_group23 
    FOREIGN KEY (PLUGIN_GROUP_ID)
    REFERENCES lib_plugin_group(PLUGIN_GROUP_ID)
;


-- 
-- TABLE: cfw_plugin_group_member 
--

ALTER TABLE cfw_plugin_group_member ADD CONSTRAINT refcfw_instance28 
    FOREIGN KEY (CFW_ID)
    REFERENCES cfw_instance(CFW_ID)
;

ALTER TABLE cfw_plugin_group_member ADD CONSTRAINT reflib_plugin_group27 
    FOREIGN KEY (PLUGIN_GROUP_ID)
    REFERENCES lib_plugin_group(PLUGIN_GROUP_ID)
;

ALTER TABLE cfw_plugin_group_member ADD CONSTRAINT reflib_plugin_ref26 
    FOREIGN KEY (PLUGIN_ID)
    REFERENCES lib_plugin_ref(PLUGIN_ID)
;


-- 
-- TABLE: community_attribute 
--

ALTER TABLE community_attribute ADD CONSTRAINT Refasb_assembly77 
    FOREIGN KEY (ASSEMBLY_ID)
    REFERENCES asb_assembly(ASSEMBLY_ID)
;


-- 
-- TABLE: community_entity_attribute 
--

ALTER TABLE community_entity_attribute ADD CONSTRAINT Refasb_assembly76 
    FOREIGN KEY (ASSEMBLY_ID)
    REFERENCES asb_assembly(ASSEMBLY_ID)
;


-- 
-- TABLE: expt_trial 
--

ALTER TABLE expt_trial ADD CONSTRAINT refexpt_experiment26 
    FOREIGN KEY (EXPT_ID)
    REFERENCES expt_experiment(EXPT_ID)
;


-- 
-- TABLE: expt_trial_assembly 
--

ALTER TABLE expt_trial_assembly ADD CONSTRAINT refasb_assembly2 
    FOREIGN KEY (ASSEMBLY_ID)
    REFERENCES asb_assembly(ASSEMBLY_ID)
;

ALTER TABLE expt_trial_assembly ADD CONSTRAINT refexpt_experiment50 
    FOREIGN KEY (EXPT_ID)
    REFERENCES expt_experiment(EXPT_ID)
;

ALTER TABLE expt_trial_assembly ADD CONSTRAINT refexpt_trial27 
    FOREIGN KEY (TRIAL_ID)
    REFERENCES expt_trial(TRIAL_ID)
;


-- 
-- TABLE: expt_trial_config_assembly 
--

ALTER TABLE expt_trial_config_assembly ADD CONSTRAINT Refexpt_trial78 
    FOREIGN KEY (TRIAL_ID)
    REFERENCES expt_trial(TRIAL_ID)
;

ALTER TABLE expt_trial_config_assembly ADD CONSTRAINT Refexpt_experiment79 
    FOREIGN KEY (EXPT_ID)
    REFERENCES expt_experiment(EXPT_ID)
;

ALTER TABLE expt_trial_config_assembly ADD CONSTRAINT Refasb_assembly80 
    FOREIGN KEY (ASSEMBLY_ID)
    REFERENCES asb_assembly(ASSEMBLY_ID)
;


-- 
-- TABLE: expt_trial_mod_recipe 
--

ALTER TABLE expt_trial_mod_recipe ADD CONSTRAINT refexpt_experiment51 
    FOREIGN KEY (EXPT_ID)
    REFERENCES expt_experiment(EXPT_ID)
;

ALTER TABLE expt_trial_mod_recipe ADD CONSTRAINT refexpt_trial47 
    FOREIGN KEY (TRIAL_ID)
    REFERENCES expt_trial(TRIAL_ID)
;

ALTER TABLE expt_trial_mod_recipe ADD CONSTRAINT reflib_mod_recipe56 
    FOREIGN KEY (MOD_RECIPE_LIB_ID)
    REFERENCES lib_mod_recipe(MOD_RECIPE_LIB_ID)
;


-- 
-- TABLE: expt_trial_org_mult 
--

ALTER TABLE expt_trial_org_mult ADD CONSTRAINT refexpt_experiment52 
    FOREIGN KEY (EXPT_ID)
    REFERENCES expt_experiment(EXPT_ID)
;

ALTER TABLE expt_trial_org_mult ADD CONSTRAINT refexpt_trial41 
    FOREIGN KEY (TRIAL_ID)
    REFERENCES expt_trial(TRIAL_ID)
;


-- 
-- TABLE: expt_trial_thread 
--

ALTER TABLE expt_trial_thread ADD CONSTRAINT refexpt_experiment53 
    FOREIGN KEY (EXPT_ID)
    REFERENCES expt_experiment(EXPT_ID)
;

ALTER TABLE expt_trial_thread ADD CONSTRAINT refexpt_trial40 
    FOREIGN KEY (TRIAL_ID)
    REFERENCES expt_trial(TRIAL_ID)
;


-- 
-- TABLE: lib_agent_org 
--

ALTER TABLE lib_agent_org ADD CONSTRAINT reflib_component45 
    FOREIGN KEY (COMPONENT_LIB_ID)
    REFERENCES lib_component(COMPONENT_LIB_ID)
;


-- 
-- TABLE: lib_component_arg 
--

ALTER TABLE lib_component_arg ADD CONSTRAINT reflib_component14 
    FOREIGN KEY (COMPONENT_LIB_ID)
    REFERENCES lib_component(COMPONENT_LIB_ID)
;


-- 
-- TABLE: lib_mod_recipe_arg 
--

ALTER TABLE lib_mod_recipe_arg ADD CONSTRAINT reflib_mod_recipe55 
    FOREIGN KEY (MOD_RECIPE_LIB_ID)
    REFERENCES lib_mod_recipe(MOD_RECIPE_LIB_ID)
;


-- 
-- TABLE: lib_plugin_arg 
--

ALTER TABLE lib_plugin_arg ADD CONSTRAINT Reflib_plugin_ref81 
    FOREIGN KEY (PLUGIN_ID)
    REFERENCES lib_plugin_ref(PLUGIN_ID)
;


-- 
-- TABLE: lib_plugin_arg_thread 
--

ALTER TABLE lib_plugin_arg_thread ADD CONSTRAINT reflib_plugin_arg42 
    FOREIGN KEY (PLUGIN_ARG_ID)
    REFERENCES lib_plugin_arg(PLUGIN_ARG_ID)
;

ALTER TABLE lib_plugin_arg_thread ADD CONSTRAINT reflib_thread41 
    FOREIGN KEY (THREAD_ID)
    REFERENCES lib_thread(THREAD_ID)
;


-- 
-- TABLE: lib_plugin_thread 
--

ALTER TABLE lib_plugin_thread ADD CONSTRAINT reflib_plugin_ref29 
    FOREIGN KEY (PLUGIN_ID)
    REFERENCES lib_plugin_ref(PLUGIN_ID)
;

ALTER TABLE lib_plugin_thread ADD CONSTRAINT reflib_thread30 
    FOREIGN KEY (THREAD_ID)
    REFERENCES lib_thread(THREAD_ID)
;


-- 
-- TABLE: lib_role_thread 
--

ALTER TABLE lib_role_thread ADD CONSTRAINT reflib_role_ref49 
    FOREIGN KEY (ROLE)
    REFERENCES lib_role_ref(ROLE)
;

ALTER TABLE lib_role_thread ADD CONSTRAINT reflib_thread50 
    FOREIGN KEY (THREAD_ID)
    REFERENCES lib_thread(THREAD_ID)
;



