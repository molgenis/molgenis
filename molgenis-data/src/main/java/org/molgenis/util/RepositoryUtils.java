package org.molgenis.util;

import org.molgenis.data.Countable;
import org.molgenis.data.Repository;

import com.google.common.collect.Iterables;

public class RepositoryUtils
{
	/**
	 * Get the nr of entities in a repository
	 * 
	 * Tries to do it as efficient as possible
	 * 
	 * @param repository
	 * @return
	 */
	public static long count(Repository repository)
	{
		if (repository instanceof Countable)
		{
			return ((Countable) repository).count();
		}

		return Iterables.size(repository);
	}
}
