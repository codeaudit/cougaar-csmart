-- SQL to change a plugin class in a copied experiment
-- used by switchPlugin-class.sh

-- V4_LIB_COMPONENT table
CREATE TEMPORARY TABLE TEMPTAB AS 
 SELECT 
	CONCAT('plugin|',':newP') AS COMPONENT_LIB_ID, 
       	COMPONENT_TYPE AS COMPONENT_TYPE, 
       	':newP' AS COMPONENT_CLASS,
       	INSERTION_POINT, 
       	CONCAT(DESCRIPTION, '-modified') AS DESCRIPTION        	
 FROM V4_LIB_COMPONENT 
 WHERE COMPONENT_CLASS = ':oldP';

REPLACE INTO V4_LIB_COMPONENT 
    (COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION) 
 SELECT * FROM TEMPTAB;

DROP TABLE TEMPTAB;

-- V4_ALIB_COMPONENT table
CREATE TEMPORARY TABLE TEMPTAB AS 
SELECT 
  CONCAT(SUBSTRING(COMPONENT_ALIB_ID,1,INSTR(COMPONENT_ALIB_ID,'|')),':newP') AS COMPONENT_ALIB_ID, 
  ':newP' AS COMPONENT_NAME, 
  CONCAT('plugin|',':newP') AS COMPONENT_LIB_ID, 
  COMPONENT_TYPE, 
  CLONE_SET_ID 
 FROM V4_ALIB_COMPONENT 
 WHERE COMPONENT_LIB_ID = CONCAT('plugin|',':oldP');

REPLACE INTO V4_ALIB_COMPONENT 
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

-- V4_ASB_COMPONENT_ARG table

UPDATE V4_ASB_COMPONENT_ARG SET COMPONENT_ALIB_ID = 
  CONCAT(SUBSTRING(COMPONENT_ALIB_ID,1,INSTR(COMPONENT_ALIB_ID,'|')),':newP')
WHERE COMPONENT_ALIB_ID LIKE '%|:oldP'
  AND ASSEMBLY_ID = ':assID';

-- V4_ASB_COMPONENT_HIERARCHY
UPDATE V4_ASB_COMPONENT_HIERARCHY SET COMPONENT_ALIB_ID = 
  CONCAT(SUBSTRING(COMPONENT_ALIB_ID,1,INSTR(COMPONENT_ALIB_ID,'|')),':newP')
WHERE COMPONENT_ALIB_ID LIKE '%|:oldP'
      AND ASSEMBLY_ID = ':assID';

