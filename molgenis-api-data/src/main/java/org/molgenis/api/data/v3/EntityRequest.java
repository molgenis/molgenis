package org.molgenis.api.data.v3;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;

public class EntityRequest {
  @NotNull private String entityTypeId;
  @NotNull private String entityId;
  private List<String> filter;
  private List<String> expand;

  public String getEntityTypeId() {
    return entityTypeId;
  }

  public void setEntityTypeId(String entityTypeId) {
    this.entityTypeId = entityTypeId;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public Set<String> getFilter() {
    return filter != null ? ImmutableSet.copyOf(filter) : ImmutableSet.of();
  }

  public void setFilter(List<String> filter) {
    this.filter = filter;
  }

  public Set<String> getExpand() {
    return expand != null ? ImmutableSet.copyOf(expand) : ImmutableSet.of();
  }

  public void setExpand(List<String> expand) {
    this.expand = expand;
  }
}
