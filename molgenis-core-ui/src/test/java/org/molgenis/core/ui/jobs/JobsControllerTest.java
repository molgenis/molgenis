package org.molgenis.core.ui.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.EntityTypePermission.READ_DATA;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.jobs.JobsService;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.schedule.JobScheduler;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.menu.MenuReaderService;

class JobsControllerTest extends AbstractMockitoTest {
  @Mock private JobsService jobsService;
  @Mock private UserAccountService userAccountService;
  @Mock private DataService dataService;
  @Mock private JobExecutionMetaData jobMetaDataMetaData;
  @Mock private JobScheduler jobScheduler;
  @Mock private MenuReaderService menuReaderService;
  @Mock private UserPermissionEvaluator userPermissionEvaluator;

  private JobsController jobsController;

  @BeforeEach
  void beforeClass() {
    jobsController =
        new JobsController(
            jobsService,
            userAccountService,
            dataService,
            jobMetaDataMetaData,
            jobScheduler,
            menuReaderService,
            userPermissionEvaluator);
  }

  @Test
  void testCancel() {
    String jobExecutionType = "MyJobExecutionType";
    String jobExecutionId = "MyJobExecutionId";
    jobsController.cancel(jobExecutionType, jobExecutionId);
    verify(jobsService).cancel(jobExecutionType, jobExecutionId);
  }

  @Test
  void testCreateJobExecutionViewHref() {
    when(menuReaderService.findMenuItemPath(JobsController.ID)).thenReturn("/menu/jobs");
    assertEquals(
        "/menu/jobs/viewJob/?jobHref=/api/v2/sys_MappingJobExecution/abcde&refreshTimeoutMillis=2345",
        jobsController.createJobExecutionViewHref("/api/v2/sys_MappingJobExecution/abcde", 2345));
  }

  @Test
  void testIsAllowedJobExecutionEntityTypeNoParent() {
    EntityType entityType = mock(EntityType.class);
    assertFalse(jobsController.isAllowedJobExecutionEntityType(entityType));
  }

  @Test
  void testIsAllowedJobExecutionEntityTypeParentIsNotJobExecutionEntityType() {
    EntityType entityType = mock(EntityType.class);
    EntityType extendsEntityType =
        when(mock(EntityType.class).getId()).thenReturn("not_sys_job_JobExecution").getMock();
    when(entityType.getExtends()).thenReturn(extendsEntityType);
    assertFalse(jobsController.isAllowedJobExecutionEntityType(entityType));
  }

  @Test
  void testIsAllowedJobExecutionEntityTypeNoReadDataPermissions() {
    when(jobMetaDataMetaData.getId()).thenReturn("sys_job_JobExecution");
    EntityType entityType =
        when(mock(EntityType.class).getId()).thenReturn("MyJobExecution").getMock();
    EntityType extendsEntityType =
        when(mock(EntityType.class).getId()).thenReturn("sys_job_JobExecution").getMock();
    when(entityType.getExtends()).thenReturn(extendsEntityType);
    assertFalse(jobsController.isAllowedJobExecutionEntityType(entityType));
  }

  @Test
  void testIsAllowedJobExecutionEntityTypeReadDataPermissions() {
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
