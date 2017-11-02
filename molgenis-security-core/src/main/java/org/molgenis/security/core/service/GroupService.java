package org.molgenis.security.core.service;

import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.Role;
import org.molgenis.security.core.model.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Manages {@link Group}s and {@link org.molgenis.security.core.model.GroupMembership}.
 */
public interface GroupService
{
	Optional<Group> findGroupById(String groupId);

	/**
	 * Adds a {@link User} to a {@link Group} effective immediately.
	 *
	 * @param user  the User to add to the Group
	 * @param group the Group to add the User to
	 */
	void addUserToGroup(User user, Group group);

	/**
	 * Adds a {@link User} to a {@link Group} effective at some {@link Instant}.
	 *
	 * @param user  the User to add to the Group
	 * @param group the Group to add the User to
	 * @param start the first {@link Instant} that the membership becomes effective
	 */
	void addUserToGroup(User user, Group group, Instant start);

	/**
	 * Adds a {@link User} to a {@link Group} effective at some {@link Instant}, until a later {@link Instant}.
	 *
	 * @param user  the User to add to the Group
	 * @param group the Group to add the User to
	 * @param start the first {@link Instant} that the membership becomes effective
	 * @param end   the last {@link Instant} that the membership is effective
	 */
	void addUserToGroup(User user, Group group, Instant start, Instant end);

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
	 * Retrieves all {@link GroupMembership}s of a particular {@link Group} in past, present and future.
	 *
	 * @param group the Group
	 * @return List containing the Group's {@link GroupMembership}s
	 */
	List<GroupMembership> getGroupMemberships(Group group);

	/**
	 * Retrieves all {@link Group}s a particular {@link User} is currently a member of.
	 *
	 * @param user the {@link User}
	 * @return Set of current {@link Group}s.
	 */
	Set<Group> getCurrentGroups(User user);

	/**
	 * Creates a parent group with children.
	 *
	 * @param group with roles
	 */
	Group createGroup(Group group);
	/**
	 *
	 *
	 *
	 * @param group
	 * @param role
	 */
	void removeRoleFromGroup(Group group, Role role);

	/**
	 *
	 * @param group
	 * @param role
	 */
	void addRoleToGroup(Group group, Role role);

}
