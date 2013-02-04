package org.molgenis.mock;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import javax.persistence.EntityManager;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.ExampleData;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.security.Login;
import org.molgenis.io.TupleReader;
import org.molgenis.io.TupleWriter;
import org.molgenis.model.elements.Model;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.Tuple;

/**
 * Mock Database implementation for use in unittests. Use setEntities for define
 * entities to be returned by the query and find methods
 * 
 * Implement more methods if you need them.
 * 
 * @author erwin
 * 
 */
public class MockDatabase implements Database
{
	@SuppressWarnings("rawtypes")
	private List entities;
	private Model metaData;
	private boolean inTransaction = false;
	private Login login;

	public MockDatabase()
	{
		super();
	}

	/**
	 * @param entities
	 *            , to be returned by the find and query methods
	 */
	public MockDatabase(@SuppressWarnings("rawtypes")
	List entities)
	{
		super();
		this.entities = entities;
	}

	public MockDatabase(Model metaData)
	{
		super();
		this.metaData = metaData;
	}

	/**
	 * Entities to be returned by the find and query methods
	 * 
	 * @param entities
	 */
	public void setEntities(List<? extends Entity> entities)
	{
		this.entities = entities;
	}

	@Override
	public void createTables() throws DatabaseException
	{
	}

	@Override
	public void updateTables() throws DatabaseException
	{
	}

	@Override
	public void dropTables() throws DatabaseException
	{
	}

	@Override
	public void loadExampleData(ExampleData exampleData) throws DatabaseException
	{
	}

	public void setMetaData(Model metaData)
	{
		this.metaData = metaData;
	}

	@Override
	public Model getMetaData() throws DatabaseException
	{
		return metaData;
	}

	@Override
	public void beginTx() throws DatabaseException
	{
		inTransaction = true;
	}

	@Override
	public boolean inTx()
	{
		return inTransaction;
	}

	@Override
	public void commitTx() throws DatabaseException
	{
		inTransaction = false;
	}

	@Override
	public void rollbackTx() throws DatabaseException
	{
		inTransaction = false;
	}

	@Override
	public <E extends Entity> int count(Class<E> entityClass, QueryRule... rules) throws DatabaseException
	{
		return entities == null ? 0 : entities.size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> List<E> find(Class<E> klazz, QueryRule... rules) throws DatabaseException
	{
		return entities;
	}

	@Override
	public <E extends Entity> void find(Class<E> entityClass, TupleWriter writer, QueryRule... rules)
			throws DatabaseException
	{

	}

	@Override
	public <E extends Entity> void find(Class<E> entityClass, TupleWriter writer, List<String> fieldsToExport,
			QueryRule... rules) throws DatabaseException
	{

	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> List<E> findByExample(E example) throws DatabaseException
	{
		return entities;
	}

	@Override
	public <E extends Entity> E findById(Class<E> entityClass, Object id) throws DatabaseException
	{
		return null;
	}

	@Override
	public <E extends Entity> Query<E> query(Class<E> entityClass)
	{
		return null;
	}

	@Override
	public <E extends Entity> Query<E> queryByExample(E entity)
	{
		return null;
	}

	@Override
	public <E extends Entity> int add(E entity) throws DatabaseException
	{
		return 0;
	}

	@Override
	public <E extends Entity> int add(List<E> entities) throws DatabaseException
	{
		return 0;
	}

	@Override
	public <E extends Entity> int add(Class<E> klazz, TupleReader reader, TupleWriter writer) throws DatabaseException
	{
		return 0;
	}

	@Override
	public <E extends Entity> int add(Class<E> klazz, TupleReader reader) throws DatabaseException
	{
		return 0;
	}

	@Override
	public <E extends Entity> int update(E entity) throws DatabaseException
	{
		return 0;
	}

	@Override
	public <E extends Entity> int update(List<E> entities) throws DatabaseException
	{
		return 0;
	}

	@Override
	public <E extends Entity> int update(Class<E> klazz, TupleReader reader) throws DatabaseException
	{
		return 0;
	}

	@Override
	public <E extends Entity> int remove(E entity) throws DatabaseException
	{
		return 0;
	}

	@Override
	public <E extends Entity> int remove(List<E> entities) throws DatabaseException
	{
		return 0;
	}

	@Override
	public <E extends Entity> int remove(Class<E> entityClass, TupleReader reader) throws DatabaseException
	{
		return 0;
	}

	@Override
	public <E extends Entity> int update(List<E> entities, DatabaseAction dbAction, String... keyName)
			throws DatabaseException
	{
		return 0;
	}

	@Override
	public File getFilesource() throws Exception
	{
		return null;
	}

	@Override
	public void close() throws IOException
	{

	}

	@Override
	public List<Class<? extends Entity>> getEntityClasses()
	{
		return null;
	}

	@Override
	public List<String> getEntityNames()
	{
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> List<E> toList(Class<E> klazz, TupleReader reader, int noEntities)
			throws DatabaseException
	{
		return entities;
	}

	@Override
	public Login getLogin()
	{
		return login;
	}

	@Override
	public void setLogin(Login login)
	{
		this.login = login;
	}

	@Override
	public Class<? extends Entity> getClassForName(String simpleName)
	{
		return null;
	}

	@Override
	public EntityManager getEntityManager()
	{
		return null;
	}

	@Override
	public void flush()
	{
	}

	@Override
	public List<Tuple> sql(String query, QueryRule... queryRules) throws DatabaseException
	{
		return null;
	}

	@Override
	public <E extends Entity> String createFindSql(Class<E> entityClass, QueryRule... rules) throws DatabaseException
	{
		return null;
	}

	@Override
	public Connection getConnection() throws DatabaseException
	{
		return null;
	}

	@Override
	public <E extends Entity> Mapper<E> getMapper(String name) throws DatabaseException
	{
		return null;
	}

	@Override
	public <E extends Entity> Mapper<E> getMapperFor(Class<E> klazz) throws DatabaseException
	{
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> List<E> search(Class<E> entityClass, String searchString) throws DatabaseException
	{
		return entities;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> List<? extends Entity> load(Class<E> superClass, List<E> entities)
			throws DatabaseException
	{
		return this.entities;
	}

	@Override
	public <E extends Entity> Class<E> getEntityClass(E entity)
	{
		return null;
	}

	@Override
	public <E extends Entity> Class<E> getEntityClass(List<E> entities)
	{
		return null;
	}
}
