# PopulateDb.q
# Used by CSMART to populate the Configuration database, the assembly
# tables, with data for running the society.
# See org.cougaar.tools.csmart.core.db.PopulateDb
database=${org.cougaar.configuration.database}
username=${org.cougaar.configuration.user}
password=${org.cougaar.configuration.password}

# Get all assemblies used in running this trial
queryTrialAssemblies=\
 SELECT ASSEMBLY_ID FROM expt_trial_assembly \
  WHERE TRIAL_ID = ':trial_id:'

# Get all assemblies used in configuring this Trial
queryConfigTrialAssemblies=\
 SELECT ASSEMBLY_ID FROM expt_trial_config_assembly \
  WHERE TRIAL_ID = ':trial_id:'

# See if Assembly is used anywhere. Return nothing if not
checkUsedAssembly=\
 SELECT	'1' FROM expt_trial_assembly R, \
   expt_trial_config_assembly C, \
   lib_mod_recipe_arg N \
   WHERE (C.ASSEMBLY_ID = :assembly_id: \
      and C.TRIAL_ID != ':trial_id:') \
      OR (R.ASSEMBLY_ID = :assembly_id: \
      and R.TRIAL_ID != ':trial_id:') \
      OR N.ARG_VALUE = :assembly_id:

# Is this assembly in this trial's config
checkThisConfigUsesAssembly=\
 SELECT	'1' FROM \
   expt_trial_config_assembly C \
   WHERE C.ASSEMBLY_ID = :assembly_id: \
      and C.TRIAL_ID = ':trial_id:' \

# Is this assembly in this trial's runtime
checkThisRuntimeUsesAssembly=\
 SELECT	'1' FROM \
   expt_trial_assembly R \
   WHERE R.ASSEMBLY_ID = :assembly_id: \
      and R.TRIAL_ID = ':trial_id:' \

queryExptAlibComponents=\
 SELECT H.COMPONENT_ALIB_ID \
   FROM asb_component_hierarchy H \
        alib_component C \
  WHERE H.COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID \
    AND H.ASSEMBLY_ID :assembly_match:

insertAlibComponent=\
 INSERT INTO alib_component \
    (COMPONENT_ALIB_ID, COMPONENT_NAME, \
     COMPONENT_LIB_ID, \
     COMPONENT_TYPE, \
     CLONE_SET_ID) \
 VALUES (:component_alib_id:, :component_name:, :component_lib_id:, :component_category:, 0)

updateAlibComponent=\
 UPDATE alib_component \n\
    SET COMPONENT_NAME = :component_name:, \n\
        COMPONENT_LIB_ID = :component_lib_id:, \n\
        COMPONENT_TYPE = :component_category: \n\
 WHERE COMPONENT_ALIB_ID = :component_alib_id:

checkAlibComponent=\
 SELECT COMPONENT_NAME, COMPONENT_LIB_ID, COMPONENT_TYPE, CLONE_SET_ID \
   FROM alib_component \
  WHERE COMPONENT_ALIB_ID = :component_alib_id:

checkLibComponent=\
 SELECT COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT \
   FROM lib_component \
  WHERE COMPONENT_LIB_ID = :component_lib_id:

insertLibComponent=\
 INSERT INTO lib_component \
    (COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION) \
 VALUES (:component_lib_id:, :component_category:, :component_class:, :insertion_point:, :description:)

updateLibComponent=\
 UPDATE lib_component \n\
    SET COMPONENT_TYPE = :component_category:, \n\
        COMPONENT_CLASS = :component_class:, \n\
        INSERTION_POINT = :insertion_point: \n\
 WHERE COMPONENT_LIB_ID = :component_lib_id:

# Is this component already in the RUNTIME 
# configuration hierarchy for this Trial? Uses assembly_match
# Note the assumption that Alib IDs are unique for a parent
# Ideally it should be checking type, class, and parameters instead
checkComponentHierarchy=\
 SELECT COMPONENT_ALIB_ID \
   FROM asb_component_hierarchy \
  WHERE ASSEMBLY_ID :assembly_match: \
    AND COMPONENT_ALIB_ID = :component_alib_id:

insertComponentHierarchy=\
 INSERT INTO asb_component_hierarchy \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, \
     PARENT_COMPONENT_ALIB_ID, \
     PRIORITY, \
     INSERTION_ORDER) \
 VALUES (:assembly_id:, :component_alib_id:, :parent_component_alib_id:, :priority:, :insertion_order:)

