package org.molgenis.api.metadata.v3;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.api.model.Query.Operator.IN;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.api.metadata.v3.job.EntityTypeSerializer;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecutionFactory;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionFactory;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionMetadata.Action;
import org.molgenis.api.model.Query;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.test.AbstractMockitoTest;

@SuppressWarnings("squid:S1192") // string literals should not be duplicated
class MetadataApiJobServiceImplTest extends AbstractMockitoTest {

  @Mock MetadataUpsertJobExecutionFactory metadataUpsertJobExecutionFactory;
  @Mock MetadataDeleteJobExecutionFactory metadataDeleteJobExecutionFactory;
  @Mock JobExecutor jobExecutor;
  @Mock DataService dataService;
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
            entityTypeSerializer,
            dataService,
            queryMapper);
  }

  @Test
  void testScheduleCreate() {
    EntityType entityType = mock(EntityType.class);
    MetadataUpsertJobExecution jobExecution = mock(MetadataUpsertJobExecution.class);
    when(metadataUpsertJobExecutionFactory.create()).thenReturn(jobExecution);
    when(entityTypeSerializer.serializeEntityType(entityType)).thenReturn("entity data");

    metadataApiJobService.scheduleCreate(entityType);

    assertAll(
        () -> verify(jobExecution).setAction(Action.CREATE),
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

    metadataApiJobService.scheduleDelete(entityTypeId);

    assertAll(
        () -> verify(jobExecution).setEntityTypeIds(singletonList(entityTypeId)),
        () -> verify(jobExecutor).submit(jobExecution));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testScheduleDeleteQuery() {
    MetadataDeleteJobExecution jobExecution = mock(MetadataDeleteJobExecution.class);
    when(metadataDeleteJobExecutionFactory.create()).thenReturn(jobExecution);
    Query query = Query.create("id", IN, asList("MyEntityTypeId0", "MyEntityTypeId1"));
    org.molgenis.data.Query<EntityType> dataServiceQuery = mock(org.molgenis.data.Query.class);
    EntityType entityType0 = mock(EntityType.class);
    when(entityType0.getId()).thenReturn("MyEntityTypeId0");
    EntityType entityType1 = mock(EntityType.class);
    when(entityType1.getId()).thenReturn("MyEntityTypeId1");
    when(dataServiceQuery.findAll()).thenReturn(Stream.of(entityType0, entityType1));
    Repository<EntityType> entityTypeRepository = mock(Repository.class);
    when(dataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class))
        .thenReturn(entityTypeRepository);
    when(queryMapper.map(query, entityTypeRepository)).thenReturn(dataServiceQuery);

    metadataApiJobService.scheduleDelete(query);

    assertAll(
        () -> verify(jobExecution).setEntityTypeIds(asList("MyEntityTypeId0", "MyEntityTypeId1")),
        () -> verify(jobExecutor).submit(jobExecution));
  }
}
