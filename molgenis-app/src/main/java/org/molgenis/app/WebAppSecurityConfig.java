package org.molgenis.app;

import java.util.ArrayList;
import java.util.List;
import org.molgenis.core.ui.security.MolgenisAccessDecisionVoter;
import org.molgenis.data.postgresql.DatabaseConfig;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.security.MolgenisWebAppSecurityConfig;
import org.molgenis.security.acl.AclConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.web.access.expression.WebExpressionVoter;

@Configuration
@Import({AclConfig.class, DatabaseConfig.class, DataServiceImpl.class})
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebAppSecurityConfig extends MolgenisWebAppSecurityConfig {

  @Override
  protected void configureUrlAuthorization(
      ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
          expressionInterceptUrlRegistry) {
    List<AccessDecisionVoter<?>> listOfVoters = new ArrayList<>();
    listOfVoters.add(new WebExpressionVoter());
    listOfVoters.add(molgenisAccessDecisionVoter());
    expressionInterceptUrlRegistry.accessDecisionManager(new AffirmativeBased(listOfVoters));

    expressionInterceptUrlRegistry.antMatchers("/").permitAll();
  }

  @Bean
  public MolgenisAccessDecisionVoter molgenisAccessDecisionVoter() {
    return new MolgenisAccessDecisionVoter();
  }
}
