package org.molgenis.jobs;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailSender;

@Import(JobFactoryRegistry.class)
@Configuration
public class JobExecutionConfig {
  private final DataService dataService;
  private final EntityManager entityManager;
  private final JobExecutionUpdater jobExecutionUpdater;
  private final MailSender mailSender;
  private final JobFactoryRegistry jobFactoryRegistry;
  private final JobExecutorTokenService jobExecutorTokenService;

  public JobExecutionConfig(
      DataService dataService,
      EntityManager entityManager,
      JobExecutionUpdater jobExecutionUpdater,
      MailSender mailSender,
      JobFactoryRegistry jobFactoryRegistry,
      JobExecutorTokenService jobExecutorTokenService) {
    this.dataService = requireNonNull(dataService);
    this.entityManager = requireNonNull(entityManager);
    this.jobExecutionUpdater = requireNonNull(jobExecutionUpdater);
    this.mailSender = requireNonNull(mailSender);
    this.jobFactoryRegistry = requireNonNull(jobFactoryRegistry);
    this.jobExecutorTokenService = requireNonNull(jobExecutorTokenService);
  }

  @Primary // Use this ExecutorService when no specific bean is demanded
  @Bean
  public ExecutorService executorService() {
    return Executors.newCachedThreadPool(
        new ThreadFactoryBuilder().setNameFormat("molgenis-job-%d").build());
  }

  @Bean
  public JobExecutor jobExecutor(ExecutorService executorService) {
    return new JobExecutor(
        dataService,
        entityManager,
        jobExecutionUpdater,
        mailSender,
        executorService,
        jobFactoryRegistry,
        jobExecutorTokenService);
  }
}
