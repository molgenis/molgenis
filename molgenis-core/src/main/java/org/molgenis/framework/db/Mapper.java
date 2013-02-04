package org.molgenis.framework.db;

import java.text.ParseException;
import java.util.List;

import org.molgenis.fieldtypes.FieldType;
import org.molgenis.io.TupleReader;
import org.molgenis.io.TupleWriter;
import org.molgenis.util.Entity;

/**
 * Interface for the Molgenis mappers. Mappers are an intermediate between
 * Molgenis and the database and provide many useful helper methods.
 */
public interface Mapper<E extends Entity>
{
	/**
	 * Get the database this mapper is attached to.
	 * 
	 * @return database
	 */
	public Database getDatabase();

	public E create();

	/** Implementation of {@link Database#count(Class, QueryRule...)} */
	public int count(QueryRule... rules) throws DatabaseException;

	/** Implementation of {@link Database#find(Class, QueryRule...)} */
	public List<E> find(QueryRule... rules) throws DatabaseException;

	/** Implementation of {@link Database#find(Class, TupleWriter, QueryRule...)} */
	public void find(TupleWriter writer, QueryRule... rules) throws DatabaseException;

	/**
	 * Implementation of
	 * 
	 */
	public void find(TupleWriter writer, List<String> fieldsToExport, QueryRule[] rules) throws DatabaseException;

	/** Implementation of {@link Database#add(Entity)} */
	// public int add(E entity) throws DatabaseException;

	/** Implementation of {@link Database#add(List)} */
	public int add(List<E> entities) throws DatabaseException;

	/** Implementation of {@link Database#add(Class, TupleReader, TupleWriter)} */
	public int add(TupleReader reader, TupleWriter writer) throws DatabaseException;

	/** Implementation of {@link Database#add(Entity)} */
	// public int update(E entity) throws DatabaseException;

	/** Implementation of {@link Database#update(List)} */
	public int update(List<E> entities) throws DatabaseException;

	/** Implementation of {@link Database#update(TupleReader)} */
	public int update(TupleReader reader) throws DatabaseException;

	/** Implementation of {@link Database#remove(Entity)} */
	// public int remove(E entity) throws DatabaseException;

	/** Implementation of {@link Database#remove(List)} */
	public int remove(List<E> entities) throws DatabaseException;

	/** Implementation of */
	public int remove(TupleReader reader) throws DatabaseException;

	public List<E> toList(TupleReader reader, int limit) throws DatabaseException;

	public String getTableFieldName(String field);

	public FieldType getFieldType(String field);

	public void resolveForeignKeys(List<E> enteties) throws ParseException, DatabaseException;

	public String createFindSqlInclRules(QueryRule[] rules) throws DatabaseException;

	public E findById(Object id) throws DatabaseException;

	List<E> findByExample(E example) throws DatabaseException;

	int executeAdd(List<? extends E> entities) throws DatabaseException;

	int executeUpdate(List<? extends E> entities) throws DatabaseException;

	int executeRemove(List<? extends E> entities) throws DatabaseException;

	public List<E> createList(int i);
}
