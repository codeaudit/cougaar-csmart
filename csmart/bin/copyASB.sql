-- COPY AN EXPERIMENT WITH A NEW NAME,
-- COPYING EVERYTHING SO CAN EDIT THINGS IN PLACE
-- NOTE THAT LIBRARY COMPONENTS ARE NOT COPIED
-- THIS LEAVES A TEMPORARY DATABASE IN PLACE,
-- FOR LATER EXPORT IF DESIRED
-- BE SURE TO DELETE IT LATER
CREATE DATABASE tempcopy;

-- COPY ROWS FROM v4_asb_component_hierarchy
DROP TABLE IF EXISTS tempcopy.v4_asb_component_hierarchy;

CREATE TABLE tempcopy.v4_asb_component_hierarchy AS
  SELECT DISTINCT 
    CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
    AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AA.PARENT_COMPONENT_ALIB_ID AS PARENT_COMPONENT_ALIB_ID, 
    AA.INSERTION_ORDER AS INSERTION_ORDER 
   FROM 
     v4_asb_component_hierarchy AA,
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_assembly EA
    WHERE
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

REPLACE INTO tempcopy.v4_asb_component_hierarchy
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, INSERTION_ORDER) 
  SELECT DISTINCT 
    CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
    AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AA.PARENT_COMPONENT_ALIB_ID AS PARENT_COMPONENT_ALIB_ID, 
    AA.INSERTION_ORDER AS INSERTION_ORDER 
   FROM 
     v4_asb_component_hierarchy AA,
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_config_assembly EA
    WHERE
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

-- update the new table to replace old expt name in parent_component_alib_id with concat('old name','-suffix')
UPDATE tempcopy.v4_asb_component_hierarchy 
	SET PARENT_COMPONENT_ALIB_ID = 'society|:oldExpt-:suffix' 
	WHERE PARENT_COMPONENT_ALIB_ID = 'society|:oldExpt';

INSERT INTO  v4_asb_component_hierarchy 
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, INSERTION_ORDER) 
SELECT DISTINCT * FROM tempcopy.v4_asb_component_hierarchy;

-- v4_asb_agent
DROP TABLE IF EXISTS tempcopy.v4_asb_agent;

CREATE TABLE tempcopy.v4_asb_agent AS
  SELECT DISTINCT 
    CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
    AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AA.COMPONENT_LIB_ID AS COMPONENT_LIB_ID, 
    AA.CLONE_SET_ID AS CLONE_SET_ID, 
    AA.COMPONENT_NAME AS COMPONENT_NAME 
   FROM 
     v4_asb_agent AA,
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_assembly EA
    WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

REPLACE INTO  tempcopy.v4_asb_agent 
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME) 
  SELECT DISTINCT 
    CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
    AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AA.COMPONENT_LIB_ID AS COMPONENT_LIB_ID, 
    AA.CLONE_SET_ID AS CLONE_SET_ID, 
    AA.COMPONENT_NAME AS COMPONENT_NAME 
   FROM 
     v4_asb_agent AA,
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_config_assembly EA
    WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

INSERT INTO  v4_asb_agent 
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME) 
SELECT DISTINCT * FROM tempcopy.v4_asb_agent;

-- v4_asb_agent_pg_attr
DROP TABLE IF EXISTS tempcopy.v4_asb_agent_pg_attr;

CREATE TABLE tempcopy.v4_asb_agent_pg_attr AS
  SELECT DISTINCT 
    CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
    AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AA.PG_ATTRIBUTE_LIB_ID AS PG_ATTRIBUTE_LIB_ID, 
    AA.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE, 
    AA.ATTRIBUTE_ORDER AS ATTRIBUTE_ORDER, 
    AA.START_DATE AS START_DATE, 
    AA.END_DATE AS END_DATE 
  FROM  v4_asb_agent_pg_attr AA,
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_assembly EA
  WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

