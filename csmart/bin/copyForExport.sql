-- COPY REFERENCE STUFF USED BY AN EXPERIMENT FOR EXPORT

-- v4_alib_component
DROP TABLE IF EXISTS tempcopy.v4_alib_component2;
DROP TABLE IF EXISTS tempcopy.v4_alib_component;

CREATE TABLE tempcopy.v4_alib_component2 AS
  SELECT DISTINCT
    AA.COMPONENT_ALIB_ID,
    AA.COMPONENT_NAME,
    AA.COMPONENT_LIB_ID,
    AA.COMPONENT_TYPE,
    AA.CLONE_SET_ID
  FROM
    v4_alib_component AA,
    v4_asb_component_hierarchy AH,
    v4_expt_trial_assembly EA,
    v4_expt_trial ET,
    v4_expt_experiment E
  WHERE
    E.NAME = ':exptName'
    AND E.EXPT_ID = ET.EXPT_ID
    AND ET.TRIAL_ID = EA.TRIAL_ID
    AND EA.ASSEMBLY_ID = AH.ASSEMBLY_ID
    AND (AH.COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID OR AH.PARENT_COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID);

REPLACE INTO tempcopy.v4_alib_component2
  (COMPONENT_ALIB_ID, COMPONENT_NAME, COMPONENT_LIB_ID, COMPONENT_TYPE, CLONE_SET_ID) 
  SELECT DISTINCT
    AA.COMPONENT_ALIB_ID,
    AA.COMPONENT_NAME,
    AA.COMPONENT_LIB_ID,
    AA.COMPONENT_TYPE,
    AA.CLONE_SET_ID
  FROM
    v4_alib_component AA,
    v4_asb_component_hierarchy AH,
    v4_expt_trial_config_assembly EA,
    v4_expt_trial ET,
    v4_expt_experiment E
  WHERE
    E.NAME = ':exptName'
    AND E.EXPT_ID = ET.EXPT_ID
    AND ET.TRIAL_ID = EA.TRIAL_ID
    AND EA.ASSEMBLY_ID = AH.ASSEMBLY_ID
    AND (AH.COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID OR AH.PARENT_COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID);

-- Add in those from recipe assembly
 REPLACE INTO tempcopy.v4_alib_component2
   (COMPONENT_ALIB_ID, COMPONENT_NAME, COMPONENT_LIB_ID, COMPONENT_TYPE, CLONE_SET_ID) 
   SELECT DISTINCT
     AA.COMPONENT_ALIB_ID,
     AA.COMPONENT_NAME,
     AA.COMPONENT_LIB_ID,
     AA.COMPONENT_TYPE,
     AA.CLONE_SET_ID
   FROM
     v4_alib_component AA,
     v4_asb_component_hierarchy AH,
     v4_expt_trial_mod_recipe EA,
     v4_lib_mod_recipe_arg MA,
     v4_expt_trial ET,
     v4_expt_experiment E
   WHERE
     E.NAME = ':exptName'
     AND E.EXPT_ID = ET.EXPT_ID
     AND ET.TRIAL_ID = EA.TRIAL_ID
     AND EA.MOD_RECIPE_LIB_ID = MA.MOD_RECIPE_LIB_ID
     AND MA.ARG_NAME = 'Assembly Id'
     AND MA.ARG_VALUE = AH.ASSEMBLY_ID
     AND (AH.COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID OR AH.PARENT_COMPONENT_ALIB_ID = AA.COMPONENT_ALIB_ID);

 CREATE TABLE tempcopy.v4_alib_component AS
   SELECT DISTINCT
     COMPONENT_ALIB_ID,
     COMPONENT_NAME,
     COMPONENT_LIB_ID,
     COMPONENT_TYPE,
     CLONE_SET_ID
   FROM
     tempcopy.v4_alib_component2;

 DROP TABLE tempcopy.v4_alib_component2;

 -- v4_lib_component
 DROP TABLE IF EXISTS tempcopy.v4_lib_component;

 CREATE TABLE tempcopy.v4_lib_component AS
   SELECT DISTINCT
     AA.COMPONENT_LIB_ID,
     AA.COMPONENT_TYPE,
     AA.COMPONENT_CLASS,
     AA.INSERTION_POINT,
     AA.DESCRIPTION
   FROM
     v4_lib_component AA,
     tempcopy.v4_alib_component AC
   WHERE
     AA.COMPONENT_LIB_ID = AC.COMPONENT_LIB_ID;

 -- v4_lib_mod_recipe
 DROP TABLE IF EXISTS tempcopy.v4_lib_mod_recipe;

 CREATE TABLE tempcopy.v4_lib_mod_recipe AS
   SELECT DISTINCT
     CONCAT(AA.MOD_RECIPE_LIB_ID, AA.NAME) AS MOD_RECIPE_LIB_ID,
     CONCAT(AA.NAME, '-cpy') AS NAME,
     AA.JAVA_CLASS,
     AA.DESCRIPTION
   FROM
     v4_lib_mod_recipe AA,
     v4_expt_trial_mod_recipe ER,
     v4_expt_trial ET,
     v4_expt_experiment E
   WHERE
     AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

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
     v4_lib_mod_recipe AT,
     v4_expt_trial_mod_recipe ER,
     v4_expt_trial ET,
     v4_expt_experiment E
   WHERE
     AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

 -- update assembly_id arg_value here
UPDATE tempcopy.v4_lib_mod_recipe_arg
   SET ARG_VALUE = LEFT(CONCAT(ARG_VALUE, '-:exptName'),50)
  WHERE ARG_NAME = 'Assembly Id'

