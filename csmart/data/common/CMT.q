# CMT.q
# Queries to take the society rules & type definitions in the CFW
# portion of the CSMART configuration database, and produce runnable
# experiments - in particular, the CMT assembly of those experiments.
# See org.cougaar.tools.csmart.core.db.CMT

database=${org.cougaar.configuration.database}
username=${org.cougaar.configuration.user}
password=${org.cougaar.configuration.password}

getAssemblyIDOnExpt = \
 SELECT ASSEMBLY_ID \
   FROM expt_trial_config_assembly \
  WHERE expt_id = ':experiment_id' \
   AND ASSEMBLY_ID LIKE 'CMT-%'

updateAssemblyIDOnExpt = \
 UPDATE expt_trial_config_assembly \
 SET ASSEMBLY_ID = ':assembly_id' \
 WHERE expt_id = ':experiment_id' \
 AND ASSEMBLY_ID LIKE 'CMT-%'

updateRuntimeAssemblyIDOnExpt = \
 UPDATE expt_trial_assembly \
 SET ASSEMBLY_ID = ':assembly_id' \
 WHERE expt_id = ':experiment_id' \
 AND ASSEMBLY_ID LIKE 'CMT-%'

getCFWInstancesFromGroup = \
 SELECT CFW_ID FROM cfw_group_member WHERE CFW_GROUP_ID=':cfw_group_id'

unusedAssemblies = \
 SELECT DISTINCT AA.ASSEMBLY_ID FROM \
  asb_assembly AA \
  WHERE AA.ASSEMBLY_ID NOT IN \
  (SELECT DISTINCT ASSEMBLY_ID FROM expt_trial_assembly) \
   AND \
   AA.ASSEMBLY_ID NOT IN \
  (SELECT DISTINCT ASSEMBLY_ID FROM expt_trial_config_assembly) \
   AND \
   AA.ASSEMBLY_ID NOT IN \	
  (SELECT DISTINCT ARG_VALUE FROM lib_mod_recipe_arg)

# FIXME - this double left join may not work, may be slow
unusedAssemblies.mysql = \
 SELECT DISTINCT AA.ASSEMBLY_ID FROM \
  asb_assembly AA LEFT JOIN  expt_trial_config_assembly ETA \
  ON (AA.ASSEMBLY_ID=ETA.ASSEMBLY_ID) \
    LEFT JOIN expt_trial_assembly ETAR \
  ON (AA.ASSEMBLY_ID=ETAR.ASSEMBLY_ID) \
    LEFT JOIN lib_mod_recipe_arg LMRA \
  ON (AA.ASSEMBLY_ID=LMRA.ARG_VALUE) \
  WHERE ETA.ASSEMBLY_ID IS NULL \
   AND ETAR.ASSEMBLY_ID IS NULL \
   AND LMRA.ARG_VALUE IS NULL

#unusedAssemblies.mysql = \
# SELECT AA.ASSEMBLY_ID FROM \
#  asb_assembly AA LEFT JOIN  expt_trial_config_assembly ETA \
#  ON (AA.ASSEMBLY_ID=ETA.ASSEMBLY_ID) \
#  WHERE ETA.ASSEMBLY_ID IS NULL

insertASBAssembly = \
 INSERT INTO asb_assembly (ASSEMBLY_ID,ASSEMBLY_TYPE,DESCRIPTION) \
  values (:assembly_id,'CMT',:assembly_description)


addClonedASBAgentsQueries = addClonedASBAgents 
addClonedASBAgents = \
 INSERT INTO  asb_agent \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME) \
    SELECT DISTINCT ':assembly_id'  AS ASSEMBLY_ID, \
      (CS.CLONE_SET_ID || '-' || OGOM.ORG_ID) AS COMPONENT_ALIB_ID,	
      OGOM.ORG_ID AS COMPONENT_LIB_ID, \
      CS.CLONE_SET_ID AS CLONE_SET_ID, \
      AC.COMPONENT_NAME AS COMPONENT_NAME \
    FROM cfw_org_group_org_member OGOM, \
         alib_component AC, \
	 lib_clone_set CS \
    WHERE OGOM.ORG_GROUP_ID= ':org_group_id'
          AND AC.COMPONENT_ALIB_ID=(CS.CLONE_SET_ID || '-' || OGOM.ORG_ID) \
	  AND CS.CLONE_SET_ID>0 AND CS.CLONE_SET_ID< :n \
	  AND NOT EXISTS \
	  (SELECT COMPONENT_ALIB_ID \
	    FROM  asb_agent AA \
	    WHERE \
	     AA.COMPONENT_ALIB_ID=(CS.CLONE_SET_ID || '-' || OGOM.ORG_ID) \
	     AND AA.ASSEMBLY_ID=':assembly_id' )


addClonedASBAgentsQueries.mysql = addClonedASBAgentsCreateTable addClonedASBAgentsInsert  addClonedASBAgentsDrop
addClonedASBAgentsCreateTable.mysql = \
 CREATE TEMPORARY TABLE tmp_asb_agent_:short_assembly_id AS \
 SELECT DISTINCT ':assembly_id'  AS ASSEMBLY_ID, \
    concat(CS.CLONE_SET_ID,'-',OGOM.ORG_ID) AS COMPONENT_ALIB_ID, \
    OGOM.ORG_ID AS COMPONENT_LIB_ID, \
    CS.CLONE_SET_ID AS CLONE_SET_ID, \
    AC.COMPONENT_NAME AS COMPONENT_NAME \
  FROM cfw_org_group_org_member OGOM, \
       alib_component AC, \
       lib_clone_set CS \
       LEFT JOIN asb_agent AA ON \
           (AA.COMPONENT_ALIB_ID=concat(CS.CLONE_SET_ID,'-',OGOM.ORG_ID) \
	    AND AA.ASSEMBLY_ID=':assembly_id' ) \
  WHERE OGOM.ORG_GROUP_ID= ':org_group_id' \
       AND AA.ASSEMBLY_ID IS NULL \
       AND AC.COMPONENT_ALIB_ID=concat(CS.CLONE_SET_ID,'-',OGOM.ORG_ID) \
       AND CS.CLONE_SET_ID>0 AND CS.CLONE_SET_ID< :n

addClonedASBAgentsInsert.mysql = \
 INSERT INTO  asb_agent \
    (ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME) \
 SELECT ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME \
    FROM tmp_asb_agent_:short_assembly_id

addClonedASBAgentsDrop.mysql = \
    DROP TABLE tmp_asb_agent_:short_assembly_id


addNewBaseAgentAlibComponentsQueries = addNewBaseAgentAlibComponents
addNewBaseAgentAlibComponents = \
 INSERT INTO alib_component \
 (COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID) \
 SELECT DISTINCT \
        GO.ORG_ID AS COMPONENT_ALIB_ID, \
        ORG.ORG_ID AS COMPONENT_NAME, \
	ORG.ORG_ID AS COMPONENT_LIB_ID, \
	'agent' AS COMPONENT_TYPE, \
	0 as  CLONE_SET_ID \
    FROM  cfw_group_org GO, \
          lib_organization ORG \
      WHERE GO.CFW_GROUP_ID= ':cfw_group_id' \
      AND GO.ORG_ID =ORG.ORG_ID \
      AND GO.ORG_ID NOT IN \
       (SELECT COMPONENT_ALIB_ID FROM  alib_component)

addNewBaseAgentAlibComponentsQueries.mysql = addNewBaseAgentAlibComponentsCreateTable addNewBaseAgentAlibComponentsInsert addNewBaseAgentAlibComponentsDrop
addNewBaseAgentAlibComponentsCreateTable = \
CREATE TEMPORARY TABLE tmp_alib_component_:short_assembly_id AS \
 SELECT DISTINCT \
	GO.ORG_ID AS COMPONENT_ALIB_ID, \
        ORG.ORG_ID AS COMPONENT_NAME, \
	ORG.ORG_ID AS COMPONENT_LIB_ID, \
	'agent' AS COMPONENT_TYPE, \
	0 as  CLONE_SET_ID \
 FROM  cfw_group_org GO, \
	lib_organization ORG \
	LEFT JOIN alib_component CAI ON (CAI.COMPONENT_ALIB_ID=GO.ORG_ID) \
 WHERE GO.CFW_GROUP_ID= ':cfw_group_id' \
    AND CAI.COMPONENT_ALIB_ID IS NULL \
	AND GO.ORG_ID =ORG.ORG_ID

addNewBaseAgentAlibComponentsInsert = \
 INSERT INTO alib_component \
    (COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID) \
 SELECT COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID \
    FROM tmp_alib_component_:short_assembly_id

addNewBaseAgentAlibComponentsDrop = \
 DROP TABLE tmp_alib_component_:short_assembly_id

# check how this should be based on the CFW_GROUP_ID!
addNewClonedAgentAlibComponentsQueries = addNewClonedAgentAlibComponents
addNewClonedAgentAlibComponents = \
 INSERT INTO alib_component \
 (COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID) \
 SELECT DISTINCT \
      (CLONE_SET_ID || '-' || OGOM.ORG_ID) AS COMPONENT_ALIB_ID, \
      (CLONE_SET_ID || '-' || OGOM.ORG_ID) AS COMPONENT_NAME, \
      OGOM.ORG_ID AS COMPONENT_LIB_ID, \
      'agent' AS COMPONENT_TYPE, \
      CLONE_SET_ID AS  CLONE_SET_ID \
     FROM  cfw_org_group_org_member OGOM, \
      lib_clone_set cs \
      WHERE OGOM.ORG_GROUP_ID=':org_group_id' \
      AND CS.CLONE_SET_ID>0 AND CS.CLONE_SET_ID< :n \
      AND (CLONE_SET_ID || '-' || OGOM.ORG_ID) NOT IN \
       (SELECT COMPONENT_ALIB_ID FROM  alib_component)