# Used in populate
queryComponentArgs=\
 SELECT ARGUMENT, ARGUMENT_ORDER \
   FROM asb_component_arg \
  WHERE ASSEMBLY_ID :assembly_match: \
    AND COMPONENT_ALIB_ID = :component_alib_id: \
  ORDER BY ARGUMENT_ORDER, ARGUMENT
 
checkComponentArg=\
 SELECT COMPONENT_ALIB_ID \
   FROM asb_component_arg \
  WHERE ASSEMBLY_ID :assembly_match: \
    AND COMPONENT_ALIB_ID = :component_alib_id: \
    AND ARGUMENT = :argument_value: \
    AND ARGUMENT_ORDER = :argument_order:
 
insertComponentArg=\
 INSERT INTO asb_component_arg \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER) \
 VALUES (:assembly_id:, :component_alib_id:, :argument_value:, :argument_order:)

checkAgentOrg=\
 SELECT COMPONENT_LIB_ID \
   FROM lib_agent_org \
  WHERE COMPONENT_LIB_ID = :component_lib_id:

insertAgentOrg=\
 INSERT INTO lib_agent_org \
    (COMPONENT_LIB_ID, AGENT_LIB_NAME, AGENT_ORG_CLASS) \
 VALUES \
    (:component_lib_id:, :agent_lib_name:, :agent_org_class:)

# Is this relationship already in the RUNTIME configuration
# for this trial? Use assembly_match
checkRelationship=\
 SELECT '1' \
   FROM asb_agent_relation \
  WHERE ASSEMBLY_ID :assembly_match: \
    AND ROLE = :role: \
    AND SUPPORTING_COMPONENT_ALIB_ID = :supporting: \
    AND SUPPORTED_COMPONENT_ALIB_ID = :supported: \
    AND START_DATE = :start_date:

insertRelationship=\
 INSERT INTO asb_agent_relation \
    (ASSEMBLY_ID, ROLE, \
     SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, \
     START_DATE, END_DATE) \
 VALUES \
    (:assembly_id:, :role:, :supporting:, :supported:, :start_date:, :end_date:)

# Add a new PG attribute/value set
insertAttribute=\
 INSERT INTO asb_agent_pg_attr \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, \
     ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, \
     START_DATE, END_DATE) \
 VALUES \
    (:assembly_id:, :component_alib_id:, :pg_attribute_lib_id:, \
     :attribute_value:, :attribute_order:, \
     :start_date:, :end_date:)

# Check that this PG attribute/value pair is not already
# in the DB (before trying to insert it)
checkAttribute=\
 SELECT '1' FROM asb_agent_pg_attr  \
   WHERE COMPONENT_ALIB_ID = :component_alib_id: AND  \
         PG_ATTRIBUTE_LIB_ID = :pg_attribute_lib_id: AND  \
         ATTRIBUTE_VALUE = :attribute_value: AND \
         ATTRIBUTE_ORDER = :attribute_order: AND \
         START_DATE = :start_date: AND \
         ASSEMBLY_ID :assembly_match:
 
# CMT assemblies use attribute_orders of zero
# So need to do some special casing in some situations
queryAttributeValueZeroOrder=\
 SELECT ATTRIBUTE_VALUE FROM asb_agent_pg_attr  \
   WHERE COMPONENT_ALIB_ID = :component_alib_id: AND  \
         PG_ATTRIBUTE_LIB_ID = :pg_attribute_lib_id: AND  \
         ATTRIBUTE_ORDER = '0' AND \
         START_DATE = :start_date: AND \
         ASSEMBLY_ID :assembly_match:

queryLibPGAttribute=\
 SELECT PG_NAME, ATTRIBUTE_NAME, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_TYPE, AGGREGATE_TYPE \
   FROM lib_pg_attribute \
  WHERE PG_NAME = ':pg_name:'

insertLibPGAttribute=\
 INSERT INTO lib_pg_attribute \
   (PG_ATTRIBUTE_LIB_ID, PG_NAME, ATTRIBUTE_NAME, ATTRIBUTE_TYPE, AGGREGATE_TYPE) \
  VALUES \
     (:attribute_lib_id:, :pg_name:, :attribute_name:, \
      :attribute_type:, :aggregate_type:)

queryMaxExptId=\
 SELECT MAX(EXPT_ID) \
   FROM expt_experiment \
  WHERE EXPT_ID LIKE ':max_id_pattern:'

queryExptCFWGroupId=\
 SELECT CFW_GROUP_ID \
   FROM expt_experiment \
  WHERE EXPT_ID LIKE ':expt_id:'

queryExptName=\
 SELECT NAME \
   FROM expt_experiment \
  WHERE EXPT_ID LIKE ':expt_id:'

