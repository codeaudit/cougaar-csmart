options (
  skip=1)
load into table FDM_UNFRMD_SRVC_RNK
  insert
  fields
 (
 UNFRMD_SRVC_RNK_CD             char terminated by ',' optionally enclosed by '"',
 SVC_CD                         char terminated by ',' optionally enclosed by '"',
 PAY_GRD_CD                     char terminated by ',' optionally enclosed by '"',
 USR_SHRT_NM                    char terminated by ',' optionally enclosed by '"',
 USR_OFCR_IND_CD                char terminated by ',' optionally enclosed by '"',
 JDL_RCRD_STAT_CD               char terminated by ',' optionally enclosed by '"',
 JDL_RCRD_CHNG_DT               date(20) "YYYY-MM-DD HH24:MI:SS" terminated by ',',
 JDL_RCRD_CHNGD_BY_CD           char terminated by ',' optionally enclosed by '"'
 )
