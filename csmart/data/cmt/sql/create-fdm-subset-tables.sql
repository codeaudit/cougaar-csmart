create table FDM_UNIT(
 ORG_ID                                                         VARCHAR2(50),
 UNIT_IDENTIFIER                                                CHAR(6),
 MILITARY_ORGANIZATION_CODE                                     VARCHAR2(18),
 UL_CD                                                          VARCHAR2(3),
 UN_ABBRD_NM                                                    VARCHAR2(30),
 GELOC_CD                                                       VARCHAR2(4),
 UNT_CD                                                         VARCHAR2(5),
 UN_ADDR_TX                                                     VARCHAR2(60),
 UN_CMP_CD                                                      VARCHAR2(1),
 UN_CND_CD                                                      VARCHAR2(1),
 CRNT_UN_ACTY_CD                                                VARCHAR2(2),
 UN_DEFCON_CD                                                   VARCHAR2(1),
 UN_EFCTNS_CD                                                   VARCHAR2(1),
 MAJ_UN_ICD                                                     VARCHAR2(1),
 UN_CQY009                                                      NUMBER(38),
 UN_MVT_STAT_ICD                                                VARCHAR2(1),
 UN_NM                                                          VARCHAR2(50),
 UN_NUCWPN_ICD                                                  VARCHAR2(1),
 UN_ICD011                                                      VARCHAR2(1),
 UN_ICD007                                                      VARCHAR2(1),
 UN_ROLE_CD                                                     VARCHAR2(1),
 UN_SCTY_CLSN_CD                                                VARCHAR2(1),
 SH_EMBRKD_UN_ICD                                               VARCHAR2(1),
 UN_SUBORD_RPTG_ICD                                             VARCHAR2(1),
 UN_TRP_PGM_ID                                                  VARCHAR2(7),
 ORDINATE_UNIT_IDENTIFIER                                       VARCHAR2(18),
 UT_SVC_CMPNT_CD                                                CHAR(1),
 DODAAC_ID                                                      VARCHAR2(6),
 JDL_RCRD_STAT_CD                                               CHAR(1),
 JDL_RCRD_CHNG_DT                                               DATE,
 JDL_RCRD_CHNGD_BY_CD                                           CHAR(3)
)
;

create table FDM_UNIT_EQUIPMENT(
 ORG_ID                                                         VARCHAR2(50),
 TI_ID                                                          VARCHAR2(6),
 UNIT_IDENTIFIER                                                CHAR(6),
 UNIT_EQUIPMENT_QTY                                             NUMBER(38),
 JDL_RCRD_STAT_CD                                               CHAR(1),
 JDL_RCRD_CHNG_DT                                               DATE,
 JDL_RCRD_CHNGD_BY_CD                                           CHAR(3)
)
;

create table FDM_TRANSPORTABLE_ITEM(
 TI_ID                                                          VARCHAR2(6),
 SCL_CD                                                         VARCHAR2(2),
 TI_NM                                                          VARCHAR2(255),
 TI_CGO_CPCTY_QY                                                FLOAT(126),
 TI_BLCK_BRC_SRC_ID                                             VARCHAR2(4),
 TI_SUTBLTY_ID                                                  VARCHAR2(1),
 JDL_RCRD_STAT_CD                                               CHAR(1),
 JDL_RCRD_CHNG_DT                                               DATE,
 JDL_RCRD_CHNGD_BY_CD                                           CHAR(3),
 LEAD_NSN                                                       VARCHAR2(20),
 FUEL_BURNER                                                    VARCHAR2(3)
)
;

