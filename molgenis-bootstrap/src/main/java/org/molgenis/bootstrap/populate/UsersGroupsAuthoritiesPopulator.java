package org.molgenis.bootstrap.populate;

/**
 * Populates empty data store with security entities such as {@link org.molgenis.auth.UserMetaData users},
 * {@link org.molgenis.auth.GroupMetaData groups} and {@link org.molgenis.auth.AuthorityMetaData authorities}.
 */
public interface UsersGroupsAuthoritiesPopulator
{
	/**
	 * Populates an empty data store with users, groups and authorities.
	 */
	void populate();
}