# Used from Experiment via PopulateDb to avoid complete resaves
updateExptName=\
 UPDATE expt_experiment SET NAME = ':expt_name:' \
  WHERE EXPT_ID LIKE ':expt_id:'

insertExptId=\
 INSERT INTO expt_experiment (EXPT_ID, DESCRIPTION, NAME, CFW_GROUP_ID) \
 VALUES (':expt_id:', ':description:', :expt_name:, ':cfw_group_id:')

queryMaxTrialId=\
 SELECT MAX(TRIAL_ID) \
   FROM expt_trial \
  WHERE TRIAL_ID LIKE ':max_id_pattern:'

insertTrialId=\
 INSERT INTO expt_trial (EXPT_ID, TRIAL_ID, DESCRIPTION, NAME) \
 VALUES (':expt_id:', ':trial_id:', ':description:', ':trial_name:')

queryMaxAssemblyId=\
 SELECT MAX(ASSEMBLY_ID) \
   FROM asb_assembly \
  WHERE ASSEMBLY_TYPE = ':assembly_type:' \
    AND ASSEMBLY_ID LIKE ':assembly_id_pattern:'

insertAssemblyId=\
 INSERT INTO asb_assembly (ASSEMBLY_ID, ASSEMBLY_TYPE, DESCRIPTION) \
 VALUES (:assembly_id:, ':assembly_type:', ':assembly_desc:')

# Used in PopulateDb.java when just added a CSA assembly
updateAssemblyDesc=\
 UPDATE asb_assembly SET DESCRIPTION = ':soc_desc:' \
   WHERE ASSEMBLY_ID = :assembly_id:

# Used in PopulateDb to create new assembly with name of old
queryAssemblyDesc=\
 SELECT DESCRIPTION FROM asb_assembly WHERE ASSEMBLY_ID = :assembly_id:

# Add new RUNTIME assembly
insertTrialAssembly=\
 INSERT INTO expt_trial_assembly (EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION) \
 VALUES (':expt_id:', ':trial_id:', :assembly_id:, ':assembly_type: assembly')

# Delete runtime assembly from trial
cleanTrialAssembly=\
 DELETE FROM expt_trial_assembly \
  WHERE EXPT_ID = ':expt_id:' \
    AND TRIAL_ID = ':trial_id:' \
    AND ASSEMBLY_ID IN :assemblies_to_clean:

# Add new CONFIG assembly to the trial
insertTrialConfigAssembly=\
 INSERT INTO expt_trial_config_assembly (EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION) \
 VALUES (':expt_id:', ':trial_id:', :assembly_id:, ':assembly_type: assembly')

# Delete config time assembly from trial
cleanTrialConfigAssembly=\
 DELETE FROM expt_trial_config_assembly \
  WHERE EXPT_ID = ':expt_id:' \
    AND TRIAL_ID = ':trial_id:' \
    AND ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBAssembly=\
 DELETE FROM asb_assembly \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBComponentArg=\
 DELETE FROM asb_component_arg \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBComponentHierarchy=\
 DELETE FROM asb_component_hierarchy \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBAgent=\
 DELETE FROM asb_agent \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBAgentPGAttr=\
 DELETE FROM asb_agent_pg_attr \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBAgentRel=\
 DELETE FROM asb_agent_relation \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBOplan=\
 DELETE FROM asb_oplan \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBOplanAAttr=\
 DELETE FROM asb_oplan_agent_attr \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBComm=\
 DELETE FROM community_attribute \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

cleanASBCommEntity=\
 DELETE FROM community_entity_attribute \
  WHERE ASSEMBLY_ID IN :assemblies_to_clean:

# Get the (single) Comm assembly from runtime
# for the given experiment / trial
queryExptCommAsb=\
 SELECT eta.ASSEMBLY_ID FROM expt_trial_config_assembly eta, \
                            asb_assembly aa \
  WHERE eta.EXPT_ID = ':expt_id:' \
    AND eta.TRIAL_ID = ':trial_id:' \
    AND aa.ASSEMBLY_TYPE = ':comm_type:' \
    AND aa.ASSEMBLY_ID = eta.ASSEMBLY_ID

# Get list of non CMT assemblies in RUNTIME configuration
queryAssembliesToClean=\
 SELECT AA.ASSEMBLY_ID \
   FROM asb_assembly AA, expt_trial_assembly ETA \
  WHERE AA.ASSEMBLY_ID = ETA.ASSEMBLY_ID \
    AND ETA.TRIAL_ID = ':trial_id:' \
    AND AA.ASSEMBLY_TYPE != ':cmt_type:'