addNewClonedAgentAlibComponentsQueries.mysql = addNewClonedAgentAlibComponentsCreateTable addNewClonedAgentAlibComponentsInsert addNewClonedAgentAlibComponentsDrop
addNewClonedAgentAlibComponentsCreateTable = \
CREATE TEMPORARY TABLE tmp_alib_component_:short_assembly_id AS \
 SELECT DISTINCT \
   CONCAT(CLONE_SET_ID,'-',OGOM.ORG_ID) AS COMPONENT_ALIB_ID, \
   CONCAT(CLONE_SET_ID,'-',OGOM.ORG_ID) AS COMPONENT_NAME, \
   OGOM.ORG_ID AS COMPONENT_LIB_ID, \
   'agent' AS COMPONENT_TYPE, \
   CLONE_SET_ID AS  CLONE_SET_ID \
  FROM  cfw_org_group_org_member OGOM, \
    lib_clone_set cs \
	LEFT JOIN alib_component VAC ON (CONCAT(CLONE_SET_ID,'-',OGOM.ORG_ID)=VAC.COMPONENT_ALIB_ID) \
  WHERE OGOM.ORG_GROUP_ID=':org_group_id' \
	AND VAC.COMPONENT_ALIB_ID IS NULL \
    AND CS.CLONE_SET_ID>0 AND CS.CLONE_SET_ID< :n

addNewClonedAgentAlibComponentsInsert = \
INSERT INTO alib_component \
   (COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID) \
 SELECT COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID \
  FROM tmp_alib_component_:short_assembly_id

addNewClonedAgentAlibComponentsDrop = \
 DROP TABLE tmp_alib_component_:short_assembly_id

addBaseASBAgentsQueries = addBaseASBAgents
addBaseASBAgents = \
INSERT INTO  asb_agent \
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME) \
 SELECT DISTINCT ':assembly_id'  AS ASSEMBLY_ID, \
   GO.ORG_ID AS COMPONENT_ALIB_ID, \
   GO.ORG_ID AS COMPONENT_LIB_ID, \
   0 AS  CLONE_SET_ID, \
   AC.COMPONENT_NAME AS COMPONENT_NAME \
 FROM \
   cfw_group_org GO, \
   alib_component AC \
 WHERE GO.CFW_GROUP_ID= ':cfw_group_id' \
   AND AC.COMPONENT_ALIB_ID=GO.ORG_ID \
   AND NOT EXISTS \
   (SELECT COMPONENT_ALIB_ID \
	   FROM asb_agent AA \
    WHERE \
	   AA.COMPONENT_ALIB_ID=GO.ORG_ID \
	   AND AA.ASSEMBLY_ID= ':assembly_id') 


addBaseASBAgentsQueries.mysql = addBaseASBAgentsCreateTable addBaseASBAgentsInsert addBaseASBAgentsDrop
addBaseASBAgentsCreateTable = \
CREATE TEMPORARY TABLE tmp_asb_agent_:short_assembly_id AS \
SELECT DISTINCT ':assembly_id'  AS ASSEMBLY_ID, \
  GO.ORG_ID AS COMPONENT_ALIB_ID, \
  GO.ORG_ID AS COMPONENT_LIB_ID, \
  0 AS  CLONE_SET_ID, \
  AC.COMPONENT_NAME AS COMPONENT_NAME \
 FROM  cfw_group_org GO, \
  alib_component AC \
  LEFT JOIN asb_agent AA ON \
  (AA.COMPONENT_ALIB_ID=GO.ORG_ID AND AA.ASSEMBLY_ID= ':assembly_id') \
 WHERE GO.CFW_GROUP_ID= ':cfw_group_id' \
  AND AA.COMPONENT_ALIB_ID IS NULL \
  AND AC.COMPONENT_ALIB_ID=GO.ORG_ID

addBaseASBAgentsInsert = \
INSERT INTO asb_agent \
  (ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME) \
 SELECT \
  ASSEMBLY_ID, COMPONENT_ALIB_ID, COMPONENT_LIB_ID, CLONE_SET_ID, COMPONENT_NAME \
 FROM tmp_asb_agent_:short_assembly_id
    
addBaseASBAgentsDrop = \
 DROP TABLE tmp_asb_agent_:short_assembly_id

addNewPluginAlibComponentsQueries = addNewPluginAlibComponents

addNewPluginAlibComponents = \
 INSERT INTO alib_component \
  (COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID) \
  SELECT DISTINCT \
    (AA.COMPONENT_ALIB_ID || '|' ||  PL.PLUGIN_ID) AS COMPONENT_ALIB_ID, \
     (AA.COMPONENT_ALIB_ID || '|' ||  PL.PLUGIN_ID) AS COMPONENT_NAME, \
     'plugin|' || PL.PLUGIN_ID AS COMPONENT_LIB_ID, \
     'plugin' AS COMPONENT_TYPE, \
     0 AS  CLONE_SET_ID \
   FROM  asb_agent AA, \
	 cfw_org_orgtype OT, \
	 cfw_orgtype_plugin_grp PG, \
	 cfw_plugin_group_member PL, \
	 lib_plugin_thread PTH \
   WHERE \
     AA.ASSEMBLY_ID=  ':assembly_id' \
     AND AA.COMPONENT_LIB_ID=OT.ORG_ID \
     AND OT.CFW_ID IN :cfw_instances \
     AND PG.CFW_ID IN :cfw_instances \
     AND OT.ORGTYPE_ID=PG.ORGTYPE_ID \
     AND PG.PLUGIN_GROUP_ID = PL.PLUGIN_GROUP_ID \
     AND PTH.PLUGIN_ID=PL.PLUGIN_ID \
     AND PTH.THREAD_ID IN :threads \
     AND (AA.COMPONENT_ALIB_ID || '|' ||  PL.PLUGIN_ID) NOT IN \
      (SELECT COMPONENT_ALIB_ID FROM  alib_component) 

addNewPluginAlibComponentsQueries.mysql = addNewPluginAlibComponentsCreateTable addNewPluginAlibComponentsInsert addNewPluginAlibComponentsDrop
addNewPluginAlibComponentsCreateTable = \
CREATE TEMPORARY TABLE tmp_alib_component_:short_assembly_id AS \
  SELECT DISTINCT \
     CONCAT(AA.COMPONENT_ALIB_ID,'|',PL.PLUGIN_ID) AS COMPONENT_ALIB_ID, \
     CONCAT(AA.COMPONENT_ALIB_ID,'|',PL.PLUGIN_ID) AS COMPONENT_NAME, \
     CONCAT('plugin|',PL.PLUGIN_ID) AS COMPONENT_LIB_ID, \
     'plugin' AS COMPONENT_TYPE, \
     0 AS  CLONE_SET_ID \
   FROM  asb_agent AA, \
	 cfw_org_orgtype OT, \
	 cfw_orgtype_plugin_grp PG, \
	 cfw_plugin_group_member PL, \
	 lib_plugin_thread PTH \
	 LEFT JOIN alib_component VAC ON \
	  (VAC.COMPONENT_ALIB_ID=CONCAT(AA.COMPONENT_ALIB_ID,'|',PL.PLUGIN_ID)) \
   WHERE \
     AA.ASSEMBLY_ID=  ':assembly_id' \
     AND VAC.COMPONENT_ALIB_ID IS NULL \
     AND AA.COMPONENT_LIB_ID=OT.ORG_ID \
     AND OT.CFW_ID IN :cfw_instances \
     AND PG.CFW_ID IN :cfw_instances \
     AND OT.ORGTYPE_ID=PG.ORGTYPE_ID \
     AND PG.PLUGIN_GROUP_ID = PL.PLUGIN_GROUP_ID \
     AND PTH.PLUGIN_ID=PL.PLUGIN_ID \
     AND PTH.THREAD_ID IN :threads

addNewPluginAlibComponentsInsert = \
INSERT INTO alib_component \
 (COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID) \
 SELECT \
  COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID \
 FROM tmp_alib_component_:short_assembly_id
  
addNewPluginAlibComponentsDrop = \
DROP TABLE tmp_alib_component_:short_assembly_id

addPluginASBComponentHierarchyQueries = addPluginASBComponentHierarchy

addPluginASBComponentHierarchy = \
 INSERT INTO  asb_component_hierarchy \
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, PRIORITY, INSERTION_ORDER) \
  SELECT DISTINCT \
    ':assembly_id' AS ASSEMBLY_ID, \
    (AA.COMPONENT_ALIB_ID || '|' ||  PL.PLUGIN_ID) AS COMPONENT_ALIB_ID, \
    AA.COMPONENT_ALIB_ID AS PARENT_COMPONENT_ALIB_ID, \
    'COMPONENT' AS PRIORITY, \
    (PL.PLUGIN_CLASS_ORDER+(1000* PG.PLUGIN_GROUP_ORDER)) AS INSERTION_ORDER \
   FROM \
     asb_agent AA, \
     cfw_org_orgtype OT, \
     cfw_orgtype_plugin_grp OPG, \
     lib_plugin_group PG, \
     cfw_plugin_group_member PL, \
     lib_plugin_thread PTH \
    WHERE \
      AA.ASSEMBLY_ID= ':assembly_id' \
      AND AA.COMPONENT_LIB_ID=OT.ORG_ID \
      AND OPG.CFW_ID IN :cfw_instances \
      AND OT.CFW_ID IN :cfw_instances \
      AND PL.CFW_ID IN :cfw_instances \
      AND OT.ORGTYPE_ID=OPG.ORGTYPE_ID \
      AND PG.PLUGIN_GROUP_ID = OPG.PLUGIN_GROUP_ID \
      AND PG.PLUGIN_GROUP_ID = PL.PLUGIN_GROUP_ID \
      AND PTH.PLUGIN_ID=PL.PLUGIN_ID \
      AND PTH.THREAD_ID IN :threads \
      AND NOT EXISTS \
       (SELECT COMPONENT_ALIB_ID \
         FROM  asb_component_hierarchy ACH \
         WHERE \
	 ACH.ASSEMBLY_ID=':assembly_id' \
	 AND ACH.COMPONENT_ALIB_ID=(AA.COMPONENT_ALIB_ID || '|' ||  PL.PLUGIN_ID) \
	 AND ACH.PARENT_COMPONENT_ALIB_ID=AA.COMPONENT_ALIB_ID)


