package org.molgenis.jobs;

import static java.util.Objects.requireNonNull;

import org.molgenis.jobs.model.JobExecution;
import org.molgenis.security.token.RunAsUserTokenFactory;
import org.molgenis.security.user.UserDetailsService;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JobExecutorTokenServiceImpl implements JobExecutorTokenService {
  private static final String JOB_EXECUTION_TOKEN_KEY = "Job Execution";

  private final UserDetailsService userDetailsService;
  private final RunAsUserTokenFactory runAsUserTokenFactory;

  JobExecutorTokenServiceImpl(
      UserDetailsService userDetailsService, RunAsUserTokenFactory runAsUserTokenFactory) {
    this.userDetailsService = requireNonNull(userDetailsService);
    this.runAsUserTokenFactory = requireNonNull(runAsUserTokenFactory);
  }

  @Override
  public AbstractAuthenticationToken createToken(JobExecution jobExecution) {
    return createAuthorization(jobExecution.getUser());
  }

  private RunAsUserToken createAuthorization(String username) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    return runAsUserTokenFactory.create(JOB_EXECUTION_TOKEN_KEY, userDetails, null);
  }
}
