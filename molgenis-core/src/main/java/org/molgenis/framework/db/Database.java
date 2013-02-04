/**
 * File: org.molgenis.framework.data.Database Copyright: Inventory 2000-2007,
 * GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-05-03; 1.0.0; MA Swertz; Creation.
 * <li>2005-11-18; 1.0.0; MA Swertz; Documentation
 * <li>2005-11-29; 1.0.0; RA Scheltema; Merge from the old java invengine-style
 * to the new, added documentation. Furthermore: 1. Changed the interface for
 * the update-function (now it's possible to change identifying values for an
 * entity). 2. Introduced the TDGMessage-class for returning messages for the
 * add and update functions.
 * <li>2007-04-19; 2.0.0; MA Swertz; moved to database (a facade around
 * mappers).
 * </ul>
 */
package org.molgenis.framework.db;

import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.text.ParseException;
import java.util.List;

import javax.persistence.EntityManager;

import org.molgenis.framework.security.Login;
import org.molgenis.io.TupleReader;
import org.molgenis.io.TupleWriter;
import org.molgenis.model.elements.Model;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.Tuple;

/**
 * Interface to manage and search persistent data Entity objects. It wraps basic
 * functionality such as find, count, add, update and delete. It also has batch
 * functions to add data in vast amount, especially added to support large scale
 * data storage such as needed in life sciences. Furthermore, it has transaction
 * capabilities, i.e., to add, update, and remove many entities as if it was one
 * operation (ensuring all actions are complete succesfully or if it fails
 * halfway, it gets undone).
 */
public interface Database extends Closeable
{
	/**
	 * Create tables.
	 * 
	 * @throws DatabaseException
	 */
	public void createTables() throws DatabaseException;

	/**
	 * Update tables.
	 * 
	 * @throws DatabaseException
	 */
	public void updateTables() throws DatabaseException;

	/**
	 * Drop tables.
	 * 
	 * @throws DatabaseException
	 */
	public void dropTables() throws DatabaseException;

	/**
	 * Load example data into database.
	 * 
	 * @throws DatabaseException
	 */
	public void loadExampleData(ExampleData exampleData) throws DatabaseException;

	/**
	 * Retrieve meta data describing data structure in this Database.
	 * 
	 * @return MOLGENIS meta model used to generate this database
	 * @throws DatabaseException
	 */
	public Model getMetaData() throws DatabaseException;

	/**
	 * Begin transaction.
	 * <p>
	 * All additions, updates and removals will be temporary until commitTx is
	 * called. This ensures that all updates are processed as one action, or
	 * that all changes are rolled back, thus ensuring consistent database
	 * state.
	 * 
	 * @throws DatabaseException
	 */
	public void beginTx() throws DatabaseException;

	/**
	 * Check whether the database is currently in a transaction. Returns true if
	 * beginTx() was called before.
	 * 
	 * @return true if in transaction
	 */
	public boolean inTx();

	/**
	 * Commit transaction.
	 * <p>
	 * Make all additions, updates and removals that have been done since
	 * beginTx permanent in the database. This may fail if another user has made
	 * conflicting changes since your transaction was started.
	 * 
	 * @throws DatabaseException
	 */
	public void commitTx() throws DatabaseException;

	/**
	 * Rollback transaction.
	 * <p>
	 * All additions, updates and removals that have been executed since beginTx
	 * are made undone. Requires beginTx to be called first otherwise an
	 * Exception is thrown.
	 * 
	 * @throws DatabaseException
	 */
	public void rollbackTx() throws DatabaseException;

	/**
	 * Count the entities of type entityClass. Optionally, additional filtering
	 * rules can be set. The rules are passed via a variable parameter-list,
	 * which can handle [0 .. n] parameters.
	 * 
	 * @param entityClass
	 *            to query
	 * @param rules
	 *            to filter or otherwise change the result
	 * @return count of entities.
	 */
	public <E extends Entity> int count(Class<E> entityClass, QueryRule... rules) throws DatabaseException;

