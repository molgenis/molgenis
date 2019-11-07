package org.molgenis.api.metadata.v3;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.api.model.Query.Operator.IN;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.api.metadata.v3.exception.ZeroResultsException;
import org.molgenis.api.metadata.v3.job.EntityTypeSerializer;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecutionFactory;
import org.molgenis.api.metadata.v3.job.MetadataDeleteJobExecutionMetadata.DeleteType;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionFactory;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecutionMetadata.Action;
import org.molgenis.api.model.Query;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
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
            entityTypeSerializer,
            dataService,
            metadataService,
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
  void testScheduleAttributeDelete() {
    String entityTypeId = "test_entity1";
    EntityType entityType = mock(EntityType.class);
    when(metadataService.getEntityType(entityTypeId)).thenReturn(Optional.of(entityType));
    when(entityType.getId()).thenReturn(entityTypeId);
    String attributeId = "attr1";
    Attribute attribute = mock(Attribute.class);
    when(dataService.findOneById(ATTRIBUTE_META_DATA, attributeId, Attribute.class))
        .thenReturn(attribute);
    when(attribute.getEntity()).thenReturn(entityType);
    MetadataDeleteJobExecution jobExecution = mock(MetadataDeleteJobExecution.class);
    when(metadataDeleteJobExecutionFactory.create()).thenReturn(jobExecution);

    metadataApiJobService.scheduleDeleteAttribute(entityTypeId, attributeId);

    assertAll(
        () -> verify(jobExecution).setDeleteType(DeleteType.ATTRIBUTE),
        () -> verify(jobExecution).setIds(singletonList(attributeId)),
        () -> verify(jobExecutor).submit(jobExecution));
  }

  @Test
  void testScheduleAttributeDeleteUnknownEntityType() {
    String entityTypeId = "unknown";

    assertThrows(
        UnknownEntityTypeException.class,
        () -> metadataApiJobService.scheduleDeleteAttribute(entityTypeId, "attr1"));
  }

  @Test
  void testScheduleAttributeDeleteUnknownAttribute() {
    String entityTypeId = "test_entity1";
    String attributeId = "unknown";
    EntityType entityType = mock(EntityType.class);
    when(metadataService.getEntityType(entityTypeId)).thenReturn(Optional.of(entityType));

    assertThrows(
        UnknownAttributeException.class,
        () -> metadataApiJobService.scheduleDeleteAttribute(entityTypeId, attributeId));
  }

  @Test
  void testScheduleAttributeDeleteAttributeNotPartOfThisEntityType() {
    String entityTypeId = "test_entity1";
    EntityType entityType = mock(EntityType.class);
    when(metadataService.getEntityType(entityTypeId)).thenReturn(Optional.of(entityType));
    String otherEntityTypeId = "test_entity2";
    EntityType otherEntityType = mock(EntityType.class);
    when(otherEntityType.getId()).thenReturn(otherEntityTypeId);
    String attributeId = "unknown";
    Attribute attribute = mock(Attribute.class);
    when(dataService.findOneById(ATTRIBUTE_META_DATA, attributeId, Attribute.class))
        .thenReturn(attribute);
    when(attribute.getEntity()).thenReturn(otherEntityType);

    assertThrows(
        UnknownAttributeException.class,
        () -> metadataApiJobService.scheduleDeleteAttribute(entityTypeId, attributeId));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testScheduleAttributeDeleteQuery() {
    String entityTypeId = "test_entity1";
    when(metadataService.hasEntityType(entityTypeId)).thenReturn(true);
    MetadataDeleteJobExecution jobExecution = mock(MetadataDeleteJobExecution.class);
    when(metadataDeleteJobExecutionFactory.create()).thenReturn(jobExecution);
    Query query = Query.create("identifier", IN, asList("attr1", "attr2"));
    org.molgenis.data.Query<Attribute> dataServiceQuery = mock(org.molgenis.data.Query.class);
    when(dataServiceQuery.and()).thenReturn(dataServiceQuery);
    when(dataServiceQuery.eq(AttributeMetadata.ENTITY, entityTypeId)).thenReturn(dataServiceQuery);
    Attribute attribute1 = mock(Attribute.class);
    when(attribute1.getIdentifier()).thenReturn("attr1");
    Attribute attribute2 = mock(Attribute.class);
    when(attribute2.getIdentifier()).thenReturn("attr2");
    when(dataServiceQuery.findAll()).thenReturn(Stream.of(attribute1, attribute2));
    Repository<Attribute> attributeRepository = mock(Repository.class);
    when(dataService.getRepository(ATTRIBUTE_META_DATA, Attribute.class))
        .thenReturn(attributeRepository);
    when(queryMapper.map(query, attributeRepository)).thenReturn(dataServiceQuery);

    metadataApiJobService.scheduleDeleteAttribute(entityTypeId, query);

    assertAll(
        () -> verify(jobExecution).setDeleteType(DeleteType.ATTRIBUTE),
        () -> verify(jobExecution).setIds(asList("attr1", "attr2")),
        () -> verify(jobExecutor).submit(jobExecution));
  }

  @Test
  void testScheduleAttributeDeleteQueryUnknownEntityType() {
    String entityTypeId = "unknown";

    assertThrows(
        UnknownEntityTypeException.class,
        () -> metadataApiJobService.scheduleDeleteAttribute(entityTypeId, mock(Query.class)));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testScheduleAttributeDeleteQueryNoResults() {
    String entityTypeId = "test_entity1";
    when(metadataService.hasEntityType(entityTypeId)).thenReturn(true);
    Query query = Query.create("identifier", IN, asList("attr1", "attr2"));
    org.molgenis.data.Query<Attribute> dataServiceQuery = mock(org.molgenis.data.Query.class);
    when(dataServiceQuery.and()).thenReturn(dataServiceQuery);
    when(dataServiceQuery.eq(AttributeMetadata.ENTITY, entityTypeId)).thenReturn(dataServiceQuery);
    when(dataServiceQuery.findAll()).thenReturn(Stream.empty());
    Repository<Attribute> attributeRepository = mock(Repository.class);
    when(dataService.getRepository(ATTRIBUTE_META_DATA, Attribute.class))
        .thenReturn(attributeRepository);
    when(queryMapper.map(query, attributeRepository)).thenReturn(dataServiceQuery);

    assertThrows(
        ZeroResultsException.class,
        () -> metadataApiJobService.scheduleDeleteAttribute(entityTypeId, query));
  }

  @Test
  void testScheduleEntityTypeDelete() {
    String entityTypeId = "test_entity1";
    when(metadataService.hasEntityType(entityTypeId)).thenReturn(true);
    MetadataDeleteJobExecution jobExecution = mock(MetadataDeleteJobExecution.class);
    when(metadataDeleteJobExecutionFactory.create()).thenReturn(jobExecution);

    metadataApiJobService.scheduleDeleteEntityType(entityTypeId);

    assertAll(
        () -> verify(jobExecution).setDeleteType(DeleteType.ENTITY_TYPE),
        () -> verify(jobExecution).setIds(singletonList(entityTypeId)),
        () -> verify(jobExecutor).submit(jobExecution));
  }

  @Test
  void testScheduleEntityTypeDeleteUnknownEntityType() {
    String entityTypeId = "unknown";

    assertThrows(
        UnknownEntityTypeException.class,
        () -> metadataApiJobService.scheduleDeleteEntityType(entityTypeId));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testScheduleEntityTypeDeleteQuery() {
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

    metadataApiJobService.scheduleDeleteEntityType(query);

    assertAll(
        () -> verify(jobExecution).setDeleteType(DeleteType.ENTITY_TYPE),
        () -> verify(jobExecution).setIds(asList("MyEntityTypeId0", "MyEntityTypeId1")),
        () -> verify(jobExecutor).submit(jobExecution));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testScheduleEntityTypeDeleteQueryNoResults() {
    Query query = Query.create("id", IN, asList("MyEntityTypeId0", "MyEntityTypeId1"));
    org.molgenis.data.Query<EntityType> dataServiceQuery = mock(org.molgenis.data.Query.class);
    when(dataServiceQuery.findAll()).thenReturn(Stream.empty());
    Repository<EntityType> entityTypeRepository = mock(Repository.class);
    when(dataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class))
        .thenReturn(entityTypeRepository);
    when(queryMapper.map(query, entityTypeRepository)).thenReturn(dataServiceQuery);

    assertThrows(
        ZeroResultsException.class, () -> metadataApiJobService.scheduleDeleteEntityType(query));
  }
}