# Get all the CSMI and CSHNA assemblies
# Used to find orphaned assemblies
queryNonSocietyAssemblies=\
 SELECT ASSEMBLY_ID \
   FROM asb_assembly \
 WHERE ASSEMBLY_TYPE NOT IN (':cmt_type:', ':csa_type:')

# Get list of config assemblies of either of 2 types in this trial
# Used to make sure that only most recent society definition included
queryOldSocietyConfigAssembliesToClean=\
 SELECT ETC.ASSEMBLY_ID \
   FROM expt_trial_config_assembly ETC, asb_assembly AA \
  WHERE ETC.ASSEMBLY_ID = AA.ASSEMBLY_ID \
    AND AA.ASSEMBLY_TYPE IN (':cmt_type:', ':csa_type:') \
    AND ETC.TRIAL_ID = ':trial_id:'

# Get list of runtime assemblies of either of 2 types in this trial
# Used to make sure that only most recent society definition included
queryOldSocietyRuntimeAssembliesToClean=\
 SELECT ETC.ASSEMBLY_ID \
   FROM expt_trial_assembly ETC, asb_assembly AA \
  WHERE ETC.ASSEMBLY_ID = AA.ASSEMBLY_ID \
    AND AA.ASSEMBLY_TYPE IN (':cmt_type:', ':csa_type:') \
    AND ETC.TRIAL_ID = ':trial_id:'

copyCMTAssembliesQueryNames=copyCMTAssemblies

# Copy RUNTIME CMT assemblies from old trial to new
copyCMTAssemblies=\
 INSERT INTO expt_trial_assembly (EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION) \
 SELECT ':expt_id:', ':new_trial_id:', ETA.ASSEMBLY_ID, ETA.DESCRIPTION \
   FROM expt_trial_assembly ETA, asb_assembly AA \
  WHERE ETA.ASSEMBLY_ID = AA.ASSEMBLY_ID \
    AND ETA.TRIAL_ID = ':old_trial_id:' \
    AND AA.ASSEMBLY_TYPE = ':cmt_type:'

copyCMTAssembliesQueryNames.mysql=copyCMTAssemblies1 copyCMTAssemblies2 copyCMTAssemblies3

# Copy RUNTIME CMT assemblies from old trial to new
copyCMTAssemblies1=\
 CREATE TEMPORARY TABLE `tmp_cmta_:expt_id:` AS \
 SELECT ':expt_id:' as EXPT_ID, \
        ':new_trial_id:' as TRIAL_ID, \
        ETA.ASSEMBLY_ID as ASSEMBLY_ID, \
        ETA.DESCRIPTION as DESCRIPTION \
   FROM expt_trial_assembly ETA, asb_assembly AA \
  WHERE ETA.ASSEMBLY_ID = AA.ASSEMBLY_ID \
    AND ETA.TRIAL_ID = ':old_trial_id:' \
    AND AA.ASSEMBLY_TYPE = ':cmt_type:'

copyCMTAssemblies2=\
 INSERT INTO expt_trial_assembly (EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION) \
 SELECT EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION \
   FROM `tmp_cmta_:expt_id:`

copyCMTAssemblies3=DROP TABLE `tmp_cmta_:expt_id:`

copyCMTConfigAssembliesQueryNames=copyCMTConfigAssemblies

# Copy CONFIG CMT assemblies from old trial to new
copyCMTConfigAssemblies=\
 INSERT INTO expt_trial_config_assembly (EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION) \
 SELECT ':expt_id:', ':new_trial_id:', ETA.ASSEMBLY_ID, ETA.DESCRIPTION \
   FROM expt_trial_config_assembly ETA, asb_assembly AA \
  WHERE ETA.ASSEMBLY_ID = AA.ASSEMBLY_ID \
    AND ETA.TRIAL_ID = ':old_trial_id:' \
    AND AA.ASSEMBLY_TYPE = ':cmt_type:'

copyCMTConfigAssembliesQueryNames.mysql=copyCMTConfigAssemblies1 copyCMTConfigAssemblies2 copyCMTConfigAssemblies3

# Copy CONFIG CMT assemblies from old trial to new
copyCMTConfigAssemblies1=\
 CREATE TEMPORARY TABLE `tmp_cmta_:expt_id:` AS \
 SELECT ':expt_id:' as EXPT_ID, \
        ':new_trial_id:' as TRIAL_ID, \
        ETA.ASSEMBLY_ID as ASSEMBLY_ID, \
        ETA.DESCRIPTION as DESCRIPTION \
   FROM expt_trial_config_assembly ETA, asb_assembly AA \
  WHERE ETA.ASSEMBLY_ID = AA.ASSEMBLY_ID \
    AND ETA.TRIAL_ID = ':old_trial_id:' \
    AND AA.ASSEMBLY_TYPE = ':cmt_type:'

