package org.molgenis.ui.jobs;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.jobs.schedule.JobScheduler;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.service.UserAccountService;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuReaderService;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

public class JobsControllerTest
{
	JobsController jobsController;
	@Mock
	UserAccountService userAccountService;
	@Mock
	DataService dataService;
	@Mock
	JobExecutionMetaData jobMetaDataMetaData;
	@Mock
	JobScheduler jobScheduler;
	@Mock
	MenuReaderService menuReaderService;
	@Mock
	Menu menu;
	@Mock
	JobExecution jobExecution;
	@Mock
	EntityType jobExecutionEntityType;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
		jobsController = new JobsController(userAccountService, dataService, jobMetaDataMetaData, jobScheduler,
				menuReaderService);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		reset(userAccountService, dataService, jobMetaDataMetaData, jobScheduler, menuReaderService, jobExecution, menu,
				jobExecutionEntityType);
		when(jobExecution.getEntityType()).thenReturn(jobExecutionEntityType);
		when(menuReaderService.getMenu()).thenReturn(menu);
		when(menu.findMenuItemPath(JobsController.ID)).thenReturn("/jobs");
		when(jobExecutionEntityType.getId()).thenReturn("sys_MappingJobExecution");
		when(jobExecution.getIdValue()).thenReturn("abcde");
	}

	@Test
	public void testCreateJobExecutionViewHref()
	{
		assertEquals(jobsController.createJobExecutionViewHref(jobExecution, 2345),
				"/jobs/viewJob/?jobHref=/api/v2/sys_MappingJobExecution/abcde&refreshTimeoutMillis=2345");
	}
}