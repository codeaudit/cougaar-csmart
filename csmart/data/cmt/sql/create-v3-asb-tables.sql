-- create-v3-tables.sql:  Society Config Tables V3

-- ASB DB (ASSEMBLY DB)

drop table V3_ASB_AGENT_PG_ATTR;
create table V3_ASB_AGENT_PG_ATTR (
    ASSEMBLY_ID             VARCHAR2(50),   -- PK
    COMPONENT_ID            VARCHAR2(150),  -- PK
    PG_ATTRIBUTE_LIB_ID     VARCHAR2(100),  -- PK
    ATTRIBUTE_VALUE         VARCHAR2(50),   -- PK
    ATTRIBUTE_ORDER         NUMBER,         -- PK
    START_DATE              DATE,           -- PK
    END_DATE                DATE
    );
alter table V3_ASB_AGENT_PG_ATTR add constraint PK_V3_ASB_AGENT_PG_ATTR
    primary key (ASSEMBLY_ID,COMPONENT_ID,PG_ATTRIBUTE_LIB_ID,ATTRIBUTE_VALUE,ATTRIBUTE_ORDER,START_DATE);

drop table V3_ASB_AGENT_RELATION;
create table V3_ASB_AGENT_RELATION (
    ASSEMBLY_ID             VARCHAR2(50),   -- PK
    ROLE                    VARCHAR2(50),   -- PK
    SUPPORTING_COMPONENT_ID VARCHAR2(50),   -- PK
    SUPPORTED_COMPONENT_ID  VARCHAR2(50),   -- PK
    START_DATE              DATE,           -- PK
    END_DATE                DATE
    );
alter table V3_ASB_AGENT_RELATION add constraint PK_V3_ASB_AGENT_RELATION
    primary key (ASSEMBLY_ID,ROLE,SUPPORTING_COMPONENT_ID,SUPPORTED_COMPONENT_ID,START_DATE);

drop table V3_ASB_ALPLOC;
create table V3_ASB_ALPLOC (
    ASSEMBLY_ID             VARCHAR2(50),   -- PK
    ALPLOC_CODE             VARCHAR2(50),   -- PK
    LOCATION_NAME           VARCHAR2(50),
    LATITUDE                NUMBER,
    LONGITUDE               NUMBER,
    INSTALLATION_TYPE_CODE  CHAR(3)
    );
alter table V3_ASB_ALPLOC add constraint PK_V3_ASB_ALPLOC
    primary key (ASSEMBLY_ID,ALPLOC_CODE);

drop table V3_ASB_ASSEMBLY;
create table V3_ASB_ASSEMBLY (
    ASSEMBLY_ID             VARCHAR2(50),   -- PK
    ASSEMBLY_TYPE           VARCHAR2(50),
    DESCRIPTION             VARCHAR2(200)
    );
alter table V3_ASB_ASSEMBLY add constraint PK_V3_ASB_ASSEMBLY
    primary key (ASSEMBLY_ID);

drop table V3_ASB_COMPONENT;
create table V3_ASB_COMPONENT (
    ASSEMBLY_ID             VARCHAR2(50),   -- AK
    COMPONENT_ID            VARCHAR2(150),  -- PK Unique for: ASSEMBLY_ID|COMPONENT_NAME|PARENT_COMPONENT_ID
    COMPONENT_NAME          VARCHAR2(100),  -- AK
    PARENT_COMPONENT_ID     VARCHAR2(100),  -- AK NULL for AGENTs and NL_COMPs
    COMPONENT_LIB_ID        VARCHAR2(100),
    COMPONENT_CATEGORY      VARCHAR2(50),
    INSERTION_ORDER         VARCHAR2(50)
    );
alter table V3_ASB_COMPONENT add constraint PK_V3_ASB_COMPONENT
    primary key (COMPONENT_ID);

drop table V3_ASB_COMPONENT_ARG;
create table V3_ASB_COMPONENT_ARG (
    ASSEMBLY_ID             VARCHAR2(50),   -- PK
    COMPONENT_ID            VARCHAR2(150),  -- PK
    ARGUMENT                VARCHAR2(100),  -- PK
    ARGUMENT_ORDER          NUMBER          -- PK
    );
