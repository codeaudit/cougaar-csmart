#
# Table structure for table 'alploc'
#

DROP TABLE IF EXISTS alploc;
CREATE TABLE alploc (
  alploc_code char(50) binary NOT NULL default '',
  location_name char(50) binary default NULL,
  latitude decimal(68,30) default NULL,
  longitude decimal(68,30) default NULL,
  installation_type_code char(3) binary default NULL
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/alploc.csv.tmp'
    INTO TABLE alploc
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ALPLOC_CODE,LOCATION_NAME,LATITUDE,LONGITUDE,INSTALLATION_TYPE_CODE);

#
# Table structure for table 'dual'
#

DROP TABLE IF EXISTS dual;
CREATE TABLE dual (
  DUMMY char(1) binary default NULL
) TYPE=MyISAM;

#
# Dumping data for table 'dual'
#

INSERT INTO dual (DUMMY) VALUES ('X');

DROP TABLE IF EXISTS fdm_unit;

CREATE TABLE fdm_unit (
  ORG_ID varchar(50) default NULL,
  UNIT_IDENTIFIER varchar(6) default NULL,
  MILITARY_ORGANIZATION_CODE varchar(18) default NULL,
  UL_CD char(3) default NULL,
  UN_ABBRD_NM varchar(30) default NULL,
  GELOC_CD varchar(4) default NULL,
  UNT_CD varchar(5) default NULL,
  UN_CMP_CD char(1) default NULL,
  UN_NM varchar(50) default NULL
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/fdm_unit.csv.tmp'
    INTO TABLE fdm_unit
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ORG_ID,UNIT_IDENTIFIER,MILITARY_ORGANIZATION_CODE,UL_CD,UN_ABBRD_NM,GELOC_CD,UNT_CD,UN_CMP_CD,UN_NM);


DROP TABLE IF EXISTS fdm_unit_equipment;

