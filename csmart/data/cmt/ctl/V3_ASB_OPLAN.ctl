options (
  skip=1)
load
  infile 'V3_ASB_OPLAN.csv'
into table V3_ASB_OPLAN
  insert
  fields
  (ASSEMBLY_ID       char terminated by ',' optionally enclosed by '"',
   OPLAN_ID          char terminated by ',' optionally enclosed by '"',
   OPERATION_NAME    char terminated by ',' optionally enclosed by '"',
   PRIORITY          char terminated by ',' optionally enclosed by '"',
   C0_DATE           char terminated by ',' optionally enclosed by '"'
  )
