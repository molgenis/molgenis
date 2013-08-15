<#include "GeneratorHelper.ftl">
<#assign fields=allFields(entity)>
package org.molgenis.service;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import ${entity.namespace}.${entity.name};
import org.molgenis.model.elements.Entity;
import org.molgenis.util.EntityPager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class ${entity.name}Service
{
	private static final Logger logger = Logger.getLogger(${entity.name}Service.class);

	@Autowired
	@Qualifier("database")
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
	
	public EntityPager<${entity.name}> readAll(int start, int num, List<QueryRule> queryRules) throws DatabaseException
	{
		logger.debug("retrieving all ${entity.name} instances");
		if (queryRules == null) queryRules = new ArrayList<QueryRule>();
		queryRules.add(new QueryRule(Operator.OFFSET, start));
		queryRules.add(new QueryRule(Operator.LIMIT, num));
		int count = db.count(${entity.name}.class, queryRules.toArray(new QueryRule[0]));
		List<${entity.name}> ${entity.name?uncap_first}Collection = db.find(${entity.name}.class, queryRules.toArray(new QueryRule[0]));
		return new EntityPager<${entity.name}>(start, num, count, ${entity.name?uncap_first}Collection);
	}
	
	public Entity getEntity() throws DatabaseException
	{
		return db.getMetaData().getEntity("${entity.name}");
	}
}