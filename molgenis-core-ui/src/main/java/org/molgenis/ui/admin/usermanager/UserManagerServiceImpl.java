package org.molgenis.ui.admin.usermanager;

import org.molgenis.security.core.service.GroupService;
import org.molgenis.security.core.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Service for the {@link UserManagerController}.
 */
@Service
public class UserManagerServiceImpl implements UserManagerService
{

	private final UserService userService;
	private final GroupService groupService;

	public UserManagerServiceImpl(UserService userService, GroupService groupService)
	{
		this.userService = requireNonNull(userService);
		this.groupService = requireNonNull(groupService);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public List<UserViewData> getAllUsers()
	{
		return userService.getAllUsers()
						  .stream()
						  .map(user -> UserViewData.create(user, groupService.getCurrentGroups(user)))
						  .collect(toList());
	}
}
