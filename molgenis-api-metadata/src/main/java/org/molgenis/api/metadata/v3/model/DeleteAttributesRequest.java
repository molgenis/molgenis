package org.molgenis.api.metadata.v3.model;

import javax.validation.constraints.NotNull;
import org.molgenis.api.model.Query;

public class DeleteAttributesRequest {
  @NotNull private String entityTypeId;
  @NotNull private Query q;

  public String getEntityTypeId() {
    return entityTypeId;
  }

  public void setEntityTypeId(String entityTypeId) {
    this.entityTypeId = entityTypeId;
  }

  public Query getQ() {
    return q;
  }

  public void setQ(Query q) {
    this.q = q;
  }
}
