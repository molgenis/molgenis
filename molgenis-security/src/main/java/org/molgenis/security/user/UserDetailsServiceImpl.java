package org.molgenis.security.user;

import com.google.common.collect.Sets;
import org.molgenis.security.core.model.Group;
import org.molgenis.security.core.model.GroupMembership;
import org.molgenis.security.core.model.Role;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.service.GroupService;
import org.molgenis.security.core.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.concat;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;

public class UserDetailsServiceImpl implements UserDetailsService
{
	private final UserService userService;
	private final GroupService groupService;

	public UserDetailsServiceImpl(UserService userService, GroupService groupService)
	{
		this.userService = requireNonNull(userService);
		this.groupService = requireNonNull(groupService);
	}

	@Override
	@RunAsSystem
	public UserDetails loadUserByUsername(String username)
	{
		User user = userService.findByUsername(username);
		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				user.isActive(), true, true, true, getAuthorities(user));
	}

	public Set<GrantedAuthority> getAuthorities(User user)
	{
		Set<GrantedAuthority> groupAuthorities = getGroupAuthorities(user);
		if (user.isSuperuser())
		{
			return Sets.union(groupAuthorities, singleton(new SimpleGrantedAuthority(AUTHORITY_SU)));
		}
		else return groupAuthorities;
	}

	private Set<GrantedAuthority> getGroupAuthorities(User user)
	{
		return groupService.getGroupMemberships(user)
						   .stream()
						   .filter(GroupMembership::isCurrent)
						   .map(GroupMembership::getGroup)
						   .flatMap(this::getRoles)
						   .map(Role::getId)
						   .map(SimpleGrantedAuthority::new)
						   .collect(Collectors.toSet());
	}

	public Stream<Role> getRoles(Group group)
	{
		return concat(group.getRoles().stream(),
				group.getParent().map(Stream::of).orElseGet(Stream::empty).flatMap(this::getRoles));
	}
}
