package org.molgenis.security.user;

import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.GroupMember;
import org.molgenis.data.security.auth.GroupMemberMetaData;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserMetaData;
import org.molgenis.security.acl.SidUtils;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.GroupMemberMetaData.GROUP_MEMBER;

public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService
{
	private final DataService dataService;
	private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;

	public UserDetailsService(DataService dataService, GrantedAuthoritiesMapper grantedAuthoritiesMapper)
	{
		this.dataService = requireNonNull(dataService);
		this.grantedAuthoritiesMapper = requireNonNull(grantedAuthoritiesMapper);
	}

	@Override
	@RunAsSystem
	public UserDetails loadUserByUsername(String username)
	{
		User user = dataService.query(UserMetaData.USER, User.class).eq(UserMetaData.USERNAME, username).findOne();
		if (user == null)
		{
			throw new UsernameNotFoundException("unknown user '" + username + "'");
		}

		Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				user.isActive(), true, true, true, authorities);
	}

	public Collection<? extends GrantedAuthority> getAuthorities(User user)
	{
		Set<GrantedAuthority> authorities = new LinkedHashSet<>();

		if (user.isSuperuser() != null && user.isSuperuser())
		{
			authorities.add(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));
		}
		if (user.getUsername().equals(SecurityUtils.ANONYMOUS_USERNAME))
		{
			authorities.add(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_ANONYMOUS));
		}

		// add authorities of groups that this user is member of
		dataService.query(GROUP_MEMBER, GroupMember.class)
				   .eq(GroupMemberMetaData.USER, user)
				   .findAll()
				   .map(GroupMember::getGroup)
				   .map(SidUtils::createGroupAuthority)
				   .map(SimpleGrantedAuthority::new)
				   .forEach(authorities::add);

		return grantedAuthoritiesMapper.mapAuthorities(authorities);
	}
}
