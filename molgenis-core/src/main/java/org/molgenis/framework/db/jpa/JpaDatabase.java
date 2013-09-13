package org.molgenis.framework.db.jpa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.molgenis.framework.db.AbstractDatabase;
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
	public static final String DEFAULT_PERSISTENCE_UNIT_NAME = "molgenis";

	@PersistenceContext
	private EntityManager em;

	public JpaDatabase()
	{
		this(null);
	}

	public JpaDatabase(Model model)
	{
		this.model = model;
	}

	@Override
	public EntityManager getEntityManager()
	{
		return em;
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