package org.molgenis.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class JobExecutionUriUtilsTest {
  @BeforeEach
  void setUpBeforeMethod() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  @Test
  void testGetUriPath() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("MyJobExecutionEntityType");
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getEntityType()).thenReturn(entityType);
    when(jobExecution.getIdValue()).thenReturn("MyJobExecutionId");
    assertEquals(
        JobExecutionUriUtils.getUriPath(jobExecution),
        "/api/v2/MyJobExecutionEntityType/MyJobExecutionId");
  }
}
