package org.molgenis.jobs;

import org.molgenis.jobs.model.JobExecution;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class JobExecutionUriUtils {
  private JobExecutionUriUtils() {}

  public static String getUriPath(JobExecution jobExecution) {
    return ServletUriComponentsBuilder.fromCurrentRequestUri()
        .encode()
        .replacePath(null)
        .pathSegment(
            "api", "v2", jobExecution.getEntityType().getId(), jobExecution.getIdValue().toString())
        .build()
        .getPath();
  }
}
