package org.molgenis.data.elasticsearch.reindex.job;

import org.mockito.*;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.security.user.MolgenisUserService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

public class RebuildIndexServiceImplTest
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

	@Mock
	private ExecutorService executorService;

	@InjectMocks
	private RebuildIndexServiceImpl rebuildIndexService;

	@Captor
	private ArgumentCaptor<ReindexJobExecution> reindexJobExecutionCaptor;

	@BeforeMethod
	public void setUp() throws Exception
	{
		initMocks(this);
	}

	@Test
	public void testRebuildIndex() throws Exception
	{
		when(dataService.findOneById(ReindexActionJobMetaData.ENTITY_NAME, "abcde")).thenReturn(reindexActionJobEntity);
		when(reindexJobFactory.createJob(reindexJobExecutionCaptor.capture())).thenReturn(reindexJob);
		when(molgenisUserService.getUser("admin")).thenReturn(admin);

		rebuildIndexService.rebuildIndex("abcde");

		ReindexJobExecution reindexJobExecution = reindexJobExecutionCaptor.getValue();
		assertEquals(reindexJobExecution.getReindexActionJobID(), "abcde");
		assertEquals(reindexJobExecution.getUser(), admin);
		verify(executorService).submit(reindexJob);
	}

	@Test
	public void testRebuildIndexIfNoReindexActionJobIsFound() throws Exception
	{
		when(dataService.findOneById(ReindexActionJobMetaData.ENTITY_NAME, "abcde")).thenReturn(null);

		rebuildIndexService.rebuildIndex("abcde");

		verify(reindexJobFactory, never()).createJob(any());
		verify(executorService, never()).submit(any(Callable.class));
	}

	@Test
	public void testCleanupJobExecutions() throws Exception
	{
		
	}

}