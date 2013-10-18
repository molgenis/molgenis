<#include "GeneratorHelper.ftl">

package ${package};

<#list model.entities as entity>
	<#if !entity.isAbstract()>
import ${entity.namespace}.${JavaName(entity)};	
	</#if>
</#list>

import org.springframework.beans.factory.annotation.Autowired;

public class ${className} extends org.molgenis.framework.db.jpa.JpaDatabase
{    
	@Autowired
	public ${className}(JDBCMetaDatabase jdbcMetaDatabase) throws org.molgenis.framework.db.DatabaseException
	{
		super(jdbcMetaDatabase);
        initMappers();
	}
	    
	private void initMappers()
	{
<#list model.entities as entity>
	<#if !entity.isAbstract()>
		<#if disable_decorators>
		this.putMapper(${JavaName(entity)}.class, new ${entity.namespace}.db.${JavaName(entity)}JpaMapper(this));			
		<#elseif entity.decorator?exists>
			<#if secure>
		this.putMapper(${JavaName(entity)}.class, new ${entity.decorator}<${JavaName(entity)}>(new ${entity.namespace}.db.${JavaName(entity)}SecurityDecorator<${JavaName(entity)}>(new ${entity.namespace}.db.${JavaName(entity)}JpaMapper(this))));
			<#else>
		this.putMapper(${JavaName(entity)}.class, new ${entity.decorator}<${JavaName(entity)}>(new ${entity.namespace}.db.${JavaName(entity)}JpaMapper(this)));
			</#if>	
		<#else>
			<#if secure>
		this.putMapper(${JavaName(entity)}.class, new ${entity.namespace}.db.${JavaName(entity)}SecurityDecorator<${JavaName(entity)}>(new ${entity.namespace}.db.${JavaName(entity)}JpaMapper(this)));
			<#else>
		this.putMapper(${JavaName(entity)}.class, new ${entity.namespace}.db.${JavaName(entity)}JpaMapper(this));
			</#if>
		</#if>
	</#if>
</#list>	
	}
}
