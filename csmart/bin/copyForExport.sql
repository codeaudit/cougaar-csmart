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
    CONCAT(AA.MOD_RECIPE_LIB_ID, '-cpy') AS MOD_RECIPE_LIB_ID,
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
    CONCAT(AA.MOD_RECIPE_LIB_ID, '-cpy') AS MOD_RECIPE_LIB_ID,
    AA.ARG_NAME,
    AA.ARG_ORDER,
    AA.ARG_VALUE
  FROM
    v4_lib_mod_recipe_arg AA,
    v4_expt_trial_mod_recipe ER,
    v4_expt_trial ET,
    v4_expt_experiment E
  WHERE
    AA.MOD_RECIPE_LIB_ID = ER.MOD_RECIPE_LIB_ID
    AND ER.TRIAL_ID = ET.TRIAL_ID
    AND ET.EXPT_ID = E.EXPT_ID
    AND E.NAME = ':exptName';

