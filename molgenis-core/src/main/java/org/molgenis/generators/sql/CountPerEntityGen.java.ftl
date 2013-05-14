<#include "GeneratorHelper.ftl">
<#foreach entity in model.getConcreteEntities()>
	<#if entity.hasDescendants() || entity.hasAncestor() >
SELECT '${SqlName(entity)}' AS entity, count(*) AS count FROM <#list superclasses(entity) as superclass>${SqlName(superclass)}<#if superclass_has_next> NATURAL JOIN </#if></#list> WHERE ${typefield()} = '${Name(entity)}'
	<#else>
SELECT '${SqlName(entity)}' AS entity, count(*) AS count FROM ${SqlName(entity)}
	</#if>
<#if entity_has_next> UNION </#if>
</#foreach>;