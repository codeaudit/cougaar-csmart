DROP TABLE IF EXISTS v4_asb_thread;
DROP TABLE IF EXISTS v4_expt_trial_metric;
DROP TABLE IF EXISTS v4_expt_trial_metric_prop;
DROP TABLE IF EXISTS v4_lib_machine;
DROP TABLE IF EXISTS v4_lib_oplan_agent_attr_ref;
DROP TABLE IF EXISTS v4_lib_optempo_ref;
DROP TABLE IF EXISTS v6_cfw_org_group_og_member;

################################################################

#
# Table structure for table 'v4_alib_component'
#

DROP TABLE IF EXISTS v4_alib_component;
CREATE TABLE v4_alib_component (
  COMPONENT_ALIB_ID varchar(150) binary NOT NULL default '',
  COMPONENT_NAME varchar(150) binary default NULL,
  COMPONENT_LIB_ID varchar(150) binary NOT NULL default '',
  COMPONENT_TYPE varchar(50) binary default NULL,
  CLONE_SET_ID decimal(68,30) NOT NULL default '0.000000000000000000000000000000',
  UNIQUE KEY PK_V4_ALIB_COMPONENT (COMPONENT_ALIB_ID),
  KEY CLONE_SET_ID (CLONE_SET_ID),
  KEY COMPONENT_LIB_ID (COMPONENT_LIB_ID),
  KEY TYPE (COMPONENT_TYPE)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/alib_component.csv.tmp'
    INTO TABLE v4_alib_component
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (COMPONENT_ALIB_ID,COMPONENT_NAME,COMPONENT_LIB_ID,COMPONENT_TYPE,CLONE_SET_ID);

#
# Table structure for table 'v4_asb_agent'
#

DROP TABLE IF EXISTS v4_asb_agent;
CREATE TABLE v4_asb_agent (
  ASSEMBLY_ID varchar(50) binary NOT NULL default '',
  COMPONENT_ALIB_ID varchar(150) binary NOT NULL default '',
  COMPONENT_LIB_ID varchar(150) binary default NULL,
  CLONE_SET_ID decimal(68,30) default NULL,
  COMPONENT_NAME varchar(50) binary default NULL,
  PRIMARY KEY  (ASSEMBLY_ID,COMPONENT_ALIB_ID),
  KEY ASSEMBLY_ID (ASSEMBLY_ID),
  KEY COMPONENT_ALIB_ID (COMPONENT_ALIB_ID),
  KEY COMPONENT_LIB_ID (COMPONENT_LIB_ID),
  KEY COMPONENT_NAME (COMPONENT_NAME)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_agent.csv.tmp'
    INTO TABLE v4_asb_agent
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,COMPONENT_ALIB_ID,COMPONENT_LIB_ID,CLONE_SET_ID,COMPONENT_NAME);


#
# Table structure for table 'v4_asb_agent_pg_attr'
#

DROP TABLE IF EXISTS v4_asb_agent_pg_attr;
CREATE TABLE v4_asb_agent_pg_attr (
  ASSEMBLY_ID varchar(50) binary NOT NULL default '',
  COMPONENT_ALIB_ID varchar(150) binary NOT NULL default '',
  PG_ATTRIBUTE_LIB_ID varchar(100) binary NOT NULL default '',
  ATTRIBUTE_VALUE varchar(100) binary NOT NULL default '',
  ATTRIBUTE_ORDER decimal(68,30) NOT NULL default '0.000000000000000000000000000000',
  START_DATE datetime NOT NULL default '0000-00-00 00:00:00',
  END_DATE datetime default NULL,
  PRIMARY KEY  (ASSEMBLY_ID,COMPONENT_ALIB_ID,PG_ATTRIBUTE_LIB_ID,ATTRIBUTE_VALUE,ATTRIBUTE_ORDER,START_DATE),
  KEY ASSEMBLY_ID (ASSEMBLY_ID),
  KEY COMPONENT_ALIB_ID (COMPONENT_ALIB_ID),
  KEY PG_ATTRIBUTE_LIB_ID (PG_ATTRIBUTE_LIB_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_agent_pg_attr.csv.tmp'
    INTO TABLE v4_asb_agent_pg_attr
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,COMPONENT_ALIB_ID,PG_ATTRIBUTE_LIB_ID,ATTRIBUTE_VALUE,ATTRIBUTE_ORDER,START_DATE,END_DATE);

#
# Table structure for table 'v4_asb_agent_relation'
#

DROP TABLE IF EXISTS v4_asb_agent_relation;
CREATE TABLE v4_asb_agent_relation (
  ASSEMBLY_ID varchar(50) binary NOT NULL default '',
  ROLE varchar(50) binary NOT NULL default '',
  SUPPORTING_COMPONENT_ALIB_ID varchar(150) binary NOT NULL default '',
  SUPPORTED_COMPONENT_ALIB_ID varchar(150) binary NOT NULL default '',
  START_DATE datetime NOT NULL default '0000-00-00 00:00:00',
  END_DATE datetime default NULL,
  PRIMARY KEY  (ASSEMBLY_ID,ROLE,SUPPORTING_COMPONENT_ALIB_ID,SUPPORTED_COMPONENT_ALIB_ID,START_DATE),
  KEY ASSEMBLY_ID (ASSEMBLY_ID),
  KEY SUPPORTING_COMPONENT_ALIB_ID (SUPPORTING_COMPONENT_ALIB_ID),
  KEY ASSEMBLY_SUPPORTING (ASSEMBLY_ID, SUPPORTING_COMPONENT_ALIB_ID),
  KEY SUPPORTED_COMPONENT_ALIB_ID (SUPPORTED_COMPONENT_ALIB_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_agent_relation.csv.tmp'
    INTO TABLE v4_asb_agent_relation
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,ROLE,SUPPORTING_COMPONENT_ALIB_ID,SUPPORTED_COMPONENT_ALIB_ID,START_DATE,END_DATE);

#
# Table structure for table 'v4_asb_alploc'
#

DROP TABLE IF EXISTS v4_asb_alploc;
CREATE TABLE v4_asb_alploc (
  ASSEMBLY_ID varchar(50) NOT NULL default '',
  ALPLOC_CODE varchar(50) NOT NULL default '',
  LOCATION_NAME varchar(50) default NULL,
  LATITUDE decimal(68,30) default NULL,
  LONGITUDE decimal(68,30) default NULL,
  INSTALLATION_TYPE_CODE char(3) default NULL,
  PRIMARY KEY  (ASSEMBLY_ID,ALPLOC_CODE),
  KEY ASSEMBLY_ID (ASSEMBLY_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_alploc.csv.tmp'
    INTO TABLE v4_asb_alploc
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,ALPLOC_CODE,LOCATION_NAME,LATITUDE,LONGITUDE,INSTALLATION_TYPE_CODE);

#
# Table structure for table 'v4_asb_assembly'
#

DROP TABLE IF EXISTS v4_asb_assembly;
CREATE TABLE v4_asb_assembly (
  ASSEMBLY_ID varchar(50) binary NOT NULL default '',
  ASSEMBLY_TYPE varchar(50) binary default NULL,
  DESCRIPTION varchar(200) binary default NULL,
  UNIQUE KEY PK_V4_ASB_ASSEMBLY (ASSEMBLY_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_assembly.csv.tmp'
    INTO TABLE v4_asb_assembly
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,ASSEMBLY_TYPE,DESCRIPTION);

#
# Table structure for table 'v4_asb_component_arg'
#

DROP TABLE IF EXISTS v4_asb_component_arg;
CREATE TABLE v4_asb_component_arg (
  ASSEMBLY_ID varchar(50) binary NOT NULL default '',
  COMPONENT_ALIB_ID varchar(150) binary NOT NULL default '',
  ARGUMENT varchar(230) binary NOT NULL default '',
  ARGUMENT_ORDER decimal(68,30) NOT NULL default '0.000000000000000000000000000000',
  UNIQUE KEY FULL_ROW (ASSEMBLY_ID,COMPONENT_ALIB_ID,ARGUMENT,ARGUMENT_ORDER),
  KEY COMPONENT_ALIB_ID (COMPONENT_ALIB_ID),
  KEY ASSEMBLY_ID (ASSEMBLY_ID),
  KEY ASSEMBLY_COMP (ASSEMBLY_ID, COMPONENT_ALIB_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_component_arg.csv.tmp'
    INTO TABLE v4_asb_component_arg
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,COMPONENT_ALIB_ID,ARGUMENT,ARGUMENT_ORDER);

#
# Table structure for table 'v4_asb_component_hierarchy'
#

DROP TABLE IF EXISTS v4_asb_component_hierarchy;
CREATE TABLE v4_asb_component_hierarchy (
  ASSEMBLY_ID varchar(50) binary NOT NULL default '',
  COMPONENT_ALIB_ID varchar(150) binary NOT NULL default '',
  PARENT_COMPONENT_ALIB_ID varchar(150) binary NOT NULL default '',
  PRIORITY varchar(20) binary NOT NULL default '',	
  INSERTION_ORDER decimal(68,30) default NULL,
  PRIMARY KEY  (ASSEMBLY_ID,COMPONENT_ALIB_ID,PARENT_COMPONENT_ALIB_ID),
  KEY COMPONENT_ALIB_ID (COMPONENT_ALIB_ID),
  KEY PARENT_COMPONENT_ALIB_ID (PARENT_COMPONENT_ALIB_ID),
  KEY ASSEMBLY_ID (ASSEMBLY_ID),
  KEY ASSEMBLY_COMP (COMPONENT_ALIB_ID, ASSEMBLY_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_component_hierarchy.csv.tmp'
    INTO TABLE v4_asb_component_hierarchy
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,COMPONENT_ALIB_ID,PARENT_COMPONENT_ALIB_ID,PRIORITY,INSERTION_ORDER);

#
# Table structure for table 'v4_asb_oplan'
#

DROP TABLE IF EXISTS v4_asb_oplan;
CREATE TABLE v4_asb_oplan (
  ASSEMBLY_ID varchar(50) binary NOT NULL default '',
  OPLAN_ID varchar(50) binary NOT NULL default '',
  OPERATION_NAME varchar(50) default NULL,
  PRIORITY varchar(50) default NULL,
  C0_DATE datetime default NULL,
  PRIMARY KEY  (ASSEMBLY_ID,OPLAN_ID),
  KEY ASSEMBLY_ID (ASSEMBLY_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_oplan.csv.tmp'
    INTO TABLE v4_asb_oplan
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,OPLAN_ID,OPERATION_NAME,PRIORITY,C0_DATE);

#
# Table structure for table 'v4_asb_oplan_agent_attr'
#

DROP TABLE IF EXISTS v4_asb_oplan_agent_attr;
CREATE TABLE v4_asb_oplan_agent_attr (
  ASSEMBLY_ID varchar(50) binary NOT NULL default '',
  OPLAN_ID varchar(50) binary NOT NULL default '',
  COMPONENT_ALIB_ID varchar(150) NOT NULL default '',
  COMPONENT_ID varchar(50) binary NOT NULL default '',
  START_CDAY decimal(68,30) NOT NULL default '0.000000000000000000000000000000',
  ATTRIBUTE_NAME varchar(50) NOT NULL default '',
  END_CDAY decimal(68,30) default NULL,
  ATTRIBUTE_VALUE varchar(50) default NULL,
  PRIMARY KEY  (ASSEMBLY_ID,OPLAN_ID,COMPONENT_ALIB_ID,COMPONENT_ID,START_CDAY,ATTRIBUTE_NAME),
  KEY ASSEMBLY_AND_OPLAN_ID (ASSEMBLY_ID,OPLAN_ID),
  KEY COMPONENT_ALIB_ID (COMPONENT_ALIB_ID),
  KEY ATTRIBUTE_NAME (ATTRIBUTE_NAME),
  KEY ASSEMBLY_ID (ASSEMBLY_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/asb_oplan_agent_attr.csv.tmp'
    INTO TABLE v4_asb_oplan_agent_attr
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,OPLAN_ID,COMPONENT_ALIB_ID,COMPONENT_ID,START_CDAY,ATTRIBUTE_NAME,END_CDAY,ATTRIBUTE_VALUE);

#
# Table structure for table 'v4_expt_experiment'
#

DROP TABLE IF EXISTS v4_expt_experiment;
CREATE TABLE v4_expt_experiment (
  EXPT_ID varchar(50) binary NOT NULL default '',
  DESCRIPTION varchar(200) binary default NULL,
  NAME varchar(50) binary NOT NULL default '',
  CFW_GROUP_ID varchar(50) binary default NULL,
  UNIQUE KEY PK_V4_EXPT_EXPERIMENT (EXPT_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/expt_experiment.csv.tmp'
    INTO TABLE v4_expt_experiment
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (EXPT_ID,DESCRIPTION,NAME,CFW_GROUP_ID);

#
# Table structure for table 'v4_expt_trial'
#

DROP TABLE IF EXISTS v4_expt_trial;
CREATE TABLE v4_expt_trial (
  TRIAL_ID varchar(50) binary NOT NULL default '',
  EXPT_ID varchar(50) binary default NULL,
  DESCRIPTION varchar(100) binary default NULL,
  NAME varchar(50) binary default NULL,
  UNIQUE KEY PK_V4_EXPT_TRIAL (TRIAL_ID),
  KEY EXPT_ID (EXPT_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/expt_trial.csv.tmp'
    INTO TABLE v4_expt_trial
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (TRIAL_ID,EXPT_ID,DESCRIPTION,NAME);

#
# Table structure for table 'v4_expt_trial_assembly'
#

DROP TABLE IF EXISTS v4_expt_trial_assembly;
CREATE TABLE v4_expt_trial_assembly (
  TRIAL_ID varchar(50) binary NOT NULL default '',
  ASSEMBLY_ID varchar(50) binary NOT NULL default '',
  EXPT_ID varchar(50) binary NOT NULL default '',
  DESCRIPTION varchar(200) binary default NULL,
  PRIMARY KEY  (TRIAL_ID,ASSEMBLY_ID),
  KEY EXPT_ID (EXPT_ID),
  KEY ASSEMBLY_ID (ASSEMBLY_ID),
  KEY TRIAL_ID (TRIAL_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/expt_trial_assembly.csv.tmp'
    INTO TABLE v4_expt_trial_assembly
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (TRIAL_ID,ASSEMBLY_ID,EXPT_ID,DESCRIPTION);


#
# Table structure for table 'v4_expt_trial_config_assembly'
#

DROP TABLE IF EXISTS v4_expt_trial_config_assembly;
CREATE TABLE v4_expt_trial_config_assembly (
  TRIAL_ID varchar(50) binary NOT NULL default '',
  ASSEMBLY_ID varchar(50) binary NOT NULL default '',
  EXPT_ID varchar(50) binary NOT NULL default '',
  DESCRIPTION varchar(200) binary default NULL,
  PRIMARY KEY  (TRIAL_ID,ASSEMBLY_ID),
  KEY EXPT_ID (EXPT_ID),
  KEY ASSEMBLY_ID (ASSEMBLY_ID),
  KEY TRIAL_ID (TRIAL_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/expt_trial_config_assembly.csv.tmp'
    INTO TABLE v4_expt_trial_config_assembly
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (TRIAL_ID,ASSEMBLY_ID,EXPT_ID,DESCRIPTION);


#
# Table structure for table 'v4_expt_trial_mod_recipe'
#

DROP TABLE IF EXISTS v4_expt_trial_mod_recipe;
CREATE TABLE v4_expt_trial_mod_recipe (
  TRIAL_ID varchar(50) binary NOT NULL default '',
  MOD_RECIPE_LIB_ID varchar(100) binary NOT NULL default '',
  RECIPE_ORDER decimal(68,30) NOT NULL default '0.000000000000000000000000000000',
  EXPT_ID varchar(50) binary default NULL,
  PRIMARY KEY  (TRIAL_ID,MOD_RECIPE_LIB_ID,RECIPE_ORDER),
  KEY TRIAL_ID (TRIAL_ID),
  KEY EXPT_ID (EXPT_ID),
  KEY MOD_RECIPE_LIB_ID (MOD_RECIPE_LIB_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/expt_trial_mod_recipe.csv.tmp'
    INTO TABLE v4_expt_trial_mod_recipe
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (TRIAL_ID,MOD_RECIPE_LIB_ID,RECIPE_ORDER,EXPT_ID);

#
# Table structure for table 'v4_expt_trial_org_mult'
#

DROP TABLE IF EXISTS v4_expt_trial_org_mult;
CREATE TABLE v4_expt_trial_org_mult (
  TRIAL_ID varchar(50) binary NOT NULL default '',
  CFW_ID varchar(50) binary NOT NULL default '',
  ORG_GROUP_ID varchar(50) binary NOT NULL default '',
  EXPT_ID varchar(50) binary default NULL,
  MULTIPLIER decimal(68,30) default NULL,
  DESCRIPTION varchar(100) binary default NULL,
  PRIMARY KEY  (TRIAL_ID,CFW_ID,ORG_GROUP_ID),
  KEY TRIAL_ID (TRIAL_ID),
  KEY EXPT_ID (EXPT_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/expt_trial_org_mult.csv.tmp'
    INTO TABLE v4_expt_trial_org_mult
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (TRIAL_ID,CFW_ID,ORG_GROUP_ID,EXPT_ID,MULTIPLIER,DESCRIPTION);

#
# Table structure for table 'v4_expt_trial_thread'
#

DROP TABLE IF EXISTS v4_expt_trial_thread;
CREATE TABLE v4_expt_trial_thread (
  TRIAL_ID varchar(50) binary NOT NULL default '',
  THREAD_ID varchar(50) binary NOT NULL default '',
  EXPT_ID varchar(50) binary default NULL,
  PRIMARY KEY  (TRIAL_ID,THREAD_ID),
  KEY TRIAL_ID (TRIAL_ID),
  KEY EXPT_ID (EXPT_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/expt_trial_thread.csv.tmp'
    INTO TABLE v4_expt_trial_thread
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (TRIAL_ID,THREAD_ID,EXPT_ID);

#
# Table structure for table 'v4_lib_activity_type_ref'
#

DROP TABLE IF EXISTS v4_lib_activity_type_ref;
CREATE TABLE v4_lib_activity_type_ref (
  ACTIVITY_TYPE varchar(50) binary NOT NULL default '',
  DESCRIPTION varchar(50) binary default NULL,
  UNIQUE KEY PK_V4_LIB_ACTIVITY_TYPE_REF (ACTIVITY_TYPE)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_activity_type_ref.csv.tmp'
    INTO TABLE v4_lib_activity_type_ref
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ACTIVITY_TYPE,DESCRIPTION);

#
# Table structure for table 'v4_lib_agent_org'
#

DROP TABLE IF EXISTS v4_lib_agent_org;
CREATE TABLE v4_lib_agent_org (
  COMPONENT_LIB_ID varchar(150) binary NOT NULL default '',
  AGENT_LIB_NAME varchar(50) binary default NULL,
  AGENT_ORG_CLASS varchar(50) binary default NULL,
  UNIQUE KEY PK_V4_LIB_AGENT_ORG (COMPONENT_LIB_ID),
  KEY AGENT_LIB_NAME (AGENT_LIB_NAME)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_agent_org.csv.tmp'
    INTO TABLE v4_lib_agent_org
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (COMPONENT_LIB_ID,AGENT_LIB_NAME,AGENT_ORG_CLASS);

#
# Table structure for table 'v4_lib_clone_set'
#

DROP TABLE IF EXISTS v4_lib_clone_set;
CREATE TABLE v4_lib_clone_set (
  CLONE_SET_ID decimal(68,30) NOT NULL default '0.000000000000000000000000000000',
  UNIQUE KEY PK_V4_LIB_CLONE_SET (CLONE_SET_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_clone_set.csv.tmp'
    INTO TABLE v4_lib_clone_set
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CLONE_SET_ID);

#
# Table structure for table 'v4_lib_component'
#

DROP TABLE IF EXISTS v4_lib_component;
CREATE TABLE v4_lib_component (
  COMPONENT_LIB_ID varchar(150) binary NOT NULL default '',
  COMPONENT_TYPE varchar(50) binary default NULL,
  COMPONENT_CLASS varchar(100) binary default NULL,
  INSERTION_POINT varchar(50) binary default NULL,
  DESCRIPTION varchar(100) binary default NULL,
  UNIQUE KEY PK_V4_LIB_COMPONENT (COMPONENT_LIB_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_component.csv.tmp'
    INTO TABLE v4_lib_component
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (COMPONENT_LIB_ID,COMPONENT_TYPE,COMPONENT_CLASS,INSERTION_POINT,DESCRIPTION);

#
# Table structure for table 'v4_lib_component_arg'
#

DROP TABLE IF EXISTS v4_lib_component_arg;
CREATE TABLE v4_lib_component_arg (
  COMPONENT_LIB_ID varchar(150) binary NOT NULL default '',
  ARGUMENT varchar(100) binary NOT NULL default '',
  PRIMARY KEY  (COMPONENT_LIB_ID,ARGUMENT),
  KEY COMPONENT_LIB_ID (COMPONENT_LIB_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_component_arg.csv.tmp'
    INTO TABLE v4_lib_component_arg
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (COMPONENT_LIB_ID,ARGUMENT);

#
# Table structure for table 'v4_lib_mod_recipe'
#

DROP TABLE IF EXISTS v4_lib_mod_recipe;
CREATE TABLE v4_lib_mod_recipe (
  MOD_RECIPE_LIB_ID varchar(100) binary NOT NULL default '',
  NAME varchar(50) binary default NULL,
  JAVA_CLASS varchar(100) binary default NULL,
  DESCRIPTION varchar(100) binary default NULL,
  UNIQUE KEY PK_V4_LIB_MOD_RECIPE (MOD_RECIPE_LIB_ID),
  KEY NAME (NAME)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_mod_recipe.csv.tmp'
    INTO TABLE v4_lib_mod_recipe
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (MOD_RECIPE_LIB_ID,NAME,JAVA_CLASS,DESCRIPTION);

#
# Table structure for table 'v4_lib_mod_recipe_arg'
#

DROP TABLE IF EXISTS v4_lib_mod_recipe_arg;
CREATE TABLE v4_lib_mod_recipe_arg (
  MOD_RECIPE_LIB_ID varchar(100) binary NOT NULL default '',
  ARG_NAME varchar(100) binary NOT NULL default '',
  ARG_ORDER decimal(68,30) NOT NULL default '0.000000000000000000000000000000',
  ARG_VALUE varchar(255) binary default NULL,
  PRIMARY KEY  (MOD_RECIPE_LIB_ID,ARG_NAME,ARG_ORDER),
  KEY MOD_RECIPE_LIB_ID (MOD_RECIPE_LIB_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_mod_recipe_arg.csv.tmp'
    INTO TABLE v4_lib_mod_recipe_arg
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (MOD_RECIPE_LIB_ID,ARG_NAME,ARG_ORDER,ARG_VALUE);


#
# Table structure for table 'v4_lib_pg_attribute'
#

DROP TABLE IF EXISTS v4_lib_pg_attribute;
CREATE TABLE v4_lib_pg_attribute (
  PG_ATTRIBUTE_LIB_ID varchar(100) binary NOT NULL default '',
  PG_NAME varchar(50) binary default NULL,
  ATTRIBUTE_NAME varchar(50) binary default NULL,
  ATTRIBUTE_TYPE varchar(50) binary default NULL,
  AGGREGATE_TYPE varchar(50) binary default NULL,
  UNIQUE KEY PK_V4_LIB_PG_ATTRIBUTE (PG_ATTRIBUTE_LIB_ID),
  KEY PG_NAME (PG_NAME)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_pg_attribute.csv.tmp'
    INTO TABLE v4_lib_pg_attribute
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (PG_ATTRIBUTE_LIB_ID,PG_NAME,ATTRIBUTE_NAME,ATTRIBUTE_TYPE,AGGREGATE_TYPE);


################################################################

#
# Table structure for table 'v6_cfw_alploc'
#

DROP TABLE IF EXISTS v6_cfw_alploc;
CREATE TABLE v6_cfw_alploc (
  CFW_ID varchar(50) NOT NULL default '',
  ALPLOC_CODE varchar(50) NOT NULL default '',
  LOCATION_NAME varchar(50) default NULL,
  LATITUDE decimal(68,30) default NULL,
  LONGITUDE decimal(68,30) default NULL,
  INSTALLATION_TYPE_CODE char(3) default NULL,
  PRIMARY KEY  (CFW_ID,ALPLOC_CODE),
  KEY CFW_ID (CFW_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_alploc.csv.tmp'
    INTO TABLE v6_cfw_alploc
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ALPLOC_CODE,LOCATION_NAME,LATITUDE,LONGITUDE,INSTALLATION_TYPE_CODE);

#
# Table structure for table 'v6_cfw_context_plugin_arg'
#

DROP TABLE IF EXISTS v6_cfw_context_plugin_arg;
CREATE TABLE v6_cfw_context_plugin_arg (
  CFW_ID varchar(50) binary NOT NULL default '',
  ORG_CONTEXT varchar(50) binary NOT NULL default '',
  PLUGIN_ARG_ID varchar(150) binary NOT NULL default '',
  PRIMARY KEY  (CFW_ID,ORG_CONTEXT,PLUGIN_ARG_ID),
  KEY PLUGIN_ARG_ID (PLUGIN_ARG_ID),
  KEY CFW_ID (CFW_ID),
  KEY ORG_CONTEXT (ORG_CONTEXT)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_context_plugin_arg.csv.tmp'
    INTO TABLE v6_cfw_context_plugin_arg
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ORG_CONTEXT,PLUGIN_ARG_ID);

#
# Table structure for table 'v6_cfw_group'
#

DROP TABLE IF EXISTS v6_cfw_group;
CREATE TABLE v6_cfw_group (
  CFW_GROUP_ID varchar(50) binary NOT NULL default '',
  DESCRIPTION varchar(100) binary default NULL,
  UNIQUE KEY PK_V6_CFW_GROUP (CFW_GROUP_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_group.csv.tmp'
    INTO TABLE v6_cfw_group
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_GROUP_ID,DESCRIPTION);


#
# Table structure for table 'v6_cfw_group_member'
#

DROP TABLE IF EXISTS v6_cfw_group_member;
CREATE TABLE v6_cfw_group_member (
  CFW_ID varchar(50) binary NOT NULL default '',
  CFW_GROUP_ID varchar(50) binary NOT NULL default '',
  PRIMARY KEY  (CFW_ID,CFW_GROUP_ID),
  KEY CFW_GROUP_ID (CFW_GROUP_ID),
  KEY CFW_ID (CFW_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_group_member.csv.tmp'
    INTO TABLE v6_cfw_group_member
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,CFW_GROUP_ID);

#
# Table structure for table 'v6_cfw_group_org'
#

DROP TABLE IF EXISTS v6_cfw_group_org;
CREATE TABLE v6_cfw_group_org (
  CFW_GROUP_ID varchar(50) binary NOT NULL default '',
  ORG_ID varchar(50) binary NOT NULL default '',
  PRIMARY KEY  (CFW_GROUP_ID,ORG_ID),
  KEY CFW_GROUP_ID (CFW_GROUP_ID),
  KEY ORG_ID (ORG_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_group_org.csv.tmp'
    INTO TABLE v6_cfw_group_org
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_GROUP_ID,ORG_ID);

#
# Table structure for table 'v6_cfw_instance'
#

DROP TABLE IF EXISTS v6_cfw_instance;
CREATE TABLE v6_cfw_instance (
  CFW_ID varchar(50) binary NOT NULL default '',
  DESCRIPTION varchar(200) binary default NULL,
  UNIQUE KEY PK_V6_CFW_INSTANCE (CFW_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_instance.csv.tmp'
    INTO TABLE v6_cfw_instance
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,DESCRIPTION);

#
# Table structure for table 'v6_cfw_oplan'
#

DROP TABLE IF EXISTS v6_cfw_oplan;
CREATE TABLE v6_cfw_oplan (
  CFW_ID varchar(50) binary NOT NULL default '',
  OPLAN_ID varchar(50) binary NOT NULL default '',
  OPERATION_NAME varchar(50) binary default NULL,
  PRIORITY varchar(50) binary default NULL,
  C0_DATE datetime default NULL,
  PRIMARY KEY  (CFW_ID,OPLAN_ID),
  KEY CFW_ID (CFW_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_oplan.csv.tmp'
    INTO TABLE v6_cfw_oplan
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,OPLAN_ID,OPERATION_NAME,PRIORITY,C0_DATE);

#
# Table structure for table 'v6_cfw_oplan_activity'
#

DROP TABLE IF EXISTS v6_cfw_oplan_activity;
CREATE TABLE v6_cfw_oplan_activity (
  CFW_ID varchar(50) binary NOT NULL default '',
  OPLAN_ID varchar(50) binary NOT NULL default '',
  ORG_GROUP_ID varchar(50) binary NOT NULL default '',
  START_CDAY decimal(68,30) NOT NULL default '0.000000000000000000000000000000',
  END_CDAY decimal(68,30) default NULL,
  OPTEMPO varchar(50) binary default NULL,
  ACTIVITY_TYPE varchar(50) binary default NULL,
  PRIMARY KEY  (CFW_ID,OPLAN_ID,ORG_GROUP_ID,START_CDAY),
  KEY CFW_AND_OPLAN_ID (CFW_ID,OPLAN_ID),
  KEY ORG_GROUP_ID (ORG_GROUP_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_oplan_activity.csv.tmp'
    INTO TABLE v6_cfw_oplan_activity
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,OPLAN_ID,ORG_GROUP_ID,START_CDAY,END_CDAY,OPTEMPO,ACTIVITY_TYPE);

#
# Table structure for table 'v6_cfw_oplan_loc'
#

DROP TABLE IF EXISTS v6_cfw_oplan_loc;
CREATE TABLE v6_cfw_oplan_loc (
  CFW_ID varchar(50) binary NOT NULL default '',
  OPLAN_ID varchar(50) binary NOT NULL default '',
  ORG_GROUP_ID varchar(50) binary NOT NULL default '',
  START_CDAY decimal(68,30) NOT NULL default '0.000000000000000000000000000000',
  END_CDAY decimal(68,30) default NULL,
  LOCATION_CODE varchar(50) binary default NULL,
  PRIMARY KEY  (CFW_ID,OPLAN_ID,ORG_GROUP_ID,START_CDAY),
  KEY ORG_GROUP_ID (ORG_GROUP_ID),
  KEY CFW_AND_OPLAN_ID (CFW_ID,OPLAN_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_oplan_loc.csv.tmp'
    INTO TABLE v6_cfw_oplan_loc
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,OPLAN_ID,ORG_GROUP_ID,START_CDAY,END_CDAY,LOCATION_CODE);

#
# Table structure for table 'v6_cfw_oplan_og_attr'
#

DROP TABLE IF EXISTS v6_cfw_oplan_og_attr;
CREATE TABLE v6_cfw_oplan_og_attr (
  CFW_ID varchar(50) binary NOT NULL default '',
  OPLAN_ID varchar(50) binary NOT NULL default '',
  ORG_GROUP_ID varchar(50) binary NOT NULL default '',
  START_CDAY decimal(68,30) NOT NULL default '0.000000000000000000000000000000',
  ATTRIBUTE_NAME varchar(50) binary NOT NULL default '',
  END_CDAY decimal(68,30) default NULL,
  ATTRIBUTE_VALUE varchar(50) binary default NULL,
  PRIMARY KEY  (CFW_ID,OPLAN_ID,ORG_GROUP_ID,START_CDAY,ATTRIBUTE_NAME),
  KEY ORG_GROUP_ID (ORG_GROUP_ID),
  KEY CFW_AND_OPLAN_ID (CFW_ID,OPLAN_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_oplan_og_attr.csv.tmp'
    INTO TABLE v6_cfw_oplan_og_attr
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,OPLAN_ID,ORG_GROUP_ID,START_CDAY,ATTRIBUTE_NAME,END_CDAY,ATTRIBUTE_VALUE);

#
# Table structure for table 'v6_cfw_org_group_org_member'
#

DROP TABLE IF EXISTS v6_cfw_org_group_org_member;
CREATE TABLE v6_cfw_org_group_org_member (
  CFW_ID varchar(50) binary NOT NULL default '',
  ORG_GROUP_ID varchar(50) binary NOT NULL default '',
  ORG_ID varchar(50) binary NOT NULL default '',
  PRIMARY KEY  (CFW_ID,ORG_GROUP_ID,ORG_ID),
  KEY ORG_GROUP_ID (ORG_GROUP_ID),
  KEY CFW_ID (CFW_ID),
  KEY ORG_ID (ORG_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_org_group_org_member.csv.tmp'
    INTO TABLE v6_cfw_org_group_org_member
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ORG_GROUP_ID,ORG_ID);

#
# Table structure for table 'v6_cfw_org_hierarchy'
#

DROP TABLE IF EXISTS v6_cfw_org_hierarchy;
CREATE TABLE v6_cfw_org_hierarchy (
  CFW_ID varchar(50) binary NOT NULL default '',
  ORG_ID varchar(50) binary NOT NULL default '',
  SUPERIOR_ORG_ID varchar(50) binary NOT NULL default '',
  PRIMARY KEY  (CFW_ID,ORG_ID),
  KEY CFW_ID (CFW_ID),
  KEY ORG_ID (ORG_ID),
  KEY SUPERIOR_ORG_ID (SUPERIOR_ORG_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_org_hierarchy.csv.tmp'
    INTO TABLE v6_cfw_org_hierarchy
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ORG_ID,SUPERIOR_ORG_ID);

#
# Table structure for table 'v6_cfw_org_list'
#

DROP TABLE IF EXISTS v6_cfw_org_list;
CREATE TABLE v6_cfw_org_list (
  CFW_ID varchar(50) binary NOT NULL default '',
  ORG_ID varchar(50) binary NOT NULL default '',
  PRIMARY KEY  (CFW_ID,ORG_ID),
  KEY ORG_ID (ORG_ID),
  KEY CFW_ID (CFW_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_org_list.csv.tmp'
    INTO TABLE v6_cfw_org_list
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ORG_ID);

#
# Table structure for table 'v6_cfw_org_og_relation'
#

DROP TABLE IF EXISTS v6_cfw_org_og_relation;
CREATE TABLE v6_cfw_org_og_relation (
  CFW_ID varchar(50) binary NOT NULL default '',
  ROLE varchar(50) binary NOT NULL default '',
  ORG_ID varchar(50) binary NOT NULL default '',
  ORG_GROUP_ID varchar(50) binary NOT NULL default '',
  START_DATE datetime NOT NULL default '0000-00-00 00:00:00',
  END_DATE datetime default NULL,
  RELATION_ORDER decimal(68,30) NOT NULL default '0.000000000000000000000000000000',
  PRIMARY KEY  (CFW_ID,ROLE,ORG_ID,ORG_GROUP_ID,START_DATE),
  KEY ORG_GROUP_ID (ORG_GROUP_ID),
  KEY ROLE (ROLE),
  KEY ORG_ID (ORG_ID),
  KEY CFW_ID (CFW_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_org_og_relation.csv.tmp'
    INTO TABLE v6_cfw_org_og_relation
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ROLE,ORG_ID,ORG_GROUP_ID,START_DATE,END_DATE,RELATION_ORDER);

#
# Table structure for table 'v6_cfw_org_orgtype'
#

DROP TABLE IF EXISTS v6_cfw_org_orgtype;
CREATE TABLE v6_cfw_org_orgtype (
  CFW_ID varchar(50) binary NOT NULL default '',
  ORG_ID varchar(50) binary NOT NULL default '',
  ORGTYPE_ID varchar(50) binary NOT NULL default '',
  PRIMARY KEY  (CFW_ID,ORG_ID,ORGTYPE_ID),
  KEY CFW_AND_ORG_ID (CFW_ID,ORG_ID),
  KEY CFW_ID (CFW_ID),
  KEY ORG_ID (ORG_ID),
  KEY ORGTYPE_ID (ORGTYPE_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_org_orgtype.csv.tmp'
    INTO TABLE v6_cfw_org_orgtype
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ORG_ID,ORGTYPE_ID);

#
# Table structure for table 'v6_cfw_org_pg_attr'
#

DROP TABLE IF EXISTS v6_cfw_org_pg_attr;
CREATE TABLE v6_cfw_org_pg_attr (
  CFW_ID varchar(50) binary NOT NULL default '',
  ORG_ID varchar(50) binary NOT NULL default '',
  PG_ATTRIBUTE_LIB_ID varchar(100) binary NOT NULL default '',
  ATTRIBUTE_VALUE varchar(100) binary NOT NULL default '',
  ATTRIBUTE_ORDER decimal(68,30) NOT NULL default '0.000000000000000000000000000000',
  START_DATE datetime NOT NULL default '0000-00-00 00:00:00',
  END_DATE datetime default NULL,
  PRIMARY KEY  (CFW_ID,ORG_ID,PG_ATTRIBUTE_LIB_ID,ATTRIBUTE_VALUE,ATTRIBUTE_ORDER,START_DATE),
  KEY CFW_AND_ORG_ID (CFW_ID,ORG_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_org_pg_attr.csv.tmp'
    INTO TABLE v6_cfw_org_pg_attr
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ORG_ID,PG_ATTRIBUTE_LIB_ID,ATTRIBUTE_VALUE,ATTRIBUTE_ORDER,START_DATE,END_DATE);

#
# Table structure for table 'v6_cfw_orgtype_plugin_grp'
#

DROP TABLE IF EXISTS v6_cfw_orgtype_plugin_grp;
CREATE TABLE v6_cfw_orgtype_plugin_grp (
  CFW_ID varchar(50) binary NOT NULL default '',
  ORGTYPE_ID varchar(50) binary NOT NULL default '',
  PLUGIN_GROUP_ID varchar(50) binary NOT NULL default '',
  PRIMARY KEY  (CFW_ID,ORGTYPE_ID,PLUGIN_GROUP_ID),
  KEY ORGTYPE_ID (ORGTYPE_ID),
  KEY CFW_ID (CFW_ID),
  KEY PLUGIN_GROUP_ID (PLUGIN_GROUP_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_orgtype_plugin_grp.csv.tmp'
    INTO TABLE v6_cfw_orgtype_plugin_grp
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,ORGTYPE_ID,PLUGIN_GROUP_ID);

#
# Table structure for table 'v6_cfw_plugin_group_member'
#

DROP TABLE IF EXISTS v6_cfw_plugin_group_member;
CREATE TABLE v6_cfw_plugin_group_member (
  CFW_ID varchar(50) binary NOT NULL default '',
  PLUGIN_GROUP_ID varchar(50) binary NOT NULL default '',
  PLUGIN_ID varchar(100) binary NOT NULL default '',
  PLUGIN_CLASS_ORDER decimal(68,30) default NULL,
  PRIMARY KEY  (CFW_ID,PLUGIN_GROUP_ID,PLUGIN_ID),
  KEY PLUGIN_ID (PLUGIN_ID),
  KEY PLUGIN_GROUP_ID (PLUGIN_GROUP_ID),
  KEY CFW_ID (CFW_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/cfw_plugin_group_member.csv.tmp'
    INTO TABLE v6_cfw_plugin_group_member
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (CFW_ID,PLUGIN_GROUP_ID,PLUGIN_ID,PLUGIN_CLASS_ORDER);

#
# Table structure for table 'v6_lib_org_group'
#

DROP TABLE IF EXISTS v6_lib_org_group;
CREATE TABLE v6_lib_org_group (
  ORG_GROUP_ID varchar(50) binary NOT NULL default '',
  DESCRIPTION varchar(50) binary default NULL,
  UNIQUE KEY PK_V6_CFW_ORG_GROUP (ORG_GROUP_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_org_group.csv.tmp'
    INTO TABLE v6_lib_org_group
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ORG_GROUP_ID,DESCRIPTION);

#
# Table structure for table 'v6_lib_organization'
#

DROP TABLE IF EXISTS v6_lib_organization;
CREATE TABLE v6_lib_organization (
  ORG_ID varchar(50) binary NOT NULL default '',
  ORG_NAME varchar(50) binary default NULL,
  UIC varchar(50) binary default NULL,
  UNIQUE KEY PK_V6_LIB_ORGANIZATION (ORG_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_organization.csv.tmp'
    INTO TABLE v6_lib_organization
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ORG_ID,ORG_NAME,UIC);

#
# Table structure for table 'v6_lib_orgtype_ref'
#

DROP TABLE IF EXISTS v6_lib_orgtype_ref;
CREATE TABLE v6_lib_orgtype_ref (
  ORGTYPE_ID varchar(50) binary NOT NULL default '',
  DESCRIPTION varchar(100) binary default NULL,
  UNIQUE KEY PK_V6_LIB_ORGTYPE_REF (ORGTYPE_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_orgtype_ref.csv.tmp'
    INTO TABLE v6_lib_orgtype_ref
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ORGTYPE_ID,DESCRIPTION);

#
# Table structure for table 'v6_lib_plugin_arg'
#

DROP TABLE IF EXISTS v6_lib_plugin_arg;
CREATE TABLE v6_lib_plugin_arg (
  PLUGIN_ARG_ID varchar(150) binary NOT NULL default '',
  ARGUMENT_ORDER decimal(68,30) NOT NULL default '0.000000000000000000000000000000',
  PLUGIN_ID varchar(100) binary NOT NULL default '',
  ARGUMENT varchar(100) binary NOT NULL default '',
  ARGUMENT_TYPE varchar(50) binary default NULL,
  UNIQUE KEY PK_V6_LIB_PLUGIN_ARG (PLUGIN_ARG_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_plugin_arg.csv.tmp'
    INTO TABLE v6_lib_plugin_arg
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (PLUGIN_ARG_ID,ARGUMENT_ORDER,PLUGIN_ID,ARGUMENT,ARGUMENT_TYPE);

#
# Table structure for table 'v6_lib_plugin_arg_thread'
#

DROP TABLE IF EXISTS v6_lib_plugin_arg_thread;
CREATE TABLE v6_lib_plugin_arg_thread (
  PLUGIN_ARG_ID varchar(150) binary NOT NULL default '',
  THREAD_ID varchar(50) binary NOT NULL default '',
  PRIMARY KEY  (PLUGIN_ARG_ID,THREAD_ID),
  KEY THREAD_ID (THREAD_ID),
  KEY PLUGIN_ARG_ID (PLUGIN_ARG_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_plugin_arg_thread.csv.tmp'
    INTO TABLE v6_lib_plugin_arg_thread
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (PLUGIN_ARG_ID,THREAD_ID);

#
# Table structure for table 'v6_lib_plugin_group'
#

DROP TABLE IF EXISTS v6_lib_plugin_group;
CREATE TABLE v6_lib_plugin_group (
  PLUGIN_GROUP_ID varchar(50) binary NOT NULL default '',
  PLUGIN_GROUP_ORDER decimal(68,30) default NULL,
  DESCRIPTION varchar(100) binary default NULL,
  UNIQUE KEY PK_V6_LIB_PLUGIN_GROUP (PLUGIN_GROUP_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_plugin_group.csv.tmp'
    INTO TABLE v6_lib_plugin_group
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (PLUGIN_GROUP_ID,PLUGIN_GROUP_ORDER,DESCRIPTION);

#
# Table structure for table 'v6_lib_plugin_ref'
#

DROP TABLE IF EXISTS v6_lib_plugin_ref;
CREATE TABLE v6_lib_plugin_ref (
  PLUGIN_ID varchar(100) binary NOT NULL default '',
  PLUGIN_CLASS varchar(100) binary default NULL,
  DESCRIPTION varchar(100) binary default NULL,
  UNIQUE KEY PK_V6_LIB_PLUGIN_REF (PLUGIN_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_plugin_ref.csv.tmp'
    INTO TABLE v6_lib_plugin_ref
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (PLUGIN_ID,PLUGIN_CLASS,DESCRIPTION);

#
# Table structure for table 'v6_lib_plugin_thread'
#

DROP TABLE IF EXISTS v6_lib_plugin_thread;
CREATE TABLE v6_lib_plugin_thread (
  PLUGIN_ID varchar(100) binary NOT NULL default '',
  THREAD_ID varchar(50) binary NOT NULL default '',
  DESCRIPTION varchar(100) binary default NULL,
  PRIMARY KEY  (PLUGIN_ID,THREAD_ID),
  KEY PLUGIN_ID (PLUGIN_ID),
  KEY THREAD_ID (THREAD_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_plugin_thread.csv.tmp'
    INTO TABLE v6_lib_plugin_thread
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (PLUGIN_ID,THREAD_ID,DESCRIPTION);

#
# Table structure for table 'v6_lib_role_ref'
#

DROP TABLE IF EXISTS v6_lib_role_ref;
CREATE TABLE v6_lib_role_ref (
  ROLE varchar(50) binary NOT NULL default '',
  DESCRIPTION varchar(100) binary default NULL,
  UNIQUE KEY PK_V6_LIB_ROLE_REF (ROLE)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_role_ref.csv.tmp'
    INTO TABLE v6_lib_role_ref
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ROLE,DESCRIPTION);

#
# Table structure for table 'v6_lib_role_thread'
#

DROP TABLE IF EXISTS v6_lib_role_thread;
CREATE TABLE v6_lib_role_thread (
  ROLE varchar(50) binary NOT NULL default '',
  THREAD_ID varchar(50) binary NOT NULL default '',
  DESCRIPTION varchar(100) binary default NULL,
  PRIMARY KEY  (ROLE,THREAD_ID),
  KEY ROLE (ROLE),
  KEY THREAD_ID (THREAD_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_role_thread.csv.tmp'
    INTO TABLE v6_lib_role_thread
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ROLE,THREAD_ID,DESCRIPTION);

#
# Table structure for table 'v6_lib_thread'
#

DROP TABLE IF EXISTS v6_lib_thread;
CREATE TABLE v6_lib_thread (
  THREAD_ID varchar(50) binary NOT NULL default '',
  DESCRIPTION varchar(100) binary default NULL,
  UNIQUE KEY PK_V6_LIB_THREAD (THREAD_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/lib_thread.csv.tmp'
    INTO TABLE v6_lib_thread
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (THREAD_ID,DESCRIPTION);

#
# Table structure for table 'v6_oplan_activity'
#

DROP TABLE IF EXISTS v6_oplan_activity;
CREATE TABLE v6_oplan_activity (
  ORG_GROUP_ID varchar(50) binary default NULL,
  ACTIVITY_TYPE varchar(50) binary default NULL,
  OPTEMPO varchar(50) binary default NULL,
  START_CDAY decimal(68,30) default NULL,
  END_CDAY decimal(68,30) default NULL
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/oplan_activity.csv.tmp'
    INTO TABLE v6_oplan_activity
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ORG_GROUP_ID,ACTIVITY_TYPE,OPTEMPO,START_CDAY,END_CDAY);

#
# Table structure for table 'v6_oplan_loc'
#

DROP TABLE IF EXISTS v6_oplan_loc;
CREATE TABLE v6_oplan_loc (
  ORG_GROUP_ID varchar(50) binary default NULL,
  LOCATION_CODE varchar(50) binary default NULL,
  START_CDAY decimal(68,30) default NULL,
  END_CDAY decimal(68,30) default NULL
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/oplan_loc.csv.tmp'
    INTO TABLE v6_oplan_loc
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ORG_GROUP_ID,LOCATION_CODE,START_CDAY,END_CDAY);

#
# Table structure for table 'community_entity_attribute'
#

DROP TABLE IF EXISTS community_entity_attribute;
CREATE TABLE community_entity_attribute (
  ASSEMBLY_ID varchar(50) binary NOT NULL default '',
  COMMUNITY_ID varchar(100) binary NOT NULL default '',
  ENTITY_ID varchar(100) binary NOT NULL default '',
  ATTRIBUTE_ID varchar(100) binary NOT NULL default '',
  ATTRIBUTE_VALUE varchar(100) binary NOT NULL default '',
  PRIMARY KEY (ASSEMBLY_ID,COMMUNITY_ID,ENTITY_ID,ATTRIBUTE_ID,ATTRIBUTE_VALUE),
  KEY ASSEMBLY_ID (ASSEMBLY_ID),
  KEY COMMUNITY_ID (COMMUNITY_ID),
  KEY ENTITY_ID (ENTITY_ID),
  KEY ATTRIBUTE_ID (ATTRIBUTE_ID),
  KEY ATTRIBUTE_VALUE (ATTRIBUTE_VALUE)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/community_entity_attribute.csv.tmp'
    INTO TABLE community_entity_attribute
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,COMMUNITY_ID,ENTITY_ID,ATTRIBUTE_ID,ATTRIBUTE_VALUE);

#
# Table structure for table 'community_attribute'
#

DROP TABLE IF EXISTS community_attribute;
CREATE TABLE community_attribute (
  ASSEMBLY_ID varchar(50) binary NOT NULL default '',
  COMMUNITY_ID varchar(100) binary NOT NULL default '',
  ATTRIBUTE_ID varchar(100) binary NOT NULL default '',
  ATTRIBUTE_VALUE varchar(100) binary NOT NULL default '',
  PRIMARY KEY (ASSEMBLY_ID,COMMUNITY_ID,ATTRIBUTE_ID,ATTRIBUTE_VALUE),
  KEY ASSEMBLY_ID (ASSEMBLY_ID),
  KEY COMMUNITY_ID (COMMUNITY_ID),
  KEY ATTRIBUTE_ID (ATTRIBUTE_ID),
  KEY ATTRIBUTE_VALUE (ATTRIBUTE_VALUE)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/csv/community_attribute.csv.tmp'
    INTO TABLE community_attribute
    FIELDS TERMINATED BY ','
    OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ASSEMBLY_ID,COMMUNITY_ID,ATTRIBUTE_ID,ATTRIBUTE_VALUE);
