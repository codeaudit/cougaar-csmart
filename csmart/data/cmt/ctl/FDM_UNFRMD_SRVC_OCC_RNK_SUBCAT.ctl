options (
  skip=1)
load into table FDM_UNFRMD_SRVC_OCC_RNK_SUBCAT
  insert
  fields
 (
 RANK_SUBCATEGORY_CODE          char terminated by ',' optionally enclosed by '"',
 RANK_SUBCATEGORY_TEXT          char terminated by ',' optionally enclosed by '"',
 JDL_RCRD_STAT_CD               char terminated by ',' optionally enclosed by '"',
 JDL_RCRD_CHNG_DT               date(20) "YYYY-MM-DD HH24:MI:SS" terminated by ',',
 JDL_RCRD_CHNGD_BY_CD           char terminated by ',' optionally enclosed by '"'
 )
