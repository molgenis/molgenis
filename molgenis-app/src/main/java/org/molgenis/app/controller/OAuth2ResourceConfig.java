package org.molgenis.app.controller;

import static org.molgenis.security.MolgenisWebAppSecurityConfig.ANONYMOUS_AUTHENTICATION_KEY;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableResourceServer
public class OAuth2ResourceConfig extends ResourceServerConfigurerAdapter {

  private TokenExtractor tokenExtractor = new BearerTokenExtractor();

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.anonymous()
        .key(ANONYMOUS_AUTHENTICATION_KEY)
        .principal(SecurityUtils.ANONYMOUS_USERNAME)
        .authorities(SecurityUtils.AUTHORITY_ANONYMOUS);

    http.anonymous().and().authorizeRequests().antMatchers("/").permitAll();

    http.addFilterAfter(contextClearer(), AbstractPreAuthenticatedProcessingFilter.class)
        .authorizeRequests()
        .anyRequest()
        .authenticated()
        .and()
        .httpBasic();
  }

  private OncePerRequestFilter contextClearer() {
    return new OncePerRequestFilter() {
      @Override
      protected void doFilterInternal(
          HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {
        if (tokenExtractor.extract(request) == null) {
          SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
      }
    };
  }
}
