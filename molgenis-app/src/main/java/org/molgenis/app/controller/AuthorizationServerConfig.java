package org.molgenis.app.controller;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
  @Autowired private AuthenticationManager authenticationManager;
  @Autowired private PasswordEncoder passwordEncoder;

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients
        .inMemory()
        .withClient("molgenis")
        .authorizedGrantTypes("password")
        .authorities("ROLE_CLIENT", "ROLE_USER")
        .scopes("read");
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer security) {
    security
        .passwordEncoder(passwordEncoder)
        .tokenKeyAccess("permitAll()")
        .checkTokenAccess("isAuthenticated()")
        .allowFormAuthenticationForClients();
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
    tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), accessTokenConverter()));

    endpoints
        .tokenStore(tokenStore())
        .tokenEnhancer(tokenEnhancerChain)
        .authenticationManager(authenticationManager);
  }

  @Bean
  public TokenEnhancer tokenEnhancer() {
    return new CustomTokenEnhancer();
  }

  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(accessTokenConverter());
  }

  @Bean
  public JwtAccessTokenConverter accessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    converter.setSigningKey("123");
    return converter;
  }

  @Bean
  @Primary
  public DefaultTokenServices tokenServices() {
    DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
    defaultTokenServices.setTokenStore(tokenStore());
    defaultTokenServices.setSupportRefreshToken(true);
    return defaultTokenServices;
  }
}
