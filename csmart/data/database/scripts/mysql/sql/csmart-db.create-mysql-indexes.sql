## INDEX: reflib_clone_set38 
##

CREATE INDEX reflib_clone_set38 ON alib_component(CLONE_SET_ID);
;
## 
## INDEX: reflib_component13 
##

CREATE INDEX reflib_component13 ON alib_component(COMPONENT_LIB_ID);
;
## 
## INDEX: refasb_assembly42 
##

CREATE INDEX refasb_assembly42 ON asb_agent(ASSEMBLY_ID);
;
## 
## INDEX: refalib_component43 
##

CREATE INDEX refalib_component43 ON asb_agent(COMPONENT_ALIB_ID);
;
## 
## INDEX: reflib_component44 
##

CREATE INDEX reflib_component44 ON asb_agent(COMPONENT_LIB_ID);
;
## 
## INDEX: refasb_assembly36 
##

CREATE INDEX refasb_assembly36 ON asb_agent_pg_attr(ASSEMBLY_ID);
;
## 
## INDEX: refalib_component12 
##

CREATE INDEX refalib_component12 ON asb_agent_pg_attr(COMPONENT_ALIB_ID);
;
## 
## INDEX: reflib_pg_attribute17 
##

CREATE INDEX reflib_pg_attribute17 ON asb_agent_pg_attr(PG_ATTRIBUTE_LIB_ID);
;
## 
## INDEX: refasb_assembly4 
##

CREATE INDEX refasb_assembly4 ON asb_agent_relation(ASSEMBLY_ID);
;
## 
## INDEX: refalib_component31 
##

CREATE INDEX refalib_component31 ON asb_agent_relation(SUPPORTING_COMPONENT_ALIB_ID);
;
## 
## INDEX: refalib_component32 
##

CREATE INDEX refalib_component32 ON asb_agent_relation(SUPPORTED_COMPONENT_ALIB_ID);
;
## 
## INDEX: refasb_assembly18 
##

CREATE INDEX refasb_assembly18 ON asb_alploc(ASSEMBLY_ID);
;
## 
## INDEX: refalib_component11 
##

CREATE INDEX refalib_component11 ON asb_component_arg(COMPONENT_ALIB_ID);
;
## 
## INDEX: refasb_assembly34 
##

CREATE INDEX refasb_assembly34 ON asb_component_arg(ASSEMBLY_ID);
;
## 
## INDEX: refalib_component28 
##

CREATE INDEX refalib_component28 ON asb_component_hierarchy(COMPONENT_ALIB_ID);
;
## 
## INDEX: refalib_component29 
##

CREATE INDEX refalib_component29 ON asb_component_hierarchy(PARENT_COMPONENT_ALIB_ID);
;
## 
## INDEX: refasb_assembly30 
##

CREATE INDEX refasb_assembly30 ON asb_component_hierarchy(ASSEMBLY_ID);
;
## 
## INDEX: refasb_assembly19 
##

CREATE INDEX refasb_assembly19 ON asb_oplan(ASSEMBLY_ID);
;
## 
## INDEX: refasb_oplan20 
##

CREATE INDEX refasb_oplan20 ON asb_oplan_agent_attr(OPLAN_ID, ASSEMBLY_ID);
;
## 
## INDEX: refalib_component21 
##

CREATE INDEX refalib_component21 ON asb_oplan_agent_attr(COMPONENT_ALIB_ID);
;
## 
## INDEX: refalib_component21 
##

CREATE INDEX refalib_component99 ON oplan_agent_attr(ORG_ID);
;
## 
## INDEX: refcfw_instance4 
##

CREATE INDEX refcfw_instance4 ON cfw_alploc(CFW_ID);
;
## 
## INDEX: reflib_plugin_arg43 
##

CREATE INDEX reflib_plugin_arg43 ON cfw_context_plugin_arg(PLUGIN_ARG_ID);
;
## 
## INDEX: refcfw_instance44 
##

CREATE INDEX refcfw_instance44 ON cfw_context_plugin_arg(CFW_ID);
;
## 
## INDEX: refcfw_group2 
##

CREATE INDEX refcfw_group2 ON cfw_group_member(CFW_GROUP_ID);
;
## 
## INDEX: refcfw_instance3 
##

CREATE INDEX refcfw_instance3 ON cfw_group_member(CFW_ID);
;
## 
## INDEX: refcfw_group32 
##

CREATE INDEX refcfw_group32 ON cfw_group_org(CFW_GROUP_ID);
;
## 
## INDEX: reflib_organization33 
##

