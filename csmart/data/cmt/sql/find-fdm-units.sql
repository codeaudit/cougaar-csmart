select
    org.org_id
,   substr(org.uic,1,6) uic
--,   funit.unit_identifier
,   funit.geloc_cd
,   funit.un_abbrd_nm
,   funit.un_nm
from
    v7_lib_organization org
,   fdm_unit funit
where
    org.uic like 'W%'
and org.uic = funit.unit_identifier(+)
;
