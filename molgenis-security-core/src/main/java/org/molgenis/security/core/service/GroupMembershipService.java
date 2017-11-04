package org.molgenis.security.core.service;

import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.User;

import java.util.List;

/**
 * Repository that does simple CRUD operations on {@link GroupMembership}.
 */
public interface GroupMembershipService
{
	/**
	 * Adds {@link GroupMembership}s.
	 *
	 * @param groupMemberships List of {@link GroupMembership}s to add
	 */
	void add(List<GroupMembership> groupMemberships);

	/**
	 * Deletes {@link GroupMembership}s.
	 *
	 * @param groupMemberships List of {@link GroupMembership}s to delete
	 */
	void delete(List<GroupMembership> groupMemberships);

	/**
	 * Retrieves all {@link GroupMembership}s of a particular {@link User} in past, present and future.
	 *
	 * @param user the User
	 * @return List containing the User's {@link GroupMembership}s, sorted by start
	 */
	List<GroupMembership> getGroupMemberships(User user);

	List<GroupMembership> getGroupMemberships(Group group);
}
