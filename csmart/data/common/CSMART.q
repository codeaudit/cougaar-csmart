# CSMART.q
# Queries for retrieving saved experiments and recipes in CSMART.
# See org.cougaar.tools.csmart.core.db.DBUtils, org.cougaar.tools.csmart.ui.viewer.OrganizerHelper
database=${org.cougaar.configuration.database}
username=${org.cougaar.configuration.user}
password=${org.cougaar.configuration.password}

queryAssemblyID = \
 SELECT ASSEMBLY_ID, DESCRIPTION \
   FROM asb_assembly \
  WHERE ASSEMBLY_TYPE = ':assembly_type'

# Get list of Agents in this Society
queryAgentNames = \
SELECT DISTINCT A.COMPONENT_NAME \
   FROM alib_component A, \
        asb_agent AGENTS \
  WHERE AGENTS.ASSEMBLY_ID :assemblyMatch \
    AND A.COMPONENT_TYPE = 'agent' \
    AND A.COMPONENT_ALIB_ID=AGENTS.COMPONENT_ALIB_ID 

#queryAgentData = \
# SELECT DISTINCT ALIB.COMPONENT_LIB_ID, ALIB.COMPONENT_TYPE \
#   FROM  alib_component ALIB, asb_component_hierarchy HIER \
#  WHERE HIER.ASSEMBLY_ID :assemblyMatch \
#   AND ALIB.COMPONENT_TYPE='agent'

queryNodes = \
 SELECT DISTINCT ALIB.COMPONENT_NAME \
   FROM alib_component ALIB \
  WHERE ALIB.COMPONENT_TYPE='node'

queryComponentArgs = \
 SELECT ARGUMENT \
   FROM asb_component_arg \
   WHERE COMPONENT_ALIB_ID=':comp_alib_id' \
   AND ASSEMBLY_ID :assemblyMatch \
ORDER BY ARGUMENT_ORDER, ARGUMENT

queryExptsWithSociety = \
 SELECT C.NAME \
   FROM expt_trial_assembly A, asb_assembly B, expt_experiment C \
  WHERE A.ASSEMBLY_ID = B.ASSEMBLY_ID \
   AND C.EXPT_ID = A.EXPT_ID \
   AND B.DESCRIPTION = ':societyName:'

queryHosts = \
 SELECT DISTINCT C.COMPONENT_NAME \
   FROM asb_component_hierarchy H, alib_component C \
  WHERE H.COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID \
    AND C.COMPONENT_TYPE = 'host' \
    AND H.ASSEMBLY_ID :assemblyMatch

queryHostNodes = \
  SELECT HC.COMPONENT_NAME AS HOST_NAME, NC.COMPONENT_NAME AS NODE_NAME \
    FROM alib_component HC, asb_component_hierarchy H, alib_component NC \
   WHERE HC.COMPONENT_ALIB_ID = H.PARENT_COMPONENT_ALIB_ID \
   AND NC.COMPONENT_ALIB_ID = H.COMPONENT_ALIB_ID \
   AND H.ASSEMBLY_ID :assemblyMatch \
   AND HC.COMPONENT_TYPE = 'host'

queryLibRecipes = \
  SELECT MOD_RECIPE_LIB_ID, NAME \
    FROM lib_mod_recipe \
ORDER BY NAME

queryRecipeByName = \
  SELECT NAME \
    FROM lib_mod_recipe \
   WHERE NAME = ':recipe_name:'

queryRecipe = \
  SELECT MOD_RECIPE_LIB_ID, NAME, JAVA_CLASS, DESCRIPTION \
    FROM lib_mod_recipe \
   WHERE MOD_RECIPE_LIB_ID = ':recipe_id'

queryRecipes = \
  SELECT LMR.MOD_RECIPE_LIB_ID, LMR.NAME, LMR.JAVA_CLASS \
    FROM expt_trial_mod_recipe ETMR, lib_mod_recipe LMR \
   WHERE ETMR.MOD_RECIPE_LIB_ID = LMR.MOD_RECIPE_LIB_ID \
     AND ETMR.TRIAL_ID = ':trial_id' \
     AND ETMR.EXPT_ID = ':expt_id' \
ORDER BY ETMR.RECIPE_ORDER

queryRecipeProperties = \
  SELECT ARG_NAME, ARG_VALUE \
    FROM lib_mod_recipe_arg \
   WHERE MOD_RECIPE_LIB_ID = ':recipe_id' \
ORDER BY ARG_ORDER

# Change this to query from expt_trial_config_assembly
# This query used in OrganizerHelper to load the 
# experiment from the DB
queryExperiment = \
 SELECT ASSEMBLY_ID \
   FROM expt_trial_config_assembly \
  WHERE EXPT_ID = ':expt_id' \
    AND TRIAL_ID = ':trial_id'

