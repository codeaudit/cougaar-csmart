-- COPY AN EXPERIMENT WITH A NEW NAME,
-- COPYING EVERYTHING SO CAN EDIT THINGS IN PLACE
-- NOTE THAT LIBRARY COMPONENTS ARE NOT COPIED
-- THIS LEAVES A TEMPORARY DATABASE IN PLACE,
-- FOR LATER EXPORT IF DESIRED
-- BE SURE TO DELETE IT LATER
CREATE DATABASE tempcopy;

-- COPY ROWS FROM V4_ASB_COMPONENT_HIERARCHY
DROP TABLE IF EXISTS tempcopy.V4_ASB_COMPONENT_HIERARCHY;

CREATE TABLE tempcopy.V4_ASB_COMPONENT_HIERARCHY AS
  SELECT DISTINCT 
    CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
    AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AA.PARENT_COMPONENT_ALIB_ID AS PARENT_COMPONENT_ALIB_ID, 
    AA.INSERTION_ORDER AS INSERTION_ORDER 
   FROM 
     V4_ASB_COMPONENT_HIERARCHY AA,
     V4_EXPT_EXPERIMENT E,
     V4_EXPT_TRIAL ET,
     V4_EXPT_TRIAL_ASSEMBLY EA
    WHERE
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

-- update the new table to replace old expt name in parent_component_alib_id with concat('old name','-suffix')
UPDATE tempcopy.V4_ASB_COMPONENT_HIERARCHY 
	SET PARENT_COMPONENT_ALIB_ID = 'society|:oldExpt-:suffix' 
	WHERE PARENT_COMPONENT_ALIB_ID = 'society|:oldExpt';

INSERT INTO  V4_ASB_COMPONENT_HIERARCHY 
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, INSERTION_ORDER) 
SELECT * FROM tempcopy.V4_ASB_COMPONENT_HIERARCHY;

-- V4_ASB_AGENT
DROP TABLE IF EXISTS tempcopy.V4_ASB_AGENT;

CREATE TABLE tempcopy.V4_ASB_AGENT AS
  SELECT DISTINCT 
    CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
    AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AA.COMPONENT_LIB_ID AS COMPONENT_LIB_ID, 
    AA.CLONE_SET_ID AS CLONE_SET_ID, 
    AA.COMPONENT_NAME AS COMPONENT_NAME 
   FROM 
     V4_ASB_AGENT AA,
     V4_EXPT_EXPERIMENT E,
     V4_EXPT_TRIAL ET,
     V4_EXPT_TRIAL_ASSEMBLY EA
    WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

INSERT INTO  V4_ASB_AGENT 
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME) 
SELECT * FROM tempcopy.V4_ASB_AGENT;

-- V4_ASB_AGENT_PG_ATTR
DROP TABLE IF EXISTS tempcopy.V4_ASB_AGENT_PG_ATTR;

CREATE TABLE tempcopy.V4_ASB_AGENT_PG_ATTR AS
  SELECT DISTINCT 
    CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
    AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AA.PG_ATTRIBUTE_LIB_ID AS PG_ATTRIBUTE_LIB_ID, 
    AA.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE, 
    AA.ATTRIBUTE_ORDER AS ATTRIBUTE_ORDER, 
    AA.START_DATE AS START_DATE, 
    AA.END_DATE AS END_DATE 
  FROM  V4_ASB_AGENT_PG_ATTR AA,
     V4_EXPT_EXPERIMENT E,
     V4_EXPT_TRIAL ET,
     V4_EXPT_TRIAL_ASSEMBLY EA
  WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

INSERT INTO V4_ASB_AGENT_PG_ATTR 
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE, END_DATE) 
SELECT * FROM tempcopy.V4_ASB_AGENT_PG_ATTR;

-- V4_ASB_AGENT_RELATION
DROP TABLE IF EXISTS tempcopy.V4_ASB_AGENT_RELATION;

CREATE TABLE tempcopy.V4_ASB_AGENT_RELATION AS
 SELECT DISTINCT 
  CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
  AA.ROLE AS ROLE, 
  AA.SUPPORTING_COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, 
  AA.SUPPORTED_COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, 
  AA.START_DATE AS START_DATE, 
  AA.END_DATE AS END_DATE 
 FROM V4_ASB_AGENT_RELATION AA, 
     V4_EXPT_EXPERIMENT E,
     V4_EXPT_TRIAL ET,
     V4_EXPT_TRIAL_ASSEMBLY EA
 WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

INSERT INTO V4_ASB_AGENT_RELATION 
    (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE) 
SELECT * FROM tempcopy.V4_ASB_AGENT_RELATION;

