package org.molgenis.data;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.populate.EntityPopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static java.util.Objects.requireNonNull;

/**
 * Entity factory base class
 *
 * @param <E> entity type
 * @param <M> entity meta data type
 * @param <P> entity id type
 */
public abstract class AbstractSystemEntityFactory<E extends Entity, M extends SystemEntityType, P>
		implements EntityFactory<E, P>
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractSystemEntityFactory.class);

	private final Class<E> entityClass;
	private final Constructor<E> entityConstructorWithEntity;
	private final Constructor<E> entityConstructorWithEntityMeta;
	private final M systemEntityMetaData;
	private final EntityPopulator entityPopulator;

	/**
	 * Constructs a new entity factory that creates entities of the given type, meta data type and id type
	 *  @param entityClass      entity type
	 * @param systemEntityMeta entity meta data type
	 * @param entityPopulator
	 */
	protected AbstractSystemEntityFactory(Class<E> entityClass, M systemEntityMeta, EntityPopulator entityPopulator)
	{
		this.entityClass = requireNonNull(entityClass);

		// determining constructors at creation time validates that required constructors exist on start-up
		this.entityConstructorWithEntity = getConstructorEntity(entityClass);
		this.entityConstructorWithEntityMeta = getConstructorEntityMeta(entityClass);
		this.systemEntityMetaData = systemEntityMeta;
		this.entityPopulator = requireNonNull(entityPopulator);
	}

	public M getEntityMetaData()
	{
		return systemEntityMetaData;
	}

	@Override
	public String getEntityName()
	{
		return systemEntityMetaData.getName();
	}

	@Override
	public E create()
	{
		E entity;
		try
		{
			entity = entityConstructorWithEntityMeta.newInstance(systemEntityMetaData);
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
		entityPopulator.populate(entity);
		return entity;
	}

	@Override
	public E create(P id)
	{
		E entity = create();
		entity.setIdValue(id);
		return entity;
	}

	@Override
	public E create(Entity entity)
	{
		if (entity == null)
		{
			return null;
		}
		if (entity.getClass().equals(entityClass))
		{
			return (E) entity;
		}

		try
		{
			return entityConstructorWithEntity.newInstance(entity);
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}

	private Constructor<E> getConstructorEntity(Class<E> entityClass)
	{
		try
		{
			return entityClass.getConstructor(Entity.class);
		}
		catch (NoSuchMethodException e)
		{
			LOG.error("[{}] is missing the required constructor [public {}({})", entityClass.getName(),
					entityClass.getSimpleName(), Entity.class.getSimpleName());
			throw new RuntimeException(e);
		}
	}

	private Constructor<E> getConstructorEntityMeta(Class<E> entityClass)
	{
		try
		{
			return entityClass.getConstructor(EntityMetaData.class);
		}
		catch (NoSuchMethodException e)
		{
			LOG.error("[{}] is missing the required constructor [public {}({})", entityClass.getName(),
					entityClass.getSimpleName(), EntityMetaData.class.getSimpleName());
			throw new RuntimeException(e);
		}
	}
}
