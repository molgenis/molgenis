package org.molgenis.api.metadata.v3;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
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
import org.molgenis.data.DataService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.test.AbstractMockitoTest;

@SuppressWarnings("squid:S1192") // string literals should not be duplicated
class MetadataApiJobServiceImplTest extends AbstractMockitoTest {

  @Mock MetadataUpsertJobExecutionFactory metadataUpsertJobExecutionFactory;
  @Mock MetadataDeleteJobExecutionFactory metadataDeleteJobExecutionFactory;
  @Mock JobExecutor jobExecutor;
  @Mock DataService dataService;
  @Mock MetaDataService metadataService;
  @Mock QueryMapper queryMapper;
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

    metadataApiJobService.scheduleCreate(entityType);

    assertAll(
        () -> verify(jobExecution).setEntityTypeData("entity data"),
        () -> verify(jobExecutor).submit(jobExecution));
  }

  @Test
  void testScheduleUpdate() {
    EntityType entityType = mock(EntityType.class);
    MetadataUpsertJobExecution jobExecution = mock(MetadataUpsertJobExecution.class);
    when(metadataUpsertJobExecutionFactory.create()).thenReturn(jobExecution);
    when(entityTypeSerializer.serializeEntityType(entityType)).thenReturn("entity data");

    metadataApiJobService.scheduleUpdate(entityType);

    assertAll(
        () -> verify(jobExecution).setAction(Action.UPDATE),
        () -> verify(jobExecution).setEntityTypeData("entity data"),
        () -> verify(jobExecutor).submit(jobExecution));
  }

  @Test
  void testScheduleDelete() {
    String entityTypeId = "test_entity1";
    MetadataDeleteJobExecution jobExecution = mock(MetadataDeleteJobExecution.class);
    when(metadataDeleteJobExecutionFactory.create()).thenReturn(jobExecution);

    metadataApiJobService.scheduleDelete(singletonList(entityTypeId));

    assertAll(
        () -> verify(jobExecution).setIds(singletonList(entityTypeId)),
        () -> verify(jobExecutor).submit(jobExecution));
  }
}
