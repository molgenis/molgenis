package org.molgenis.data;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

/**
 * Generic <Entity> version of the Repository api. This alternative api doesn't
 * require extensive use of '<Xyz>'
 */
public interface Repo extends Repository<Entity> {
	/**
	 * type-safe iterate
	 */
	<E extends Entity> Iterable<E> iterator(Class<E> clazz);
}
