package org.molgenis.api.metrics;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MetricsControllerTest extends AbstractMockitoTest {

  @Mock private PrometheusMeterRegistry meterRegistry;
  private MetricsController metricsController;

  @BeforeMethod
  public void beforeMethod() {
    metricsController = new MetricsController(meterRegistry);
  }

  @Test
  public void testPrometheus() {
    when(meterRegistry.scrape()).thenReturn("scraped");
    String actual = metricsController.prometheus();
    assertEquals(actual, "scraped");
  }
}
