package org.molgenis.data;

import java.util.List;

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
	void deleteById(Integer id);

	/* Streaming delete based on multiple ids */
	void deleteById(Iterable<Integer> ids);

	/* Delete all entities */
	void deleteAll();

	/**
	 * Flexible update function that selectively updates the database using (composite) key fields of your choice and by
	 * mixing adds, updates and/or removes.
	 * 
	 * @see DatabaseAction
	 * @param entities
	 *            list of entity objects
	 * @param dbAction
	 *            the action to use. For example: ADD_UPDATE_EXISTING
	 * @param keyName
	 *            key field name, or list of composite key fields, you want to use. For example: experiment, name
	 */
	void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName);
}
