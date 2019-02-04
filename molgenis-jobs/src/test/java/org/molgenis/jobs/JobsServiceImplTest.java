package org.molgenis.jobs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JobsServiceImplTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private JobExecutor jobExecutor;
  private JobsServiceImpl jobsService;

  @BeforeMethod
  public void setUpBeforeMethod() {
    jobsService = new JobsServiceImpl(dataService, jobExecutor);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testJobServiceImpl() {
    new JobsServiceImpl(null, null);
  }

  @Test
  public void testCancel() {
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

  @Test(expectedExceptions = UnknownEntityException.class)
  public void testCancelUnknownJobExecution() {
    String jobExecutionType = "MyJobExecutionType";
    String jobExecutionId = "MyJobExecutionId";

    EntityType jobExecutionMetadataParent = mock(EntityType.class);
    when(jobExecutionMetadataParent.getId()).thenReturn("sys_job_JobExecution");
    EntityType jobExecutionMetadata = mock(EntityType.class);
    when(jobExecutionMetadata.getExtends()).thenReturn(jobExecutionMetadataParent);
    when(dataService.getEntityType(jobExecutionType)).thenReturn(jobExecutionMetadata);

    jobsService.cancel(jobExecutionType, jobExecutionId);
  }

  @Test(expectedExceptions = InvalidJobExecutionTypeException.class)
  public void testCancelInvalidJobExecutionType() {
    String jobExecutionType = "MyJobExecutionType";
    String jobExecutionId = "MyJobExecutionId";
    EntityType jobExecutionMetadata = mock(EntityType.class);
    when(dataService.getEntityType(jobExecutionType)).thenReturn(jobExecutionMetadata);
    jobsService.cancel(jobExecutionType, jobExecutionId);
  }
}
