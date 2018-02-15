package org.molgenis.bootstrap.populate;

import org.molgenis.data.security.auth.GroupMetaData;
import org.molgenis.data.security.auth.UserMetaData;

/**
 * Populates empty data store with security entities such as {@link UserMetaData users} and {@link GroupMetaData groups}.
 */
public interface UsersGroupsPopulator
{
	/**
	 * Populates an empty data store with users, groups and authorities.
	 */
	void populate();
}