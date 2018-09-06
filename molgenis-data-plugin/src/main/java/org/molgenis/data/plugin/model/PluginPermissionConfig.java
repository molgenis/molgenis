package org.molgenis.data.plugin.model;

import static java.util.Objects.requireNonNull;

import javax.annotation.PostConstruct;
import org.molgenis.i18n.PropertiesMessageSource;
import org.molgenis.security.core.PermissionRegistry;
import org.molgenis.security.core.PermissionSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PluginPermissionConfig {
  private final PermissionRegistry permissionRegistry;

  public PluginPermissionConfig(PermissionRegistry permissionRegistry) {
    this.permissionRegistry = requireNonNull(permissionRegistry);
  }

  @PostConstruct
  public void registerPermissions() {
    permissionRegistry.addMapping(PluginPermission.VIEW_PLUGIN, PermissionSet.READ);
  }

  @Bean
  public PropertiesMessageSource dataPluginMessageSource() {
    return new PropertiesMessageSource("data-plugin");
  }
}
