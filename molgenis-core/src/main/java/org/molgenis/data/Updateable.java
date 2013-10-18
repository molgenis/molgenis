package org.molgenis.data;

/* 
 * Updateable repository
 */
public interface Updateable<E extends Entity>
{
	/* Update one entity */
	void update(E entity);

	/* Streaming update multiple entities */
	void update(Iterable<E> records);

	/* Delete one entity */
	void delete(E entity);

	/* Streaming delete multiple entities */
	void delete(Iterable<E> entities);

	/* Delete one entity based on id */
	void deleteById(Integer id);

	/* Streaming delete based on multiple ids */
	void deleteById(Iterable<Integer> ids);

	/* Delete all entities */
	void deleteAll();
}
