-- The below commands create each given file with no headings

-- lib_pg_attribute
SELECT PG_ATTRIBUTE_LIB_ID,PG_NAME,ATTRIBUTE_NAME,ATTRIBUTE_TYPE,AGGREGATE_TYPE 
INTO OUTFILE ":cip/csmart/data/database/new/raw_data/lib_pg_attribute_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY ",\n" FROM lib_pg_attribute;

-- lib_organization
SELECT ORG_ID,ORG_NAME,UIC,ORG_CLASS 
INTO OUTFILE ":cip/csmart/data/database/new/raw_data/lib_organization_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY ",\n" FROM lib_organization;

-- org_relation
SELECT ROLE,SUPPORTING_ORG_ID,SUPPORTED_ORG_ID,START_DATE,END_DATE
INTO OUTFILE ":cip/csmart/data/database/new/raw_data/org_relation_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY ",\n" FROM org_relation;

-- org_pg_attr
SELECT ORG_ID,PG_ATTRIBUTE_LIB_ID,ATTRIBUTE_VALUE,ATTRIBUTE_ORDER,START_DATE,END_DATE 
INTO OUTFILE ":cip/csmart/data/database/new/raw_data/org_pg_attr_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY ",\n" FROM org_pg_attr;

-- oplan_agent_attr
SELECT DISTINCT OPLAN_ID,ORG_ID,COMPONENT_ID,START_CDAY,ATTRIBUTE_NAME,END_CDAY,ATTRIBUTE_VALUE
INTO OUTFILE ":cip/csmart/data/database/new/raw_data/oplan_agent_attr_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY ",\n" FROM oplan_agent_attr;

-- lib_oplan
SELECT DISTINCT OPLAN_ID,OPERATION_NAME,PRIORITY,C0_DATE
INTO OUTFILE ":cip/csmart/data/database/new/raw_data/lib_oplan_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY ",\n" FROM lib_oplan;

-- alploc
SELECT DISTINCT ALPLOC_CODE,LOCATION_NAME,LATITUDE,LONGITUDE,INSTALLATION_TYPE_CODE
INTO OUTFILE ":cip/csmart/data/database/new/raw_data/alploc_data.csv" 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY ",\n" FROM alploc;
