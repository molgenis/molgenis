package org.molgenis.jobs.model.hello;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertTrue;

import com.google.gson.Gson;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.jobs.*;
import org.molgenis.jobs.config.JobTestConfig;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.mail.MailSender;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      JobTestConfig.class,
      JobExecutionConfig.class,
      HelloWorldJobExecutionFactory.class,
      HelloWorldJobExecutionMetadata.class,
      JobExecutionMetaData.class,
      HelloWorldConfig.class,
      JobExecutor.class,
      EntityManagerImpl.class,
      HelloWorldTest.Config.class,
      JobFactoryRegistry.class,
      JobFactoryRegistrar.class
    })
public class HelloWorldTest extends AbstractMolgenisSpringTest {
  @Autowired JobExecutor jobExecutor;

  @Autowired HelloWorldJobExecutionFactory factory;

  public HelloWorldTest() {
    super(Strictness.WARN);
  }

  @Test
  public void helloWorld() throws InterruptedException, TimeoutException, ExecutionException {
    HelloWorldJobExecution jobExecution = factory.create();
    jobExecution.setDelay(1);
    jobExecution.setUser("user");
    CompletableFuture<Void> job = jobExecutor.submit(jobExecution);
    job.get(2, SECONDS);
    assertTrue(jobExecution.getLog().contains("Hello user!"));
  }

  public static class Config implements ApplicationListener<ContextRefreshedEvent> {
    @Mock private MailSender mailSender;

    @Mock private JobExecutorTokenService jobExecutorTokenService;

    @Autowired JobFactoryRegistrar jobFactoryRegistrar;

    public Config() {
      initMocks(this);
    }

    @Bean
    public MailSender mailSender() {
      return mailSender;
    }

    @Bean
    public JobExecutorTokenService jobExecutorTokenService() {
      return jobExecutorTokenService;
    }

    @Bean
    public Gson gson() {
      return new Gson();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
      jobFactoryRegistrar.register(event);
    }
  }
}
