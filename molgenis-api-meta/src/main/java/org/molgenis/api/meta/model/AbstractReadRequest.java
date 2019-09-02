package org.molgenis.api.meta.model;

import org.molgenis.api.model.Selection;

abstract class AbstractReadRequest {
  private Selection filter;
  private Selection expand;

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
