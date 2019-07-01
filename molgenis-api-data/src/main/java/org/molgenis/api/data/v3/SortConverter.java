package org.molgenis.api.data.v3;

import org.springframework.core.convert.converter.Converter;

public class SortConverter implements Converter<String, Sort> {

  @Override
  public Sort convert(String source) {
    try {
      return new SortParser(source).parse();
    } catch (ParseException e) {
      throw new RuntimeException(e); // TODO proper exception handling
    }
  }
}
