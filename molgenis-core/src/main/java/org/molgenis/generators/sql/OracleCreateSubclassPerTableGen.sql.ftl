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
<#list entities as entity>
	<#-- Generate a table for each concrete class (so, not abstract) -->
	<#if !entity.isAbstract()>
CREATE SEQUENCE ${SqlName(entity)}_seq start with 1 increment by 1;
CREATE TABLE ${SqlName(entity)} (<#list dbFields(entity) as f><#if f_index != 0>,</#if>
<@compress single_line=true>
			${SqlName(f)} ${oracle_type(model,f)}
			<#if f.getDefaultValue()?exists && f.getDefaultValue() != "" 
				&& f.type != "text" && f.type != "freemarker" && f.type != "richtext" && f.type != "blob" && f.type != "hyperlink">
					 DEFAULT 
					 <#if f.type == "bool" || f.type == "int">
					 	${f.getDefaultValue()}
				 	<#else>
				 		<#if f.type == "date">
				 			to_date('${f.getDefaultValue()}','yyyy-mm-dd')
				 		<#else>
				 			'${f.getDefaultValue()}'
			 			</#if>
			 		</#if>
			</#if>
			<#if !f.nillable> NOT </#if>NULL
			<#if f.type == "bool">CHECK (${SqlName(f)} in (0,1) )</#if>
</@compress></#list>
<#list entity.getKeys() as key>,<#if key_index == 0>
CONSTRAINT ${SqlName(entity)}_pkey PRIMARY KEY(${csv(key.fields)}<#if key.subclass>,${typefield()}</#if>)
<#else>CONSTRAINT ${SqlName(entity)}_unique${key_index} UNIQUE(${csv(key.fields)}<#if key.subclass>,${typefield()}</#if>)</#if></#list><#list entity.getIndices() as i>,
INDEX (<#list i.fields as f>${SqlName(f)}<#if f_has_next>,</#if></#list>)</#list>
);
CREATE OR REPLACE TRIGGER ${SqlName(entity)}_insert
BEFORE INSERT ON ${SqlName(entity)}
FOR EACH ROW
BEGIN
	:new.${csv(entity.getKeys()[0].fields)} := ${SqlName(entity)}_seq.nextval;
END;
/
	</#if>
</#list>


/**********ADD FOREIGN KEYS**********/
<#-- Generate a table for each concrete class (so, not abstract) -->
<#list entities as entity><#if !entity.isAbstract() && entity.hasAncestor()>
ALTER TABLE ${SqlName(entity)} ADD CONSTRAINT ${SqlName(entity)}_parent FOREIGN KEY (${SqlName(pkey(entity))}) REFERENCES ${SqlName(entity.getAncestor())} (${SqlName(pkey(entity))});
</#if></#list>

<#list entities as entity><#if !entity.isAbstract()>
<#list dbFields(entity) as f><#if f.type == "xref">
ALTER TABLE ${SqlName(entity)} ADD CONSTRAINT ${SqlName(entity)}_fkey${f_index} FOREIGN KEY (${SqlName(f)}) REFERENCES ${SqlName(f.xrefEntity)} (${SqlName(f.xrefField)})<#if f.xrefCascade> ON DELETE CASCADE</#if>;
</#if></#list>
</#if></#list>