queryExptDescriptions = \
 SELECT DISTINCT A.EXPT_ID, A.DESCRIPTION \
   FROM expt_experiment A, expt_trial B \
  WHERE A.EXPT_ID = B.EXPT_ID

queryTrials = \
 SELECT TRIAL_ID, DESCRIPTION \
   FROM expt_trial \
  WHERE EXPT_ID = ':expt_id'

querySocietyName = \
 SELECT DESCRIPTION \
   FROM asb_assembly \
  WHERE ASSEMBLY_ID = ':assembly_id:'

querySocietyByName = \
 SELECT DESCRIPTION \
   FROM asb_assembly \
  WHERE DESCRIPTION = ':society_name:'

queryPluginNames = \
 SELECT DISTINCT LC.COMPONENT_CLASS, AC.COMPONENT_ALIB_ID, AC.COMPONENT_LIB_ID, AC.COMPONENT_NAME, AC.COMPONENT_TYPE, HIER.PRIORITY, HIER.INSERTION_ORDER \
   FROM asb_component_hierarchy HIER, alib_component AC, alib_component APC, lib_component LC \
   WHERE APC.COMPONENT_NAME =':agent_name' \
     AND HIER.COMPONENT_ALIB_ID = AC.COMPONENT_ALIB_ID \
     AND HIER.ASSEMBLY_ID :assemblyMatch \
     AND HIER.PARENT_COMPONENT_ALIB_ID = APC.COMPONENT_ALIB_ID \
     AND AC.COMPONENT_LIB_ID = LC.COMPONENT_LIB_ID \
     AND AC.COMPONENT_TYPE :comp_type: \
   ORDER BY HIER.INSERTION_ORDER

queryComponents = \
 SELECT A.COMPONENT_NAME, C.COMPONENT_CLASS, \
        A.COMPONENT_ALIB_ID, H.INSERTION_ORDER AS INSERTION_ORDER \
   FROM alib_component A, \
        alib_component P, \
        asb_component_hierarchy H, \
        lib_component C \
  WHERE H.ASSEMBLY_ID :assemblyMatch \
    AND A.COMPONENT_ALIB_ID = H.COMPONENT_ALIB_ID \
    AND P.COMPONENT_ALIB_ID = H.PARENT_COMPONENT_ALIB_ID \
    AND C.COMPONENT_LIB_ID = A.COMPONENT_LIB_ID \
    AND C.INSERTION_POINT = ':insertion_point' \
    AND P.COMPONENT_NAME = ':parent_name' \
ORDER BY INSERTION_ORDER

queryAgentRelationships = \
  SELECT SUPPORTED_COMPONENT_ALIB_ID, ROLE, START_DATE, END_DATE \
    FROM asb_agent_relation \
   WHERE SUPPORTING_COMPONENT_ALIB_ID=':agent_name' \
     AND ASSEMBLY_ID :assemblyMatch
   
queryAgentAssetClass = \
  SELECT AGENT_ORG_CLASS \
    FROM lib_agent_org \
   WHERE AGENT_LIB_NAME = ':agent_name'

queryAgentClass = \
  SELECT L.COMPONENT_CLASS FROM lib_component L, asb_agent A \
   WHERE L.COMPONENT_LIB_ID = A.COMPONENT_LIB_ID \
     AND A.COMPONENT_NAME = ':agent_name' \
     AND A.ASSEMBLY_ID = ':assembly_id:'

queryAllAgentNames = \
  SELECT DISTINCT C.COMPONENT_NAME \
    FROM alib_component C \
    WHERE C.COMPONENT_TYPE = 'agent' \
    ORDER BY C.COMPONENT_NAME

# Get all experiments that have the given Society 
# where Society is specified by name (assembly description)
queryExptsWithSociety = \
  SELECT DISTINCT E.NAME \
    FROM expt_experiment E, expt_trial T, asb_assembly A, expt_trial_config_assembly C, expt_trial_assembly R \
	WHERE E.EXPT_ID = T.EXPT_ID \
        AND ((T.TRIAL_ID = R.TRIAL_ID \
	AND R.ASSEMBLY_ID = A.ASSEMBLY_ID) \
	OR \
	(T.TRIAL_ID = C.TRIAL_ID \
	AND C.ASSEMBLY_ID = A.ASSEMBLY_ID)) \
	AND A.DESCRIPTION = ':societyName'

