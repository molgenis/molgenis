<#include "GeneratorHelper.ftl">
<#foreach entity in model.getConcreteEntities()>
SELECT '${SqlName(entity)}' AS entity, count(*) AS count FROM ${name(entity)}
<#if entity_has_next> UNION </#if>
</#foreach>;