-- V4_ASB_COMPONENT_ARG
DROP TABLE IF EXISTS tempcopy.V4_ASB_COMPONENT_ARG;

CREATE TABLE tempcopy.V4_ASB_COMPONENT_ARG AS
 SELECT DISTINCT 
  CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
  AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
  AA.ARGUMENT AS ARGUMENT, 
  AA.ARGUMENT_ORDER AS ARGUMENT_ORDER 
  FROM V4_ASB_COMPONENT_ARG AA, 
     V4_EXPT_EXPERIMENT E,
     V4_EXPT_TRIAL ET,
     V4_EXPT_TRIAL_ASSEMBLY EA
 WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

UPDATE tempcopy.V4_ASB_COMPONENT_ARG 
	SET COMPONENT_ALIB_ID = 'society|:oldExpt-:suffix' 
	WHERE COMPONENT_ALIB_ID = 'society|:oldExpt';

-- Must get the old Experiment ID - another sed replacement
UPDATE tempcopy.V4_ASB_COMPONENT_ARG 
	SET ARGUMENT = '-Dorg.cougaar.experiment.id=:oldeid-:suffix.TRIAL' 
	WHERE ARGUMENT LIKE '-Dorg.cougaar.experiment.id=%';

INSERT INTO V4_ASB_COMPONENT_ARG 
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER)
SELECT * FROM tempcopy.V4_ASB_COMPONENT_ARG;

-- V4_ASB_OPLAN_AGENT_ATTR
DROP TABLE IF EXISTS tempcopy.V4_ASB_OPLAN_AGENT_ATTR;

CREATE TABLE tempcopy.V4_ASB_OPLAN_AGENT_ATTR AS
 SELECT DISTINCT 
  CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
  AA.OPLAN_ID AS OPLAN_ID, 
  AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
  AA.COMPONENT_ID AS COMPONENT_ID, 
  AA.START_CDAY AS START_CDAY, 
  AA.ATTRIBUTE_NAME AS ATTRIBUTE_NAME,
  AA.END_CDAY AS END_CDAY, 
  AA.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE 
FROM V4_ASB_OPLAN_AGENT_ATTR AA, 
     V4_EXPT_EXPERIMENT E,
     V4_EXPT_TRIAL ET,
     V4_EXPT_TRIAL_ASSEMBLY EA
 WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

INSERT INTO V4_ASB_OPLAN_AGENT_ATTR 
    (ASSEMBLY_ID, OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME , END_CDAY, ATTRIBUTE_VALUE)
SELECT * FROM tempcopy.V4_ASB_OPLAN_AGENT_ATTR;

-- V4_ASB_OPLAN
DROP TABLE IF EXISTS tempcopy.V4_ASB_OPLAN;

CREATE TABLE tempcopy.V4_ASB_OPLAN AS
SELECT DISTINCT 
 CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
 AA.OPLAN_ID AS OPLAN_ID, 
 AA.OPERATION_NAME AS OPERATION_NAME,
 AA.PRIORITY AS PRIORITY, 
 AA.C0_DATE AS C0_DATE 
FROM V4_ASB_OPLAN AA, 
     V4_EXPT_EXPERIMENT E,
     V4_EXPT_TRIAL ET,
     V4_EXPT_TRIAL_ASSEMBLY EA
 WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

INSERT INTO V4_ASB_OPLAN 
   (ASSEMBLY_ID, OPLAN_ID, OPERATION_NAME , PRIORITY, C0_DATE) 
SELECT * FROM tempcopy.V4_ASB_OPLAN;

-- V4_ASB_ASSEMBLY
DROP TABLE IF EXISTS tempcopy.V4_ASB_ASSEMBLY;