	/**
	 * Find all entities of type entityClass and return them as list. Optionally
	 * filtering rules can be provided.
	 * 
	 * @param <E>
	 *            type of entity to be retrieved
	 * @param entityClass
	 *            type of entity to be retrieved
	 * @param rules
	 *            to filter or otherwise change result
	 * @return List of entity objects.
	 * @throws ParseException
	 *             as result of incompatible QueryRules
	 */
	public <E extends Entity> List<E> find(Class<E> klazz, QueryRule... rules) throws DatabaseException;

	/**
	 * Find all entities of type entityClass and write them to a csv file.
	 * 
	 * Optionally the records can be filtered using QueryRule rules.
	 * 
	 * @param <E>
	 *            type of entity
	 * @param entityClass
	 *            clazz of entity
	 * @param writer
	 *            to write entities found to
	 * @param rules
	 *            to filter or otherwise change result
	 * @throws DatabaseException
	 */
	public <E extends Entity> void find(Class<E> entityClass, TupleWriter writer, QueryRule... rules)
			throws DatabaseException;

	/**
	 * Find all entities of type entityClass and write them to a csv file.
	 * 
	 * Optionally the records can be filtered using QueryRule rules.
	 * 
	 * Optionally the auto id's are not exported.
	 * 
	 */
	public <E extends Entity> void find(Class<E> entityClass, TupleWriter writer, List<String> fieldsToExport,
			QueryRule... rules) throws DatabaseException;

	/**
	 * Find all entity objects matching the not-null properties of one example
	 * object.
	 * 
	 * @param <E>
	 *            type of entity
	 * @param example
	 *            object which not-null properties will be used as QueryRules
	 *            for search
	 * @return list of entities that match the example
	 * @throws DatabaseException
	 */
	public <E extends Entity> List<E> findByExample(E example) throws DatabaseException;

	/**
	 * Find one entity object of type entityClass by using its primary id.
	 * 
	 * @param <E>
	 * @param entityClass
	 * @param id
	 *            of the object
	 * @return entity object or null of not found.
	 * @throws DatabaseException
	 */
	public <E extends Entity> E findById(Class<E> entityClass, Object id) throws DatabaseException;

	/**
	 * Create a Query to easily search the entities of type entityClass.
	 * 
	 * @see Query
	 * 
	 * @param <E>
	 *            type of entity
	 * @param entityClass
	 *            class of entity
	 * @return query object for this entityClass. Optionally one can add
	 *         additional filtering rules on this Query.
	 */
	public <E extends Entity> Query<E> query(Class<E> entityClass);

	/**
	 * Create a Query to easily to search the entities by an example.
	 * 
	 * @see Query
	 * 
	 * @param <E>
	 *            type of entity
	 * @param entityClass
	 *            class of entity
	 * @return query object for this entityClass. Optionally one can add
	 *         additional filtering rules on this Query.
	 */
	public <E extends Entity> Query<E> queryByExample(E entity);

	/**
	 * Add one entity object to the database.
	 * 
	 * @param <E>
	 *            type of entity
	 * @param entity
	 *            to be added.
	 * @return number of entity objects that have added.
	 */
	public <E extends Entity> int add(E entity) throws DatabaseException;

	/**
	 * Add a list of entity objects in batch to the database
	 * 
	 * @param <E>
	 *            type of entity
	 * @param entities
	 *            to be added
	 * @return number of entity objects that have been added
	 */
	public <E extends Entity> int add(List<E> entities) throws DatabaseException;

	/**
	 * Add a list of entity objects to the database by parsing them from a csv
	 * file. Optionally the inserted records can be written back to another csv
	 * file, for example to extract auto-generated keys.
	 * 
	 * @param <E>
	 * @param klazz
	 * @param reader
	 * @param writer
	 *            to write the result onto (optional)
	 * @return number of entities added
	 * @throws Exception
	 */
	public <E extends Entity> int add(Class<E> klazz, TupleReader reader, TupleWriter writer) throws DatabaseException;

