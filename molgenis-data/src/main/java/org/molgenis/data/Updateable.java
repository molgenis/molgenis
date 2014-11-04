package org.molgenis.data;


/* 
 * Updateable repository
 */
public interface Updateable extends Writable
{
	/* Update one entity */
	void update(Entity entity);

	/* Streaming update multiple entities */
	void update(Iterable<? extends Entity> records);

	/* Delete one entity */
	void delete(Entity entity);

	/* Streaming delete multiple entities */
	void delete(Iterable<? extends Entity> entities);

	/* Delete one entity based on id */
	void deleteById(Object id);

	/* Streaming delete based on multiple ids */
	void deleteById(Iterable<Object> ids);

	/* Delete all entities */
	void deleteAll();
}
