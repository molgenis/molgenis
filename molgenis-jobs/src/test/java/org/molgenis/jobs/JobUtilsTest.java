package org.molgenis.jobs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {JobUtilsTest.Config.class})
@SecurityTestExecutionListeners
class JobUtilsTest extends AbstractMockitoSpringContextTests {

  @Test
  @WithMockUser
  void cleanupAfterRunJobCallingThreadId() {
    JobUtils.cleanupAfterRunJob(Thread.currentThread().getId());
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  @WithMockUser
  void cleanupAfterRunJobNonCallingThreadId() {
    JobUtils.cleanupAfterRunJob(Thread.currentThread().getId() + 1);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Configuration
  static class Config {}
}
