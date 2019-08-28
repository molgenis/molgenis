package org.molgenis.api.data.v3.model;

import java.util.Optional;
import org.molgenis.api.model.Query;

public class DeleteEntitiesRequest extends AbstractEntityTypeRequest {
  private Query q;

  public Optional<Query> getQ() {
    return Optional.ofNullable(q);
  }

  public void setQ(Query q) {
    this.q = q;
  }
}
