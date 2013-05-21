<#include "GeneratorHelper.ftl">

package ${package};

import javax.persistence.EntityManagerFactory;

import org.molgenis.util.WebAppUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class JpaDatabase extends org.molgenis.framework.db.jpa.JpaDatabase
{    
	@Autowired
	public JpaDatabase(EntityManagerFactory entityManagerFactory) throws org.molgenis.framework.db.DatabaseException {
		super(entityManagerFactory.createEntityManager(), new JDBCMetaDatabase());
        initMappers();
	}
	    
	private void initMappers()
	{
		<#list model.entities as entity><#if !entity.isAbstract()>
			<#if disable_decorators>
				this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.namespace}.db.${JavaName(entity)}JpaMapper(this));			
			<#elseif entity.decorator?exists>
				<#if auth_loginclass?ends_with("SimpleLogin")>
		this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.decorator}(new ${entity.namespace}.db.${JavaName(entity)}JpaMapper(this)));
				<#else>
		this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.decorator}(new ${entity.namespace}.db.${JavaName(entity)}SecurityDecorator(new ${entity.namespace}.db.${JavaName(entity)}JpaMapper(this))));
				</#if>	
			<#else>
				<#if auth_loginclass?ends_with("SimpleLogin")>
		this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.namespace}.db.${JavaName(entity)}JpaMapper(this));
				<#else>
		this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.namespace}.db.${JavaName(entity)}SecurityDecorator(new ${entity.namespace}.db.${JavaName(entity)}JpaMapper(this)));
				</#if>
			</#if>
		</#if></#list>	
	}
}
