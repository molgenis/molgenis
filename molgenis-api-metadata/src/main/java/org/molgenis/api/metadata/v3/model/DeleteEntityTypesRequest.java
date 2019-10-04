package org.molgenis.api.metadata.v3.model;

import javax.validation.constraints.NotNull;
import org.molgenis.api.model.Query;

public class DeleteEntityTypesRequest {
  @NotNull private Query q;

  public Query getQ() {
    return q;
  }

  public void setQ(Query q) {
    this.q = q;
  }
}
