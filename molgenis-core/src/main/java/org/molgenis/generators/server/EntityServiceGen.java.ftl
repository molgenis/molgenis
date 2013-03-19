<#include "GeneratorHelper.ftl">
<#assign fields=allFields(entity)>
package org.molgenis.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import ${entity.namespace}.${entity.name};
import org.molgenis.model.elements.Entity;
import org.molgenis.util.EntityPager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = WebApplicationContext.SCOPE_REQUEST)
public class ${entity.name}Service
{
	private static final Logger logger = Logger.getLogger(${entity.name}Service.class);

	@Autowired
	private Database db;

	public ${entity.name} create(${entity.name} ${entity.name?uncap_first}) throws DatabaseException
	{
		logger.debug("creating ${entity.name}");
		db.add(${entity.name?uncap_first});
		return ${entity.name?uncap_first};
	}

	public ${entity.name} read(${type(entity.primaryKey)} id) throws DatabaseException
	{
		logger.debug("retrieving ${entity.name}");
		return db.findById(${entity.name}.class, id);
	}

	public void update(${entity.name} ${entity.name?uncap_first}) throws DatabaseException
	{
		logger.debug("updating ${entity.name}");
		db.update(${entity.name?uncap_first});
	}

	public boolean deleteById(${type(entity.primaryKey)} id) throws DatabaseException
	{
		logger.debug("deleting ${entity.name}");
		${entity.name} ${entity.name?uncap_first} = db.findById(${entity.name}.class, id);
		return db.remove(${entity.name?uncap_first}) == 1;
	}
	
	public Iterable<${entity.name}> readAll() throws DatabaseException
	{
		logger.debug("retrieving all ${entity.name} instances");
		return db.find(${entity.name}.class);
	}
	
	public EntityPager<${entity.name}> readAll(int start, int num) throws DatabaseException
	{
		logger.debug("retrieving all ${entity.name} instances");
		int count = db.count(${entity.name}.class);
		List<${entity.name}> ${entity.name?uncap_first}s = db.find(${entity.name}.class, new QueryRule(Operator.OFFSET, start), new QueryRule(Operator.LIMIT, num));
		return new EntityPager<${entity.name}>(start, num, count, ${entity.name?uncap_first}s);
	}
	
	public Entity getEntity() throws DatabaseException
	{
		return db.getMetaData().getEntity("${entity.name}");
	}
}