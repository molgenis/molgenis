package org.molgenis.data;

/**
 * Source of entities that can be queried
 */
public interface Queryable<E extends Entity>
{
	/**
	 * return number of entities
	 */
	long count();

	/**
	 * return number of entities matched by query
	 **/
	long count(Query q);

	/**
	 * type-safe find entities that match a quer
	 */
	Iterable<E> findAll(Query q);

	/**
	 * type-safe find one entity based on id. Returns null if not exists
	 */
	E findOne(Integer id);

	/**
	 * find entities based on a stream of ids
	 */
	Iterable<E> findAll(Iterable<Integer> ids);
}
