-- LOCATION for all OGs
--insert into v7_cfw_oplan_og_attr (cfw_id, oplan_id, org_group_id, start_cday, attribute_name, end_cday, attribute_value)
--
select
       '1AD-CFW' as cfw_id
,      '093FF' as oplan_id
,      substr(org.org_id,1,20) as org_id
,      substr(loc.org_group_id,1,20) as org_group_id
,      loc.start_cday as start_cday
,      'LOCATION' as ATTRIBUTE_NAME
,      loc.end_cday as end_cday
,      loc.location_code as attribute_value
from 
        v7_oplan_loc loc
,       v7_lib_organization org
,       v7_cfw_org_group_org_member orgmem
where
        1=1
and     org.uic like 'W%'
and     orgmem.org_id = org.org_id
and     loc.org_group_id = orgmem.org_group_id
order by
        substr(loc.org_group_id,1,20)
,       substr(org.org_id,1,20)
,       loc.start_cday
;
-- ACTIVITY_TYPE for all OGs
--insert into v7_cfw_oplan_og_attr (cfw_id, oplan_id, org_group_id, start_cday, attribute_name, end_cday, attribute_value)
--
select
       '1AD-CFW' as cfw_id
,      '093FF' as oplan_id
,      substr(org.org_id,1,20) as org_id
,      substr(act.org_group_id,1,20) as org_group_id
,      act.start_cday as start_cday
,      'ACTIVITY_TYPE' as ATTRIBUTE_NAME
,      act.end_cday as end_cday
,      act.activity_type as attribute_value
from 
        v7_oplan_activity act
,       v7_lib_organization org
,       v7_cfw_org_group_org_member orgmem
where
        1=1
and     org.uic like 'W%'
and     orgmem.org_id = org.org_id
and     act.org_group_id = orgmem.org_group_id
order by
        substr(act.org_group_id,1,20)
,       substr(org.org_id,1,20)
,       act.start_cday
;
-- OPTEMPO for all OGs
--insert into v7_cfw_oplan_og_attr (cfw_id, oplan_id, org_group_id, start_cday, attribute_name, end_cday, attribute_value)
--
select
       '1AD-CFW' as cfw_id
,      '093FF' as oplan_id
,      substr(org.org_id,1,20) as org_id
,      substr(act.org_group_id,1,20) as org_group_id
,      act.start_cday as start_cday
,      'OPTEMPO' as ATTRIBUTE_NAME
,      act.end_cday as end_cday
,      act.optempo as attribute_value
from 
        v7_oplan_activity act
,       v7_lib_organization org
,       v7_cfw_org_group_org_member orgmem
where
        1=1
and     org.uic like 'W%'
and     orgmem.org_id = org.org_id
and     act.org_group_id = orgmem.org_group_id
order by
        substr(act.org_group_id,1,20)
,       substr(org.org_id,1,20)
,       act.start_cday
;
