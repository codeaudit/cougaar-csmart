options (
  skip=1)
load into table FDM_UNIT_EQUIPMENT
  insert
  fields
 (
 ORG_ID                         char terminated by ',' optionally enclosed by '"',
 TI_ID                          char terminated by ',' optionally enclosed by '"',
 UNIT_IDENTIFIER                char terminated by ',' optionally enclosed by '"',
 UNIT_EQUIPMENT_QTY             char terminated by ',' optionally enclosed by '"',
 JDL_RCRD_STAT_CD               char terminated by ',' optionally enclosed by '"',
 JDL_RCRD_CHNG_DT               date(20) "YYYY-MM-DD HH24:MI:SS" terminated by ',',
 JDL_RCRD_CHNGD_BY_CD           char terminated by ',' optionally enclosed by '"'
 )