CREATE TABLE fdm_unit_equipment (
  ORG_ID varchar(50) default NULL,
  TI_ID varchar(6) default NULL,
  UNIT_IDENTIFIER varchar(6) default NULL,
  UNIT_EQUIPMENT_QTY decimal(38,0) default NULL,
  KEY UNIT_IDENTIFIER (UNIT_IDENTIFIER),
  KEY TI_ID (TI_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/fdm_unit_equipment.csv.tmp'
    INTO TABLE fdm_unit_equipment
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ORG_ID,TI_ID,UNIT_IDENTIFIER,UNIT_EQUIPMENT_QTY);


#
# Table structure for table 'fdm_transportable_item'
#

DROP TABLE IF EXISTS fdm_transportable_item;

CREATE TABLE fdm_transportable_item (
  TI_ID varchar(6) default NULL,
  SCL_CD char(2) default NULL,
  TI_NM varchar(255) default NULL,
  TI_CGO_CPCTY_QY decimal(126,0) default NULL,
  TI_BLCK_BRC_SRC_ID varchar(4) default NULL,
  FUEL_BURNER char(3) default NULL,
  KEY TI_ID (TI_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/fdm_transportable_item.csv.tmp'
    INTO TABLE fdm_transportable_item
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (TI_ID,SCL_CD,TI_NM,TI_CGO_CPCTY_QY,TI_BLCK_BRC_SRC_ID,FUEL_BURNER);

DROP TABLE IF EXISTS fdm_transportable_item_detail;

CREATE TABLE fdm_transportable_item_detail (
  TID_ID char(2) default NULL,
  TI_ID varchar(6) default NULL,
  SHPPNG_CNFGRTN_CD char(2) default NULL,
  CGO_TP_CD char(1) default NULL,
  CGO_XTNT_CD char(1) default NULL,
  CGO_CNTZN_CD char(1) default NULL,
  MATERIEL_ITEM_IDENTIFIER varchar(20) default NULL,
  TYPE_PACK_CODE varchar(18) default NULL,
  TID_RDBL_CD char(1) default NULL,
  TID_LG_DM decimal(126,0) default NULL,
  TID_WDTH_DM decimal(126,0) default NULL,
  TID_EQ_TY_CD char(1) default NULL,
  TID_HT_DM decimal(126,0) default NULL,
  TID_WT decimal(126,0) default NULL,
  TID_VL decimal(126,0) default NULL,
  TID_CGO_CPCTY_QY decimal(126,0) default NULL,
  TID_MAX_LDED_HT_DM decimal(126,0) default NULL,
  TID_HVY_LFT_CD char(1) default NULL,
  TID_AR_CGO_LD_CD char(1) default NULL,
  TID_CB_DM decimal(126,0) default NULL,
  TID_MN_GND_CLNC_DM decimal(126,0) default NULL,
  TID_FTPRNT_AR decimal(126,0) default NULL,
  TID_PLZTN_CD char(1) default NULL,
  TID_CGO_CMPT_LG_DM decimal(126,0) default NULL,
  TID_CGO_CMPT_WD_DM decimal(126,0) default NULL,
  TID_CGO_CMPT_HT_DM decimal(126,0) default NULL,
  TID_CGOCMPBD_HT_DM decimal(126,0) default NULL,
  TID_CGO_BED_HT_DM decimal(126,0) default NULL,
  TID_VNTG_CD char(1) default NULL,
  TID_WHL_BS_DM decimal(126,0) default NULL,
  TID_EMTY_LD_CLS_ID varchar(4) default NULL,
  TID_LDED_LD_CLS_ID varchar(4) default NULL,
  TID_MDL_ID varchar(14) default NULL,
  KEY TI_ID (TI_ID),
  KEY TID_ID (TID_ID),
  KEY CGP_TP_CD (CGO_TP_CD),
  KEY MATERIEL_ITEM_IDENTIFIER (MATERIEL_ITEM_IDENTIFIER)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/fdm_transportable_item_detail.csv.tmp'
    INTO TABLE fdm_transportable_item_detail
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (TID_ID,TI_ID,SHPPNG_CNFGRTN_CD,CGO_TP_CD,CGO_XTNT_CD,CGO_CNTZN_CD,MATERIEL_ITEM_IDENTIFIER,TYPE_PACK_CODE,TID_RDBL_CD,TID_LG_DM,TID_WDTH_DM,TID_EQ_TY_CD,TID_HT_DM,TID_WT,TID_VL,TID_CGO_CPCTY_QY,TID_MAX_LDED_HT_DM,TID_HVY_LFT_CD,TID_AR_CGO_LD_CD,TID_CB_DM,TID_MN_GND_CLNC_DM,TID_FTPRNT_AR,TID_PLZTN_CD,TID_CGO_CMPT_LG_DM,TID_CGO_CMPT_WD_DM,TID_CGO_CMPT_HT_DM,TID_CGOCMPBD_HT_DM,TID_CGO_BED_HT_DM,TID_WHL_BS_DM,TID_EMTY_LD_CLS_ID,TID_LDED_LD_CLS_ID,TID_MDL_ID);

DROP TABLE IF EXISTS fdm_unfrmd_srvc_occ_rnk_subcat;

CREATE TABLE fdm_unfrmd_srvc_occ_rnk_subcat (
  RANK_SUBCATEGORY_CODE varchar(10) default NULL,
  RANK_SUBCATEGORY_TEXT varchar(30) default NULL
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/fdm_unfrmd_srvc_occ_rnk_subcat.csv.tmp'
    INTO TABLE fdm_unfrmd_srvc_occ_rnk_subcat
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (RANK_SUBCATEGORY_CODE,RANK_SUBCATEGORY_TEXT);

DROP TABLE IF EXISTS fdm_unfrmd_srvc_occptn;

CREATE TABLE fdm_unfrmd_srvc_occptn (
  UNFRMD_SRVC_OCCPTN_CD varchar(8) default NULL,
  SVC_CD char(1) default NULL,
  RANK_SUBCATEGORY_CODE varchar(10) default NULL,
  UNFRMD_SRVC_OCCPTN_TX varchar(120) default NULL,
  KEY UNFRMD_SRVC_OCCPTN_CD (UNFRMD_SRVC_OCCPTN_CD),
  KEY RANK_SUBCATEGORY_CODE (RANK_SUBCATEGORY_CODE)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/fdm_unfrmd_srvc_occptn.csv.tmp'
    INTO TABLE fdm_unfrmd_srvc_occptn
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (UNFRMD_SRVC_OCCPTN_CD,SVC_CD,RANK_SUBCATEGORY_CODE,UNFRMD_SRVC_OCCPTN_TX);

DROP TABLE IF EXISTS fdm_unfrmd_srvc_rnk;

CREATE TABLE fdm_unfrmd_srvc_rnk (
  UNFRMD_SRVC_RNK_CD char(2) default NULL,
  SVC_CD char(1) default NULL,
  PAY_GRD_CD varchar(4) default NULL,
  USR_SHRT_NM varchar(6) default NULL,
  USR_OFCR_IND_CD char(1) default NULL
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/fdm_unfrmd_srvc_rnk.csv.tmp'
    INTO TABLE fdm_unfrmd_srvc_rnk
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (UNFRMD_SRVC_RNK_CD,SVC_CD,PAY_GRD_CD,USR_SHRT_NM,USR_OFCR_IND_CD);

DROP TABLE IF EXISTS fdm_unit_billet;

CREATE TABLE fdm_unit_billet (
  ORG_ID varchar(50) default NULL,
  UNIT_IDENTIFIER varchar(6) default NULL,
  BILLET_ID varchar(18) default NULL,
  SVC_CD char(1) default NULL,
  UNFRMD_SRVC_RNK_CD char(2) default NULL,
  UNFRMD_SRVC_OCCPTN_CD varchar(8) default NULL,
  REQ_SEI_CD varchar(18) default NULL,
  UNT_SVRC_CMPNT_CD char(1) default NULL,
  TO_NUMBER varchar(6) default NULL,
  TO_SUFFIX char(1) default NULL,
  TO_LINE_NUMBER varchar(5) default NULL,
  TO_RANK varchar(6) default NULL,
  TO_STRENGTH decimal(38,0) default NULL,
  KEY UNFRMD_SRVC_OCCPTN_CD (UNFRMD_SRVC_OCCPTN_CD),
  KEY UNIT_UDENTIFIER (UNIT_IDENTIFIER)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/fdm_unit_billet.csv.tmp'
    INTO TABLE fdm_unit_billet
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ORG_ID,UNIT_IDENTIFIER,BILLET_ID,SVC_CD,UNFRMD_SRVC_RNK_CD,UNFRMD_SRVC_OCCPTN_CD,REQ_SEI_CD,UNT_SVRC_CMPNT_CD,TO_NUMBER,TO_SUFFIX,TO_LINE_NUMBER,TO_RANK,TO_STRENGTH);

DROP TABLE IF EXISTS geoloc;

CREATE TABLE geoloc (
  GEOLOC_CODE char(4) NOT NULL default '',
  LOCATION_NAME char(17) default NULL,
  INSTALLATION_TYPE_CODE char(3) default NULL,
  COUNTRY_STATE_CODE char(2) default NULL,
  COUNTRY_STATE_SHORT_NAME char(5) default NULL,
  COUNTRY_STATE_LONG_NAME char(15) default NULL,
  PROVINCE_CODE char(3) default NULL,
  PROVINCE_NAME char(14) default NULL,
  LATITUDE decimal(6,4) NOT NULL default '0.0000',
  LONGITUDE decimal(7,4) NOT NULL default '0.0000',
  LOGISTIC_PLANNING_CODE char(2) default NULL,
  PRIME_GEOLOC_CODE char(4) default NULL,
  RECORD_OWNER_UIC char(6) default NULL,
  CIVIL_AVIATION_CODE char(4) default NULL,
  GSA_STATE_CODE char(2) default NULL,
  GSA_CITY_CODE char(4) default NULL,
  GSA_COUNTY_CODE char(3) default NULL,
  PRIMARY KEY  (GEOLOC_CODE),
  KEY IX_GEOLOC_NAME (LOCATION_NAME),
  KEY IX_GEOLOC_LAT_LONG (LATITUDE,LONGITUDE),
  KEY IX_GEOLOC_CIVIL_AVIATION_CODE (CIVIL_AVIATION_CODE)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/geoloc.csv.tmp'
    INTO TABLE geoloc 
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (GEOLOC_CODE,LOCATION_NAME,INSTALLATION_TYPE_CODE,COUNTRY_STATE_CODE,COUNTRY_STATE_SHORT_NAME,COUNTRY_STATE_LONG_NAME,PROVINCE_CODE,PROVINCE_NAME,LATITUDE,LONGITUDE,LOGISTIC_PLANNING_CODE,PRIME_GEOLOC_CODE,RECORD_OWNER_UIC,CIVIL_AVIATION_CODE,GSA_STATE_CODE,GSA_CITY_CODE,GSA_COUNTY_CODE);

#
# Table structure for table 'lib_organization'
#

DROP TABLE IF EXISTS lib_organization;
CREATE TABLE lib_organization (
  ORG_ID varchar(50) binary NOT NULL default '',
  ORG_NAME varchar(50) binary default NULL,
  UIC varchar(50) binary default NULL,
  ORG_CLASS varchar(50) binary default NULL,
  UNIQUE KEY pk_lib_organization (ORG_ID),
  KEY reflib_org45 (ORG_NAME),
  KEY reflib_org46 (ORG_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/lib_organization.csv.tmp'
    INTO TABLE lib_organization
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ORG_ID,ORG_NAME,UIC,ORG_CLASS);

#
# Table structure for table 'lib_pg_attribute'
#

DROP TABLE IF EXISTS lib_pg_attribute;
CREATE TABLE lib_pg_attribute (
  PG_ATTRIBUTE_LIB_ID varchar(100) binary NOT NULL default '',
  PG_NAME varchar(50) binary default NULL,
  ATTRIBUTE_NAME varchar(50) binary default NULL,
  ATTRIBUTE_TYPE varchar(50) binary default NULL,
  AGGREGATE_TYPE varchar(50) binary default NULL,
  UNIQUE KEY pk_lib_pg_attribute (PG_ATTRIBUTE_LIB_ID)
) TYPE=MyISAM;


LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/lib_pg_attribute.csv.tmp'
    INTO TABLE lib_pg_attribute
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (PG_ATTRIBUTE_LIB_ID,PG_NAME,ATTRIBUTE_NAME,ATTRIBUTE_TYPE,AGGREGATE_TYPE);

#
# Table structure for table 'oplan'
#

DROP TABLE IF EXISTS lib_oplan;
DROP TABLE IF EXISTS oplan;
CREATE TABLE oplan (
  oplan_id char(50) binary NOT NULL default '',
  operation_name char(50) binary default NULL,
  priority char(50) binary default NULL,
  min_planning_offset    decimal(68,30)       default NULL,
  start_offset    decimal(68,30)         default NULL,
  end_offset    decimal(68,30)           default NULL
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/oplan.csv.tmp'
    INTO TABLE oplan
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (OPLAN_ID,OPERATION_NAME,PRIORITY,MIN_PLANNING_OFFSET,START_OFFSET,END_OFFSET);
#
# Table structure for table 'oplan_agent_attr'
#

DROP TABLE IF EXISTS oplan_agent_attr;
CREATE TABLE oplan_agent_attr (
  oplan_id char(50) binary NOT NULL default '',
  org_id char(150) binary NOT NULL default '',
  component_id char(50) binary NOT NULL default '',
  start_cday decimal(68,30) NOT NULL default '0.000000000000000000000000000000',
  attribute_name char(50) binary NOT NULL default '',
  end_cday decimal(68,30) default NULL,
  attribute_value char(50) binary default NULL
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/oplan_agent_attr.csv.tmp'
    INTO TABLE oplan_agent_attr
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (OPLAN_ID,ORG_ID,COMPONENT_ID,START_CDAY,ATTRIBUTE_NAME,END_CDAY,ATTRIBUTE_VALUE);
#
# Table structure for table 'org_pg_attr'
#

DROP TABLE IF EXISTS org_pg_attr;
CREATE TABLE org_pg_attr (
  ORG_ID varchar(50) binary NOT NULL default '',
  PG_ATTRIBUTE_LIB_ID varchar(100) binary NOT NULL default '',
  ATTRIBUTE_VALUE varchar(50) binary NOT NULL default '',
  ATTRIBUTE_ORDER decimal(68,30) NOT NULL default '0.000000000000000000000000000000',
  START_DATE datetime NOT NULL default '0000-00-00 00:00:00',
  END_DATE datetime default NULL,
  UNIQUE KEY pk_org_pg_attr (ORG_ID,PG_ATTRIBUTE_LIB_ID,ATTRIBUTE_VALUE,ATTRIBUTE_ORDER,START_DATE),
  KEY ref_org_list37 (ORG_ID),
  KEY ref_org_list38 (PG_ATTRIBUTE_LIB_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/org_pg_attr.csv.tmp'
    INTO TABLE org_pg_attr
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ORG_ID,PG_ATTRIBUTE_LIB_ID,ATTRIBUTE_VALUE,ATTRIBUTE_ORDER,START_DATE,END_DATE);

#
# Table structure for table 'org_relation'
#

DROP TABLE IF EXISTS org_relation;
CREATE TABLE org_relation (
  ROLE varchar(50) binary NOT NULL default '',
  SUPPORTING_ORG_ID varchar(150) binary NOT NULL default '',
  SUPPORTED_ORG_ID varchar(150) binary NOT NULL default '',
  START_DATE datetime NOT NULL default '0000-00-00 00:00:00',
  END_DATE datetime default NULL,
  UNIQUE KEY pk_org_relation (ROLE,SUPPORTING_ORG_ID,SUPPORTED_ORG_ID,START_DATE),
  KEY reforg_rel31 (SUPPORTING_ORG_ID),
  KEY reforg_rel32 (SUPPORTED_ORG_ID)
) TYPE=MyISAM;

LOAD DATA INFILE ':cip/csmart/data/database/ref-csv/org_relation.csv.tmp'
    INTO TABLE org_relation
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ROLE,SUPPORTING_ORG_ID,SUPPORTED_ORG_ID,START_DATE,END_DATE);
