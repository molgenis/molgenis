package org.molgenis.omx;

import static org.molgenis.security.SecurityUtils.defaultPluginAuthorities;

import javax.sql.DataSource;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.omx.auth.OmxPermissionService;
import org.molgenis.security.MolgenisPasswordEncoder;
import org.molgenis.security.MolgenisUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebAppSecurityConfig extends WebSecurityConfigurerAdapter
{
	@Autowired
	private Database unsecuredDatabase;

	@Autowired
	private DataSource dataSource;

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		http.authorizeRequests()

		.antMatchers("/").permitAll()

		.antMatchers("/login").permitAll()

		.antMatchers("/account/**").permitAll()

		.antMatchers("/css/**").permitAll()

		.antMatchers("/img/**").permitAll()

		.antMatchers("/js/**").permitAll()

		.antMatchers("/html/**").permitAll()

		.antMatchers("/plugin/void/**").permitAll()

		.antMatchers("/plugin/home/**").permitAll()

		.antMatchers("/plugin/protocolviewer/**").hasAnyAuthority(defaultPluginAuthorities("protocolviewer"))

		.antMatchers("/plugin/dataexplorer/**").hasAnyAuthority(defaultPluginAuthorities("dataexplorer"))

		.antMatchers("/plugin/entityexplorer/**").hasAnyAuthority(defaultPluginAuthorities("entityexplorer"))

		.antMatchers("/plugin/importwizard/**").hasAnyAuthority(defaultPluginAuthorities("importwizard"))

		.antMatchers("/plugin/news/**").hasAnyAuthority(defaultPluginAuthorities("news"))

		.antMatchers("/plugin/background/**").hasAnyAuthority(defaultPluginAuthorities("background"))

		.antMatchers("/plugin/references/**").hasAnyAuthority(defaultPluginAuthorities("references"))

		.antMatchers("/plugin/contact/**").hasAnyAuthority(defaultPluginAuthorities("contact"))

		.antMatchers("/plugin/cbmtoomxconverter/**").hasAnyAuthority(defaultPluginAuthorities("cbmtoomxconverter"))

		.antMatchers("/plugin/form.**").hasAnyAuthority(defaultPluginAuthorities("form"))

		.antMatchers("/plugin/permissionmanager/**").hasAnyAuthority(defaultPluginAuthorities("permissionmanager"))

		.antMatchers("/plugin/catalogmanager/**").hasAnyAuthority(defaultPluginAuthorities("catalogmanager"))

		.antMatchers("/plugin/studymanager/**").hasAnyAuthority(defaultPluginAuthorities("studymanager"))

		.antMatchers("/plugin/dataindexer/**").hasAnyAuthority(defaultPluginAuthorities("dataindexer"))

		.antMatchers("/plugin/datasetdeleter/**").hasAnyAuthority(defaultPluginAuthorities("datasetdeleter"))

		.antMatchers("/plugin/useraccount/**").hasAnyAuthority(defaultPluginAuthorities("useraccount"))

		.antMatchers("/plugin/**").denyAll()

		.anyRequest().authenticated().and()

		.formLogin().loginPage("/login").failureUrl("/login?error").and()

		.logout().logoutSuccessUrl("/").and()

		.csrf().disable(); // FIXME enable
	}

	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new MolgenisPasswordEncoder();
	}

	@Override
	protected UserDetailsService userDetailsService()
	{
		return new MolgenisUserDetailsService(unsecuredDatabase, passwordEncoder());
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
	public MolgenisPermissionService molgenisPermissionService()
	{
		return new OmxPermissionService();
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception
	{
		return super.authenticationManagerBean();
	}
}