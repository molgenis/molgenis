package org.molgenis.api.data.v3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class EntitiesRequest {
  @NotNull private String entityTypeId;
  private List<String> filter;
  private List<String> expand;

  @Min(0)
  private int number = 0;

  @Min(0)
  @Max(100)
  private int size = 100;

  private List<String> sort;
  // TODO RSQL query
  private String q;

  public String getEntityTypeId() {
    return entityTypeId;
  }

  public void setEntityTypeId(String entityTypeId) {
    this.entityTypeId = entityTypeId;
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

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public List<String> getSort() {
    return sort != null ? ImmutableList.copyOf(sort) : ImmutableList.of();
  }

  public void setSort(List<String> sort) {
    this.sort = sort;
  }

  public String getQ() {
    return q;
  }

  public void setQ(String q) {
    this.q = q;
  }
}
