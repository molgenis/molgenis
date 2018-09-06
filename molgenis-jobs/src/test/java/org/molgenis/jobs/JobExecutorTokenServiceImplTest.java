package org.molgenis.jobs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.security.token.RunAsUserTokenFactory;
import org.molgenis.security.user.UserDetailsService;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JobExecutorTokenServiceImplTest extends AbstractMockitoTest {
  @Mock private UserDetailsService userDetailsService;
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
  public void testCreateToken() {
    String username = "user";
    JobExecution jobExecution =
        when(mock(JobExecution.class).getUser()).thenReturn(username).getMock();

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
    RunAsUserToken runAsUserToken = mock(RunAsUserToken.class);
    when(runAsUserTokenFactory.create("Job Execution", userDetails, null))
        .thenReturn(runAsUserToken);

    assertEquals(jobExecutorTokenServiceImpl.createToken(jobExecution), runAsUserToken);
  }
}
