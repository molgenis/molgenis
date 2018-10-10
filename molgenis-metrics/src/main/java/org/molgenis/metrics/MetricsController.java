package org.molgenis.metrics;

import static io.prometheus.client.exporter.common.TextFormat.CONTENT_TYPE_004;
import static org.molgenis.metrics.MetricsController.BASE_URI;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api("Metrics")
@RestController
@RequestMapping(BASE_URI)
public class MetricsController {
  public static final String ROLE_METRICS = "ROLE_METRICS";
  static final String BASE_URI = "/api/metrics";
  private final PrometheusMeterRegistry meterRegistry;

  MetricsController(PrometheusMeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @GetMapping(path = "prometheus", produces = CONTENT_TYPE_004)
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasAnyRole('METRICS')")
  @ApiResponses({
    @ApiResponse(
        code = 200,
        message = "Tab separated metrics values in a format fit for prometheus to consume"),
    @ApiResponse(code = 403, message = "Current user does not have ROLE_METRICS"),
    @ApiResponse(
        code = 401,
        message = "User is not authenticated and anonymous user does not have ROLE_METRICS")
  })
  @ApiOperation(
      value = "Get recorded metrics for prometheus.",
      response = String.class,
      produces = CONTENT_TYPE_004)
  public String prometheus() {
    return meterRegistry.scrape();
  }
}
