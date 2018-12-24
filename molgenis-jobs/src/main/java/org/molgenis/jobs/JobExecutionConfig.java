package org.molgenis.jobs;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({JobFactoryRegistry.class, JobExecutor.class})
@Configuration
public class JobExecutionConfig {
  @Bean
  public ExecutorService executorService() {
    return Executors.newCachedThreadPool(
        new ThreadFactoryBuilder().setNameFormat("molgenis-job-%d").build());
  }
}
