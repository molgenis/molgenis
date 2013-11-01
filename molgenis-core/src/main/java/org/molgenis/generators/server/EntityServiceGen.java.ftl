<#include "GeneratorHelper.ftl">
<#assign fields=allFields(entity)>
package org.molgenis.service;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.QueryRule;
import ${entity.namespace}.${entity.name};
import org.molgenis.util.EntityPager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class ${entity.name}Service
{
	private static final Logger logger = Logger.getLogger(${entity.name}Service.class);

	private final CrudRepository<${entity.name}> repository;

	@Autowired
	public ${entity.name}Service(DataService dataService)
	{
		this.repository = dataService.getCrudRepository("${entity.name}");
	}

	@PreAuthorize("hasAnyRole('ROLE_SU<#if !entity.system>, ROLE_ENTITY_WRITE_${entity.name?upper_case}</#if>')")
	public ${entity.name} create(${entity.name} ${entity.name?uncap_first})
	{
		logger.debug("creating ${entity.name}");
		repository.add(${entity.name?uncap_first});
		return ${entity.name?uncap_first};
	}

	@PreAuthorize("hasAnyRole('ROLE_SU<#if !entity.system>, ROLE_ENTITY_READ_${entity.name?upper_case}</#if>')")
	public ${entity.name} read(${type(entity.primaryKey)} id)
	{
		logger.debug("retrieving ${entity.name}");
		return repository.findOne(id);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU<#if !entity.system>, ROLE_ENTITY_WRITE_${entity.name?upper_case}</#if>')")
	public void update(${entity.name} ${entity.name?uncap_first})
	{
		logger.debug("updating ${entity.name}");
		repository.update(${entity.name?uncap_first});
	}

	@PreAuthorize("hasAnyRole('ROLE_SU<#if !entity.system>, ROLE_ENTITY_WRITE_${entity.name?upper_case}</#if>')")
	public void deleteById(${type(entity.primaryKey)} id)
	{
		logger.debug("deleting ${entity.name}");
		repository.deleteById(id);
	}
	
	@PreAuthorize("hasAnyRole('ROLE_SU<#if !entity.system>, ROLE_ENTITY_READ_${entity.name?upper_case}</#if>')")
	public Iterable<${entity.name}> readAll()
	{
		logger.debug("retrieving all ${entity.name} instances");
		return repository.findAll(new QueryImpl());
	}
	
	@PreAuthorize("hasAnyRole('ROLE_SU<#if !entity.system>, ROLE_ENTITY_READ_${entity.name?upper_case}</#if>')")
	public EntityPager<${entity.name}> readAll(int start, int num, List<QueryRule> queryRules)
	{
		logger.debug("retrieving all Accession instances");
		if (queryRules == null) queryRules = new ArrayList<QueryRule>();

		QueryImpl q = new QueryImpl(queryRules);
		q.setPageSize(num);
		q.setOffset(start);

		long count = repository.count();
		Iterable<${entity.name}> ${entity.name?uncap_first}Collection = repository.findAll(q);

		return new EntityPager<${entity.name}>(start, num, count, ${entity.name?uncap_first}Collection);
	}
	
	@PreAuthorize("hasAnyRole('ROLE_SU<#if !entity.system>, ROLE_ENTITY_READ_${entity.name?upper_case}</#if>')")
	public EntityMetaData getEntityMetaData()
	{
		return repository;
	}
}