# Get all experiments that have the given Recipe 
# where Recipe is specified by name
queryExptsWithRecipe = \
  SELECT DISTINCT E.NAME \
    FROM expt_experiment E, expt_trial_mod_recipe R, expt_trial T, lib_mod_recipe M \
	WHERE E.EXPT_ID = T.EXPT_ID \
        AND T.TRIAL_ID = R.TRIAL_ID \
	AND M.MOD_RECIPE_LIB_ID = R.MOD_RECIPE_LIB_ID \
	AND M.NAME = ':recipeName'


# Get all experiments that have the given Society
# where Society is specified by name
queryExptsWithSociety = \
	SELECT C.NAME \
	FROM expt_trial_assembly A, asb_assembly B, expt_experiment C \
	WHERE A.ASSEMBLY_ID = B.ASSEMBLY_ID \
	AND C.EXPT_ID = A.EXPT_ID \
	AND B.DESCRIPTION = ':societyName'

# get the property groups for a plugin
queryPGId = \
	SELECT DISTINCT PG_ATTRIBUTE_LIB_ID \
	FROM asb_agent_pg_attr \
	WHERE COMPONENT_ALIB_ID = ':agent_name' \
	AND ASSEMBLY_ID :assemblyMatch

queryPGAttrs = \
	SELECT PG_NAME, ATTRIBUTE_NAME, ATTRIBUTE_TYPE, AGGREGATE_TYPE \
	FROM lib_pg_attribute \
	WHERE PG_ATTRIBUTE_LIB_ID = ':pgAttrLibId'

queryPGValues = \
	SELECT ATTRIBUTE_VALUE, START_DATE, END_DATE \
	FROM asb_agent_pg_attr \
	WHERE PG_ATTRIBUTE_LIB_ID = ':pgAttrLibId' \
	AND COMPONENT_ALIB_ID = ':agent_name' \
	AND ASSEMBLY_ID :assemblyMatch \
	ORDER BY ATTRIBUTE_ORDER

queryCMTAssembly = \
	SELECT '1' \
	FROM expt_trial A, expt_trial_config_assembly B \
	WHERE A.EXPT_ID = ':expt_id:' \
	AND A.TRIAL_ID = B.TRIAL_ID \
	AND ASSEMBLY_ID LIKE 'CMT%'

queryAgentAssetData = \
        SELECT '1' \
        FROM asb_agent \
        WHERE ASSEMBLY_ID = ':assembly_id' \
        AND COMPONENT_ALIB_ID = ':agent:'

# Used to build convenience drop-down lists
queryGetPluginClasses = \
  SELECT DISTINCT COMPONENT_CLASS \
    FROM lib_component \
   WHERE COMPONENT_TYPE = 'plugin'

# Used to build convenience drop-down lists
queryGetBinderClasses = \
  SELECT DISTINCT COMPONENT_CLASS \
    FROM lib_component \
   WHERE COMPONENT_TYPE = 'agent binder'

queryGetAgentNames = \
SELECT DISTINCT COMPONENT_NAME \
   FROM alib_component \
  WHERE COMPONENT_TYPE = 'agent' 

##############################################
# Community editing queries follow
# See DatabaseTableModel and CommunityDBUtils in octc.ui.community
# Note that this code uses table and column names explicitly,
# including aliases, so the code must be edited if these
# queries are edited

queryCommunities = \
  SELECT DISTINCT COMMUNITY_ID \
    FROM community_attribute

queryMyCommunities = \
  SELECT DISTINCT COMMUNITY_ID \
    FROM community_attribute \
   WHERE ASSEMBLY_ID :assembly_match:

queryEntities = \
  SELECT DISTINCT ENTITY_ID \
    FROM community_entity_attribute \
   WHERE COMMUNITY_ID = ':community_id' \
     AND ASSEMBLY_ID :assembly_match:

queryEntityType = \
  SELECT ATTRIBUTE_VALUE \
    FROM community_entity_attribute \
   WHERE ENTITY_ID = ':entity_id' \
     AND ATTRIBUTE_ID = 'EntityType' \
     AND ASSEMBLY_ID :assembly_match:

queryAllCommunityInfo = \
  SELECT cea.COMMUNITY_ID, \
         cea.ENTITY_ID, \
         cea.ATTRIBUTE_ID, \
         cea.ATTRIBUTE_VALUE, \
         ca.ATTRIBUTE_ID AS COMMUNITY_ATTRIBUTE_ID, \
         ca.ATTRIBUTE_VALUE AS COMMUNITY_ATTRIBUTE_VALUE \
    FROM community_attribute ca, community_entity_attribute cea \
   WHERE ca.COMMUNITY_ID = \
            cea.COMMUNITY_ID \
     AND ca.COMMUNITY_ID = ':community_id' \
     AND ca.ASSEMBLY_ID :assembly_match: \
     AND cea.ASSEMBLY_ID = ca.ASSEMBLY_ID

