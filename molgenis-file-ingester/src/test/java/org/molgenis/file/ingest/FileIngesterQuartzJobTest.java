package org.molgenis.file.ingest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.file.FileMeta;
import org.molgenis.file.ingest.execution.FileIngestJob;
import org.molgenis.file.ingest.execution.FileIngestJobFactory;
import org.molgenis.file.ingest.execution.FileIngester;
import org.molgenis.file.ingest.meta.FileIngest;
import org.molgenis.file.ingest.meta.FileIngestJobExecution;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileIngesterQuartzJobTest
{
	private FileIngesterQuartzJob fileIngesterQuartzJob;
	@Mock
	private FileIngester fileIngesterMock;
	@Mock
	private JobExecutionContext contextMock;
	@Mock
	private FileIngestJobFactory fileIngestJobFactoryMock;
	@Mock
	private DataService dataServiceMock;

	@BeforeMethod
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
		fileIngesterQuartzJob = new FileIngesterQuartzJob(fileIngestJobFactoryMock, dataServiceMock);
		contextMock = mock(JobExecutionContext.class);
	}

	@Test
	public void quartzJobRetrievesFileIngestCreatesJobExecutionEntityAndJobAndRunsJob() throws Exception
	{
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(FileIngesterQuartzJob.ENTITY_KEY, "abcde");
		when(contextMock.getMergedJobDataMap()).thenReturn(jobDataMap);

		FileIngest fileIngest = new FileIngest(dataServiceMock);
		fileIngest.set(FileIngestMetaData.FAILURE_EMAIL, "x@y.z");
		Entity targetEntity = new DefaultEntity(EntityMetaDataMetaData.INSTANCE, dataServiceMock);
		targetEntity.set(EntityMetaDataMetaData.FULL_NAME, "org_molgenis_test_TypeTest");
		fileIngest.set(FileIngestMetaData.ENTITY_META_DATA, targetEntity);
		when(dataServiceMock.findOne(FileIngestMetaData.ENTITY_NAME, "abcde", FileIngest.class)).thenReturn(fileIngest);
		
		Query queryMock = Mockito.mock(Query.class);
		MolgenisUser admin = new MolgenisUser();
		admin.setUsername("admin");
		when(dataServiceMock.query(MolgenisUser.ENTITY_NAME)).thenReturn(queryMock);
		when(queryMock.eq(MolgenisUser.USERNAME, "admin")).thenReturn(queryMock);
		when(queryMock.findOne()).thenReturn(admin);

		ArgumentCaptor<FileIngestJobExecution> captor = ArgumentCaptor.forClass(FileIngestJobExecution.class);
		FileIngestJob fileIngestJobMock = mock(FileIngestJob.class);
		when(fileIngestJobFactoryMock.createJob(captor.capture())).thenReturn(fileIngestJobMock);

		FileMeta fileMeta = mock(FileMeta.class);
		when(fileIngestJobMock.call()).thenReturn(fileMeta);

		fileIngesterQuartzJob.execute(contextMock);

		// check that properly filled jobExecution entity was fed to the factory
		FileIngestJobExecution jobExecution = captor.getValue();
		assertEquals(jobExecution.getFailureEmail(), new String[]
		{ "x@y.z" });
		assertEquals(jobExecution.getFileIngest(), fileIngest);
		verify(dataServiceMock).add("FileMeta", fileMeta);
	}
}
