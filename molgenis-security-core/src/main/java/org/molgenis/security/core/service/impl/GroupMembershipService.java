package org.molgenis.security.core.service.impl;

import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.User;

import java.util.List;

/**
 * Repository that does simple CRUD operations on {@link GroupMembership}.
 */
public interface GroupMembershipService
{
	/**
	 * Retrieves all {@link GroupMembership}s of a particular {@link User} in past, present and future.
	 *
	 * @param user the User
	 * @return List containing the User's {@link GroupMembership}s
	 */
	List<GroupMembership> getGroupMemberships(User user);
}
