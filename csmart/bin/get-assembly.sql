-- Get the ID of the assembly containing the plugin
-- which is to be swapped.
-- Note: if the plugin is in more than one assembly,
-- there will be problems.
-- Used by switchPlugin-class.sh

-- FIXME: This wont find the assembly(ies)
-- Used for config and not runtime
-- And of course the caller still only
-- Likes getting one assembly
SELECT DISTINCT AA.ASSEMBLY_ID
 FROM v4_asb_component_hierarchy aa,
     v4_expt_experiment E,
     v4_expt_trial ET,
     v4_expt_trial_assembly EA
  WHERE AA.COMPONENT_ALIB_ID LIKE '%|:oldP'
      AND E.EXPT_ID = ET.EXPT_ID
      AND ET.TRIAL_ID = EA.TRIAL_ID
      AND EA.ASSEMBLY_ID = AA.ASSEMBLY_ID
      AND E.NAME = ':newExpt';