package org.molgenis.omx;

import javax.sql.DataSource;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.security.MolgenisPasswordEncoder;
import org.molgenis.security.permission.MolgenisPermissionServiceImpl;
import org.molgenis.security.user.MolgenisUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class MolgenisWebAppSecurityConfig extends WebSecurityConfigurerAdapter
{
	@Autowired
	private Database unsecuredDatabase;

	@Autowired
	private DataSource dataSource;

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		ExpressionUrlAuthorizationConfigurer<HttpSecurity> euac = http.authorizeRequests();
		configureUrlAuthorization(euac);

		euac.antMatchers("/login").permitAll()

		.antMatchers("/account/**").permitAll()

		.antMatchers("/css/**").permitAll()

		.antMatchers("/img/**").permitAll()

		.antMatchers("/js/**").permitAll()

		.antMatchers("/html/**").permitAll()
		
		//TEMPORARY FOR TESTNG PURPOSES
		.antMatchers("/das/**").permitAll()
		.antMatchers("/myDas/**").permitAll()
		//-----------------------------
		
		.antMatchers("/plugin/void/**").permitAll()

		.antMatchers("/plugin/**").denyAll()

		.anyRequest().authenticated().and()

		.formLogin().loginPage("/login").failureUrl("/login?error").and()

		.logout().logoutSuccessUrl("/").and()

		.csrf().disable();
	}

	protected abstract void configureUrlAuthorization(ExpressionUrlAuthorizationConfigurer<HttpSecurity> euac);

	protected abstract RoleHierarchy roleHierarchy();

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
		return new MolgenisUserDetailsService(unsecuredDatabase, passwordEncoder(), roleHierarchyAuthoritiesMapper());
	}

	@Override
	@Bean
	public UserDetailsService userDetailsServiceBean() throws Exception
	{
		return userDetailsService();
	}

	@Override
	protected void registerAuthentication(AuthenticationManagerBuilder auth)
	{
		try
		{
			auth.userDetailsService(userDetailsServiceBean());

			DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
			daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
			daoAuthenticationProvider.setUserDetailsService(userDetailsServiceBean());
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