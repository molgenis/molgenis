package org.molgenis.app;

import org.molgenis.security.MolgenisWebAppSecurityConfig;
import org.molgenis.ui.security.MolgenisAccessDecisionVoter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import java.util.ArrayList;
import java.util.List;

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
		List<AccessDecisionVoter<?>> listOfVoters = new ArrayList<>();
		listOfVoters.add(new WebExpressionVoter());
		listOfVoters.add(new MolgenisAccessDecisionVoter());
		expressionInterceptUrlRegistry.accessDecisionManager(new AffirmativeBased(listOfVoters));

		expressionInterceptUrlRegistry.antMatchers("/").permitAll()

									  .antMatchers("/fdp/**").permitAll()

									  .antMatchers("/annotators/**").authenticated()

									  .antMatchers("/charts/**").authenticated();
	}

	@Bean
	public MolgenisAccessDecisionVoter molgenisAccessDecisionVoter()
	{
		return new MolgenisAccessDecisionVoter();
	}
}