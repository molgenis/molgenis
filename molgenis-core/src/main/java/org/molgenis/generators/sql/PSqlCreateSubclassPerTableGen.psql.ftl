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
/* PostgreSQL create tables.
 * Created by: ${generator}
 * Date: ${date}
 *
 * BIG TODO: can we use Postgresql inheritance mechanism???
 */

/*********DROP VIEWS**********/
<#--list entities as entity>
	<#if !entity.isAbstract()>
DROP VIEW IF EXISTS view_${SqlName(entity)}s;
	</#if>
</#list-->

/*********DROP TABLES**********/
<#list entities?reverse as entity>
<#-- Generate a table for each concrete class (so, not abstract) -->
<#if !entity.isAbstract()>
DROP TABLE IF EXISTS ${SqlName(entity)};
</#if>
</#list>

/*********CREATE TABLES**********/
<#list entities as entity>
<#-- Generate a table for each concrete class (so, not abstract) -->
<#if !entity.isAbstract()>
/*${name(entity)}<#if entity.hasAncestor()> extends ${name(entity.getAncestor())}</#if><#if entity.hasImplements()> implements <#list entity.getImplements() as i>${name(i)}<#if i_has_next>,</#if></#list></#if>*/
<#--create enum type, drop existing type-->
<#list dbFields(entity) as f><#if f.type="enum">
DROP TYPE IF EXISTS ENUM_${SqlName(entity)}_${SqlName(f)};
CREATE TYPE ENUM_${SqlName(entity)}_${SqlName(f)} AS ENUM ( ${csv( f.getEnumOptions() )} );
</#if></#list>
CREATE TABLE ${SqlName(entity)} (
<#list dbFields(entity) as f>
	<#if f_index != 0>, </#if><@compress single_line=true>
	<#if f.auto && f.type == "int">${SqlName(f)} SERIAL NOT NULL <#else>
	${SqlName(f)} ${psql_type(model,f)}
	<#if !f.nillable> NOT </#if>NULL
	<#if f.getDefaultValue()?exists && f.getDefaultValue() != "" && f.type != "text" && f.type != "blob" && f.type != "hyperlink"> DEFAULT <#if f.type == "bool" || f.type == "int">${f.getDefaultValue()}<#else>'${f.getDefaultValue()}'</#if></#if>
	</#if></@compress>
	
</#list>
<#list entity.getKeys() as key>
<#if key_index == 0>
	, PRIMARY KEY(${csv(key.fields)}<#if key.subclass>,${typefield()}</#if>)
<#else>
	, UNIQUE(${csv(key.fields)}<#if key.subclass>,${typefield()}</#if>)
</#if>
</#list>
<#list entity.getIndices() as i>
	, INDEX (${csv(i.fields)})
</#list>
);
</#if>
</#list>
COMMIT;

/**********ADD FOREIGN KEYS**********/
<#-- Generate a table for each concrete class (so, not abstract) -->
<#list entities as entity><#if !entity.isAbstract()>
<#list dbFields(entity) as f><#if f.type == "xref">
ALTER TABLE ${SqlName(entity)} ADD FOREIGN KEY (${SqlName(f)}) REFERENCES ${SqlName(f.xrefEntity)} (${SqlName(f.xrefField)}) ON DELETE RESTRICT DEFERRABLE;
</#if></#list>
</#if></#list>

<#--
/**********CREATE VIEWS**********/
<#list entities as entity>
	<#if !entity.isAbstract()>
DROP VIEW IF EXISTS view_${SqlName(entity)}s;
	<#if entity.hasAncestor() || entity.hasDescendants()>
CREATE VIEW view_${SqlName(entity)}s AS SELECT * FROM <#list superclasses(entity) as superclass>${SqlName(superclass)}<#if superclass_has_next> NATURAL JOIN </#if></#list>;
	<#else>
CREATE VIEW view_${SqlName(entity)}s AS SELECT * FROM ${SqlName(entity)};	
	</#if>
	</#if>
</#list>-->