delete from community_attribute where ASSEMBLY_ID = ':asb_id';
delete from community_entity_attribute where ASSEMBLY_ID = ':asb_id';

update community_attribute set ASSEMBLY_ID = ':asb_id' where ASSEMBLY_ID = '';
update community_entity_attribute set ASSEMBLY_ID = ':asb_id' where ASSEMBLY_ID = '';