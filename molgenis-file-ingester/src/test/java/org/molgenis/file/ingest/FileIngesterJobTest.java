package org.molgenis.file.ingest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.file.ingest.execution.FileIngester;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileIngesterJobTest {
	private FileIngesterQuartzJob fileIngesterJob;
	private FileIngester fileIngesterMock;
	private JobExecutionContext contextMock;

	@BeforeMethod
	public void setUp()
	{
		fileIngesterJob = new FileIngesterQuartzJob();
		fileIngesterMock = mock(FileIngester.class);
		fileIngesterJob.fileIngester = fileIngesterMock;
		contextMock = mock(JobExecutionContext.class);
	}

	@Test
	public void execute() throws JobExecutionException
	{
		Entity e = new MapEntity();
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(FileIngesterQuartzJob.ENTITY_KEY, e);

		when(contextMock.getMergedJobDataMap()).thenReturn(jobDataMap);
		fileIngesterJob.execute(contextMock);
		verify(fileIngesterMock).ingest(e);
	}
}
