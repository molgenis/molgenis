package org.molgenis.jobs;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.test.AbstractMockitoTest;

class JobsServiceImplTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private JobExecutor jobExecutor;
  private JobsServiceImpl jobsService;

  @BeforeEach
  void setUpBeforeMethod() {
    jobsService = new JobsServiceImpl(dataService, jobExecutor);
  }

  @Test
  void testJobServiceImpl() {
    assertThrows(NullPointerException.class, () -> new JobsServiceImpl(null, null));
  }

  @Test
  void testCancel() {
    String jobExecutionType = "MyJobExecutionType";
    String jobExecutionId = "MyJobExecutionId";

    EntityType jobExecutionMetadataGrandparent = mock(EntityType.class);
    when(jobExecutionMetadataGrandparent.getId()).thenReturn("sys_job_JobExecution");
    EntityType jobExecutionMetadataParent = mock(EntityType.class);
    when(jobExecutionMetadataParent.getExtends()).thenReturn(jobExecutionMetadataGrandparent);
    EntityType jobExecutionMetadata = mock(EntityType.class);
    when(jobExecutionMetadata.getExtends()).thenReturn(jobExecutionMetadataParent);
    when(dataService.getEntityType(jobExecutionType)).thenReturn(jobExecutionMetadata);

    JobExecution jobExecution = mock(JobExecution.class);
    when(dataService.findOneById(jobExecutionType, jobExecutionId)).thenReturn(jobExecution);

    jobsService.cancel(jobExecutionType, jobExecutionId);
    verify(jobExecutor).cancel(jobExecution);
  }

  @Test
  void testCancelUnknownJobExecution() {
    String jobExecutionType = "MyJobExecutionType";
    String jobExecutionId = "MyJobExecutionId";

    EntityType jobExecutionMetadataParent = mock(EntityType.class);
    when(jobExecutionMetadataParent.getId()).thenReturn("sys_job_JobExecution");
    EntityType jobExecutionMetadata = mock(EntityType.class);
    when(jobExecutionMetadata.getExtends()).thenReturn(jobExecutionMetadataParent);
    when(dataService.getEntityType(jobExecutionType)).thenReturn(jobExecutionMetadata);

    assertThrows(
        UnknownEntityException.class, () -> jobsService.cancel(jobExecutionType, jobExecutionId));
  }

  @Test
  void testCancelInvalidJobExecutionType() {
    String jobExecutionType = "MyJobExecutionType";
    String jobExecutionId = "MyJobExecutionId";
    EntityType jobExecutionMetadata = mock(EntityType.class);
    when(dataService.getEntityType(jobExecutionType)).thenReturn(jobExecutionMetadata);
    assertThrows(
        InvalidJobExecutionTypeException.class,
        () -> jobsService.cancel(jobExecutionType, jobExecutionId));
  }
}
