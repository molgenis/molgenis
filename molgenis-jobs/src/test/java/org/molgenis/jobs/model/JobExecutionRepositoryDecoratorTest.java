package org.molgenis.jobs.model;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.ActiveJobExecutionDeleteForbiddenException;
import org.molgenis.jobs.model.JobExecution.Status;
import org.molgenis.test.AbstractMockitoTest;

class JobExecutionRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<JobExecution> delegateRepository;

  private JobExecutionRepositoryDecorator jobExecutionRepositoryDecorator;

  @BeforeEach
  void setUpBeforeMethod() {
    jobExecutionRepositoryDecorator = new JobExecutionRepositoryDecorator(delegateRepository);
  }

  @Test
  void testJobExecutionRepositoryDecorator() {
    assertThrows(NullPointerException.class, () -> new JobExecutionRepositoryDecorator(null));
  }

  @Test
  void testDeleteAllowed() {
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.CANCELED).getMock();
    jobExecutionRepositoryDecorator.delete(jobExecution);
    verify(delegateRepository).delete(jobExecution);
  }

  @Test
  void testDeleteForbidden() {
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.PENDING).getMock();
    assertThrows(
        ActiveJobExecutionDeleteForbiddenException.class,
        () -> jobExecutionRepositoryDecorator.delete(jobExecution));
  }

  @Test
  void testDeleteByIdAllowed() {
    Object jobExecutionId = "myJobExecutionId";
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.FAILED).getMock();
    when(delegateRepository.findOneById(jobExecutionId)).thenReturn(jobExecution);

    jobExecutionRepositoryDecorator.deleteById(jobExecutionId);
    verify(delegateRepository).deleteById(jobExecutionId);
  }

  @Test
  void testDeleteByIdForbidden() {
    Object jobExecutionId = "myJobExecutionId";
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.RUNNING).getMock();
    when(delegateRepository.findOneById(jobExecutionId)).thenReturn(jobExecution);

    assertThrows(
        ActiveJobExecutionDeleteForbiddenException.class,
        () -> jobExecutionRepositoryDecorator.deleteById(jobExecutionId));
  }

  @Test
  void testDeleteByIdUnknownJobExecution() {
    EntityType entityType = mock(EntityType.class);
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Object jobExecutionId = "unknownJobExecutionId";
    assertThrows(
        UnknownEntityException.class,
        () -> jobExecutionRepositoryDecorator.deleteById(jobExecutionId));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteAllAllowed() {
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
  @Test
  void testDeleteAllForbidden() {
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

    assertThrows(
        ActiveJobExecutionDeleteForbiddenException.class,
        () -> jobExecutionRepositoryDecorator.deleteAll());
  }

  @Test
  void testDeleteStreamAllowed() {
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.SUCCESS).getMock();
    jobExecutionRepositoryDecorator.delete(Stream.of(jobExecution));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<JobExecution>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(entityStreamCaptor.capture());
    assertEquals(singletonList(jobExecution), entityStreamCaptor.getValue().collect(toList()));
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  void testDeleteStreamForbidden() {
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.RUNNING).getMock();
    jobExecutionRepositoryDecorator.delete(Stream.of(jobExecution));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<JobExecution>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(entityStreamCaptor.capture());
    assertThrows(
        ActiveJobExecutionDeleteForbiddenException.class,
        () -> entityStreamCaptor.getValue().collect(toList()));
  }

  @Test
  void testDeleteAllStreamAllowed() {
    Object jobExecutionId = "myJobExecutionId";
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.SUCCESS).getMock();
    when(delegateRepository.findOneById(jobExecutionId)).thenReturn(jobExecution);

    jobExecutionRepositoryDecorator.deleteAll(Stream.of(jobExecutionId));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> entityIdStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(entityIdStreamCaptor.capture());
    assertEquals(singletonList(jobExecutionId), entityIdStreamCaptor.getValue().collect(toList()));
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  void testDeleteAllStreamForbidden() {
    Object jobExecutionId = "myJobExecutionId";
    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getStatus()).thenReturn(Status.RUNNING).getMock();
    when(delegateRepository.findOneById(jobExecutionId)).thenReturn(jobExecution);

    jobExecutionRepositoryDecorator.deleteAll(Stream.of(jobExecutionId));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> entityIdStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(entityIdStreamCaptor.capture());
    assertThrows(
        ActiveJobExecutionDeleteForbiddenException.class,
        () -> entityIdStreamCaptor.getValue().collect(toList()));
  }
}
