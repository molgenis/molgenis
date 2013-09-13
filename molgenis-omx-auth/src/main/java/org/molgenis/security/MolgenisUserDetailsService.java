package org.molgenis.security;

import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.auth.Authority;
import org.molgenis.omx.auth.MolgenisUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
	@Qualifier("unauthorizedPrototypeDatabase")
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
			MolgenisUser user = MolgenisUser.findByName(database, username);
			if (user == null) throw new UsernameNotFoundException("unknown user '" + username + "'");
			List<Authority> authorities = database.find(Authority.class, new QueryRule(Authority.MOLGENISUSER,
					Operator.EQUALS, user));
			List<GrantedAuthority> grantedAuthorities = authorities != null ? Lists.transform(authorities,
					new Function<Authority, GrantedAuthority>()
					{
						@Override
						public GrantedAuthority apply(Authority authority)
						{
							return new SimpleGrantedAuthority(authority.getRole());
						}
					}) : null;
			return new User(user.getName(), user.getPassword(), grantedAuthorities);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}
}
