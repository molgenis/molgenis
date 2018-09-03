package org.molgenis.bootstrap.populate;

import java.util.Collection;
import org.molgenis.data.Entity;

/**
 * Registry of application system entities used to populate an empty database. Classes implementing
 * this interface must be annotated with as {@link org.springframework.stereotype.Component
 * Component}.
 */
public interface SystemEntityRegistry {
  /**
   * Returns the system entities to populate an empty database
   *
   * @return system entities to populate an empty database
   */
  Collection<Entity> getEntities();
}