REPLACE INTO tempcopy.v4_asb_agent_pg_attr 
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE, END_DATE) 
  SELECT DISTINCT 
    CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
    AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AA.PG_ATTRIBUTE_LIB_ID AS PG_ATTRIBUTE_LIB_ID, 
    AA.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE, 
    AA.ATTRIBUTE_ORDER AS ATTRIBUTE_ORDER, 
    AA.START_DATE AS START_DATE, 
    AA.END_DATE AS END_DATE 
  FROM  v4_asb_agent_pg_attr AA,
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_config_assembly EA
  WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

INSERT INTO v4_asb_agent_pg_attr 
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE, END_DATE) 
SELECT DISTINCT * FROM tempcopy.v4_asb_agent_pg_attr;

-- v4_asb_agent_relation
DROP TABLE IF EXISTS tempcopy.v4_asb_agent_relation;

CREATE TABLE tempcopy.v4_asb_agent_relation AS
 SELECT DISTINCT 
  CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
  AA.ROLE AS ROLE, 
  AA.SUPPORTING_COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, 
  AA.SUPPORTED_COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, 
  AA.START_DATE AS START_DATE, 
  AA.END_DATE AS END_DATE 
 FROM v4_asb_agent_relation AA, 
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_assembly EA
 WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

REPLACE INTO tempcopy.v4_asb_agent_relation 
    (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE) 
 SELECT DISTINCT 
  CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
  AA.ROLE AS ROLE, 
  AA.SUPPORTING_COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, 
  AA.SUPPORTED_COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, 
  AA.START_DATE AS START_DATE, 
  AA.END_DATE AS END_DATE 
 FROM v4_asb_agent_relation AA, 
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_config_assembly EA
 WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

INSERT INTO v4_asb_agent_relation 
    (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE) 
SELECT DISTINCT * FROM tempcopy.v4_asb_agent_relation;

-- v4_asb_component_arg
DROP TABLE IF EXISTS tempcopy.v4_asb_component_arg;

CREATE TABLE tempcopy.v4_asb_component_arg AS
 SELECT DISTINCT 
  CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
  AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
  AA.ARGUMENT AS ARGUMENT, 
  AA.ARGUMENT_ORDER AS ARGUMENT_ORDER 
  FROM v4_asb_component_arg AA, 
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_assembly EA
 WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

REPLACE INTO tempcopy.v4_asb_component_arg 
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER)
 SELECT DISTINCT 
  CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
  AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
  AA.ARGUMENT AS ARGUMENT, 
  AA.ARGUMENT_ORDER AS ARGUMENT_ORDER 
  FROM v4_asb_component_arg AA, 
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_config_assembly EA
 WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

UPDATE tempcopy.v4_asb_component_arg 
	SET COMPONENT_ALIB_ID = 'society|:oldExpt-:suffix' 
	WHERE COMPONENT_ALIB_ID = 'society|:oldExpt';

-- Must get the old Experiment ID - another sed replacement
UPDATE tempcopy.v4_asb_component_arg 
	SET ARGUMENT = '-Dorg.cougaar.experiment.id=:oldeid-:suffix.TRIAL' 
	WHERE ARGUMENT LIKE '-Dorg.cougaar.experiment.id=%';

INSERT INTO v4_asb_component_arg 
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER)
SELECT DISTINCT * FROM tempcopy.v4_asb_component_arg;

-- v4_asb_oplan_agent_attr
DROP TABLE IF EXISTS tempcopy.v4_asb_oplan_agent_attr;

CREATE TABLE tempcopy.v4_asb_oplan_agent_attr AS
 SELECT DISTINCT 
  CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
  AA.OPLAN_ID AS OPLAN_ID, 
  AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
  AA.COMPONENT_ID AS COMPONENT_ID, 
  AA.START_CDAY AS START_CDAY, 
  AA.ATTRIBUTE_NAME AS ATTRIBUTE_NAME,
  AA.END_CDAY AS END_CDAY, 
  AA.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE 
FROM v4_asb_oplan_agent_attr AA, 
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_assembly EA
 WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

REPLACE INTO tempcopy.v4_asb_oplan_agent_attr
 (ASSEMBLY_ID, OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME, END_CDAY, ATTRIBUTE_VALUE)
 SELECT DISTINCT 
  CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
  AA.OPLAN_ID AS OPLAN_ID, 
  AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
  AA.COMPONENT_ID AS COMPONENT_ID, 
  AA.START_CDAY AS START_CDAY, 
  AA.ATTRIBUTE_NAME AS ATTRIBUTE_NAME,
  AA.END_CDAY AS END_CDAY, 
  AA.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE 
