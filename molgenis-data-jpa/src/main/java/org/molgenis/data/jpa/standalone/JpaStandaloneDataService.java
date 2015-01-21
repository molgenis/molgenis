package org.molgenis.data.jpa.standalone;

import java.lang.reflect.Constructor;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.metamodel.EntityType;

import org.molgenis.data.Entity;
import org.molgenis.data.jpa.JpaRepository;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.NonDecoratingRepositoryDecoratorFactory;
import org.molgenis.data.support.QueryResolver;
import org.molgenis.data.validation.DefaultEntityValidator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.EntityValidator;
import org.springframework.beans.BeanUtils;

import com.google.common.collect.Maps;

/**
 * DataService for use in standalone apps that automatically instanciates all JpaRepsoitories.
 */
public class JpaStandaloneDataService extends DataServiceImpl
{
	private final EntityManager entityManager;

	public JpaStandaloneDataService(String dbUrl, String dbUsername, String dbPassword) throws ClassNotFoundException,
			NoSuchMethodException, SecurityException
	{
		super(new NonDecoratingRepositoryDecoratorFactory());

		Map<String, String> props = Maps.newHashMap();
		props.put("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
		props.put("javax.persistence.jdbc.url", dbUrl);
		props.put("javax.persistence.jdbc.user", dbUsername);
		props.put("javax.persistence.jdbc.password", dbPassword);

		entityManager = Persistence.createEntityManagerFactory("molgenis", props).createEntityManager();
		for (EntityType<?> entityType : entityManager.getMetamodel().getEntities())
		{
			addRepository(createJpaRepository(entityType));
		}
	}

	private JpaRepository createJpaRepository(EntityType<?> entityType) throws ClassNotFoundException,
			NoSuchMethodException, SecurityException
	{
		String repoClassName = entityType.getJavaType().getName() + "Repository";

		@SuppressWarnings("unchecked")
		Class<? extends JpaRepository> repoClass = (Class<? extends JpaRepository>) Class.forName(repoClassName);
		Constructor<? extends JpaRepository> constructor = repoClass.getConstructor(EntityManager.class,
				EntityValidator.class, QueryResolver.class);

		return BeanUtils.instantiateClass(constructor, entityManager, new DefaultEntityValidator(this,
				new EntityAttributesValidator()), new QueryResolver(this));
	}

	@Override
	public void add(final String entityName, final Entity entity)
	{
		doInTransaction(new TransactionCallback<Object>()
		{
			@Override
			public Object execute()
			{
				JpaStandaloneDataService.super.add(entityName, entity);
				return null;
			}

		});
	}

	@Override
	public void add(final String entityName, final Iterable<? extends Entity> entities)
	{
		doInTransaction(new TransactionCallback<Object>()
		{
			@Override
			public Object execute()
			{
				JpaStandaloneDataService.super.add(entityName, entities);
				return null;
			}
		});
	}

	@Override
	public void update(final String entityName, final Entity entity)
	{
		doInTransaction(new TransactionCallback<Object>()
		{
			@Override
			public Object execute()
			{
				JpaStandaloneDataService.super.update(entityName, entity);
				return null;
			}
		});
	}

	@Override
	public void update(final String entityName, final Iterable<? extends Entity> entities)
	{
		doInTransaction(new TransactionCallback<Object>()
		{
			@Override
			public Object execute()
			{
				JpaStandaloneDataService.super.update(entityName, entities);
				return null;
			}
		});
	}

	@Override
	public void delete(final String entityName, final Entity entity)
	{
		doInTransaction(new TransactionCallback<Object>()
		{
			@Override
			public Object execute()
			{
				JpaStandaloneDataService.super.delete(entityName, entity);
				return null;
			}
		});
	}

	@Override
	public void delete(final String entityName, final Iterable<? extends Entity> entities)
	{
		doInTransaction(new TransactionCallback<Object>()
		{
			@Override
			public Object execute()
			{
				JpaStandaloneDataService.super.delete(entityName, entities);
				return null;
			}
		});
	}

	@Override
	public void delete(final String entityName, final Object id)
	{
		doInTransaction(new TransactionCallback<Object>()
		{
			@Override
			public Object execute()
			{
				JpaStandaloneDataService.super.delete(entityName, id);
				return null;
			}
		});
	}

	private <T> T doInTransaction(TransactionCallback<T> callback)
	{
		try
		{
			entityManager.getTransaction().begin();
			T result = callback.execute();
			entityManager.getTransaction().commit();

			return result;
		}
		catch (RuntimeException e)
		{
			entityManager.getTransaction().rollback();
			throw e;
		}
	}

	private static interface TransactionCallback<T>
	{
		T execute();
	}
}
