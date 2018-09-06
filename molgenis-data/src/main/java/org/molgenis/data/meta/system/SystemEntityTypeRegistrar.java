package org.molgenis.data.meta.system;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Discovers and registers system entity meta data with the system entity meta data registry.
 *
 * @see SystemEntityType
 * @see SystemEntityTypeRegistry
 */
@Component
public class SystemEntityTypeRegistrar {
  private final SystemEntityTypeRegistry systemEntityTypeRegistry;

  public SystemEntityTypeRegistrar(SystemEntityTypeRegistry systemEntityTypeRegistry) {
    this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
  }

  public void register(ContextRefreshedEvent event) {
    ApplicationContext ctx = event.getApplicationContext();
    Map<String, SystemEntityType> systemEntityTypeMap = ctx.getBeansOfType(SystemEntityType.class);
    systemEntityTypeMap.values().forEach(this::register);
  }

  private void register(SystemEntityType systemEntityType) {
    systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
  }
}
