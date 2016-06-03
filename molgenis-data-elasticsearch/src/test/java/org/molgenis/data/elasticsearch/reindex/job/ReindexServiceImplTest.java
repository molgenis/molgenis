package org.molgenis.data.elasticsearch.reindex.job;

public class ReindexServiceImplTest
{
	//	public static final int FIVE_MINUTES = 5 * 60 * 1000;
	//	public static final int ONE_SECOND = 1000;
	//	@Mock
	//	private DataService dataService;
	//
	//	@Mock
	//	private ReindexJobFactory reindexJobFactory;
	//
	//	@Mock
	//	private ReindexJob reindexJob;
	//
	//	@Mock
	//	private MolgenisUserService molgenisUserService;
	//
	//	@Mock
	//	private MolgenisUser admin;
	//
	//	@Mock
	//	private Entity reindexActionJobEntity;
	//
	//	@Mock
	//	private ExecutorService executorService;
	//
	//	@Mock
	//	private Repository<Entity> repository;
	//
	//	@Mock
	//	private Stream<Entity> jobExecutions;
	//
	//	@InjectMocks
	//	private ReindexServiceImpl rebuildIndexService;
	//
	//	@Captor
	//	private ArgumentCaptor<ReindexJobExecution> reindexJobExecutionCaptor;
	//
	//	@Captor
	//	private ArgumentCaptor<Query<Entity>> queryCaptor;
	//
	//	@BeforeClass
	//	public void setUp() throws Exception
	//	{
	//		initMocks(this);
	//	}
	//
	//	@AfterMethod
	//	public void afterMethod()
	//	{
	//		reset(dataService, reindexJobFactory, reindexJob, molgenisUserService, admin, reindexActionJobEntity,
	//				executorService, repository, jobExecutions);
	//	}
	//
	//	@Test
	//	public void testRebuildIndex() throws Exception
	//	{
	//		when(dataService.findOneById(ReindexActionJobMetaData.ENTITY_NAME, "abcde")).thenReturn(reindexActionJobEntity);
	//		when(reindexJobFactory.createJob(reindexJobExecutionCaptor.capture())).thenReturn(reindexJob);
	//		when(molgenisUserService.getUser("admin")).thenReturn(admin);
	//
	//		rebuildIndexService.rebuildIndex("abcde");
	//
	//		ReindexJobExecution reindexJobExecution = reindexJobExecutionCaptor.getValue();
	//		assertEquals(reindexJobExecution.getReindexActionJobID(), "abcde");
	//		assertEquals(reindexJobExecution.getUser(), "admin");
	//		verify(executorService).submit(reindexJob);
	//	}
	//
	//	@Test
	//	public void testRebuildIndexDoesNothingIfNoReindexActionJobIsFound() throws Exception
	//	{
	//		when(dataService.findOneById(ReindexActionJobMetaData.ENTITY_NAME, "abcde")).thenReturn(null);
	//
	//		rebuildIndexService.rebuildIndex("abcde");
	//
	//		verify(reindexJobFactory, never()).createJob(any());
	//		verify(executorService, never()).submit(reindexJob);
	//	}
	//
	//	@Test
	//	public void testCleanupJobExecutions() throws Exception
	//	{
	//		when(dataService.getRepository(REINDEX_JOB_EXECUTION)).thenReturn(repository);
	//		when(repository.query()).thenReturn(new QueryImpl<>(repository));
	//		when(repository.findAll(queryCaptor.capture())).thenReturn(jobExecutions);
	//		when(dataService.hasRepository(REINDEX_JOB_EXECUTION)).thenReturn(true);
	//		rebuildIndexService.cleanupJobExecutions();
	//
	//		verify(dataService).delete(REINDEX_JOB_EXECUTION, jobExecutions);
	//
	//		Query<Entity> actualQuery = queryCaptor.getValue();
	//		Pattern queryPattern = Pattern.compile("rules=\\['endDate' < '(.*)', AND, 'status' = 'SUCCESS'\\]");
	//		Matcher queryMatcher = queryPattern.matcher(actualQuery.toString());
	//		assertTrue(queryMatcher.matches());
	//
	//		// check the endDate time limit in the query
	//		String dateString = queryMatcher.group(1);
	//		DateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
	//		Date date = sdf.parse(dateString);
	//		assertEquals(date.toString(), dateString);
	//		long ago = System.currentTimeMillis() - date.getTime();
	//		assertTrue(ago > MINUTES.toMillis(5));
	//		assertTrue(ago < MINUTES.toMillis(5) + SECONDS.toMillis(3));
	//	}

}