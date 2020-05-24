package org.molgenis.data.elasticsearch.client;

import java.util.Map;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class PreBuiltTransportClientFactory {
  public PreBuiltTransportClient build(String clusterName, Map<String, String> settings) {
    return new PreBuiltTransportClient(createSettings(clusterName, settings));
  }

  private static Settings createSettings(String clusterName, Map<String, String> settings) {
    if (clusterName == null) {
      throw new NullPointerException("clusterName cannot be null");
    }

    Settings.Builder builder = Settings.builder();
    builder.put("cluster.name", clusterName);
    if (settings != null) {
      settings.forEach(builder::put);
    }
    return builder.build();
  }
}
