package org.molgenis.framework.db.jpa;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.molgenis.util.Entity;

/**
 * @author joris lops
 */
class HibernateImp implements JpaFramework
{

	@Override
	public <E extends Entity> List<E> findByExample(EntityManager em, E example)
	{
		Session session = (Session) em.getDelegate();
		Example customerExample = Example.create(example).excludeZeroes();
		Criteria criteria = session.createCriteria(example.getClass()).add(customerExample);
		@SuppressWarnings("unchecked")
		List<E> list = criteria.list();
		return list;
	}

	@Override
	public void createTables(final String persistenceUnitName, final Map<String, Object> configOverwrites)
	{
		final Ejb3Configuration cfg = new Ejb3Configuration();
		cfg.configure(persistenceUnitName, configOverwrites);
		final SchemaExport schemaExport = new SchemaExport(cfg.getHibernateConfiguration());
		schemaExport.setOutputFile("schema.sql");
		schemaExport.create(true, true);
	}

	@Override
	public void updateTables(String persistenceUnitName, final Map<String, Object> configOverwrites)
	{
		final Ejb3Configuration cfg = new Ejb3Configuration();
		cfg.configure(persistenceUnitName, configOverwrites);
		final SchemaUpdate schemaUpdate = new SchemaUpdate(cfg.getHibernateConfiguration());
		schemaUpdate.setOutputFile("schema.sql");
		schemaUpdate.execute(true, true);
	}

	@Override
	public void dropTables(String persistenceUnitName, final Map<String, Object> configOverwrites)
	{
		final Ejb3Configuration cfg = new Ejb3Configuration();
		cfg.configure(persistenceUnitName, configOverwrites);
		final SchemaExport schemaExport = new SchemaExport(cfg.getHibernateConfiguration());
		schemaExport.setOutputFile("schema.sql");
		schemaExport.drop(true, true);
	}

	@Override
	public Connection getConnection(EntityManager em)
	{
		// FIXME ((HibernateEntityManager) em).getSession().connection() removed in
		// Hibernate 4.x
		// More info:
		// http://stackoverflow.com/questions/3526556/session-connection-deprecated-on-hibernate
		throw new UnsupportedOperationException("FIXME: implement");
	}
}
