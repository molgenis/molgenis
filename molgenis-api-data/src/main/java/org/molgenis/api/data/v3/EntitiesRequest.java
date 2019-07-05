package org.molgenis.api.data.v3;

import java.util.Optional;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.molgenis.api.model.Sort;

public class EntitiesRequest extends BaseEntityRequest {
  @Min(0)
  private int number = 0;

  @Min(1)
  @Max(100)
  private int size = 100;

  private Sort sort;
  // TODO RSQL query
  private String q;

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

  public Optional<Sort> getSort() {
    return Optional.ofNullable(sort);
  }

  public void setSort(Sort sort) {
    this.sort = sort;
  }

  public Optional<String> getQ() {
    return Optional.ofNullable(q);
  }

  public void setQ(String q) {
    this.q = q;
  }
}