addPluginASBComponentHierarchyQueries.mysql = addPluginASBComponentHierarchyCreateTable addPluginASBComponentHierarchyInsert addPluginASBComponentHierarchyDrop

addPluginASBComponentHierarchyCreateTable = \
CREATE TEMPORARY TABLE tmp_asb_component_hierarchy:short_assembly_id AS \
  SELECT DISTINCT \
    ':assembly_id' AS ASSEMBLY_ID, \
    CONCAT(AA.COMPONENT_ALIB_ID,'|',PL.PLUGIN_ID) AS COMPONENT_ALIB_ID, \
    AA.COMPONENT_ALIB_ID AS PARENT_COMPONENT_ALIB_ID, \
    'COMPONENT' AS PRIORITY, \
    (PL.PLUGIN_CLASS_ORDER+(1000* PG.PLUGIN_GROUP_ORDER)) AS INSERTION_ORDER \
   FROM \
     asb_agent AA, \
     cfw_org_orgtype OT, \
     cfw_orgtype_plugin_grp OPG, \
     lib_plugin_group PG, \
     cfw_plugin_group_member PL, \
     lib_plugin_thread PTH \
	 LEFT JOIN asb_component_hierarchy ACH ON \
	   (ACH.ASSEMBLY_ID=':assembly_id' \
	    AND ACH.COMPONENT_ALIB_ID=CONCAT(AA.COMPONENT_ALIB_ID,'|',PL.PLUGIN_ID) \
		AND ACH.PARENT_COMPONENT_ALIB_ID=AA.COMPONENT_ALIB_ID) \
    WHERE \
      AA.ASSEMBLY_ID= ':assembly_id' \
      AND ACH.ASSEMBLY_ID IS NULL \
      AND AA.COMPONENT_LIB_ID=OT.ORG_ID \
      AND OPG.CFW_ID IN :cfw_instances \
      AND OT.CFW_ID IN :cfw_instances \
      AND PL.CFW_ID IN :cfw_instances \
      AND OT.ORGTYPE_ID=OPG.ORGTYPE_ID \
      AND PG.PLUGIN_GROUP_ID = OPG.PLUGIN_GROUP_ID \
      AND PG.PLUGIN_GROUP_ID = PL.PLUGIN_GROUP_ID \
      AND PTH.PLUGIN_ID=PL.PLUGIN_ID \
      AND PTH.THREAD_ID IN :threads

addPluginASBComponentHierarchyInsert = \
INSERT INTO  asb_component_hierarchy \
  (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, PRIORITY, INSERTION_ORDER) \
 SELECT ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, PRIORITY, INSERTION_ORDER \
  FROM tmp_asb_component_hierarchy:short_assembly_id

addPluginASBComponentHierarchyDrop = \
DROP TABLE tmp_asb_component_hierarchy:short_assembly_id

addAgentNameComponentArgQueries = addAgentNameComponentArg

addAgentNameComponentArg = \
 INSERT INTO  asb_component_arg \
  (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER) \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
   AA.COMPONENT_ALIB_ID AS ARGUMENT, \
   0 AS ARGUMENT_ORDER \
 FROM \
    asb_agent AA \
   WHERE \
    AA.ASSEMBLY_ID= ':assembly_id' \
    AND NOT EXISTS \
    (SELECT ASSEMBLY_ID \
      FROM \
        asb_component_arg ACA \
      WHERE \
        ASSEMBLY_ID=':assembly_id' \
		AND ACA.COMPONENT_ALIB_ID=AA.COMPONENT_ALIB_ID \
		AND ACA.ARGUMENT=AA.COMPONENT_ALIB_ID )

addAgentNameComponentArgQueries.mysql = addAgentNameComponentArgCreateTable addAgentNameComponentArgInsert addAgentNameComponentArgDrop

addAgentNameComponentArgCreateTable = \
CREATE TEMPORARY TABLE tmp_asb_component_arg:short_assembly_id AS \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
   AA.COMPONENT_ALIB_ID AS ARGUMENT, \
   0 AS ARGUMENT_ORDER \
 FROM \
    asb_agent AA \
	LEFT JOIN asb_component_arg ACA ON \
	(ACA.ASSEMBLY_ID=':assembly_id' \
	 AND ACA.COMPONENT_ALIB_ID=AA.COMPONENT_ALIB_ID \
	 AND ACA.ARGUMENT=AA.COMPONENT_ALIB_ID)
   WHERE \
    AA.ASSEMBLY_ID= ':assembly_id' \
	ACA.ASSEMBLY_ID IS NULL

addAgentNameComponentArgInsert = \
 INSERT INTO  asb_component_arg \
  (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER) \
 SELECT ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER \
  FROM tmp_asb_component_arg:short_assembly_id

addAgentNameComponentArgDrop = \
DROP TABLE tmp_asb_component_arg:short_assembly_id

addASBAgentPGAttrQueries = addASBAgentPGAttr

addASBAgentPGAttr = \
 INSERT INTO  asb_agent_pg_attr \
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE, END_DATE) \
  SELECT DISTINCT \
     ':assembly_id'  AS ASSEMBLY_ID, \
     AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
     PGA.PG_ATTRIBUTE_LIB_ID AS PG_ATTRIBUTE_LIB_ID, \
     PGA.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE, \
     PGA.ATTRIBUTE_ORDER AS ATTRIBUTE_ORDER, \
     PGA.START_DATE AS START_DATE, \
     PGA.END_DATE AS END_DATE \
   FROM \
     asb_agent AA, \
     cfw_org_pg_attr PGA, \
     lib_pg_attribute LPGA \
   WHERE \
     AA.ASSEMBLY_ID= ':assembly_id' \
     AND AA.COMPONENT_LIB_ID=PGA.ORG_ID \
     AND PGA.CFW_ID IN :cfw_instances \
     AND LPGA.PG_ATTRIBUTE_LIB_ID=PGA.PG_ATTRIBUTE_LIB_ID \
     AND NOT EXISTS \
      (SELECT ASSEMBLY_ID \
       FROM  asb_agent_pg_attr PX \
       WHERE \
        PX.ASSEMBLY_ID=':assembly_id' \
	AND PX.COMPONENT_ALIB_ID=AA.COMPONENT_ALIB_ID \
	AND PX.PG_ATTRIBUTE_LIB_ID=PGA.PG_ATTRIBUTE_LIB_ID \
        AND PX.START_DATE=PGA.START_DATE) 

addASBAgentPGAttrQueries.mysql = addASBAgentPGAttrCreateTable addASBAgentPGAttrInsert addASBAgentPGAttrDrop
addASBAgentPGAttrCreateTable = \
CREATE TEMPORARY TABLE tmp_asb_agent_pg_attr_:short_assembly_id AS \
  SELECT DISTINCT \
     ':assembly_id'  AS ASSEMBLY_ID, \
     AA.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
     PGA.PG_ATTRIBUTE_LIB_ID AS PG_ATTRIBUTE_LIB_ID, \
     PGA.ATTRIBUTE_VALUE AS ATTRIBUTE_VALUE, \
     PGA.ATTRIBUTE_ORDER AS ATTRIBUTE_ORDER, \
     PGA.START_DATE AS START_DATE, \
     PGA.END_DATE AS END_DATE \
   FROM \
     asb_agent AA, \
     cfw_org_pg_attr PGA, \
     lib_pg_attribute LPGA \
	 LEFT JOIN asb_agent_pg_attr PX ON \
	 (PX.ASSEMBLY_ID=':assembly_id' \
	  AND PX.COMPONENT_ALIB_ID=AA.COMPONENT_ALIB_ID \
	  AND PX.PG_ATTRIBUTE_LIB_ID=PGA.PG_ATTRIBUTE_LIB_ID \
	  AND PX.START_DATE=PGA.START_DATE) \
   WHERE \
     AA.ASSEMBLY_ID= ':assembly_id' \
	 AND PX.ASSEMBLY_ID IS NULL \
     AND AA.COMPONENT_LIB_ID=PGA.ORG_ID \
     AND PGA.CFW_ID IN :cfw_instances \
     AND LPGA.PG_ATTRIBUTE_LIB_ID=PGA.PG_ATTRIBUTE_LIB_ID \

addASBAgentPGAttrInsert = \
INSERT INTO asb_agent_pg_attr \
  (ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE, END_DATE) \
 SELECT \
  ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE, END_DATE \
 FROM tmp_asb_agent_pg_attr_:short_assembly_id
   
addASBAgentPGAttrDrop = \
DROP TABLE tmp_asb_agent_pg_attr_:short_assembly_id

addASBAgentRelationToBaseQueries = addASBAgentRelationToBase

