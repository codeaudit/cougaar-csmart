
# <copyright>
#  Copyright 2003 BBNT Solutions, LLC
#  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
# 
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the Cougaar Open Source License as published by
#  DARPA on the Cougaar Open Source Website (www.cougaar.org).
# 
#  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
#  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
#  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
#  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
#  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
#  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
#  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
#  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
#  PERFORMANCE OF THE COUGAAR SOFTWARE.
# </copyright>

HEADER_PATH=${COUGAAR_INSTALL_PATH}/csmart/data/database/headers
DATA_PATH=${COUGAAR_INSTALL_PATH}/csmart/data/database/raw_data
CSV_PATH=${COUGAAR_INSTALL_PATH}/csmart/data/database/csv


cat $HEADER_PATH/alib_component_header.csv $DATA_PATH/alib_component_data.csv > $CSV_PATH/alib_component.csv
cat $HEADER_PATH/asb_agent_header.csv $DATA_PATH/asb_agent_data.csv > $CSV_PATH/asb_agent.csv
cat $HEADER_PATH/asb_agent_pg_attr_header.csv $DATA_PATH/asb_agent_pg_attr_data.csv > $CSV_PATH/asb_agent_pg_attr.csv
cat $HEADER_PATH/asb_agent_relation_header.csv $DATA_PATH/asb_agent_relation_data.csv > $CSV_PATH/asb_agent_relation.csv
cat $HEADER_PATH/asb_alploc_header.csv $DATA_PATH/asb_alploc_data.csv > $CSV_PATH/asb_alploc.csv
cat $HEADER_PATH/asb_assembly_header.csv $DATA_PATH/asb_assembly_data.csv > $CSV_PATH/asb_assembly.csv
cat $HEADER_PATH/asb_component_arg_header.csv $DATA_PATH/asb_component_arg_data.csv > $CSV_PATH/asb_component_arg.csv
cat $HEADER_PATH/asb_component_hierarchy_header.csv $DATA_PATH/asb_component_hierarchy_data.csv > $CSV_PATH/asb_component_hierarchy.csv
cat $HEADER_PATH/asb_oplan_header.csv $DATA_PATH/asb_oplan_data.csv > $CSV_PATH/asb_oplan.csv
cat $HEADER_PATH/asb_oplan_agent_attr_header.csv $DATA_PATH/asb_oplan_agent_attr_data.csv > $CSV_PATH/asb_oplan_agent_attr.csv
cat $HEADER_PATH/lib_oplan_header.csv $DATA_PATH/lib_oplan_data.csv > $CSV_PATH/lib_oplan.csv
cat $HEADER_PATH/oplan_agent_attr_header.csv $DATA_PATH/oplan_agent_attr_data.csv > $CSV_PATH/oplan_agent_attr.csv
cat $HEADER_PATH/expt_experiment_header.csv $DATA_PATH/expt_experiment_data.csv > $CSV_PATH/expt_experiment.csv
cat $HEADER_PATH/expt_trial_header.csv $DATA_PATH/expt_trial_data.csv > $CSV_PATH/expt_trial.csv
cat $HEADER_PATH/expt_trial_assembly_header.csv $DATA_PATH/expt_trial_assembly_data.csv > $CSV_PATH/expt_trial_assembly.csv
cat $HEADER_PATH/expt_trial_config_assembly_header.csv $DATA_PATH/expt_trial_config_assembly_data.csv > $CSV_PATH/expt_trial_config_assembly.csv
cat $HEADER_PATH/expt_trial_mod_recipe_header.csv $DATA_PATH/expt_trial_mod_recipe_data.csv > $CSV_PATH/expt_trial_mod_recipe.csv
cat $HEADER_PATH/expt_trial_org_mult_header.csv $DATA_PATH/expt_trial_org_mult_data.csv > $CSV_PATH/expt_trial_org_mult.csv
cat $HEADER_PATH/expt_trial_thread_header.csv $DATA_PATH/expt_trial_thread_data.csv > $CSV_PATH/expt_trial_thread.csv
cat $HEADER_PATH/lib_activity_type_ref_header.csv $DATA_PATH/lib_activity_type_ref_data.csv > $CSV_PATH/lib_activity_type_ref.csv
cat $HEADER_PATH/lib_agent_org_header.csv $DATA_PATH/lib_agent_org_data.csv > $CSV_PATH/lib_agent_org.csv
cat $HEADER_PATH/lib_clone_set_header.csv $DATA_PATH/lib_clone_set_data.csv > $CSV_PATH/lib_clone_set.csv
cat $HEADER_PATH/lib_component_header.csv $DATA_PATH/lib_component_data.csv > $CSV_PATH/lib_component.csv
cat $HEADER_PATH/lib_component_arg_header.csv $DATA_PATH/lib_component_arg_data.csv > $CSV_PATH/lib_component_arg.csv
cat $HEADER_PATH/lib_mod_recipe_header.csv $DATA_PATH/lib_mod_recipe_data.csv > $CSV_PATH/lib_mod_recipe.csv
cat $HEADER_PATH/lib_mod_recipe_arg_header.csv $DATA_PATH/lib_mod_recipe_arg_data.csv > $CSV_PATH/lib_mod_recipe_arg.csv
cat $HEADER_PATH/lib_pg_attribute_header.csv $DATA_PATH/lib_pg_attribute_data.csv > $CSV_PATH/lib_pg_attribute.csv

