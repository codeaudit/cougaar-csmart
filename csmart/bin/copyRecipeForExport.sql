-- Copy the named Recipe for export.
-- Creates the tempcopy db again, and leaves it there.
-- Used by export-recipe.sh

CREATE DATABASE tempcopy;

-- lib_mod_recipe
DROP TABLE IF EXISTS tempcopy.lib_mod_recipe;

CREATE TABLE tempcopy.lib_mod_recipe AS
  SELECT DISTINCT
    CONCAT(MOD_RECIPE_LIB_ID, NAME) AS MOD_RECIPE_LIB_ID,
    CONCAT(NAME, '-cpy') AS NAME,
    JAVA_CLASS,
    DESCRIPTION
  FROM
    lib_mod_recipe
  WHERE
    NAME = ':recipeName';

-- lib_mod_recipe_arg
DROP TABLE IF EXISTS tempcopy.lib_mod_recipe_arg;

CREATE TABLE tempcopy.lib_mod_recipe_arg AS
  SELECT DISTINCT
    CONCAT(AA.MOD_RECIPE_LIB_ID, AT.NAME) AS MOD_RECIPE_LIB_ID,
    AA.ARG_NAME,
    AA.ARG_ORDER,
    AA.ARG_VALUE
  FROM
    lib_mod_recipe_arg AA,
    lib_mod_recipe AT
  WHERE
    AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName';

-- must also update the one arg value with the new Assembly ID
-- If necessary
UPDATE tempcopy.lib_mod_recipe_arg
   SET ARG_VALUE = LEFT(CONCAT(ARG_VALUE, '-:recipeName'),50)
  WHERE ARG_NAME = 'Assembly Id';

------------------------------------------
-- Now grab the assembly contents for this recipe
-- Note that in the case of this script (and only this script)
-- it is safe to drop the table first
-- other scripts will need to be more careful

-- asb_assembly
DROP TABLE IF EXISTS tempcopy.asb_assembly;

CREATE TABLE tempcopy.asb_assembly AS
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
    AB.ASSEMBLY_TYPE AS ASSEMBLY_TYPE, 
    CONCAT(AB.DESCRIPTION,'-cpy') AS DESCRIPTION 
  FROM asb_assembly AB, 
    lib_mod_recipe_arg AA,
    lib_mod_recipe AT
  WHERE 
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- asb_component_hierarchy
DROP TABLE IF EXISTS tempcopy.asb_component_hierarchy;

CREATE TABLE tempcopy.asb_component_hierarchy AS
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
    AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AB.PARENT_COMPONENT_ALIB_ID AS PARENT_COMPONENT_ALIB_ID, 
    AB.PRIORITY AS PRIORITY,
    AB.INSERTION_ORDER AS INSERTION_ORDER 
   FROM 
     asb_component_hierarchy AB,
     lib_mod_recipe_arg AA,
     lib_mod_recipe AT
    WHERE
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- asb_agent
DROP TABLE IF EXISTS tempcopy.asb_agent;

CREATE TABLE tempcopy.asb_agent AS
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
    AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AB.COMPONENT_LIB_ID AS COMPONENT_LIB_ID, 
    AB.CLONE_SET_ID AS CLONE_SET_ID, 
    AB.COMPONENT_NAME AS COMPONENT_NAME 
   FROM 
     asb_agent AB,
     lib_mod_recipe_arg AA,
     lib_mod_recipe AT
    WHERE 
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- lib_agent_org

DROP TABLE IF EXISTS tempcopy.lib_agent_org;

CREATE TABLE tempcopy.lib_agent_org AS
  SELECT DISTINCT 
    L.COMPONENT_LIB_ID,
    L.AGENT_LIB_NAME,
    L.AGENT_ORG_CLASS
   FROM
    lib_agent_org L,
    tempcopy.asb_agent A
   WHERE
    A.COMPONENT_LIB_ID = L.COMPONENT_LIB_ID;

-- asb_agent_pg_attr
DROP TABLE IF EXISTS tempcopy.asb_agent_pg_attr;

CREATE TABLE tempcopy.asb_agent_pg_attr AS
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
    AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AB.PG_ATTRIBUTE_LIB_ID AS PG_ATTRIBUTE_LIB_ID, 
    AB.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE, 
    AB.ATTRIBUTE_ORDER AS ATTRIBUTE_ORDER, 
    AB.START_DATE AS START_DATE, 
    AB.END_DATE AS END_DATE 
  FROM  asb_agent_pg_attr AB,
     lib_mod_recipe_arg AA,
     lib_mod_recipe AT
  WHERE 
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- asb_agent_relation
DROP TABLE IF EXISTS tempcopy.asb_agent_relation;

CREATE TABLE tempcopy.asb_agent_relation AS
 SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
  AB.ROLE AS ROLE, 
  AB.SUPPORTING_COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, 
  AB.SUPPORTED_COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, 
  AB.START_DATE AS START_DATE, 
  AB.END_DATE AS END_DATE 
 FROM asb_agent_relation AB, 
     lib_mod_recipe_arg AA,
     lib_mod_recipe AT
 WHERE 
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- asb_component_arg
DROP TABLE IF EXISTS tempcopy.asb_component_arg;

CREATE TABLE tempcopy.asb_component_arg AS
 SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
  AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
  AB.ARGUMENT AS ARGUMENT, 
  AB.ARGUMENT_ORDER AS ARGUMENT_ORDER 
  FROM asb_component_arg AB, 
     lib_mod_recipe_arg AA,
     lib_mod_recipe AT
 WHERE 
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- asb_oplan_agent_attr
DROP TABLE IF EXISTS tempcopy.asb_oplan_agent_attr;

