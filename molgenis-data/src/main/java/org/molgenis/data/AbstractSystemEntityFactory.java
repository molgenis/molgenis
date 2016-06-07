package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.molgenis.data.meta.SystemEntity;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entity factory base class
 *
 * @param <E> entity type
 * @param <M> entity meta data type
 * @param <P> entity id type
 */
public abstract class AbstractSystemEntityFactory<E extends SystemEntity, M extends SystemEntityMetaData, P>
		implements SystemEntityFactory<E, P>
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractSystemEntityFactory.class);

	private final Class<E> entityClass;
	private final Constructor<E> entityConstructorWithEntity;
	private final Constructor<E> entityConstructorWithEntityMeta;
	private final Constructor<E> entityConstructorWithIdAndEntityMeta;
	private final M systemEntityMetaData;

	/**
	 * Constructs a new entity factory that creates entities of the given type, meta data type and id type
	 *
	 * @param entityClass      entity type
	 * @param systemEntityMeta entity meta data type
	 * @param entityIdClass    entity id type
	 */
	protected AbstractSystemEntityFactory(Class<E> entityClass, M systemEntityMeta, Class<P> entityIdClass)
	{
		this.entityClass = requireNonNull(entityClass);

		// determining constructors at creation time validates that required constructors exist on start-up
		this.entityConstructorWithEntity = getConstructor(entityClass);
		this.entityConstructorWithEntityMeta = getConstructor(entityClass, systemEntityMeta.getClass());
		this.entityConstructorWithIdAndEntityMeta = getConstructor(entityClass, systemEntityMeta.getClass(),
				entityIdClass);
		this.systemEntityMetaData = systemEntityMeta;
	}

	@Override
	public Class<E> getEntityClass()
	{
		return entityClass;
	}

	@Override
	public E create()
	{
		try
		{
			return entityConstructorWithEntityMeta.newInstance(systemEntityMetaData);
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public E create(P id)
	{
		try
		{
			return entityConstructorWithIdAndEntityMeta.newInstance(id, systemEntityMetaData);
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
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

	private Constructor<E> getConstructor(Class<E> entityClass)
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

	private Constructor<E> getConstructor(Class<E> entityClass,
			Class<? extends SystemEntityMetaData> entityMetaArgClass)
	{
		try
		{
			return entityClass.getConstructor(entityMetaArgClass);
		}
		catch (NoSuchMethodException e)
		{
			LOG.error("[{}] is missing the required constructor [public {}({})", entityClass.getName(),
					entityClass.getSimpleName(), entityMetaArgClass.getSimpleName());
			throw new RuntimeException(e);
		}
	}

	private Constructor<E> getConstructor(Class<E> entityClass,
			Class<? extends SystemEntityMetaData> entityMetaArgClass, Class<?> idArgType)
	{
		try
		{
			return entityClass.getConstructor(idArgType, entityMetaArgClass);
		}
		catch (NoSuchMethodException e)
		{
			LOG.error("[{}] is missing the required constructor [public {}({}, {})", entityClass.getName(),
					entityClass.getSimpleName(), idArgType.getSimpleName(), entityMetaArgClass.getSimpleName());
			throw new RuntimeException(e);
		}
	}
}
