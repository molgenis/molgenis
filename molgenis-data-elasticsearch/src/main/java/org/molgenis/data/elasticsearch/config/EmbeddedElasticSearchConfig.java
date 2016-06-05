package org.molgenis.data.elasticsearch.config;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.io.File;
import java.util.Collections;

import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.logging.slf4j.Slf4jESLoggerFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.ElasticsearchEntityFactory;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.elasticsearch.reindex.job.ReindexJobExecutionFactory;
import org.molgenis.data.elasticsearch.reindex.job.ReindexJobFactory;
import org.molgenis.data.elasticsearch.reindex.job.ReindexService;
import org.molgenis.data.elasticsearch.reindex.job.ReindexServiceImpl;
import org.molgenis.data.elasticsearch.reindex.job.ReindexJobFactory;
import org.molgenis.data.elasticsearch.transaction.ReindexTransactionListener;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.JobExecutionUpdaterImpl;
import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring config for embedded elastic search server. Use this in your own app by importing this in your spring config:
 * <code> @Import(EmbeddedElasticSearchConfig.class)</code>
 *
 * @author erwin
 */
@Configuration
@EnableScheduling
public class EmbeddedElasticSearchConfig
{
	static
	{
		// force Elasticsearch to use slf4j instead of default log4j logging
		ESLoggerFactory.setDefaultFactory(new Slf4jESLoggerFactory());
	}

	@Autowired
	private DataService dataService;

	@Autowired
	private ElasticsearchEntityFactory elasticsearchEntityFactory;

	@Autowired
	private MolgenisTransactionManager molgenisTransactionManager;

	@Autowired
	public ReindexJobExecutionFactory reindexJobExecutionFactory;

	@Autowired
	private ReindexActionRegisterService reindexActionRegisterService;

	@Bean(destroyMethod = "close")
	public EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory()
	{
		// get molgenis home directory
		String molgenisHomeDir = System.getProperty("molgenis.home");
		if (molgenisHomeDir == null)
		{
			throw new IllegalArgumentException("missing required java system property 'molgenis.home'");
		}
		if (!molgenisHomeDir.endsWith("/")) molgenisHomeDir = molgenisHomeDir + '/';

		// create molgenis data directory if not exists
		String molgenisDataDirStr = molgenisHomeDir + "data";
		File molgenisDataDir = new File(molgenisDataDirStr);
		if (!molgenisDataDir.exists())
		{
			if (!molgenisDataDir.mkdir())
			{
				throw new RuntimeException("failed to create directory: " + molgenisDataDirStr);
			}
		}

		return new EmbeddedElasticSearchServiceFactory(Collections.singletonMap("path.data", molgenisDataDirStr));
	}

	@Bean
	public SearchService searchService()
	{
		return embeddedElasticSearchServiceFactory().create(dataService, elasticsearchEntityFactory);
	}

	@Bean
	public ReindexTransactionListener reindexTransactionListener()
	{
		final ReindexTransactionListener reindexTransactionListener = new ReindexTransactionListener(
				rebuildIndexService(), reindexActionRegisterService);
		molgenisTransactionManager.addTransactionListener(reindexTransactionListener);
		return reindexTransactionListener;
	}

	@Bean
	public JobExecutionUpdater jobExecutionUpdater()
	{
		return new JobExecutionUpdaterImpl();
	}

	@Bean
	public ReindexJobFactory reindexJobFactory()
	{
		return new ReindexJobFactory(dataService, searchService());
	}

	@Bean
	public ReindexService rebuildIndexService()
	{
		return new ReindexServiceImpl(dataService, reindexJobFactory(), reindexJobExecutionFactory, newSingleThreadExecutor());
	}
}
