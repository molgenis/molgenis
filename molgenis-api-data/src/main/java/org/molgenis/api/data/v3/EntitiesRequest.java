package org.molgenis.api.data.v3;

import java.util.List;
import java.util.Optional;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;

public class EntitiesRequest extends BaseEntityRequest {
  @Min(0)
  private int number = 0;

  @Min(1)
  @Max(100)
  private int size = 100;

  private List<String> sort;
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
    if (sort == null) {
      return Optional.empty();
    }

    Sort sortObj = new Sort();
    sort.forEach(
        sortItem -> {
          if (sortItem.charAt(0) == '+') {
            sortObj.on(sortItem.substring(1), Direction.ASC);
          } else if (sortItem.charAt(0) == '-') {
            sortObj.on(sortItem.substring(1), Direction.DESC);
          } else {
            sortObj.on(sortItem);
          }
        });
    return Optional.ofNullable(sortObj);
  }

  public void setSort(List<String> sort) {
    this.sort = sort;
  }

  public Optional<String> getQ() {
    return Optional.ofNullable(q);
  }

  public void setQ(String q) {
    this.q = q;
  }
}
