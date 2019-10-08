package org.molgenis.data.cache.utils;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.EntityTestHarness.ATTR_BOOL;
import static org.molgenis.data.EntityTestHarness.ATTR_CATEGORICAL;
import static org.molgenis.data.EntityTestHarness.ATTR_CATEGORICAL_MREF;
import static org.molgenis.data.EntityTestHarness.ATTR_COMPOUND_CHILD_INT;
import static org.molgenis.data.EntityTestHarness.ATTR_DATE;
import static org.molgenis.data.EntityTestHarness.ATTR_DATETIME;
import static org.molgenis.data.EntityTestHarness.ATTR_DECIMAL;
import static org.molgenis.data.EntityTestHarness.ATTR_EMAIL;
import static org.molgenis.data.EntityTestHarness.ATTR_ENUM;
import static org.molgenis.data.EntityTestHarness.ATTR_HTML;
import static org.molgenis.data.EntityTestHarness.ATTR_HYPERLINK;
import static org.molgenis.data.EntityTestHarness.ATTR_ID;
import static org.molgenis.data.EntityTestHarness.ATTR_INT;
import static org.molgenis.data.EntityTestHarness.ATTR_LONG;
import static org.molgenis.data.EntityTestHarness.ATTR_MREF;
import static org.molgenis.data.EntityTestHarness.ATTR_SCRIPT;
import static org.molgenis.data.EntityTestHarness.ATTR_STRING;
import static org.molgenis.data.EntityTestHarness.ATTR_XREF;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.XREF;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.Fetch;
import org.molgenis.data.TestHarnessConfig;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.EntityWithComputedAttributes;
import org.molgenis.data.support.PartialEntity;
import org.molgenis.data.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@MockitoSettings(strictness = Strictness.LENIENT)
@ContextConfiguration(classes = {TestHarnessConfig.class})
class EntityHydrationTest extends AbstractMolgenisSpringTest {
  @Autowired private EntityTestHarness entityTestHarness;

  private EntityType entityType;
  private Entity hydratedEntity;
  private Map<String, Object> dehydratedEntity;
  private EntityHydration entityHydration;
  private EntityManager entityManager;

  @Captor private ArgumentCaptor<EntityType> entityTypeArgumentCaptor;

  @BeforeEach
  void setUpBeforeMethod() {
    // create referenced entities
    EntityType refEntityType = entityTestHarness.createDynamicRefEntityType();
    List<Entity> refEntities = entityTestHarness.createTestRefEntities(refEntityType, 1);

    entityType = entityTestHarness.createDynamicTestEntityType(refEntityType);

    // create hydrated entity
    hydratedEntity =
        entityTestHarness.createTestEntities(entityType, 1, refEntities).collect(toList()).get(0);

    LocalDate date = LocalDate.parse("2012-12-21");
    Instant dateTime = Instant.parse("1985-08-12T06:12:13Z");

    // create dehydrated entity
    dehydratedEntity = newHashMap();
    dehydratedEntity.put(ATTR_ID, "0");
    dehydratedEntity.put(ATTR_STRING, "string1");
    dehydratedEntity.put(ATTR_BOOL, true);
    dehydratedEntity.put(ATTR_CATEGORICAL, "0");
    dehydratedEntity.put(ATTR_CATEGORICAL_MREF, singletonList("0"));
    dehydratedEntity.put(ATTR_DATE, date);
    dehydratedEntity.put(ATTR_DATETIME, dateTime);
    dehydratedEntity.put(ATTR_EMAIL, "this.is@mail.address");
    dehydratedEntity.put(ATTR_DECIMAL, 0.123);
    dehydratedEntity.put(ATTR_HTML, null);
    dehydratedEntity.put(ATTR_HYPERLINK, "http://www.molgenis.org");
    dehydratedEntity.put(ATTR_LONG, 0L);
    dehydratedEntity.put(ATTR_INT, 10);
    dehydratedEntity.put(ATTR_SCRIPT, "/bin/blaat/script.sh");
    dehydratedEntity.put(ATTR_XREF, "0");
    dehydratedEntity.put(ATTR_MREF, singletonList("0"));
    dehydratedEntity.put(ATTR_COMPOUND_CHILD_INT, 10);
    dehydratedEntity.put(ATTR_ENUM, "option1");

    // mock entity manager
    entityManager =
        when(mock(EntityManager.class).create(entityType, EntityManager.CreationMode.NO_POPULATE))
            .thenReturn(new EntityWithComputedAttributes(new DynamicEntity(entityType)))
            .getMock();
    when(entityManager.getReference(entityTypeArgumentCaptor.capture(), eq("0")))
        .thenReturn(refEntities.get(0));
    when(entityManager.getReferences(entityTypeArgumentCaptor.capture(), eq(newArrayList("0"))))
        .thenReturn(refEntities);
    entityHydration = new EntityHydration(entityManager);
  }