addASBAgentRelationToBase = \
INSERT INTO  asb_agent_relation \
   (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE) \
 SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   ORGREL.ROLE AS ROLE, \
   SUPPORTING_ORG.COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, \
   SUPPORTED_ORG.COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, \
   ORGREL.START_DATE AS START_DATE, \
   ORGREL.END_DATE AS END_DATE \
  FROM \
    asb_agent SUPPORTED_ORG, \
    asb_agent SUPPORTING_ORG, \
    cfw_org_og_relation ORGREL, \
    cfw_org_group_org_member OGOM \
  WHERE \
    ORGREL.CFW_ID IN :cfw_instances \
    AND SUPPORTED_ORG.ASSEMBLY_ID=':assembly_id' \
    AND SUPPORTING_ORG.ASSEMBLY_ID=':assembly_id' \
    AND SUPPORTING_ORG.COMPONENT_LIB_ID=ORGREL.ORG_ID \
    AND OGOM.CFW_ID IN :cfw_instances \
    AND OGOM.ORG_GROUP_ID = ORGREL.ORG_GROUP_ID \
    AND SUPPORTED_ORG.COMPONENT_LIB_ID=OGOM.ORG_ID \
    AND SUPPORTING_ORG.CLONE_SET_ID=0 \
    AND SUPPORTING_ORG.COMPONENT_ALIB_ID<>SUPPORTED_ORG.COMPONENT_ALIB_ID \
    AND ORGREL.ROLE <> 'Subordinate' \
    AND ORGREL.ROLE <> 'Superior' \
    AND NOT EXISTS \
      (SELECT ASSEMBLY_ID \
        FROM asb_agent_relation AR \
		WHERE \
		  AR.ASSEMBLY_ID=':assembly_id' \
		  AND AR.SUPPORTED_COMPONENT_ALIB_ID =SUPPORTED_ORG.COMPONENT_ALIB_ID \
		  AND AR.ROLE=ORGREL.ROLE \
		  AND AR.START_DATE=ORGREL.START_DATE)


addASBAgentRelationToBaseQueries.mysql = addASBAgentRelationToBaseCreateTable addASBAgentRelationToBaseInsert addASBAgentRelationToBaseDrop

addASBAgentRelationToBaseCreateTable = \
CREATE TEMPORARY TABLE tmp_asb_agent_relation_:short_assembly_id AS \
SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   ORGREL.ROLE AS ROLE, \
   SUPPORTING_ORG.COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, \
   SUPPORTED_ORG.COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, \
   ORGREL.START_DATE AS START_DATE, \
   ORGREL.END_DATE AS END_DATE \
 FROM \
   asb_agent SUPPORTED_ORG, \
   asb_agent SUPPORTING_ORG, \
   cfw_org_og_relation ORGREL, \
   cfw_org_group_org_member OGOM \
   LEFT JOIN asb_agent_relation AR ON \
	(AR.ASSEMBLY_ID=':assembly_id' \
	 AND AR.SUPPORTED_COMPONENT_ALIB_ID =SUPPORTED_ORG.COMPONENT_ALIB_ID \
	 AND AR.ROLE=ORGREL.ROLE \
	 AND AR.START_DATE=ORGREL.START_DATE) \
 WHERE \
   ORGREL.CFW_ID IN :cfw_instances \
   AND AR.ASSEMBLY_ID IS NULL \
   AND SUPPORTED_ORG.ASSEMBLY_ID=':assembly_id' \
   AND SUPPORTING_ORG.ASSEMBLY_ID=':assembly_id' \
   AND SUPPORTING_ORG.COMPONENT_LIB_ID=ORGREL.ORG_ID \
   AND OGOM.CFW_ID IN :cfw_instances \
   AND OGOM.ORG_GROUP_ID = ORGREL.ORG_GROUP_ID \
   AND SUPPORTED_ORG.COMPONENT_LIB_ID=OGOM.ORG_ID \
   AND SUPPORTING_ORG.CLONE_SET_ID=0 \
   AND SUPPORTING_ORG.COMPONENT_ALIB_ID<>SUPPORTED_ORG.COMPONENT_ALIB_ID \
   AND ORGREL.ROLE <> 'Subordinate' \
   AND ORGREL.ROLE <> 'Superior'

addASBAgentRelationToBaseInsert = \
INSERT INTO asb_agent_relation \
 SELECT \
  ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE \
 FROM tmp_asb_agent_relation_:short_assembly_id
   
addASBAgentRelationToBaseDrop = \
DROP TABLE tmp_asb_agent_relation_:short_assembly_id

addASBAgentRelationToClonesetQueries = addASBAgentRelationToCloneset
addASBAgentRelationToClonesetQueries.mysql = addASBAgentRelationToClonesetCreateTable addASBAgentRelationToClonesetInsert addASBAgentRelationToClonesetDrop

addASBAgentRelationToCloneset = \
 INSERT INTO  asb_agent_relation \
   (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE) \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   ORGREL.ROLE AS ROLE, \
   SUPPORTING_ORG.COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, \
   SUPPORTED_ORG.COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, \
   ORGREL.START_DATE AS START_DATE, \
   ORGREL.END_DATE AS END_DATE \
  FROM \
   asb_agent SUPPORTED_ORG, \
   asb_agent SUPPORTING_ORG, \
   cfw_org_og_relation ORGREL, \
   cfw_org_group_org_member OGOM \
  WHERE \
   ORGREL.CFW_ID IN :cfw_instances \
   AND SUPPORTED_ORG.ASSEMBLY_ID=':assembly_id' \
   AND SUPPORTING_ORG.ASSEMBLY_ID=':assembly_id' \
   AND SUPPORTING_ORG.COMPONENT_LIB_ID=ORGREL.ORG_ID \
   AND OGOM.CFW_ID IN :cfw_instances \
   AND OGOM.ORG_GROUP_ID = ORGREL.ORG_GROUP_ID \
   AND SUPPORTED_ORG.COMPONENT_LIB_ID=OGOM.ORG_ID \
   AND SUPPORTING_ORG.CLONE_SET_ID=SUPPORTED_ORG.CLONE_SET_ID \
   AND SUPPORTING_ORG.COMPONENT_ALIB_ID<>SUPPORTED_ORG.COMPONENT_ALIB_ID \
   AND ORGREL.ROLE <> 'Subordinate' \
   AND ORGREL.ROLE <> 'Superior' \
   AND NOT EXISTS \
    (SELECT ASSEMBLY_ID \
      FROM \
       asb_agent_relation AR \
      WHERE \
       AR.ASSEMBLY_ID=':assembly_id' \
       AND AR.SUPPORTED_COMPONENT_ALIB_ID =SUPPORTED_ORG.COMPONENT_ALIB_ID \
       AND AR.ROLE=ORGREL.ROLE \
       AND AR.START_DATE=ORGREL.START_DATE)

addASBAgentRelationToClonesetCreateTable = \
CREATE TEMPORARY TABLE tmp_asb_agent_relation_:short_assembly_id AS \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   ORGREL.ROLE AS ROLE, \
   SUPPORTING_ORG.COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, \
   SUPPORTED_ORG.COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, \
   ORGREL.START_DATE AS START_DATE, \
   ORGREL.END_DATE AS END_DATE \
  FROM \
   asb_agent SUPPORTED_ORG, \
   asb_agent SUPPORTING_ORG, \
   cfw_org_og_relation ORGREL, \
   cfw_org_group_org_member OGOM \
   LEFT JOIN asb_agent_relation AR ON \
	 (AR.ASSEMBLY_ID=':assembly_id' \
      AND AR.SUPPORTED_COMPONENT_ALIB_ID =SUPPORTED_ORG.COMPONENT_ALIB_ID \
      AND AR.ROLE=ORGREL.ROLE \
      AND AR.START_DATE=ORGREL.START_DATE) \
  WHERE \
   ORGREL.CFW_ID IN :cfw_instances \
   AND AR.ASSEMBLY_ID IS NULL \
   AND SUPPORTED_ORG.ASSEMBLY_ID=':assembly_id' \
   AND SUPPORTING_ORG.ASSEMBLY_ID=':assembly_id' \
   AND SUPPORTING_ORG.COMPONENT_LIB_ID=ORGREL.ORG_ID \
   AND OGOM.CFW_ID IN :cfw_instances \
   AND OGOM.ORG_GROUP_ID = ORGREL.ORG_GROUP_ID \
   AND SUPPORTED_ORG.COMPONENT_LIB_ID=OGOM.ORG_ID \
   AND SUPPORTING_ORG.CLONE_SET_ID=SUPPORTED_ORG.CLONE_SET_ID \
   AND SUPPORTING_ORG.COMPONENT_ALIB_ID<>SUPPORTED_ORG.COMPONENT_ALIB_ID \
   AND ORGREL.ROLE <> 'Subordinate' \
   AND ORGREL.ROLE <> 'Superior'


addASBAgentRelationToClonesetInsert = \
INSERT INTO asb_agent_relation \
 SELECT \
  ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE \
 FROM tmp_asb_agent_relation_:short_assembly_id
   
addASBAgentRelationToClonesetDrop = \
DROP TABLE tmp_asb_agent_relation_:short_assembly_id

addASBAgentHierarchyRelationToBaseQueries = addASBAgentHierarchyRelationToBase

addASBAgentHierarchyRelationToBase = \
 INSERT INTO  asb_agent_relation \
   (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE) \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   'Subordinate' AS ROLE, \
   SUPPORTING_ORG.COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, \
   SUPPORTED_ORG.COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, \
   TO_DATE('1-JAN-2001') AS START_DATE, \
   NULL AS END_DATE \
  FROM \
   asb_agent SUPPORTED_ORG, \
   asb_agent SUPPORTING_ORG, \
   cfw_org_hierarchy OH \
  WHERE \
   OH.CFW_ID IN :cfw_instances \
   AND SUPPORTED_ORG.ASSEMBLY_ID=':assembly_id' \
   AND SUPPORTING_ORG.ASSEMBLY_ID=':assembly_id' \
   AND OH.ORG_ID=SUPPORTING_ORG.COMPONENT_LIB_ID \
   AND OH.SUPERIOR_ORG_ID=SUPPORTED_ORG.COMPONENT_LIB_ID \
   AND SUPPORTED_ORG.CLONE_SET_ID=0 \
   AND NOT EXISTS \
    (SELECT ASSEMBLY_ID \
      FROM \
       asb_agent_relation AR \
      WHERE \
       AR.ASSEMBLY_ID=':assembly_id' \
       AND AR.SUPPORTING_COMPONENT_ALIB_ID=SUPPORTING_ORG.COMPONENT_ALIB_ID \
       AND AR.ROLE='Subordinate' \
       AND AR.START_DATE=TO_DATE('1-JAN-2001'))


