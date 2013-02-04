<#include "GeneratorHelper.ftl">
<#if db_driver?contains('hsql')>
SET WRITE_DELAY FALSE;
</#if>
INSERT INTO MolgenisRole (__Type, id, name) values ('MolgenisGroup', 1, 'system');
INSERT INTO MolgenisRole (__Type, id, name) values ('MolgenisUser', 2, 'admin');
INSERT INTO MolgenisRole (__Type, id, name) values ('MolgenisUser', 3, 'anonymous');
INSERT INTO MolgenisRole (__Type, id, name) values ('MolgenisGroup', 4, 'AllUsers');

INSERT INTO MolgenisGroup (id) values (1);
INSERT INTO MolgenisGroup (id) values (4);

INSERT INTO Person (id, Email, FirstName, LastName) values (2, 'put_something_here@somewhere.com', 'admin', 'admin');
INSERT INTO Person (id, Email, FirstName, LastName) values (3, 'put_something_here@somewhere.com', 'anonymous','anonymous');
INSERT INTO MolgenisUser (id, password_, active, superuser) values (2, 'md5_21232f297a57a5a743894a0e4a801fc3', true, true);
INSERT INTO MolgenisUser (id, password_, active) values (3, 'md5_294de3557d9d00b3d2d8a1e6aab028cf', true);

<#-- 
INSERT INTO Person(title, lastname, firstname, institute, department, position_, city, country, emailaddress, id ) values ("", "admin", "admin", "", "", "", "", "", "please_fill_in_@somewhere.com", 2);
INSERT INTO Person(Address, Phone, Email, Fax, tollFreePhone,City,Country,FirstName,MidInitials,LastName,Title,Affiliation,Department,Roles) values ("Address", "", "please_fill_in_@somewhere.com", "", "","City","Country","FirstName","MidInitials","LastName","Title","","Department",2);
-->

<#-- 
INSERT INTO MolgenisUser (id, password_, emailaddress, firstname, lastname, active, superuser) values (2, 'md5_21232f297a57a5a743894a0e4a801fc3', '', 'admin', 'admin', true, true);
INSERT INTO MolgenisUser (id, password_, emailaddress, firstname, lastname, active) values (3, 'md5_294de3557d9d00b3d2d8a1e6aab028cf', '', 'anonymous','anonymous', true);
-->
<#list model.getUserinterface().getAllUniqueGroups() as group>
INSERT INTO MolgenisRole (__Type, id, name) values ('MolgenisGroup', ${group_index+5}, '${group}');
INSERT INTO MolgenisGroup (id) values (${group_index+5});
</#list>

INSERT INTO MolgenisRoleGroupLink (group_, role_) values (1, 2);
INSERT INTO MolgenisRoleGroupLink (group_, role_) values (4, 2);
INSERT INTO MolgenisRoleGroupLink (group_, role_) values (1, 3);
INSERT INTO MolgenisRoleGroupLink (group_, role_) values (4, 3);

<#list model.getConcreteEntities() as entity>
INSERT INTO MolgenisEntity(name, type_, classname) values ('${JavaName(entity)}', 'ENTITY', '${entity.namespace}.${JavaName(entity)}');
</#list>

<#assign schema = model.getUserinterface()>
<#list schema.getAllChildren() as screen>
	<#if screen.getType() == "FORM">
INSERT INTO MolgenisEntity(name, type_, classname) values ('${screen.getName()}${screen.getType()?lower_case?cap_first}Controller', '${screen.getType()}', 'app.ui.${screen.getName()}${screen.getType()?lower_case?cap_first}Controller');
	<#else>
INSERT INTO MolgenisEntity(name, type_, classname) values ('${screen.getName()}${screen.getType()?lower_case?cap_first}', '${screen.getType()}', 'app.ui.${screen.getName()}${screen.getType()?lower_case?cap_first}');
	</#if>
</#list>

INSERT INTO MolgenisPermission (role_, entity, permission) SELECT 3, id, 'read' FROM MolgenisEntity WHERE MolgenisEntity.name = 'UserLoginPlugin';
<#list schema.getAllChildren() as screen>
	<#if screen.getGroup()?exists>
		<#if screen.getType() == "FORM">
INSERT INTO MolgenisPermission (role_, entity, permission) SELECT (SELECT id FROM MolgenisRole WHERE name = '${screen.getGroup()}'), id, 'write' FROM MolgenisEntity WHERE MolgenisEntity.name = '${screen.getName()}${screen.getType()?lower_case?cap_first}Controller';
INSERT INTO MolgenisPermission (role_, entity, permission) SELECT (SELECT id FROM MolgenisRole WHERE name = '${screen.getGroup()}'), id, 'write' FROM MolgenisEntity WHERE MolgenisEntity.id = (SELECT id FROM MolgenisEntity WHERE className = '${screen.getEntity().namespace}.${screen.getEntity().name}');
		<#else>
INSERT INTO MolgenisPermission (role_, entity, permission) SELECT (SELECT id FROM MolgenisRole WHERE name = '${screen.getGroup()}'), id, 'write' FROM MolgenisEntity WHERE MolgenisEntity.name = '${screen.getName()}${screen.getType()?lower_case?cap_first}';
		</#if>
	</#if>
</#list>