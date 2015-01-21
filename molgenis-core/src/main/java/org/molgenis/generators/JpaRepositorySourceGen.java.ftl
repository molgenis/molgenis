<#include "GeneratorHelper.ftl">
/** 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */
package org.molgenis.data.jpa;

import java.util.Iterator;
import java.util.Map;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

@Component("JpaRepositoryCollection")
public class JpaRepositoryCollection implements RepositoryCollection
{
	private final Map<String, Repository> repositories = Maps.newLinkedHashMap();
     
	<#list model.entities as entity>
	<#if !entity.abstract>
	@Autowired
	@Qualifier("${JavaName(entity)}Repository")
	public void set${JavaName(entity)}Repository(Repository ${name(entity)}Repository)
	{	
		<#if disable_decorators>
		repositories.put("${entity.name}", ${name(entity)}Repository);
		<#elseif entity.decorator?exists>
		repositories.put("${entity.name}", new ${entity.decorator}(${name(entity)}Repository));	
		<#else>
		repositories.put("${entity.name}", ${name(entity)}Repository);	
		</#if>
	}
	</#if>
	</#list>
	
	@Override
	public Iterable<String> getEntityNames()
	{
		return repositories.keySet();
	}

	@Override
	public Repository getRepository(String name)
	{
		return repositories.get(name);
	}
	
	@Override
	public String getName()
	{
		return "JPA";
	}
	
	@Override
	public Repository addEntityMeta(EntityMetaData entityMeta)
	{
		return getRepository(entityMeta.getName());
	}
	
	@Override
	public Iterator<Repository> iterator()
	{
		return repositories.values().iterator();
	}
}