addASBAgentHierarchyRelationToBaseQueries.mysql = addASBAgentHierarchyRelationToBaseCreateTable addASBAgentHierarchyRelationToBaseInsert addASBAgentHierarchyRelationToBaseDrop

addASBAgentHierarchyRelationToBaseCreateTable = \
CREATE TEMPORARY TABLE tmp_asb_agent_relation_:short_assembly_id AS \
SELECT DISTINCT \
  ':assembly_id'  AS ASSEMBLY_ID, \
  'Subordinate' AS ROLE, \
  SUPPORTING_ORG.COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, \
  SUPPORTED_ORG.COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, \
  "2001-01-01 00:00:00" AS START_DATE, \
  NULL AS END_DATE \
 FROM \
  asb_agent SUPPORTED_ORG, \
  asb_agent SUPPORTING_ORG, \
  cfw_org_hierarchy OH \
  LEFT JOIN asb_agent_relation AR ON \
    (AR.ASSEMBLY_ID=':assembly_id' \
      AND AR.SUPPORTING_COMPONENT_ALIB_ID=SUPPORTING_ORG.COMPONENT_ALIB_ID \
	  AND AR.ROLE='Subordinate' \
	  AND AR.START_DATE="2001-01-01 00:00:00") \
 WHERE \
  OH.CFW_ID IN :cfw_instances \
  AND AR.ASSEMBLY_ID IS NULL \
  AND SUPPORTED_ORG.ASSEMBLY_ID=':assembly_id' \
  AND SUPPORTING_ORG.ASSEMBLY_ID=':assembly_id' \
  AND OH.ORG_ID=SUPPORTING_ORG.COMPONENT_LIB_ID \
  AND OH.SUPERIOR_ORG_ID=SUPPORTED_ORG.COMPONENT_LIB_ID \
  AND SUPPORTED_ORG.CLONE_SET_ID=0

addASBAgentHierarchyRelationToBaseInsert = \
INSERT INTO asb_agent_relation \
 SELECT \
  ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE \
 FROM tmp_asb_agent_relation_:short_assembly_id
   
addASBAgentHierarchyRelationToBaseDrop = \
DROP TABLE tmp_asb_agent_relation_:short_assembly_id

addASBAgentHierarchyRelationToClonesetQueries = addASBAgentHierarchyRelationToCloneset

addASBAgentHierarchyRelationToCloneset = \
 INSERT INTO  asb_agent_relation \
   (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE) \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   'Subordinate' AS ROLE, \
   SUPPORTING_ORG.COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, \
   SUPPORTED_ORG.COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, \
   TO_DATE('1-JAN-2001') AS START_DATE, \
   NULL AS END_DATE \
  FROM \
   asb_agent SUPPORTED_ORG, \
   asb_agent SUPPORTING_ORG, \
   cfw_org_hierarchy OH \
  WHERE \
   OH.CFW_ID IN :cfw_instances \
   AND SUPPORTED_ORG.ASSEMBLY_ID=':assembly_id' \
   AND SUPPORTING_ORG.ASSEMBLY_ID=':assembly_id' \
   AND OH.ORG_ID=SUPPORTING_ORG.COMPONENT_LIB_ID \
   AND OH.SUPERIOR_ORG_ID=SUPPORTED_ORG.COMPONENT_LIB_ID \
   AND SUPPORTING_ORG.CLONE_SET_ID=SUPPORTED_ORG.CLONE_SET_ID \
   AND NOT EXISTS \
    (SELECT ASSEMBLY_ID \
      FROM \
       asb_agent_relation AR \
      WHERE \
       AR.ASSEMBLY_ID=':assembly_id' \
       AND AR.SUPPORTING_COMPONENT_ALIB_ID=SUPPORTING_ORG.COMPONENT_ALIB_ID \
       AND AR.ROLE='Subordinate' \
       AND AR.START_DATE=TO_DATE('1-JAN-2001'))

addASBAgentHierarchyRelationToClonesetQueries.mysql = addASBAgentHierarchyRelationToClonesetCreateTable addASBAgentHierarchyRelationToClonesetInsert addASBAgentHierarchyRelationToClonesetDrop

addASBAgentHierarchyRelationToClonesetCreateTable = \
CREATE TEMPORARY TABLE tmp_asb_agent_relation_:short_assembly_id AS \
SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   'Subordinate' AS ROLE, \
   SUPPORTING_ORG.COMPONENT_ALIB_ID AS SUPPORTING_COMPONENT_ALIB_ID, \
   SUPPORTED_ORG.COMPONENT_ALIB_ID AS SUPPORTED_COMPONENT_ALIB_ID, \
   "2001-01-01 00:00:00" AS START_DATE, \
   NULL AS END_DATE \
 FROM \
   asb_agent SUPPORTED_ORG, \
   asb_agent SUPPORTING_ORG, \
   cfw_org_hierarchy OH \
   LEFT JOIN asb_agent_relation AR ON \
    (AR.ASSEMBLY_ID=':assembly_id' \
	  AND AR.SUPPORTING_COMPONENT_ALIB_ID=SUPPORTING_ORG.COMPONENT_ALIB_ID \
	  AND AR.ROLE='Subordinate' \
	  AND AR.START_DATE="2001-01-01 00:00:00") \
  WHERE \
   OH.CFW_ID IN :cfw_instances \
   AND AR.ASSEMBLY_ID IS NULL \
   AND SUPPORTED_ORG.ASSEMBLY_ID=':assembly_id' \
   AND SUPPORTING_ORG.ASSEMBLY_ID=':assembly_id' \
   AND OH.ORG_ID=SUPPORTING_ORG.COMPONENT_LIB_ID \
   AND OH.SUPERIOR_ORG_ID=SUPPORTED_ORG.COMPONENT_LIB_ID \
   AND SUPPORTING_ORG.CLONE_SET_ID=SUPPORTED_ORG.CLONE_SET_ID

addASBAgentHierarchyRelationToClonesetInsert = \
INSERT INTO asb_agent_relation \
 SELECT \
  ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE, END_DATE \
 FROM tmp_asb_agent_relation_:short_assembly_id
   
addASBAgentHierarchyRelationToClonesetDrop = \
DROP TABLE tmp_asb_agent_relation_:short_assembly_id

addPluginAgentASBComponentArgQueries = addPluginAgentASBComponentArg

addPluginAgentASBComponentArg = \
 INSERT INTO  asb_component_arg \
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER) \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   CH.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
   PA.ARGUMENT AS ARGUMENT, \
   PA.ARGUMENT_ORDER AS ARGUMENT_ORDER \
  FROM \
    asb_component_hierarchy CH, \
    alib_component PLUGIN_ALIB, \
    asb_agent ORG_AGENT, \
    cfw_context_plugin_arg CPA, \
    lib_plugin_arg PA, \
    lib_plugin_arg_thread PAT \
  WHERE \
    CH.ASSEMBLY_ID=':assembly_id' \
    AND PLUGIN_ALIB.COMPONENT_TYPE='plugin' \
    AND CH.PARENT_COMPONENT_ALIB_ID=ORG_AGENT.COMPONENT_ALIB_ID \
    AND CH.COMPONENT_ALIB_ID=PLUGIN_ALIB.COMPONENT_ALIB_ID \
    AND CPA.CFW_ID IN :cfw_instances \
    AND PA.ARGUMENT IS NOT NULL \
    AND CPA.ORG_CONTEXT = ORG_AGENT.COMPONENT_LIB_ID \
    AND PA.PLUGIN_ARG_ID=CPA.PLUGIN_ARG_ID \
    AND ('plugin|' || PA.PLUGIN_ID)=PLUGIN_ALIB.COMPONENT_LIB_ID \
    AND PA.PLUGIN_ARG_ID=PAT.PLUGIN_ARG_ID \
    AND PAT.THREAD_ID IN :threads \
    AND NOT EXISTS \
    (SELECT ASSEMBLY_ID \
      FROM \
       asb_component_arg ACA \
      WHERE \
       ASSEMBLY_ID=':assembly_id' \
       AND ACA.COMPONENT_ALIB_ID=CH.COMPONENT_ALIB_ID)

addPluginAgentASBComponentArgQueries.mysql = addPluginAgentASBComponentArgCreateTable addPluginAgentASBComponentArgInsert addPluginAgentASBComponentArgDrop

addPluginAgentASBComponentArgCreateTable = \
CREATE TEMPORARY TABLE tmp_asb_component_arg_:short_assembly_id AS \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   CH.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
   PA.ARGUMENT AS ARGUMENT, \
   PA.ARGUMENT_ORDER AS ARGUMENT_ORDER \
  FROM \
    asb_component_hierarchy CH, \
    alib_component PLUGIN_ALIB, \
    asb_agent ORG_AGENT, \
    cfw_context_plugin_arg CPA, \
    lib_plugin_arg PA, \
    lib_plugin_arg_thread PAT \
	LEFT JOIN asb_component_arg ACA ON \
	 (ACA.ASSEMBLY_ID=':assembly_id' \
	  AND ACA.COMPONENT_ALIB_ID=CH.COMPONENT_ALIB_ID) \
  WHERE \
    CH.ASSEMBLY_ID=':assembly_id' \
	AND ACA.ASSEMBLY_ID IS NULL \
    AND PLUGIN_ALIB.COMPONENT_TYPE='plugin' \
    AND CH.PARENT_COMPONENT_ALIB_ID=ORG_AGENT.COMPONENT_ALIB_ID \
    AND CH.COMPONENT_ALIB_ID=PLUGIN_ALIB.COMPONENT_ALIB_ID \
    AND CPA.CFW_ID IN :cfw_instances \
    AND PA.ARGUMENT IS NOT NULL \
    AND CPA.ORG_CONTEXT = ORG_AGENT.COMPONENT_LIB_ID \
    AND PA.PLUGIN_ARG_ID=CPA.PLUGIN_ARG_ID \
    AND CONCAT('plugin|',PA.PLUGIN_ID)=PLUGIN_ALIB.COMPONENT_LIB_ID \
    AND PA.PLUGIN_ARG_ID=PAT.PLUGIN_ARG_ID \
    AND PAT.THREAD_ID IN :threads