alter table V3_ASB_COMPONENT_ARG add constraint PK_V3_ASB_COMPONENT_ARG
    primary key (ASSEMBLY_ID,COMPONENT_ID,ARGUMENT,ARGUMENT_ORDER);

drop table V3_ASB_COMPONENT_NODE;
create table V3_ASB_COMPONENT_NODE (
    ASSEMBLY_ID             VARCHAR2(50),   -- PK
    COMPONENT_ID            VARCHAR2(150),  -- PK
    NODE_ID                 VARCHAR2(50)
    );
alter table V3_ASB_COMPONENT_NODE add constraint PK_V3_ASB_COMPONENT_NODE
    primary key (ASSEMBLY_ID,COMPONENT_ID);

drop table V3_ASB_MACHINE;
create table V3_ASB_MACHINE ( -- Move to LIB?
    ASSEMBLY_ID             VARCHAR2(50),   -- PK
    MACHINE_ID              VARCHAR2(50),   -- PK
    MACHINE_NAME            VARCHAR2(50),
    IP_ADDRESS              VARCHAR2(50),
    OPERATING_SYSTEM        VARCHAR2(50),
    DESCRIPTION             VARCHAR2(100)
    );
alter table V3_ASB_MACHINE add constraint PK_V3_ASB_MACHINE
    primary key (ASSEMBLY_ID,MACHINE_ID);

drop table V3_ASB_NODE;
create table V3_ASB_NODE (
    ASSEMBLY_ID             VARCHAR2(50),   -- PK
    NODE_ID                 VARCHAR2(50),   -- PK
    NODE_NAME               VARCHAR2(50),   -- Just for human use
    DESCRIPTION             VARCHAR2(50)
    );
alter table V3_ASB_NODE add constraint PK_V3_ASB_NODE
    primary key (ASSEMBLY_ID,NODE_ID);

drop table V3_ASB_NODE_MACHINE;
create table V3_ASB_NODE_MACHINE (
    ASSEMBLY_ID             VARCHAR2(50),   -- PK
    NODE_ID                 VARCHAR2(50),   -- PK
    MACHINE_ID              VARCHAR2(50)    -- PK
    );
alter table V3_ASB_NODE_MACHINE add constraint PK_V3_ASB_NODE_MACHINE
    primary key (ASSEMBLY_ID,NODE_ID,MACHINE_ID);

drop table V3_ASB_OPLAN;
create table V3_ASB_OPLAN (
    ASSEMBLY_ID             VARCHAR2(50),    -- PK
    OPLAN_ID                VARCHAR2(50),    -- PK
    OPERATION_NAME          VARCHAR2(50),
    PRIORITY                VARCHAR2(50),
    C0_DATE                 DATE
        );
alter table V3_ASB_OPLAN add constraint PK_V3_ASB_OPLAN
    primary key (ASSEMBLY_ID,OPLAN_ID);

drop table V3_ASB_OPLAN_AGENT_ATTR;
create table V3_ASB_OPLAN_AGENT_ATTR (
    ASSEMBLY_ID             VARCHAR2(50),    -- PK
    OPLAN_ID                VARCHAR2(50),    -- PK
    COMPONENT_ID            VARCHAR2(50),    -- PK
    START_CDAY              NUMBER,          -- PK
    END_CDAY                NUMBER,
    ATTRIBUTE_NAME          VARCHAR2(50),    -- PK
    ATTRIBUTE_VALUE         VARCHAR2(50)
        );
alter table V3_ASB_OPLAN_AGENT_ATTR add constraint PK_V3_ASB_OPLAN_AGENT_ATTR
    primary key (ASSEMBLY_ID,OPLAN_ID,COMPONENT_ID,START_CDAY,ATTRIBUTE_NAME);

-- EXPT DB

drop table V3_EXPT_ASSEMBLY;
create table V3_EXPT_ASSEMBLY (
    EXPT_ID                 VARCHAR2(50),   -- PK
    ASSEMBLY_ID             VARCHAR2(50),   -- PK
    DESCRIPTION             VARCHAR2(200)
    );
alter table V3_EXPT_ASSEMBLY add constraint PK_V3_EXPT_ASSEMBLY
    primary key (EXPT_ID,ASSEMBLY_ID);

