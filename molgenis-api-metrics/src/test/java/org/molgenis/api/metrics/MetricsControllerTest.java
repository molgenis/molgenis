package org.molgenis.api.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;

class MetricsControllerTest extends AbstractMockitoTest {

  @Mock private PrometheusMeterRegistry meterRegistry;
  private MetricsController metricsController;

  @BeforeEach
  void beforeMethod() {
    metricsController = new MetricsController(meterRegistry);
  }

  @Test
  void testPrometheus() {
    when(meterRegistry.scrape()).thenReturn("scraped");
    String actual = metricsController.prometheus();
    assertEquals(actual, "scraped");
  }
}