FROM v4_asb_oplan_agent_attr AA, 
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_config_assembly EA
 WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

INSERT INTO v4_asb_oplan_agent_attr 
    (ASSEMBLY_ID, OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME , END_CDAY, ATTRIBUTE_VALUE)
SELECT DISTINCT * FROM tempcopy.v4_asb_oplan_agent_attr;

-- v4_asb_oplan
DROP TABLE IF EXISTS tempcopy.v4_asb_oplan;

CREATE TABLE tempcopy.v4_asb_oplan AS
SELECT DISTINCT 
 CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
 AA.OPLAN_ID AS OPLAN_ID, 
 AA.OPERATION_NAME AS OPERATION_NAME,
 AA.PRIORITY AS PRIORITY, 
 AA.C0_DATE AS C0_DATE 
FROM v4_asb_oplan AA, 
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_assembly EA
 WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

REPLACE INTO tempcopy.v4_asb_oplan
 (ASSEMBLY_ID, OPLAN_ID, OPERATION_NAME, PRIORITY, C0_DATE)
SELECT DISTINCT 
 CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
 AA.OPLAN_ID AS OPLAN_ID, 
 AA.OPERATION_NAME AS OPERATION_NAME,
 AA.PRIORITY AS PRIORITY, 
 AA.C0_DATE AS C0_DATE 
FROM v4_asb_oplan AA, 
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_config_assembly EA
 WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

INSERT INTO v4_asb_oplan 
   (ASSEMBLY_ID, OPLAN_ID, OPERATION_NAME , PRIORITY, C0_DATE) 
SELECT DISTINCT * FROM tempcopy.v4_asb_oplan;

-- v4_asb_assembly
DROP TABLE IF EXISTS tempcopy.v4_asb_assembly;

CREATE TABLE tempcopy.v4_asb_assembly AS
SELECT DISTINCT 
 CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
 AA.ASSEMBLY_TYPE AS ASSEMBLY_TYPE, 
 concat(AA.DESCRIPTION,' Modified for :suffix') AS DESCRIPTION 
FROM v4_asb_assembly AA, 
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_assembly EA
 WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

REPLACE INTO tempcopy.v4_asb_assembly 
   (ASSEMBLY_ID,ASSEMBLY_TYPE,DESCRIPTION)
SELECT DISTINCT 
 CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
 AA.ASSEMBLY_TYPE AS ASSEMBLY_TYPE, 
 concat(AA.DESCRIPTION,' Modified for :suffix') AS DESCRIPTION 
FROM v4_asb_assembly AA, 
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_config_assembly EA
 WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

INSERT INTO v4_asb_assembly 
   (ASSEMBLY_ID,ASSEMBLY_TYPE,DESCRIPTION)
SELECT DISTINCT * FROM tempcopy.v4_asb_assembly;

-- v4_trial_mod_recipe
DROP TABLE IF EXISTS tempcopy.v4_expt_trial_mod_recipe;

CREATE TABLE tempcopy.v4_expt_trial_mod_recipe AS
  SELECT DISTINCT
   CONCAT(CONCAT(E.EXPT_ID,'-:suffix'),'.TRIAL') AS TRIAL_ID, 
    AA.MOD_RECIPE_LIB_ID AS MOD_RECIPE_LIB_ID,
    AA.RECIPE_ORDER,
    CONCAT(E.EXPT_ID,'-:suffix') AS EXPT_ID
  FROM
    v4_expt_trial_mod_recipe AA,
     v4_expt_experiment E,
     v4_expt_trial ET
  WHERE
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = AA.TRIAL_ID
      AND E.NAME = ':oldExpt';

INSERT INTO v4_expt_trial_mod_recipe
(TRIAL_ID, MOD_RECIPE_LIB_ID, RECIPE_ORDER, EXPT_ID)
SELECT * FROM tempcopy.v4_expt_trial_mod_recipe;

