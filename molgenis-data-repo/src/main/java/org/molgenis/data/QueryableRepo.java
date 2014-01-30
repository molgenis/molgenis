package org.molgenis.data;

public interface QueryableRepo
{
	public long count(Query q);

	public Entity findOne(Query q);
	
	public Entity findOne(Integer id);
	
	public Iterable<Entity> findAll(Query q);
	
	public Iterable<Entity> findAll(Iterable<Integer> ids);
	
	public long count();
	
	/**
	 * type-safe find entities that match a query
	 */
	<E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz);

	/**
	 * type-safe find entities that match a stream of ids
	 */
	<E extends Entity> Iterable<E> findAll(Iterable<Integer> ids, Class<E> clazz);
}
