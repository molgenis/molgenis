package org.molgenis.framework.db.jpa;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.db.AbstractMapper;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.io.TupleWriter;
import org.molgenis.util.Entity;

/**
 * JPA implementation of the Mapper interface.
 */
public abstract class AbstractJpaMapper<E extends Entity> extends AbstractMapper<E>
{
	public AbstractJpaMapper(Database database)
	{
		super(database);
	}

	@Override
	public abstract E create();

	@SuppressWarnings("unchecked")
	public Class<E> getEntityClass()
	{
		return (Class<E>) create().getClass();
	}

	@Override
	public abstract String getTableFieldName(String field);

	@Override
	public abstract FieldType getFieldType(String field);

	@Override
	public abstract void resolveForeignKeys(List<E> enteties) throws ParseException, DatabaseException;

	@Override
	public abstract String createFindSqlInclRules(QueryRule[] rules) throws DatabaseException;

	@Override
	public int count(QueryRule... rules) throws DatabaseException
	{
		TypedQuery<Long> query = JPAQueryGeneratorUtil.createCount(getDatabase(), getEntityClass(), this, getDatabase()
				.getEntityManager(), rules);
		Long result = query.getSingleResult();
		return result.intValue();
	}

	@Override
	public List<E> find(QueryRule... rules) throws DatabaseException
	{
		TypedQuery<E> query = JPAQueryGeneratorUtil.createQuery(this.getDatabase(), getEntityClass(), this,
				getDatabase().getEntityManager(), rules);
		return query.getResultList();
	}

	@Override
	public E findById(Object id)
	{
		return getDatabase().getEntityManager().find(getEntityClass(), id);
	}

	@Override
	public List<E> findByExample(E example)
	{
		return JpaFrameworkFactory.createFramework().findByExample(getEntityManager(), example);
	}

	public EntityManager getEntityManager()
	{
		return getDatabase().getEntityManager();
	}

	@Override
	public void storeMrefs(List<E> entities) throws DatabaseException, IOException, ParseException
	{
		// automatically done by JPA
	}

	@Override
	public void removeMrefs(List<E> entities) throws SQLException, IOException, DatabaseException, ParseException
	{
		// automatically done by JPA
	}

	@Override
	public void find(TupleWriter writer, List<String> fieldsToExport, QueryRule[] rules) throws DatabaseException
	{
		throw new UnsupportedOperationException("not implemented");
	}

}
