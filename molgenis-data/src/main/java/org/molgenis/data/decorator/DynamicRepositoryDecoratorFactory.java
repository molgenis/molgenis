package org.molgenis.data.decorator;

import java.util.Map;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;

/**
 * Repository decorator factory that creates decorated {@link Repository repositories} for specific
 * entity types. Used to decorate (system) entity types dynamically.
 *
 * @see RepositoryDecoratorFactory
 */
public interface DynamicRepositoryDecoratorFactory<E extends Entity> {
  String getId();

  String getLabel();

  String getDescription();

  /** @return JSON schema for the parameters, null when no parameters */
  String getSchema();

  /**
   * Creates a decorated repository based on the given {@link Repository}
   *
   * @param repository undecorated repository
   * @return decorated repository
   */
  Repository<E> createDecoratedRepository(Repository<E> repository, Map<String, Object> parameters);
}
