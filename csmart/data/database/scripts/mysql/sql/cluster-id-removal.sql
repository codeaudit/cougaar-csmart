-- Remove all ClusterIdentifier references, in favor of MessageAddress
-- This updates a 9.x configuration database for 10.0
-- Run this with something like: mysql -u [user] -p[pw] [db] < cluster-id-removal.sql

-- Update Reference information, so future experiments are correct

-- cfw_org_pg_attr
UPDATE cfw_org_pg_attr
  SET   pg_attribute_lib_id = 'ClusterPG|MessageAddress'
  WHERE pg_attribute_lib_id = 'ClusterPG|ClusterIdentifier';

-- lib_pg_attribute
INSERT INTO lib_pg_attribute
  VALUES ('ClusterPG|MessageAddress','ClusterPG',
          'MessageAddress','MessageAddress','SINGLE');
DELETE FROM lib_pg_attribute
  WHERE PG_ATTRIBUTE_LIB_ID = 'ClusterPG|ClusterIdentifier';

-- Now update any existing experiments:

-- asb_agent_pg_attr
UPDATE asb_agent_pg_attr
  SET   PG_ATTRIBUTE_LIB_ID = 'ClusterPG|MessageAddress'
  WHERE PG_ATTRIBUTE_LIB_ID = 'ClusterPG|ClusterIdentifier';