-- v4_expt_experiment
DROP TABLE IF EXISTS tempcopy.v4_expt_experiment;

CREATE TABLE tempcopy.v4_expt_experiment AS
  SELECT DISTINCT
   CONCAT(VEE.EXPT_ID,'-:suffix') AS EXPT_ID, 
   CONCAT(':oldExpt','-:suffix a copy') AS DESCRIPTION, 
   CONCAT(':oldExpt','-:suffix') AS NAME, 
   VEE.CFW_GROUP_ID  AS CFW_GROUP_ID
  FROM 
   v4_expt_experiment VEE 
  WHERE 
  VEE.NAME =':oldExpt';

INSERT INTO  v4_expt_experiment 
   (EXPT_ID, DESCRIPTION, NAME, CFW_GROUP_ID) 
SELECT * FROM tempcopy.v4_expt_experiment;

-- v4_expt_trial_thread
DROP TABLE IF EXISTS tempcopy.v4_expt_trial_thread;

CREATE TABLE tempcopy.v4_expt_trial_thread AS
  SELECT DISTINCT
   CONCAT(VE.EXPT_ID,'-:suffix') AS EXPT_ID, 
   CONCAT(CONCAT(VE.EXPT_ID,'-:suffix'),'.TRIAL') AS TRIAL_ID, 
   THREAD_ID AS THREAD_ID
  FROM 
   v4_expt_trial_thread VETT,
   v4_expt_trial VT,
   v4_expt_experiment VE
  WHERE VETT.TRIAL_ID = VT.TRIAL_ID AND VT.EXPT_ID = VE.EXPT_ID AND VE.NAME = ':oldExpt';

 INSERT INTO  v4_expt_trial_thread 
   (EXPT_ID, TRIAL_ID, THREAD_ID) 
SELECT * FROM tempcopy.v4_expt_trial_thread;

-- v4_expt_trial_org_mult
DROP TABLE IF EXISTS tempcopy.v4_expt_trial_org_mult;

CREATE TABLE tempcopy.v4_expt_trial_org_mult AS
SELECT DISTINCT
   CONCAT(VE.EXPT_ID,'-:suffix') AS EXPT_ID, 
   CONCAT(CONCAT(VE.EXPT_ID,'-:suffix'),'.TRIAL') AS  TRIAL_ID, 
   CFW_ID AS CFW_ID, 
   ORG_GROUP_ID AS  ORG_GROUP_ID, 
   MULTIPLIER AS  MULTIPLIER, 
   VETOM.DESCRIPTION AS DESCRIPTION 
  FROM 
   v4_expt_trial_org_mult VETOM, v4_expt_trial VT, v4_expt_experiment VE 
  WHERE 
   VETOM.TRIAL_ID = VT.TRIAL_ID AND VT.EXPT_ID = VE.EXPT_ID AND VE.NAME = ':oldExpt';

INSERT INTO  v4_expt_trial_org_mult 
   (EXPT_ID,TRIAL_ID, CFW_ID , ORG_GROUP_ID, MULTIPLIER, DESCRIPTION) 
SELECT * FROM tempcopy.v4_expt_trial_org_mult;

-- v4_expt_trial_assembly
DROP TABLE IF EXISTS tempcopy.v4_expt_trial_assembly;

