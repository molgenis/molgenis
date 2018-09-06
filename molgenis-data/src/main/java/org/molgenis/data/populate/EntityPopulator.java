package org.molgenis.data.populate;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.Entity;
import org.springframework.stereotype.Component;

/** Populate entity values for auto attributes */
@Component
public class EntityPopulator {
  private final AutoValuePopulator autoValuePopulator;
  private final DefaultValuePopulator defaultValuePopulator;

  public EntityPopulator(
      AutoValuePopulator autoValuePopulator, DefaultValuePopulator defaultValuePopulator) {
    this.autoValuePopulator = requireNonNull(autoValuePopulator);
    this.defaultValuePopulator = requireNonNull(defaultValuePopulator);
  }

  public void populate(Entity entity) {
    autoValuePopulator.populate(entity);
    defaultValuePopulator.populate(entity);
  }
}
