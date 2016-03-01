package org.molgenis.file.ingest;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileIngesterJobSchedulerTest {
	private FileIngesterJobScheduler fileIngesterJobScheduler;
	private DataService dataServiceMock;
	private Scheduler schedulerMock;

	@BeforeMethod
	public void setUp()
	{
		dataServiceMock = mock(DataService.class);
		schedulerMock = mock(Scheduler.class);
		fileIngesterJobScheduler = new FileIngesterJobScheduler(schedulerMock, dataServiceMock);
	}

	@Test
	public void runNow() throws SchedulerException
	{
		String id = "id";
		Entity fileIngest = new MapEntity(FileIngestMetaData.ID, id);
		
		when(dataServiceMock.findOne(FileIngestMetaData.ENTITY_NAME, id)).thenReturn(fileIngest);
		when(schedulerMock.checkExists(new JobKey(id, FileIngesterJobScheduler.JOB_GROUP))).thenReturn(false);
		
		fileIngesterJobScheduler.runNow(id);

		verify(schedulerMock).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test(expectedExceptions = UnknownEntityException.class)
	public void runNowUnknownEntity()
	{
		String id = "id";
		when(dataServiceMock.findOne(FileIngestMetaData.ENTITY_NAME, id)).thenReturn(null);
		fileIngesterJobScheduler.runNow(id);
	}

	@Test
	public void runNowExists() throws SchedulerException
	{
		String id = "id";
		Entity fileIngest = new MapEntity(FileIngestMetaData.ID, id);

		when(dataServiceMock.findOne(FileIngestMetaData.ENTITY_NAME, id)).thenReturn(fileIngest);
		when(schedulerMock.checkExists(new JobKey(id, FileIngesterJobScheduler.JOB_GROUP))).thenReturn(true);

		fileIngesterJobScheduler.runNow(id);

		verify(schedulerMock).triggerJob(new JobKey(id, FileIngesterJobScheduler.JOB_GROUP));
	}

	@Test
	public void schedule() throws SchedulerException
	{
		String id = "id";
		Entity fileIngest = new MapEntity();
		fileIngest.set(FileIngestMetaData.ID, id);
		fileIngest.set(FileIngestMetaData.CRONEXPRESSION, "	0/20 * * * * ?");
		fileIngest.set(FileIngestMetaData.NAME, "name");
		fileIngest.set(FileIngestMetaData.ACTIVE, true);

		when(schedulerMock.checkExists(new JobKey(id, FileIngesterJobScheduler.JOB_GROUP))).thenReturn(false);

		fileIngesterJobScheduler.schedule(fileIngest);

		verify(schedulerMock).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test
	public void scheduleInactive() throws SchedulerException
	{
		String id = "id";
		Entity fileIngest = new MapEntity();
		fileIngest.set(FileIngestMetaData.ID, id);
		fileIngest.set(FileIngestMetaData.CRONEXPRESSION, "	0/20 * * * * ?");
		fileIngest.set(FileIngestMetaData.NAME, "name");
		fileIngest.set(FileIngestMetaData.ACTIVE, false);

		when(schedulerMock.checkExists(new JobKey(id, FileIngesterJobScheduler.JOB_GROUP))).thenReturn(false);

		fileIngesterJobScheduler.schedule(fileIngest);

		verify(schedulerMock, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}
	
	@Test(expectedExceptions = MolgenisValidationException.class)
	public void scheduleInvalidCronExpression() throws SchedulerException
	{
		String id = "id";
		Entity fileIngest = new MapEntity();
		fileIngest.set(FileIngestMetaData.ID, id);
		fileIngest.set(FileIngestMetaData.CRONEXPRESSION, "XXX");
		fileIngest.set(FileIngestMetaData.NAME, "name");
		fileIngest.set(FileIngestMetaData.ACTIVE, false);

		fileIngesterJobScheduler.schedule(fileIngest);
	}

	@Test
	public void scheduleExisting() throws SchedulerException
	{
		String id = "id";
		Entity fileIngest = new MapEntity();
		fileIngest.set(FileIngestMetaData.ID, id);
		fileIngest.set(FileIngestMetaData.CRONEXPRESSION, "	0/20 * * * * ?");
		fileIngest.set(FileIngestMetaData.NAME, "name");
		fileIngest.set(FileIngestMetaData.ACTIVE, true);

		when(schedulerMock.checkExists(new JobKey(id, FileIngesterJobScheduler.JOB_GROUP))).thenReturn(true);

		fileIngesterJobScheduler.schedule(fileIngest);

		verify(schedulerMock).deleteJob((new JobKey(id, FileIngesterJobScheduler.JOB_GROUP)));
		verify(schedulerMock).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test
	public void unschedule() throws SchedulerException
	{
		String id = "id";
		fileIngesterJobScheduler.unschedule(id);
		verify(schedulerMock).deleteJob((new JobKey(id, FileIngesterJobScheduler.JOB_GROUP)));
	}
}
