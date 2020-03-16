package org.molgenis.jobs;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PreDestroy;
import org.molgenis.util.ExecutorServiceUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({JobFactoryRegistry.class, JobExecutor.class})
@Configuration
public class JobExecutionConfig {
  private ExecutorService executorService;

  @PreDestroy
  void preDestroy() {
    if (executorService != null) {
      ExecutorServiceUtils.shutdownAndAwaitTermination(executorService);
    }
  }

  @Bean
  public synchronized ExecutorService executorService() {
    if (executorService == null) {
      executorService =
          Executors.newCachedThreadPool(
              new ThreadFactoryBuilder().setNameFormat("molgenis-job-%d").build());
    }
    return executorService;
  }
}
