options (
  skip=1)
load
  infile 'V3_ASB_COMPONENT_ARG.csv'
into table V3_ASB_COMPONENT_ARG
  insert
  fields
  (ASSEMBLY_ID           char terminated by ',' optionally enclosed by '"',
   COMPONENT_ID          char terminated by ',' optionally enclosed by '"',
   ARGUMENT              char terminated by ',' optionally enclosed by '"',
   ARGUMENT_ORDER        char terminated by ',' optionally enclosed by '"'
  )
