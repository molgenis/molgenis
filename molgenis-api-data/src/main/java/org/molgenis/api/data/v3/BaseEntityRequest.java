package org.molgenis.api.data.v3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

public abstract class BaseEntityRequest {
  @NotNull private String entityTypeId;
  private List<String> filter;
  private List<String> expand;

  public String getEntityTypeId() {
    return entityTypeId;
  }

  public void setEntityTypeId(String entityTypeId) {
    this.entityTypeId = entityTypeId;
  }

  public Selection getFilter() {
    // TODO move to convertor / remove code duplication
    Map<String, Selection> filterMap;
    if (filter != null && !filter.isEmpty()) {
      filterMap = new HashMap<>();
      filter.forEach(key -> filterMap.put(key, Selection.FULL_SELECTION));
    } else {
      filterMap = null;
    }
    return filterMap != null ? new Selection(filterMap) : Selection.FULL_SELECTION;
  }

  public void setFilter(List<String> filter) {
    this.filter = filter;
  }

  public Selection getExpand() {
    // TODO move to convertor / remove code duplication
    Map<String, Selection> expandMap;
    if (expand != null && !expand.isEmpty()) {
      expandMap = new HashMap<>();
      expand.forEach(key -> expandMap.put(key, Selection.EMPTY_SELECTION));
    } else {
      expandMap = null;
    }
    return expandMap != null ? new Selection(expandMap) : Selection.EMPTY_SELECTION;
  }

  public void setExpand(List<String> expand) {
    this.expand = expand;
  }
}
