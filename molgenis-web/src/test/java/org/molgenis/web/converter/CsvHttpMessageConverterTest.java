package org.molgenis.web.converter;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Stream;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityCollection;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CsvHttpMessageConverterTest extends AbstractMockitoTest {

  private CsvHttpMessageConverter csvHttpMessageConverter;

  @BeforeMethod
  public void setUpBeforeMethod() {
    csvHttpMessageConverter = new CsvHttpMessageConverter();
  }

  @Test
  public void testWriteInternal() throws IOException {
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
  public void testSupports() {
    assertTrue(csvHttpMessageConverter.supports(EntityCollection.class));
  }

  @Test
  public void testSupportsNo() {
    assertFalse(csvHttpMessageConverter.supports(Object.class));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testReadInternal() throws IOException {
    csvHttpMessageConverter.readInternal(EntityCollection.class, mock(HttpInputMessage.class));
  }
}
