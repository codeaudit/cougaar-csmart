options (
  skip=1)
load into table FDM_UNIT
  insert
  fields
 (
 ORG_ID                         char terminated by ',' optionally enclosed by '"',
 UNIT_IDENTIFIER                char terminated by ',' optionally enclosed by '"',
 MILITARY_ORGANIZATION_CODE     char terminated by ',' optionally enclosed by '"',
 UL_CD                          char terminated by ',' optionally enclosed by '"',
 UN_ABBRD_NM                    char terminated by ',' optionally enclosed by '"',
 GELOC_CD                       char terminated by ',' optionally enclosed by '"',
 UNT_CD                         char terminated by ',' optionally enclosed by '"',
 UN_ADDR_TX                     char terminated by ',' optionally enclosed by '"',
 UN_CMP_CD                      char terminated by ',' optionally enclosed by '"',
 UN_CND_CD                      char terminated by ',' optionally enclosed by '"',
 CRNT_UN_ACTY_CD                char terminated by ',' optionally enclosed by '"',
 UN_DEFCON_CD                   char terminated by ',' optionally enclosed by '"',
 UN_EFCTNS_CD                   char terminated by ',' optionally enclosed by '"',
 MAJ_UN_ICD                     char terminated by ',' optionally enclosed by '"',
 UN_CQY009                      char terminated by ',' optionally enclosed by '"',
 UN_MVT_STAT_ICD                char terminated by ',' optionally enclosed by '"',
 UN_NM                          char terminated by ',' optionally enclosed by '"',
 UN_NUCWPN_ICD                  char terminated by ',' optionally enclosed by '"',
 UN_ICD011                      char terminated by ',' optionally enclosed by '"',
 UN_ICD007                      char terminated by ',' optionally enclosed by '"',
 UN_ROLE_CD                     char terminated by ',' optionally enclosed by '"',
 UN_SCTY_CLSN_CD                char terminated by ',' optionally enclosed by '"',
 SH_EMBRKD_UN_ICD               char terminated by ',' optionally enclosed by '"',
 UN_SUBORD_RPTG_ICD             char terminated by ',' optionally enclosed by '"',
 UN_TRP_PGM_ID                  char terminated by ',' optionally enclosed by '"',
 ORDINATE_UNIT_IDENTIFIER       char terminated by ',' optionally enclosed by '"',
 UT_SVC_CMPNT_CD                char terminated by ',' optionally enclosed by '"',
 DODAAC_ID                      char terminated by ',' optionally enclosed by '"',
 JDL_RCRD_STAT_CD               char terminated by ',' optionally enclosed by '"',
 JDL_RCRD_CHNG_DT               date(20) "YYYY-MM-DD HH24:MI:SS" terminated by ',',
 JDL_RCRD_CHNGD_BY_CD           char terminated by ',' optionally enclosed by '"'
 )
