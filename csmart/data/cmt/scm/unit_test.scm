(vq "select assembly_id,count(*) from V4_ASB_AGENT group by assembly_id")
(vq "select assembly_id,count(*) from V4_ASB_AGENT_PG_ATTR group by assembly_id")
(vq "select assembly_id,count(*) from V4_ASB_AGENT_RELATION group by assembly_id")
(vq "select assembly_id,count(*) from V4_ASB_ASSEMBLY group by assembly_id")
(vq "select assembly_id,count(*) from V4_ASB_COMPONENT_ARG group by assembly_id")
(vq "select assembly_id,count(*) from V4_ASB_COMPONENT_HIERARCHY group by assembly_id")

(vq (string-append "select * from v4_asb_component_arg a where assembly_id like '%S T%' and not exists"
                     " (select assembly_id from v4_asb_component_arg b where a.component_alib_id=b.component_alib_id and "
                     "a.argument=b.argument and b.assembly_id like '%A-5%')"))

(vq "select * from V4_ASB_AGENT_PG_ATTR a where assembly_id like '%A-5%' and not exists (select assembly_id from V4_ASB_AGENT_PG_ATTR b where assembly_id like '%S T%' and a.component_alib_id=b.component_alib_id and a.PG_ATTRIBUTE_LIB_ID=b.PG_ATTRIBUTE_LIB_ID and a.ATTRIBUTE_VALUE=b.ATTRIBUTE_VALUE and a.ATTRIBUTE_ORDER=b.ATTRIBUTE_ORDER and a.start_date=b.start_date)")
(vq "select * from V4_ASB_AGENT_PG_ATTR a where assembly_id like '%S T%' and not exists (select assembly_id from V4_ASB_AGENT_PG_ATTR b where assembly_id like '%A-5%' and a.component_alib_id=b.component_alib_id and a.PG_ATTRIBUTE_LIB_ID=b.PG_ATTRIBUTE_LIB_ID and a.ATTRIBUTE_VALUE=b.ATTRIBUTE_VALUE and a.ATTRIBUTE_ORDER=b.ATTRIBUTE_ORDER and a.start_date=b.start_date)")
(vq "select * from V4_ASB_AGENT_RELATION a where assembly_id like '%S T%' and not exists (select assembly_id from V4_ASB_AGENT_RELATION b where assembly_id like '%A-5%' and a.supporting_component_alib_id=b.supporting_component_alib_id and a.supported_component_alib_id=b.supported_component_alib_id and a.role=b.role and a.start_date=b.start_date)")
(vq "select * from V4_ASB_AGENT_RELATION a where assembly_id like '%A-5%' and not exists (select assembly_id from V4_ASB_AGENT_RELATION b where assembly_id like '%S T%' and a.supporting_component_alib_id=b.supporting_component_alib_id and a.supported_component_alib_id=b.supported_component_alib_id and a.role=b.role and a.start_date=b.start_date)")
(vq "select * from V4_ASB_component_arg a where assembly_id like '%S T%' and not exists (select assembly_id from V4_ASB_component_arg b where assembly_id like '%A-5%' and a.component_alib_id=b.component_alib_id and a.argument=b.argument)")
(vq "select * from V4_ASB_component_arg a where assembly_id like '%A-5%' and not exists (select assembly_id from V4_ASB_component_arg b where assembly_id like '%S T%' and a.component_alib_id=b.component_alib_id and a.argument=b.argument)")
(vq "select * from V4_ASB_component_hierarchy a where assembly_id like '%A-5%' and not exists (select assembly_id from V4_ASB_component_hierarchy b where assembly_id like '%S T%' and a.component_alib_id=b.component_alib_id and a.parent_component_alib_id=b.parent_component_alib_id)")
(vq "select * from V4_ASB_component_hierarchy a where assembly_id like '%S T%' and not exists (select assembly_id from V4_ASB_component_hierarchy b where assembly_id like '%A-5%'  and a.component_alib_id=b.component_alib_id and a.parent_component_alib_id=b.parent_component_alib_id)")

