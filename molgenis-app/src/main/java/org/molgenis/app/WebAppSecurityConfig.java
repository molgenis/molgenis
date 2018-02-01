package org.molgenis.app;

import org.molgenis.core.ui.security.MolgenisAccessDecisionVoter;
import org.molgenis.security.MolgenisRoleHierarchy;
import org.molgenis.security.MolgenisWebAppSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.acls.jdbc.AclConfig;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Import(AclConfig.class)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebAppSecurityConfig extends MolgenisWebAppSecurityConfig
{
	@Autowired
	private MolgenisAccessDecisionVoter molgenisAccessDecisionVoter;

	@Autowired
	private RoleVoter roleVoter;

	// TODO automate URL authorization configuration (ticket #2133)
	@Override
	protected void configureUrlAuthorization(
			ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry)
	{
		List<AccessDecisionVoter<?>> listOfVoters = new ArrayList<>();
		listOfVoters.add(new WebExpressionVoter());
		listOfVoters.add(new MolgenisAccessDecisionVoter());
		expressionInterceptUrlRegistry.accessDecisionManager(new AffirmativeBased(listOfVoters));

		expressionInterceptUrlRegistry.antMatchers("/").permitAll()

									  .antMatchers("/fdp/**").permitAll()

									  .antMatchers("/annotators/**").authenticated();
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