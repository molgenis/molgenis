package org.molgenis.api.data.v3;

import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.molgenis.api.model.Query;

public class DeleteEntitiesRequest {
  @NotNull private String entityTypeId;

  public String getEntityTypeId() {
    return entityTypeId;
  }

  public void setEntityTypeId(String entityTypeId) {
    this.entityTypeId = entityTypeId;
  }

  private Query q;

  public Optional<Query> getQ() {
    return Optional.ofNullable(q);
  }

  public void setQ(Query q) {
    this.q = q;
  }
}