copyCMTConfigAssemblies2=\
 INSERT INTO expt_trial_config_assembly (EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION) \
 SELECT EXPT_ID, TRIAL_ID, ASSEMBLY_ID, DESCRIPTION \
   FROM `tmp_cmta_:expt_id:`

copyCMTConfigAssemblies3=DROP TABLE `tmp_cmta_:expt_id:`

# Copy the CMT Threads from one Trial to another
copyCMTThreadsQueryNames.oracle=copyCMTThreads

copyCMTThreads=\
 INSERT INTO expt_trial_thread (EXPT_ID, TRIAL_ID, THREAD_ID) \
 SELECT ':expt_id:', ':new_trial_id:', THREAD_ID \
   FROM expt_trial_thread \
  WHERE TRIAL_ID = ':old_trial_id:'

copyCMTThreadsQueryNames.mysql=copyCMTThreads1 copyCMTThreads2 copyCMTThreads3

copyCMTThreads1=\
 CREATE TEMPORARY TABLE `tmp_cmtt_:expt_id:` AS \
 select THREAD_ID \
   FROM expt_trial_thread \
  WHERE TRIAL_ID = ':old_trial_id:'

copyCMTThreads2=\
 INSERT INTO expt_trial_thread (EXPT_ID, TRIAL_ID, THREAD_ID) \
 SELECT ':expt_id:', ':new_trial_id:', THREAD_ID \
   FROM `tmp_cmtt_:expt_id:`

copyCMTThreads3=DROP TABLE `tmp_cmtt_:expt_id:`

# OPLAN Copy stuff
copyOPLANQueryNames.oracle=copyOPLANEntry copyOPLANAttrEntries

copyOPLANEntry=\
 INSERT INTO asb_oplan (ASSEMBLY_ID, OPLAN_ID, OPERATION_NAME, PRIORITY, C0_DATE) \
 SELECT ':new_assembly_id:', OPLAN_ID, OPERATION_NAME, PRIORITY, C0_DATE \
	FROM asb_oplan \
   WHERE ASSEMBLY_ID = ':old_assembly_id:'

copyOPLANAttrEntries=\
 INSERT INTO asb_oplan_agent_attr (ASSEMBLY_ID, OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME, END_CDAY, ATTRIBUTE_VALUE) \
   SELECT ':new_assembly_id:', OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME, END_CDAY, ATTRIBUTE_VALUE \
       FROM asb_oplan_agent_attr \
    WHERE ASSEMBLY_ID = ':old_assembly_id:'

copyOPLANQueryNames.mysql=copyOPLANEntry1 copyOPLANEntry2 copyOPLANEntry3 copyOPLANAttrEntries1 copyOPLANAttrEntries2 copyOPLANAttrEntries3

copyOPLANEntry1=\
 CREATE TEMPORARY TABLE `tmp_ope_:old_assembly_id:` AS \
   SELECT OPLAN_ID, OPERATION_NAME, PRIORITY, C0_DATE \
    FROM asb_oplan \
   WHERE ASSEMBLY_ID = ':old_assembly_id:'

copyOPLANEntry2=\
 INSERT INTO asb_oplan (ASSEMBLY_ID, OPLAN_ID, OPERATION_NAME, PRIORITY, C0_DATE) \
  SELECT ':new_assembly_id:', OPLAN_ID, OPERATION_NAME, PRIORITY, C0_DATE \
   FROM `tmp_ope_:old_assembly_id:`

copyOPLANEntry3=DROP TABLE `tmp_ope_:old_assembly_id:`

copyOPLANAttrEntries1=\
 CREATE TEMPORARY TABLE `tmp_opae_:old_assembly_id:` AS \
   SELECT OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME, END_CDAY, ATTRIBUTE_VALUE \
  FROM asb_oplan_agent_attr \
   WHERE ASSEMBLY_ID = ':old_assembly_id:'

copyOPLANAttrEntries2=\
 INSERT INTO asb_oplan_agent_attr (ASSEMBLY_ID, OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME, END_CDAY, ATTRIBUTE_VALUE) \
   SELECT ':new_assembly_id:', OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME, END_CDAY, ATTRIBUTE_VALUE \
    FROM `tmp_opae_:old_assembly_id:`

