-- SQL to change a plugin class in a copied experiment
-- used by switchPlugin-class.sh

-- lib_component table
CREATE TEMPORARY TABLE TEMPTAB AS 
 SELECT 
	CONCAT('plugin|',':newP') AS COMPONENT_LIB_ID, 
       	COMPONENT_TYPE AS COMPONENT_TYPE, 
       	':newP' AS COMPONENT_CLASS,
       	INSERTION_POINT, 
       	CONCAT(DESCRIPTION, '-modified') AS DESCRIPTION        	
 FROM lib_component 
 WHERE COMPONENT_CLASS = ':oldP';

REPLACE INTO lib_component 
    (COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION) 
 SELECT * FROM TEMPTAB;

DROP TABLE TEMPTAB;

-- alib_component table
CREATE TEMPORARY TABLE TEMPTAB AS 
SELECT 
  CONCAT(SUBSTRING(COMPONENT_ALIB_ID,1,INSTR(COMPONENT_ALIB_ID,'|')),':newP') AS COMPONENT_ALIB_ID, 
  ':newP' AS COMPONENT_NAME, 
  CONCAT('plugin|',':newP') AS COMPONENT_LIB_ID, 
  COMPONENT_TYPE, 
  CLONE_SET_ID 
 FROM alib_component 
 WHERE COMPONENT_LIB_ID = CONCAT('plugin|',':oldP');

REPLACE INTO alib_component 
  (COMPONENT_ALIB_ID, COMPONENT_NAME , COMPONENT_LIB_ID, COMPONENT_TYPE , CLONE_SET_ID) 
SELECT * FROM TEMPTAB;

DROP TABLE TEMPTAB;

-- Or JDBC
------ Turn these .sql files into real .q files & use the DBUtils
------ to get them. Also use the existing cougaar.rc to get
------ DB connection info. First pass: command line input
------ of expt to copy and suffix, later a GUI: list the expts
------ in the DB (stealing code from Organizer.java), and a gui
------ to input the suffix.
------ Then for this stuff I can use that input to find the
------ assembly id, store it, then have a .q file
------ that takes that assembly_id, and have a GUI to 
------ to take the old plugin class (drop-down?) and the new one (check if its already there?)

-- asb_component_arg table
UPDATE asb_component_arg SET COMPONENT_ALIB_ID = 
  CONCAT(SUBSTRING(COMPONENT_ALIB_ID,1,INSTR(COMPONENT_ALIB_ID,'|')),':newP')
WHERE COMPONENT_ALIB_ID LIKE '%|:oldP'
  AND ASSEMBLY_ID = ':assID';

-- asb_component_hierarchy
UPDATE IGNORE asb_component_hierarchy SET COMPONENT_ALIB_ID = 
  CONCAT(SUBSTRING(COMPONENT_ALIB_ID,1,INSTR(COMPONENT_ALIB_ID,'|')),':newP')
WHERE COMPONENT_ALIB_ID LIKE '%|:oldP'
      AND ASSEMBLY_ID = ':assID';

-- If users try to replace 2 plugins with one
-- (as in both AntsInventory and ConstructionInventory with BPDTInventory)
-- then the second run will not replace the original plugin
-- in the above update, due to the primary key.
-- So we delete any remaining instances with this command.
DELETE FROM asb_component_hierarchy
WHERE COMPONENT_ALIB_ID LIKE '%|:oldP'
      AND ASSEMBLY_ID = ':assID';

