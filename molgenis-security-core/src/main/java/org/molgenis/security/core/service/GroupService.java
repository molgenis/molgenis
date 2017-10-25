package org.molgenis.security.core.service;

import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.User;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Manages {@link Group}s and {@link org.molgenis.security.core.model.GroupMembership}.
 */
public interface GroupService
{
	/**
	 * Adds a {@link User} to a {@link Group} effective immediately.
	 *
	 * @param userId  ID of the User to add to the Group
	 * @param groupId ID of the Group to add the User to
	 */
	void addUserToGroup(String userId, String groupId);

	/**
	 * Adds a {@link User} to a {@link Group} effective at some {@link LocalDate} in the future.
	 *
	 * @param userId  ID of the User to add to the Group
	 * @param groupId ID of the Group to add the User to
	 * @param start the first {@link Instant} that the membership becomes effective, must be now or later
	 */
	void addUserToGroup(String userId, String groupId, Instant start);

	/**
	 * Adds a {@link User} to a {@link Group} effective at some {@link Instant} in the future, until a later {@link Instant} in the future.
	 *
	 * @param userId  ID of the User to add to the Group
	 * @param groupId ID of the Group to add the User to
	 * @param start the first {@link LocalDate} that the membership becomes effective, must be today or later
	 * @param end   the last {@link LocalDate} that the membership is effective, must be on or after start
	 */
	void addUserToGroup(String userId, String groupId, Instant start, Instant end);

	/**
	 * Removes a User from a Group, effective immediately.
	 *
	 * @param userId  ID of the User to remove from the Group
	 * @param groupId ID of the Group to remove the User from
	 */
	void removeUserFromGroup(String userId, String groupId);

	/**
	 * Retrieves all {@link GroupMembership}s of a particular {@link User} in past, present and future.
	 *
	 * @param user the User
	 * @return List containing the User's {@link GroupMembership}s
	 */
	List<GroupMembership> getGroupMemberships(User user);

	/**
	 * Retrieves all {@link Group}s a particular {@link User} is currently a member of.
	 *
	 * @param user the {@link User}
	 * @return Set of current {@link Group}s.
	 */
	Set<Group> getCurrentGroups(User user);
}