copyOPLANAttrEntries3=DROP TABLE `tmp_opae_:old_assembly_id:`
# End of OPLAN copy stuff

# Copy Community Assembly
copyCommQueryNames.oracle=copyCommAttr copyCommEntityAttr

copyCommAttr=\
 INSERT INTO community_attribute (ASSEMBLY_ID, COMMUNITY_ID, \
                                 ATTRIBUTE_ID, ATTRIBUTE_VALUE) \
 SELECT ':new_assembly_id:', COMMUNITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE \
	FROM community_attribute \
   WHERE ASSEMBLY_ID = ':old_assembly_id:'

copyCommEntityAttr=\
 INSERT INTO community_entity_attribute (ASSEMBLY_ID, COMMUNITY_ID, \
                         ENTITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE) \
   SELECT ':new_assembly_id:', COMMUNITY_ID, ENTITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE \
       FROM community_entity_attribute \
    WHERE ASSEMBLY_ID = ':old_assembly_id:'

copyCommQueryNames.mysql=copyCommAttr1 copyCommAttr2 copyCommAttr3 copyCommEntityAttr1 copyCommEntityAttr2 copyCommEntityAttr3

copyCommAttr1=\
 CREATE TEMPORARY TABLE `tmp_ca_:old_assembly_id:` AS \
   SELECT COMMUNITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE \
    FROM community_attribute \
   WHERE ASSEMBLY_ID = ':old_assembly_id:'

copyCommAttr2=\
 INSERT INTO community_attribute (ASSEMBLY_ID, COMMUNITY_ID, \
                          ATTRIBUTE_ID, ATTRIBUTE_VALUE) \
  SELECT ':new_assembly_id:', COMMUNITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE \
   FROM `tmp_ca_:old_assembly_id:`

copyCommAttr3=DROP TABLE `tmp_ca_:old_assembly_id:`

copyCommEntityAttr1=\
 CREATE TEMPORARY TABLE `tmp_cea_:old_assembly_id:` AS \
   SELECT COMMUNITY_ID, ENTITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE \
  FROM community_entity_attribute \
   WHERE ASSEMBLY_ID = ':old_assembly_id:'

copyCommEntityAttr2=\
 INSERT INTO community_entity_attribute (ASSEMBLY_ID, COMMUNITY_ID, \
     ENTITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE) \
   SELECT ':new_assembly_id:', COMMUNITY_ID, ENTITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE \
    FROM `tmp_cea_:old_assembly_id:`

copyCommEntityAttr3=DROP TABLE `tmp_cea_:old_assembly_id:`
# End of Community copy stuff

queryLibRecipeByName=\
 SELECT MOD_RECIPE_LIB_ID, JAVA_CLASS \
   FROM lib_mod_recipe \
  WHERE NAME = ':recipe_name:'

queryLibRecipeProps=\
 SELECT ARG_NAME, ARG_VALUE \
   FROM lib_mod_recipe_arg \
  WHERE MOD_RECIPE_LIB_ID = ':recipe_id:' \
  ORDER BY ARG_ORDER

queryMaxRecipeId=\
 SELECT MAX(MOD_RECIPE_LIB_ID) \
   FROM lib_mod_recipe \
  WHERE MOD_RECIPE_LIB_ID LIKE ':max_id_pattern:'

# PdbBase.reallyChangeRecipeName
updateRecipeName=\
 UPDATE lib_mod_recipe \
    SET NAME = ':new_name:' \
  WHERE NAME = ':old_name:'

insertLibRecipe=\
 INSERT INTO lib_mod_recipe \
    (MOD_RECIPE_LIB_ID, NAME, JAVA_CLASS, DESCRIPTION) \
 VALUES (':recipe_id:', ':recipe_name:', ':java_class:', ':description:')

updateLibRecipe=\
 UPDATE lib_mod_recipe \
   SET NAME = ':recipe_name:', JAVA_CLASS=':java_class:' \
   WHERE MOD_RECIPE_LIB_ID = ':recipe_id:'

insertLibRecipeProp=\
 INSERT INTO lib_mod_recipe_arg \
    (MOD_RECIPE_LIB_ID, ARG_NAME, ARG_VALUE, ARG_ORDER) \
 VALUES (':recipe_id:', ':arg_name:', ':arg_value:', ':arg_order:')

deleteLibRecipe=\
 DELETE FROM lib_mod_recipe \
  WHERE MOD_RECIPE_LIB_ID = ':recipe_id:'

deleteLibRecipeArgs=\
 DELETE FROM lib_mod_recipe_arg \
  WHERE MOD_RECIPE_LIB_ID = ':recipe_id:'

