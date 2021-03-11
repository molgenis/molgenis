package org.molgenis.security;

import org.molgenis.data.DataService;
import org.molgenis.data.security.DataserviceRoleHierarchy;
import org.molgenis.data.security.auth.CachedRoleHierarchyImpl;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.data.security.auth.VOGroupService;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.data.security.permission.VOGroupRoleMembershipService;
import org.molgenis.data.security.user.UserService;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.security.core.MolgenisPasswordEncoder;
import org.molgenis.security.oidc.MappedOidcUserService;
import org.molgenis.security.oidc.OidcUserMapper;
import org.molgenis.security.oidc.OidcUserMapperImpl;
import org.molgenis.security.oidc.ResettableOAuth2AuthorizedClientService;
import org.molgenis.security.oidc.model.OidcUserMappingFactory;
import org.molgenis.security.user.MolgenisUserDetailsChecker;
import org.molgenis.security.user.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
public class SecurityConfig {

  @Bean
  public UserDetailsServiceImpl userDetailsService(
      GrantedAuthoritiesMapper authoritiesMapper,
      UserService userService,
      RoleMembershipService roleMembershipService) {
    return new UserDetailsServiceImpl(authoritiesMapper, userService, roleMembershipService);
  }

  @Bean
  public UserDetailsChecker userDetailsChecker() {
    return new MolgenisUserDetailsChecker();
  }

  @Bean
  public RoleHierarchy roleHierarchy(
      DataService dataService, TransactionManager transactionManager) {
    return new CachedRoleHierarchyImpl(
        new DataserviceRoleHierarchy(dataService), transactionManager);
  }

  @Bean
  public MappedOidcUserService oidcUserService(
      OidcUserMapper oidcUserMapper,
      UserDetailsServiceImpl userDetailsService,
      DataService dataService,
      VOGroupService voGroupService,
      VOGroupRoleMembershipService voGroupRoleMembershipService) {
    return new MappedOidcUserService(
        oidcUserMapper,
        userDetailsService,
        dataService,
        voGroupService,
        voGroupRoleMembershipService);
  }

  @Bean
  public OidcUserMapper oidcUserMapper(
      DataService dataService,
      OidcUserMappingFactory oidcUserMappingFactory,
      UserFactory userFactory) {
    return new OidcUserMapperImpl(dataService, oidcUserMappingFactory, userFactory);
  }

  @Bean
  public OAuth2AuthorizedClientService authorizedClientService(
      ClientRegistrationRepository clientRegistrationRepository) {
    return new ResettableOAuth2AuthorizedClientService(clientRegistrationRepository);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new MolgenisPasswordEncoder(new BCryptPasswordEncoder());
  }
}
