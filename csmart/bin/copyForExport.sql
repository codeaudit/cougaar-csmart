-- COPY REFERENCE STUFF USED BY AN EXPERIMENT FOR EXPORT

-- alib_component
DROP TABLE IF EXISTS tempcopy.alib_component2;
DROP TABLE IF EXISTS tempcopy.alib_component;

CREATE TABLE tempcopy.alib_component2 AS
  SELECT DISTINCT
    AA.COMPONENT_ALIB_ID,
    AA.COMPONENT_NAME,
    AA.COMPONENT_LIB_ID,
    AA.COMPONENT_TYPE,
    AA.CLONE_SET_ID
  FROM
    alib_component AA,
    asb_component_hierarchy AH,
    expt_trial_assembly EA,
    expt_trial ET,
    expt_experiment E
  WHERE
    E.NAME = ':exptName'
    AND E.EXPT_ID = ET.EXPT_ID
    AND ET.TRIAL_ID = EA.TRIAL_ID
    AND EA.ASSEMBLY_ID = AH.ASSEMBLY_ID
    AND (AH.COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID OR AH.PARENT_COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID);

REPLACE INTO tempcopy.alib_component2
  (COMPONENT_ALIB_ID, COMPONENT_NAME, COMPONENT_LIB_ID, COMPONENT_TYPE, CLONE_SET_ID) 
  SELECT DISTINCT
    AA.COMPONENT_ALIB_ID,
    AA.COMPONENT_NAME,
    AA.COMPONENT_LIB_ID,
    AA.COMPONENT_TYPE,
    AA.CLONE_SET_ID
  FROM
    alib_component AA,
    asb_component_hierarchy AH,
    expt_trial_config_assembly EA,
    expt_trial ET,
    expt_experiment E
  WHERE
    E.NAME = ':exptName'
    AND E.EXPT_ID = ET.EXPT_ID
    AND ET.TRIAL_ID = EA.TRIAL_ID
    AND EA.ASSEMBLY_ID = AH.ASSEMBLY_ID
    AND (AH.COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID OR AH.PARENT_COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID);

-- Add in those from recipe assembly
 REPLACE INTO tempcopy.alib_component2
   (COMPONENT_ALIB_ID, COMPONENT_NAME, COMPONENT_LIB_ID, COMPONENT_TYPE, CLONE_SET_ID) 
   SELECT DISTINCT
     AA.COMPONENT_ALIB_ID,
     AA.COMPONENT_NAME,
     AA.COMPONENT_LIB_ID,
     AA.COMPONENT_TYPE,
     AA.CLONE_SET_ID
   FROM
     alib_component AA,
     asb_component_hierarchy AH,
     expt_trial_mod_recipe EA,
     lib_mod_recipe_arg MA,
     expt_trial ET,
     expt_experiment E
   WHERE
     E.NAME = ':exptName'
     AND E.EXPT_ID = ET.EXPT_ID
     AND ET.TRIAL_ID = EA.TRIAL_ID
     AND EA.MOD_RECIPE_LIB_ID = MA.MOD_RECIPE_LIB_ID
     AND MA.ARG_NAME = 'Assembly Id'
     AND MA.ARG_VALUE = AH.ASSEMBLY_ID
     AND (AH.COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID OR AH.PARENT_COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID);

 CREATE TABLE tempcopy.alib_component AS
   SELECT DISTINCT
     COMPONENT_ALIB_ID,
     COMPONENT_NAME,
     COMPONENT_LIB_ID,
     COMPONENT_TYPE,
     CLONE_SET_ID
   FROM
     tempcopy.alib_component2;

 DROP TABLE tempcopy.alib_component2;

-- lib_pg_attribute
DROP TABLE IF EXISTS tempcopy.lib_pg_attribute;
DROP TABLE IF EXISTS tempcopy.lib_pg_attribute2;

CREATE TABLE tempcopy.lib_pg_attribute2 AS
  SELECT DISTINCT 
    LP.PG_ATTRIBUTE_LIB_ID,
    LP.PG_NAME,
    LP.ATTRIBUTE_NAME,
    LP.ATTRIBUTE_TYPE,
    LP.AGGREGATE_TYPE
   FROM
     lib_pg_attribute LP,
     asb_agent_pg_attr AP,
    expt_trial_config_assembly EA,
    expt_trial ET,
    expt_experiment E
  WHERE
    E.NAME = ':exptName'
    AND E.EXPT_ID = ET.EXPT_ID
    AND ET.TRIAL_ID = EA.TRIAL_ID
    AND EA.ASSEMBLY_ID = AP.ASSEMBLY_ID
    AND AP.PG_ATTRIBUTE_LIB_ID = LP.PG_ATTRIBUTE_LIB_ID;
    

