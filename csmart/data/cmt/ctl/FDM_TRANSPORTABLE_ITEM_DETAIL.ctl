options (
  skip=1)
load into table FDM_TRANSPORTABLE_ITEM_DETAIL
  insert
  fields
 (
 TID_ID                         char terminated by ',' optionally enclosed by '"',
 TI_ID                          char terminated by ',' optionally enclosed by '"',
 SHPPNG_CNFGRTN_CD              char terminated by ',' optionally enclosed by '"',
 CGO_TP_CD                      char terminated by ',' optionally enclosed by '"',
 CGO_XTNT_CD                    char terminated by ',' optionally enclosed by '"',
 CGO_CNTZN_CD                   char terminated by ',' optionally enclosed by '"',
 MATERIEL_ITEM_IDENTIFIER       char terminated by ',' optionally enclosed by '"',
 TID_VLDTN_CD                   char terminated by ',' optionally enclosed by '"',
 TYPE_PACK_CODE                 char terminated by ',' optionally enclosed by '"',
 TID_RDBL_CD                    char terminated by ',' optionally enclosed by '"',
 FTY_CD                         char terminated by ',' optionally enclosed by '"',
 TID_PRFNC_CD                   char terminated by ',' optionally enclosed by '"',
 TID_LG_DM                      char terminated by ',' optionally enclosed by '"',
 TID_WDTH_DM                    char terminated by ',' optionally enclosed by '"',
 TID_EQ_TY_CD                   char terminated by ',' optionally enclosed by '"',
 TID_HT_DM                      char terminated by ',' optionally enclosed by '"',
 TID_WT                         char terminated by ',' optionally enclosed by '"',
 TID_VL                         char terminated by ',' optionally enclosed by '"',
 TID_CGO_CPCTY_QY               char terminated by ',' optionally enclosed by '"',
 TID_MAX_LDED_HT_DM             char terminated by ',' optionally enclosed by '"',
 TID_HVY_LFT_CD                 char terminated by ',' optionally enclosed by '"',
 TID_AR_CGO_LD_CD               char terminated by ',' optionally enclosed by '"',
 TID_CB_DM                      char terminated by ',' optionally enclosed by '"',
 TID_MN_GND_CLNC_DM             char terminated by ',' optionally enclosed by '"',
 TID_FTPRNT_AR                  char terminated by ',' optionally enclosed by '"',
 TID_PLZTN_CD                   char terminated by ',' optionally enclosed by '"',
 TID_CGO_CMPT_LG_DM             char terminated by ',' optionally enclosed by '"',
 TID_CGO_CMPT_WD_DM             char terminated by ',' optionally enclosed by '"',
 TID_CGO_CMPT_HT_DM             char terminated by ',' optionally enclosed by '"',
 TID_CGOCMPBD_HT_DM             char terminated by ',' optionally enclosed by '"',
 TID_CGO_BED_HT_DM              char terminated by ',' optionally enclosed by '"',
 TID_VNTG_CD                    char terminated by ',' optionally enclosed by '"',
 TID_WHL_BS_DM                  char terminated by ',' optionally enclosed by '"',
 TID_TLSCPD_LG_DM               char terminated by ',' optionally enclosed by '"',
 TID_EMTY_LD_CLS_ID             char terminated by ',' optionally enclosed by '"',
 TID_LDED_LD_CLS_ID             char terminated by ',' optionally enclosed by '"',
 TID_MDL_ID                     char terminated by ',' optionally enclosed by '"',
 TID_TRLR_TNG_LG_DM             char terminated by ',' optionally enclosed by '"',
 TID_CRAF_RQMT_TX               char terminated by ',' optionally enclosed by '"'
 )
