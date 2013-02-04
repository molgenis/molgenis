<#-- see http://www.graphviz.org/doc/info/colors.html -->
<#include "GeneratorHelper.ftl">
<#function csv items>
	<#local result = "">
	<#list items as item>
		<#if item_index != 0>
			<#local result = result + ", ">
		</#if>
		<#if item?is_hash>
			<#local result = result + item.name>
		<#else>
			<#local result = result + "'"+item+"'">
		</#if>
	</#list>
	<#return result>
</#function>

package dataType;
primitive bool;
primitive int; 
primitive string; 
primitive decimal; 
primitive xref;
primitive mref;
primitive file;
primitive date;
primitive datetime;
end.

<#macro render_entity entity>
(*${entity.description?trim}*)
<#if entity.isAbstract()>interface<#else>class</#if> ${entity.name}<#if entity.hasAncestor()>
  specializes ${entity.getAncestor().getName()}</#if><#if entity.hasImplements()>
  implements ${csv(entity.getImplements())}</#if>
<#list entity.fields as field><#if field.type == "xref" || field.type = "mref"><#else>
  attribute ${field.name} : dataType::${field.type};
</#if></#list>
end;
</#macro>

package ${name(model)};
<#list model.entities as entity><#if !entity.system>

<@render_entity entity/>
</#if></#list>

<#list model.modules as m>

package ${name(m)};
<#list m.entities as entity><#if !entity.system>

<@render_entity entity/>
<#list entity.fields as field><#if field.type == "xref">

association
  navigable role ${field.name}: ${field.xrefField}[*];
  role : ${entity.name}[1]; 
end;
<#elseif field.type = "mref">

association
  navigable role ${field.name}: ${field.xrefField}[*];
  role : ${entity.name}[*]; 
end;
</#if></#list>

end.
</#if></#list>
</#list>

end.