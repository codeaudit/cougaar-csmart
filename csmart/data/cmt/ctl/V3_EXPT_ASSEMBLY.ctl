options (
  skip=1)
load
  infile 'V3_EXPT_ASSEMBLY.csv'
into table V3_EXPT_ASSEMBLY
  insert
  fields
  (EXPT_ID           char terminated by ',' optionally enclosed by '"',
   ASSEMBLY_ID       char terminated by ',' optionally enclosed by '"',
   DESCRIPTION       char terminated by ',' optionally enclosed by '"'
  )
