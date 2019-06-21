package org.molgenis.api.data.v3;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.api.data.v3.Selection.EMPTY_SELECTION;
import static org.molgenis.api.data.v3.Selection.FULL_SELECTION;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL_MREF;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.EMAIL;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.HTML;
import static org.molgenis.data.meta.AttributeType.HYPERLINK;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.SCRIPT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.mockito.quality.Strictness;
import org.molgenis.api.data.v3.EntityCollection.Page;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EntityMapperImplTest extends AbstractMockitoTest {
  private EntityMapperImpl entityMapper;

  @SuppressWarnings("deprecation")
  public EntityMapperImplTest() {
    super(Strictness.LENIENT); // due to generic mocking code
  }

  @BeforeMethod
  public void setUpBeforeMethod() {
    entityMapper = new EntityMapperImpl();
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(new MockHttpServletRequest()));
  }

  @Test
  public void testMapEntityBool() throws URISyntaxException {
    Entity entity = createMockEntity(BOOL);
    doReturn(true).when(entity).getBoolean("attr");

    URI self = new URI("http://localhost/api/entity/EntityType/id0");
    EntityResponse expectedEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, self, null))
            .setData(singletonMap("attr", true))
            .build();

    assertEquals(entityMapper.map(entity, FULL_SELECTION, EMPTY_SELECTION), expectedEntityResponse);
  }

  @DataProvider(name = "testMapEntityRefTypeProvider")
  public Iterator<Object[]> refTypeProvider() {
    List<Object[]> dataList = new ArrayList<>();
    dataList.add(new Object[] {CATEGORICAL});
    dataList.add(new Object[] {FILE});
    dataList.add(new Object[] {XREF});
    return dataList.iterator();
  }

  @Test(dataProvider = "testMapEntityRefTypeProvider")
  public void testMapEntityRefType(AttributeType attributeType) throws URISyntaxException {
    Entity refEntity = createMockEntity(STRING, "RefEntityType", "refId0");
    Entity entity = createMockEntity(attributeType);
    doReturn(refEntity).when(entity).getEntity("attr");

    URI refSelf = new URI("http://localhost/api/entity/RefEntityType/refId0");
    EntityResponse expectedRefEntityResponse =
        EntityResponse.builder().setLinks(LinksResponse.create(null, refSelf, null)).build();

    URI self = new URI("http://localhost/api/entity/EntityType/id0");
    EntityResponse expectedEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, self, null))
            .setData(singletonMap("attr", expectedRefEntityResponse))
            .build();

    assertEquals(entityMapper.map(entity, FULL_SELECTION, EMPTY_SELECTION), expectedEntityResponse);
  }

  @DataProvider(name = "testMapEntityRefsTypeProvider")
  public Iterator<Object[]> refsTypeProvider() {
    List<Object[]> dataList = new ArrayList<>();
    dataList.add(new Object[] {CATEGORICAL_MREF});
    dataList.add(new Object[] {MREF});
    dataList.add(new Object[] {ONE_TO_MANY});
    return dataList.iterator();
  }

  @Test(dataProvider = "testMapEntityRefsTypeProvider")
  public void testMapEntityRefsType(AttributeType attributeType) throws URISyntaxException {
    Entity entity = createMockEntity(attributeType);
    doReturn(emptyList()).when(entity).getEntities("attr");

    URI refSelf = new URI("http://localhost/api/entity/EntityType/id0/attr");
    EntitiesResponse expectedRefEntitiesResponse =
        EntitiesResponse.builder().setLinks(LinksResponse.create(null, refSelf, null)).build();

    URI self = new URI("http://localhost/api/entity/EntityType/id0");
    EntityResponse expectedEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, self, null))
            .setData(singletonMap("attr", expectedRefEntitiesResponse))
            .build();

    assertEquals(entityMapper.map(entity, FULL_SELECTION, EMPTY_SELECTION), expectedEntityResponse);
  }

  @Test
  public void testMapEntityDate() throws URISyntaxException {
    Entity entity = createMockEntity(DATE);
    doReturn(LocalDate.of(2019, 4, 30)).when(entity).getLocalDate("attr");

    URI self = new URI("http://localhost/api/entity/EntityType/id0");
    EntityResponse expectedEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, self, null))
            .setData(singletonMap("attr", LocalDate.of(2019, 4, 30)))
            .build();

    assertEquals(expectedEntityResponse, entityMapper.map(entity, FULL_SELECTION, EMPTY_SELECTION));
  }

  @Test
  public void testMapEntityDateTime() throws URISyntaxException {
    Entity entity = createMockEntity(DATE_TIME);
    doReturn(Instant.ofEpochMilli(1561010330984L)).when(entity).getInstant("attr");

    URI self = new URI("http://localhost/api/entity/EntityType/id0");
    EntityResponse expectedEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, self, null))
            .setData(singletonMap("attr", Instant.ofEpochMilli(1561010330984L)))
            .build();

    assertEquals(entityMapper.map(entity, FULL_SELECTION, EMPTY_SELECTION), expectedEntityResponse);
  }

  @Test
  public void testMapEntityDecimal() throws URISyntaxException {
    Entity entity = createMockEntity(DECIMAL);
    doReturn(3.14).when(entity).getDouble("attr");

    URI self = new URI("http://localhost/api/entity/EntityType/id0");
    EntityResponse expectedEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, self, null))
            .setData(singletonMap("attr", 3.14))
            .build();

    assertEquals(entityMapper.map(entity, FULL_SELECTION, EMPTY_SELECTION), expectedEntityResponse);
  }

  @DataProvider(name = "testMapEntityStringTypeProvider")
  public Iterator<Object[]> stringTypeProvider() {
    List<Object[]> dataList = new ArrayList<>();
    dataList.add(new Object[] {EMAIL});
    dataList.add(new Object[] {ENUM});
    dataList.add(new Object[] {HTML});
    dataList.add(new Object[] {HYPERLINK});
    dataList.add(new Object[] {SCRIPT});
    dataList.add(new Object[] {STRING});
    dataList.add(new Object[] {TEXT});
    return dataList.iterator();
  }

  @Test(dataProvider = "testMapEntityStringTypeProvider")
  public void testMapEntityStringType(AttributeType attributeType) throws URISyntaxException {
    Entity entity = createMockEntity(attributeType);
    doReturn("string").when(entity).getString("attr");

    URI self = new URI("http://localhost/api/entity/EntityType/id0");
    EntityResponse expectedEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, self, null))
            .setData(singletonMap("attr", "string"))
            .build();

    assertEquals(entityMapper.map(entity, FULL_SELECTION, EMPTY_SELECTION), expectedEntityResponse);
  }

  @Test
  public void testMapEntityInt() throws URISyntaxException {
    Entity entity = createMockEntity(INT);
    doReturn(123).when(entity).getInt("attr");

    URI self = new URI("http://localhost/api/entity/EntityType/id0");
    EntityResponse expectedEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, self, null))
            .setData(singletonMap("attr", 123))
            .build();

    assertEquals(entityMapper.map(entity, FULL_SELECTION, EMPTY_SELECTION), expectedEntityResponse);
  }

  @Test
  public void testMapEntityLong() throws URISyntaxException {
    Entity entity = createMockEntity(LONG);
    doReturn(Long.MAX_VALUE).when(entity).getLong("attr");

    URI self = new URI("http://localhost/api/entity/EntityType/id0");
    EntityResponse expectedEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, self, null))
            .setData(singletonMap("attr", Long.MAX_VALUE))
            .build();

    assertEquals(entityMapper.map(entity, FULL_SELECTION, EMPTY_SELECTION), expectedEntityResponse);
  }

  @Test
  public void testMapEntityFilter() throws URISyntaxException {
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("EntityType").getMock();
    Attribute attribute0 = when(mock(Attribute.class).getName()).thenReturn("attr0").getMock();
    doReturn(STRING).when(attribute0).getDataType();
    Attribute attribute1 = when(mock(Attribute.class).getName()).thenReturn("attr1").getMock();
    doReturn(STRING).when(attribute1).getDataType();
    doReturn(asList(attribute0, attribute1)).when(entityType).getAtomicAttributes();

    Entity entity = mock(Entity.class);
    doReturn("id0").when(entity).getIdValue();
    doReturn(entityType).when(entity).getEntityType();
    doReturn("string0").when(entity).getString("attr0");
    doReturn("string1").when(entity).getString(("attr1"));

    URI self = new URI("http://localhost/api/entity/EntityType/id0");
    EntityResponse expectedEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, self, null))
            .setData(singletonMap("attr1", "string1"))
            .build();

    assertEquals(
        entityMapper.map(entity, new Selection(singletonMap("attr1", null)), EMPTY_SELECTION),
        expectedEntityResponse);
  }

  @Test
  public void testMapEntityExpandXref() throws URISyntaxException {
    Entity refRefEntity = createMockEntity(XREF, "RefRefEntityType", "refRefId0");
    Entity refEntity = createMockEntity(XREF, "RefEntityType", "refId0");
    Entity entity = createMockEntity(XREF);
    doReturn(refRefEntity).when(refEntity).getEntity("attr");
    doReturn(refEntity).when(entity).getEntity("attr");

    URI refRefSelf = new URI("http://localhost/api/entity/RefRefEntityType/refRefId0");
    EntityResponse expectedRefRefEntityResponse =
        EntityResponse.builder().setLinks(LinksResponse.create(null, refRefSelf, null)).build();

    URI refSelf = new URI("http://localhost/api/entity/RefEntityType/refId0");
    EntityResponse expectedRefEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, refSelf, null))
            .setData(singletonMap("attr", expectedRefRefEntityResponse))
            .build();

    URI self = new URI("http://localhost/api/entity/EntityType/id0");
    EntityResponse expectedEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, self, null))
            .setData(singletonMap("attr", expectedRefEntityResponse))
            .build();

    assertEquals(
        entityMapper.map(entity, FULL_SELECTION, new Selection(singletonMap("attr", null))),
        expectedEntityResponse);
  }

  @Test
  public void testMapEntityRefNull() throws URISyntaxException {
    Entity entity = createMockEntity(XREF);
    doReturn(null).when(entity).getEntity("attr");

    URI entitySelf = new URI("http://localhost/api/entity/EntityType/id0");
    EntityResponse expectedEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, entitySelf, null))
            .setData(singletonMap("attr", null))
            .build();

    URI entitiesSelf = new URI("http://localhost/api/entity/EntityType");
    EntitiesResponse expectedEntitiesResponse =
        EntitiesResponse.builder()
            .setLinks(LinksResponse.create(null, entitiesSelf, null))
            .setItems(singletonList(expectedEntityResponse))
            .build();

    EntityCollection entityCollection =
        EntityCollection.builder()
            .setEntityTypeId("EntityType")
            .setEntities(singletonList(entity))
            .build();
    assertEquals(
        entityMapper.map(entityCollection, FULL_SELECTION, FULL_SELECTION),
        expectedEntitiesResponse);
  }

  @Test
  public void testMapEntityCollection() throws URISyntaxException {
    Entity entity = createMockEntity(STRING);
    doReturn("string").when(entity).getString("attr");

    EntityCollection entityCollection =
        EntityCollection.builder()
            .setEntityTypeId("EntityType")
            .setEntities(singletonList(entity))
            .setPage(Page.builder().setOffset(0).setPageSize(1).setTotal(2).build())
            .build();

    URI self = new URI("http://localhost/api/entity/EntityType/id0");
    EntityResponse expectedEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, self, null))
            .setData(singletonMap("attr", "string"))
            .build();

    URI entitiesSelf = new URI("http://localhost/api/entity/EntityType");
    EntitiesResponse expectedEntitiesResponse =
        EntitiesResponse.builder()
            .setLinks(LinksResponse.create(null, entitiesSelf, null))
            .setItems(singletonList(expectedEntityResponse))
            .setPage(PageResponse.create(1, 2, 2, 0))
            .build();
    assertEquals(
        entityMapper.map(entityCollection, FULL_SELECTION, EMPTY_SELECTION),
        expectedEntitiesResponse);
  }

  @Test
  public void testMapEntityCollectionExpand() throws URISyntaxException {
    Entity refEntity = createMockEntity(STRING, "RefEntityType", "refId0");
    Entity entity = createMockEntity(MREF);
    doReturn("refString").when(refEntity).getString("attr");
    doReturn(singletonList(refEntity)).when(entity).getEntities("attr");

    EntityCollection entityCollection =
        EntityCollection.builder()
            .setEntityTypeId("EntityType")
            .setEntities(singletonList(entity))
            .setPage(Page.builder().setOffset(0).setPageSize(1).setTotal(2).build())
            .build();

    URI refSelf = new URI("http://localhost/api/entity/RefEntityType/refId0");
    EntityResponse expectedRefEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, refSelf, null))
            .setData(singletonMap("attr", "refString"))
            .build();

    URI entitiesRefSelf = new URI("http://localhost/api/entity/EntityType/id0/attr");
    EntitiesResponse expectedRefEntitiesResponse =
        EntitiesResponse.builder()
            .setLinks(LinksResponse.create(null, entitiesRefSelf, null))
            .setItems(singletonList(expectedRefEntityResponse))
            .build();

    URI self = new URI("http://localhost/api/entity/EntityType/id0");
    EntityResponse expectedEntityResponse =
        EntityResponse.builder()
            .setLinks(LinksResponse.create(null, self, null))
            .setData(singletonMap("attr", expectedRefEntitiesResponse))
            .build();

    URI entitiesSelf = new URI("http://localhost/api/entity/EntityType");
    EntitiesResponse expectedEntitiesResponse =
        EntitiesResponse.builder()
            .setLinks(LinksResponse.create(null, entitiesSelf, null))
            .setItems(singletonList(expectedEntityResponse))
            .setPage(PageResponse.create(1, 2, 2, 0))
            .build();
    assertEquals(
        entityMapper.map(entityCollection, FULL_SELECTION, FULL_SELECTION),
        expectedEntitiesResponse);
  }

  private Entity createMockEntity(AttributeType attributeType) {
    return createMockEntity(attributeType, "EntityType", "id0");
  }

  private Entity createMockEntity(
      AttributeType attributeType, String entityTypeId, String entityId) {
    EntityType entityType = createMockEntityType(attributeType, entityTypeId);

    Entity entity = mock(Entity.class);
    doReturn(entityId).when(entity).getIdValue();
    doReturn(entityType).when(entity).getEntityType();
    return entity;
  }

  private EntityType createMockEntityType(AttributeType attributeType, String entityTypeId) {
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Attribute attribute = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
    doReturn(attributeType).when(attribute).getDataType();
    doReturn(singletonList(attribute)).when(entityType).getAtomicAttributes();

    EntityType refEntityType =
        when(mock(EntityType.class).getId()).thenReturn("RefEntityType").getMock();
    when(attribute.getRefEntity()).thenReturn(refEntityType);
    return entityType;
  }
}
