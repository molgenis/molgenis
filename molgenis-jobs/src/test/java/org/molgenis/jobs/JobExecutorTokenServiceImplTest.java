package org.molgenis.jobs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Optional;
import org.mockito.Mock;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.molgenis.security.token.RunAsUserTokenFactory;
import org.molgenis.security.user.UserDetailsServiceImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JobExecutorTokenServiceImplTest extends AbstractMockitoTest {
  @Mock private UserDetailsServiceImpl userDetailsService;
  @Mock private RunAsUserTokenFactory runAsUserTokenFactory;

  private JobExecutorTokenServiceImpl jobExecutorTokenServiceImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    jobExecutorTokenServiceImpl =
        new JobExecutorTokenServiceImpl(userDetailsService, runAsUserTokenFactory);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testJobExecutorTokenServiceImpl() {
    new JobExecutorTokenServiceImpl(null, null);
  }

  @Test
  public void testCreateTokenUser() {
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
  public void testCreateTokenSystem() {
    JobExecution jobExecution = mock(JobExecution.class);
    assertTrue(
        jobExecutorTokenServiceImpl.createToken(jobExecution) instanceof SystemSecurityToken);
  }
}
