options (
  skip=1)
load
--  infile 'V5_CFW_OPLAN.csv'
into table VV_CFW_OPLAN
  insert
  fields
  (CFW_ID                  char terminated by ',' optionally enclosed by '"',
   OPLAN_ID                char terminated by ',' optionally enclosed by '"',
   OPERATION_NAME          char terminated by ',' optionally enclosed by '"',
   PRIORITY                char terminated by ',' optionally enclosed by '"',
   C0_DATE                 char terminated by ',' optionally enclosed by '"'
  )