CREATE TABLE tempcopy.V4_ASB_ASSEMBLY AS
SELECT DISTINCT 
 CONCAT(AA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID, 
 AA.ASSEMBLY_TYPE AS ASSEMBLY_TYPE, 
 concat(AA.DESCRIPTION,'_MODIFIED_FOR_', AA.ASSEMBLY_ID, '-:suffix') AS DESCRIPTION 
FROM V4_ASB_ASSEMBLY AA, 
     V4_EXPT_EXPERIMENT E,
     V4_EXPT_TRIAL ET,
     V4_EXPT_TRIAL_ASSEMBLY EA
 WHERE 
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':oldExpt';

INSERT INTO V4_ASB_ASSEMBLY 
   (ASSEMBLY_ID,ASSEMBLY_TYPE,DESCRIPTION)
SELECT * FROM tempcopy.V4_ASB_ASSEMBLY;

-- V4_TRIAL_MOD_RECIPE
DROP TABLE IF EXISTS tempcopy.V4_EXPT_TRIAL_MOD_RECIPE;

CREATE TABLE tempcopy.V4_EXPT_TRIAL_MOD_RECIPE AS
  SELECT DISTINCT
   CONCAT(CONCAT(E.EXPT_ID,'-:suffix'),'.TRIAL') AS TRIAL_ID, 
    concat(AA.MOD_RECIPE_LIB_ID, '-:suffix') AS MOD_RECIPE_LIB_ID,
    AA.RECIPE_ORDER,
    CONCAT(E.EXPT_ID,'-:suffix') AS EXPT_ID
  FROM
    V4_EXPT_TRIAL_MOD_RECIPE AA,
     V4_EXPT_EXPERIMENT E,
     V4_EXPT_TRIAL ET
  WHERE
      E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = AA.TRIAL_ID
      AND E.NAME = ':oldExpt';

INSERT INTO V4_EXPT_TRIAL_MOD_RECIPE
(TRIAL_ID, MOD_RECIPE_LIB_ID, RECIPE_ORDER, EXPT_ID)
SELECT * FROM tempcopy.V4_EXPT_TRIAL_MOD_RECIPE;

-- V4_EXPT_EXPERIMENT
DROP TABLE IF EXISTS tempcopy.V4_EXPT_EXPERIMENT;

CREATE TABLE tempcopy.V4_EXPT_EXPERIMENT AS
  SELECT DISTINCT
   CONCAT(VEE.EXPT_ID,'-:suffix') AS EXPT_ID, 
   CONCAT(':oldExpt','-:suffix a copy') AS DESCRIPTION, 
   CONCAT(':oldExpt','-:suffix') AS NAME, 
   VEE.CFW_GROUP_ID  AS CFW_GROUP_ID
  FROM 
   V4_EXPT_EXPERIMENT VEE 
  WHERE 
  VEE.NAME =':oldExpt';

INSERT INTO  V4_EXPT_EXPERIMENT 
   (EXPT_ID, DESCRIPTION, NAME, CFW_GROUP_ID) 
SELECT * FROM tempcopy.V4_EXPT_EXPERIMENT;

-- V4_EXPT_TRIAL_THREAD
DROP TABLE IF EXISTS tempcopy.V4_EXPT_TRIAL_THREAD;

CREATE TABLE tempcopy.V4_EXPT_TRIAL_THREAD AS
  SELECT DISTINCT
   CONCAT(VE.EXPT_ID,'-:suffix') AS EXPT_ID, 
   CONCAT(CONCAT(VE.EXPT_ID,'-:suffix'),'.TRIAL') AS TRIAL_ID, 
   THREAD_ID AS THREAD_ID
  FROM 
   V4_EXPT_TRIAL_THREAD VETT,
   V4_EXPT_TRIAL VT,
   V4_EXPT_EXPERIMENT VE
  WHERE VETT.TRIAL_ID = VT.TRIAL_ID AND VT.EXPT_ID = VE.EXPT_ID AND VE.NAME = ':oldExpt';

 INSERT INTO  V4_EXPT_TRIAL_THREAD 
   (EXPT_ID, TRIAL_ID, THREAD_ID) 
SELECT * FROM tempcopy.V4_EXPT_TRIAL_THREAD;

-- V4_EXPT_TRIAL_ORG_MULT
DROP TABLE IF EXISTS tempcopy.V4_EXPT_TRIAL_ORG_MULT;

CREATE TABLE tempcopy.V4_EXPT_TRIAL_ORG_MULT AS
SELECT DISTINCT
   CONCAT(VE.EXPT_ID,'-:suffix') AS EXPT_ID, 
   CONCAT(CONCAT(VE.EXPT_ID,'-:suffix'),'.TRIAL') AS  TRIAL_ID, 
   CFW_ID AS CFW_ID, 
   ORG_GROUP_ID AS  ORG_GROUP_ID, 
   MULTIPLIER AS  MULTIPLIER, 
   VETOM.DESCRIPTION AS DESCRIPTION 
  FROM 
   V4_EXPT_TRIAL_ORG_MULT VETOM, V4_EXPT_TRIAL VT, V4_EXPT_EXPERIMENT VE 
  WHERE 
   VETOM.TRIAL_ID = VT.TRIAL_ID AND VT.EXPT_ID = VE.EXPT_ID AND VE.NAME = ':oldExpt';

INSERT INTO  V4_EXPT_TRIAL_ORG_MULT 
   (EXPT_ID,TRIAL_ID, CFW_ID , ORG_GROUP_ID, MULTIPLIER, DESCRIPTION) 
SELECT * FROM tempcopy.V4_EXPT_TRIAL_ORG_MULT;

-- V4_EXPT_TRIAL_ASSEMBLY
DROP TABLE IF EXISTS tempcopy.V4_EXPT_TRIAL_ASSEMBLY;

CREATE TABLE tempcopy.V4_EXPT_TRIAL_ASSEMBLY AS
 SELECT DISTINCT
   CONCAT(CONCAT(VE.EXPT_ID,'-:suffix'),'.TRIAL') AS TRIAL_ID, 
   CONCAT(TA.ASSEMBLY_ID, '-:suffix') AS ASSEMBLY_ID,
   CONCAT(VE.EXPT_ID,'-:suffix') AS EXPT_ID,
   CONCAT(TA.DESCRIPTION, ' copy') AS DESCRIPTION
 FROM 
   V4_EXPT_TRIAL_ASSEMBLY TA, V4_EXPT_TRIAL VT, V4_EXPT_EXPERIMENT VE 
 WHERE 
    TA.TRIAL_ID= VT.TRIAL_ID AND VT.EXPT_ID = VE.EXPT_ID AND VE.NAME = ':oldExpt' ;

INSERT INTO  V4_EXPT_TRIAL_ASSEMBLY 
   (TRIAL_ID,ASSEMBLY_ID,EXPT_ID,DESCRIPTION) 
SELECT * FROM tempcopy.V4_EXPT_TRIAL_ASSEMBLY;

-- V4_EXPT_TRIAL
DROP TABLE IF EXISTS tempcopy.V4_EXPT_TRIAL;

CREATE TABLE tempcopy.V4_EXPT_TRIAL AS 
SELECT DISTINCT
  CONCAT(CONCAT(VE.EXPT_ID,'-:suffix'),'.TRIAL') AS TRIAL_ID,
  CONCAT(VE.EXPT_ID, '-:suffix') AS EXPT_ID,
  CONCAT(VE.DESCRIPTION, '-:suffix a copy') AS DESCRIPTION,
  CONCAT(VE.DESCRIPTION, '-:suffix a copy') AS NAME
FROM V4_EXPT_EXPERIMENT VE
WHERE VE.NAME = ':oldExpt';

INSERT INTO  V4_EXPT_TRIAL 
   (TRIAL_ID, EXPT_ID, DESCRIPTION, NAME)
SELECT * FROM tempcopy.V4_EXPT_TRIAL;

-- Add new entry in V4_ALIB_COMPONENT for the society
DROP TABLE IF EXISTS tempcopy.V4_ALIB_COMPONENT;

CREATE TABLE tempcopy.V4_ALIB_COMPONENT AS
 SELECT DISTINCT 'society|:oldExpt-:suffix' AS COMPONENT_ALIB_ID, 
     CONCAT(A.COMPONENT_NAME, '-:suffix') AS COMPONENT_NAME, 
     'society|:oldExpt-:suffix' AS COMPONENT_LIB_ID, 
     A.COMPONENT_TYPE, A.CLONE_SET_ID 
 FROM V4_ALIB_COMPONENT A
 WHERE A.COMPONENT_ALIB_ID = 'society|:oldExpt';

INSERT INTO V4_ALIB_COMPONENT
    (COMPONENT_ALIB_ID, COMPONENT_NAME, COMPONENT_LIB_ID, COMPONENT_TYPE, CLONE_SET_ID) 
 SELECT * FROM tempcopy.V4_ALIB_COMPONENT;

-- Add new entry in V4_LIB_COMPONENT for the society
DROP TABLE IF EXISTS tempcopy.V4_LIB_COMPONENT;

CREATE TABLE tempcopy.V4_LIB_COMPONENT AS 
 SELECT DISTINCT 'society|:oldExpt-:suffix' AS COMPONENT_LIB_ID,
	A.COMPONENT_TYPE, A.COMPONENT_CLASS, A.INSERTION_POINT, 
	CONCAT(A.DESCRIPTION, '-:suffix') AS DESCRIPTION
 FROM V4_LIB_COMPONENT A
 WHERE A.COMPONENT_LIB_ID = 'society|:oldExpt';

INSERT INTO V4_LIB_COMPONENT
 (COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION)
 SELECT * FROM tempcopy.V4_LIB_COMPONENT;

-- THESE TABLES NECESSARY FOR EXPORT ONLY
-- V4_LIB_COMPONENT
-- V4_ALIB_COMPONENT
-- V4_LIB_MOD_RECIPE
-- V4_LIB_MOD_RECIPE_ARG

-- REMEMBER TO DROP THE OTHER DB
-- DROP DATABASE tempcopy;

