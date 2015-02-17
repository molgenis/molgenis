<#include "GeneratorHelper.ftl">
/** 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */
package org.molgenis.data.jpa;

import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.SearchRepositoryDecoratorCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

@Component("JpaRepositoryCollection")
public class JpaRepositoryCollection extends SearchRepositoryDecoratorCollection
{
	private final Map<String, Repository> repositories = Maps.newLinkedHashMap();
    
    @Autowired
	public JpaRepositoryCollection(SearchService searchService, DataService dataService)
	{
		super(searchService, dataService, "JPA");
	}

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
	protected Repository createRepository(EntityMetaData entityMeta)
	{
		return repositories.get(entityMeta.getName());
	}
}
