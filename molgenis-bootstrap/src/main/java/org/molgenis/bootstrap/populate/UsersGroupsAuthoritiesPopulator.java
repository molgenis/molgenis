package org.molgenis.bootstrap.populate;

/**
 * Populates empty data store with security entities such as {@link org.molgenis.data.security.model.UserMetadata users},
 * {@link org.molgenis.data.security.model.GroupMetadata groups} and {@link org.molgenis.data.security.model.RoleMetadata roles}.
 */
public interface UsersGroupsAuthoritiesPopulator
{
	/**
	 * Populates an empty data store with users, groups and authorities.
	 */
	void populate();
}