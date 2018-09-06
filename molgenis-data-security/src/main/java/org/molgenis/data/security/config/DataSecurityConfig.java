package org.molgenis.data.security.config;

import static org.molgenis.data.security.auth.GroupPermission.*;
import static org.molgenis.security.core.PermissionSet.*;

import javax.annotation.PostConstruct;
import org.molgenis.i18n.PropertiesMessageSource;
import org.molgenis.security.core.PermissionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSecurityConfig {
  @Autowired private PermissionRegistry permissionRegistry;

  public static final String NAMESPACE = "data-security";

  @Bean
  public PropertiesMessageSource dataSecurityMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }

  @PostConstruct
  public void registerGroupPermissions() {
    permissionRegistry.addMapping(ADD_MEMBERSHIP, WRITEMETA);
    permissionRegistry.addMapping(UPDATE_MEMBERSHIP, WRITEMETA);
    permissionRegistry.addMapping(REMOVE_MEMBERSHIP, WRITEMETA);
    permissionRegistry.addMapping(VIEW_MEMBERSHIP, WRITEMETA, WRITE, READ);
    permissionRegistry.addMapping(VIEW, WRITEMETA, WRITE, READ);
  }
}
