s@ECHO OFF

REM "<copyright>"
REM " Copyright 2003 BBNT Solutions, LLC"
REM " under sponsorship of the Defense Advanced Research Projects Agency (DARPA)."
REM ""
REM " This program is free software; you can redistribute it and/or modify"
REM " it under the terms of the Cougaar Open Source License as published by"
REM " DARPA on the Cougaar Open Source Website (www.cougaar.org)."
REM ""
REM " THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS"
REM " PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR"
REM " IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF"
REM " MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT"
REM " ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT"
REM " HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL"
REM " DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,"
REM " TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR"
REM " PERFORMANCE OF THE COUGAAR SOFTWARE."
REM "</copyright>"


COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\alib_component_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\alib_component_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\alib_component.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\asb_agent_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\asb_agent_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\asb_agent.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\asb_agent_pg_attr_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\asb_agent_pg_attr_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\asb_agent_pg_attr.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\asb_agent_relation_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\asb_agent_relation_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\asb_agent_relation.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\alploc_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\alploc_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\alploc.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\asb_assembly_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\asb_assembly_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\asb_assembly.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\asb_component_arg_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\asb_component_arg_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\asb_component_arg.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\asb_component_hierarchy_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\asb_component_hierarchy_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\asb_component_hierarchy.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\oplan_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\oplan_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\oplan.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\oplan_agent_attr_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\oplan_agent_attr_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\oplan_agent_attr.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\expt_experiment_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\expt_experiment_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\expt_experiment.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\expt_trial_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\expt_trial_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\expt_trial.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\expt_trial_assembly_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\expt_trial_assembly_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\expt_trial_assembly.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\expt_trial_config_assembly_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\expt_trial_config_assembly_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\expt_trial_config_assembly.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\expt_trial_mod_recipe_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\expt_trial_mod_recipe_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\expt_trial_mod_recipe.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\expt_trial_org_mult_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\expt_trial_org_mult_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\expt_trial_org_mult.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\expt_trial_thread_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\expt_trial_thread_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\expt_trial_thread.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_activity_type_ref_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_activity_type_ref_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_activity_type_ref.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_agent_org_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_agent_org_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_agent_org.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_clone_set_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_clone_set_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_clone_set.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_component_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_component_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_component.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_component_arg_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_component_arg_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_component_arg.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_mod_recipe_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_mod_recipe_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_mod_recipe.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_mod_recipe_arg_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_mod_recipe_arg_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_mod_recipe_arg.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_pg_attribute_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_pg_attribute_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_pg_attribute.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\cfw_context_plugin_arg_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\cfw_context_plugin_arg_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\cfw_context_plugin_arg.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\cfw_group_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\cfw_group_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\cfw_group.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\cfw_group_org_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\cfw_group_org_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\cfw_group_org.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\cfw_group_member_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\cfw_group_member_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\cfw_group_member.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\cfw_instance_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\cfw_instance_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\cfw_instance.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\cfw_org_group_org_member_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\cfw_org_group_org_member_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\cfw_org_group_org_member.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\cfw_org_hierarchy_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\cfw_org_hierarchy_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\cfw_org_hierarchy.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\cfw_org_list_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\cfw_org_list_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\cfw_org_list.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\cfw_org_og_relation_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\cfw_org_og_relation_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\cfw_org_og_relation.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\cfw_org_orgtype_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\cfw_org_orgtype_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\cfw_org_orgtype.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\cfw_org_pg_attr_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\cfw_org_pg_attr_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\cfw_org_pg_attr.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\cfw_orgtype_plugin_grp_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\cfw_orgtype_plugin_grp_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\cfw_orgtype_plugin_grp.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\cfw_plugin_group_member_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\cfw_plugin_group_member_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\cfw_plugin_group_member.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_org_group_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_org_group_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_org_group.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_organization_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_organization_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_organization.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_orgtype_ref_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_orgtype_ref_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_orgtype_ref.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_plugin_arg_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_plugin_arg_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_plugin_arg.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_plugin_arg_thread_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_plugin_arg_thread_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_plugin_arg_thread.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_plugin_group_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_plugin_group_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_plugin_group.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_plugin_ref_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_plugin_ref_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_plugin_ref.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_plugin_thread_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_plugin_thread_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_plugin_thread.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_role_ref_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_role_ref_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_role_ref.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_role_thread_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_role_thread_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_role_thread.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\lib_thread_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\lib_thread_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\lib_thread.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\community_entity_attribute_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\community_entity_attribute_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\community_entity_attribute.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\community_attribute_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\community_attribute_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\community_attribute.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\dual_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\dual_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\dual.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\fdm_unit_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\fdm_unit_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unit.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\fdm_unit_equipment_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\fdm_unit_equipment_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unit_equipment.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\fdm_transportable_item_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\fdm_transportable_item_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_transportable_item.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\fdm_transportable_item_detail_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\fdm_transportable_item_detail_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_transportable_item_detail.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\fdm_unfrmd_srvc_occ_rnk_subcat_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\fdm_unfrmd_srvc_occ_rnk_subcat_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unfrmd_srvc_occ_rnk_subcat.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\fdm_unfrmd_srvc_occptn_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\fdm_unfrmd_srvc_occptn_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unfrmd_srvc_occptn.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\fdm_unfrmd_srvc_rnk_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\fdm_unfrmd_srvc_rnk_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unfrmd_srvc_rnk.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\fdm_unit_billet_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\fdm_unit_billet_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\fdm_unit_billet.csv
COPY /b %COUGAAR_INSTALL_PATH%\csmart\data\database\headers\geoloc_header.csv+%COUGAAR_INSTALL_PATH%\csmart\data\database\raw_data\geoloc_data.csv %COUGAAR_INSTALL_PATH%\csmart\data\database\csv\geoloc.csv

