options (
  skip=1)
load
  infile 'V4_LIB_MACHINE.csv'
into table V4_LIB_MACHINE
  insert
  fields
  (COMPONENT_ALIB_ID       char terminated by ',' optionally enclosed by '"',
   MACHINE_NAME            char terminated by ',' optionally enclosed by '"',
   IP_ADDRESS              char terminated by ',' optionally enclosed by '"',
   OPERATING_SYSTEM        char terminated by ',' optionally enclosed by '"',
   DESCRIPTION             char terminated by ',' optionally enclosed by '"'
  )
