package org.molgenis.security;

import static org.molgenis.core.framework.ui.ResourcePathPatterns.PATTERN_CSS;
import static org.molgenis.core.framework.ui.ResourcePathPatterns.PATTERN_FONTS;
import static org.molgenis.core.framework.ui.ResourcePathPatterns.PATTERN_IMG;
import static org.molgenis.core.framework.ui.ResourcePathPatterns.PATTERN_JS;
import static org.molgenis.core.framework.ui.ResourcePathPatterns.PATTERN_SWAGGER;
import static org.molgenis.security.UriConstants.PATH_SEGMENT_APPS;

import java.util.LinkedHashMap;
import javax.servlet.Filter;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.TokenFactory;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.account.AccountController;
import org.molgenis.security.core.MolgenisPasswordEncoder;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.login.MolgenisLoginController;
import org.molgenis.security.oidc.DataServiceClientRegistrationRepository;
import org.molgenis.security.oidc.MappedOidcUserService;
import org.molgenis.security.oidc.OidcUserMapper;
import org.molgenis.security.oidc.OidcUserMapperImpl;
import org.molgenis.security.oidc.ResettableOAuth2AuthorizedClientService;
import org.molgenis.security.oidc.model.OidcUserMappingFactory;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.token.DataServiceTokenService;
import org.molgenis.security.token.TokenAuthenticationFilter;
import org.molgenis.security.token.TokenAuthenticationProvider;
import org.molgenis.security.token.TokenGenerator;
import org.molgenis.security.twofactor.TwoFactorAuthenticationController;
import org.molgenis.security.twofactor.auth.RecoveryAuthenticationProvider;
import org.molgenis.security.twofactor.auth.RecoveryAuthenticationProviderImpl;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationFilter;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationProvider;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationProviderImpl;
import org.molgenis.security.twofactor.service.OtpService;
import org.molgenis.security.twofactor.service.RecoveryService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.security.user.MolgenisUserDetailsChecker;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.security.user.UserDetailsServiceImpl;
import org.molgenis.web.i18n.HttpLocaleResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
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
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.header.writers.CacheControlHeadersWriter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.LocaleResolver;

@Import(DataServiceClientRegistrationRepository.class)
public abstract class MolgenisWebAppSecurityConfig extends WebSecurityConfigurerAdapter {
  private static final String ANONYMOUS_AUTHENTICATION_KEY = "anonymousAuthenticationKey";

  @Autowired private DataService dataService;

  @Autowired private UserService userService;

  @Autowired private AuthenticationSettings authenticationSettings;

  @Autowired private TokenFactory tokenFactory;

  @Autowired private UserFactory userFactory;

  @Autowired private OtpService otpService;

  @Autowired private TwoFactorAuthenticationService twoFactorAuthenticationService;

  @Autowired private RecoveryService recoveryService;

  @Autowired private UserAccountService userAccountService;

  @Autowired private ClientRegistrationRepository clientRegistrationRepository;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // do not write cache control headers for static resources
    RequestMatcher matcher =
        new NegatedRequestMatcher(
            new OrRequestMatcher(
                new AntPathRequestMatcher(PATTERN_CSS),
                new AntPathRequestMatcher(PATTERN_JS),
                new AntPathRequestMatcher(PATTERN_IMG),
                new AntPathRequestMatcher(PATTERN_FONTS)));

    DelegatingRequestMatcherHeaderWriter cacheControlHeaderWriter =
        new DelegatingRequestMatcherHeaderWriter(matcher, new CacheControlHeadersWriter());

    http.exceptionHandling().authenticationEntryPoint(delegatingEntryPoint());

    HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
    requestCache.setRequestMatcher(createWebRequestMatcher());
    http.requestCache().requestCache(requestCache);

    http.sessionManagement().invalidSessionStrategy(invalidSessionStrategy());

    // add default header options but use custom cache control header writer
    http.cors()
        .and()
        .headers()
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

    http.addFilterAfter(changePasswordFilter(), SwitchUserFilter.class);

