package org.molgenis.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.security.permission.MolgenisPermissionServiceImpl;
import org.molgenis.security.user.MolgenisUserDetailsChecker;
import org.molgenis.security.user.MolgenisUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

public abstract class MolgenisWebAppSecurityConfig extends WebSecurityConfigurerAdapter
{
	private static final String ANONYMOUS_AUTHENTICATION_KEY = "anonymousAuthenticationKey";

	@Autowired
	private Database unsecuredDatabase;

	@Autowired
	private DataSource dataSource;

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		http.addFilter(anonymousAuthFilter());
		http.authenticationProvider(anonymousAuthenticationProvider());

		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry = http
				.authorizeRequests();
		configureUrlAuthorization(expressionInterceptUrlRegistry);

		expressionInterceptUrlRegistry.antMatchers("/login").permitAll()

		.antMatchers("/account/**").permitAll()

		.antMatchers("/css/**").permitAll()

		.antMatchers("/img/**").permitAll()

		.antMatchers("/js/**").permitAll()

		.antMatchers("/html/**").permitAll()

		.antMatchers("/plugin/void/**").permitAll()

		.antMatchers("/api/**").permitAll()

		.antMatchers("/search").permitAll()

		.antMatchers("/captcha").permitAll()

		.anyRequest().denyAll().and()

		.formLogin().loginPage("/login").failureUrl("/login?error").and()

		.logout().logoutSuccessUrl("/").and()

		.csrf().disable();
	}

	protected abstract void configureUrlAuthorization(
			ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry);

	protected abstract RoleHierarchy roleHierarchy();

	@Bean
	public AnonymousAuthenticationFilter anonymousAuthFilter()
	{
		List<GrantedAuthority> anonymousUserAuthorities = createAnonymousUserAuthorities();

		Collection<? extends GrantedAuthority> anonymousUserMappedAuthorities = roleHierarchyAuthoritiesMapper()
				.mapAuthorities(anonymousUserAuthorities);
		List<GrantedAuthority> allAnonymousUserAuthorityList = new ArrayList<GrantedAuthority>();
		for (GrantedAuthority anonymousUserMappedAuthority : anonymousUserMappedAuthorities)
			allAnonymousUserAuthorityList.add(anonymousUserMappedAuthority);

		return new AnonymousAuthenticationFilter(ANONYMOUS_AUTHENTICATION_KEY, SecurityUtils.ANONYMOUS_USERNAME,
				allAnonymousUserAuthorityList);
	}

	protected abstract List<GrantedAuthority> createAnonymousUserAuthorities();

	@Bean
	public AnonymousAuthenticationProvider anonymousAuthenticationProvider()
	{
		return new AnonymousAuthenticationProvider(ANONYMOUS_AUTHENTICATION_KEY);
	}

	@Bean
	public RoleHierarchy roleHierarchyBean()
	{
		return roleHierarchy();
	}

	@Bean
	public RoleVoter roleVoter()
	{
		return new RoleHierarchyVoter(roleHierarchy());
	}

	@Bean
	public GrantedAuthoritiesMapper roleHierarchyAuthoritiesMapper()
	{
		return new RoleHierarchyAuthoritiesMapper(roleHierarchy());
	}

	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new MolgenisPasswordEncoder(new BCryptPasswordEncoder());
	}

	@Override
	protected UserDetailsService userDetailsService()
	{
		return new MolgenisUserDetailsService(unsecuredDatabase, roleHierarchyAuthoritiesMapper());
	}

	@Override
	@Bean
	public UserDetailsService userDetailsServiceBean() throws Exception
	{
		return userDetailsService();
	}

	@Bean
	public UserDetailsChecker userDetailsChecker()
	{
		return new MolgenisUserDetailsChecker();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth)
	{
		try
		{
			auth.userDetailsService(userDetailsServiceBean());

			DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
			daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
			daoAuthenticationProvider.setUserDetailsService(userDetailsServiceBean());
			daoAuthenticationProvider.setPreAuthenticationChecks(userDetailsChecker());
			auth.authenticationProvider(daoAuthenticationProvider);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception
	{
		return super.authenticationManagerBean();
	}

	@Bean
	public MolgenisPermissionService molgenisPermissionService()
	{
		return new MolgenisPermissionServiceImpl();
	}
}