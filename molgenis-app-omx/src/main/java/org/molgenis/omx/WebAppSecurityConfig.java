package org.molgenis.omx;

import static org.molgenis.framework.ui.ResourcePathPatterns.PATTERN_CSS;
import static org.molgenis.framework.ui.ResourcePathPatterns.PATTERN_IMG;
import static org.molgenis.framework.ui.ResourcePathPatterns.PATTERN_JS;
import static org.molgenis.security.core.utils.SecurityUtils.getPluginReadAuthority;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.security.MolgenisRoleHierarchy;
import org.molgenis.security.MolgenisWebAppSecurityConfig;
import org.molgenis.ui.security.MolgenisAccessDecisionVoter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.header.writers.CacheControlHeadersWriter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter.XFrameOptionsMode;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebAppSecurityConfig extends MolgenisWebAppSecurityConfig
{
	@Autowired
	private MolgenisAccessDecisionVoter molgenisAccessDecisionVoter;

	@Autowired
	private RoleVoter roleVoter;
	
	protected void configure(HttpSecurity http) throws Exception
	{
		super.configure(http);
		reconfigureHeaderWriters(http);
	}
	
	/**
	 * Reconfigures the HTTP header writers to allow same origin frames.
	 * This is needed to allow an iframe to upload files in the study data request form.
	 * The "normal" xhr request without iframe, using FormData, does not work in IE9.
	 */
	private static void reconfigureHeaderWriters(HttpSecurity http) throws Exception
	{
		http.headers().disable();
		// do not write cache control headers for static resources
		RequestMatcher matcher = new NegatedRequestMatcher(new OrRequestMatcher(new AntPathRequestMatcher(PATTERN_CSS),
				new AntPathRequestMatcher(PATTERN_JS), new AntPathRequestMatcher(PATTERN_IMG)));

		DelegatingRequestMatcherHeaderWriter cacheControlHeaderWriter = new DelegatingRequestMatcherHeaderWriter(
				matcher, new CacheControlHeadersWriter());

		XFrameOptionsHeaderWriter xframeOptionsWriter = new XFrameOptionsHeaderWriter(XFrameOptionsMode.SAMEORIGIN);

		// add default header options but use custom cache control header writer
		http.headers().contentTypeOptions().xssProtection().httpStrictTransportSecurity().addHeaderWriter(xframeOptionsWriter)
				.addHeaderWriter(cacheControlHeaderWriter);
	}

	// TODO automate URL authorization configuration (ticket #2133)
	@Override
	protected void configureUrlAuthorization(
			ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry)
	{
		@SuppressWarnings("rawtypes")
		List<AccessDecisionVoter> listOfVoters = new ArrayList<AccessDecisionVoter>();
		listOfVoters.add(new WebExpressionVoter());
		listOfVoters.add(new MolgenisAccessDecisionVoter());
		expressionInterceptUrlRegistry.accessDecisionManager(new AffirmativeBased(listOfVoters));

		expressionInterceptUrlRegistry.antMatchers("/").permitAll()
		// DAS datasource uses the database, unauthenticated users can
		// not see any data
				.antMatchers("/das/**").permitAll()

				.antMatchers("/myDas/**").permitAll()

				.antMatchers("/annotators/**").authenticated()

				.antMatchers("/diseasematcher/**").authenticated()

				.antMatchers("/omim/**").authenticated()

				.antMatchers("/charts/**").authenticated();
	}

	@Override
	protected List<GrantedAuthority> createAnonymousUserAuthorities()
	{
		String s = getPluginReadAuthority("home");
		return AuthorityUtils.createAuthorityList(s);
	}

	@Override
	public RoleHierarchy roleHierarchy()
	{
		return new MolgenisRoleHierarchy();
	}

	@Bean
	public MolgenisAccessDecisionVoter molgenisAccessDecisionVoter()
	{
		return new MolgenisAccessDecisionVoter();
	}
}