drop table V3_EXPT_EXPERIMENT;
create table V3_EXPT_EXPERIMENT (
    EXPT_ID                 VARCHAR2(50),   -- PK
    DESCRIPTION             VARCHAR2(200)
    );
alter table V3_EXPT_EXPERIMENT add constraint PK_V3_EXPT_EXPERIMENT
    primary key (EXPT_ID);

-- LIB DB

drop table V3_LIB_ACTIVITY_TYPE_REF;
create table V3_LIB_ACTIVITY_TYPE_REF (
    ACTIVITY_TYPE           VARCHAR2(50),    -- PK
    DESCRIPTION             VARCHAR2(50)
        );
alter table V3_LIB_ACTIVITY_TYPE_REF add constraint PK_V3_LIB_ACTIVITY_TYPE_REF
    primary key (ACTIVITY_TYPE);

drop table V3_LIB_AGENT_ORG;
create table V3_LIB_AGENT_ORG (
    COMPONENT_LIB_ID        VARCHAR2(50),   -- PK  Generated: AGENT_TYPE|AGENT_CLASS 
    AGENT_ORG_PROTOTYPE     VARCHAR2(50)
    );
alter table V3_LIB_AGENT_ORG add constraint PK_V3_LIB_AGENT_ORG
    primary key (COMPONENT_LIB_ID);

drop table V3_LIB_COMPONENT;
                               -- AGENTs, NL_COMPs and SUB_COMPONENTs (e.g. plugins)
create table V3_LIB_COMPONENT (
    COMPONENT_LIB_ID        VARCHAR2(150),  -- PK  Generated: COMPONENT_TYPE|COMPONENT_CLASS
    COMPONENT_TYPE          VARCHAR2(50),
    COMPONENT_CLASS         VARCHAR2(100),
    INSERTION_POINT         VARCHAR2(50),
    DESCRIPTION             VARCHAR2(100)
    );
alter table V3_LIB_COMPONENT add constraint PK_V3_LIB_COMPONENT
    primary key (COMPONENT_LIB_ID);

drop table V3_LIB_COMPONENT_ARG;
create table V3_LIB_COMPONENT_ARG (         -- 80 rows; not for run time use
    COMPONENT_LIB_ID        VARCHAR2(100),  -- PK
    ARGUMENT                VARCHAR2(100)   -- PK  Allow multiple arguments
                                            -- Add ARG_ORDER???
    );
alter table V3_LIB_COMPONENT_ARG add constraint PK_V3_LIB_COMPONENT_ARG
    primary key (COMPONENT_LIB_ID,ARGUMENT);

drop table V3_LIB_OPLAN_AGENT_ATTR_REF;
create table V3_LIB_OPLAN_AGENT_ATTR_REF (
    ATTRIBUTE_NAME          VARCHAR2(50),  -- PK
    DESCRIPTION             VARCHAR2(50)
        );
alter table V3_LIB_OPLAN_AGENT_ATTR_REF add constraint PK_V3_LIB_OPLAN_AGENT_ATTR_REF
    primary key (ATTRIBUTE_NAME);

drop table V3_LIB_OPTEMPO_REF;
create table V3_LIB_OPTEMPO_REF (
    OPTEMPO                 VARCHAR2(50),  -- PK
    DESCRIPTION             VARCHAR2(50)
    );
alter table V3_LIB_OPTEMPO_REF add constraint PK_V3_LIB_OPTEMPO_REF
    primary key (OPTEMPO);

drop table V3_LIB_PG_ATTRIBUTE;
create table V3_LIB_PG_ATTRIBUTE (
    PG_ATTRIBUTE_LIB_ID     VARCHAR2(100),  -- PK  Generated: PG_NAME|ATTRIBUTE_NAME
    PG_NAME                 VARCHAR2(50),
    ATTRIBUTE_NAME          VARCHAR2(50),
    ATTRIBUTE_TYPE          VARCHAR2(50),
    AGGREGATE_TYPE          VARCHAR2(50)    -- SINGLE, LIST, COLLECTION
    );
alter table V3_LIB_PG_ATTRIBUTE add constraint PK_V3_LIB_PG_ATTRIBUTE
    primary key (PG_ATTRIBUTE_LIB_ID);

-- REF DB

drop synonym GEOLOC;
create synonym GEOLOC for ALP_PLUGIN.GEOLOC;
