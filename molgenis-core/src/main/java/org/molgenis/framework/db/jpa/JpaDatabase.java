package org.molgenis.framework.db.jpa;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.molgenis.framework.db.AbstractDatabase;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.ExampleData;
import org.molgenis.model.elements.Model;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

/**
 * Java Persistence API (JPA) implementation of Database to query relational
 * databases.
 * <p>
 * In order to function, {@link org.molgenis.framework.db.JpaMapper} must be
 * added for each {@link org.molgenis.framework.util.Entity} E that can be
 * queried. These mappers take care of the interaction with a database.
 * 
 * @author Morris Swertz
 * @author Joris Lops
 */
public class JpaDatabase extends AbstractDatabase
{
	public static final String DEFAULT_PERSISTENCE_UNIT_NAME = "molgenis";

	private final EntityManager em;

	public JpaDatabase(EntityManager em)
	{
		this(em, null);
	}

	public JpaDatabase(EntityManager em, Model model)
	{
		if (em == null) throw new IllegalArgumentException("entity manager is null");
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
		return DEFAULT_PERSISTENCE_UNIT_NAME;
	}

	@Deprecated
	public List<Tuple> sql2(String sql, String... columnNames)
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