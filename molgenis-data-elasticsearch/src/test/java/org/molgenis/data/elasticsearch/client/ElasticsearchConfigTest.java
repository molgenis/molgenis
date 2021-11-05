package org.molgenis.data.elasticsearch.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.molgenis.data.elasticsearch.client.ElasticsearchConfig.toHost;
import static org.molgenis.data.elasticsearch.client.ElasticsearchConfig.toHosts;

import java.util.List;
import org.apache.http.HttpHost;
import org.junit.jupiter.api.Test;

class ElasticsearchConfigTest {
  @Test
  void testToHostNoPort() {
    var thrown = assertThrows(IllegalArgumentException.class, () -> toHost("localhost"));
    assertEquals(
        "Invalid hostname 'localhost' in property elasticsearch.hosts. Host should be of form 'hostname:port'.",
        thrown.getMessage());
  }

  @Test
  void testToHostPortIsNotNumeric() {
    var thrown = assertThrows(IllegalArgumentException.class, () -> toHost("localhost:foo"));
    assertEquals(
        "Invalid hostname 'localhost:foo' in property elasticsearch.hosts. Port number should be numeric.",
        thrown.getMessage());
  }

  @Test
  void testToHost() {
    var host = toHost("localhost:9300");
    assertEquals(new HttpHost("localhost", 9300), host);
  }

  @Test
  void testToHosts() {
    var hosts = toHosts(List.of("localhost:9300", "192.168.0.1:9200"));
    assertEquals(
        List.of(new HttpHost("localhost", 9300), new HttpHost("192.168.0.1", 9200)), hosts);
  }
}
