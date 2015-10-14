package org.molgenis.security;

import static org.molgenis.framework.ui.ResourcePathPatterns.PATTERN_CSS;
import static org.molgenis.framework.ui.ResourcePathPatterns.PATTERN_FONTS;
import static org.molgenis.framework.ui.ResourcePathPatterns.PATTERN_IMG;
import static org.molgenis.framework.ui.ResourcePathPatterns.PATTERN_JS;

import java.util.List;

import javax.servlet.Filter;
import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.security.account.AccountController;
import org.molgenis.security.core.MolgenisPasswordEncoder;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.MolgenisPermissionServiceImpl;
import org.molgenis.security.token.DataServiceTokenService;
import org.molgenis.security.token.TokenAuthenticationFilter;
import org.molgenis.security.token.TokenAuthenticationProvider;
import org.molgenis.security.token.TokenGenerator;
import org.molgenis.security.user.MolgenisUserDetailsChecker;
import org.molgenis.security.user.MolgenisUserDetailsService;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
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
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.header.writers.CacheControlHeadersWriter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public abstract class MolgenisWebAppSecurityConfig extends WebSecurityConfigurerAdapter
{
	private static final String ANONYMOUS_AUTHENTICATION_KEY = "anonymousAuthenticationKey";

	@Autowired
	private DataService dataService;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private MolgenisUserService molgenisUserService;

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		// do not write cache control headers for static resources
		RequestMatcher matcher = new NegatedRequestMatcher(
				new OrRequestMatcher(new AntPathRequestMatcher(PATTERN_CSS), new AntPathRequestMatcher(PATTERN_JS),
						new AntPathRequestMatcher(PATTERN_IMG), new AntPathRequestMatcher(PATTERN_FONTS)));

		DelegatingRequestMatcherHeaderWriter cacheControlHeaderWriter = new DelegatingRequestMatcherHeaderWriter(
				matcher, new CacheControlHeadersWriter());

		// add default header options but use custom cache control header writer
		http.headers().contentTypeOptions().and().xssProtection().and().httpStrictTransportSecurity().and()
				.frameOptions().and().addHeaderWriter(cacheControlHeaderWriter);

		http.addFilterBefore(anonymousAuthFilter(), AnonymousAuthenticationFilter.class);
		http.authenticationProvider(anonymousAuthenticationProvider());

		http.addFilterBefore(tokenAuthenticationFilter(), MolgenisAnonymousAuthenticationFilter.class);
		http.authenticationProvider(tokenAuthenticationProvider());

		http.addFilterAfter(changePasswordFilter(), SwitchUserFilter.class);

		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry = http
				.authorizeRequests();
		configureUrlAuthorization(expressionInterceptUrlRegistry);

		expressionInterceptUrlRegistry.antMatchers("/login").permitAll()

		.antMatchers("/logo/**").permitAll()

		.antMatchers("/molgenis.R").permitAll()

		.antMatchers(AccountController.CHANGE_PASSWORD_URI).authenticated()

		.antMatchers("/account/**").permitAll()

		.antMatchers(PATTERN_CSS).permitAll()

		.antMatchers(PATTERN_IMG).permitAll()

		.antMatchers(PATTERN_JS).permitAll()

		.antMatchers(PATTERN_FONTS).permitAll()

		.antMatchers("/html/**").permitAll()

		.antMatchers("/plugin/void/**").permitAll()

		.antMatchers("/api/**").permitAll()

		.antMatchers("/search").permitAll()

		.antMatchers("/captcha").permitAll()

		.antMatchers("/dataindexerstatus").authenticated()

		.antMatchers("/permission/**/write/**").permitAll()

		.antMatchers("/scripts/**/run").authenticated()

		.antMatchers("/files/**").permitAll()

		.anyRequest().denyAll().and()

		.httpBasic().authenticationEntryPoint(authenticationEntryPoint()).and()

		.formLogin().loginPage("/login").failureUrl("/login?error").and()

		.logout().addLogoutHandler((req, res, auth) -> {
			if (req.getSession().getAttribute("continueWithUnsupportedBrowser") != null)
			{
				req.setAttribute("continueWithUnsupportedBrowser", true);
			}
		}).logoutSuccessHandler((req, res, auth) -> {
			StringBuilder logoutSuccessUrl = new StringBuilder("/");
			if (req.getAttribute("continueWithUnsupportedBrowser") != null)
			{
				logoutSuccessUrl.append("?continueWithUnsupportedBrowser=true");
			}
			SimpleUrlLogoutSuccessHandler logoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
			logoutSuccessHandler.setDefaultTargetUrl(logoutSuccessUrl.toString());
			logoutSuccessHandler.onLogoutSuccess(req, res, auth);
		})

		.and()

		.csrf().disable();
	}

	protected abstract void configureUrlAuthorization(
			ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry);

	protected abstract RoleHierarchy roleHierarchy();

	@Bean
	public MolgenisAnonymousAuthenticationFilter anonymousAuthFilter()
	{
		return new MolgenisAnonymousAuthenticationFilter(ANONYMOUS_AUTHENTICATION_KEY, SecurityUtils.ANONYMOUS_USERNAME,
				userDetailsService());
	}

	protected abstract List<GrantedAuthority> createAnonymousUserAuthorities();

	@Bean
	public AnonymousAuthenticationProvider anonymousAuthenticationProvider()
	{
		return new AnonymousAuthenticationProvider(ANONYMOUS_AUTHENTICATION_KEY);
	}

	@Bean
	public TokenService tokenService()
	{
		return new DataServiceTokenService(new TokenGenerator(), dataService, userDetailsService());
	}

	@Bean
	public AuthenticationProvider tokenAuthenticationProvider()
	{
		return new TokenAuthenticationProvider(tokenService());
	}

	@Bean
	public Filter tokenAuthenticationFilter()
	{
		return new TokenAuthenticationFilter(tokenAuthenticationProvider());
	}

	@Bean
	public Filter changePasswordFilter()
	{
		return new MolgenisChangePasswordFilter(molgenisUserService, redirectStrategy());
	}

	@Bean
	public RedirectStrategy redirectStrategy()
	{
		return new DefaultRedirectStrategy();
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
		return new MolgenisUserDetailsService(dataService, roleHierarchyAuthoritiesMapper());
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

	@Bean
	public LoginUrlAuthenticationEntryPoint authenticationEntryPoint()
	{
		return new AjaxAwareLoginUrlAuthenticationEntryPoint("/login");
	}
}