database=${org.cougaar.configuration.database}
username=${org.cougaar.configuration.user}
password=${org.cougaar.configuration.password}

fillAgentV4_LIB_COMPONENT.eventual=
insert into V4_LIB_COMPONENT (COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION) \
select ORG_ID, 'agent',
'org.cougaar.core.cluster.ClusterImpl','Node.AgentManager.Agent', org_name \
from v7_lib_organization org,V4_ASB_AGENT_PG_ATTR aapg \
where org.org_id=aapg.component_alib_id \
and PG_ATTRIBUTE_LIB_ID ='TypeIdentificationPG|Nomenclature'


fillAgentV4_LIB_COMPONENT=
insert into V4_LIB_COMPONENT (COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION) \
select ORG_ID, 'agent', 'org.cougaar.core.cluster.ClusterImpl','Node.AgentManager.Agent', org_name \
from v7_lib_organization org

fillV4_LIB_AGENT_org=
insert into v4_lib_agent_org (COMPONENT_LIB_ID,AGENT_LIB_NAME, AGENT_ORG_CLASS)\
select ORG_ID, ORG_ID, 'MilitaryOrganization' \
from v7_lib_organization