queryCommunityInfo = \
  SELECT COMMUNITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE FROM community_attribute \
   WHERE COMMUNITY_ID = ':community_id' \
     AND ASSEMBLY_ID :assembly_match:

queryEntityInfo = \
  SELECT ENTITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE \
    FROM community_entity_attribute \
   WHERE COMMUNITY_ID = ':community_id' \
     AND ENTITY_ID = ':entity_id' \
     AND ASSEMBLY_ID :assembly_match:

queryChildrenEntityInfo = \
  SELECT ENTITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE \
    FROM community_entity_attribute \
   WHERE COMMUNITY_ID = ':community_id' \
     AND ENTITY_ID IN (':children_entity_ids') \
     AND ASSEMBLY_ID :assembly_match:

queryInsertCommunityInfo = \
  INSERT INTO community_attribute \
  VALUES (':assembly_id:', ':community_id', 'CommunityType', ':community_type')

queryInsertCommunityAttribute = \
  INSERT INTO community_attribute \
  VALUES (':assembly_id:', ':community_id', '', '')

queryInsertEntityInfo = \
  INSERT INTO community_entity_attribute \
  VALUES (':assembly_id:', ':community_id', ':entity_id', ':attribute_id', ':attribute_value')

queryInsertEntityAttribute = \
  INSERT INTO community_entity_attribute \
  VALUES (':assembly_id:', ':community_id', ':entity_id', '', '')

queryDeleteCommunityInfo = \
  DELETE FROM community_attribute \
   WHERE COMMUNITY_ID = ':community_id' \
     AND ASSEMBLY_ID :assembly_match:

queryDeleteEntityInfo = \
  DELETE FROM community_entity_attribute \
   WHERE COMMUNITY_ID = ':community_id' \
     AND ENTITY_ID = ':entity_id' \
     AND ASSEMBLY_ID :assembly_match:

# FIXME: qualify by assembly_id?
queryIsCommunityInUse = \
  SELECT ENTITY_ID \
    FROM community_entity_attribute \
   WHERE COMMUNITY_ID = ':community_id'

queryUpdateCommunityAttributeId  = \
  UPDATE community_attribute \
     SET ATTRIBUTE_ID = ':attribute_id' \
   WHERE COMMUNITY_ID = ':community_id' \
     AND ATTRIBUTE_ID = ':prev_attribute_id' \
     AND ATTRIBUTE_VALUE = ':attribute_value' \
     AND ASSEMBLY_ID :assembly_match:

queryUpdateCommunityAttributeValue = \
  UPDATE community_attribute \
     SET ATTRIBUTE_VALUE = ':attribute_value' \
   WHERE COMMUNITY_ID = ':community_id' \
     AND ATTRIBUTE_ID = ':attribute_id' \
     AND ATTRIBUTE_VALUE = ':prev_attribute_value' \
     AND ASSEMBLY_ID :assembly_match:

queryUpdateEntityAttributeId  = \
  UPDATE community_entity_attribute \
     SET ATTRIBUTE_ID = ':attribute_id' \
   WHERE COMMUNITY_ID = ':community_id' \
     AND ENTITY_ID = ':entity_id' \
     AND ATTRIBUTE_ID = ':prev_attribute_id' \
     AND ATTRIBUTE_VALUE = ':attribute_value' \
     AND ASSEMBLY_ID :assembly_match:

queryUpdateEntityAttributeValue = \
  UPDATE community_entity_attribute \
     SET ATTRIBUTE_VALUE = ':attribute_value' \
   WHERE COMMUNITY_ID = ':community_id' \
     AND ENTITY_ID = ':entity_id' \
     AND ATTRIBUTE_ID = ':attribute_id' \
     AND ATTRIBUTE_VALUE = ':prev_attribute_value' \
     AND ASSEMBLY_ID :assembly_match:

queryDeleteCommunityAttribute = \
  DELETE FROM community_attribute \
   WHERE COMMUNITY_ID = ':community_id' \
     AND ATTRIBUTE_ID = ':attribute_id' \
     AND ATTRIBUTE_VALUE = ':attribute_value' \
     AND ASSEMBLY_ID :assembly_match:

queryDeleteEntityAttribute = \
  DELETE FROM community_entity_attribute \
   WHERE COMMUNITY_ID = ':community_id' \
     AND ENTITY_ID = ':entity_id' \
     AND ATTRIBUTE_ID = ':attribute_id' \
     AND ATTRIBUTE_VALUE = ':attribute_value' \
     AND ASSEMBLY_ID :assembly_match:

