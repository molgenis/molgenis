package org.molgenis.dataexplorer.negotiator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.web.converter.GsonWebConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = GsonWebConfig.class)
class NegotiatorQueryTest extends AbstractMockitoSpringContextTests {
  @Autowired private Gson gson;

  private NegotiatorQuery negotiatorQuery =
      NegotiatorQuery.create("url", Collections.emptyList(), "humanReadable", null);

  private String json = "{\"URL\":\"url\",\"collections\":[],\"humanReadable\":\"humanReadable\"}";

  @Test
  void testSerialization() {
    assertEquals(json, gson.toJson(negotiatorQuery));
  }

  @Test
  void testDeserialization() {
    assertEquals(negotiatorQuery, gson.fromJson(json, NegotiatorQuery.class));
  }
}
