select
    substr(org.org_id,1,20) org_id
,   substr(org.uic,1,6) uic
-- ,   fue.unit_identifier
,   fue.unit_equipment_qty
,   fue.ti_id
,   fted.tid_id
,   fted.shppng_cnfgrtn_cd
,   substr(fte.ti_nm,1,30) ti_nm
,   fted.cgo_tp_cd
,   fted.cgo_xtnt_cd
,   fted.cgo_cntzn_cd
,   fted.materiel_item_identifier
,   fted.type_pack_code
,   fted.tid_eq_ty_cd
from
    v7_lib_organization org
,   fdm_unit_equipment fue
,   fdm_transportable_item fte
,   fdm_transportable_item_detail fted
where
    org.uic like 'W%'
and org.uic = fue.unit_identifier
and fue.ti_id = fte.ti_id
and fue.ti_id = fted.ti_id
and org.org_id = '3-133-FABN-155'
-- and org.org_id like '%FABN'
-- and fue.ti_id = 'T13168'
-- and fue.ti_id = 'H57642'
-- and fue.ti_id = 'K57667'
and fted.tid_id = '01'
and fted.cgo_tp_cd in ('A', 'B', 'C', 'K', 'R') -- Vehicles or NSDA
order by
    org.org_id
,   fue.unit_equipment_qty asc
;
