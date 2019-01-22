package org.molgenis.jobs;

import static java.util.Objects.requireNonNull;

import org.molgenis.jobs.model.JobExecution;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.molgenis.security.token.RunAsUserTokenFactory;
import org.molgenis.security.user.UserDetailsServiceImpl;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JobExecutorTokenServiceImpl implements JobExecutorTokenService {
  private static final String JOB_EXECUTION_TOKEN_KEY = "Job Execution";

  private final UserDetailsServiceImpl userDetailsServiceImpl;
  private final RunAsUserTokenFactory runAsUserTokenFactory;

  JobExecutorTokenServiceImpl(
      UserDetailsServiceImpl userDetailsServiceImpl, RunAsUserTokenFactory runAsUserTokenFactory) {
    this.userDetailsServiceImpl = requireNonNull(userDetailsServiceImpl);
    this.runAsUserTokenFactory = requireNonNull(runAsUserTokenFactory);
  }

  @Override
  public AbstractAuthenticationToken createToken(JobExecution jobExecution) {
    return jobExecution
        .getUser()
        .map(this::createRunAsUsertoken)
        .orElseGet(SystemSecurityToken::getInstance);
  }

  private AbstractAuthenticationToken createRunAsUsertoken(String username) {
    UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);
    return runAsUserTokenFactory.create(JOB_EXECUTION_TOKEN_KEY, userDetails, null);
  }
}
