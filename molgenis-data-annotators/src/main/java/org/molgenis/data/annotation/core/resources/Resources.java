package org.molgenis.data.annotation.core.resources;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;

import java.util.Set;

public interface Resources
{
	/**
	 * Indicates if a specific Resource is currently available.
	 *
	 * @param name the name of the {@link Resource}
	 */
	boolean hasRepository(String name);

	/**
	 * Queries a resource.
	 *
	 * @param name the name of the {@link Resource} to query
	 * @param q    the Query to use
	 * @return {@link Entity}s found
	 */
	Iterable<Entity> findAll(String name, Query<Entity> q);

	Set<String> getResourcesNames();
}
