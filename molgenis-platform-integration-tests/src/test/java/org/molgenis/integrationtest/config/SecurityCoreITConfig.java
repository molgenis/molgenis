package org.molgenis.integrationtest.config;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.security.core.utils.SecurityUtils.ROLE_SYSTEM;

import java.util.Collection;
import org.molgenis.security.token.RunAsUserTokenFactory;
import org.molgenis.security.user.MolgenisUserDetailsChecker;
import org.molgenis.security.user.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Configuration
@Import({RunAsUserTokenFactory.class, MolgenisUserDetailsChecker.class})
public class SecurityCoreITConfig {
  @SuppressWarnings("unchecked")
  @Bean
  public UserDetailsServiceImpl userDetailsService() {
    UserDetailsServiceImpl userDetailsService = mock(UserDetailsServiceImpl.class);
    UserDetails adminUserDetails = mock(UserDetails.class);
    when(adminUserDetails.isEnabled()).thenReturn(true);
    Collection authorities = singleton(new SimpleGrantedAuthority(ROLE_SYSTEM));
    when(adminUserDetails.getAuthorities()).thenReturn(authorities);
    when(userDetailsService.loadUserByUsername("admin")).thenReturn(adminUserDetails);
    return userDetailsService;
  }
}
