package org.molgenis.api.data.v3;

import org.molgenis.data.Entity;

public interface EntityService {
  Entity getEntity(String entityTypeId, String entityId);
}
