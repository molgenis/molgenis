package org.molgenis.core.ui.data.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withCreatedEntity;

import com.google.auto.value.AutoValue;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.util.AutoGson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.RestTemplate;

@ContextConfiguration(classes = {HttpClientConfig.class})
class HttpClientConfigTest extends AbstractMockitoSpringContextTests {
  @Autowired RestTemplate restTemplate;
  MockRestServiceServer server;
  TestNegotiatorQuery testNegotiatorQuery = TestNegotiatorQuery.createQuery("url", "ntoken");

  @BeforeEach
  void beforeEach() {
    server = MockRestServiceServer.bindTo(restTemplate).build();
  }

  @Test
  void testGsonSerialization() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Basic ABCDE");

    HttpEntity<TestNegotiatorQuery> entity = new HttpEntity<>(testNegotiatorQuery, headers);
    server
        .expect(once(), requestTo("http://directory.url/request"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string("{\"URL\":\"url\",\"nToken\":\"ntoken\"}"))
        .andExpect(MockRestRequestMatchers.header("Authorization", "Basic ABCDE"))
        .andRespond(withCreatedEntity(URI.create("http://directory.url/request/DEF")));

    String redirectURL =
        restTemplate.postForLocation("http://directory.url/request", entity).toASCIIString();
    assertEquals("http://directory.url/request/DEF", redirectURL);

    // Verify all expectations met
    server.verify();
  }

  @AutoValue
  @AutoGson(autoValueClass = AutoValue_HttpClientConfigTest_TestNegotiatorQuery.class)
  abstract static class TestNegotiatorQuery {
    abstract String getURL();

    abstract String getnToken();

    static TestNegotiatorQuery createQuery(String url, String nToken) {
      return new AutoValue_HttpClientConfigTest_TestNegotiatorQuery(url, nToken);
    }
  }
}
