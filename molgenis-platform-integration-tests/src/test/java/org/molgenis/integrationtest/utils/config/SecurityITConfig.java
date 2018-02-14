package org.molgenis.integrationtest.utils.config;

import org.molgenis.data.security.auth.*;
import org.molgenis.data.security.owned.OwnedEntityType;
import org.molgenis.data.security.user.UserServiceImpl;
import org.molgenis.integrationtest.data.aggregation.AggregationTestConfig;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.AuthenticationAuthoritiesUpdaterImpl;
import org.molgenis.security.permission.PermissionServiceImpl;
import org.molgenis.security.permission.PrincipalSecurityContextRegistryImpl;
import org.molgenis.security.permission.SecurityContextRegistryImpl;
import org.molgenis.security.settings.AuthenticationSettingsImpl;
import org.molgenis.security.token.DataServiceTokenService;
import org.molgenis.security.token.TokenGenerator;
import org.molgenis.security.user.UserAccountServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collection;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@EnableWebSecurity
@Import({ PermissionServiceImpl.class, DataServiceTokenService.class, TokenGenerator.class, TokenFactory.class,
		TokenMetaData.class, SecurityPackage.class, UserMetaData.class, OwnedEntityType.class,
		UserAccountServiceImpl.class, UserServiceImpl.class, BCryptPasswordEncoder.class, UserFactory.class,
		AggregationTestConfig.class, RoleHierarchyAuthoritiesMapper.class, GroupMetaData.class,
		AuthenticationSettingsImpl.class, PrincipalSecurityContextRegistryImpl.class, SecurityContextRegistryImpl.class,
		AuthenticationAuthoritiesUpdaterImpl.class })
public class SecurityITConfig
{
	public final static String SUPERUSER_NAME = "admin";
	public final static String TOKEN_DESCRIPTION = "REST token";

	@Bean
	public RoleHierarchy roleHierarchy()
	{
		return mock(RoleHierarchy.class);
	}

	@Bean
	public UserDetailsService userDetailsService()
	{
		UserDetailsService userDetailsService = mock(UserDetailsService.class);
		UserDetails adminUserDetails = mock(UserDetails.class);
		Collection authorities = singleton(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));
		when(adminUserDetails.getAuthorities()).thenReturn(authorities);
		when(userDetailsService.loadUserByUsername(SUPERUSER_NAME)).thenReturn(adminUserDetails);
		return userDetailsService;
	}
}
