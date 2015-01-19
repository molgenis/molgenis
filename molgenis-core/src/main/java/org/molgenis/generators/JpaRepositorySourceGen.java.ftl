<#include "GeneratorHelper.ftl">
/** 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */
package org.molgenis.data.jpa;

import java.util.Map;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

@Component("JpaRepositoryCollection")
public class JpaRepositoryCollection implements CrudRepositoryCollection
{
	private final Map<String, CrudRepository> repositories = Maps.newLinkedHashMap();
     
	<#list model.entities as entity>
	<#if !entity.abstract>
	@Autowired
	@Qualifier("${JavaName(entity)}Repository")
	public void set${JavaName(entity)}Repository(CrudRepository ${name(entity)}Repository)
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
	public CrudRepository getCrudRepository(String name)
	{
		return repositories.get(name);
	}
	
	@Override
	public String getName()
	{
		return "JPA";
	}
	
	@Override
	public CrudRepository addEntityMeta(EntityMetaData entityMeta)
	{
		return getCrudRepository(entityMeta.getName());
	}
}
