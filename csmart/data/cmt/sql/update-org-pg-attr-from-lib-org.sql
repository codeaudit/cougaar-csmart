--ClusterPG|ClusterIdentifier
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
    liborg.uic is NULL
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
    liborg.uic is NULL
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
    liborg.uic is NULL
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
,   org.org_id as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   v7_lib_organization liborg
where
    liborg.uic is NULL
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
,   'GeolocCode='||'VDYD' as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   v7_lib_organization liborg
where
    liborg.uic is NULL
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
    liborg.uic is NULL
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
,   'XXXXXX' as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   v7_lib_organization liborg
where
    liborg.uic is NULL
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
,   'XXXXX' as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   v7_lib_organization liborg
where
    liborg.uic is NULL
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
    liborg.uic is NULL
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
,   'JOINT' as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   v7_lib_organization liborg
where
    liborg.uic is NULL
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
,   org.org_id as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   v7_lib_organization liborg
where
    liborg.uic is NULL
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
,   'UTC/'||org.org_id as attribute_value
,   0 as attribute_order
,   '2000-01-01 00:00:00' as start_date
from
    v7_cfw_org_list org
,   v7_lib_organization liborg
where
    liborg.uic is NULL
and org.org_id = liborg.org_id
;
