package org.molgenis.api.data.v3.model;

import java.util.Optional;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Sort;

public class ReadEntitiesRequest extends AbstractReadRequest {
  @Min(0)
  private int number = 0;

  @Min(1)
  @Max(100)
  private int size = 100;

  private Sort sort;

  private Query q;

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

  public Sort getSort() {
    return sort != null ? sort : Sort.EMPTY_SORT;
  }

  public void setSort(Sort sort) {
    this.sort = sort;
  }

  public Optional<Query> getQ() {
    return Optional.ofNullable(q);
  }

  public void setQ(Query q) {
    this.q = q;
  }
}
