package org.molgenis.app;

import static org.molgenis.security.core.utils.SecurityUtils.getPluginReadAuthority;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.security.MolgenisRoleHierarchy;
import org.molgenis.security.MolgenisWebAppSecurityConfig;
import org.molgenis.ui.security.MolgenisAccessDecisionVoter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.expression.WebExpressionVoter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebAppSecurityConfig extends MolgenisWebAppSecurityConfig
{
	// TODO automate URL authorization configuration (ticket #2133)
	@Override
	protected void configureUrlAuthorization(
			ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry)
	{
		List<AccessDecisionVoter<?>> listOfVoters = new ArrayList<AccessDecisionVoter<?>>();
		listOfVoters.add(new WebExpressionVoter());
		listOfVoters.add(new MolgenisAccessDecisionVoter());
		expressionInterceptUrlRegistry.accessDecisionManager(new AffirmativeBased(listOfVoters));

		expressionInterceptUrlRegistry.antMatchers("/").permitAll()
				// DAS datasource uses the database, unauthenticated users can
				// not see any data
				.antMatchers("/das/**").permitAll()

				.antMatchers("/myDas/**").permitAll()

				.antMatchers("/annotators/**").authenticated()

				.antMatchers("/omim/**").authenticated()

				.antMatchers("/phenotips/**").authenticated()

				.antMatchers("/gavin/**").authenticated()

				.antMatchers("/charts/**").authenticated();
	}

	@Override
	protected List<GrantedAuthority> createAnonymousUserAuthorities()
	{
		String homePluginReadAuthority = getPluginReadAuthority("home");
		return createAuthorityList(homePluginReadAuthority);
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