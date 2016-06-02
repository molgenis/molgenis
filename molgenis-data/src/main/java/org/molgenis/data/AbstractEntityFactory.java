package org.molgenis.data;

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
public abstract class AbstractEntityFactory<E extends SystemEntity, M extends SystemEntityMetaData, P>
		implements EntityFactory<E, P>
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractEntityFactory.class);

	private final Constructor<E> entityConstructor;
	private final Constructor<E> entityConstructorWithId;
	private final M systemEntityMetaData;

	/**
	 * Constructs a new entity factory that creates entities of the given type, meta data type and id type
	 *
	 * @param entityClass          entity type
	 * @param systemEntityMetaData entity meta data type
	 * @param entityIdClass        entity id type
	 */
	protected AbstractEntityFactory(Class<E> entityClass, M systemEntityMetaData, Class<P> entityIdClass)
	{
		this.entityConstructor = getConstructor(entityClass, systemEntityMetaData.getClass());
		this.entityConstructorWithId = getConstructor(entityClass, systemEntityMetaData.getClass(), entityIdClass);
		this.systemEntityMetaData = systemEntityMetaData;
	}

	/**
	 * Creates a new entity
	 *
	 * @return new entity
	 */
	@Override
	public E create()
	{
		try
		{
			return entityConstructor.newInstance(systemEntityMetaData);
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a new entity with the given id
	 *
	 * @param id entity id
	 * @return new entity
	 */
	@Override
	public E create(P id)
	{
		try
		{
			return entityConstructorWithId.newInstance(id, systemEntityMetaData);
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}

	private Constructor<E> getConstructor(Class<E> entityClass,
			Class<? extends SystemEntityMetaData> entityMetaDataClass)
	{
		try
		{
			return entityClass.getConstructor(entityMetaDataClass);
		}
		catch (NoSuchMethodException e)
		{
			LOG.error("[{}] is missing the required constructor [public {}({})", entityClass.getName(),
					entityClass.getSimpleName(), entityMetaDataClass.getSimpleName());
			throw new RuntimeException(e);
		}
	}

	private Constructor<E> getConstructor(Class<E> entityClass,
			Class<? extends SystemEntityMetaData> entityMetaDataClass, Class<?> idType)
	{
		try
		{
			return entityClass.getConstructor(idType, entityMetaDataClass);
		}
		catch (NoSuchMethodException e)
		{
			LOG.error("[{}] is missing the required constructor [public {}({}, {})", entityClass.getName(),
					entityClass.getSimpleName(), idType.getSimpleName(), entityMetaDataClass.getSimpleName());
			throw new RuntimeException(e);
		}
	}
}
