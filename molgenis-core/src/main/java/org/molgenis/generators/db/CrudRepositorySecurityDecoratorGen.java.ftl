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

import java.util.Iterator;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Query;
import org.molgenis.data.DatabaseAction;

/**
 * TODO add column level security filters
 */
public class ${clazzName}<E extends Entity> extends CrudRepositoryDecorator<E>
{
	public ${clazzName}(CrudRepository<E> repository)
	{
		super(repository);
	}
	
	@Override
	public long count()
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_READ_${securityName}"))
		{
			throw new MolgenisDataAccessException("No read permission on ${entityClass}");
		}
		
		return super.count();
	}

	@Override
	public Integer add(Entity entity)
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new MolgenisDataAccessException("No write permission on ${entityClass}");
		}
		
		return super.add(entity);
	}

	@Override
	public long count(Query q)
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_READ_${securityName}"))
		{
			throw new MolgenisDataAccessException("No read permission on ${entityClass}");
		}
		
		return super.count(q);
	}

	@Override
	public void update(Entity entity)
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new MolgenisDataAccessException("No write permission on ${entityClass}");
		}
		
		super.update(entity);
	}

	@Override
	public void add(Iterable<? extends Entity> entities)
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new MolgenisDataAccessException("No write permission on ${entityClass}");
		}
		
		super.add(entities);
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new MolgenisDataAccessException("No write permission on ${entityClass}");
		} 
		
		super.update(records);
	}

	@Override
	public Iterable<E> findAll(Query q)
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_READ_${securityName}"))
		{
			throw new MolgenisDataAccessException("No read permission on ${entityClass}");
		}
		
		return super.findAll(q);
	}

	@Override
	public void flush()
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new MolgenisDataAccessException("No write permission on ${entityClass}");
		}
		
		super.flush();
	}

	@Override
	public void delete(Entity entity)
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new MolgenisDataAccessException("No write permission on ${entityClass}");
		}
		
		super.delete(entity);
	}

	@Override
	public E findOne(Query q)
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_READ_${securityName}"))
		{
			throw new MolgenisDataAccessException("No read permission on ${entityClass}");
		}
		
		return super.findOne(q);
	}

	@Override
	public String getLabel()
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_READ_${securityName}"))
		{
			throw new MolgenisDataAccessException("No read permission on ${entityClass}");
		}
		
		return super.getLabel();
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new MolgenisDataAccessException("No write permission on ${entityClass}");
		}
		
		super.delete(entities);
	}

	@Override
	public void deleteById(Integer id)
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new MolgenisDataAccessException("No write permission on ${entityClass}");
		}
		
		super.deleteById(id);
	}

	@Override
	public Iterator<E> iterator()
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_READ_${securityName}"))
		{
			throw new MolgenisDataAccessException("No read permission on ${entityClass}");
		}
		
		return super.iterator();
	}

	@Override
	public E findOne(Integer id)
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_READ_${securityName}"))
		{
			throw new MolgenisDataAccessException("No read permission on ${entityClass}");
		}
		
		return super.findOne(id);
	}

	@Override
	public void deleteById(Iterable<Integer> ids)
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new MolgenisDataAccessException("No write permission on ${entityClass}");
		}
		
		super.deleteById(ids);
	}

	@Override
	public Iterable<E> findAll(Iterable<Integer> ids)
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_READ_${securityName}"))
		{
			throw new MolgenisDataAccessException("No read permission on ${entityClass}");
		}
		
		return super.findAll(ids);
	}

	@Override
	public void deleteAll()
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new MolgenisDataAccessException("No write permission on ${entityClass}");
		}
		
		super.deleteAll();
	}

	@Override
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_WRITE_${securityName}"))
		{
			throw new MolgenisDataAccessException("No write permission on ${entityClass}");
		}
		
		super.update(entities, dbAction, keyName);
	}
}