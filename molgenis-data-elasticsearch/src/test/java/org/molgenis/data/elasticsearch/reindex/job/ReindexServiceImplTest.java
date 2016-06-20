package org.molgenis.data.elasticsearch.reindex.job;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.elasticsearch.reindex.meta.ReindexJobExecutionMeta.REINDEX_JOB_EXECUTION;
import static org.molgenis.data.reindex.meta.ReindexActionJobMetaData.REINDEX_ACTION_JOB;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ENTITY_FULL_NAME;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.REINDEX_ACTION;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.REINDEX_ACTION_GROUP;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.reindex.meta.ReindexActionJob;
import org.molgenis.data.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.user.MolgenisUserService;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ReindexServiceImplTest
{
	@Mock
	private DataService dataService;

	@Mock
	private ReindexJobFactory reindexJobFactory;

	@Mock
	private ReindexJob reindexJob;

	@Mock
	private MolgenisUserService molgenisUserService;

	@Mock
	private MolgenisUser admin;

	@Mock
	private Entity reindexActionJobEntity;

	@Captor
	private ArgumentCaptor<Runnable> runnableArgumentCaptor;

	private Entity reindexActionEntity;

	@Mock
	private ExecutorService executorService;

	@Mock
	private Repository<Entity> repository;

	@Mock
	private Stream<Entity> jobExecutions;

	@Mock
	private ReindexActionJobMetaData reindexActionJobMetaData;
	@InjectMocks
	private ReindexServiceImpl rebuildIndexService;

	@Captor
	private ArgumentCaptor<ReindexJobExecution> reindexJobExecutionCaptor;

	@Captor
	private ArgumentCaptor<Query<Entity>> queryCaptor;

	@BeforeClass
	public void setUp() throws Exception
	{
		initMocks(this);
		reindexActionEntity = new ReindexActionJob(reindexActionJobMetaData);
		reindexActionEntity.set(ENTITY_FULL_NAME, "test_TestEntity");
	}

	@AfterMethod
	public void afterMethod()
	{
		reset(dataService, reindexJobFactory, reindexJob, molgenisUserService, admin, reindexActionJobEntity,
				executorService, repository, jobExecutions);
	}

	@Test
	public void testRebuildIndex() throws Exception
	{
		when(dataService.findOneById(REINDEX_ACTION_JOB, "abcde")).thenReturn(reindexActionJobEntity);

		when(dataService.findAll(REINDEX_ACTION, new QueryImpl<>().eq(REINDEX_ACTION_GROUP, reindexActionJobEntity)))
				.thenReturn(Stream.of(reindexActionEntity));

		when(reindexJobFactory.createJob(reindexJobExecutionCaptor.capture())).thenReturn(reindexJob);
		when(molgenisUserService.getUser("admin")).thenReturn(admin);

		rebuildIndexService.rebuildIndex("abcde");

		ReindexJobExecution reindexJobExecution = reindexJobExecutionCaptor.getValue();
		assertEquals(reindexJobExecution.getReindexActionJobID(), "abcde");
		assertEquals(reindexJobExecution.getUser(), "admin");

		verify(executorService).execute(runnableArgumentCaptor.capture());
		verify(reindexJob, never()).call();

		runnableArgumentCaptor.getValue().run();

		verify(reindexJob).call();
	}

	@Test
	public void testRebuildIndexDoesNothingIfNoReindexActionJobIsFound() throws Exception
	{
		when(dataService.findOneById(REINDEX_ACTION_JOB, "abcde")).thenReturn(null);

		rebuildIndexService.rebuildIndex("abcde");

		verify(reindexJobFactory, never()).createJob(any());
		verify(executorService, never()).submit(reindexJob);
	}

	@Test
	public void testCleanupJobExecutions() throws Exception
	{
		when(dataService.getRepository(REINDEX_JOB_EXECUTION)).thenReturn(repository);
		when(repository.query()).thenReturn(new QueryImpl<>(repository));
		when(repository.findAll(queryCaptor.capture())).thenReturn(jobExecutions);
		when(dataService.hasRepository(REINDEX_JOB_EXECUTION)).thenReturn(true);
		rebuildIndexService.cleanupJobExecutions();

		verify(dataService).delete(REINDEX_JOB_EXECUTION, jobExecutions);

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

}