package org.molgenis.security;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.auth.Authority;
import org.molgenis.omx.auth.GroupAuthority;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisGroupMember;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.UserAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class MolgenisUserDetailsService implements UserDetailsService
{
	@Autowired
	protected Database database;
	protected final PasswordEncoder passwordEncoder;

	public MolgenisUserDetailsService(PasswordEncoder passwordEncoder)
	{
		if (passwordEncoder == null) throw new IllegalArgumentException("Password encoder is null");
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{
		try
		{
			MolgenisUser user = MolgenisUser.findByUsername(database, username);
			if (user == null) throw new UsernameNotFoundException("unknown user '" + username + "'");

			// user authorities
			List<? extends Authority> authorities = getUserAuthorities(user);
			List<GrantedAuthority> grantedAuthorities = authorities != null ? Lists.transform(authorities,
					new Function<Authority, GrantedAuthority>()
					{
						@Override
						public GrantedAuthority apply(Authority authority)
						{
							return new SimpleGrantedAuthority(authority.getRole());
						}
					}) : null;

			// // user group authorities
			List<GroupAuthority> groupAuthorities = getGroupAuthorities(user);
			List<GrantedAuthority> grantedGroupAuthorities = groupAuthorities != null ? Lists.transform(
					groupAuthorities, new Function<GroupAuthority, GrantedAuthority>()
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

			return new User(user.getUsername(), user.getPassword(), allGrantedAuthorities);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}

	private List<UserAuthority> getUserAuthorities(MolgenisUser molgenisUser) throws DatabaseException
	{
		return database.find(UserAuthority.class, new QueryRule(UserAuthority.MOLGENISUSER, Operator.EQUALS,
				molgenisUser));
	}

	private List<GroupAuthority> getGroupAuthorities(MolgenisUser molgenisUser) throws DatabaseException
	{
		List<MolgenisGroupMember> groupMembers = database.find(MolgenisGroupMember.class, new QueryRule(
				MolgenisGroupMember.MOLGENISUSER, Operator.EQUALS, molgenisUser));
		if (groupMembers != null && !groupMembers.isEmpty())
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

			return database.find(GroupAuthority.class, new QueryRule(GroupAuthority.MOLGENISGROUP, Operator.IN,
					molgenisGroups));
		}
		return null;
	}
}
