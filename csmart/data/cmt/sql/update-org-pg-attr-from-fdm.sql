--Clus+terPG|ClusterIdentifier
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select
    org.cfw_id as cfw_id
,   org.org_id as org_id
,   'ClusterPG|ClusterIdentifier' as pg_attribute_lib_id
,   org.org_id as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   v7_lib_organization liborg
where
    liborg.uic like 'W%'
and org.org_id = liborg.org_id
;
--
--ItemIdentificationPG|AlternateItemIdentification
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select
    org.cfw_id as cfw_id
,   org.org_id as org_id
,   'ItemIdentificationPG|AlternateItemIdentification' as pg_attribute_lib_id
,   'UIC/'||liborg.uic attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   v7_lib_organization liborg
where
    liborg.uic like 'W%'
and org.org_id = liborg.org_id
;
--
--ItemIdentificationPG|ItemIdentification
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select
    org.cfw_id as cfw_id
,   org.org_id as org_id
,   'ItemIdentificationPG|ItemIdentification' as pg_attribute_lib_id
,   org.org_id as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   v7_lib_organization liborg
where
    liborg.uic like 'W%'
and org.org_id = liborg.org_id
;
--
--ItemIdentificationPG|Nomenclature
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select
    org.cfw_id as cfw_id
,   org.org_id as org_id
,   'ItemIdentificationPG|Nomenclature' as pg_attribute_lib_id
,   decode(funit.un_nm,NULL,org.org_id,funit.un_nm) as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   fdm_unit funit
,   v7_lib_organization liborg
where
    liborg.uic like 'W%'
and liborg.uic = funit.unit_identifier(+)
and org.org_id = liborg.org_id
;
--
--MilitaryOrgPG|HomeLocation
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select
    org.cfw_id as cfw_id
,   org.org_id as org_id
,   'MilitaryOrgPG|HomeLocation' as pg_attribute_lib_id
,   'GeolocCode='||decode(funit.geloc_cd,NULL,'VDYD',funit.geloc_cd) as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   fdm_unit funit
,   v7_lib_organization liborg
where
    liborg.uic like 'W%'
and liborg.uic = funit.unit_identifier(+)
and org.org_id = liborg.org_id
;
--
--MilitaryOrgPG|IsReserve
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select
    org.cfw_id as cfw_id
,   org.org_id as org_id
,   'MilitaryOrgPG|IsReserve' as pg_attribute_lib_id
,   'FALSE' as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   v7_lib_organization liborg
where
    liborg.uic like 'W%'
and org.org_id = liborg.org_id
;
--
--MilitaryOrgPG|UIC
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select
    org.cfw_id as cfw_id
,   org.org_id as org_id
,   'MilitaryOrgPG|UIC' as pg_attribute_lib_id
,   liborg.uic as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   v7_lib_organization liborg
where
    liborg.uic like 'W%'
and org.org_id = liborg.org_id
;
--
--MilitaryOrgPG|UTC
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select
    org.cfw_id as cfw_id
,   org.org_id as org_id
,   'MilitaryOrgPG|UTC' as pg_attribute_lib_id
,   decode(funit.unt_cd,NULL,'XXXXX',funit.unt_cd) as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   fdm_unit funit
,   v7_lib_organization liborg
where
    liborg.uic like 'W%'
and liborg.uic = funit.unit_identifier(+)
and org.org_id = liborg.org_id
;
--
--OrganizationPG|Agency
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select
    org.cfw_id as cfw_id
,   org.org_id as org_id
,   'OrganizationPG|Agency' as pg_attribute_lib_id
,   'FORSCOM' as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   v7_lib_organization liborg
where
    liborg.uic like 'W%'
and org.org_id = liborg.org_id
;
--
--OrganizationPG|Service
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select
    org.cfw_id as cfw_id
,   org.org_id as org_id
,   'OrganizationPG|Service' as pg_attribute_lib_id
,   'ARMY' as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   v7_lib_organization liborg
where
    liborg.uic like 'W%'
and org.org_id = liborg.org_id
;
--
--TypeIdentificationPG|Nomenclature
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select
    org.cfw_id as cfw_id
,   org.org_id as org_id
,   'TypeIdentificationPG|Nomenclature' as pg_attribute_lib_id
,   decode(funitt.unt_nm,NULL,org.org_id,funitt.unt_nm) as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   fdm_unit funit
,   fdm_unit_type funitt
,   v7_lib_organization liborg
where
    liborg.uic like 'W%'
and liborg.uic = funit.unit_identifier(+)
and funit.unt_cd = funitt.unt_cd(+)
and org.org_id = liborg.org_id
;
--
--TypeIdentificationPG|TypeIdentification
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select
    org.cfw_id as cfw_id
,   org.org_id as org_id
,   'TypeIdentificationPG|TypeIdentification' as pg_attribute_lib_id
,   'UTC/'||decode(funit.unt_cd,NULL,'XXXXX',funit.unt_cd)as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   fdm_unit funit
,   v7_lib_organization liborg
where
    liborg.uic like 'W%'
and liborg.uic = funit.unit_identifier(+)
and org.org_id = liborg.org_id
;
--OrganizationPG|Roles
insert into v7_cfw_org_pg_attr (cfw_id, org_id, pg_attribute_lib_id, attribute_value, attribute_order, start_date)
--
select distinct
    org.cfw_id as cfw_id
,   org.org_id as org_id
,   'OrganizationPG|Roles' as pg_attribute_lib_id
,   orgrelate.role as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   v7_cfw_org_og_relation orgrelate
where
    org.org_id = orgrelate.org_id
;