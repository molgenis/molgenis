package org.molgenis.data.security;

import org.molgenis.data.DataService;
import org.molgenis.data.security.model.SecurityMetadataConfig;
import org.molgenis.data.security.model.TokenFactory;
import org.molgenis.data.security.model.UserFactory;
import org.molgenis.data.security.service.impl.DataServiceTokenService;
import org.molgenis.data.security.service.impl.UserServiceImpl;
import org.molgenis.data.security.user.UserAccountServiceImpl;
import org.molgenis.security.core.service.GroupService;
import org.molgenis.security.core.service.TokenService;
import org.molgenis.security.core.service.UserAccountService;
import org.molgenis.security.core.service.UserService;
import org.molgenis.security.core.service.impl.GroupServiceImpl;
import org.molgenis.security.core.service.impl.TokenGenerator;
import org.molgenis.security.core.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configures administrative security service beans.
 */
@Configuration
@Import({ SecurityMetadataConfig.class })
public class DataSecurityConfig
{
	@Autowired
	private UserFactory userFactory;

	@Autowired
	private DataService dataService;

	@Autowired
	private TokenFactory tokenFactory;

	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserService userService()
	{
		return new UserServiceImpl(dataService, userFactory);
	}

	@Bean
	public UserAccountService userAccountService()
	{
		return new UserAccountServiceImpl(userService(), passwordEncoder());
	}

	@Bean
	public GroupService groupService()
	{
		return new GroupServiceImpl();
	}

	@Bean
	public UserDetailsService userDetailsService()
	{
		return new UserDetailsServiceImpl(userService(), groupService());
	}

	@Bean
	public TokenService tokenService()
	{
		return new DataServiceTokenService(new TokenGenerator(), dataService, userDetailsService(), tokenFactory);
	}
}
