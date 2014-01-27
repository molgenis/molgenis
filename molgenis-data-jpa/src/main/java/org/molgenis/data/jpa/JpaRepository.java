package org.molgenis.data.jpa;

import javax.persistence.EntityManager;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

public class JpaRepository<E extends Entity> extends AbstractJpaRepository<E>
{
	Class<E> entityClass;
	EntityMetaData entityMetaData;
	
	
	public JpaRepository(EntityManager em, Class<E> entityClass)
	{
		super(em,entityClass);
	}
	
	public JpaRepository(Class<E> entityClass)
	{
		super(entityClass);
		this.entityClass = entityClass;
		try
		{
			this.entityMetaData = entityClass.newInstance().getEntityMetaData();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}
}
