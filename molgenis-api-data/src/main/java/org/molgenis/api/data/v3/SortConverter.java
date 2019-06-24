package org.molgenis.api.data.v3;

import org.springframework.core.convert.converter.Converter;

public class SortConverter implements Converter<String, Sort> {
  private static final String ORDER_ASC_PREFIX = "+";
  private static final String ORDER_DESC_PREFIX = "-";

  @Override
  public Sort convert(String source) {
    Sort sort = new Sort();
    for (String attr : source.split(",")) {
      Sort.Direction direction;
      if (attr.startsWith(ORDER_DESC_PREFIX)) {
        direction = Sort.Direction.DESC;
        attr = attr.substring(ORDER_DESC_PREFIX.length());
      } else if (attr.startsWith(ORDER_ASC_PREFIX)) {
        direction = Sort.Direction.ASC;
        attr = attr.substring(ORDER_ASC_PREFIX.length());
      } else {
        direction = Sort.Direction.ASC;
      }
      sort.on(attr, direction);
    }
    return sort;
  }
}
