package org.molgenis.data.elasticsearch.config;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.molgenis.data.EntityManager;
import org.molgenis.data.elasticsearch.ElasticsearchEntityFactory;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.elasticsearch.index.SourceToEntityConverter;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.molgenis.data.reindex.ReindexActionRegisterServiceImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class EmbeddedElasticSearchConfigTest
{
	private AnnotationConfigApplicationContext context;

	private File molgenisHomeDir;

	@BeforeMethod
	public void beforeMethod()
	{
		molgenisHomeDir = Files.createTempDir();
		molgenisHomeDir.deleteOnExit();

		System.setProperty("molgenis.home", molgenisHomeDir.getAbsolutePath());
		context = new AnnotationConfigApplicationContext(DataServiceImpl.class, EmbeddedElasticSearchConfig.class,
				ElasticsearchEntityFactory.class, Config.class);
	}

	@AfterMethod
	public void tearDownAfterMethod() throws IOException
	{
		context.destroy();
		FileUtils.deleteDirectory(molgenisHomeDir);
	}

	@Configuration
	public static class Config
	{
		@Bean
		public EntityManager entityManager()
		{
			return mock(EntityManager.class);
		}

		@Bean
		public SourceToEntityConverter sourceToEntityConverter()
		{
			return mock(SourceToEntityConverter.class);
		}

		@Bean
		public MolgenisTransactionManager molgenisTransactionManager()
		{
			return mock(MolgenisTransactionManager.class);
		}

		@Bean
		public JobExecutionUpdater jobExecutionUpdater()
		{
			return mock(JobExecutionUpdater.class);
		}

		@Bean
		public MailSender mailSender()
		{
			return mock(MailSender.class);
		}

		@Bean
		public MolgenisUserService molgenisUserService()
		{
			return mock(MolgenisUserService.class);
		}

		@Bean
		public ReindexActionRegisterService reindexActionRegisterService()
		{
			return mock(ReindexActionRegisterServiceImpl.class);
		}
	}

	@Test
	public void embeddedElasticSearchServiceFactory()
	{
		EmbeddedElasticSearchServiceFactory factory = context.getBean(EmbeddedElasticSearchServiceFactory.class);
		assertNotNull(factory);
	}

	@Test
	public void searchService()
	{
		SearchService searchService = context.getBean(SearchService.class);
		assertNotNull(searchService);
		assertTrue(searchService instanceof ElasticsearchService);
	}
}
