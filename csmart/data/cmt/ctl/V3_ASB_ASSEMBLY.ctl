options (
  skip=1)
load
  infile 'V3_ASB_ASSEMBLY.csv'
into table V3_ASB_ASSEMBLY
  insert
  fields
  (ASSEMBLY_ID                   char terminated by ',' optionally enclosed by '"',
   ASSEMBLY_TYPE                 char terminated by ',' optionally enclosed by '"',
   DESCRIPTION                   char terminated by ',' optionally enclosed by '"'
  )
