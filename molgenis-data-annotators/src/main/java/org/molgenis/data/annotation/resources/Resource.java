package org.molgenis.data.annotation.resources;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;

/**
 * A Resource that can be queried and that may be unavailable. Used to annotate entities.
 */
public interface Resource
{
	/**
	 * @return indication if this {@link Resource} is currently available
	 */
	boolean isAvailable();

	/**
	 * @return the name of the {@link Resource}
	 */
	String getName();

	/**
	 * Queries this Resource
	 * 
	 * @param q
	 *            the Query to use
	 * @return the {@link Entity}s found
	 */
	Iterable<Entity> findAll(Query q);

	boolean needsRefresh();
}
