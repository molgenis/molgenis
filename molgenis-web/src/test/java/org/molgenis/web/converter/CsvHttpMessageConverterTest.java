package org.molgenis.web.converter;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityCollection;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;

class CsvHttpMessageConverterTest extends AbstractMockitoTest {

  private CsvHttpMessageConverter csvHttpMessageConverter;

  @BeforeEach
  void setUpBeforeMethod() {
    csvHttpMessageConverter = new CsvHttpMessageConverter();
  }

  @Test
  void testWriteInternal() throws IOException {
    List<String> attrName = asList("attr0", "attr1");
    Entity entity0 = mock(Entity.class);
    Entity entity1 = mock(Entity.class);
    EntityCollection entityCollection = mock(EntityCollection.class);
    when(entityCollection.getAttributeNames()).thenReturn(attrName);
    when(entityCollection.stream()).thenReturn(Stream.of(entity0, entity1));

    HttpHeaders httpHeaders = mock(HttpHeaders.class);
    when(httpHeaders.getContentType()).thenReturn(APPLICATION_JSON_UTF8);

    OutputStream outputStream = mock(OutputStream.class);
    HttpOutputMessage outputMessage = mock(HttpOutputMessage.class);
    when(outputMessage.getHeaders()).thenReturn(httpHeaders);
    when(outputMessage.getBody()).thenReturn(outputStream);

    csvHttpMessageConverter.writeInternal(entityCollection, outputMessage);
    verify(outputStream).close();
  }

  @Test
  void testSupports() {
    assertTrue(csvHttpMessageConverter.supports(EntityCollection.class));
  }

  @Test
  void testSupportsNo() {
    assertFalse(csvHttpMessageConverter.supports(Object.class));
  }

  @Test
  void testReadInternal() throws IOException {
    assertThrows(
        UnsupportedOperationException.class,
        () ->
            csvHttpMessageConverter.readInternal(
                EntityCollection.class, mock(HttpInputMessage.class)));
  }
}
