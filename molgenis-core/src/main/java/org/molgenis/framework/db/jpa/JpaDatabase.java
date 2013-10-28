package org.molgenis.framework.db.jpa;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.molgenis.framework.db.AbstractDatabase;
import org.molgenis.model.elements.Model;

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

}