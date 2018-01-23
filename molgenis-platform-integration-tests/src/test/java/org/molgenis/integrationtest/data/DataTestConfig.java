package org.molgenis.integrationtest.data;

import org.molgenis.data.platform.config.PlatformConfig;
import org.molgenis.integrationtest.data.aggregation.AggregationTestConfig;
import org.molgenis.integrationtest.data.cache.CacheTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchTestConfig;
import org.molgenis.integrationtest.data.i18n.LanguageTestConfig;
import org.molgenis.integrationtest.data.meta.MetaTestConfig;
import org.molgenis.integrationtest.data.transaction.TransactionTestConfig;
import org.molgenis.integrationtest.data.validation.ValidationTestConfig;
import org.molgenis.integrationtest.jobs.JobsTestConfig;
import org.molgenis.settings.AppSettings;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * <p>This guy uses the {@link AppSettings}. This bean has to be declared on test-level.
 * See integration test examples (SortaControllerIT.java)</p>
 */
@Configuration
@Import({ LanguageTestConfig.class, RepositoryTestConfig.class, JobsTestConfig.class, MetaTestConfig.class,
		TransactionTestConfig.class, ValidationTestConfig.class, CacheTestConfig.class, AggregationTestConfig.class,
		ElasticsearchTestConfig.class, PlatformConfig.class })
public class DataTestConfig
{
}