    http.addFilterAfter(twoFactorAuthenticationFilter(), MolgenisChangePasswordFilter.class);
    http.authenticationProvider(twoFactorAuthenticationProvider());
    http.authenticationProvider(recoveryAuthenticationProvider());

    ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
        expressionInterceptUrlRegistry = http.authorizeRequests();
    configureUrlAuthorization(expressionInterceptUrlRegistry);

    expressionInterceptUrlRegistry
        .antMatchers(MolgenisLoginController.URI)
        .permitAll()
        .antMatchers(TwoFactorAuthenticationController.URI + "/**")
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
        .antMatchers("/plugin/app-ui-context")
        .permitAll()
        .antMatchers("/api/**")
        .permitAll()
        .antMatchers("/webjars/**")
        .permitAll()
        .antMatchers("/search")
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
        .antMatchers("/scripts/**/submit")
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
        .oauth2Login()
        .clientRegistrationRepository(clientRegistrationRepository)
        .authorizedClientService(authorizedClientService())
        .loginPage(MolgenisLoginController.URI)
        .failureUrl(MolgenisLoginController.URI)
        .userInfoEndpoint()
        .oidcUserService(oidcUserService())
        .and()
        .and()
        .logout()
        .deleteCookies("JSESSIONID")
        .addLogoutHandler(
            (req, res, auth) -> {
              if (req.getSession(false) != null
                  && req.getSession().getAttribute("continueWithUnsupportedBrowser") != null) {
                req.setAttribute("continueWithUnsupportedBrowser", true);
              }
            })
        .logoutSuccessHandler(
            (req, res, auth) -> {
              StringBuilder logoutSuccessUrl = new StringBuilder("/");
              if (req.getAttribute("continueWithUnsupportedBrowser") != null) {
                logoutSuccessUrl.append("?continueWithUnsupportedBrowser=true");
              }
              SimpleUrlLogoutSuccessHandler logoutSuccessHandler =
                  new SimpleUrlLogoutSuccessHandler();
              logoutSuccessHandler.setDefaultTargetUrl(logoutSuccessUrl.toString());
              logoutSuccessHandler.onLogoutSuccess(req, res, auth);
            })
        .and()
        .csrf()
        .disable();
  }

  @Bean
  public AuthenticationEntryPoint delegatingEntryPoint() {
    AuthenticationEntryPoint pluginEntryPoint =
        new LoginUrlAuthenticationEntryPoint(MolgenisLoginController.URI);

    LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> entryPoints = new LinkedHashMap<>();
    entryPoints.put(createWebRequestMatcher(), pluginEntryPoint);

    DelegatingAuthenticationEntryPoint delegatingEntryPoint =
        new DelegatingAuthenticationEntryPoint(entryPoints);
    delegatingEntryPoint.setDefaultEntryPoint(new Http403ForbiddenEntryPoint());

    return delegatingEntryPoint;
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring()
        .antMatchers(PATTERN_CSS)
        .antMatchers(PATTERN_IMG)
        .antMatchers(PATTERN_JS)
        .antMatchers(PATTERN_FONTS);
  }

  @Bean
  public AuthenticationProvider runAsAuthenticationProvider() {
    RunAsImplAuthenticationProvider provider = new RunAsImplAuthenticationProvider();
    provider.setKey("Job Execution");
    return provider;
  }

  protected abstract void configureUrlAuthorization(
      ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
          expressionInterceptUrlRegistry);

  protected abstract RoleHierarchy roleHierarchy();

  @Bean
  public AnonymousAuthenticationFilter anonymousAuthFilter() {
    return new AnonymousAuthenticationFilter(
        ANONYMOUS_AUTHENTICATION_KEY,
        SecurityUtils.ANONYMOUS_USERNAME,
        AuthorityUtils.createAuthorityList(SecurityUtils.AUTHORITY_ANONYMOUS));
  }

  @Bean
  public AnonymousAuthenticationProvider anonymousAuthenticationProvider() {
    return new AnonymousAuthenticationProvider(ANONYMOUS_AUTHENTICATION_KEY);
  }

  @Bean
  public TokenService tokenService() {
    return new DataServiceTokenService(
        new TokenGenerator(), dataService, userDetailsService(), tokenFactory);
  }

  @Bean
  public AuthenticationProvider tokenAuthenticationProvider() {
    return new TokenAuthenticationProvider(tokenService(), userDetailsChecker());
  }

  @Bean
  public Filter tokenAuthenticationFilter() {
    return new TokenAuthenticationFilter(tokenAuthenticationProvider());
  }

  @Bean
  public Filter changePasswordFilter() {
    return new MolgenisChangePasswordFilter(userService, redirectStrategy());
  }

  @Bean
  public TwoFactorAuthenticationFilter twoFactorAuthenticationFilter() {
    return new TwoFactorAuthenticationFilter(
        authenticationSettings,
        twoFactorAuthenticationService,
        redirectStrategy(),
        userAccountService);
  }

  @Bean
  public TwoFactorAuthenticationProvider twoFactorAuthenticationProvider() {
    return new TwoFactorAuthenticationProviderImpl(
        twoFactorAuthenticationService, otpService, recoveryService);
  }

  @Bean
  public RecoveryAuthenticationProvider recoveryAuthenticationProvider() {
    return new RecoveryAuthenticationProviderImpl(recoveryService);
  }

  @Bean
  public RedirectStrategy redirectStrategy() {
    return new DefaultRedirectStrategy();
  }

  @Bean
  public RoleHierarchy roleHierarchyBean() {
    return roleHierarchy();
  }

  @Bean
  public RoleVoter roleVoter() {
    return new RoleHierarchyVoter(roleHierarchy());
  }

  @Bean
  public GrantedAuthoritiesMapper roleHierarchyAuthoritiesMapper() {
    return new RoleHierarchyAuthoritiesMapper(roleHierarchy());
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new MolgenisPasswordEncoder(new BCryptPasswordEncoder());
  }

  @Bean
  @Override
  protected UserDetailsServiceImpl userDetailsService() {
    return new UserDetailsServiceImpl(dataService, roleHierarchyAuthoritiesMapper());
  }

  @Override
  @Bean
  public org.springframework.security.core.userdetails.UserDetailsService userDetailsServiceBean()
      throws Exception {
    return userDetailsService();
  }

  @Bean
  public UserDetailsChecker userDetailsChecker() {
    return new MolgenisUserDetailsChecker();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
    try {
      DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
      authenticationProvider.setPasswordEncoder(passwordEncoder());
      authenticationProvider.setUserDetailsService(userDetailsServiceBean());
      authenticationProvider.setPreAuthenticationChecks(userDetailsChecker());
      auth.authenticationProvider(authenticationProvider);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Bean
  public LoginUrlAuthenticationEntryPoint authenticationEntryPoint() {
    return new AjaxAwareLoginUrlAuthenticationEntryPoint(MolgenisLoginController.URI);
  }

  @Bean
  public InvalidSessionStrategy invalidSessionStrategy() {
    return new AjaxAwareInvalidSessionStrategy(
        MolgenisLoginController.URI + '?' + MolgenisLoginController.PARAM_SESSION_EXPIRED);
  }

  @Bean
  public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
  }

  @Bean
  public ResettableOAuth2AuthorizedClientService authorizedClientService() {
    return new ResettableOAuth2AuthorizedClientService(clientRegistrationRepository);
  }

  @Bean
  public OidcUserService oidcUserService() {
    return new MappedOidcUserService(oidcUserMapper(), userDetailsService());
  }

  @Autowired private OidcUserMappingFactory oidcUserMappingFactory;

  @Bean
  public OidcUserMapper oidcUserMapper() {
    return new OidcUserMapperImpl(dataService, oidcUserMappingFactory, userFactory);
  }

  @Autowired private HttpLocaleResolver httpLocaleResolver;

  @Bean
  public LocaleResolver localeResolver() {
    return httpLocaleResolver;
  }

  private RequestMatcher createWebRequestMatcher() {
    return new OrRequestMatcher(
        new AntPathRequestMatcher("/"),
        new AntPathRequestMatcher("/plugin/**"),
        new AntPathRequestMatcher("/menu/**"));
  }
}