  @Test
  void hydrateTest() {
    Entity actualHydratedEntity = entityHydration.hydrate(dehydratedEntity, entityType);
    assertTrue(EntityUtils.equals(actualHydratedEntity, hydratedEntity));
    // check that it has retrieved references of type TypeTestRef
    assertTrue(
        entityTypeArgumentCaptor.getAllValues().stream()
            .allMatch(emd -> emd.getId().equals("TypeTestRefDynamic")));
  }

  @Test
  void testHydrateFetch() {
    Fetch fetch = new Fetch().field(ATTR_ID);
    when(entityManager.createFetch(entityType, fetch))
        .thenReturn(
            new EntityWithComputedAttributes(
                new PartialEntity(new DynamicEntity(entityType), fetch, entityManager)));

    Entity actualHydratedEntity = entityHydration.hydrate(dehydratedEntity, entityType, fetch);
    assertEquals("0", hydratedEntity.getIdValue());
  }

  @Test
  void dehydrateTest() {
    Map<String, Object> actualDehydratedEntity = entityHydration.dehydrate(hydratedEntity);
    assertEquals(dehydratedEntity, actualDehydratedEntity);
  }

  @Test
  void dehydrateOnetoMany() {
    String attrName = "attr";
    Entity entity = mock(Entity.class);
    Entity oneToManyEntity0 = mock(Entity.class);
    String oneToManyEntity0IdValue = "ref0";
    when(oneToManyEntity0.getIdValue()).thenReturn(oneToManyEntity0IdValue);
    Entity oneToManyEntity1 = mock(Entity.class);
    String oneToManyEntity1IdValue = "ref1";
    when(oneToManyEntity1.getIdValue()).thenReturn(oneToManyEntity1IdValue);
    when(entity.getEntities(attrName)).thenReturn(newArrayList(oneToManyEntity0, oneToManyEntity1));
    EntityType entityType = mock(EntityType.class);
    Attribute oneToManyAttr = mock(Attribute.class);
    when(oneToManyAttr.getName()).thenReturn(attrName);
    when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
    when(entityType.getAtomicAttributes()).thenReturn(singleton(oneToManyAttr));
    when(entity.getEntityType()).thenReturn(entityType);
    assertEquals(
        singletonMap(attrName, newArrayList(oneToManyEntity0IdValue, oneToManyEntity1IdValue)),
        entityHydration.dehydrate(entity));
  }

  @Test
  void dehydrateXref() {
    String attrName = "attr";
    Entity entity = mock(Entity.class);
    Entity manyToOneEntity = mock(Entity.class);
    String manyToOneEntityIdValue = "ref0";
    when(manyToOneEntity.getIdValue()).thenReturn(manyToOneEntityIdValue);
    when(entity.getEntity(attrName)).thenReturn(manyToOneEntity);
    EntityType entityType = mock(EntityType.class);
    Attribute xrefAttr = mock(Attribute.class);
    when(xrefAttr.getName()).thenReturn(attrName);
    when(xrefAttr.getDataType()).thenReturn(XREF);
    when(entityType.getAtomicAttributes()).thenReturn(singleton(xrefAttr));
    when(entity.getEntityType()).thenReturn(entityType);
    assertEquals(singletonMap(attrName, manyToOneEntityIdValue), entityHydration.dehydrate(entity));
  }
}
