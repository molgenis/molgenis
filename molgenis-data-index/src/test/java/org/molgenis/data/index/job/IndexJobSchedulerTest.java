package org.molgenis.data.index.job;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.*;
import org.molgenis.data.index.IndexActionRegisterServiceImpl;
import org.molgenis.data.index.IndexConfig;
import org.molgenis.data.index.IndexService;
import org.molgenis.data.index.config.IndexTestConfig;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.transaction.TransactionListener;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.jobs.JobExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailSender;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.index.job.IndexJobExecutionMeta.INDEX_JOB_EXECUTION;
import static org.molgenis.data.index.meta.IndexActionGroupMetaData.INDEX_ACTION_GROUP;
import static org.molgenis.data.util.MolgenisDateFormat.parseInstant;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { IndexJobSchedulerTest.Config.class, IndexTestConfig.class })
public class IndexJobSchedulerTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private DataService dataService;

	@Autowired
	private TransactionManager transactionManager;

	@Autowired
	private TransactionListener molgenisTransactionListener;

	@Autowired
	private JobExecutor jobExecutor;

	@Mock
	private Repository<Entity> repository;

	@Autowired
	private IndexJobScheduler indexJobScheduler;

	@Mock
	private Stream<Entity> jobExecutions;

	@Captor
	private ArgumentCaptor<IndexJobExecution> indexJobExecutionCaptor;

	@Captor
	private ArgumentCaptor<Query<Entity>> queryCaptor;

	@Captor
	private ArgumentCaptor<Runnable> runnableArgumentCaptor;

	@Autowired
	private Config config;

	@BeforeClass
	public void setUp() throws Exception
	{
		verify(transactionManager).addTransactionListener(molgenisTransactionListener);
	}

	@BeforeMethod
	public void beforeMethod() throws Exception
	{
		config.resetMocks();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRebuildIndexDoesNothingIfNoIndexActionJobIsFound() throws Exception
	{
		when(dataService.findOneById(INDEX_ACTION_GROUP, "abcde")).thenReturn(null);

		indexJobScheduler.scheduleIndexJob("abcde");

		verify(jobExecutor, never()).submit(any());
	}

	@Test
	public void testCleanupJobExecutions() throws Exception
	{
		when(dataService.getRepository(INDEX_JOB_EXECUTION)).thenReturn(repository);
		when(repository.query()).thenReturn(new QueryImpl<>(repository));
		when(repository.findAll(queryCaptor.capture())).thenReturn(jobExecutions);
		when(dataService.hasRepository(INDEX_JOB_EXECUTION)).thenReturn(true);

		indexJobScheduler.cleanupJobExecutions();

		verify(dataService).delete(INDEX_JOB_EXECUTION, jobExecutions);

		Query<Entity> actualQuery = queryCaptor.getValue();
		Pattern queryPattern = Pattern.compile("rules=\\['endDate' < '(.*)', AND, 'status' = 'SUCCESS'\\]");
		Matcher queryMatcher = queryPattern.matcher(actualQuery.toString());
		assertTrue(queryMatcher.matches());

		// check the endDate time limit in the query
		assertEquals(Duration.between(parseInstant(queryMatcher.group(1)), Instant.now()).toMinutes(), 5);
	}

	@Configuration
	@Import({ IndexConfig.class, IndexActionRegisterServiceImpl.class })
	public static class Config
	{
		@Mock
		private JobExecutor jobExecutor;

		@Mock
		private MailSender mailSender;

		@Mock
		private TransactionManager transactionManager;

		@Mock
		private IndexService indexService;

		public Config()
		{
			initMocks(this);
		}

		private void resetMocks()
		{
			reset(jobExecutor, mailSender, transactionManager, indexService);
		}

		@Bean
		public JobExecutor jobExecutor()
		{
			return jobExecutor;
		}

		@Bean
		public IndexService indexService()
		{
			return indexService;
		}

		@Bean
		public TransactionManager molgenisTransactionManager()
		{
			return transactionManager;
		}

		@Bean
		public MailSender mailSender()
		{
			return mailSender;
		}
	}

}