options (
  skip=1)
load into table FDM_TRANSPORTABLE_ITEM
  insert
  fields
 (
 TI_ID                          char terminated by ',' optionally enclosed by '"',
 SCL_CD                         char terminated by ',' optionally enclosed by '"',
 TI_NM                          char terminated by ',' optionally enclosed by '"',
 TI_CGO_CPCTY_QY                char terminated by ',' optionally enclosed by '"',
 TI_BLCK_BRC_SRC_ID             char terminated by ',' optionally enclosed by '"',
 TI_SUTBLTY_ID                  char terminated by ',' optionally enclosed by '"',
 JDL_RCRD_STAT_CD               char terminated by ',' optionally enclosed by '"',
 JDL_RCRD_CHNG_DT               date(20) "YYYY-MM-DD HH24:MI:SS" terminated by ',',
 JDL_RCRD_CHNGD_BY_CD           char terminated by ',' optionally enclosed by '"',
 LEAD_NSN                       char terminated by ',' optionally enclosed by '"',
 FUEL_BURNER                    char terminated by ',' optionally enclosed by '"'
 )
