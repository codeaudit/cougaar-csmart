options (
  skip=1)
load
  infile 'V3_ASB_COMPONENT.csv'
into table V3_ASB_COMPONENT
  insert
  fields
  (ASSEMBLY_ID                char terminated by ',' optionally enclosed by '"',
   COMPONENT_ID               char terminated by ',' optionally enclosed by '"',
   COMPONENT_NAME             char terminated by ',' optionally enclosed by '"',
   PARENT_COMPONENT_ID        char terminated by ',' optionally enclosed by '"',
   COMPONENT_LIB_ID           char terminated by ',' optionally enclosed by '"',
   COMPONENT_CATEGORY         char terminated by ',' optionally enclosed by '"',
   INSERTION_ORDER            char terminated by ',' optionally enclosed by '"'
  )
