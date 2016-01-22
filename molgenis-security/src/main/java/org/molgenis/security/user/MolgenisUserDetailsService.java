package org.molgenis.security.user;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.molgenis.auth.Authority;
import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupMember;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class MolgenisUserDetailsService implements UserDetailsService
{
	private final DataService dataService;
	private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;

	@Autowired
	public MolgenisUserDetailsService(DataService dataService, GrantedAuthoritiesMapper grantedAuthoritiesMapper)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (grantedAuthoritiesMapper == null) throw new IllegalArgumentException("Granted authorities mapper is null");
		this.dataService = dataService;
		this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
	}

	@Override
	@RunAsSystem
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{
		try
		{
			MolgenisUser user = dataService.findOne(MolgenisUser.ENTITY_NAME,
					new QueryImpl().eq(MolgenisUser.USERNAME, username), MolgenisUser.class);

			if (user == null) throw new UsernameNotFoundException("unknown user '" + username + "'");

			Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
			return new User(user.getUsername(), user.getPassword(), user.isActive(), true, true, true, authorities);
		}
		catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	public Collection<? extends GrantedAuthority> getAuthorities(MolgenisUser user)
	{
		// user authorities
		List<? extends Authority> authorities = getUserAuthorities(user);
		List<GrantedAuthority> grantedAuthorities = authorities != null
				? Lists.transform(authorities, new Function<Authority, GrantedAuthority>()
				{
					@Override
					public GrantedAuthority apply(Authority authority)
					{
						return new SimpleGrantedAuthority(authority.getRole());
					}
				}) : null;

		// // user group authorities
		List<GroupAuthority> groupAuthorities = getGroupAuthorities(user);
		List<GrantedAuthority> grantedGroupAuthorities = groupAuthorities != null
				? Lists.transform(groupAuthorities, new Function<GroupAuthority, GrantedAuthority>()
				{
					@Override
					public GrantedAuthority apply(GroupAuthority groupAuthority)
					{
						return new SimpleGrantedAuthority(groupAuthority.getRole());
					}
				}) : null;

		// union of user and group authorities
		Set<GrantedAuthority> allGrantedAuthorities = new HashSet<GrantedAuthority>();
		if (grantedAuthorities != null) allGrantedAuthorities.addAll(grantedAuthorities);
		if (grantedGroupAuthorities != null) allGrantedAuthorities.addAll(grantedGroupAuthorities);
		if (user.isSuperuser() != null && user.isSuperuser().booleanValue() == true)
		{
			allGrantedAuthorities.add(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));
		}
		return grantedAuthoritiesMapper.mapAuthorities(allGrantedAuthorities);
	}

	private List<UserAuthority> getUserAuthorities(MolgenisUser molgenisUser)
	{
		return dataService.findAll(UserAuthority.ENTITY_NAME,
				new QueryImpl().eq(UserAuthority.MOLGENISUSER, molgenisUser), UserAuthority.class).collect(toList());
	}

	private List<GroupAuthority> getGroupAuthorities(MolgenisUser molgenisUser)
	{
		List<MolgenisGroupMember> groupMembers = dataService
				.findAll(MolgenisGroupMember.ENTITY_NAME,
						new QueryImpl().eq(MolgenisGroupMember.MOLGENISUSER, molgenisUser), MolgenisGroupMember.class)
				.collect(toList());

		if (!groupMembers.isEmpty())
		{
			List<MolgenisGroup> molgenisGroups = Lists.transform(groupMembers,
					new Function<MolgenisGroupMember, MolgenisGroup>()
					{
						@Override
						public MolgenisGroup apply(MolgenisGroupMember molgenisGroupMember)
						{
							return molgenisGroupMember.getMolgenisGroup();
						}
					});

			return dataService
					.findAll(GroupAuthority.ENTITY_NAME,
							new QueryImpl().in(GroupAuthority.MOLGENISGROUP, molgenisGroups), GroupAuthority.class)
					.collect(toList());
		}
		return null;
	}
}