CREATE INDEX reflib_organization33 ON cfw_group_org(ORG_ID);
;
## 
## INDEX: refcfw_instance5 
##

CREATE INDEX refcfw_instance5 ON cfw_oplan(CFW_ID);
;
## 
## INDEX: refcfw_oplan62 
##

CREATE INDEX refcfw_oplan62 ON cfw_oplan_activity(OPLAN_ID, CFW_ID);
;
## 
## INDEX: reflib_org_group63 
##

CREATE INDEX reflib_org_group63 ON cfw_oplan_activity(ORG_GROUP_ID);
;
## 
## INDEX: reflib_org_group67 
##

CREATE INDEX reflib_org_group67 ON cfw_oplan_loc(ORG_GROUP_ID);
;
## 
## INDEX: refcfw_oplan64 
##

CREATE INDEX refcfw_oplan64 ON cfw_oplan_loc(OPLAN_ID, CFW_ID);
;
## 
## INDEX: reflib_org_group36 
##

CREATE INDEX reflib_org_group36 ON cfw_oplan_og_attr(ORG_GROUP_ID);
;
## 
## INDEX: refcfw_oplan7 
##

CREATE INDEX refcfw_oplan7 ON cfw_oplan_og_attr(OPLAN_ID, CFW_ID);
;
## 
## INDEX: reflib_org_group11 
##

CREATE INDEX reflib_org_group11 ON cfw_org_group_org_member(ORG_GROUP_ID);
;
## 
## INDEX: refcfw_instance60 
##

CREATE INDEX refcfw_instance60 ON cfw_org_group_org_member(CFW_ID);
;
## 
## INDEX: reflib_organization61 
##

CREATE INDEX reflib_organization61 ON cfw_org_group_org_member(ORG_ID);
;
## 
## INDEX: refcfw_instance56 
##

CREATE INDEX refcfw_instance56 ON cfw_org_hierarchy(CFW_ID);
;
## 
## INDEX: reflib_organization57 
##

CREATE INDEX reflib_organization57 ON cfw_org_hierarchy(ORG_ID);
;
## 
## INDEX: reflib_organization59 
##

CREATE INDEX reflib_organization59 ON cfw_org_hierarchy(SUPERIOR_ORG_ID);
;
## 
## INDEX: reflib_organization16 
##

CREATE INDEX reflib_organization16 ON cfw_org_list(ORG_ID);
;
## 
## INDEX: refcfw_instance17 
##

CREATE INDEX refcfw_instance17 ON cfw_org_list(CFW_ID);
;
## 
## INDEX: reflib_org_group20 
##

CREATE INDEX reflib_org_group20 ON cfw_org_og_relation(ORG_GROUP_ID);
;
## 
## INDEX: reflib_role_ref31 
##

CREATE INDEX reflib_role_ref31 ON cfw_org_og_relation(ROLE);
;
## 
## INDEX: reflib_organization52 
##

CREATE INDEX reflib_organization52 ON cfw_org_og_relation(ORG_ID);
;
## 
## INDEX: refcfw_instance53 
##

CREATE INDEX refcfw_instance53 ON cfw_org_og_relation(CFW_ID);
;
## 
## INDEX: refcfw_org_list47 
##

CREATE INDEX refcfw_org_list47 ON cfw_org_orgtype(ORG_ID, CFW_ID);
;
## 
## INDEX: reflib_orgtype_ref48 
##

CREATE INDEX reflib_orgtype_ref48 ON cfw_org_orgtype(ORGTYPE_ID);
;
## 
## INDEX: refcfw_org_list37 
##

CREATE INDEX refcfw_org_list37 ON cfw_org_pg_attr(ORG_ID, CFW_ID);
;
## 
## INDEX: reflib_orgtype_ref21 
##

CREATE INDEX reflib_orgtype_ref21 ON cfw_orgtype_plugin_grp(ORGTYPE_ID);
;
## 
## INDEX: refcfw_instance22 
##

CREATE INDEX refcfw_instance22 ON cfw_orgtype_plugin_grp(CFW_ID);
;
## 
## INDEX: reflib_plugin_group23 
##

CREATE INDEX reflib_plugin_group23 ON cfw_orgtype_plugin_grp(PLUGIN_GROUP_ID);
;
## 
## INDEX: reflib_plugin_ref26 
##

CREATE INDEX reflib_plugin_ref26 ON cfw_plugin_group_member(PLUGIN_ID);
;
## 
## INDEX: reflib_plugin_group27 
##

