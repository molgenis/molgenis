package org.molgenis.bootstrap.populate;

import org.molgenis.data.security.auth.AuthorityMetaData;
import org.molgenis.data.security.auth.GroupMetaData;
import org.molgenis.data.security.auth.UserMetaData;

/**
 * Populates empty data store with security entities such as {@link UserMetaData users},
 * {@link GroupMetaData groups} and {@link AuthorityMetaData authorities}.
 */
public interface UsersGroupsAuthoritiesPopulator
{
	/**
	 * Populates an empty data store with users, groups and authorities.
	 */
	void populate();
}