addPluginAgentASBComponentArgInsert = \
INSERT INTO asb_component_arg \
 SELECT \
  ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER \
 FROM tmp_asb_component_arg_:short_assembly_id 
   
addPluginAgentASBComponentArgDrop = \
DROP TABLE tmp_asb_component_arg_:short_assembly_id 

addPluginOrgtypeASBComponentArgQueries = addPluginOrgtypeASBComponentArg

addPluginOrgtypeASBComponentArg = \
 INSERT INTO  asb_component_arg \
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER) \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   CH.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
   PA.ARGUMENT AS ARGUMENT, \
   PA.ARGUMENT_ORDER AS ARGUMENT_ORDER \
  FROM \
   asb_component_hierarchy CH, \
   alib_component PLUGIN_ALIB, \
   asb_agent ORG_AGENT, \
   cfw_context_plugin_arg CPA, \
   cfw_org_orgtype OT, \
   lib_plugin_arg PA, \
   lib_plugin_arg_thread PAT \
  WHERE \
   CH.ASSEMBLY_ID=':assembly_id' \
   AND CH.PARENT_COMPONENT_ALIB_ID=ORG_AGENT.COMPONENT_ALIB_ID \
   AND CH.COMPONENT_ALIB_ID=PLUGIN_ALIB.COMPONENT_ALIB_ID \
   AND CPA.CFW_ID IN :cfw_instances \
   AND OT.CFW_ID IN :cfw_instances \
   AND OT.ORG_ID=ORG_AGENT.COMPONENT_LIB_ID \
   AND PA.ARGUMENT IS NOT NULL \
   AND CPA.ORG_CONTEXT = OT.ORGTYPE_ID \
   AND PA.PLUGIN_ARG_ID=CPA.PLUGIN_ARG_ID \
   AND ('plugin|' || PA.PLUGIN_ID)=PLUGIN_ALIB.COMPONENT_LIB_ID \
   AND PA.PLUGIN_ARG_ID=PAT.PLUGIN_ARG_ID \
   AND PAT.THREAD_ID IN :threads \
   AND NOT EXISTS \
    (SELECT ASSEMBLY_ID \
       FROM \
         asb_component_arg ACA \
       WHERE \
         ASSEMBLY_ID=':assembly_id' \
	 AND ACA.COMPONENT_ALIB_ID=CH.COMPONENT_ALIB_ID \
	 AND ACA.ARGUMENT_ORDER=PA.ARGUMENT_ORDER)

addPluginOrgtypeASBComponentArgQueries.mysql = addPluginOrgtypeASBComponentArgCreateTable addPluginOrgtypeASBComponentArgInsert addPluginOrgtypeASBComponentArgDrop

addPluginOrgtypeASBComponentArgCreateTable = \
CREATE TEMPORARY TABLE tmp_asb_component_arg_:short_assembly_id AS \
SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   CH.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
   PA.ARGUMENT AS ARGUMENT, \
   PA.ARGUMENT_ORDER AS ARGUMENT_ORDER \
 FROM \
   asb_component_hierarchy CH, \
   alib_component PLUGIN_ALIB, \
   asb_agent ORG_AGENT, \
   cfw_context_plugin_arg CPA, \
   cfw_org_orgtype OT, \
   lib_plugin_arg PA, \
   lib_plugin_arg_thread PAT \
   LEFT JOIN asb_component_arg ACA ON \
    (ACA.ASSEMBLY_ID=':assembly_id' \
	 AND ACA.COMPONENT_ALIB_ID=CH.COMPONENT_ALIB_ID \
	 AND ACA.ARGUMENT_ORDER=PA.ARGUMENT_ORDER) \
  WHERE \
   CH.ASSEMBLY_ID=':assembly_id' \
   AND ACA.ASSEMBLY_ID IS NULL \
   AND CH.PARENT_COMPONENT_ALIB_ID=ORG_AGENT.COMPONENT_ALIB_ID \
   AND CH.COMPONENT_ALIB_ID=PLUGIN_ALIB.COMPONENT_ALIB_ID \
   AND CPA.CFW_ID IN :cfw_instances \
   AND OT.CFW_ID IN :cfw_instances \
   AND OT.ORG_ID=ORG_AGENT.COMPONENT_LIB_ID \
   AND PA.ARGUMENT IS NOT NULL \
   AND CPA.ORG_CONTEXT = OT.ORGTYPE_ID \
   AND PA.PLUGIN_ARG_ID=CPA.PLUGIN_ARG_ID \
   AND CONCAT('plugin|',PA.PLUGIN_ID)=PLUGIN_ALIB.COMPONENT_LIB_ID \
   AND PA.PLUGIN_ARG_ID=PAT.PLUGIN_ARG_ID \
   AND PAT.THREAD_ID IN :threads

addPluginOrgtypeASBComponentArgInsert = \
INSERT INTO asb_component_arg \
 SELECT \
  ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER \
 FROM tmp_asb_component_arg_:short_assembly_id 
   
addPluginOrgtypeASBComponentArgDrop = \
DROP TABLE tmp_asb_component_arg_:short_assembly_id 

addPluginAllASBComponentArgQueries = addPluginAllASBComponentArg

addPluginAllASBComponentArg = \
 INSERT INTO  asb_component_arg \
   (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER) \
  SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   CH.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
   PA.ARGUMENT AS ARGUMENT, \
   PA.ARGUMENT_ORDER AS ARGUMENT_ORDER \
  FROM \
   asb_component_hierarchy CH, \
   alib_component PLUGIN_ALIB, \
   asb_agent ORG_AGENT, \
   cfw_context_plugin_arg CPA, \
   lib_plugin_arg PA, \
   lib_plugin_arg_thread PAT \
  WHERE \
   CH.ASSEMBLY_ID=':assembly_id' \
   AND CH.PARENT_COMPONENT_ALIB_ID=ORG_AGENT.COMPONENT_ALIB_ID \
   AND CH.COMPONENT_ALIB_ID=PLUGIN_ALIB.COMPONENT_ALIB_ID \
   AND CPA.CFW_ID IN :cfw_instances \
   AND PA.ARGUMENT IS NOT NULL \
   AND CPA.ORG_CONTEXT = 'ALL' \
   AND PA.PLUGIN_ARG_ID=CPA.PLUGIN_ARG_ID \
   AND ('plugin|' || PA.PLUGIN_ID)=PLUGIN_ALIB.COMPONENT_LIB_ID \
   AND PA.PLUGIN_ARG_ID=PAT.PLUGIN_ARG_ID \
   AND PAT.THREAD_ID IN :threads \
   AND NOT EXISTS \
     (SELECT ASSEMBLY_ID \
       FROM \
        asb_component_arg ACA \
       WHERE \
        ASSEMBLY_ID=':assembly_id' \
	AND ACA.COMPONENT_ALIB_ID=CH.COMPONENT_ALIB_ID)

addPluginAllASBComponentArgQueries.mysql = addPluginAllASBComponentArgCreateTable addPluginAllASBComponentArgInsert addPluginAllASBComponentArgDrop

addPluginAllASBComponentArgCreateTable = \
CREATE TEMPORARY TABLE tmp_asb_component_arg_:short_assembly_id AS \
SELECT DISTINCT \
   ':assembly_id'  AS ASSEMBLY_ID, \
   CH.COMPONENT_ALIB_ID AS COMPONENT_ALIB_ID, \
   PA.ARGUMENT AS ARGUMENT, \
   PA.ARGUMENT_ORDER AS ARGUMENT_ORDER \
 FROM \
   asb_component_hierarchy CH, \
   alib_component PLUGIN_ALIB, \
   asb_agent ORG_AGENT, \
   cfw_context_plugin_arg CPA, \
   lib_plugin_arg PA, \
   lib_plugin_arg_thread PAT \
   LEFT JOIN asb_component_arg ACA ON \
	 (ACA.ASSEMBLY_ID=':assembly_id' \
	  AND ACA.COMPONENT_ALIB_ID=CH.COMPONENT_ALIB_ID) \
 WHERE \
   CH.ASSEMBLY_ID=':assembly_id' \
   AND ACA.ASSEMBLY_ID IS NULL \
   AND CH.PARENT_COMPONENT_ALIB_ID=ORG_AGENT.COMPONENT_ALIB_ID \
   AND CH.COMPONENT_ALIB_ID=PLUGIN_ALIB.COMPONENT_ALIB_ID \
   AND CPA.CFW_ID IN :cfw_instances \
   AND PA.ARGUMENT IS NOT NULL \
   AND CPA.ORG_CONTEXT = 'ALL' \
   AND PA.PLUGIN_ARG_ID=CPA.PLUGIN_ARG_ID \
   AND CONCAT('plugin|',PA.PLUGIN_ID)=PLUGIN_ALIB.COMPONENT_LIB_ID \
   AND PA.PLUGIN_ARG_ID=PAT.PLUGIN_ARG_ID \
   AND PAT.THREAD_ID IN :threads


addPluginAllASBComponentArgInsert = \
INSERT INTO asb_component_arg \
 SELECT \
  ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER \
 FROM tmp_asb_component_arg_:short_assembly_id 
   
addPluginAllASBComponentArgDrop = \
DROP TABLE tmp_asb_component_arg_:short_assembly_id 

getExperimentNames = \
 SELECT  NAME,EXPT_ID FROM expt_experiment

