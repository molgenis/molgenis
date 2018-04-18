package org.molgenis.security;

import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.GroupMemberFactory;
import org.molgenis.data.security.auth.TokenFactory;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.account.AccountController;
import org.molgenis.security.core.MolgenisPasswordEncoder;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.google.GoogleAuthenticationProcessingFilter;
import org.molgenis.security.login.MolgenisLoginController;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.token.DataServiceTokenService;
import org.molgenis.security.token.TokenAuthenticationFilter;
import org.molgenis.security.token.TokenAuthenticationProvider;
import org.molgenis.security.token.TokenGenerator;
import org.molgenis.security.twofactor.TwoFactorAuthenticationController;
import org.molgenis.security.twofactor.auth.*;
import org.molgenis.security.twofactor.service.OtpService;
import org.molgenis.security.twofactor.service.RecoveryService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.security.user.MolgenisUserDetailsChecker;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.security.user.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.intercept.RunAsImplAuthenticationProvider;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetailsChecker;
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
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.Filter;

import static org.molgenis.core.framework.ui.ResourcePathPatterns.*;
import static org.molgenis.security.UriConstants.PATH_SEGMENT_APPS;
import static org.molgenis.security.google.GoogleAuthenticationProcessingFilter.GOOGLE_AUTHENTICATION_URL;

public abstract class MolgenisWebAppSecurityConfig extends WebSecurityConfigurerAdapter
{
	private static final String ANONYMOUS_AUTHENTICATION_KEY = "anonymousAuthenticationKey";

	@Autowired
	private DataService dataService;

	@Autowired
	private UserService userService;

	@Autowired
	private AuthenticationSettings authenticationSettings;

	@Autowired
	private TokenFactory tokenFactory;

	@Autowired
	private UserFactory userFactory;

	@Autowired
	private GroupMemberFactory groupMemberFactory;

	@Autowired
	private OtpService otpService;

	@Autowired
	private TwoFactorAuthenticationService twoFactorAuthenticationService;

	@Autowired
	private RecoveryService recoveryService;

	@Autowired
	private UserAccountService userAccountService;

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		// do not write cache control headers for static resources
		RequestMatcher matcher = new NegatedRequestMatcher(
				new OrRequestMatcher(new AntPathRequestMatcher(PATTERN_CSS), new AntPathRequestMatcher(PATTERN_JS),
						new AntPathRequestMatcher(PATTERN_IMG), new AntPathRequestMatcher(PATTERN_FONTS)));

		DelegatingRequestMatcherHeaderWriter cacheControlHeaderWriter = new DelegatingRequestMatcherHeaderWriter(
				matcher, new CacheControlHeadersWriter());

		http.sessionManagement().invalidSessionStrategy(invalidSessionStrategy());

		// add default header options but use custom cache control header writer
		http.cors().and().headers()
			.contentTypeOptions()
			.and()
			.xssProtection()
			.and()
			.httpStrictTransportSecurity()
			.and()
			.frameOptions()
			.and()
			.addHeaderWriter(cacheControlHeaderWriter);

		http.addFilterBefore(anonymousAuthFilter(), AnonymousAuthenticationFilter.class);

		http.authenticationProvider(anonymousAuthenticationProvider());

		http.authenticationProvider(tokenAuthenticationProvider());

		http.authenticationProvider(runAsAuthenticationProvider());

		http.addFilterBefore(tokenAuthenticationFilter(), AnonymousAuthenticationFilter.class);

		http.addFilterBefore(googleAuthenticationProcessingFilter(), TokenAuthenticationFilter.class);

		http.addFilterAfter(changePasswordFilter(), SwitchUserFilter.class);

		http.addFilterAfter(twoFactorAuthenticationFilter(), MolgenisChangePasswordFilter.class);
		http.authenticationProvider(twoFactorAuthenticationProvider());
		http.authenticationProvider(recoveryAuthenticationProvider());

		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry = http
				.authorizeRequests();
		configureUrlAuthorization(expressionInterceptUrlRegistry);

		expressionInterceptUrlRegistry

				.antMatchers(MolgenisLoginController.URI)
				.permitAll()

				.antMatchers(TwoFactorAuthenticationController.URI + "/**")
				.permitAll()

				.antMatchers(GOOGLE_AUTHENTICATION_URL)
				.permitAll()

				.antMatchers("/beacon/**")
				.permitAll()

				.antMatchers("/logo/**")
				.permitAll()

				.antMatchers("/molgenis.py")
				.permitAll()

				.antMatchers("/molgenis.R")
				.permitAll()

				.antMatchers(AccountController.CHANGE_PASSWORD_URI)
				.authenticated()

				.antMatchers("/account/**")
				.permitAll()

				.antMatchers(PATTERN_SWAGGER)
				.permitAll()

				.antMatchers(PATTERN_CSS)
				.permitAll()

				.antMatchers(PATTERN_IMG)
				.permitAll()

				.antMatchers(PATTERN_JS)
				.permitAll()

				.antMatchers(PATTERN_FONTS)
				.permitAll()

				.antMatchers("/html/**")
				.permitAll()

				.antMatchers("/plugin/void/**")
				.permitAll()

				.antMatchers("/api/**")
				.permitAll()

				.antMatchers("/webjars/**")
				.permitAll()

				.antMatchers("/search")
				.permitAll()

