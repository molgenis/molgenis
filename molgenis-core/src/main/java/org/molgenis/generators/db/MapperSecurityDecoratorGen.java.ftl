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
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * TODO add column level security filters
 */
public class ${clazzName}<E extends ${entityClass}> extends MapperDecorator<E>
{
	public ${clazzName}(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_WRITE_USER')")
	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		return super.add(entities);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_WRITE_USER')")
	@Override
	public int update(List<E> entities) throws DatabaseException
	{
<#if authorizable??>
		this.addRowLevelSecurityFilters(entities);
</#if>
		return super.update(entities);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_WRITE_USER')")
	@Override
	public int remove(List<E> entities) throws DatabaseException
	{			
<#if authorizable??>
		this.addRowLevelSecurityFilters(entities);
</#if>
		return super.remove(entities);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_WRITE_USER')")
	@Override
	public int add(TupleReader reader, TupleWriter writer) throws DatabaseException
	{
		return super.add(reader, writer);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_READ_USER')")
	@Override
	public int count(QueryRule... rules) throws DatabaseException
	{
<#if authorizable??>
		rules = this.addRowLevelSecurityFilters(${entityClass}.CANREAD, rules);
</#if>
		return super.count(rules);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_READ_USER')")
	@Override
	public List<E> find(QueryRule ...rules) throws DatabaseException
	{
<#if authorizable??>
		rules = this.addRowLevelSecurityFilters(${entityClass}.CANREAD, rules);
</#if>
		return super.find(rules);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_READ_USER')")
	@Override
	public void find(TupleWriter writer, QueryRule ...rules) throws DatabaseException
	{
<#if authorizable??>
		rules = this.addRowLevelSecurityFilters(${entityClass}.CANREAD, rules);
</#if>
		super.find(writer, rules);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_READ_USER')")
	@Override
	public E findById(Object id) throws DatabaseException
	{
		return super.findById(id);
	}
	
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_WRITE_USER')")
	@Override
	public int remove(TupleReader reader) throws DatabaseException
	{
		return super.remove(reader);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_WRITE_USER')")
	@Override
	public int update(TupleReader reader) throws DatabaseException
	{
		return super.update(reader);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_READ_USER')")
	@Override
	public void find(TupleWriter writer, List<String> fieldsToExport, QueryRule[] rules) throws DatabaseException
	{
<#if authorizable??>
		rules = this.addRowLevelSecurityFilters(${entityClass}.CANREAD, rules);
</#if>
		super.find(writer, fieldsToExport, rules);
	}

	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_WRITE_USER')")
	@Override
	public int executeAdd(List<? extends E> entities) throws DatabaseException
	{
		return super.executeAdd(entities);
	}
	
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_WRITE_USER')")
	@Override
	public int executeUpdate(List<? extends E> entities) throws DatabaseException
	{
		return super.executeUpdate(entities);
	}
	
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_WRITE_USER')")
	@Override
	public int executeRemove(List<? extends E> entities) throws DatabaseException
	{
		return super.executeRemove(entities);
	}
	
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_READ_USER')")
	@Override
	public void resolveForeignKeys(List<E> entities) throws ParseException, DatabaseException
	{
		super.resolveForeignKeys(entities);
	}
	
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_READ_USER')")
	@Override
	public List<E> toList(TupleReader reader, int limit) throws DatabaseException
	{
		return super.toList(reader, limit);
	}
	
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_ENTITY_${entityClass?capitalize}_READ_USER')")
	@Override
	public String createFindSqlInclRules(QueryRule[] rules) throws DatabaseException
	{
		return super.createFindSqlInclRules(rules);
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
}