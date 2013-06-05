package org.molgenis.mock;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javax.persistence.EntityManager;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.SubQueryRule;
import org.molgenis.framework.security.Login;
import org.molgenis.io.TupleReader;
import org.molgenis.io.TupleWriter;
import org.molgenis.model.elements.Model;
import org.molgenis.util.Entity;

/**
 * Mock Database implementation for use in unittests. Use setEntities for define entities to be returned by the query
 * and find methods
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

	@Override
	public <E extends Entity> E findById(Class<E> entityClass, Object id) throws DatabaseException
	{
		return null;
	}

	@Override
	public <E extends Entity> Query<E> query(Class<E> entityClass)
	{
		return new Query<E>()
		{

			@Override
			public Query<E> filter(String filter)
			{
				return this;
			}

			@Override
			public Query<E> equals(String field, Object value)
			{
				return this;
			}

			@Override
			public Query<E> eq(String field, Object value)
			{
				return this;
			}

			@Override
			public Query<E> search(String searchTerms) throws DatabaseException
			{
				return this;
			}

			@Override
			public Query<E> in(String field, List<?> objectList)
			{
				return this;
			}

			@Override
			public Query<E> greater(String field, Object value)
			{
				return this;
			}

			@Override
			public Query<E> gt(String field, Object value)
			{
				return this;
			}

			@Override
			public Query<E> greaterOrEqual(String field, Object value)
			{
				return this;
			}

			@Override
			public Query<E> less(String field, Object value)
			{
				return this;
			}

			@Override
			public Query<E> lt(String field, Object value)
			{
				return this;
			}

			@Override
			public Query<E> lessOrEqual(String field, Object value)
			{
				return this;
			}

			@Override
			public Query<E> like(String field, Object value)
			{
				return this;
			}

			@Override
			public Query<E> between(String field, Object min, Object max)
			{
				return this;
			}

			@Override
			public Query<E> or()
			{
				return this;
			}

			@Override
			public Query<E> and()
			{
				return this;
			}

			@Override
			public Query<E> last()
			{
				return this;
			}

			@Override
			public Query<E> limit(int limit)
			{
				return this;
			}

			@Override
			public Query<E> offset(int offset)
			{
				return this;
			}

			@Override
			public Query<E> sortASC(String orderByField)
			{
				return this;
			}

			@Override
			public Query<E> sortDESC(String orderByField)
			{
				return this;
			}

			@SuppressWarnings("unchecked")
			@Override
			public List<E> find() throws DatabaseException
			{
				return entities;
			}

			@Override
			public void find(TupleWriter writer) throws DatabaseException, ParseException
			{
			}

			@Override
			public void find(TupleWriter writer, List<String> fieldsToExport) throws DatabaseException, ParseException
			{

			}

			@Override
			public void find(TupleWriter writer, boolean skipAutoIds) throws DatabaseException, ParseException,
					InstantiationException, IllegalAccessException
			{
			}

			@SuppressWarnings("unchecked")
			@Override
			public List<E> find(Database db, Class<E> klazz) throws DatabaseException, ParseException
			{
				return entities;
			}

			@Override
			public int count() throws DatabaseException
			{
				return entities.size();
			}

			@Override
			public int count(Database db, Class<E> klazz) throws DatabaseException
			{
				return entities.size();
			}

			@Override
			public QueryRule[] getRules()
			{
				return null;
			}

			@Override
			public void addRules(QueryRule... addRules)
			{
			}

			@Override
			public void setDatabase(Database db)
			{
			}

			@Override
			public Database getDatabase()
			{
				return null;
			}

			@Override
			public Query<E> example(Entity example)
			{
				return this;
			}

			@Override
			public void removeRule(QueryRule ruleToBeRemoved)
			{
			}

			@Override
			public String createFindSql() throws DatabaseException
			{
				return null;
			}

			@Override
			public Query<E> subquery(String field, String sql)
			{
				return this;
			}

			@Override
			public Query<E> subQuery(SubQueryRule subQueryRule)
			{
				return this;
			}

		};
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
	public <E extends Entity> String createFindSql(Class<E> entityClass, QueryRule... rules) throws DatabaseException
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