create table FDM_TRANSPORTABLE_ITEM_DETAIL(
 TID_ID                                                         VARCHAR2(2),
 TI_ID                                                          VARCHAR2(6),
 SHPPNG_CNFGRTN_CD                                              VARCHAR2(2),
 CGO_TP_CD                                                      VARCHAR2(1),
 CGO_XTNT_CD                                                    VARCHAR2(1),
 CGO_CNTZN_CD                                                   VARCHAR2(1),
 MATERIEL_ITEM_IDENTIFIER                                       VARCHAR2(20),
 TID_VLDTN_CD                                                   VARCHAR2(1),
 TYPE_PACK_CODE                                                 VARCHAR2(18),
 TID_RDBL_CD                                                    VARCHAR2(1),
 FTY_CD                                                         VARCHAR2(3),
 TID_PRFNC_CD                                                   VARCHAR2(1),
 TID_LG_DM                                                      FLOAT(126),
 TID_WDTH_DM                                                    FLOAT(126),
 TID_EQ_TY_CD                                                   VARCHAR2(1),
 TID_HT_DM                                                      FLOAT(126),
 TID_WT                                                         FLOAT(126),
 TID_VL                                                         FLOAT(126),
 TID_CGO_CPCTY_QY                                               FLOAT(126),
 TID_MAX_LDED_HT_DM                                             FLOAT(126),
 TID_HVY_LFT_CD                                                 VARCHAR2(1),
 TID_AR_CGO_LD_CD                                               VARCHAR2(1),
 TID_CB_DM                                                      FLOAT(126),
 TID_MN_GND_CLNC_DM                                             FLOAT(126),
 TID_FTPRNT_AR                                                  FLOAT(126),
 TID_PLZTN_CD                                                   VARCHAR2(1),
 TID_CGO_CMPT_LG_DM                                             FLOAT(126),
 TID_CGO_CMPT_WD_DM                                             FLOAT(126),
 TID_CGO_CMPT_HT_DM                                             FLOAT(126),
 TID_CGOCMPBD_HT_DM                                             FLOAT(126),
 TID_CGO_BED_HT_DM                                              FLOAT(126),
 TID_VNTG_CD                                                    VARCHAR2(1),
 TID_WHL_BS_DM                                                  FLOAT(126),
 TID_TLSCPD_LG_DM                                               FLOAT(126),
 TID_EMTY_LD_CLS_ID                                             VARCHAR2(4),
 TID_LDED_LD_CLS_ID                                             VARCHAR2(4),
 TID_MDL_ID                                                     VARCHAR2(14),
 TID_TRLR_TNG_LG_DM                                             FLOAT(126),
 TID_CRAF_RQMT_TX                                               VARCHAR2(255)
)
;

create table FDM_UNIT_BILLET(
 ORG_ID                                                         VARCHAR2(50),
 UNIT_IDENTIFIER                                                CHAR(6),
 BILLET_ID                                                      VARCHAR2(18),
 SVC_CD                                                         CHAR(1),
 SCRTY_CLSFCTN_CD                                               VARCHAR2(2),
 UNFRMD_SRVC_RNK_CD                                             VARCHAR2(2),
 UNIT_BILLET_WEAPON_TYPE_CODE                                   CHAR(1),
 BILLET_DSCN_TX                                                 VARCHAR2(60),
 UNFRMD_SRVC_OCCPTN_CD                                          VARCHAR2(8),
 PER_RP_ASG_ST_CD                                               VARCHAR2(18),
 RANK_SUBCATEGORY_CODE                                          VARCHAR2(10),
 REQ_SEI_CD                                                     VARCHAR2(18),
 UNT_SVRC_CMPNT_CD                                              VARCHAR2(1),
 MAJCOM_ASSIGN_AREA_2                                           VARCHAR2(4),
 TO_NUMBER                                                      VARCHAR2(6),
 TO_SUFFIX                                                      VARCHAR2(1),
 TO_LINE_NUMBER                                                 VARCHAR2(5),
 TO_RANK                                                        VARCHAR2(6),
 BILLET_MOS                                                     VARCHAR2(7),
 TO_STRENGTH                                                    NUMBER(38),
 JDL_RCRD_STAT_CD                                               CHAR(1),
 JDL_RCRD_CHNG_DT                                               DATE,
 JDL_RCRD_CHNGD_BY_CD                                           CHAR(3)
)
;

create table FDM_UNFRMD_SRVC_OCCPTN(
 UNFRMD_SRVC_OCCPTN_CD                                          VARCHAR2(8),
 SVC_CD                                                         CHAR(1),
 RANK_SUBCATEGORY_CODE                                          VARCHAR2(10),
 UNFRMD_SRVC_OCCPTN_TX                                          CHAR(120),
 JDL_RCRD_STAT_CD                                               CHAR(1),
 JDL_RCRD_CHNG_DT                                               DATE,
 JDL_RCRD_CHNGD_BY_CD                                           CHAR(3)
)
;

create table FDM_UNFRMD_SRVC_RNK(
 UNFRMD_SRVC_RNK_CD                                             VARCHAR2(2),
 SVC_CD                                                         CHAR(1),
 PAY_GRD_CD                                                     VARCHAR2(4),
 USR_SHRT_NM                                                    VARCHAR2(6),
 USR_OFCR_IND_CD                                                VARCHAR2(1),
 JDL_RCRD_STAT_CD                                               CHAR(1),
 JDL_RCRD_CHNG_DT                                               DATE,
 JDL_RCRD_CHNGD_BY_CD                                           CHAR(3)
)
;

create table FDM_UNFRMD_SRVC_OCC_RNK_SUBCAT(
 RANK_SUBCATEGORY_CODE                                          VARCHAR2(10),
 RANK_SUBCATEGORY_TEXT                                          VARCHAR2(30),
 JDL_RCRD_STAT_CD                                               CHAR(1),
 JDL_RCRD_CHNG_DT                                               DATE,
 JDL_RCRD_CHNGD_BY_CD                                           CHAR(3)
)
;
