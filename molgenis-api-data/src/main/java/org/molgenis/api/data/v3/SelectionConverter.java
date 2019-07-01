package org.molgenis.api.data.v3;

import org.springframework.core.convert.converter.Converter;

public class SelectionConverter implements Converter<String, Selection> {

  @Override
  public Selection convert(String source) {
    if (source.isEmpty()) {
      return Selection.FULL_SELECTION;
    }
    try {
      return new SelectionParser(source).parse();
    } catch (ParseException e) {
      throw new RuntimeException(e); // TODO proper exception handling
    }
  }
}