	/**
	 * Add a list of entity objects to the database by parsing them from a csv
	 * file. Optionally the inserted records can be written back to another csv
	 * file, for example to extract auto-generated keys.
	 * 
	 * @param <E>
	 * @param klazz
	 * @param reader
	 * @return number of entities added
	 * @throws Exception
	 */
	public <E extends Entity> int add(Class<E> klazz, TupleReader reader) throws DatabaseException;

	/**
	 * Update one entity object in the database. In JPA the entity will be
	 * automatically merged if not attached to EntityManager, and the xref/mref
	 * proxies set based on xref_ids.
	 * 
	 * @TODO <p>
	 *       Note: each entity has a "primary key" that cannot be updated. If
	 *       you want to change a primary key, you have to remove the previous,
	 *       and add the new record. Otherwise this will result in a
	 *       DatabaseException or unexpected behaviour.
	 * 
	 * @param <E>
	 *            type of entity
	 * @param entity
	 *            The entity which needs to be updated in the database.
	 */
	public <E extends Entity> int update(E entity) throws DatabaseException;

	/**
	 * Update a list of entity objects in batch from the database. In JPA the
	 * entity will be automatically merged if not attached to EntityManager, and
	 * the xref/mref proxies set based on xref_ids.
	 * 
	 * @param <E>
	 *            type of entity
	 * @param entities
	 *            to be updated
	 */
	public <E extends Entity> int update(List<E> entities) throws DatabaseException;

	/**
	 * Update a list of entity objects in the database by reading the new values
	 * from a csv file.
	 * 
	 * @param <E>
	 *            type of entity
	 * @param klazz
	 *            specifying type of data
	 * @param reader
	 *            of csv file
	 * @return number of entities update
	 * @throws Exception
	 */
	public <E extends Entity> int update(Class<E> klazz, TupleReader reader) throws DatabaseException;

	/**
	 * Remove one particular entity from the database (and remove from
	 * EntityManager cache).
	 * 
	 * @param <E>
	 *            type of entity
	 * @param entity
	 *            to be removed.
	 */
	public <E extends Entity> int remove(E entity) throws DatabaseException;

	/**
	 * Remove a list of entity objects in batch from the database (and remove
	 * from EntityManager cache).
	 * 
	 * @param <E>
	 *            type of entity
	 * @param entities
	 *            to be removed
	 */
	public <E extends Entity> int remove(List<E> entities) throws DatabaseException;

	/**
	 * Remove a list of entity objects from the database by parsing the
	 * to-be-removed data from a csv file.
	 * 
	 * @param <E>
	 *            type of entity
	 * @param entityClass
	 *            type of entity objects in the csv file
	 * @param reader
	 *            of csv file
	 * @return number of entities that have been removed
	 * @throws Exception
	 */
	public <E extends Entity> int remove(Class<E> entityClass, TupleReader reader) throws DatabaseException;

	/**
	 * Enumeration of complex database update actions supported by updateByName
	 */
	public enum DatabaseAction
	{
		/** add records , error on duplicate records */
		ADD,
		/** add, ignore existing records */
		ADD_IGNORE_EXISTING,
		/** add, update existing records */
		ADD_UPDATE_EXISTING,
		/**
		 * update records, throw an error if records are missing in the database
		 */
		UPDATE,
		/** update records, ignore missing records */
		UPDATE_IGNORE_MISSING,
		/**
		 * remove records in the list from database; throw an exception of
		 * records are missing in the database
		 */
		REMOVE,
		/** remove records in the list from database; ignore missing records */
		REMOVE_IGNORE_MISSING,
	};

	/**
	 * Flexible update function that selectively updates the database using
	 * (composite) key fields of your choice and by mixing adds, updates and/or
	 * removes.
	 * 
	 * @see DatabaseAction
	 * @param entities
	 *            list of entity objects
	 * @param dbAction
	 *            the action to use. For example: ADD_UPDATE_EXISTING
	 * @param keyName
	 *            key field name, or list of composite key fields, you want to
	 *            use. For example: experiment, name
	 */
	public <E extends Entity> int update(List<E> entities, DatabaseAction dbAction, String... keyName)
			throws DatabaseException;

