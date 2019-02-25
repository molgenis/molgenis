package org.molgenis.jobs;

import static java.util.Objects.requireNonNull;

import org.molgenis.jobs.scheduler.SchedulerConfig;
import org.molgenis.security.token.RunAsUserTokenFactory;
import org.molgenis.security.user.UserDetailsServiceImpl;
import org.molgenis.web.i18n.UserLocaleResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Jobs configuration */
@Configuration
@Import({SchedulerConfig.class, JobExecutionRegistryImpl.class, ProgressFactoryImpl.class})
public class JobConfig {
  private final UserDetailsServiceImpl userDetailsServiceImpl;
  private final RunAsUserTokenFactory runAsUserTokenFactory;
  private final UserLocaleResolver userLocaleResolver;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public JobConfig(
      UserDetailsServiceImpl userDetailsServiceImpl,
      RunAsUserTokenFactory runAsUserTokenFactory,
      UserLocaleResolver userLocaleResolver) {
    this.userDetailsServiceImpl = requireNonNull(userDetailsServiceImpl);
    this.runAsUserTokenFactory = requireNonNull(runAsUserTokenFactory);
    this.userLocaleResolver = requireNonNull(userLocaleResolver);
  }

  @Bean
  public JobExecutorTokenService jobExecutorTokenService() {
    return new JobExecutorTokenServiceImpl(userDetailsServiceImpl, runAsUserTokenFactory);
  }

  @Bean
  public JobExecutorLocaleService jobExecutorLocaleService() {
    return new JobExecutorLocaleServiceImpl(userLocaleResolver);
  }

  @Bean
  public JobExecutionContextFactory jobExecutionContextFactory() {
    return new JobExecutionContextFactoryImpl(
        jobExecutorTokenService(), jobExecutorLocaleService());
  }

  @Bean
  public JobExecutionUpdater jobExecutionUpdater() {
    return new JobExecutionUpdaterImpl(jobExecutionContextFactory());
  }
}
