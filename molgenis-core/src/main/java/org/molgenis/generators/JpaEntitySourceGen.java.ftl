<#include "GeneratorHelper.ftl">
package org.molgenis.data.jpa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Repository
public class JpaEntitySourceImpl implements JpaEntitySource
{
	private final Map<String, CrudRepository<? extends Entity>> repos = new LinkedHashMap<String, CrudRepository<? extends Entity>>();
	
	@Override
	public String getUrl()
	{
		return "jpa://";
	}

	<#list model.entities as entity>
	<#if !entity.abstract>
	@Autowired
	public void set${JavaName(entity)}Repository(${entity.namespace}.${JavaName(entity)}Repository ${name(entity)}Repository)
	{	
		<#if disable_decorators>
		repos.put("${entity.name}", ${name(entity)}Repository);
		<#elseif entity.decorator?exists>
		repos.put("${entity.name}", new ${entity.decorator}<${entity.namespace}.${JavaName(entity)}>(new ${entity.namespace}.db.${JavaName(entity)}SecurityDecorator<${entity.namespace}.${JavaName(entity)}>(${name(entity)}Repository)));	
		<#else>
		repos.put("${entity.name}", new ${entity.namespace}.db.${JavaName(entity)}SecurityDecorator<${entity.namespace}.${JavaName(entity)}>(${name(entity)}Repository));	
		</#if>
	}
	</#if>
	</#list>

	@Override
	public Iterable<String> getEntityNames()
	{
		return new ArrayList<String>(repos.keySet());
	}

	@Override
	public Repository<? extends Entity> getRepositoryByEntityName(String entityName)
	{
		Repository<? extends Entity> repo = repos.get(entityName);
		if (repo == null)
		{
			throw new MolgenisDataException("Unknown jpa entity [" + entityName + "]");
		}
		
		return repo;
	}
	
	@Override
	public void close() throws IOException
	{
		// Nothing
	}
}
