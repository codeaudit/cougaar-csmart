###########################
## Non CSMART DB Indexes
##

## 
## INDEX: ref_org_list37 
##

CREATE INDEX ref_org_list37 ON org_pg_attr(ORG_ID);
;

## 
## INDEX: ref_org_list38 
##

CREATE INDEX ref_org_list38 ON org_pg_attr(PG_ATTRIBUTE_LIB_ID);
;

## 
## INDEX: reforg_rel31 
##

CREATE INDEX reforg_rel31 ON org_relation(SUPPORTING_ORG_ID);
;
## 
## INDEX: reforg_rel32 
##

CREATE INDEX reforg_rel32 ON org_relation(SUPPORTED_ORG_ID);
;
## 
## INDEX: reflib_org45 
##

CREATE INDEX reflib_org45 ON lib_organization(ORG_NAME);
;
## 
## INDEX: reflib_org46 
##

CREATE INDEX reflib_org46 ON lib_organization(ORG_ID);
;

## 
## INDEX: refoplan20 
##

CREATE INDEX refoplan20 ON oplan_agent_attr(OPLAN_ID);
;
## 
## INDEX: refoplan21 
##

CREATE INDEX refoplan21 ON oplan_agent_attr(ORG_ID);
;
