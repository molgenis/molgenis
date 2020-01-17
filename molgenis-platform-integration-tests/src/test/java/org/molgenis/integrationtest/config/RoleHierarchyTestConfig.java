package org.molgenis.integrationtest.config;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.molgenis.data.DataService;
import org.molgenis.data.config.DataConfig;
import org.molgenis.data.security.DataserviceRoleHierarchy;
import org.molgenis.data.security.auth.CachedRoleHierarchy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;

@Import(DataConfig.class)
@Configuration
public class RoleHierarchyTestConfig {
  private final DataService dataService;

  RoleHierarchyTestConfig(DataService dataService) {
    this.dataService = requireNonNull(dataService);
  }

  // Springs @WithMockUser doesn't play nice with CachedRoleHierarchyImpl so we use a
  // DataserviceRoleHierarchy wrapper which (surprisingly) does play nice with mock users.
  @Bean
  public CachedRoleHierarchy cachedRoleHierarchy() {
    DataserviceRoleHierarchy dataserviceRoleHierarchy = new DataserviceRoleHierarchy(dataService);
    return new CachedRoleHierarchy() {
      @Override
      public void markRoleHierarchyCacheDirty() {
        // no operation
      }

      @Override
      public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(
          Collection<? extends GrantedAuthority> authorities) {
        return dataserviceRoleHierarchy.getReachableGrantedAuthorities(authorities);
      }
    };
  }
}