CREATE TABLE tempcopy.asb_oplan_agent_attr AS
 SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
  AB.OPLAN_ID AS OPLAN_ID, 
  AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
  AB.COMPONENT_ID AS COMPONENT_ID, 
  AB.START_CDAY AS START_CDAY, 
  AB.ATTRIBUTE_NAME AS ATTRIBUTE_NAME,
  AB.END_CDAY AS END_CDAY, 
  AB.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE 
FROM asb_oplan_agent_attr AB, 
     lib_mod_recipe_arg AA,
     lib_mod_recipe AT
 WHERE 
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- asb_oplan
DROP TABLE IF EXISTS tempcopy.asb_oplan;

CREATE TABLE tempcopy.asb_oplan AS
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
   AB.OPLAN_ID AS OPLAN_ID, 
   AB.OPERATION_NAME AS OPERATION_NAME,
   AB.PRIORITY AS PRIORITY, 
   AB.C0_DATE AS C0_DATE 
  FROM asb_oplan AB, 
     lib_mod_recipe_arg AA,
     lib_mod_recipe AT
  WHERE 
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- community_attribute
DROP TABLE IF EXISTS tempcopy.community_attribute;

CREATE TABLE tempcopy.community_attribute AS
  SELECT DISTINCT
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
   AB.COMMUNITY_ID AS COMMUNITY_ID, 
   AB.ATTRIBUTE_ID AS ATTRIBUTE_ID,
   AB.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE
  FROM
   community_attribute AB,
     lib_mod_recipe_arg AA,
     lib_mod_recipe AT
  WHERE
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- community_entity_attribute
DROP TABLE IF EXISTS tempcopy.community_entity_attribute;

CREATE TABLE tempcopy.community_entity_attribute AS
  SELECT DISTINCT
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
   AB.COMMUNITY_ID AS COMMUNITY_ID,
   AB.ENTITY_ID AS ENTITY_ID, 
   AB.ATTRIBUTE_ID AS ATTRIBUTE_ID,
   AB.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE
  FROM
   community_entity_attribute AB,
     lib_mod_recipe_arg AA,
     lib_mod_recipe AT
  WHERE
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-------

-- Add in those from recipe assembly
-- alib_component
DROP TABLE IF EXISTS tempcopy.alib_component;

CREATE TABLE tempcopy.alib_component AS
   SELECT DISTINCT
     AA.COMPONENT_ALIB_ID,
     AA.COMPONENT_NAME,
     AA.COMPONENT_LIB_ID,
     AA.COMPONENT_TYPE,
     AA.CLONE_SET_ID
   FROM
     alib_component AA,
     asb_component_hierarchy AH,
     lib_mod_recipe_arg MA,
     lib_mod_recipe LM
   WHERE
         MA.ARG_NAME = 'Assembly Id'
     AND MA.MOD_RECIPE_LIB_ID = LM.MOD_RECIPE_LIB_ID
     AND LM.NAME = ':recipeName'
     AND MA.ARG_VALUE = AH.ASSEMBLY_ID
     AND (AH.COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID OR AH.PARENT_COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID);

 -- lib_component

 DROP TABLE IF EXISTS tempcopy.lib_component;

 CREATE TABLE tempcopy.lib_component AS
   SELECT DISTINCT
     AA.COMPONENT_LIB_ID,
     AA.COMPONENT_TYPE,
     AA.COMPONENT_CLASS,
     AA.INSERTION_POINT,
     AA.DESCRIPTION
   FROM
     lib_component AA,
     tempcopy.alib_component AC
   WHERE
     AA.COMPONENT_LIB_ID = AC.COMPONENT_LIB_ID;

UPDATE tempcopy.alib_component
   SET COMPONENT_ALIB_ID = ':recipeName-cpy'
 WHERE COMPONENT_ALIB_ID = ':recipeName';

UPDATE tempcopy.alib_component
   SET COMPONENT_NAME = ':recipeName-cpy'
 WHERE COMPONENT_NAME = ':recipeName';

UPDATE tempcopy.asb_agent
   SET COMPONENT_ALIB_ID = ':recipeName-cpy'
 WHERE COMPONENT_ALIB_ID = ':recipeName';

UPDATE tempcopy.asb_component_arg
   SET COMPONENT_ALIB_ID = ':recipeName-cpy'
 WHERE COMPONENT_ALIB_ID = ':recipeName';

UPDATE tempcopy.asb_component_hierarchy
   SET COMPONENT_ALIB_ID = ':recipeName-cpy'
 WHERE COMPONENT_ALIB_ID = ':recipeName';

UPDATE tempcopy.asb_component_hierarchy
   SET PARENT_COMPONENT_ALIB_ID = ':recipeName-cpy'
 WHERE PARENT_COMPONENT_ALIB_ID = ':recipeName';

-- lib_pg_attribute
DROP TABLE IF EXISTS tempcopy.lib_pg_attribute;

CREATE TABLE tempcopy.lib_pg_attribute AS
  SELECT DISTINCT 
    LP.PG_ATTRIBUTE_LIB_ID,
    LP.PG_NAME,
    LP.ATTRIBUTE_NAME,
    LP.ATTRIBUTE_TYPE,
    LP.AGGREGATE_TYPE
   FROM
     lib_pg_attribute LP,
     tempcopy.asb_agent_pg_attr AP
   WHERE
     AP.PG_ATTRIBUTE_LIB_ID = LP.PG_ATTRIBUTE_LIB_ID;

UPDATE tempcopy.asb_agent_pg_attr
   SET COMPONENT_ALIB_ID = ':recipeName-cpy'
 WHERE COMPONENT_ALIB_ID = ':recipeName';

UPDATE tempcopy.asb_oplan_agent_attr
   SET COMPONENT_ALIB_ID = ':recipeName-cpy'
 WHERE COMPONENT_ALIB_ID = ':recipeName';

