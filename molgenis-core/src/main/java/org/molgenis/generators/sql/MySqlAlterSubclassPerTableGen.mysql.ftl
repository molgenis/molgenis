<#setting number_format="#"/>
<#include "GeneratorHelper.ftl">
<#include "SqlHelper.ftl">
<#--#####################################################################
One table per concrete class
One table per inheritance root.
If the root is not abstract, a extra table is added called IName...with
as only column(s) the primary key of IName.

Note:
There are three alternative strategies: "one table per subclass", "multiple tables per subclass" and "one table per hierarchy".
advantage of this strategy:
* easy to understand (than table per hierarchy)
* easy to query (than table per class).
* easier to query subclasses
* good performance as not as many joins/updates needed as in "table per class" (max 2)
* easier to define tuple/table constraints over properties of class AND subclass
drawback of this strategy:
* more difficult change classes ("add/change/remove property" affects many tables).
* foreign keys cannot distinguish between classes (as they can in  "table per class").
* bit harder to query a class and its subclasses (than one table per hierarchy).
#####################################################################-->

<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/*
 * Created by: ${generator}
 * Date: ${date}
 */

/**********CREATE TABLES**********/
SET FOREIGN_KEY_CHECKS = 0; ##allows us to drop fkeyed tables
<#list entities as entity>
<#-- Generate a table for each concrete class (so, not abstract) -->
<#if !entity.isAbstract()>

/*${name(entity)}<#if entity.hasAncestor()> extends ${name(entity.getAncestor())}</#if><#if entity.hasImplements()> implements <#list entity.getImplements() as i>${name(i)}<#if i_has_next>,</#if></#list></#if>*/
#create the table if not exists
CREATE TABLE ${SqlName(entity)} (
<#list dbFields(entity) as f>
	<#if f_index != 0>, </#if><@compress single_line=true>
	${SqlName(f)} ${mysql_type(model,f)}
	<#if !f.nillable> NOT </#if>NULL
	<#if f.getDefaultValue()?exists && f.getDefaultValue() != "" && f.type != "text" && f.type != "blob" && f.type != "hyperlink"> DEFAULT <#if f.type == "bool" || f.type == "int">${f.getDefaultValue()}<#else>"${f.getDefaultValue()}"</#if></#if>
	<#if f.auto && f.type == "int" && ( !entity.getAncestor()?exists || !entity.getAncestor().getField(f.name)?exists )> AUTO_INCREMENT</#if></@compress>
</#list>
<#list entity.getKeys() as key>
<#if key_index == 0>
	, PRIMARY KEY(${csv(key.fields)}<#if key.subclass>,type</#if>)
<#else>
	, UNIQUE(${csv(key.fields)}<#if key.subclass>,type</#if>)
</#if>
</#list>
<#list entity.getIndices() as i>
	, INDEX (<#list i.fields as f>${SqlName(f)}<#if f_has_next>,</#if></#list>)
</#list>
) ENGINE=InnoDB;

<#list dbFields(entity) as f>
#strip dropped columns from constraints (keep data to be sure)
ALTER TABLE 

#modify the existing columns, simply fail if missing
ALTER TABLE ${SqlName(entity)} <@compress single_line=true>
	<#if f_index != 0>, </#if>
	MODIFY COLUMN ${SqlName(f)} ${mysql_type(model,f)}
	<#if !f.nillable> NOT </#if>NULL
	<#if f.getDefaultValue()?exists && f.getDefaultValue() != "" && f.type != "text" && f.type != "blob" && f.type != "hyperlink"> DEFAULT <#if f.type == "bool" || f.type == "int">${f.getDefaultValue()}<#else>"${f.getDefaultValue()}"</#if></#if>
	<#if f.auto && f.type == "int" && ( !entity.getAncestor()?exists || !entity.getAncestor().getField(f.name)?exists )> AUTO_INCREMENT</#if>
</@compress> IF EXISTS;

#add missing columns, simply fail if exist
ALTER TABLE ${SqlName(entity)} <@compress single_line=true>
	<#if f_index != 0>, </#if>
	ADD COLUMN ${SqlName(f)} ${mysql_type(model,f)}
	<#if !f.nillable> NOT </#if>NULL
	<#if f.getDefaultValue()?exists && f.getDefaultValue() != "" && f.type != "text" && f.type != "blob" && f.type != "hyperlink"> DEFAULT <#if f.type == "bool" || f.type == "int">${f.getDefaultValue()}<#else>"${f.getDefaultValue()}"</#if></#if>
	<#if f.auto && f.type == "int" && ( !entity.getAncestor()?exists || !entity.getAncestor().getField(f.name)?exists )> AUTO_INCREMENT</#if>
</@compress> IF NOT EXISTS;
</#list>


#else modify an existing table
#make dropped column names nullable, not auto


<#--list entity.getKeys() as key>
<#if key_index == 0>
	, PRIMARY KEY(${csv(key.fields)}<#if key.subclass>,type</#if>)
<#else>
	, UNIQUE(${csv(key.fields)}<#if key.subclass>,type</#if>)
</#if>
</#list>
<#list entity.getIndices() as i>
	, INDEX (<#list i.fields as f>${SqlName(f)}<#if f_has_next>,</#if></#list>)
</#list-->
#add the new columns

</#if></#list>
SET FOREIGN_KEY_CHECKS = 1;

/**********ADD/UPDATE FOREIGN KEYS**********/
<#-- Generate a table for each concrete class (so, not abstract) -->
<#list entities as entity><#if !entity.isAbstract() && entity.hasAncestor()>
ALTER TABLE ${SqlName(entity)} ADD FOREIGN KEY (${SqlName(pkey(entity))}) REFERENCES ${SqlName(entity.getAncestor())} (${SqlName(pkey(entity))}) ON DELETE RESTRICT;
</#if></#list>

<#list entities as entity><#if !entity.isAbstract()>
<#list dbFields(entity) as f><#if f.type == "xref">
ALTER TABLE ${SqlName(entity)} ADD FOREIGN KEY (${SqlName(f)}) REFERENCES ${SqlName(f.xrefEntity)} (${SqlName(f.xrefField)}) ON DELETE RESTRICT;
</#if></#list>
</#if></#list>
