package org.molgenis.security.user;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.security.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.data.security.auth.GroupMemberMetaData.GROUP_MEMBER;
import static org.molgenis.data.security.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.data.security.auth.UserMetaData.USER;

public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService
{
	private final DataService dataService;
	private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;

	public UserDetailsService(DataService dataService, GrantedAuthoritiesMapper grantedAuthoritiesMapper)
	{
		this.dataService = requireNonNull(dataService, "DataService is null");
		this.grantedAuthoritiesMapper = requireNonNull(grantedAuthoritiesMapper, "Granted authorities mapper is null");
	}

	@Override
	@RunAsSystem
	public UserDetails loadUserByUsername(String username)
	{
		try
		{
			User user = dataService.findOne(USER, new QueryImpl<User>().eq(UserMetaData.USERNAME, username),
					User.class);

			if (user == null) throw new UsernameNotFoundException("unknown user '" + username + "'");

			Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
			return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
					user.isActive(), true, true, true, authorities);
		}
		catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	public Collection<? extends GrantedAuthority> getAuthorities(User user)
	{
		// user authorities
		List<? extends Authority> authorities = getUserAuthorities(user);
		List<GrantedAuthority> grantedAuthorities = authorities != null ? Lists.transform(authorities,
				(Function<Authority, GrantedAuthority>) authority -> new SimpleGrantedAuthority(
						authority.getRole())) : null;

		// // user group authorities
		List<GroupAuthority> groupAuthorities = getGroupAuthorities(user);
		List<GrantedAuthority> grantedGroupAuthorities = groupAuthorities != null ? Lists.transform(groupAuthorities,
				(Function<GroupAuthority, GrantedAuthority>) groupAuthority -> new SimpleGrantedAuthority(
						groupAuthority.getRole())) : null;

		// union of user and group authorities
		Set<GrantedAuthority> allGrantedAuthorities = new HashSet<>();
		if (grantedAuthorities != null) allGrantedAuthorities.addAll(grantedAuthorities);
		if (grantedGroupAuthorities != null) allGrantedAuthorities.addAll(grantedGroupAuthorities);
		if (user.isSuperuser() != null && user.isSuperuser() == true)
		{
			allGrantedAuthorities.add(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));
		}
		return grantedAuthoritiesMapper.mapAuthorities(allGrantedAuthorities);
	}

	private List<UserAuthority> getUserAuthorities(User user)
	{
		return dataService.findAll(USER_AUTHORITY, new QueryImpl<UserAuthority>().eq(UserAuthorityMetaData.USER, user),
				UserAuthority.class).collect(toList());
	}

	private List<GroupAuthority> getGroupAuthorities(User user)
	{
		List<GroupMember> groupMembers = dataService.findAll(GROUP_MEMBER,
				new QueryImpl<GroupMember>().eq(GroupMemberMetaData.USER, user), GroupMember.class).collect(toList());

		if (!groupMembers.isEmpty())
		{
			List<Group> groups = Lists.transform(groupMembers, GroupMember::getGroup);

			return dataService.findAll(GROUP_AUTHORITY,
					new QueryImpl<GroupAuthority>().in(GroupAuthorityMetaData.GROUP, groups), GroupAuthority.class)
							  .collect(toList());
		}
		return null;
	}
}