cat $HEADER_PATH/cfw_alploc_header.csv $DATA_PATH/cfw_alploc_data.csv > $CSV_PATH/cfw_alploc.csv
cat $HEADER_PATH/cfw_context_plugin_arg_header.csv $DATA_PATH/cfw_context_plugin_arg_data.csv > $CSV_PATH/cfw_context_plugin_arg.csv
cat $HEADER_PATH/cfw_group_header.csv $DATA_PATH/cfw_group_data.csv > $CSV_PATH/cfw_group.csv
cat $HEADER_PATH/cfw_group_org_header.csv $DATA_PATH/cfw_group_org_data.csv > $CSV_PATH/cfw_group_org.csv
cat $HEADER_PATH/cfw_group_member_header.csv $DATA_PATH/cfw_group_member_data.csv > $CSV_PATH/cfw_group_member.csv
cat $HEADER_PATH/cfw_instance_header.csv $DATA_PATH/cfw_instance_data.csv > $CSV_PATH/cfw_instance.csv
cat $HEADER_PATH/cfw_oplan_header.csv $DATA_PATH/cfw_oplan_data.csv > $CSV_PATH/cfw_oplan.csv
cat $HEADER_PATH/cfw_oplan_activity_header.csv $DATA_PATH/cfw_oplan_activity_data.csv > $CSV_PATH/cfw_oplan_activity.csv
cat $HEADER_PATH/cfw_oplan_loc_header.csv $DATA_PATH/cfw_oplan_loc_data.csv > $CSV_PATH/cfw_oplan_loc.csv
cat $HEADER_PATH/cfw_oplan_og_attr_header.csv $DATA_PATH/cfw_oplan_og_attr_data.csv > $CSV_PATH/cfw_oplan_og_attr.csv
cat $HEADER_PATH/cfw_org_group_org_member_header.csv $DATA_PATH/cfw_org_group_org_member_data.csv > $CSV_PATH/cfw_org_group_org_member.csv
cat $HEADER_PATH/cfw_org_hierarchy_header.csv $DATA_PATH/cfw_org_hierarchy_data.csv > $CSV_PATH/cfw_org_hierarchy.csv
cat $HEADER_PATH/cfw_org_list_header.csv $DATA_PATH/cfw_org_list_data.csv > $CSV_PATH/cfw_org_list.csv
cat $HEADER_PATH/cfw_org_og_relation_header.csv $DATA_PATH/cfw_org_og_relation_data.csv > $CSV_PATH/cfw_org_og_relation.csv
cat $HEADER_PATH/cfw_org_orgtype_header.csv $DATA_PATH/cfw_org_orgtype_data.csv > $CSV_PATH/cfw_org_orgtype.csv
cat $HEADER_PATH/cfw_org_pg_attr_header.csv $DATA_PATH/cfw_org_pg_attr_data.csv > $CSV_PATH/cfw_org_pg_attr.csv
cat $HEADER_PATH/cfw_orgtype_plugin_grp_header.csv $DATA_PATH/cfw_orgtype_plugin_grp_data.csv > $CSV_PATH/cfw_orgtype_plugin_grp.csv
cat $HEADER_PATH/cfw_plugin_group_member_header.csv $DATA_PATH/cfw_plugin_group_member_data.csv > $CSV_PATH/cfw_plugin_group_member.csv
cat $HEADER_PATH/lib_org_group_header.csv $DATA_PATH/lib_org_group_data.csv > $CSV_PATH/lib_org_group.csv
cat $HEADER_PATH/lib_organization_header.csv $DATA_PATH/lib_organization_data.csv > $CSV_PATH/lib_organization.csv
cat $HEADER_PATH/lib_orgtype_ref_header.csv $DATA_PATH/lib_orgtype_ref_data.csv > $CSV_PATH/lib_orgtype_ref.csv
cat $HEADER_PATH/lib_plugin_arg_header.csv $DATA_PATH/lib_plugin_arg_data.csv > $CSV_PATH/lib_plugin_arg.csv
cat $HEADER_PATH/lib_plugin_arg_thread_header.csv $DATA_PATH/lib_plugin_arg_thread_data.csv > $CSV_PATH/lib_plugin_arg_thread.csv
cat $HEADER_PATH/lib_plugin_group_header.csv $DATA_PATH/lib_plugin_group_data.csv > $CSV_PATH/lib_plugin_group.csv
cat $HEADER_PATH/lib_plugin_ref_header.csv $DATA_PATH/lib_plugin_ref_data.csv > $CSV_PATH/lib_plugin_ref.csv
cat $HEADER_PATH/lib_plugin_thread_header.csv $DATA_PATH/lib_plugin_thread_data.csv > $CSV_PATH/lib_plugin_thread.csv
cat $HEADER_PATH/lib_role_ref_header.csv $DATA_PATH/lib_role_ref_data.csv > $CSV_PATH/lib_role_ref.csv
cat $HEADER_PATH/lib_role_thread_header.csv $DATA_PATH/lib_role_thread_data.csv > $CSV_PATH/lib_role_thread.csv
cat $HEADER_PATH/lib_thread_header.csv $DATA_PATH/lib_thread_data.csv > $CSV_PATH/lib_thread.csv
cat $HEADER_PATH/community_entity_attribute_header.csv $DATA_PATH/community_entity_attribute_data.csv > $CSV_PATH/community_entity_attribute.csv
cat $HEADER_PATH/community_attribute_header.csv $DATA_PATH/community_attribute_data.csv > $CSV_PATH/community_attribute.csv
cat $HEADER_PATH/dual_header.csv $DATA_PATH/dual_data.csv > $CSV_PATH/dual.csv
cat $HEADER_PATH/fdm_unit_header.csv $DATA_PATH/fdm_unit_data.csv > $CSV_PATH/fdm_unit.csv
cat $HEADER_PATH/fdm_unit_equipment_header.csv $DATA_PATH/fdm_unit_equipment_data.csv > $CSV_PATH/fdm_unit_equipment.csv
cat $HEADER_PATH/fdm_transportable_item_header.csv $DATA_PATH/fdm_transportable_item_data.csv > $CSV_PATH/fdm_transportable_item.csv
cat $HEADER_PATH/fdm_transportable_item_detail_header.csv $DATA_PATH/fdm_transportable_item_detail_data.csv > $CSV_PATH/fdm_transportable_item_detail.csv
cat $HEADER_PATH/fdm_unfrmd_srvc_occ_rnk_subcat_header.csv $DATA_PATH/fdm_unfrmd_srvc_occ_rnk_subcat_data.csv > $CSV_PATH/fdm_unfrmd_srvc_occ_rnk_subcat.csv
cat $HEADER_PATH/fdm_unfrmd_srvc_occptn_header.csv $DATA_PATH/fdm_unfrmd_srvc_occptn_data.csv > $CSV_PATH/fdm_unfrmd_srvc_occptn.csv
cat $HEADER_PATH/fdm_unfrmd_srvc_rnk_header.csv $DATA_PATH/fdm_unfrmd_srvc_rnk_data.csv > $CSV_PATH/fdm_unfrmd_srvc_rnk.csv
cat $HEADER_PATH/fdm_unit_billet_header.csv $DATA_PATH/fdm_unit_billet_data.csv > $CSV_PATH/fdm_unit_billet.csv
cat $HEADER_PATH/geoloc_header.csv $DATA_PATH/geoloc_data.csv > $CSV_PATH/geoloc.csv

