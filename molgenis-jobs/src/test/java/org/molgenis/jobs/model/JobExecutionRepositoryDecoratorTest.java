package org.molgenis.jobs.model;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.ActiveJobExecutionDeleteForbiddenException;
import org.molgenis.jobs.model.JobExecution.Status;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JobExecutionRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<JobExecution> delegateRepository;

  private JobExecutionRepositoryDecorator jobExecutionRepositoryDecorator;

  @BeforeMethod
  public void setUpBeforeMethod() {
    jobExecutionRepositoryDecorator = new JobExecutionRepositoryDecorator(delegateRepository);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testJobExecutionRepositoryDecorator() {
    new JobExecutionRepositoryDecorator(null);
  }

  @Test
  public void testDeleteAllowed() {
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.CANCELED).getMock();
    jobExecutionRepositoryDecorator.delete(jobExecution);
    verify(delegateRepository).delete(jobExecution);
  }

  @Test(expectedExceptions = ActiveJobExecutionDeleteForbiddenException.class)
  public void testDeleteForbidden() {
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.PENDING).getMock();
    jobExecutionRepositoryDecorator.delete(jobExecution);
  }

  @Test
  public void testDeleteByIdAllowed() {
    Object jobExecutionId = "myJobExecutionId";
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.FAILED).getMock();
    when(delegateRepository.findOneById(jobExecutionId)).thenReturn(jobExecution);

    jobExecutionRepositoryDecorator.deleteById(jobExecutionId);
    verify(delegateRepository).deleteById(jobExecutionId);
  }

  @Test(expectedExceptions = ActiveJobExecutionDeleteForbiddenException.class)
  public void testDeleteByIdForbidden() {
    Object jobExecutionId = "myJobExecutionId";
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.RUNNING).getMock();
    when(delegateRepository.findOneById(jobExecutionId)).thenReturn(jobExecution);

    jobExecutionRepositoryDecorator.deleteById(jobExecutionId);
    verify(delegateRepository).deleteById(jobExecutionId);
  }

  @Test(expectedExceptions = UnknownEntityException.class)
  public void testDeleteByIdUnknownJobExecution() {
    EntityType entityType = mock(EntityType.class);
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Object jobExecutionId = "unknownJobExecutionId";
    jobExecutionRepositoryDecorator.deleteById(jobExecutionId);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDeleteAllAllowed() {
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.SUCCESS).getMock();
    doAnswer(
            invocation -> {
              ((Consumer<List<Entity>>) invocation.getArgument(1))
                  .accept(singletonList(jobExecution));
              return null;
            })
        .when(delegateRepository)
        .forEachBatched(any(), any(), eq(1000));

    jobExecutionRepositoryDecorator.deleteAll();
    verify(delegateRepository).deleteAll();
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = ActiveJobExecutionDeleteForbiddenException.class)
  public void testDeleteAllForbidden() {
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.PENDING).getMock();
    doAnswer(
            invocation -> {
              ((Consumer<List<Entity>>) invocation.getArgument(1))
                  .accept(singletonList(jobExecution));
              return null;
            })
        .when(delegateRepository)
        .forEachBatched(any(), any(), eq(1000));

    jobExecutionRepositoryDecorator.deleteAll();
    verify(delegateRepository).deleteAll();
  }

  @Test
  public void testDeleteStreamAllowed() {
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.SUCCESS).getMock();
    jobExecutionRepositoryDecorator.delete(Stream.of(jobExecution));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<JobExecution>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(entityStreamCaptor.capture());
    assertEquals(entityStreamCaptor.getValue().collect(toList()), singletonList(jobExecution));
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test(expectedExceptions = ActiveJobExecutionDeleteForbiddenException.class)
  public void testDeleteStreamForbidden() {
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.RUNNING).getMock();
    jobExecutionRepositoryDecorator.delete(Stream.of(jobExecution));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<JobExecution>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(entityStreamCaptor.capture());
    entityStreamCaptor.getValue().collect(toList());
  }

  @Test
  public void testDeleteAllStreamAllowed() {
    Object jobExecutionId = "myJobExecutionId";
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.SUCCESS).getMock();
    when(delegateRepository.findOneById(jobExecutionId)).thenReturn(jobExecution);

    jobExecutionRepositoryDecorator.deleteAll(Stream.of(jobExecutionId));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> entityIdStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(entityIdStreamCaptor.capture());
    assertEquals(entityIdStreamCaptor.getValue().collect(toList()), singletonList(jobExecutionId));
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test(expectedExceptions = ActiveJobExecutionDeleteForbiddenException.class)
  public void testDeleteAllStreamForbidden() {
    Object jobExecutionId = "myJobExecutionId";
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.RUNNING).getMock();
    when(delegateRepository.findOneById(jobExecutionId)).thenReturn(jobExecution);

    jobExecutionRepositoryDecorator.deleteAll(Stream.of(jobExecutionId));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> entityIdStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(entityIdStreamCaptor.capture());
    entityIdStreamCaptor.getValue().collect(toList());
  }
}