REPLACE INTO tempcopy.lib_pg_attribute2
  (PG_ATTRIBUTE_LIB_ID, PG_NAME, ATTRIBUTE_NAME, ATTRIBUTE_TYPE, AGGREGATE_TYPE) 
  SELECT DISTINCT 
    LP.PG_ATTRIBUTE_LIB_ID,
    LP.PG_NAME,
    LP.ATTRIBUTE_NAME,
    LP.ATTRIBUTE_TYPE,
    LP.AGGREGATE_TYPE
   FROM
     lib_pg_attribute LP,
     asb_agent_pg_attr AP,
    expt_trial_assembly EA,
    expt_trial ET,
    expt_experiment E
  WHERE
    E.NAME = ':exptName'
    AND E.EXPT_ID = ET.EXPT_ID
    AND ET.TRIAL_ID = EA.TRIAL_ID
    AND EA.ASSEMBLY_ID = AP.ASSEMBLY_ID
    AND AP.PG_ATTRIBUTE_LIB_ID = LP.PG_ATTRIBUTE_LIB_ID;

REPLACE INTO tempcopy.lib_pg_attribute2
  (PG_ATTRIBUTE_LIB_ID, PG_NAME, ATTRIBUTE_NAME, ATTRIBUTE_TYPE, AGGREGATE_TYPE) 
  SELECT DISTINCT 
    LP.PG_ATTRIBUTE_LIB_ID,
    LP.PG_NAME,
    LP.ATTRIBUTE_NAME,
    LP.ATTRIBUTE_TYPE,
    LP.AGGREGATE_TYPE
   FROM
     lib_pg_attribute LP,
     asb_agent_pg_attr AP,
    expt_trial_mod_recipe EA,
    lib_mod_recipe_arg LA,
    expt_trial ET,
    expt_experiment E
  WHERE
    E.NAME = ':exptName'
    AND E.EXPT_ID = ET.EXPT_ID
    AND ET.TRIAL_ID = EA.TRIAL_ID
    AND EA.MOD_RECIPE_LIB_ID = LA.MOD_RECIPE_LIB_ID
    AND LA.ARG_NAME = "Assembly Id"
    AND LA.ARG_VALUE = AP.ASSEMBLY_ID
    AND AP.PG_ATTRIBUTE_LIB_ID = LP.PG_ATTRIBUTE_LIB_ID;

CREATE TABLE tempcopy.lib_pg_attribute AS
  SELECT DISTINCT 
    LP.PG_ATTRIBUTE_LIB_ID,
    LP.PG_NAME,
    LP.ATTRIBUTE_NAME,
    LP.ATTRIBUTE_TYPE,
    LP.AGGREGATE_TYPE
   FROM
     tempcopy.lib_pg_attribute2 LP;

DROP TABLE IF EXISTS tempcopy.lib_pg_attribute2;

 -- lib_mod_recipe
 DROP TABLE IF EXISTS tempcopy.lib_mod_recipe;

 CREATE TABLE tempcopy.lib_mod_recipe AS
   SELECT DISTINCT
     CONCAT(AA.MOD_RECIPE_LIB_ID, AA.NAME) AS MOD_RECIPE_LIB_ID,
     CONCAT(AA.NAME, '-cpy') AS NAME,
     AA.JAVA_CLASS,
     AA.DESCRIPTION
   FROM
     lib_mod_recipe AA,
     expt_trial_mod_recipe ER,
     expt_trial ET,
     expt_experiment E
   WHERE
     AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

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
     lib_mod_recipe AT,
     expt_trial_mod_recipe ER,
     expt_trial ET,
     expt_experiment E
   WHERE
     AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

 -- update assembly_id arg_value here
UPDATE tempcopy.lib_mod_recipe_arg
   SET ARG_VALUE = LEFT(CONCAT(ARG_VALUE, '-:exptName'),50)
  WHERE ARG_NAME = 'Assembly Id';

-- Must also copy all the entries in the assembly tables for the assemblies
-- with the appropriate recipe_lib_ids
-- Note that the table tempcopy.<foo> will already exist so
-- dont create or drop it -- instead, do a replace into
-- Also, the new assembly_id is old with -:exptName (not -:recipeName)

-- asb_assembly
 REPLACE INTO tempcopy.asb_assembly
   (ASSEMBLY_ID, ASSEMBLY_TYPE, DESCRIPTION) 
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
    AB.ASSEMBLY_TYPE AS ASSEMBLY_TYPE, 
    CONCAT(AB.DESCRIPTION,'-cpy') AS DESCRIPTION 
  FROM asb_assembly AB, 
     lib_mod_recipe_arg AA,
     expt_trial_mod_recipe ER,
     expt_trial ET,
     expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- asb_component_hierarchy
