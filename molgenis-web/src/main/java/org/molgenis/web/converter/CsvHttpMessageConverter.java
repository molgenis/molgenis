package org.molgenis.web.converter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import org.molgenis.data.EntityCollection;
import org.molgenis.data.csv.CsvWriter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

/** Converts an EntityCollection to comma separated values */
public class CsvHttpMessageConverter extends BaseHttpMessageConverter<EntityCollection> {

  public CsvHttpMessageConverter() {
    super(new MediaType("text", "csv", BaseHttpMessageConverter.DEFAULT_CHARSET));
  }

  @Override
  protected void writeInternal(EntityCollection entities, HttpOutputMessage outputMessage)
      throws IOException {
    Charset charset = getCharset(outputMessage.getHeaders());
    try (CsvWriter csvWriter =
        new CsvWriter(new OutputStreamWriter(outputMessage.getBody(), charset))) {
      csvWriter.writeAttributeNames(entities.getAttributeNames());
      csvWriter.add(entities.stream());
    }
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return EntityCollection.class.isAssignableFrom(clazz);
  }

  @Override
  protected EntityCollection readInternal(
      Class<? extends EntityCollection> clazz, HttpInputMessage inputMessage) throws IOException {
    throw new UnsupportedOperationException();
  }
}
