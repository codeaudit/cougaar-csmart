options (
  skip=1)
load into table FDM_UNIT_BILLET
  insert
  fields
 (
 ORG_ID                         char terminated by ',' optionally enclosed by '"',
 UNIT_IDENTIFIER                char terminated by ',' optionally enclosed by '"',
 BILLET_ID                      char terminated by ',' optionally enclosed by '"',
 SVC_CD                         char terminated by ',' optionally enclosed by '"',
 SCRTY_CLSFCTN_CD               char terminated by ',' optionally enclosed by '"',
 UNFRMD_SRVC_RNK_CD             char terminated by ',' optionally enclosed by '"',
 UNIT_BILLET_WEAPON_TYPE_CODE   char terminated by ',' optionally enclosed by '"',
 BILLET_DSCN_TX                 char terminated by ',' optionally enclosed by '"',
 UNFRMD_SRVC_OCCPTN_CD          char terminated by ',' optionally enclosed by '"',
 PER_RP_ASG_ST_CD               char terminated by ',' optionally enclosed by '"',
 RANK_SUBCATEGORY_CODE          char terminated by ',' optionally enclosed by '"',
 REQ_SEI_CD                     char terminated by ',' optionally enclosed by '"',
 UNT_SVRC_CMPNT_CD              char terminated by ',' optionally enclosed by '"',
 MAJCOM_ASSIGN_AREA_2           char terminated by ',' optionally enclosed by '"',
 TO_NUMBER                      char terminated by ',' optionally enclosed by '"',
 TO_SUFFIX                      char terminated by ',' optionally enclosed by '"',
 TO_LINE_NUMBER                 char terminated by ',' optionally enclosed by '"',
 TO_RANK                        char terminated by ',' optionally enclosed by '"',
 BILLET_MOS                     char terminated by ',' optionally enclosed by '"',
 TO_STRENGTH                    char terminated by ',' optionally enclosed by '"',
 JDL_RCRD_STAT_CD               char terminated by ',' optionally enclosed by '"',
 JDL_RCRD_CHNG_DT               date(20) "YYYY-MM-DD HH24:MI:SS" terminated by ',',
 JDL_RCRD_CHNGD_BY_CD           char terminated by ',' optionally enclosed by '"'
 )
