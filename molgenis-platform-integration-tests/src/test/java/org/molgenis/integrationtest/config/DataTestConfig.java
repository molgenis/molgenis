package org.molgenis.integrationtest.config;

import org.molgenis.data.platform.config.PlatformConfig;
import org.molgenis.settings.AppSettings;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * This guy uses the {@link AppSettings}. This bean has to be declared on test-level. See
 * integration test examples (SortaControllerIT.java)
 */
@Configuration
@Import({
  LanguageTestConfig.class,
  RepositoryTestConfig.class,
  JobsTestConfig.class,
  MetaTestConfig.class,
  TransactionTestConfig.class,
  ValidationTestConfig.class,
  CacheTestConfig.class,
  AggregationTestConfig.class,
  ElasticsearchTestConfig.class,
  PlatformConfig.class
})
public class DataTestConfig {}
