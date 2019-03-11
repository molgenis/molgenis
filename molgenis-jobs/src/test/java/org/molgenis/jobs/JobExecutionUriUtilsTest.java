package org.molgenis.jobs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JobExecutionUriUtilsTest {
  @BeforeMethod
  public void setUpBeforeMethod() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  @Test
  public void testGetUriPath() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("MyJobExecutionEntityType");
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getEntityType()).thenReturn(entityType);
    when(jobExecution.getIdValue()).thenReturn("MyJobExecutionId");
    assertEquals(JobExecutionUriUtils.getUriPath(jobExecution), "/api/v2/MyJobExecutionEntityType/MyJobExecutionId");
  }
}