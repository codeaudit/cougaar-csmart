-- Get the ID of the assembly containing the plugin
-- which is to be swapped.
-- Note: if the plugin is in more than one assembly,
-- there will be problems.
-- Used by switchPlugin-class.sh

SELECT DISTINCT AA.ASSEMBLY_ID
 FROM V4_ASB_COMPONENT_HIERARCHY AA,
     V4_EXPT_EXPERIMENT E,
     V4_EXPT_TRIAL ET,
     V4_EXPT_TRIAL_ASSEMBLY EA
  WHERE AA.COMPONENT_ALIB_ID LIKE '%|:oldP'
      AND E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':newExpt';