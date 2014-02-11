package org.molgenis.data;

/**
 * Source of entities that can be queried
 */
public interface Queryable extends Countable
{
	/**
	 * return number of entities matched by query
	 **/
	long count(Query q);

	/**
	 * type-safe find entities that match a query
	 */
	Iterable<Entity> findAll(Query q);

	/**
	 * type-safe find entities that match a query
	 */
	<E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz);

	/**
	 * Find an entity base on a query
	 * 
	 * Returns null if not exists.
	 * 
	 * Returns first result if multiple found
	 */
	Entity findOne(Query q);

	/**
	 * type-safe find one entity based on id. Returns null if not exists
	 */
	Entity findOne(Integer id);

	/**
	 * find entities based on a stream of ids
	 */
	Iterable<Entity> findAll(Iterable<Integer> ids);

	/**
	 * type-safe find entities that match a stream of ids
	 */
	<E extends Entity> Iterable<E> findAll(Iterable<Integer> ids, Class<E> clazz);

	<E extends Entity> E findOne(Integer id, Class<E> clazz);

	/**
	 * type-save find an entity by it's id
	 */
	<E extends Entity> E findOne(Query q, Class<E> clazz);
}
