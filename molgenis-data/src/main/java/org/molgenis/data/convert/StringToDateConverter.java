package org.molgenis.data.convert;

import java.time.LocalDate;
import org.molgenis.data.util.MolgenisDateFormat;
import org.springframework.core.convert.converter.Converter;

public class StringToDateConverter implements Converter<String, LocalDate> {

  @Override
  public LocalDate convert(String source) {
    return MolgenisDateFormat.parseLocalDate(source);
  }
}
