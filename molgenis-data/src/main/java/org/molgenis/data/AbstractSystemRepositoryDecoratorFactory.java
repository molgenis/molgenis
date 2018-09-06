package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.SystemEntityType;

/**
 * Repository decorator factory for a specific {@link SystemEntityType}.
 *
 * @param <E>
 * @param <M>
 */
public abstract class AbstractSystemRepositoryDecoratorFactory<
        E extends Entity, M extends SystemEntityType>
    implements SystemRepositoryDecoratorFactory<E, M> {
  private final M entityType;

  public AbstractSystemRepositoryDecoratorFactory(M entityType) {
    this.entityType = requireNonNull(entityType);
  }

  @Override
  public M getEntityType() {
    return entityType;
  }
}
