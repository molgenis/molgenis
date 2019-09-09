package org.molgenis.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.molgenis.security.token.RunAsUserTokenFactory;
import org.molgenis.security.user.UserDetailsServiceImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.userdetails.UserDetails;

class JobExecutorTokenServiceImplTest extends AbstractMockitoTest {
  @Mock private UserDetailsServiceImpl userDetailsService;
  @Mock private RunAsUserTokenFactory runAsUserTokenFactory;

  private JobExecutorTokenServiceImpl jobExecutorTokenServiceImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    jobExecutorTokenServiceImpl =
        new JobExecutorTokenServiceImpl(userDetailsService, runAsUserTokenFactory);
  }

  @Test
  void testJobExecutorTokenServiceImpl() {
    assertThrows(NullPointerException.class, () -> new JobExecutorTokenServiceImpl(null, null));
  }

  @Test
  void testCreateTokenUser() {
    String username = "user";
    JobExecution jobExecution =
        when(mock(JobExecution.class).getUser()).thenReturn(Optional.of(username)).getMock();

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
    RunAsUserToken runAsUserToken = mock(RunAsUserToken.class);
    when(runAsUserTokenFactory.create("Job Execution", userDetails, null))
        .thenReturn(runAsUserToken);

    assertEquals(jobExecutorTokenServiceImpl.createToken(jobExecution), runAsUserToken);
  }

  @Test
  void testCreateTokenSystem() {
    JobExecution jobExecution = mock(JobExecution.class);
    assertTrue(
        jobExecutorTokenServiceImpl.createToken(jobExecution) instanceof SystemSecurityToken);
  }
}