queryRecipeUsed=\
 SELECT TRIAL_ID FROM expt_trial_mod_recipe \
   WHERE MOD_RECIPE_LIB_ID = ':recipe_id:'

queryRecipeAssemblyId=\
 SELECT ARG_VALUE \
   FROM lib_mod_recipe_arg \
  WHERE MOD_RECIPE_LIB_ID = ':recipe_id:' \
   AND ARG_NAME = 'Assembly Id' 

insertTrialRecipe=\
 INSERT INTO expt_trial_mod_recipe \
    (TRIAL_ID, MOD_RECIPE_LIB_ID, RECIPE_ORDER, EXPT_ID) \
 VALUES (':trial_id:', ':recipe_id:', ':recipe_order:', ':expt_id:')

cleanTrialRecipe=\
 DELETE FROM expt_trial_mod_recipe \
  WHERE TRIAL_ID = ':trial_id:'

# Is this agent listed in any of the matching assemblies?
checkAsbAgent=\
 SELECT '1' \
   FROM asb_agent \
  WHERE COMPONENT_ALIB_ID = :component_alib_id: \
    AND ASSEMBLY_ID :assembly_match:

insertAsbAgent=\
 INSERT INTO asb_agent \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME) \
 VALUES \
    (:assembly_id:, :component_alib_id:, :component_lib_id:, :clone_set_id:, :component_name:)

########
# Sample recipe queries follow
# ALL queries that you use in your recipes must be included in
# this file or a file called recipeQueries.q which you create
# and place on your ConfigPath (typically in this directory)

# Typical usage therefore is to create a new query by editing this file,
# copying one of the provided sample queries.
# Note that the available substitutions include:
# :assembly_match: (ie "in ('CSMI-0001','ASB2','ASB3')")
# :expt_id: (ie EXPT-0001)
# :trial_id: (ie EXPT-0001.TRIAL)

# First, sample target component queries: where to insert the component
# First, those that look for agents

# Find all agents
recipeQueryAllAgents=\
 SELECT C.COMPONENT_ALIB_ID \
   FROM alib_component C, asb_component_hierarchy H \
  WHERE C.COMPONENT_TYPE='agent' \
    AND (H.COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID OR H.PARENT_COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID) \
    AND H.ASSEMBLY_ID :assembly_match:

# Find all agents including NodeAgents
recipeQueryAllAgentsAndNodeAgents=\
 SELECT C.COMPONENT_ALIB_ID \
   FROM alib_component C, asb_component_hierarchy H \
  WHERE (C.COMPONENT_TYPE='agent' OR C.COMPONENT_TYPE='node') \
    AND (H.COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID OR H.PARENT_COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID) \
    AND H.ASSEMBLY_ID :assembly_match:

# Find some agents by name - in this case, subordinates of 2BDE
recipeQuery2BDE_Sub_AgentsByName=\
 SELECT C.COMPONENT_ALIB_ID \
   FROM alib_component C, asb_component_hierarchy H \
  WHERE C.COMPONENT_TYPE='agent' \
    AND (H.COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID OR H.PARENT_COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID) \
    AND H.ASSEMBLY_ID :assembly_match: \
    AND C.COMPONENT_NAME in ('2-7-INFBN', '3-69-ARBN', '3-FSB')

# Find the subordinates of 3BDE. By changing the agent name and the role,
# you can do different relationships. Note that this must be a direct relationship - 
# this is not transitive. Also note that if you have recipes that add
# relevant agent OrgAssets or change relationships, you must change your CSMART
# startup scripts to supply the -D argument
# org.cougaar.tools.csmart.allowComplexRecipeQueries. See the startup
# scripts for details.
recipeQuerySubordinatesOf3_BDE_2ID_HHC=\
 SELECT SPTG.COMPONENT_ALIB_ID \
   FROM alib_component SPTG, alib_component SPTD, asb_agent_relation R \
  WHERE R.SUPPORTED_COMPONENT_ALIB_ID = SPTD.COMPONENT_ALIB_ID \
    AND R.SUPPORTING_COMPONENT_ALIB_ID = SPTG.COMPONENT_ALIB_ID \
    AND R.ASSEMBLY_ID :assembly_match: \
    AND R.ROLE = 'Subordinate' \
    AND SPTD.COMPONENT_NAME = '3-BDE-2ID-HHC'

