package org.molgenis.api.data.v3;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.STRING;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Query.Operator;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.api.model.Sort.Order.Direction;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetadataAccessException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;

class DataServiceV3ImplTest extends AbstractMockitoTest {
  @Mock private MetaDataService metaDataService;
  @Mock private EntityManagerV3 entityManagerV3;
  @Mock private QueryV3Mapper queryMapperV3;
  @Mock private SortV3Mapper sortMapperV3;
  @Mock private FetchMapper fetchMapper;
  private DataServiceV3Impl dataServiceV3Impl;

  @BeforeEach
  void setUpBeforeMethod() {
    dataServiceV3Impl =
        new DataServiceV3Impl(
            metaDataService, entityManagerV3, queryMapperV3, sortMapperV3, fetchMapper);
  }

  @Test
  void testCreate() {
    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);

    when(repository.getEntityType()).thenReturn(entityType);
    when(entityManagerV3.create(entityType)).thenReturn(entity);
    when(metaDataService.getRepository("entityTypeId")).thenReturn(Optional.of(repository));

    dataServiceV3Impl.create("entityTypeId", Collections.singletonMap("attr", "value"));

    verify(entityManagerV3).populate(entityType, entity, Collections.singletonMap("attr", "value"));
    verify(repository).add(entity);
  }

  @Test
  void testCreateUnknownRepo() {
    when(metaDataService.getRepository("entityTypeId")).thenReturn(Optional.empty());
    assertThrows(
        UnknownRepositoryException.class,
        () -> dataServiceV3Impl.create("entityTypeId", Collections.singletonMap("attr", "value")));
  }

  @Test
  void testCreateMetadataNotCapable() {
    assertThrows(
        MetadataAccessException.class,
        () -> dataServiceV3Impl.create("sys_md_EntityType", emptyMap()));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFind() {
    String entityTypeId = "sys_md_EntityType";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getDataType()).thenReturn(STRING);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch().field("id");
    when(fetchMapper.toFetch(entityType, filter, expand)).thenReturn(fetch);

    Entity entity = mock(Entity.class);
    when(repository.findOneById(entityId, fetch)).thenReturn(entity);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    assertEquals(dataServiceV3Impl.find(entityTypeId, entityId, filter, expand), entity);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindExpand() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.FULL_SELECTION;

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getDataType()).thenReturn(STRING);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch().field("id").field("refAttr");
    when(fetchMapper.toFetch(entityType, filter, expand)).thenReturn(fetch);

    Entity entity = mock(Entity.class);
    when(repository.findOneById(entityId, fetch)).thenReturn(entity);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    assertEquals(dataServiceV3Impl.find(entityTypeId, entityId, filter, expand), entity);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindUnknownEntity() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getDataType()).thenReturn(STRING);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    assertThrows(
        UnknownEntityException.class,
        () -> dataServiceV3Impl.find(entityTypeId, entityId, filter, expand));
  }

  @Test
  void testFindUnknownRepository() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.empty());

    assertThrows(
        UnknownRepositoryException.class,
        () -> dataServiceV3Impl.find(entityTypeId, entityId, filter, expand));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindAll() {
    String entityTypeId = "MyEntityType";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    EntityType entityType = mock(EntityType.class);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);

    Entity entity1 = mock(Entity.class);
    Entity entity2 = mock(Entity.class);

    Sort sort = Sort.create("field", Direction.ASC);

    Fetch fetch = new Fetch().field("id", new Fetch().field("refAttr"));

    Query q = Query.builder().setOperator(Operator.MATCHES).setValue("value").build();
    org.molgenis.data.Query<Entity> findAllQuery = mock(org.molgenis.data.Query.class);
    org.molgenis.data.Sort dataSort = mock(org.molgenis.data.Sort.class);
    org.molgenis.data.Query<Entity> findQuery = new QueryImpl<>(findAllQuery);
    findQuery.fetch(fetch);
    findQuery.offset(10);
    findQuery.pageSize(10);
    findQuery.sort(dataSort);

    org.molgenis.data.Query<Entity> countQuery = new QueryImpl<>(findAllQuery);
    countQuery.offset(0);
    countQuery.pageSize(Integer.MAX_VALUE);

    when(repository.findAll(findQuery)).thenReturn(Stream.of(entity1, entity2));
    when(repository.count(countQuery)).thenReturn(100L);
    when(queryMapperV3.map(q, repository)).thenReturn(findAllQuery).thenReturn(countQuery);
    when(sortMapperV3.map(sort)).thenReturn(dataSort);

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    Entities actual = dataServiceV3Impl.findAll(entityTypeId, q, filter, expand, sort, 10, 1);

    assertEquals(
        actual, Entities.builder().setEntities(asList(entity1, entity2)).setTotal(100).build());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindField() {
    String entityTypeId = "MyEntityType";
    String refEntityTypeId = "refEntityType";
    String entityId = "MyEntity";
    String fieldId = "MyField";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute refIdAttribute = mock(Attribute.class, "mrefId");
    when(refIdAttribute.getName()).thenReturn("id");

    EntityType refEntityType = mock(EntityType.class, "refEntityType");
    when(refEntityType.getId()).thenReturn(refEntityTypeId);
    when(refEntityType.getIdAttribute()).thenReturn(refIdAttribute);

    Attribute idAttribute = mock(Attribute.class, "id");
    when(idAttribute.getDataType()).thenReturn(STRING);

    Attribute mrefAttribute = mock(Attribute.class, "mref");
    when(mrefAttribute.getName()).thenReturn("MyField");
    when(mrefAttribute.getDataType()).thenReturn(MREF);
    when(mrefAttribute.getRefEntity()).thenReturn(refEntityType);

    EntityType entityType = mock(EntityType.class, "entityType");
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(entityType.getAttribute(fieldId)).thenReturn(mrefAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch().field("MyField", new Fetch().field("id"));

    Entity entity = mock(Entity.class);
    doReturn(entity).when(repository).findOneById(entityId, fetch);

    Repository<Entity> refRepository = mock(Repository.class);
    when(refRepository.getEntityType()).thenReturn(refEntityType);

    Entity entity1 = mock(Entity.class);
    doReturn("entity1").when(entity1).getIdValue();
    Entity entity2 = mock(Entity.class);
    doReturn("entity2").when(entity2).getIdValue();
    Entity entity3 = mock(Entity.class);
    doReturn("entity3").when(entity3).getIdValue();

    doReturn(Arrays.asList(entity1, entity2, entity3)).when(entity).getEntities("MyField");

    Sort sort = Sort.create("field", Direction.ASC);
    Query q = Query.builder().setOperator(Operator.MATCHES).setValue("value").build();
    org.molgenis.data.Query<Entity> findAllQuery = mock(org.molgenis.data.Query.class);
    org.molgenis.data.Sort dataSort = mock(org.molgenis.data.Sort.class);
    org.molgenis.data.Query<Entity> findQuery = new QueryImpl<>(findAllQuery);
    findQuery.in("id", Arrays.asList("entity1", "entity2", "entity3"));
    findQuery.fetch(fetch);
    findQuery.offset(10);
    findQuery.pageSize(10);
    findQuery.sort(dataSort);

    org.molgenis.data.Query<Entity> countQuery = new QueryImpl<>(findAllQuery);
    countQuery.offset(0);
    countQuery.pageSize(Integer.MAX_VALUE);

    when(queryMapperV3.map(q, refRepository)).thenReturn(findAllQuery).thenReturn(countQuery);
    countQuery.in("id", Arrays.asList("entity1", "entity2", "entity3"));
    when(refRepository.count(countQuery)).thenReturn(100L);
    when(refRepository.findAll(findQuery)).thenReturn(Stream.of(entity1, entity2));
    when(sortMapperV3.map(sort)).thenReturn(dataSort);

    doReturn(Optional.of(repository)).when(metaDataService).getRepository(entityTypeId);
    doReturn(Optional.of(refRepository)).when(metaDataService).getRepository(refEntityTypeId);

    Entities actual =
        dataServiceV3Impl.findSubresources(
            entityTypeId, entityId, fieldId, q, filter, expand, sort, 10, 1);

    assertEquals(
        actual, Entities.builder().setEntities(asList(entity1, entity2)).setTotal(100).build());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindFieldWithQuery() {
    String entityTypeId = "MyEntityType";
    String refEntityTypeId = "refEntityType";
    String entityId = "MyEntity";
    String fieldId = "MyField";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute refIdAttribute = mock(Attribute.class, "mrefId");
    when(refIdAttribute.getName()).thenReturn("id");

    EntityType refEntityType = mock(EntityType.class, "refEntityType");
    when(refEntityType.getId()).thenReturn(refEntityTypeId);
    when(refEntityType.getIdAttribute()).thenReturn(refIdAttribute);

    Attribute idAttribute = mock(Attribute.class, "id");
    when(idAttribute.getDataType()).thenReturn(STRING);

    Attribute mrefAttribute = mock(Attribute.class, "mref");
    when(mrefAttribute.getName()).thenReturn("MyField");
    when(mrefAttribute.getDataType()).thenReturn(MREF);
    when(mrefAttribute.getRefEntity()).thenReturn(refEntityType);

    EntityType entityType = mock(EntityType.class, "entityType");
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(entityType.getAttribute(fieldId)).thenReturn(mrefAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch().field("MyField", new Fetch().field("id"));

    Entity entity = mock(Entity.class);
    doReturn(entity).when(repository).findOneById(entityId, fetch);

    Repository<Entity> refRepository = mock(Repository.class);
    when(refRepository.getEntityType()).thenReturn(refEntityType);

    Entity entity1 = mock(Entity.class);
    doReturn("entity1").when(entity1).getIdValue();
    Entity entity2 = mock(Entity.class);
    doReturn("entity2").when(entity2).getIdValue();
    Entity entity3 = mock(Entity.class);
    doReturn("entity3").when(entity3).getIdValue();

    doReturn(Arrays.asList(entity1, entity2, entity3)).when(entity).getEntities("MyField");

    Sort sort = Sort.create("field", Direction.ASC);
    Query q = Query.builder().setOperator(Operator.MATCHES).setValue("value").build();
    org.molgenis.data.Query<Entity> findAllQuery = new QueryImpl<>();
    findAllQuery.eq("field1", "value1").or().eq("field2", "value2");
    org.molgenis.data.Sort dataSort = mock(org.molgenis.data.Sort.class);
    org.molgenis.data.Query<Entity> findQuery = new QueryImpl<>();
    findQuery.nest().eq("field1", "value1").or().eq("field2", "value2").unnest().and();
    findQuery.in("id", Arrays.asList("entity1", "entity2", "entity3"));
    findQuery.fetch(fetch);
    findQuery.offset(10);
    findQuery.pageSize(10);
    findQuery.sort(dataSort);

    org.molgenis.data.Query<Entity> countQuery = new QueryImpl<>();
    countQuery.nest().eq("field1", "value1").or().eq("field2", "value2").unnest().and();
    countQuery.offset(0);
    countQuery.pageSize(Integer.MAX_VALUE);

    when(queryMapperV3.map(q, refRepository)).thenReturn(findAllQuery).thenReturn(countQuery);
    countQuery.in("id", Arrays.asList("entity1", "entity2", "entity3"));
    when(refRepository.count(countQuery)).thenReturn(100L);
    when(refRepository.findAll(findQuery)).thenReturn(Stream.of(entity1, entity2));
    when(sortMapperV3.map(sort)).thenReturn(dataSort);

    doReturn(Optional.of(repository)).when(metaDataService).getRepository(entityTypeId);
    doReturn(Optional.of(refRepository)).when(metaDataService).getRepository(refEntityTypeId);

    Entities actual =
        dataServiceV3Impl.findSubresources(
            entityTypeId, entityId, fieldId, q, filter, expand, sort, 10, 1);

    assertEquals(
        actual, Entities.builder().setEntities(asList(entity1, entity2)).setTotal(100).build());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindFieldNoEntities() {
    String entityTypeId = "MyEntityType";
    String refEntityTypeId = "refEntityType";
    String entityId = "MyEntity";
    String fieldId = "MyField";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute refIdAttribute = mock(Attribute.class, "mrefId");
    when(refIdAttribute.getName()).thenReturn("id");

    EntityType refEntityType = mock(EntityType.class, "refEntityType");
    when(refEntityType.getId()).thenReturn(refEntityTypeId);
    when(refEntityType.getIdAttribute()).thenReturn(refIdAttribute);

    Attribute idAttribute = mock(Attribute.class, "id");
    when(idAttribute.getDataType()).thenReturn(STRING);

    Attribute mrefAttribute = mock(Attribute.class, "mref");
    when(mrefAttribute.getName()).thenReturn("MyField");
    when(mrefAttribute.getDataType()).thenReturn(MREF);
    when(mrefAttribute.getRefEntity()).thenReturn(refEntityType);

    EntityType entityType = mock(EntityType.class, "entityType");
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(entityType.getAttribute(fieldId)).thenReturn(mrefAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch().field("MyField", new Fetch().field("id"));

    Entity entity = mock(Entity.class);
    doReturn(entity).when(repository).findOneById(entityId, fetch);

    Repository<Entity> refRepository = mock(Repository.class);

    Sort sort = Sort.create("field", Direction.ASC);
    Query q = Query.builder().setOperator(Operator.MATCHES).setValue("value").build();
    org.molgenis.data.Query<Entity> findAllQuery = mock(org.molgenis.data.Query.class);
    org.molgenis.data.Sort dataSort = mock(org.molgenis.data.Sort.class);
    org.molgenis.data.Query<Entity> findQuery = new QueryImpl<>(findAllQuery);
    findQuery.fetch(fetch);
    findQuery.offset(10);
    findQuery.pageSize(10);
    findQuery.sort(dataSort);

    org.molgenis.data.Query<Entity> countQuery = new QueryImpl<>(findAllQuery);
    countQuery.offset(0);
    countQuery.pageSize(Integer.MAX_VALUE);

    when(queryMapperV3.map(q, refRepository)).thenReturn(findAllQuery).thenReturn(countQuery);

    doReturn(Optional.of(repository)).when(metaDataService).getRepository(entityTypeId);
    doReturn(Optional.of(refRepository)).when(metaDataService).getRepository(refEntityTypeId);

    Entities actual =
        dataServiceV3Impl.findSubresources(
            entityTypeId, entityId, fieldId, q, filter, expand, sort, 10, 1);

    assertEquals(
        actual, Entities.builder().setEntities(Collections.emptyList()).setTotal(0).build());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindFieldUnknownEntity() {
    String entityTypeId = "MyEntityType";
    String refEntityTypeId = "refEntityType";
    String entityId = "MyEntity";
    String fieldId = "MyField";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute refIdAttribute = mock(Attribute.class, "mrefId");
    when(refIdAttribute.getName()).thenReturn("id");

    EntityType refEntityType = mock(EntityType.class, "refEntityType");
    when(refEntityType.getId()).thenReturn(refEntityTypeId);
    when(refEntityType.getIdAttribute()).thenReturn(refIdAttribute);

    Attribute idAttribute = mock(Attribute.class, "id");
    when(idAttribute.getDataType()).thenReturn(STRING);

    Attribute mrefAttribute = mock(Attribute.class, "mref");
    when(mrefAttribute.getName()).thenReturn("MyField");
    when(mrefAttribute.getDataType()).thenReturn(MREF);
    when(mrefAttribute.getRefEntity()).thenReturn(refEntityType);

    EntityType entityType = mock(EntityType.class, "entityType");
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(entityType.getAttribute(fieldId)).thenReturn(mrefAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch().field("MyField", new Fetch().field("id"));

    doReturn(null).when(repository).findOneById(entityId, fetch);

    Sort sort = Sort.create("field", Direction.ASC);
    Query q = Query.builder().setOperator(Operator.MATCHES).setValue("value").build();
    org.molgenis.data.Query<Entity> findAllQuery = mock(org.molgenis.data.Query.class);
    org.molgenis.data.Sort dataSort = mock(org.molgenis.data.Sort.class);
    org.molgenis.data.Query<Entity> findQuery = new QueryImpl<>(findAllQuery);
    findQuery.fetch(fetch);
    findQuery.offset(10);
    findQuery.pageSize(10);
    findQuery.sort(dataSort);

    org.molgenis.data.Query<Entity> countQuery = new QueryImpl<>(findAllQuery);
    countQuery.offset(0);
    countQuery.pageSize(Integer.MAX_VALUE);

    doReturn(Optional.of(repository)).when(metaDataService).getRepository(entityTypeId);

    assertThrows(
        UnknownEntityException.class,
        () ->
            dataServiceV3Impl.findSubresources(
                entityTypeId, entityId, fieldId, q, filter, expand, sort, 10, 1));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindFieldUnsupportedAttributeType() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyEntity";
    String fieldId = "MyField";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute idAttribute = mock(Attribute.class, "id");
    when(idAttribute.getDataType()).thenReturn(STRING);

    Attribute mrefAttribute = mock(Attribute.class, "mref");
    when(mrefAttribute.getDataType()).thenReturn(STRING);

    EntityType entityType = mock(EntityType.class, "entityType");
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(entityType.getAttribute(fieldId)).thenReturn(mrefAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch().field("MyField", new Fetch().field("id"));

    Sort sort = Sort.create("field", Direction.ASC);
    Query q = Query.builder().setOperator(Operator.MATCHES).setValue("value").build();
    org.molgenis.data.Query<Entity> findAllQuery = mock(org.molgenis.data.Query.class);
    org.molgenis.data.Sort dataSort = mock(org.molgenis.data.Sort.class);
    org.molgenis.data.Query<Entity> findQuery = new QueryImpl<>(findAllQuery);
    findQuery.fetch(fetch);
    findQuery.offset(10);
    findQuery.pageSize(10);
    findQuery.sort(dataSort);

    org.molgenis.data.Query<Entity> countQuery = new QueryImpl<>(findAllQuery);
    countQuery.offset(0);
    countQuery.pageSize(Integer.MAX_VALUE);

    doReturn(Optional.of(repository)).when(metaDataService).getRepository(entityTypeId);

    assertThrows(
        UnsupportedAttributeTypeException.class,
        () ->
            dataServiceV3Impl.findSubresources(
                entityTypeId, entityId, fieldId, q, filter, expand, sort, 10, 1));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindFieldUnknownAttributeException() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyEntity";
    String fieldId = "MyField";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    Attribute idAttribute = mock(Attribute.class, "id");
    when(idAttribute.getDataType()).thenReturn(STRING);

    EntityType entityType = mock(EntityType.class, "entityType");
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(entityType.getAttribute(fieldId)).thenReturn(null);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    Fetch fetch = new Fetch().field("MyField", new Fetch().field("id"));

    Sort sort = Sort.create("field", Direction.ASC);
    Query q = Query.builder().setOperator(Operator.MATCHES).setValue("value").build();
    org.molgenis.data.Query<Entity> findAllQuery = mock(org.molgenis.data.Query.class);
    org.molgenis.data.Sort dataSort = mock(org.molgenis.data.Sort.class);
    org.molgenis.data.Query<Entity> findQuery = new QueryImpl<>(findAllQuery);
    findQuery.fetch(fetch);
    findQuery.offset(10);
    findQuery.pageSize(10);
    findQuery.sort(dataSort);

    org.molgenis.data.Query<Entity> countQuery = new QueryImpl<>(findAllQuery);
    countQuery.offset(0);
    countQuery.pageSize(Integer.MAX_VALUE);

    doReturn(Optional.of(repository)).when(metaDataService).getRepository(entityTypeId);

    assertThrows(
        UnknownAttributeException.class,
        () ->
            dataServiceV3Impl.findSubresources(
                entityTypeId, entityId, fieldId, q, filter, expand, sort, 10, 1));
  }

  @Test
  void testFindAllUnknownRepositoryUnknownEntity() {
    String entityTypeId = "MyEntityType";
    Selection filter = Selection.FULL_SELECTION;
    Selection expand = Selection.EMPTY_SELECTION;

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.empty());

    assertThrows(
        UnknownRepositoryException.class,
        () -> dataServiceV3Impl.findAll(entityTypeId, null, filter, expand, Sort.EMPTY_SORT, 1, 1));
  }

  @Test
  void testUpdate() {
    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute idAttribute = mock(Attribute.class);

    when(repository.getEntityType()).thenReturn(entityType);
    when(metaDataService.getRepository("entityTypeId")).thenReturn(Optional.of(repository));

    when(repository.getEntityType()).thenReturn(entityType);
    when(idAttribute.getDataType()).thenReturn(STRING);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    when(entityManagerV3.create(entityType)).thenReturn(entity);

    dataServiceV3Impl.update("entityTypeId", "entityId", Collections.singletonMap("attr", "value"));

    verify(entityManagerV3).populate(entityType, entity, Collections.singletonMap("attr", "value"));
    verify(entity).setIdValue("entityId");
    verify(repository).update(entity);
  }

  @Test
  void testUpdateMetadataNotCapable() {
    assertThrows(
        MetadataAccessException.class,
        () -> dataServiceV3Impl.update("sys_md_Attribute", "myAttributeId", emptyMap()));
  }

  @Test
  void testUpdatePartially() {
    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute idAttribute = mock(Attribute.class);

    when(repository.getEntityType()).thenReturn(entityType);
    when(metaDataService.getRepository("entityTypeId")).thenReturn(Optional.of(repository));

    when(repository.getEntityType()).thenReturn(entityType);
    when(idAttribute.getDataType()).thenReturn(STRING);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    when(repository.findOneById("entityId")).thenReturn(entity);

    dataServiceV3Impl.updatePartial(
        "entityTypeId", "entityId", Collections.singletonMap("attr", "value"));

    verify(entityManagerV3).populate(entityType, entity, Collections.singletonMap("attr", "value"));
    verify(entity).setIdValue("entityId");
    verify(repository).update(entity);
  }

  @Test
  void testUpdatePartiallyUnknownEntity() {
    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);
    EntityType entityType = mock(EntityType.class);
    Attribute idAttribute = mock(Attribute.class);

    when(repository.getEntityType()).thenReturn(entityType);
    when(metaDataService.getRepository("entityTypeId")).thenReturn(Optional.of(repository));

    when(repository.getEntityType()).thenReturn(entityType);
    when(idAttribute.getDataType()).thenReturn(STRING);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    when(repository.findOneById("entityId")).thenReturn(null);

    assertThrows(
        UnknownEntityException.class,
        () ->
            dataServiceV3Impl.updatePartial(
                "entityTypeId", "entityId", Collections.singletonMap("attr", "value")));
  }

  @Test
  void testUpdatePartiallyMetadataNotCapable() {
    assertThrows(
        MetadataAccessException.class,
        () -> dataServiceV3Impl.updatePartial("sys_md_EntityType", "myEntityTypeId", emptyMap()));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDelete() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getDataType()).thenReturn(STRING);
    when(idAttribute.getName()).thenReturn("id");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    Entity entity = mock(Entity.class);
    when(repository.findOneById(entityId, new Fetch().field("id"))).thenReturn(entity);
    dataServiceV3Impl.delete(entityTypeId, entityId);

    verify(repository).deleteById(entityId);
  }

  @Test
  void testDeleteUnknownEntityType() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";
    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.empty());

    assertThrows(
        UnknownRepositoryException.class, () -> dataServiceV3Impl.delete(entityTypeId, entityId));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteUnknownEntity() {
    String entityTypeId = "MyEntityType";
    String entityId = "MyId";

    Attribute idAttribute = mock(Attribute.class);
    when(idAttribute.getDataType()).thenReturn(STRING);
    when(idAttribute.getName()).thenReturn("id");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);

    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);
    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    assertThrows(
        UnknownEntityException.class, () -> dataServiceV3Impl.delete(entityTypeId, entityId));
  }

  @Test
  void testDeleteMetadataNotCapable() {
    assertThrows(
        MetadataAccessException.class,
        () -> dataServiceV3Impl.delete("sys_md_Attribute", "myAttributeId"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteAll() {
    String entityTypeId = "MyEntityType";

    Repository<Entity> repository = mock(Repository.class);
    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.of(repository));

    Entity entity1 = mock(Entity.class);
    Entity entity2 = mock(Entity.class);

    Query query = Query.builder().setOperator(Operator.MATCHES).setValue("value").build();
    org.molgenis.data.Query<Entity> molgenisQuery = mock(org.molgenis.data.Query.class);
    when(queryMapperV3.map(query, repository)).thenReturn(molgenisQuery);

    dataServiceV3Impl.deleteAll(entityTypeId, query);

    ArgumentCaptor<Stream> captor = ArgumentCaptor.forClass(Stream.class);
    verify(repository).delete(captor.capture());
    Stream<Entity> entityStream = captor.getValue();
    entityStream.collect(Collectors.toList()).containsAll(Arrays.asList(entity1, entity2));
  }

  @Test
  void testDeleteAllUnknownEntityType() {
    String entityTypeId = "MyEntityType";

    when(metaDataService.getRepository(entityTypeId)).thenReturn(Optional.empty());

    assertThrows(
        UnknownRepositoryException.class, () -> dataServiceV3Impl.deleteAll(entityTypeId, null));
  }

  @Test
  void testDeleteAllMetadataNotCapable() {
    assertThrows(
        MetadataAccessException.class,
        () -> dataServiceV3Impl.deleteAll("sys_md_Attribute", mock(Query.class)));
  }
}
