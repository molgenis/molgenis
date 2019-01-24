package org.molgenis.core.ui.jobs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.EntityTypePermission.READ_DATA;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.schedule.JobScheduler;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.model.Menu;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JobsControllerTest extends AbstractMockitoTest {
  @Mock private UserAccountService userAccountService;
  @Mock private DataService dataService;
  @Mock private JobExecutionMetaData jobMetaDataMetaData;
  @Mock private JobScheduler jobScheduler;
  @Mock private MenuReaderService menuReaderService;
  @Mock private Menu menu;
  @Mock private JobExecution jobExecution;
  @Mock private EntityType jobExecutionEntityType;
  @Mock private UserPermissionEvaluator userPermissionEvaluator;

  private JobsController jobsController;

  @BeforeMethod
  public void beforeClass() {
    jobsController =
        new JobsController(
            userAccountService,
            dataService,
            jobMetaDataMetaData,
            jobScheduler,
            menuReaderService,
            userPermissionEvaluator);
  }

  @Test
  public void testCreateJobExecutionViewHref() {
    when(menuReaderService.findMenuItemPath(JobsController.ID)).thenReturn("/menu/jobs");
    assertEquals(
        jobsController.createJobExecutionViewHref("/api/v2/sys_MappingJobExecution/abcde", 2345),
        "/menu/jobs/viewJob/?jobHref=/api/v2/sys_MappingJobExecution/abcde&refreshTimeoutMillis=2345");
  }

  @Test
  public void testIsAllowedJobExecutionEntityTypeNoParent() {
    EntityType entityType = mock(EntityType.class);
    assertFalse(jobsController.isAllowedJobExecutionEntityType(entityType));
  }

  @Test
  public void testIsAllowedJobExecutionEntityTypeParentIsNotJobExecutionEntityType() {
    EntityType entityType = mock(EntityType.class);
    EntityType extendsEntityType =
        when(mock(EntityType.class).getId()).thenReturn("not_sys_job_JobExecution").getMock();
    when(entityType.getExtends()).thenReturn(extendsEntityType);
    assertFalse(jobsController.isAllowedJobExecutionEntityType(entityType));
  }

  @Test
  public void testIsAllowedJobExecutionEntityTypeNoReadDataPermissions() {
    when(jobMetaDataMetaData.getId()).thenReturn("sys_job_JobExecution");
    EntityType entityType =
        when(mock(EntityType.class).getId()).thenReturn("MyJobExecution").getMock();
    EntityType extendsEntityType =
        when(mock(EntityType.class).getId()).thenReturn("sys_job_JobExecution").getMock();
    when(entityType.getExtends()).thenReturn(extendsEntityType);
    assertFalse(jobsController.isAllowedJobExecutionEntityType(entityType));
  }

  @Test
  public void testIsAllowedJobExecutionEntityTypeReadDataPermissions() {
    when(jobMetaDataMetaData.getId()).thenReturn("sys_job_JobExecution");
    EntityType entityType =
        when(mock(EntityType.class).getId()).thenReturn("MyJobExecution").getMock();
    EntityType extendsEntityType =
        when(mock(EntityType.class).getId()).thenReturn("sys_job_JobExecution").getMock();
    when(entityType.getExtends()).thenReturn(extendsEntityType);
    when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(entityType), READ_DATA))
        .thenReturn(true);
    assertTrue(jobsController.isAllowedJobExecutionEntityType(entityType));
  }
}
