package org.molgenis.api.data.v3;

import javax.validation.constraints.NotNull;
import org.molgenis.api.model.Selection;

public abstract class BaseEntityRequest {
  @NotNull private String entityTypeId;
  private Selection filter;
  private Selection expand;

  public String getEntityTypeId() {
    return entityTypeId;
  }

  public void setEntityTypeId(String entityTypeId) {
    this.entityTypeId = entityTypeId;
  }

  public Selection getFilter() {
    return filter != null ? filter : Selection.FULL_SELECTION;
  }

  public void setFilter(Selection filter) {
    this.filter = filter;
  }

  public Selection getExpand() {
    return expand != null ? expand : Selection.EMPTY_SELECTION;
  }

  public void setExpand(Selection expand) {
    this.expand = expand;
  }
}