CREATE INDEX reflib_plugin_group27 ON cfw_plugin_group_member(PLUGIN_GROUP_ID);
;
## 
## INDEX: refcfw_instance28 
##

CREATE INDEX refcfw_instance28 ON cfw_plugin_group_member(CFW_ID);
;
## 
## INDEX: Ref677 
##

CREATE INDEX Ref677 ON community_attribute(ASSEMBLY_ID);
;
## 
## INDEX: Ref676 
##

CREATE INDEX Ref676 ON community_entity_attribute(ASSEMBLY_ID);
;
## 
## INDEX: refexpt_experiment26 
##

CREATE INDEX refexpt_experiment26 ON expt_trial(EXPT_ID);
;
## 
## INDEX: refexpt_experiment50 
##

CREATE INDEX refexpt_experiment50 ON expt_trial_assembly(EXPT_ID);
;
## 
## INDEX: refasb_assembly2 
##

CREATE INDEX refasb_assembly2 ON expt_trial_assembly(ASSEMBLY_ID);
;
## 
## INDEX: refexpt_trial27 
##

CREATE INDEX refexpt_trial27 ON expt_trial_assembly(TRIAL_ID);
;
## 
## INDEX: Ref1278 
##

CREATE INDEX Ref1278 ON expt_trial_config_assembly(TRIAL_ID);
;
## 
## INDEX: Ref1179 
##

CREATE INDEX Ref1179 ON expt_trial_config_assembly(EXPT_ID);
;
## 
## INDEX: Ref680 
##

CREATE INDEX Ref680 ON expt_trial_config_assembly(ASSEMBLY_ID);
;
## 
## INDEX: refexpt_trial47 
##

CREATE INDEX refexpt_trial47 ON expt_trial_mod_recipe(TRIAL_ID);
;
## 
## INDEX: refexpt_experiment51 
##

CREATE INDEX refexpt_experiment51 ON expt_trial_mod_recipe(EXPT_ID);
;
## 
## INDEX: reflib_mod_recipe56 
##

CREATE INDEX reflib_mod_recipe56 ON expt_trial_mod_recipe(MOD_RECIPE_LIB_ID);
;
## 
## INDEX: refexpt_trial41 
##

CREATE INDEX refexpt_trial41 ON expt_trial_org_mult(TRIAL_ID);
;
## 
## INDEX: refexpt_experiment52 
##

CREATE INDEX refexpt_experiment52 ON expt_trial_org_mult(EXPT_ID);
;
## 
## INDEX: refexpt_trial40 
##

CREATE INDEX refexpt_trial40 ON expt_trial_thread(TRIAL_ID);
;
## 
## INDEX: refexpt_experiment53 
##

CREATE INDEX refexpt_experiment53 ON expt_trial_thread(EXPT_ID);
;
## 
## INDEX: reflib_component45 
##

CREATE INDEX reflib_component45 ON lib_agent_org(COMPONENT_LIB_ID);
;
## 
## INDEX: reflib_component14 
##

CREATE INDEX reflib_component14 ON lib_component_arg(COMPONENT_LIB_ID);
;
## 
## INDEX: reflib_mod_recipe55 
##

CREATE INDEX reflib_mod_recipe55 ON lib_mod_recipe_arg(MOD_RECIPE_LIB_ID);
;
## 
## INDEX: Ref5081 
##

CREATE INDEX Ref5081 ON lib_plugin_arg(PLUGIN_ID);
;
## 
## INDEX: reflib_thread41 
##

CREATE INDEX reflib_thread41 ON lib_plugin_arg_thread(THREAD_ID);
;
## 
## INDEX: reflib_plugin_arg42 
##

CREATE INDEX reflib_plugin_arg42 ON lib_plugin_arg_thread(PLUGIN_ARG_ID);
;
## 
## INDEX: reflib_plugin_ref29 
##

CREATE INDEX reflib_plugin_ref29 ON lib_plugin_thread(PLUGIN_ID);
;
## 
## INDEX: reflib_thread30 
##

CREATE INDEX reflib_thread30 ON lib_plugin_thread(THREAD_ID);
;
## 
## INDEX: reflib_role_ref49 
##

CREATE INDEX reflib_role_ref49 ON lib_role_thread(ROLE);
;
## 
## INDEX: reflib_thread50 
##

CREATE INDEX reflib_thread50 ON lib_role_thread(THREAD_ID);
;
## 
## TABLE: alib_component 
##

