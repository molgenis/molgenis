package org.molgenis.jobs;

import static java.util.Objects.requireNonNull;

import org.molgenis.jobs.scheduler.SchedulerConfig;
import org.molgenis.security.token.RunAsUserTokenFactory;
import org.molgenis.security.user.UserDetailsService;
import org.molgenis.web.i18n.UserLocaleResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Jobs configuration */
@Configuration
@Import(SchedulerConfig.class)
public class JobConfig {
  private final UserDetailsService userDetailsService;
  private final RunAsUserTokenFactory runAsUserTokenFactory;
  private final UserLocaleResolver userLocaleResolver;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public JobConfig(
      UserDetailsService userDetailsService,
      RunAsUserTokenFactory runAsUserTokenFactory,
      UserLocaleResolver userLocaleResolver) {
    this.userDetailsService = requireNonNull(userDetailsService);
    this.runAsUserTokenFactory = requireNonNull(runAsUserTokenFactory);
    this.userLocaleResolver = requireNonNull(userLocaleResolver);
  }

  @Bean
  public JobExecutorTokenService jobExecutorTokenService() {
    return new JobExecutorTokenServiceImpl(userDetailsService, runAsUserTokenFactory);
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