CREATE TABLE tempcopy.v4_expt_trial_assembly AS
 SELECT DISTINCT
   CONCAT(CONCAT(VE.EXPT_ID,'-:suffix'),'.TRIAL') AS TRIAL_ID, 
   CONCAT(TA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID,
   CONCAT(VE.EXPT_ID,'-:suffix') AS EXPT_ID,
   CONCAT(TA.DESCRIPTION, ' copy') AS DESCRIPTION
 FROM 
   v4_expt_trial_assembly TA, v4_expt_trial VT, v4_expt_experiment VE 
 WHERE 
    TA.TRIAL_ID= VT.TRIAL_ID AND VT.EXPT_ID = VE.EXPT_ID AND VE.NAME = ':oldExpt' ;

INSERT INTO  v4_expt_trial_assembly 
   (TRIAL_ID,ASSEMBLY_ID,EXPT_ID,DESCRIPTION) 
SELECT * FROM tempcopy.v4_expt_trial_assembly;

-- v4_expt_trial_config_assembly
DROP TABLE IF EXISTS tempcopy.v4_expt_trial_config_assembly;

CREATE TABLE tempcopy.v4_expt_trial_config_assembly AS
 SELECT DISTINCT
   CONCAT(CONCAT(VE.EXPT_ID,'-:suffix'),'.TRIAL') AS TRIAL_ID, 
   CONCAT(TA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID,
   CONCAT(VE.EXPT_ID,'-:suffix') AS EXPT_ID,
   CONCAT(TA.DESCRIPTION, ' copy') AS DESCRIPTION
 FROM 
   v4_expt_trial_config_assembly TA, v4_expt_trial VT, v4_expt_experiment VE 
 WHERE 
    TA.TRIAL_ID= VT.TRIAL_ID AND VT.EXPT_ID = VE.EXPT_ID AND VE.NAME = ':oldExpt' ;

INSERT INTO  v4_expt_trial_config_assembly 
   (TRIAL_ID,ASSEMBLY_ID,EXPT_ID,DESCRIPTION) 
SELECT * FROM tempcopy.v4_expt_trial_config_assembly;

-- v4_expt_trial
DROP TABLE IF EXISTS tempcopy.v4_expt_trial;

CREATE TABLE tempcopy.v4_expt_trial AS 
SELECT DISTINCT
  CONCAT(CONCAT(VE.EXPT_ID,'-:suffix'),'.TRIAL') AS TRIAL_ID,
  CONCAT(VE.EXPT_ID, '-:suffix') AS EXPT_ID,
  CONCAT(VE.DESCRIPTION, '-:suffix a copy') AS DESCRIPTION,
  CONCAT(VE.DESCRIPTION, '-:suffix a copy') AS NAME
FROM v4_expt_experiment VE
WHERE VE.NAME = ':oldExpt';

INSERT INTO  v4_expt_trial 
   (TRIAL_ID, EXPT_ID, DESCRIPTION, NAME)
SELECT * FROM tempcopy.v4_expt_trial;

-- Add new entry in v4_alib_component for the society
DROP TABLE IF EXISTS tempcopy.v4_alib_component;

CREATE TABLE tempcopy.v4_alib_component AS
 SELECT DISTINCT 'society|:oldExpt-:suffix' AS COMPONENT_ALIB_ID, 
     CONCAT(A.COMPONENT_NAME, '-:suffix') AS COMPONENT_NAME, 
     'society|:oldExpt-:suffix' AS COMPONENT_LIB_ID, 
     A.COMPONENT_TYPE, A.CLONE_SET_ID 
 FROM v4_alib_component A
 WHERE A.COMPONENT_ALIB_ID = 'society|:oldExpt';

INSERT INTO v4_alib_component
    (COMPONENT_ALIB_ID, COMPONENT_NAME, COMPONENT_LIB_ID, COMPONENT_TYPE, CLONE_SET_ID) 
 SELECT * FROM tempcopy.v4_alib_component;

-- Add new entry in v4_lib_component for the society
DROP TABLE IF EXISTS tempcopy.v4_lib_component;

CREATE TABLE tempcopy.v4_lib_component AS 
 SELECT DISTINCT 'society|:oldExpt-:suffix' AS COMPONENT_LIB_ID,
	A.COMPONENT_TYPE, A.COMPONENT_CLASS, A.INSERTION_POINT, 
	CONCAT(A.DESCRIPTION, '-:suffix') AS DESCRIPTION
 FROM v4_lib_component A
 WHERE A.COMPONENT_LIB_ID = 'society|:oldExpt';

INSERT INTO v4_lib_component
 (COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION)
 SELECT * FROM tempcopy.v4_lib_component;

-- THESE TABLES NECESSARY FOR EXPORT ONLY
-- v4_lib_component
-- v4_alib_component
-- v4_lib_mod_recipe
-- v4_lib_mod_recipe_arg

-- REMEMBER TO DROP THE OTHER DB
-- DROP DATABASE tempcopy;

