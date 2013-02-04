package org.molgenis.framework.db.jpa;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.molgenis.util.Entity;

/**
 * 
 * @author joris lops
 */
public interface JpaFramework
{

	public <E extends Entity> List<E> findByExample(EntityManager em, E example);

	/**
	 * Create tables based on annotations.
	 * 
	 * @param persistenceUnitName
	 */
	public void createTables(final String persistenceUnitName, final Map<String, Object> configOverwrites);

	/**
	 * Update tables based on annotations.
	 * 
	 * @param persistenceUnitName
	 */
	public void updateTables(final String persistenceUnitName, final Map<String, Object> configOverwrites);

	/**
	 * Drop tables based on annotations
	 * 
	 * @param persistenceUnitName
	 */
	public void dropTables(final String persistenceUnitName, final Map<String, Object> configOverwrites);

	@Deprecated
	public Connection getConnection(EntityManager em);
}
