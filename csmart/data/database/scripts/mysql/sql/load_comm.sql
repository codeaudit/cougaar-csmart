#
# Table structure for table 'community_entity_attribute'
#

LOAD DATA INFILE ':cip/csmart/data/database/scripts/mysql/community_entity_attribute.csv.tmp'
    INTO TABLE community_entity_attribute
    FIELDS
        TERMINATED BY ','
        OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (COMMUNITY_ID,ENTITY_ID,ATTRIBUTE_ID,ATTRIBUTE_VALUE);

#
# Table structure for table 'community_attribute'
#

LOAD DATA INFILE ':cip/csmart/data/database/scripts/mysql/community_attribute.csv.tmp'
    INTO TABLE community_attribute
    FIELDS TERMINATED BY ','
    OPTIONALLY ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (COMMUNITY_ID,ATTRIBUTE_ID,ATTRIBUTE_VALUE);