getTrialNames = \
 SELECT  DESCRIPTION,TRIAL_ID FROM expt_trial WHERE EXPT_ID=':experiment_id' AND DESCRIPTION IS NOT NULL  

getTrialId = \
 SELECT  TRIAL_ID FROM expt_trial WHERE EXPT_ID=':experiment_id'

addTrialName = \
INSERT INTO expt_trial(TRIAL_ID, EXPT_ID, DESCRIPTION, NAME) \
       values (':trial_id',':experiment_id',':trial_name',':trial_name')

addAssembly = \
 insert into expt_trial_config_assembly (EXPT_ID,TRIAL_ID,ASSEMBLY_ID,DESCRIPTION) \
   values (':experiment_id',':trial_id',':assembly_id',':trial_name')

addRuntimeAssembly = \
 insert into expt_trial_assembly (EXPT_ID,TRIAL_ID,ASSEMBLY_ID,DESCRIPTION) \
   values (':experiment_id',':trial_id',':assembly_id',':trial_name')

createExperiment = \
 insert into expt_experiment (EXPT_ID, DESCRIPTION, NAME, CFW_GROUP_ID) \
  values (':experiment_id',':experiment_id',':experiment_id',':cfw_group_id')

updateCMTAssemblyThreadID = \
 SELECT THREAD_ID FROM expt_trial_thread WHERE EXPT_ID=':experiment_id'

updateCMTAssemblyCFW_GROUP_ID = \
 SELECT CFW_GROUP_ID FROM expt_experiment WHERE  EXPT_ID=':experiment_id'

updateCMTAssemblyClones = \
 SELECT ORG_GROUP_ID, MULTIPLIER FROM expt_trial_org_mult WHERE MULTIPLIER >1 AND EXPT_ID=':experiment_id'

updateCMTAssembly = \
 UPDATE expt_trial_config_assembly SET ASSEMBLY_ID= ':assembly_id' WHERE EXPT_ID=':experiment_id' AND ASSEMBLY_ID LIKE 'CMT-%'

updateRuntimeCMTAssembly = \
 UPDATE expt_trial_assembly SET ASSEMBLY_ID= ':assembly_id' WHERE EXPT_ID=':experiment_id' AND ASSEMBLY_ID LIKE 'CMT-%'

getOrganizationGroups = \
 SELECT DISTINCT \
  OG.DESCRIPTION , \
  OGOM.ORG_GROUP_ID \
 FROM expt_experiment EXP, \
  cfw_group_member GM, \
  lib_org_group OG, \
  cfw_org_group_org_member OGOM \
 where \
  EXP.EXPT_ID=':experiment_id' \
  AND EXP.CFW_GROUP_ID =GM.CFW_GROUP_ID \
  AND OGOM.CFW_ID=GM.CFW_ID \
  AND OG.ORG_GROUP_ID=OGOM.ORG_GROUP_ID \
  AND OG.DESCRIPTION LIKE '%CLONABLE%'

getOrganizationsInGroup = \
 SELECT OM.ORG_ID \
  FROM expt_experiment EXP, \
  cfw_group_member GM, \
  cfw_org_group_org_member OM \
 WHERE EXP.EXPT_ID=':experiment_id' \
  AND EXP.CFW_GROUP_ID =GM.CFW_GROUP_ID \
  AND OM.CFW_ID=GM.CFW_ID \
  AND OM.ORG_GROUP_ID=':group_id'

isULThreadSelected = \
 SELECT * \
  FROM expt_trial_thread \
 WHERE \
   TRIAL_ID=':trial_id' \
   AND THREAD_ID=':thread_id'

setULThreadSelected = \
 INSERT INTO expt_trial_thread \
   (EXPT_ID,TRIAL_ID, THREAD_ID) \
   SELECT \
    EXPT_ID, \
    TRIAL_ID, \
    ':thread_id' \
   FROM expt_trial \
    WHERE \
     TRIAL_ID = ':trial_id'

setULThreadNotSelected = \
 DELETE FROM expt_trial_thread \
   WHERE \
    TRIAL_ID=':trial_id' \
    AND THREAD_ID=':thread_id'

getGroupId = \
 SELECT OG.ORG_GROUP_ID FROM \
  expt_experiment EXP, \
  expt_trial ET, \
  cfw_group_member GM, \
  lib_org_group OG, \
  cfw_org_group_org_member OGOM \
 WHERE ET.TRIAL_ID=':trial_id' \
  AND EXP.EXPT_ID=ET.EXPT_ID \
   AND EXP.CFW_GROUP_ID =GM.CFW_GROUP_ID \
   AND OGOM.CFW_ID=GM.CFW_ID \
   AND OGOM.ORG_GROUP_ID=OG.ORG_GROUP_ID \
   AND OG.DESCRIPTION=':group_name' \

isGroupSelected = \
SELECT * \
  FROM expt_trial_org_mult \
 WHERE \
  TRIAL_ID=':trial_id' \
  AND ORG_GROUP_ID=':group_id'

setGroupNotSelected = \
 DELETE FROM \
   expt_trial_ORG_MULT \
  WHERE TRIAL_ID=':trial_id' \
  AND ORG_GROUP_ID =':group_id'

setGroupSelected = \
 INSERT INTO expt_trial_org_mult \
   (TRIAL_ID, CFW_ID , ORG_GROUP_ID, EXPT_ID, MULTIPLIER, DESCRIPTION) \
   SELECT DISTINCT \
   ET.EXPT_ID, \
   ET.TRIAL_ID, \
   GM.CFW_ID, \
   OG.ORG_GROUP_ID, \
   1, \
   NULL \
   FROM \
    expt_experiment EXP, \
    expt_trial ET, \
    cfw_group_member GM, \
    lib_org_group OG \
   WHERE ET.TRIAL_ID=':trial_id' \
   AND EXP.EXPT_ID=ET.EXPT_ID \
   AND EXP.CFW_GROUP_ID =GM.CFW_GROUP_ID \
   AND OG.CFW_ID=GM.CFW_ID \
   AND OG.ORG_GROUP_ID=':group_id'


getMultiplier = \
 SELECT OM.MULTIPLIER FROM \
   expt_trial_org_mult OM \
 WHERE \
   OM.TRIAL_ID=':trial_id' \
   AND OM.ORG_GROUP_ID=':group_id'


setMultiplier = \
 UPDATE expt_trial_org_mult \
   SET MULTIPLIER= :value \
  WHERE TRIAL_ID=':trial_id' \
  AND ORG_GROUP_ID=':group_id'

nextExperimentId = \
 SELECT EXPERIMENT_NUMBER.NEXTVAL FROM DUAL

nextExperimentIdHack = \
 SELECT MAX(EXPT_ID) FROM expt_experiment \
  WHERE \
  EXPT_ID LIKE ':max_id_pattern'

nextExperimentIdHack.mysql = \
 SELECT MAX(EXPT_ID) FROM expt_experiment \
  WHERE \
  EXPT_ID LIKE ':max_id_pattern'


cloneExperimentEXPT_EXPERIMENTQueries = cloneExperimentEXPT_EXPERIMENT
cloneExperimentEXPT_EXPERIMENT = \
 INSERT INTO  expt_experiment \
   (EXPT_ID, DESCRIPTION, NAME, CFW_GROUP_ID) \
  SELECT \
   ':new_expt_id' , \
   ':new_name' , \
   ':new_name' , \
   VEE.CFW_GROUP_ID \
  FROM \
   expt_experiment VEE \
  WHERE \
  VEE.EXPT_ID=':experiment_id'

cloneExperimentEXPT_EXPERIMENTQueries.mysql = cloneExperimentEXPT_EXPERIMENTCreateTable cloneExperimentEXPT_EXPERIMENTInsert cloneExperimentEXPT_EXPERIMENTDrop

cloneExperimentEXPT_EXPERIMENTCreateTable = \
CREATE TEMPORARY TABLE tmp_expt_experiment:short_assembly_id AS \
SELECT \
   ':new_expt_id' AS EXPT_ID, \
   ':new_name' AS NAME, \
   ':new_name' AS DESCRIPTION, \
   VEE.CFW_GROUP_ID AS CFW_GROUP_ID \
  FROM \
   expt_experiment VEE \
  WHERE \
  VEE.EXPT_ID=':experiment_id'

cloneExperimentEXPT_EXPERIMENTInsert = \
INSERT INTO expt_experiment \
(EXPT_ID,NAME,DESCRIPTION,CFW_GROUP_ID) \
SELECT EXPT_ID,NAME,DESCRIPTION,CFW_GROUP_ID \
       FROM tmp_expt_experiment:short_assembly_id

cloneExperimentEXPT_EXPERIMENTDrop = \
DROP TABLE tmp_expt_experiment:short_assembly_id


cloneExperimentEXPT_TRIALQueries = cloneExperimentEXPT_TRIAL


cloneExperimentEXPT_TRIAL = \
INSERT INTO  expt_trial \
   (TRIAL_ID, EXPT_ID, DESCRIPTION, NAME) \
SELECT \
   (':new_expt_id' || '.TRIAL'), \
   ':new_expt_id' , \
   ':new_name' , \
   ':new_name' \
FROM \
  expt_trial VET WHERE VET.EXPT_ID= ':experiment_id'

cloneExperimentEXPT_TRIALQueries.mysql = cloneExperimentEXPT_TRIALCreateTable cloneExperimentEXPT_TRIALInsert cloneExperimentEXPT_TRIALDrop

cloneExperimentEXPT_TRIALCreateTable  = \
CREATE TEMPORARY TABLE tmp_expt_trial:short_assembly_id AS \
SELECT \
   CONCAT(':new_expt_id','.TRIAL') AS TRIAL_ID, \
   ':new_expt_id' AS EXPT_ID, \
   ':new_name' AS DESCRIPTION, \
   ':new_name' AS NAME \
  FROM \
  expt_trial VET WHERE VET.EXPT_ID= ':experiment_id'

