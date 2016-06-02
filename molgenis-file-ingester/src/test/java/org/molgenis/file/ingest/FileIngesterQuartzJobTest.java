package org.molgenis.file.ingest;

public class FileIngesterQuartzJobTest
{
	//	private FileIngesterQuartzJob fileIngesterQuartzJob;
	//	@Mock
	//	private FileIngester fileIngesterMock;
	//	@Mock
	//	private JobExecutionContext contextMock;
	//	@Mock
	//	private FileIngestJobFactory fileIngestJobFactoryMock;
	//	@Mock
	//	private DataService dataServiceMock;
	//
	//	@BeforeMethod
	//	public void setUp()
	//	{
	//		MockitoAnnotations.initMocks(this);
	//		fileIngesterQuartzJob = new FileIngesterQuartzJob(fileIngestJobFactoryMock, dataServiceMock, );
	//		contextMock = mock(JobExecutionContext.class);
	//	}
	//
	//	@Test
	//	public void quartzJobRetrievesFileIngestCreatesJobExecutionEntityAndJobAndRunsJob() throws Exception
	//	{
	//		JobDataMap jobDataMap = new JobDataMap();
	//		jobDataMap.put(FileIngesterQuartzJob.ENTITY_KEY, "abcde");
	//		when(contextMock.getMergedJobDataMap()).thenReturn(jobDataMap);
	//
	//		FileIngest fileIngest = new FileIngest(dataServiceMock);
	//		fileIngest.set(FileIngestMetaData.FAILURE_EMAIL, "x@y.z");
	//		Entity targetEntity = new DefaultEntity(null /*EntityMetaDataMetaData.get()*/, dataServiceMock);
	//		targetEntity.set(EntityMetaDataMetaData.FULL_NAME, "org_molgenis_test_TypeTest");
	//		fileIngest.set(FileIngestMetaData.ENTITY_META_DATA, targetEntity);
	//		when(dataServiceMock.findOneById(FileIngestMetaData.TAG, "abcde", FileIngest.class)).thenReturn(fileIngest);
	//
	//		Query<MolgenisUser> queryMock = Mockito.mock(Query.class);
	//		MolgenisUser admin = new MolgenisUser();
	//		admin.setUsername("admin");
	//		when(dataServiceMock.query(MolgenisUserMetaData.TAG, MolgenisUser.class)).thenReturn(queryMock);
	//		when(queryMock.eq(MolgenisUserMetaData.USERNAME, "admin")).thenReturn(queryMock);
	//		when(queryMock.findOne()).thenReturn(admin);
	//
	//		ArgumentCaptor<FileIngestJobExecution> captor = ArgumentCaptor.forClass(FileIngestJobExecution.class);
	//		FileIngestJob fileIngestJobMock = mock(FileIngestJob.class);
	//		when(fileIngestJobFactoryMock.createJob(captor.capture())).thenReturn(fileIngestJobMock);
	//
	//		FileMeta fileMeta = mock(FileMeta.class);
	//		when(fileIngestJobMock.call()).thenReturn(fileMeta);
	//
	//		fileIngesterQuartzJob.execute(contextMock);
	//
	//		// check that properly filled jobExecution entity was fed to the factory
	//		FileIngestJobExecution jobExecution = captor.getValue();
	//		assertEquals(jobExecution.getFailureEmail(), new String[]
	//		{ "x@y.z" });
	//		assertEquals(jobExecution.getFileIngest(), fileIngest);
	//		verify(dataServiceMock).add("FileMeta", fileMeta);
	//	}
}