# Similar to above, but a different agent name
recipeQuerySubordinatesOf2_BDE_3ID_HHC=\
 SELECT SPTG.COMPONENT_ALIB_ID \
   FROM alib_component SPTG, alib_component SPTD, asb_agent_relation R \
  WHERE R.SUPPORTED_COMPONENT_ALIB_ID = SPTD.COMPONENT_ALIB_ID \
    AND R.SUPPORTING_COMPONENT_ALIB_ID = SPTG.COMPONENT_ALIB_ID \
    AND R.ASSEMBLY_ID :assembly_match: \
    AND R.ROLE = 'Subordinate' \
    AND SPTD.COMPONENT_NAME = '2-BDE-3ID-HHC'

# Next, sample queries where the target is a Node (for inserting Agents or
# Agent level Binders)
# First, get all nodes in the experiment
recipeQueryAllNodes =\
 SELECT C.COMPONENT_ALIB_ID \
   FROM alib_component C, asb_component_hierarchy H \
  WHERE C.COMPONENT_TYPE='node' \
    AND (H.PARENT_COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID OR \
         H.COMPONENT_ALIB_ID = C.COMPONENT_ALIB_ID) \
    AND H.ASSEMBLY_ID :assembly_match:

# Then, get a set of Nodes by name
recipeQuerySetOfNodes =\
 SELECT COMPONENT_ALIB_ID \
   FROM alib_component \
  WHERE COMPONENT_TYPE = 'node' \
  AND COMPONENT_NAME IN ('Name1', 'Name2')

# Finally, get some Nodes by specifying the names of the agent
# that should be in those Nodes
recipeQueryNodesWithSpecificAgents =\
 SELECT N.COMPONENT_ALIB_ID \
   FROM alib_component A, asb_component_hierarchy H, alib_component N \
  WHERE A.COMPONENT_ALIB_ID = H.COMPONENT_ALIB_ID \
   AND H.PARENT_COMPONENT_ALIB_ID = N.COMPONENT_ALIB_ID \
   AND N.COMPONENT_TYPE='node' \
   AND H.ASSEMBLY_ID :assembly_match: \
   AND A.COMPONENT_NAME='Agent Name'

### Now, queries to get the component to insert
# These examples don't actually get the data from the DB,
# but rather, hard code the values.
# We are retrieving the component name, type, and class
recipeQueryExampleBinderSpecification=\
 SELECT 'org.cougaar.core.examples.PluginServiceFilter', 'agent binder', 'org.cougaar.core.examples.PluginServiceFilter' \
   FROM dual

# Here we load the MIC TechSpecBinder. Be sure that techspecs.jar (built
# against your version of Cougaar and including the appropriate
# default_techspecs.xml is in CIP/sys on all machines)
recipeQueryMICBinder=\
 SELECT 'com.mobile_intelligence.contracts.TechSpecBinderFactory', 'agent binder', 'com.mobile_intelligence.contracts.TechSpecBinderFactory' \
   FROM dual

#####
# Next, queries to get the arguments to the inserted component.
# The number of arguments is arbitrary - use the last selectNothing query if you
# don't need any arguments

# Here we supply the properties for the MIC TechSpecBinder for one community in capture mode
# Note that you will want to put the property file on the ConfigPath.
# One option is to create a separate directory for these files,
# And add the a -D argument to the appropriate Nodes to include that directory on
# the config path, as in:
# org.cougaar.config.path="C\:\\Cougaar\\configs\\mic\;"
recipeQueryMICBinderParams2BDEPropFileCaptureMode=\
 SELECT 'file=2bde.properties', 'CAPTURE' FROM dual WHERE DUMMY = 'X'

recipeQueryMICBinderParamsOnePropFileMonitorMode=\
 SELECT 'file=2bde.properties', 'MONITOR' FROM dual WHERE DUMMY = 'X'

# Here is a query that gives no arguments to the component.
recipeQueryExampleBinderArgs=\
 SELECT NULL, NULL FROM dual WHERE DUMMY IS NULL

recipeQuerySelectNothing=\
 SELECT * FROM dual WHERE DUMMY IS NULL

recipeQueryNCAAgent=\
 SELECT COMPONENT_ALIB_ID FROM alib_component WHERE COMPONENT_TYPE = 'agent' AND COMPONENT_NAME='NCA'

recipeQuery47FSBAgent=\
 SELECT COMPONENT_ALIB_ID FROM alib_component WHERE COMPONENT_TYPE = 'agent' AND COMPONENT_NAME='47-FSB'

# For Adding UniversalAllocator or AmmoPacker to OSC
recipeQueryOSCAgent=\
 SELECT COMPONENT_ALIB_ID FROM alib_component WHERE COMPONENT_TYPE = 'agent' AND COMPONENT_NAME='OSC'
