package org.molgenis.data.meta.system;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.molgenis.data.meta.SystemPackage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Discovers and registers system packages with the system package registry.
 *
 * @see SystemPackage
 * @see SystemPackageRegistry
 */
@Component
public class SystemPackageRegistrar {
  private final SystemPackageRegistry systemPackageRegistry;

  public SystemPackageRegistrar(SystemPackageRegistry systemPackageRegistry) {
    this.systemPackageRegistry = requireNonNull(systemPackageRegistry);
  }

  public void register(ContextRefreshedEvent event) {
    ApplicationContext ctx = event.getApplicationContext();
    Map<String, SystemPackage> systemPackageMap = ctx.getBeansOfType(SystemPackage.class);
    systemPackageMap.values().forEach(this::register);
  }

  private void register(SystemPackage systemPackage) {
    systemPackageRegistry.addSystemPackage(systemPackage);
  }
}
