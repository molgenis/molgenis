package org.molgenis.framework.db.jpa;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.molgenis.framework.db.AbstractDatabase;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.ExampleData;
import org.molgenis.model.elements.Model;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

/**
 * Java Persistence API (JPA) implementation of Database to query relational databases.
 * <p>
 * In order to function, {@link org.molgenis.framework.db.JpaMapper} must be added for each
 * {@link org.molgenis.framework.util.Entity} E that can be queried. These mappers take care of the interaction with a
 * database.
 * 
 * @author Morris Swertz
 * @author Joris Lops
 */
public class JpaDatabase extends AbstractDatabase
{
	protected static class EMFactory
	{
		private static Map<String, EntityManagerFactory> emfs = new HashMap<String, EntityManagerFactory>();
		private static EMFactory instance = null;

		private EMFactory(String persistenceUnit, Map<String, Object> configOverrides)
		{
			addEntityManagerFactory(persistenceUnit, configOverrides);
		}

		private static void addEntityManagerFactory(String persistenceUnitName, Map<String, Object> configOverwrite)
		{
			if (!emfs.containsKey(persistenceUnitName))
			{
				EntityManagerFactory emFactory = null;
				if (configOverwrite != null)
				{
					emFactory = Persistence.createEntityManagerFactory(persistenceUnitName, configOverwrite);
				}
				else
				{
					emFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
				}
				emfs.put(persistenceUnitName, emFactory);
			}
		}

		public static EntityManager createEntityManager(String persistenceUnit)
		{
			if (instance == null)
			{
				instance = new EMFactory(persistenceUnit, null);
			}
			if (!emfs.containsKey(persistenceUnit))
			{
				addEntityManagerFactory(persistenceUnit, null);
			}
			EntityManager result = emfs.get(persistenceUnit).createEntityManager();
			return result;
		}

		public static EntityManager createEntityManager()
		{
			if (instance == null)
			{
				instance = new EMFactory("molgenis", null);
			}
			EntityManager result = emfs.get("molgenis").createEntityManager();
			return result;
		}

		public static EntityManagerFactory getEntityManagerFactoryByName(String name)
		{
			return emfs.get(name);
		}

		public static EntityManager createEntityManager(String persistenceUnitName, Map<String, Object> configOverrides)
		{
			if (instance == null)
			{
				instance = new EMFactory(persistenceUnitName, configOverrides);
			}
			if (!emfs.containsKey(persistenceUnitName))
			{
				addEntityManagerFactory(persistenceUnitName, configOverrides);
			}
			EntityManager result = emfs.get(persistenceUnitName).createEntityManager();
			return result;
		}
	}

	private final EntityManager em;
	private String persistenceUnitName = "molgenis"; // default

	protected JpaDatabase(String persistenceUnitName)
	{
		this.persistenceUnitName = persistenceUnitName;
		this.em = EMFactory.createEntityManager();
	}

	public JpaDatabase(EntityManager em, Model model)
	{
		this.em = em;
		this.model = model;
	}

	@Override
	public EntityManager getEntityManager()
	{
		return em;
	}

	@Override
	public void beginTx() throws DatabaseException
	{
		try
		{
			if (em.getTransaction() != null && !em.getTransaction().isActive())
			{
				em.getTransaction().begin();
			}
		}
		catch (Exception e)
		{
			throw new DatabaseException(e);
		}
	}

	@Override
	public boolean inTx()
	{
		return em.getTransaction().isActive();
	}

	@Override
	public void commitTx() throws DatabaseException
	{
		try
		{
			if (em.getTransaction() != null && em.getTransaction().isActive())
			{
				em.getTransaction().commit();
			}
		}
		catch (Exception e)
		{
			throw new DatabaseException(e);
		}
	}

	@Override
	public void rollbackTx() throws DatabaseException
	{
		try
		{
			if (em.getTransaction().isActive())
			{
				em.getTransaction().rollback();
			}
		}
		catch (Exception e)
		{
			throw new DatabaseException(e);
		}
	}

	@Override
	public void close() throws IOException
	{
		try
		{
			em.close();
		}
		catch (IllegalStateException e)
		{
			throw new IOException(e);
		}
	}

	@Override
	public void createTables()
	{
		JpaUtil.dropAndCreateTables(this, null);
	}

	@Override
	public void updateTables()
	{
		JpaUtil.updateTables(this, null);
	}

	@Override
	public void dropTables()
	{
		JpaUtil.dropTables(this, null);
	}

	@Override
	public void loadExampleData(ExampleData exampleData) throws DatabaseException
	{
		exampleData.load(this);
	}

	@Override
	@Deprecated
	public Connection getConnection() throws DatabaseException
	{
		return JpaFrameworkFactory.createFramework().getConnection(em);
	}

	@Override
	public void flush()
	{
		em.flush();
	}

	@SuppressWarnings("rawtypes")
	public List executeSQLQuery(String sqlQuery)
	{
		return em.createNativeQuery(sqlQuery).getResultList();
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> executeSQLQuery(String sqlQuery, Class<T> resultClass)
	{
		return em.createNativeQuery(sqlQuery, resultClass).getResultList();
	}

	public String getPersistenceUnitName()
	{
		return persistenceUnitName;
	}

	public List<Tuple> sql(String sql, String... columnNames)
	{
		javax.persistence.Query q = this.em.createNativeQuery(sql);

		List<Tuple> result = new ArrayList<Tuple>();

		for (Object o : q.getResultList())
		{
			WritableTuple row = new KeyValueTuple();

			if (columnNames.length == 1)
			{
				row.set(columnNames[0], o);
			}
			else
			{
				Object[] arr = (Object[]) o;
				for (int i = 0; i < columnNames.length; i++)
				{
					row.set(columnNames[i], arr[i]);
				}
			}
			result.add(row);
		}
		return result;
	}
}