package org.molgenis.api.metadata.v3;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.api.metadata.v3.job.EntityTypeSerializer;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecutionFactory;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionFactory;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionMetadata.Action;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.test.AbstractMockitoTest;

@SuppressWarnings("java:S1192") // string literals should not be duplicated
class MetadataApiJobServiceImplTest extends AbstractMockitoTest {

  @Mock MetadataUpsertJobExecutionFactory metadataUpsertJobExecutionFactory;
  @Mock MetadataDeleteJobExecutionFactory metadataDeleteJobExecutionFactory;
  @Mock JobExecutor jobExecutor;
  @Mock EntityTypeSerializer entityTypeSerializer;

  private MetadataApiJobService metadataApiJobService;

  @BeforeEach
  void setUpBeforeEach() {
    metadataApiJobService =
        new MetadataApiJobServiceImpl(
            metadataUpsertJobExecutionFactory,
            metadataDeleteJobExecutionFactory,
            jobExecutor,
            entityTypeSerializer);
  }

  @Test
  void testScheduleCreate() {
    EntityType entityType = mock(EntityType.class);
    MetadataUpsertJobExecution jobExecution = mock(MetadataUpsertJobExecution.class);
    when(metadataUpsertJobExecutionFactory.create()).thenReturn(jobExecution);
    when(entityTypeSerializer.serializeEntityType(entityType)).thenReturn("entity data");

    MetadataUpsertJobExecution actualJobExecution =
        metadataApiJobService.scheduleCreate(entityType);

    assertAll(
        () -> assertEquals(jobExecution, actualJobExecution),
        () -> verify(jobExecution).setEntityTypeData("entity data"),
        () -> verify(jobExecutor).submit(jobExecution));
  }

  @Test
  void testScheduleUpdate() {
    EntityType entityType = mock(EntityType.class);
    MetadataUpsertJobExecution jobExecution = mock(MetadataUpsertJobExecution.class);
    when(metadataUpsertJobExecutionFactory.create()).thenReturn(jobExecution);
    when(entityTypeSerializer.serializeEntityType(entityType)).thenReturn("entity data");

    MetadataUpsertJobExecution actualJobExecution =
        metadataApiJobService.scheduleUpdate(entityType);

    assertAll(
        () -> assertEquals(jobExecution, actualJobExecution),
        () -> verify(jobExecution).setAction(Action.UPDATE),
        () -> verify(jobExecution).setEntityTypeData("entity data"),
        () -> verify(jobExecutor).submit(jobExecution));
  }

  @Test
  void testScheduleDelete() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("entity1");
    MetadataDeleteJobExecution jobExecution = mock(MetadataDeleteJobExecution.class);
    when(metadataDeleteJobExecutionFactory.create()).thenReturn(jobExecution);

    MetadataDeleteJobExecution actualJobExecution =
        metadataApiJobService.scheduleDelete(entityType);

    assertAll(
        () -> assertEquals(jobExecution, actualJobExecution),
        () -> verify(jobExecution).setIds(singletonList("entity1")),
        () -> verify(jobExecutor).submit(jobExecution));
  }

  @Test
  void testScheduleDeleteCollection() {
    EntityType entityType1 = mock(EntityType.class);
    when(entityType1.getId()).thenReturn("entity1");
    EntityType entityType2 = mock(EntityType.class);
    when(entityType2.getId()).thenReturn("entity2");
    MetadataDeleteJobExecution jobExecution = mock(MetadataDeleteJobExecution.class);
    when(metadataDeleteJobExecutionFactory.create()).thenReturn(jobExecution);

    MetadataDeleteJobExecution actualJobExecution =
        metadataApiJobService.scheduleDelete(asList(entityType1, entityType2));

    assertAll(
        () -> assertEquals(jobExecution, actualJobExecution),
        () -> verify(jobExecution).setIds(asList("entity1", "entity2")),
        () -> verify(jobExecutor).submit(jobExecution));
  }
}
