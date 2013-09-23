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

import static org.molgenis.security.SecurityUtils.currentUserHasRole;

import java.util.List;

import java.text.ParseException;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.io.TupleReader;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.io.TupleWriter;

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
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		
		return super.add(entities);
	}

	@Override
	public int update(List<E> entities) throws DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		return super.update(entities);
	}

	@Override
	public int remove(List<E> entities) throws DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		return super.remove(entities);
	}

	@Override
	public int add(TupleReader reader, TupleWriter writer) throws DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		return super.add(reader, writer);
	}

	@Override
	public int count(QueryRule... rules) throws DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_READ_${securityName}"))
		{
			throw new DatabaseAccessException("No read permission on ${entityClass}");
		}
		return super.count(rules);
	}

	@Override
	public List<E> find(QueryRule ...rules) throws DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_READ_${securityName}"))
		{
			throw new DatabaseAccessException("No read permission on ${entityClass}");
		}
		return super.find(rules);
	}

	@Override
	public void find(TupleWriter writer, QueryRule ...rules) throws DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_READ_${securityName}"))
		{
			throw new DatabaseAccessException("No read permission on ${entityClass}");
		}
		super.find(writer, rules);
	}

	@Override
	public E findById(Object id) throws DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_READ_${securityName}"))
		{
			throw new DatabaseAccessException("No read permission on ${entityClass}");
		}
		return super.findById(id);
	}
	
	@Override
	public int remove(TupleReader reader) throws DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		return super.remove(reader);
	}

	@Override
	public int update(TupleReader reader) throws DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_WRITED_${securityName}"))
		{
			throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		return super.update(reader);
	}

	@Override
	public void find(TupleWriter writer, List<String> fieldsToExport, QueryRule[] rules) throws DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_READ_${securityName}"))
		{
			throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		super.find(writer, fieldsToExport, rules);
	}

	@Override
	public int executeAdd(List<? extends E> entities) throws DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		return super.executeAdd(entities);
	}
	
	@Override
	public int executeUpdate(List<? extends E> entities) throws DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		return super.executeUpdate(entities);
	}
	
	@Override
	public int executeRemove(List<? extends E> entities) throws DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		return super.executeRemove(entities);
	}
	
	@Override
	public void resolveForeignKeys(List<E> entities) throws ParseException, DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		super.resolveForeignKeys(entities);
	}
	
	@Override
	public List<E> toList(TupleReader reader, int limit) throws DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new DatabaseAccessException("No write permission on ${entityClass}");
		}
		return super.toList(reader, limit);
	}
	
	@Override
	public String createFindSqlInclRules(QueryRule[] rules) throws DatabaseException
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_ENTITY_READ_${securityName}"))
		{
			throw new DatabaseAccessException("No write permission on ${entityClass}");
		}	
		return super.createFindSqlInclRules(rules);
	}
}