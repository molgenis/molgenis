package org.molgenis.jobs;

import static java.util.Objects.requireNonNull;

import org.molgenis.jobs.scheduler.SchedulerConfig;
import org.molgenis.security.token.RunAsUserTokenFactory;
import org.molgenis.security.user.UserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Jobs configuration */
@Configuration
@Import(SchedulerConfig.class)
public class JobConfig {
  private final UserDetailsService userDetailsService;
  private final RunAsUserTokenFactory runAsUserTokenFactory;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public JobConfig(
      UserDetailsService userDetailsService, RunAsUserTokenFactory runAsUserTokenFactory) {
    this.userDetailsService = requireNonNull(userDetailsService);
    this.runAsUserTokenFactory = requireNonNull(runAsUserTokenFactory);
  }

  @Bean
  public JobExecutorTokenService jobExecutorTokenService() {
    return new JobExecutorTokenServiceImpl(userDetailsService, runAsUserTokenFactory);
  }

  @Bean
  public JobExecutionUpdater jobExecutionUpdater() {
    return new JobExecutionUpdaterImpl(jobExecutorTokenService());
  }
}
