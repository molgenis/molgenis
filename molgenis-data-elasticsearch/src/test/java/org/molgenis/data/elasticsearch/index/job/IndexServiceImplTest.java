package org.molgenis.data.elasticsearch.index.job;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.index.IndexConfig;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.transaction.MolgenisTransactionListener;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.molgenis.security.user.UserService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailSender;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.elasticsearch.index.job.IndexJobExecutionMeta.INDEX_JOB_EXECUTION;
import static org.molgenis.data.index.meta.IndexActionGroupMetaData.INDEX_ACTION_GROUP;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { IndexServiceImplTest.Config.class })
public class IndexServiceImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private IndexJobFactory indexJobFactory;

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisTransactionManager molgenisTransactionManager;

	@Autowired
	private MolgenisTransactionListener molgenisTransactionListener;

	@Autowired
	private ExecutorService executorService;

	@Mock
	private Repository<Entity> repository;

	@Mock
	private UserService userService;

	@Autowired
	private IndexService indexService;

	@Mock
	private Stream<Entity> jobExecutions;

	@Mock
	private IndexJob indexJob;

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
		verify(molgenisTransactionManager).addTransactionListener(molgenisTransactionListener);
	}

	@BeforeMethod
	public void beforeMethod() throws Exception
	{
		initMocks(this);
		config.resetMocks();
	}

	@Test
	public void testRebuildIndexDoesNothingIfNoIndexActionJobIsFound() throws Exception
	{
		when(dataService.findOneById(INDEX_ACTION_GROUP, "abcde")).thenReturn(null);

		indexService.rebuildIndex("abcde");

		verify(indexJobFactory, never()).createJob(any());
		verify(executorService, never()).submit(indexJob);
	}

	@Test
	public void testCleanupJobExecutions() throws Exception
	{
		when(dataService.getRepository(INDEX_JOB_EXECUTION)).thenReturn(repository);
		when(repository.query()).thenReturn(new QueryImpl<>(repository));
		when(repository.findAll(queryCaptor.capture())).thenReturn(jobExecutions);
		when(dataService.hasRepository(INDEX_JOB_EXECUTION)).thenReturn(true);

		indexService.cleanupJobExecutions();

		verify(dataService).delete(INDEX_JOB_EXECUTION, jobExecutions);

		Query<Entity> actualQuery = queryCaptor.getValue();
		Pattern queryPattern = Pattern.compile("rules=\\['endDate' < '(.*)', AND, 'status' = 'SUCCESS'\\]");
		Matcher queryMatcher = queryPattern.matcher(actualQuery.toString());
		assertTrue(queryMatcher.matches());

		// check the endDate time limit in the query
		String dateString = queryMatcher.group(1);
		DateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
		Date date = sdf.parse(dateString);
		assertEquals(date.toString(), dateString);
		long ago = System.currentTimeMillis() - date.getTime();
		assertTrue(ago > MINUTES.toMillis(5));
		assertTrue(ago < MINUTES.toMillis(5) + SECONDS.toMillis(3));
	}

	@ComponentScan(basePackages = {
			"org.molgenis.data.elasticsearch.index.job, org.molgenis.data.jobs.model, org.molgenis.auth" })
	@Configuration
	@Import({ IndexConfig.class })
	public static class Config
	{
		@Mock
		private DataService dataService;

		@Mock
		private IndexJobFactory indexJobFactory;

		@Mock
		private ExecutorService executorService;

		@Mock
		private MailSender mailSender;

		@Mock
		private MolgenisTransactionManager molgenisTransactionManager;

		public Config()
		{
			initMocks(this);
		}

		private void resetMocks()
		{
			Mockito.reset(dataService, indexJobFactory, executorService, mailSender);
		}

		@Bean
		public DataService dataService()
		{
			return dataService;
		}

		@Bean
		public IndexJobFactory indexJobFactory()
		{
			return indexJobFactory;
		}

		@Bean
		public ExecutorService executorService()
		{
			return executorService;
		}

		@Bean
		public MolgenisTransactionManager molgenisTransactionManager()
		{
			return molgenisTransactionManager;
		}

		@Bean
		public MailSender mailSender()
		{
			return mailSender;
		}
	}

}