				.antMatchers("/captcha")
				.permitAll()

				.antMatchers("/dataindexerstatus")
				.authenticated()

				.antMatchers("/permission/**/read/**")
				.permitAll()

				.antMatchers("/permission/**/write/**")
				.permitAll()

				.antMatchers("/scripts/**/run")
				.authenticated()

				.antMatchers("/scripts/**/start")
				.authenticated()

				.antMatchers("/files/**")
				.permitAll()

				.antMatchers('/' + PATH_SEGMENT_APPS + "/**")
				.permitAll()

				.anyRequest()
				.denyAll()
				.and()

				.httpBasic()
				.authenticationEntryPoint(authenticationEntryPoint())
				.and()

				.formLogin()
				.loginPage(MolgenisLoginController.URI)
				.failureUrl(MolgenisLoginController.URI + "?error")
				.and()

				.logout().deleteCookies("JSESSIONID").addLogoutHandler((req, res, auth) ->
		{
					if (req.getSession(false) != null
							&& req.getSession().getAttribute("continueWithUnsupportedBrowser") != null)
					{
						req.setAttribute("continueWithUnsupportedBrowser", true);
					}
				})

				.logoutSuccessHandler((req, res, auth) ->
				{
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

				.csrf()
				.disable();

	}

	@Override
	public void configure(WebSecurity web) throws Exception
	{
		web.ignoring()
		   .antMatchers(PATTERN_CSS)
		   .antMatchers(PATTERN_IMG)
		   .antMatchers(PATTERN_JS)
		   .antMatchers(PATTERN_FONTS);
	}

	@Bean
	public AuthenticationProvider runAsAuthenticationProvider()
	{
		RunAsImplAuthenticationProvider provider = new RunAsImplAuthenticationProvider();
		provider.setKey("Job Execution");
		return provider;
	}

	protected abstract void configureUrlAuthorization(
			ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry);

	protected abstract RoleHierarchy roleHierarchy();

	@Bean
	public AnonymousAuthenticationFilter anonymousAuthFilter()
	{
		return new AnonymousAuthenticationFilter(ANONYMOUS_AUTHENTICATION_KEY, SecurityUtils.ANONYMOUS_USERNAME,
				AuthorityUtils.createAuthorityList(SecurityUtils.AUTHORITY_ANONYMOUS));
	}

	@Bean
	public AnonymousAuthenticationProvider anonymousAuthenticationProvider()
	{
		return new AnonymousAuthenticationProvider(ANONYMOUS_AUTHENTICATION_KEY);
	}

	@Bean
	public TokenService tokenService()
	{
		return new DataServiceTokenService(new TokenGenerator(), dataService, userDetailsService(), tokenFactory);
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
	public GooglePublicKeysManager googlePublicKeysManager()
	{
		HttpTransport transport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		return new GooglePublicKeysManager(transport, jsonFactory);
	}

	@Bean
	public Filter googleAuthenticationProcessingFilter() throws Exception
	{
		GoogleAuthenticationProcessingFilter googleAuthenticationProcessingFilter = new GoogleAuthenticationProcessingFilter(
				googlePublicKeysManager(), dataService, (UserDetailsService) userDetailsService(),
				authenticationSettings, userFactory, groupMemberFactory);
		googleAuthenticationProcessingFilter.setAuthenticationManager(authenticationManagerBean());
		return googleAuthenticationProcessingFilter;
	}

	@Bean
	public Filter changePasswordFilter()
	{
		return new MolgenisChangePasswordFilter(userService, redirectStrategy());
	}

	@Bean
	public TwoFactorAuthenticationFilter twoFactorAuthenticationFilter()
	{
		return new TwoFactorAuthenticationFilter(authenticationSettings, twoFactorAuthenticationService,
				redirectStrategy(), userAccountService);
	}

	@Bean
	public TwoFactorAuthenticationProvider twoFactorAuthenticationProvider()
	{
		return new TwoFactorAuthenticationProviderImpl(twoFactorAuthenticationService, otpService, recoveryService);
	}

	@Bean
	public RecoveryAuthenticationProvider recoveryAuthenticationProvider()
	{
		return new RecoveryAuthenticationProviderImpl(recoveryService);
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
	protected org.springframework.security.core.userdetails.UserDetailsService userDetailsService()
	{
		return new UserDetailsService(dataService, roleHierarchyAuthoritiesMapper());
	}

	@Override
	@Bean
	public org.springframework.security.core.userdetails.UserDetailsService userDetailsServiceBean() throws Exception
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
			DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
			authenticationProvider.setPasswordEncoder(passwordEncoder());
			authenticationProvider.setUserDetailsService(userDetailsServiceBean());
			authenticationProvider.setPreAuthenticationChecks(userDetailsChecker());
			auth.authenticationProvider(authenticationProvider);
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
	public LoginUrlAuthenticationEntryPoint authenticationEntryPoint()
	{
		return new AjaxAwareLoginUrlAuthenticationEntryPoint(MolgenisLoginController.URI);
	}

	@Bean
	public InvalidSessionStrategy invalidSessionStrategy()
	{
		return new AjaxAwareInvalidSessionStrategy(
				MolgenisLoginController.URI + '?' + MolgenisLoginController.PARAM_SESSION_EXPIRED);
	}

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher()
	{
		return new HttpSessionEventPublisher();
	}
}