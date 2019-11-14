package org.molgenis.api.metadata.v3;

import static java.util.Arrays.asList;
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
import org.molgenis.api.model.Query;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class MetadataApiServiceImplTest extends AbstractMockitoTest {

  @Mock private MetaDataService metadataService;
  @Mock private QueryMapper queryMapper;
  @Mock private SortMapper sortMapper;
  @Mock private DataService dataService;
  @Mock private MetadataApiJobService metadataApiJobService;
  private MetadataApiServiceImpl metadataApiService;

  @BeforeEach
  void setUpBeforeEach() {
    metadataApiService =
        new MetadataApiServiceImpl(
            metadataService, queryMapper, sortMapper, dataService, metadataApiJobService);
  }

  @Test
  void testMetadataApiServiceImpl() {
    assertThrows(
        NullPointerException.class,
        () -> new MetadataApiServiceImpl(null, null, null, null, metadataApiJobService));
  }

  @Test
  void testDeleteAttribute() {
    String entityTypeId = "entityTypeId";
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn(entityTypeId);
    when(metadataService.getEntityType(entityTypeId)).thenReturn(Optional.of(entityType));
    String attributeId = "attr1";
    Attribute attr = mock(Attribute.class);
    when(attr.getEntity()).thenReturn(entityType);
    when(dataService.findOneById(ATTRIBUTE_META_DATA, attributeId, Attribute.class))
        .thenReturn(attr);

    metadataApiService.deleteAttributeAsync(entityTypeId, attributeId);

    assertAll(
        () -> verify(entityType).removeAttribute(attr),
        () -> verify(metadataApiJobService).scheduleUpdate(entityType));
  }

  @Test
  void testDeleteAttributeUnknownEntityType() {
    String entityTypeId = "unknown";

    assertThrows(
        UnknownEntityTypeException.class,
        () -> metadataApiService.deleteAttributeAsync(entityTypeId, "attr1"));
  }

  @Test
  void testDeleteAttributeUnknownAttribute() {
    String entityTypeId = "test_entity1";
    String attributeId = "unknown";
    EntityType entityType = mock(EntityType.class);
    when(metadataService.getEntityType(entityTypeId)).thenReturn(Optional.of(entityType));

    assertThrows(
        UnknownAttributeException.class,
        () -> metadataApiService.deleteAttributeAsync(entityTypeId, attributeId));
  }

  @Test
  void testDeleteAttributeNotPartOfThisEntityType() {
    String entityTypeId = "test_entity1";
    EntityType entityType = mock(EntityType.class);
    when(metadataService.getEntityType(entityTypeId)).thenReturn(Optional.of(entityType));
    EntityType otherEntityType = mock(EntityType.class);
    when(otherEntityType.getId()).thenReturn("test_entity2");

    String attributeId = "unknown";
    Attribute attribute = mock(Attribute.class);
    when(dataService.findOneById(ATTRIBUTE_META_DATA, attributeId, Attribute.class))
        .thenReturn(attribute);
    when(attribute.getEntity()).thenReturn(otherEntityType);

    assertThrows(
        UnknownAttributeException.class,
        () -> metadataApiService.deleteAttributeAsync(entityTypeId, attributeId));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteAttributesQuery() {
    String entityTypeId = "test_entity1";
    EntityType entityType = mock(EntityType.class);
    when(metadataService.getEntityType(entityTypeId)).thenReturn(Optional.of(entityType));
    Query query = Query.create("identifier", IN, asList("attr1", "attr2"));
    org.molgenis.data.Query<Attribute> dataServiceQuery = mock(org.molgenis.data.Query.class);
    when(dataServiceQuery.and()).thenReturn(dataServiceQuery);
    when(dataServiceQuery.eq(AttributeMetadata.ENTITY, entityTypeId)).thenReturn(dataServiceQuery);
    Attribute attribute1 = mock(Attribute.class);
    Attribute attribute2 = mock(Attribute.class);
    when(dataServiceQuery.findAll()).thenReturn(Stream.of(attribute1, attribute2));
    Repository<Attribute> attributeRepository = mock(Repository.class);
    when(dataService.getRepository(ATTRIBUTE_META_DATA, Attribute.class))
        .thenReturn(attributeRepository);
    when(queryMapper.map(query, attributeRepository)).thenReturn(dataServiceQuery);

    metadataApiService.deleteAttributesAsync(entityTypeId, query);

    assertAll(
        () -> verify(entityType).removeAttribute(attribute1),
        () -> verify(entityType).removeAttribute(attribute2),
        () -> metadataApiJobService.scheduleUpdate(entityType));
  }

  @Test
  void testDeleteAttributesQueryUnknownEntityType() {
    String entityTypeId = "unknown";

    assertThrows(
        UnknownEntityTypeException.class,
        () -> metadataApiService.deleteAttributesAsync(entityTypeId, mock(Query.class)));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteAttributesQueryNoResults() {
    String entityTypeId = "test_entity1";
    EntityType entityType = mock(EntityType.class);
    when(metadataService.getEntityType(entityTypeId)).thenReturn(Optional.of(entityType));
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
        () -> metadataApiService.deleteAttributesAsync(entityTypeId, query));
  }

  @Test
  void testDeleteEntityType() {
    String entityTypeId = "MyEntityTypeId";
    EntityType entityType = mock(EntityType.class);
    when(metadataService.getEntityType(entityTypeId)).thenReturn(Optional.of(entityType));

    metadataApiService.deleteEntityTypeAsync(entityTypeId);

    verify(metadataApiJobService).scheduleDelete(entityType);
  }

  @Test
  void testDeleteEntityTypeUnknownEntityType() {
    String entityTypeId = "unknown";

    assertThrows(
        UnknownEntityTypeException.class,
        () -> metadataApiService.deleteEntityTypeAsync(entityTypeId));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteEntityTypesQuery() {
    String entityTypeId0 = "MyEntityTypeId0";
    String entityTypeId1 = "MyEntityTypeId1";
    Query query = Query.create("id", IN, asList(entityTypeId0, entityTypeId1));
    org.molgenis.data.Query<EntityType> dataServiceQuery = mock(org.molgenis.data.Query.class);
    EntityType entityType0 = mock(EntityType.class);
    EntityType entityType1 = mock(EntityType.class);
    when(dataServiceQuery.findAll()).thenReturn(Stream.of(entityType0, entityType1));
    Repository<EntityType> entityTypeRepository = mock(Repository.class);
    when(dataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class))
        .thenReturn(entityTypeRepository);
    when(queryMapper.map(query, entityTypeRepository)).thenReturn(dataServiceQuery);

    metadataApiService.deleteEntityTypesAsync(query);

    assertAll(() -> verify(metadataApiJobService).scheduleDelete(asList(entityType0, entityType1)));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteEntityTypesQueryNoResults() {
    Query query = Query.create("id", IN, asList("MyEntityTypeId0", "MyEntityTypeId1"));
    org.molgenis.data.Query<EntityType> dataServiceQuery = mock(org.molgenis.data.Query.class);
    when(dataServiceQuery.findAll()).thenReturn(Stream.empty());
    Repository<EntityType> entityTypeRepository = mock(Repository.class);
    when(dataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class))
        .thenReturn(entityTypeRepository);
    when(queryMapper.map(query, entityTypeRepository)).thenReturn(dataServiceQuery);

    assertThrows(
        ZeroResultsException.class, () -> metadataApiService.deleteEntityTypesAsync(query));
  }
}