cloneExperimentEXPT_TRIALInsert = \
INSERT INTO  expt_trial \
   (TRIAL_ID, EXPT_ID, DESCRIPTION, NAME) \
SELECT TRIAL_ID, EXPT_ID, DESCRIPTION, NAME \
       FROM tmp_expt_trial:short_assembly_id

cloneExperimentEXPT_TRIALDrop = \
DROP TABLE tmp_expt_trial:short_assembly_id

cloneExperimentEXPT_TRIAL_THREADQueries = cloneExperimentEXPT_TRIAL_THREAD
cloneExperimentEXPT_TRIAL_THREAD = \
 INSERT INTO  expt_trial_thread \
   (EXPT_ID, TRIAL_ID, THREAD_ID) \
  SELECT \
   ':new_expt_id' , \
   (':new_expt_id' || '.TRIAL'), \
   THREAD_ID \
  FROM \
   expt_trial_thread VETT WHERE VETT.EXPT_ID= ':experiment_id'

cloneExperimentEXPT_TRIAL_THREADQueries.mysql = cloneExperimentEXPT_TRIAL_THREADCreateTable cloneExperimentEXPT_TRIAL_THREADInsert cloneExperimentEXPT_TRIAL_THREADDrop

cloneExperimentEXPT_TRIAL_THREADCreateTable = \
CREATE TEMPORARY TABLE tmp_expt_trial_thread:short_assembly_id AS \
SELECT \
   ':new_expt_id' as EXPT_ID, \
   CONCAT(':new_expt_id','.TRIAL') as  TRIAL_ID, \
   THREAD_ID as THREAD_ID \
  FROM \
   expt_trial_thread VETT WHERE VETT.EXPT_ID= ':experiment_id'

cloneExperimentEXPT_TRIAL_THREADInsert = \
INSERT INTO  expt_trial_thread \
   (EXPT_ID, TRIAL_ID, THREAD_ID) \
SELECT EXPT_ID, TRIAL_ID, THREAD_ID \
  FROM tmp_expt_trial_thread:short_assembly_id

cloneExperimentEXPT_TRIAL_THREADDrop = \
DROP TABLE tmp_expt_trial_thread:short_assembly_id

cloneExperimentEXPT_TRIAL_ORG_MULTQueries = cloneExperimentEXPT_TRIAL_ORG_MULT

cloneExperimentEXPT_TRIAL_ORG_MULT = \
INSERT INTO  expt_trial_org_mult \
   (TRIAL_ID, CFW_ID , ORG_GROUP_ID, EXPT_ID, MULTIPLIER, DESCRIPTION) \
  SELECT \
   ':new_expt_id', \
   (':new_expt_id' || '.TRIAL'), \
   CFW_ID, \
   ORG_GROUP_ID, \
   MULTIPLIER, \
   DESCRIPTION \
  FROM \
   expt_trial_org_mult VETOM \
  WHERE \
   VETOM.EXPT_ID= ':experiment_id'

cloneExperimentEXPT_TRIAL_ORG_MULTQueries.mysql = cloneExperimentEXPT_TRIAL_ORG_MULTCreateTable cloneExperimentEXPT_TRIAL_ORG_MULTInsert cloneExperimentEXPT_TRIAL_ORG_MULTDrop

cloneExperimentEXPT_TRIAL_ORG_MULTCreateTable = \
CREATE TEMPORARY TABLE tmp_expt_trial_org_mult:short_assembly_id AS \
SELECT \
   ':new_expt_id' AS TRIAL_ID, \
   CONCAT(':new_expt_id','.TRIAL') AS  CFW_ID, \
   CFW_ID AS ORG_GROUP_ID, \
   ORG_GROUP_ID AS  EXPT_ID, \
   MULTIPLIER AS  MULTIPLIER, \
   DESCRIPTION AS DESCRIPTION \
  FROM \
   expt_trial_org_mult VETOM \
  WHERE \
   VETOM.EXPT_ID= ':experiment_id'

cloneExperimentEXPT_TRIAL_ORG_MULTInsert = \
INSERT INTO  expt_trial_org_mult \
   (TRIAL_ID, CFW_ID , ORG_GROUP_ID, EXPT_ID, MULTIPLIER, DESCRIPTION) \
SELECT TRIAL_ID, CFW_ID , ORG_GROUP_ID, EXPT_ID, MULTIPLIER, DESCRIPTION \
   FROM tmp_expt_trial_org_mult:short_assembly_id

cloneExperimentEXPT_TRIAL_ORG_MULTDrop = \
DROP TABLE  tmp_expt_trial_org_mult:short_assembly_id

# Stuff to copy an experiment follows
# Note that these copy only the config assemblies,
# Not the runtime assemblies
cloneExperimentEXPT_TRIAL_CONFIG_ASSEMBLYQueries = cloneExperimentEXPT_TRIAL_CONFIG_ASSEMBLY

cloneExperimentEXPT_TRIAL_CONFIG_ASSEMBLY = \
INSERT INTO  expt_trial_config_assembly \
   (EXPT_ID,TRIAL_ID,ASSEMBLY_ID,DESCRIPTION) \
 SELECT \
  ':new_expt_id' , \
   (':new_expt_id' || '.TRIAL'), \
   TA.ASSEMBLY_ID, \
   TA.DESCRIPTION \
 FROM \
   expt_trial_config_assembly TA, \
   asb_assembly A \
 WHERE \
    TA.EXPT_ID= ':experiment_id' \
    AND TA.ASSEMBLY_ID=A.ASSEMBLY_ID \
    AND A.ASSEMBLY_TYPE <> 'CSM'

cloneExperimentEXPT_TRIAL_CONFIG_ASSEMBLYQueries.mysql = cloneExperimentEXPT_TRIAL_CONFIG_ASSEMBLYCreateTable cloneExperimentEXPT_TRIAL_CONFIG_ASSEMBLYInsert cloneExperimentEXPT_TRIAL_CONFIG_ASSEMBLYDrop

cloneExperimentEXPT_TRIAL_CONFIG_ASSEMBLYCreateTable = \
CREATE TEMPORARY TABLE tmp_expt_trial_config_assembly:short_assembly_id AS \
SELECT \
  ':new_expt_id' AS EXPT_ID, \
   CONCAT(':new_expt_id','.TRIAL') AS TRIAL_ID, \
   TA.ASSEMBLY_ID AS ASSEMBLY_ID, \
   TA.DESCRIPTION AS DESCRIPTION \
 FROM \
   expt_trial_config_assembly TA, \
   asb_assembly A \
 WHERE \
   TA.EXPT_ID= ':experiment_id' \
   AND TA.ASSEMBLY_ID=A.ASSEMBLY_ID \
   AND A.ASSEMBLY_TYPE <> 'CSM'

cloneExperimentEXPT_TRIAL_CONFIG_ASSEMBLYInsert = \
INSERT INTO  expt_trial_config_assembly \
   (EXPT_ID,TRIAL_ID,ASSEMBLY_ID,DESCRIPTION) \
SELECT EXPT_ID,TRIAL_ID,ASSEMBLY_ID,DESCRIPTION \
       FROM tmp_expt_trial_config_assembly:short_assembly_id

cloneExperimentEXPT_TRIAL_CONFIG_ASSEMBLYDrop = \
DROP TABLE  tmp_expt_trial_config_assembly:short_assembly_id

# Stuff to copy an experiment follows
# Note that these copy only the runtime assemblies,
# Not the config assemblies
cloneExperimentEXPT_TRIAL_ASSEMBLYQueries = cloneExperimentEXPT_TRIAL_ASSEMBLY

cloneExperimentEXPT_TRIAL_ASSEMBLY = \
INSERT INTO  expt_trial_assembly \
   (EXPT_ID,TRIAL_ID,ASSEMBLY_ID,DESCRIPTION) \
 SELECT \
  ':new_expt_id' , \
   (':new_expt_id' || '.TRIAL'), \
   TA.ASSEMBLY_ID, \
   TA.DESCRIPTION \
 FROM \
   expt_trial_assembly TA, \
   asb_assembly A \
 WHERE \
    TA.EXPT_ID= ':experiment_id' \
    AND TA.ASSEMBLY_ID=A.ASSEMBLY_ID \
    AND A.ASSEMBLY_TYPE <> 'CSM'

cloneExperimentEXPT_TRIAL_ASSEMBLYQueries.mysql = cloneExperimentEXPT_TRIAL_ASSEMBLYCreateTable cloneExperimentEXPT_TRIAL_ASSEMBLYInsert cloneExperimentEXPT_TRIAL_ASSEMBLYDrop

cloneExperimentEXPT_TRIAL_ASSEMBLYCreateTable = \
CREATE TEMPORARY TABLE tmp_expt_trial_assembly:short_assembly_id AS \
SELECT \
  ':new_expt_id' AS EXPT_ID, \
   CONCAT(':new_expt_id','.TRIAL') AS TRIAL_ID, \
   TA.ASSEMBLY_ID AS ASSEMBLY_ID, \
   TA.DESCRIPTION AS DESCRIPTION \
 FROM \
   expt_trial_assembly TA, \
   asb_assembly A \
 WHERE \
   TA.EXPT_ID= ':experiment_id' \
   AND TA.ASSEMBLY_ID=A.ASSEMBLY_ID \
   AND A.ASSEMBLY_TYPE <> 'CSM'

cloneExperimentEXPT_TRIAL_ASSEMBLYInsert = \
INSERT INTO  expt_trial_assembly \
   (EXPT_ID,TRIAL_ID,ASSEMBLY_ID,DESCRIPTION) \
SELECT EXPT_ID,TRIAL_ID,ASSEMBLY_ID,DESCRIPTION \
       FROM tmp_expt_trial_assembly:short_assembly_id

cloneExperimentEXPT_TRIAL_ASSEMBLYDrop = \
DROP TABLE  tmp_expt_trial_assembly:short_assembly_id