	/**
	 * Get the path to the file directory that this database uses to store file
	 * attachments. In a MOLGENIS model these fields are specified as &lt;field
	 * type="file" ... &gt;
	 * 
	 * @throws Exception
	 */
	public File getFilesource() throws Exception;

	/**
	 * Return a list of the classes of the entities managed.
	 * 
	 * @return list of entity classes managed in this database
	 */
	public List<Class<? extends Entity>> getEntityClasses();

	/**
	 * Get a list of entities managed in this database.
	 * 
	 * @return list of entity names that are managed in this database
	 */
	public List<String> getEntityNames();

	/**
	 * Read data elements from a csv file and convert them into a list of entity
	 * objects. Optionally limit the number of elements to read.
	 * 
	 * @param <E>
	 * @param klazz
	 * @param reader
	 *            of csv file
	 * @param noEntities
	 *            limit
	 * @return list of entity objects of type=klazz
	 * @throws Exception
	 */
	public <E extends Entity> List<E> toList(Class<E> klazz, TupleReader reader, int noEntities)
			throws DatabaseException;

	/**
	 * Return the security strategy object that takes care of authorization in
	 * this Database.
	 * 
	 * Deprecated, use getLogin() instead
	 */
	public Login getLogin();

	/**
	 * Set the Login.
	 * 
	 * @param login
	 */
	public void setLogin(Login login);

	/**
	 * Retrieve the full class object for an entity name. For example:
	 * "Experiment" may produce a "my.package.Experiment" class. This works
	 * because MOLGENIS requires unique Class names (ignoring package names).
	 * 
	 * @param simpleName
	 *            of a class without packages.
	 * @return entity class
	 */
	public Class<? extends Entity> getClassForName(String simpleName);

	/**
	 * Get the entityManager, if JPA isn't supported a UnsupportedOperations
	 * exceptions is thrown.
	 * 
	 * Deprecated: Database should become entityManager itself ;-)
	 * 
	 * @return EntityManager
	 */
	public EntityManager getEntityManager();

	public void flush();

	/**
	 * Executes a query and get back a List of (Molgenis)Tuples
	 * 
	 * @return List<Tuple>
	 */
	public List<Tuple> sql(String query, QueryRule... queryRules) throws DatabaseException;

	/**
	 * Generate the find SQL (use with caution!)
	 */
	public <E extends Entity> String createFindSql(Class<E> entityClass, QueryRule... rules) throws DatabaseException;

	public Connection getConnection() throws DatabaseException;

	public <E extends Entity> Mapper<E> getMapper(String name) throws DatabaseException;

	/**
	 * Find the mapper from this.mappers
	 * 
	 * @param klazz
	 *            the entity class to get the mapper from
	 * @return a mapper or a exception
	 * @throws DatabaseException
	 */
	public <E extends Entity> Mapper<E> getMapperFor(Class<E> klazz) throws DatabaseException;

	/**
	 * Generic search of database based on a search string. Searches string and
	 * text fields using 'LIKE'. Can be very expensive.
	 */
	public <E extends Entity> List<E> search(Class<E> entityClass, String searchString) throws DatabaseException;

	/**
	 * Convert superclass entities into their proper subclass types, to get the
	 * the subclass specific fields. Requires the original superclass to compare
	 * against: if the entity class is the same, no requery is needed. If the
	 * entity is not a subclass, requerying is meaningless.
	 * 
	 * @param superClass
	 *            the original superclass that was queried and now needs
	 *            refining
	 * @param entities
	 *            the list of entities that may contain subclasses of the
	 *            original superclass and are now requeried in their proper type
	 *            to get all of the subclass fields
	 * @return the list of resulting refined entities
	 * @throws DatabaseException
	 */
	public <E extends Entity> List<? extends Entity> load(Class<E> superClass, List<E> entities)
			throws DatabaseException;

	public <E extends Entity> Class<E> getEntityClass(E entity);

	public <E extends Entity> Class<E> getEntityClass(List<E> entities);
}