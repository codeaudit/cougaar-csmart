-- Copy the named Recipe for export.
-- Creates the tempcopy db again, and leaves it there.
-- Used by export-recipe.sh

CREATE DATABASE tempcopy;

-- v4_lib_mod_recipe
DROP TABLE IF EXISTS tempcopy.v4_lib_mod_recipe;

CREATE TABLE tempcopy.v4_lib_mod_recipe AS
  SELECT DISTINCT
    CONCAT(MOD_RECIPE_LIB_ID, NAME) AS MOD_RECIPE_LIB_ID,
    CONCAT(NAME, '-cpy') AS NAME,
    JAVA_CLASS,
    DESCRIPTION
  FROM
    v4_lib_mod_recipe
  WHERE
    NAME = ':recipeName';

-- v4_lib_mod_recipe_arg
DROP TABLE IF EXISTS tempcopy.v4_lib_mod_recipe_arg;

CREATE TABLE tempcopy.v4_lib_mod_recipe_arg AS
  SELECT DISTINCT
    CONCAT(AA.MOD_RECIPE_LIB_ID, AT.NAME) AS MOD_RECIPE_LIB_ID,
    AA.ARG_NAME,
    AA.ARG_ORDER,
    AA.ARG_VALUE
  FROM
    v4_lib_mod_recipe_arg AA,
    v4_lib_mod_recipe AT
  WHERE
    AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName';

-- must also update the one arg value with the new Assembly ID
-- If necessary
UPDATE tempcopy.v4_lib_mod_recipe_arg
   SET ARG_VALUE = LEFT(CONCAT(ARG_VALUE, '-:recipeName'),50)
  WHERE ARG_NAME = 'Assembly Id';

------------------------------------------
-- Now grab the assembly contents for this recipe
-- Note that in the case of this script (and only this script)
-- it is safe to drop the table first
-- other scripts will need to be more careful

-- asb_assembly
DROP TABLE IF EXISTS tempcopy.v4_asb_assembly;

CREATE TABLE tempcopy.v4_asb_assembly AS
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
    AB.ASSEMBLY_TYPE AS ASSEMBLY_TYPE, 
    AB.DESCRIPTION AS DESCRIPTION 
  FROM v4_asb_assembly AB, 
    v4_lib_mod_recipe_arg AA,
    v4_lib_mod_recipe AT
  WHERE 
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- asb_component_hierarchy
DROP TABLE IF EXISTS tempcopy.v4_asb_component_hierarchy;

CREATE TABLE tempcopy.v4_asb_component_hierarchy AS
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
    AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AB.PARENT_COMPONENT_ALIB_ID AS PARENT_COMPONENT_ALIB_ID, 
    AB.PRIORITY AS PRIORITY,
    AB.INSERTION_ORDER AS INSERTION_ORDER 
   FROM 
     v4_asb_component_hierarchy AB,
     v4_lib_mod_recipe_arg AA,
     v4_lib_mod_recipe AT
    WHERE
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- asb_agent
DROP TABLE IF EXISTS tempcopy.v4_asb_agent;

CREATE TABLE tempcopy.v4_asb_agent AS
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
    AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AB.COMPONENT_LIB_ID AS COMPONENT_LIB_ID, 
    AB.CLONE_SET_ID AS CLONE_SET_ID, 
    AB.COMPONENT_NAME AS COMPONENT_NAME 
   FROM 
     v4_asb_agent AB,
     v4_lib_mod_recipe_arg AA,
     v4_lib_mod_recipe AT
    WHERE 
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- asb_agent_pg_attr
DROP TABLE IF EXISTS tempcopy.v4_asb_agent_pg_attr;

CREATE TABLE tempcopy.v4_asb_agent_pg_attr AS
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
    AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AB.PG_ATTRIBUTE_LIB_ID AS PG_ATTRIBUTE_LIB_ID, 
    AB.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE, 
    AB.ATTRIBUTE_ORDER AS ATTRIBUTE_ORDER, 
    AB.START_DATE AS START_DATE, 
    AB.END_DATE AS END_DATE 
  FROM  v4_asb_agent_pg_attr AB,
     v4_lib_mod_recipe_arg AA,
     v4_lib_mod_recipe AT
  WHERE 
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- asb_agent_relation
DROP TABLE IF EXISTS tempcopy.v4_asb_agent_relation;

CREATE TABLE tempcopy.v4_asb_agent_relation AS
 SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
  AB.ROLE AS ROLE, 
  AB.SUPPORTING_COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, 
  AB.SUPPORTED_COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, 
  AB.START_DATE AS START_DATE, 
  AB.END_DATE AS END_DATE 
 FROM v4_asb_agent_relation AB, 
     v4_lib_mod_recipe_arg AA,
     v4_lib_mod_recipe AT
 WHERE 
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- asb_component_arg
DROP TABLE IF EXISTS tempcopy.v4_asb_component_arg;

CREATE TABLE tempcopy.v4_asb_component_arg AS
 SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
  AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
  AB.ARGUMENT AS ARGUMENT, 
  AB.ARGUMENT_ORDER AS ARGUMENT_ORDER 
  FROM v4_asb_component_arg AB, 
     v4_lib_mod_recipe_arg AA,
     v4_lib_mod_recipe AT
 WHERE 
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- asb_oplan_agent_attr
DROP TABLE IF EXISTS tempcopy.v4_asb_oplan_agent_attr;

CREATE TABLE tempcopy.v4_asb_oplan_agent_attr AS
 SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
  AB.OPLAN_ID AS OPLAN_ID, 
  AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
  AB.COMPONENT_ID AS COMPONENT_ID, 
  AB.START_CDAY AS START_CDAY, 
  AB.ATTRIBUTE_NAME AS ATTRIBUTE_NAME,
  AB.END_CDAY AS END_CDAY, 
  AB.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE 
FROM v4_asb_oplan_agent_attr AB, 
     v4_lib_mod_recipe_arg AA,
     v4_lib_mod_recipe AT
 WHERE 
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

-- asb_oplan
DROP TABLE IF EXISTS tempcopy.v4_asb_oplan;

CREATE TABLE tempcopy.v4_asb_oplan AS
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:recipeName'),50) AS ASSEMBLY_ID,
   AB.OPLAN_ID AS OPLAN_ID, 
   AB.OPERATION_NAME AS OPERATION_NAME,
   AB.PRIORITY AS PRIORITY, 
   AB.C0_DATE AS C0_DATE 
  FROM v4_asb_oplan AB, 
     v4_lib_mod_recipe_arg AA,
     v4_lib_mod_recipe AT
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
     v4_lib_mod_recipe_arg AA,
     v4_lib_mod_recipe AT
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
     v4_lib_mod_recipe_arg AA,
     v4_lib_mod_recipe AT
  WHERE
        AA.ARG_NAME = 'Assembly Id'
    AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
    AND AT.NAME = ':recipeName'
    AND AA.ARG_VALUE = AB.ASSEMBLY_ID;

