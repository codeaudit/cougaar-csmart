--ItemIdentificationPG|AlternateItemIdentification
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select
   '1ad-cfw' as cfw_id
,   substr(org.org_id,1,20) as org_id
,   'ItemIdentificationPG|AlternateItemIdentification' as pg_attribute_lib_id
,   'UIC/'||substr(org.uic,1,6) attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_lib_organization org
where
    org.uic like 'W%'
;
--
--ItemIdentificationPG|Nomenclature
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select
   '1ad-cfw' as cfw_id
,   substr(org.org_id,1,20) as org_id
,   'ItemIdentificationPG|Nomenclature' as pg_attribute_lib_id
,   decode(funit.un_nm,NULL,org_id,funit.un_nm) as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_lib_organization org
,   fdm_unit funit
where
    org.uic like 'W%'
and org.uic = funit.unit_identifier(+)
;
--
--TypeIdentificationPG|Nomenclature
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select
   '1ad-cfw' as cfw_id
,   substr(org.org_id,1,20) as org_id
,   'TypeIdentificationPG|Nomenclature' as pg_attribute_lib_id
,   decode(funitt.unt_nm,NULL,org_id,funitt.unt_nm) as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_lib_organization org
,   fdm_unit funit
,   fdm_unit_type funitt
where
    org.uic like 'W%'
and org.uic = funit.unit_identifier(+)
and funit.unt_cd = funitt.unt_cd(+)
;
