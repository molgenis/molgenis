package org.molgenis.data.convert;

import java.time.Instant;
import org.molgenis.data.util.MolgenisDateFormat;
import org.springframework.core.convert.converter.Converter;

public class StringToDateTimeConverter implements Converter<String, Instant> {

  @Override
  public Instant convert(String source) {
    return MolgenisDateFormat.parseInstant(source);
  }
}
