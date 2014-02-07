<#include "GeneratorHelper.ftl">
/** 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */
package org.molgenis.data.jpa;

import java.util.List;
import java.util.Map;

import org.molgenis.data.Repository;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.RepositorySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component("JpaRepositorySource")
public class JpaRepositorySource implements RepositorySource
{
	private final Map<String, Repository> repositories = Maps.newLinkedHashMap();

	<#list model.entities as entity>
	<#if !entity.abstract>
	@Autowired
	@Qualifier("${JavaName(entity)}Repository")
	public void set${JavaName(entity)}Repository(CrudRepository ${name(entity)}Repository)
	{	
		<#if disable_decorators>
		repositories.put("${entity.name}", ${name(entity)}Repository);
		<#elseif entity.decorator?exists>
		repositories.put("${entity.name}", new ${entity.decorator}(new ${entity.namespace}.db.${JavaName(entity)}SecurityDecorator(${name(entity)}Repository)));	
		<#else>
		repositories.put("${entity.name}", new ${entity.namespace}.db.${JavaName(entity)}SecurityDecorator(${name(entity)}Repository));	
		</#if>
	}
	</#if>
	</#list>
	
	@Override
	public List<Repository> getRepositories()
	{
		return Lists.newArrayList(repositories.values());
	}

	@Override
	public Repository getRepository(String name)
	{
		return repositories.get(name);
	}

}