REPLACE INTO tempcopy.asb_component_hierarchy
 (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, PRIORITY, INSERTION_ORDER)
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
    AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AB.PARENT_COMPONENT_ALIB_ID AS PARENT_COMPONENT_ALIB_ID, 
    AB.PRIORITY AS PRIORITY,
    AB.INSERTION_ORDER AS INSERTION_ORDER 
   FROM 
     asb_component_hierarchy AB,
     lib_mod_recipe_arg AA,
     expt_trial_mod_recipe ER,
     expt_trial ET,
     expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- asb_agent
REPLACE INTO tempcopy.asb_agent
  (ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME)
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
    AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AB.COMPONENT_LIB_ID AS COMPONENT_LIB_ID, 
    AB.CLONE_SET_ID AS CLONE_SET_ID, 
    AB.COMPONENT_NAME AS COMPONENT_NAME 
   FROM 
     asb_agent AB,
     lib_mod_recipe_arg AA,
     expt_trial_mod_recipe ER,
     expt_trial ET,
     expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- asb_agent_pg_attr
REPLACE INTO tempcopy.asb_agent_pg_attr
 (ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE, END_DATE)
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
    AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AB.PG_ATTRIBUTE_LIB_ID AS PG_ATTRIBUTE_LIB_ID, 
    AB.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE, 
    AB.ATTRIBUTE_ORDER AS ATTRIBUTE_ORDER, 
    AB.START_DATE AS START_DATE, 
    AB.END_DATE AS END_DATE 
  FROM  asb_agent_pg_attr AB,
     lib_mod_recipe_arg AA,
     expt_trial_mod_recipe ER,
     expt_trial ET,
     expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- asb_agent_relation
REPLACE INTO tempcopy.asb_agent_relation
 (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE)
 SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
  AB.ROLE AS ROLE, 
  AB.SUPPORTING_COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, 
  AB.SUPPORTED_COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, 
  AB.START_DATE AS START_DATE, 
  AB.END_DATE AS END_DATE 
 FROM asb_agent_relation AB, 
     lib_mod_recipe_arg AA,
     expt_trial_mod_recipe ER,
     expt_trial ET,
     expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- asb_component_arg
REPLACE INTO tempcopy.asb_component_arg
  (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER)
 SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
  AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
  AB.ARGUMENT AS ARGUMENT, 
  AB.ARGUMENT_ORDER AS ARGUMENT_ORDER 
  FROM asb_component_arg AB, 
     lib_mod_recipe_arg AA,
     expt_trial_mod_recipe ER,
     expt_trial ET,
     expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- asb_oplan_agent_attr
REPLACE INTO tempcopy.asb_oplan_agent_attr
 (ASSEMBLY_ID, OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME, END_CDAY, ATTRIBUTE_VALUE)
 SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
  AB.OPLAN_ID AS OPLAN_ID, 
  AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
  AB.COMPONENT_ID AS COMPONENT_ID, 
  AB.START_CDAY AS START_CDAY, 
  AB.ATTRIBUTE_NAME AS ATTRIBUTE_NAME,
  AB.END_CDAY AS END_CDAY, 
  AB.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE 
FROM asb_oplan_agent_attr AB, 
     lib_mod_recipe_arg AA,
     expt_trial_mod_recipe ER,
     expt_trial ET,
     expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- asb_oplan
REPLACE INTO tempcopy.asb_oplan
 (ASSEMBLY_ID, OPLAN_ID, OPERATION_NAME, PRIORITY, C0_DATE)
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
   AB.OPLAN_ID AS OPLAN_ID, 
   AB.OPERATION_NAME AS OPERATION_NAME,
   AB.PRIORITY AS PRIORITY, 
   AB.C0_DATE AS C0_DATE 
  FROM asb_oplan AB, 
     lib_mod_recipe_arg AA,
     expt_trial_mod_recipe ER,
     expt_trial ET,
     expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- community_attribute
REPLACE INTO tempcopy.community_attribute
   (ASSEMBLY_ID, COMMUNITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE)
  SELECT DISTINCT
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
   AB.COMMUNITY_ID AS COMMUNITY_ID, 
   AB.ATTRIBUTE_ID AS ATTRIBUTE_ID,
   AB.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE
  FROM
   community_attribute AB,
     lib_mod_recipe_arg AA,
     expt_trial_mod_recipe ER,
     expt_trial ET,
     expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- community_entity_attribute
REPLACE INTO tempcopy.community_entity_attribute
   (ASSEMBLY_ID, COMMUNITY_ID, ENTITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE)
  SELECT DISTINCT
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
   AB.COMMUNITY_ID AS COMMUNITY_ID,
   AB.ENTITY_ID AS ENTITY_ID, 
   AB.ATTRIBUTE_ID AS ATTRIBUTE_ID,
   AB.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE
  FROM
   community_entity_attribute AB,
     lib_mod_recipe_arg AA,
     expt_trial_mod_recipe ER,
     expt_trial ET,
     expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

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

-- After running this, must update the ALIB IDs on this copy
-- before doing the export, so that the recipe type ALIB IDs
-- are updated appropriately (adding -cpy)
