package org.molgenis.data.validation;

import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.EntityType;

public interface FetchValidator {
  /**
   * Validates the fetch and fixes invalid fetches.
   *
   * @param fetch fetch to validate
   * @param entityType entity type
   * @return new valid fetch
   */
  Fetch validateFetch(Fetch fetch, EntityType entityType);
}
