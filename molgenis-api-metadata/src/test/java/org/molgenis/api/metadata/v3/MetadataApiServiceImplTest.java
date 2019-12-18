package org.molgenis.api.metadata.v3;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.api.model.Query.Operator.IN;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.api.data.QueryMapper;
import org.molgenis.api.data.SortMapper;
import org.molgenis.api.metadata.v3.job.MetadataUpsertJobExecution;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Query.Operator;
import org.molgenis.api.model.Sort;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;

class MetadataApiServiceImplTest extends AbstractMockitoTest {

  @Mock private MetaDataService metadataService;
  @Mock private QueryMapper queryMapper;
  @Mock private SortMapper sortMapper;
  @Mock private MetadataApiJobService metadataApiJobService;
  private MetadataApiServiceImpl metadataApiService;

  @BeforeEach
  void setUpBeforeEach() {
    metadataApiService =
        new MetadataApiServiceImpl(metadataService, queryMapper, sortMapper, metadataApiJobService);
  }

  @Test
  void testMetadataApiServiceImpl() {
    assertThrows(
        NullPointerException.class,
        () -> new MetadataApiServiceImpl(null, null, null, metadataApiJobService));
  }

  @Test
  void testFindEntityTypes() {
    EntityType entityType = mock(EntityType.class);
    @SuppressWarnings("unchecked")
    Repository<EntityType> entityTypeRepository = mock(Repository.class);
    when(entityTypeRepository.getEntityType()).thenReturn(entityType);
    when(metadataService.getRepository("sys_md_EntityType", EntityType.class))
        .thenReturn(Optional.of(entityTypeRepository));

    Query query =
        Query.builder().setItem("id").setOperator(Operator.EQUALS).setValue("id0").build();
    when(queryMapper.map(query, entityTypeRepository)).thenReturn(new QueryImpl<>());

    @SuppressWarnings("unchecked")
    ArgumentCaptor<org.molgenis.data.Query<EntityType>> findQueryCaptor =
        ArgumentCaptor.forClass(org.molgenis.data.Query.class);
    when(entityTypeRepository.findAll(findQueryCaptor.capture())).thenReturn(Stream.of(entityType));

    long total = 100L;
    @SuppressWarnings("unchecked")
    ArgumentCaptor<org.molgenis.data.Query<EntityType>> countQueryCaptor =
        ArgumentCaptor.forClass(org.molgenis.data.Query.class);
    when(entityTypeRepository.count(countQueryCaptor.capture())).thenReturn(total);

    Sort sort = Sort.EMPTY_SORT;
    org.molgenis.data.Sort repositorySort = mock(org.molgenis.data.Sort.class);
    when(sortMapper.map(sort, entityType)).thenReturn(repositorySort);

    int size = 5;
    int number = 2;

    EntityTypes expectedEntityTypes =
        EntityTypes.builder()
            .setEntityTypes(ImmutableList.of(entityType))
            .setTotal((int) total)
            .build();
    EntityTypes entityTypes = metadataApiService.findEntityTypes(query, sort, size, number);
    org.molgenis.data.Query<EntityType> findQuery = findQueryCaptor.getValue();
    org.molgenis.data.Query<EntityType> countQuery = countQueryCaptor.getValue();

    assertAll(
        () -> assertEquals(expectedEntityTypes, entityTypes),
        () -> assertEquals(size * number, findQuery.getOffset()),
        () -> assertEquals(size, findQuery.getPageSize()),
        () -> assertEquals(repositorySort, findQuery.getSort()),
        () -> assertEquals(0, countQuery.getOffset()),
        () -> assertEquals(Integer.MAX_VALUE, countQuery.getPageSize()));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindAttributes() {
    Repository<Attribute> attributeRepository = mock(Repository.class);
    EntityType entityType = mock(EntityType.class);
    when(attributeRepository.getEntityType()).thenReturn(entityType);
    when(metadataService.getRepository("sys_md_Attribute", Attribute.class))
        .thenReturn(Optional.of(attributeRepository));

    Query query = mock(Query.class);
    when(queryMapper.map(query, attributeRepository)).thenReturn(new QueryImpl<>());

    Attribute attribute = mock(Attribute.class);
    ArgumentCaptor<org.molgenis.data.Query<Attribute>> findQueryCaptor =
        ArgumentCaptor.forClass(org.molgenis.data.Query.class);
    when(attributeRepository.findAll(findQueryCaptor.capture())).thenReturn(Stream.of(attribute));

    long total = 100L;
    ArgumentCaptor<org.molgenis.data.Query<Attribute>> countQueryCaptor =
        ArgumentCaptor.forClass(org.molgenis.data.Query.class);
    when(attributeRepository.count(countQueryCaptor.capture())).thenReturn(total);

    Sort sort = Sort.EMPTY_SORT;
    org.molgenis.data.Sort repositorySort = mock(org.molgenis.data.Sort.class);
    when(sortMapper.map(sort, entityType)).thenReturn(repositorySort);

    int size = 5;
    int number = 2;

    Attributes expectedAttributes =
        Attributes.builder()
            .setAttributes(ImmutableList.of(attribute))
            .setTotal((int) total)
            .build();

    String entityTypeId = "MyEntityTypeId";
    when(metadataService.hasEntityType(entityTypeId)).thenReturn(true);

    Attributes attributes =
        metadataApiService.findAttributes(entityTypeId, query, sort, size, number);
    org.molgenis.data.Query<Attribute> findQuery = findQueryCaptor.getValue();
    org.molgenis.data.Query<Attribute> countQuery = countQueryCaptor.getValue();
    assertAll(
        () -> assertEquals(expectedAttributes, attributes),
        () ->
            assertEquals(
                singletonList(new QueryRule("entity", QueryRule.Operator.EQUALS, entityTypeId)),
                findQuery.getRules()),
        () -> assertEquals(size * number, findQuery.getOffset()),
        () -> assertEquals(size, findQuery.getPageSize()),
        () -> assertEquals(repositorySort, findQuery.getSort()),
        () ->
            assertEquals(
                singletonList(new QueryRule("entity", QueryRule.Operator.EQUALS, entityTypeId)),
                countQuery.getRules()),
        () -> assertEquals(0, countQuery.getOffset()),
        () -> assertEquals(Integer.MAX_VALUE, countQuery.getPageSize()));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindAttributesNestedQuery() {
    Repository<Attribute> attributeRepository = mock(Repository.class);
    EntityType entityType = mock(EntityType.class);
    when(attributeRepository.getEntityType()).thenReturn(entityType);
    when(metadataService.getRepository("sys_md_Attribute", Attribute.class))
        .thenReturn(Optional.of(attributeRepository));

    Query query = mock(Query.class);
    org.molgenis.data.Query<Attribute> repositoryQuery =
        new QueryImpl<>(
            asList(
                new QueryRule("id", QueryRule.Operator.EQUALS, "id0"),
                new QueryRule(QueryRule.Operator.OR),
                new QueryRule("id", QueryRule.Operator.EQUALS, "id1")));
    when(queryMapper.map(query, attributeRepository)).thenReturn(repositoryQuery);

    Attribute attribute = mock(Attribute.class);
    ArgumentCaptor<org.molgenis.data.Query<Attribute>> findQueryCaptor =
        ArgumentCaptor.forClass(org.molgenis.data.Query.class);
    when(attributeRepository.findAll(findQueryCaptor.capture())).thenReturn(Stream.of(attribute));

    long total = 100L;
    ArgumentCaptor<org.molgenis.data.Query<Attribute>> countQueryCaptor =
        ArgumentCaptor.forClass(org.molgenis.data.Query.class);
    when(attributeRepository.count(countQueryCaptor.capture())).thenReturn(total);

    Sort sort = Sort.EMPTY_SORT;
    org.molgenis.data.Sort repositorySort = mock(org.molgenis.data.Sort.class);
    when(sortMapper.map(sort, entityType)).thenReturn(repositorySort);

    int size = 5;
    int number = 2;

    Attributes expectedAttributes =
        Attributes.builder()
            .setAttributes(ImmutableList.of(attribute))
            .setTotal((int) total)
            .build();

    String entityTypeId = "MyEntityTypeId";
    when(metadataService.hasEntityType(entityTypeId)).thenReturn(true);

    Attributes attributes =
        metadataApiService.findAttributes(entityTypeId, query, sort, size, number);
    org.molgenis.data.Query<Attribute> findQuery = findQueryCaptor.getValue();
    org.molgenis.data.Query<Attribute> countQuery = countQueryCaptor.getValue();
    assertAll(
        () -> assertEquals(expectedAttributes, attributes),
        () ->
            assertEquals(
                asList(
                    new QueryRule(
                        asList(
                            new QueryRule("id", QueryRule.Operator.EQUALS, "id0"),
                            new QueryRule(QueryRule.Operator.OR),
                            new QueryRule("id", QueryRule.Operator.EQUALS, "id1"))),
                    new QueryRule(QueryRule.Operator.AND),
                    new QueryRule("entity", QueryRule.Operator.EQUALS, entityTypeId)),
                findQuery.getRules()),
        () -> assertEquals(size * number, findQuery.getOffset()),
        () -> assertEquals(size, findQuery.getPageSize()),
        () -> assertEquals(repositorySort, findQuery.getSort()),
        () ->
            assertEquals(
                asList(
                    new QueryRule(
                        asList(
                            new QueryRule("id", QueryRule.Operator.EQUALS, "id0"),
                            new QueryRule(QueryRule.Operator.OR),
                            new QueryRule("id", QueryRule.Operator.EQUALS, "id1"))),
                    new QueryRule(QueryRule.Operator.AND),
                    new QueryRule("entity", QueryRule.Operator.EQUALS, entityTypeId)),
                countQuery.getRules()),
        () -> assertEquals(0, countQuery.getOffset()),
        () -> assertEquals(Integer.MAX_VALUE, countQuery.getPageSize()));
  }

  @Test
  void testFindAttribute() {
    String attributeId = "MyAttributeId";
    Attribute attribute = mock(Attribute.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getOwnAttributeById(attributeId)).thenReturn(attribute);

    String entityTypeId = "MyEntityTypeId";
    when(metadataService.getEntityType(entityTypeId)).thenReturn(Optional.of(entityType));

    assertEquals(attribute, metadataApiService.findAttribute(entityTypeId, attributeId));
  }

  @Test
  void testFindAttributeUnknownEntityType() {
    assertThrows(
        UnknownEntityTypeException.class,
        () -> metadataApiService.findAttribute("UnknownEntityTypeId", "MyAttributeId"));
  }

  @Test
  void testFindAttributeUnknownAttribute() {
    EntityType entityType = mock(EntityType.class);
    String entityTypeId = "MyEntityTypeId";
    when(metadataService.getEntityType(entityTypeId)).thenReturn(Optional.of(entityType));

    assertThrows(
        UnknownAttributeException.class,
        () -> metadataApiService.findAttribute(entityTypeId, "UnknownAttributeId"));
  }

  @Test
  void testCreateEntityType() {
    EntityType entityType = mock(EntityType.class);
    metadataApiService.createEntityType(entityType);
    verify(metadataService).addEntityType(entityType);
  }

  @Test
  void testFindAttributesUnknownEntityType() {
    String entityTypeId = "UnknownEntityType";
    assertThrows(
        UnknownEntityTypeException.class,
        () ->
            metadataApiService.findAttributes(
                entityTypeId, mock(Query.class), mock(Sort.class), 1, 1));
  }

  @Test
  void testUpdateEntityTypeAsync() {
    EntityType entityType = mock(EntityType.class);
    MetadataUpsertJobExecution metadataUpsertJobExecution = mock(MetadataUpsertJobExecution.class);
    when(metadataApiJobService.scheduleUpdate(entityType)).thenReturn(metadataUpsertJobExecution);
    assertEquals(metadataUpsertJobExecution, metadataApiService.updateEntityTypeAsync(entityType));
  }

  @Test
  void testDeleteAttribute() {
    String entityTypeId = "entityTypeId";
    EntityType entityType = mock(EntityType.class);
    when(metadataService.getEntityType(entityTypeId)).thenReturn(Optional.of(entityType));
    String attributeId = "attr1";
    Attribute attr = mock(Attribute.class);
    when(entityType.getOwnAttributeById(attributeId)).thenReturn(attr);

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

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteAttributesQuery() {
    String entityTypeId = "test_entity1";
    EntityType entityType = mock(EntityType.class);
    when(metadataService.getEntityType(entityTypeId)).thenReturn(Optional.of(entityType));
    Query query = Query.create("identifier", IN, asList("attr1", "attr2"));
    org.molgenis.data.Query<Attribute> dataServiceQuery = mock(org.molgenis.data.Query.class);
    when(dataServiceQuery.eq(AttributeMetadata.ENTITY, entityTypeId)).thenReturn(dataServiceQuery);
    Attribute attribute1 = mock(Attribute.class);
    Attribute attribute2 = mock(Attribute.class);
    when(dataServiceQuery.findAll()).thenReturn(Stream.of(attribute1, attribute2));
    Repository<Attribute> attributeRepository = mock(Repository.class);
    when(metadataService.getRepository(ATTRIBUTE_META_DATA, Attribute.class))
        .thenReturn(Optional.of(attributeRepository));
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
    when(metadataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class))
        .thenReturn(Optional.of(entityTypeRepository));
    when(queryMapper.map(query, entityTypeRepository)).thenReturn(dataServiceQuery);

    metadataApiService.deleteEntityTypesAsync(query);

    assertAll(() -> verify(metadataApiJobService).scheduleDelete(asList(entityType0, entityType1)));
  }
}
