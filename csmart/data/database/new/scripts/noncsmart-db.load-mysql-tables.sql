LOAD DATA INFILE ':cip/csmart/data/database/new/csv/org_pg_attr.csv.tmp'
    INTO TABLE org_pg_attr
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ORG_ID,PG_ATTRIBUTE_LIB_ID,ATTRIBUTE_VALUE,ATTRIBUTE_ORDER,START_DATE,END_DATE);

LOAD DATA INFILE ':cip/csmart/data/database/new/csv/org_relation.csv.tmp'
    INTO TABLE org_relation
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ROLE,SUPPORTING_ORG_ID,SUPPORTED_ORG_ID,START_DATE,END_DATE);

LOAD DATA INFILE ':cip/csmart/data/database/new/csv/lib_organization.csv.tmp'
    INTO TABLE lib_organization
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ORG_ID,ORG_NAME,UIC,ORG_CLASS);

LOAD DATA INFILE ':cip/csmart/data/database/new/csv/lib_pg_attribute.csv.tmp'
    INTO TABLE lib_pg_attribute
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (PG_ATTRIBUTE_LIB_ID,PG_NAME,ATTRIBUTE_NAME,ATTRIBUTE_TYPE,AGGREGATE_TYPE);

LOAD DATA INFILE ':cip/csmart/data/database/new/csv/lib_oplan.csv.tmp'
    INTO TABLE lib_oplan
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (OPLAN_ID,OPERATION_NAME,PRIORITY,C0_DATE);

LOAD DATA INFILE ':cip/csmart/data/database/new/csv/oplan_agent_attr.csv.tmp'
    INTO TABLE oplan_agent_attr
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (OPLAN_ID,ORG_ID,COMPONENT_ID,START_CDAY,ATTRIBUTE_NAME,END_CDAY,ATTRIBUTE_VALUE);

LOAD DATA INFILE ':cip/csmart/data/database/new/csv/alploc.csv.tmp'
    INTO TABLE alploc
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (ALPLOC_CODE,LOCATION_NAME,LATITUDE,LONGITUDE,INSTALLATION_TYPE_CODE);

