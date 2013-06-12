<#include "GeneratorHelper.ftl">
<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* Date:        ${date}
 * Template:	${template}
 * generator:   ${generator} ${version}
 */

package ${package};

<#if authorizable??>
import java.util.ArrayList;
import java.util.Collections;
</#if>
import java.util.List;

import java.text.ParseException;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.security.SimpleLogin;
import org.molgenis.io.TupleReader;
import org.molgenis.io.TupleWriter;
<#if authorizable??>
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.service.MolgenisUserService;
</#if>

/**
 * TODO add column level security filters
 */
public class ${clazzName}<E extends ${entityClass}> extends MapperDecorator<E>
{
	public ${clazzName}(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canWrite(${entityClass}.class))
				throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		return super.add(entities);
	}

	@Override
	public int update(List<E> entities) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canWrite(${entityClass}.class))
				throw new DatabaseAccessException("No write permission on ${entityClass}");

<#if authorizable??>
			this.addRowLevelSecurityFilters(entities);
</#if>
		}
		return super.update(entities);
	}

	@Override
	public int remove(List<E> entities) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canWrite(${entityClass}.class))
				throw new DatabaseAccessException("No write permission on ${entityClass}");
				
<#if authorizable??>
			this.addRowLevelSecurityFilters(entities);
</#if>
		}
		return super.remove(entities);
	}

	@Override
	public int add(TupleReader reader, TupleWriter writer) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canWrite(${entityClass}.class))
				throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		return super.add(reader, writer);
	}

	@Override
	public int count(QueryRule... rules) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canRead(${entityClass}.class))
				throw new DatabaseAccessException("No read permission on ${entityClass}");

<#if authorizable??>
			rules = this.addRowLevelSecurityFilters(${entityClass}.CANREAD, rules);
</#if>
		}
		return super.count(rules);
	}

	@Override
	public List<E> find(QueryRule ...rules) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canRead(${entityClass}.class))
				throw new DatabaseAccessException("No read permission on ${entityClass}");

<#if authorizable??>
			rules = this.addRowLevelSecurityFilters(${entityClass}.CANREAD, rules);
</#if>
		}

		List<E> result = super.find(rules);
		return result;
	}

	@Override
	public void find(TupleWriter writer, QueryRule ...rules) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canRead(${entityClass}.class))
				throw new DatabaseAccessException("No read permission on ${entityClass}");

<#if authorizable??>
			rules = this.addRowLevelSecurityFilters(${entityClass}.CANREAD, rules);
</#if>
		}

		super.find(writer, rules);
	}

	@Override
	public E findById(Object id) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canRead(${entityClass}.class))
				throw new DatabaseAccessException("No read permission on ${entityClass}");
		}
		
		return super.findById(id);
	}
	
	@Override
	public int remove(TupleReader reader) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canWrite(${entityClass}.class))
				throw new DatabaseAccessException("No write permission on ${entityClass}");

			//TODO: Add row level security filters
		}
		return super.remove(reader);
	}

	@Override
	public int update(TupleReader reader) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canWrite(${entityClass}.class))
				throw new DatabaseAccessException("No write permission on ${entityClass}");

			//TODO: Add row level security filters
		}
		return super.update(reader);
	}

	@Override
	public void find(TupleWriter writer, List<String> fieldsToExport, QueryRule[] rules) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canRead(${entityClass}.class))
				throw new DatabaseAccessException("No read permission on ${entityClass}");

<#if authorizable??>
			rules = this.addRowLevelSecurityFilters(${entityClass}.CANREAD, rules);
</#if>
		}

		super.find(writer, fieldsToExport, rules);
	}

<#if authorizable??>
	//TODO: Move this to Login interface
	private QueryRule[] addRowLevelSecurityFilters(String permission, QueryRule ...rules) throws DatabaseException
	{
		if (this.getDatabase().getLogin().isAuthenticated() && this.getDatabase().getLogin().getUserName().equals(Login.USER_ADMIN_NAME))
			return rules;

		MolgenisUserService service = MolgenisUserService.getInstance(this.getDatabase());
		MolgenisUser user = service.findById(this.getDatabase().getLogin().getUserId());
		
		List<Integer> roleIdList = service.findGroupIds(user);
		
		List<QueryRule> rulesList = new ArrayList<QueryRule>();
		Collections.addAll(rulesList, rules);
		if (permission.equals(${entityClass}.CANREAD))
		{
			QueryRule rule1 = new QueryRule(${entityClass}.CANWRITE, org.molgenis.framework.db.QueryRule.Operator.IN, roleIdList);
			QueryRule rule2 = new QueryRule(${entityClass}.CANREAD, org.molgenis.framework.db.QueryRule.Operator.IN, roleIdList);
			QueryRule rule4 = new QueryRule(${entityClass}.OWNS, org.molgenis.framework.db.QueryRule.Operator.IN, roleIdList);
			QueryRule rule3 = new QueryRule(org.molgenis.framework.db.QueryRule.Operator.OR);
			rulesList.add(new QueryRule(rule1, rule3, rule2, rule3, rule4));
		}
		return rulesList.toArray(new QueryRule[0]);
	}
	
	private void addRowLevelSecurityFilters(List<E> entities) throws DatabaseException
	{
		for (E entity : entities)
		{
			if (!(this.getDatabase().getLogin().canWrite(entity) || entity.getOwns().equals(this.getDatabase().getLogin().getUserId())))
			{
				throw new DatabaseAccessException("No row level write permission on ${entityClass}");
			}
		}
	}
</#if>
	@Override
	public int executeAdd(List<? extends E> entities) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canWrite(${entityClass}.class))
				throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		return super.executeAdd(entities);
	}
	
	@Override
	public int executeUpdate(List<? extends E> entities) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canWrite(${entityClass}.class))
				throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		return super.executeUpdate(entities);
	}
	
	@Override
	public int executeRemove(List<? extends E> entities) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canWrite(${entityClass}.class))
				throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		return super.executeRemove(entities);
	}
	
	@Override
	public void resolveForeignKeys(List<E> entities) throws ParseException, DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canWrite(${entityClass}.class))
					throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		super.resolveForeignKeys(entities);
	}
	
	@Override
	public List<E> toList(TupleReader reader, int limit) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canWrite(${entityClass}.class))
				throw new DatabaseAccessException("No write permission on ${entityClass}");

			//TODO: Add row level security filters
		}
		return super.toList(reader, limit);
	}
	
	@Override
	public String createFindSqlInclRules(QueryRule[] rules) throws DatabaseException
	{
		if (this.getDatabase().getLogin() != null && !(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			if (!this.getDatabase().getLogin().canRead(${entityClass}.class))
				throw new DatabaseAccessException("No read permission on ${entityClass}");
		}
		
		return super.createFindSqlInclRules(rules);
	}

	
	
}