-- Must also copy all the entries in the assembly tables for the assemblies
-- with the appropriate recipe_lib_ids
-- Note that the table tempcopy.<foo> will already exist so
-- dont create or drop it -- instead, do a replace into
-- Also, the new assembly_id is old with -:exptName (not -:recipeName)

-- asb_assembly
 REPLACE INTO tempcopy.v4_asb_assembly
   (ASSEMBLY_ID, ASSEMBLY_TYPE, DESCRIPTION) 
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
    AB.ASSEMBLY_TYPE AS ASSEMBLY_TYPE, 
    AB.DESCRIPTION AS DESCRIPTION 
  FROM v4_asb_assembly AB, 
     v4_lib_mod_recipe_arg AA,
     v4_expt_trial_mod_recipe ER,
     v4_expt_trial ET,
     v4_expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- asb_component_hierarchy
REPLACE INTO tempcopy.v4_asb_component_hierarchy
 (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, PRIORITY, INSERTION_ORDER)
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
    AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AB.PARENT_COMPONENT_ALIB_ID AS PARENT_COMPONENT_ALIB_ID, 
    AB.PRIORITY AS PRIORITY,
    AB.INSERTION_ORDER AS INSERTION_ORDER 
   FROM 
     v4_asb_component_hierarchy AB,
     v4_lib_mod_recipe_arg AA,
     v4_expt_trial_mod_recipe ER,
     v4_expt_trial ET,
     v4_expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- asb_agent
REPLACE INTO tempcopy.v4_asb_agent
  (ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME)
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
    AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AB.COMPONENT_LIB_ID AS COMPONENT_LIB_ID, 
    AB.CLONE_SET_ID AS CLONE_SET_ID, 
    AB.COMPONENT_NAME AS COMPONENT_NAME 
   FROM 
     v4_asb_agent AB,
     v4_lib_mod_recipe_arg AA,
     v4_expt_trial_mod_recipe ER,
     v4_expt_trial ET,
     v4_expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- asb_agent_pg_attr
REPLACE INTO tempcopy.v4_asb_agent_pg_attr
 (ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE, END_DATE)
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
    AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
    AB.PG_ATTRIBUTE_LIB_ID AS PG_ATTRIBUTE_LIB_ID, 
    AB.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE, 
    AB.ATTRIBUTE_ORDER AS ATTRIBUTE_ORDER, 
    AB.START_DATE AS START_DATE, 
    AB.END_DATE AS END_DATE 
  FROM  v4_asb_agent_pg_attr AB,
     v4_lib_mod_recipe_arg AA,
     v4_expt_trial_mod_recipe ER,
     v4_expt_trial ET,
     v4_expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- asb_agent_relation
REPLACE INTO tempcopy.v4_asb_agent_relation
 (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE)
 SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
  AB.ROLE AS ROLE, 
  AB.SUPPORTING_COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, 
  AB.SUPPORTED_COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, 
  AB.START_DATE AS START_DATE, 
  AB.END_DATE AS END_DATE 
 FROM v4_asb_agent_relation AB, 
     v4_lib_mod_recipe_arg AA,
     v4_expt_trial_mod_recipe ER,
     v4_expt_trial ET,
     v4_expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- asb_component_arg
REPLACE INTO tempcopy.v4_asb_component_arg
  (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER)
 SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
  AB.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, 
  AB.ARGUMENT AS ARGUMENT, 
  AB.ARGUMENT_ORDER AS ARGUMENT_ORDER 
  FROM v4_asb_component_arg AB, 
     v4_lib_mod_recipe_arg AA,
     v4_expt_trial_mod_recipe ER,
     v4_expt_trial ET,
     v4_expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- asb_oplan_agent_attr
REPLACE INTO tempcopy.v4_asb_oplan_agent_attr
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
FROM v4_asb_oplan_agent_attr AB, 
     v4_lib_mod_recipe_arg AA,
     v4_expt_trial_mod_recipe ER,
     v4_expt_trial ET,
     v4_expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

-- asb_oplan
REPLACE INTO tempcopy.v4_asb_oplan
 (ASSEMBLY_ID, OPLAN_ID, OPERATION_NAME, PRIORITY, C0_DATE)
  SELECT DISTINCT 
    LEFT(CONCAT(AA.ARG_VALUE, '-:exptName'),50) AS ASSEMBLY_ID,
   AB.OPLAN_ID AS OPLAN_ID, 
   AB.OPERATION_NAME AS OPERATION_NAME,
   AB.PRIORITY AS PRIORITY, 
   AB.C0_DATE AS C0_DATE 
  FROM v4_asb_oplan AB, 
     v4_lib_mod_recipe_arg AA,
     v4_expt_trial_mod_recipe ER,
     v4_expt_trial ET,
     v4_expt_experiment E
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
     v4_lib_mod_recipe_arg AA,
     v4_expt_trial_mod_recipe ER,
     v4_expt_trial ET,
     v4_expt_experiment E
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
   community_attribute AB,
     v4_lib_mod_recipe_arg AA,
     v4_expt_trial_mod_recipe ER,
     v4_expt_trial ET,
     v4_expt_experiment E
  WHERE 
         AA.ARG_NAME = 'Assembly Id'
     AND AA.ARG_VALUE = AB.ASSEMBLY_ID
     AND AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
     AND ER.TRIAL_ID = ET.TRIAL_ID
     AND ET.EXPT_ID = E.EXPT_ID
     AND E.NAME = ':exptName';

