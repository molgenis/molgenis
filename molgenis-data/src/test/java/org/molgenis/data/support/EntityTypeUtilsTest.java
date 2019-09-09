package org.molgenis.data.support;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL_MREF;
import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.XREF;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.util.EntityTypeUtils;

class EntityTypeUtilsTest {
  static Iterator<Object[]> isReferenceTypeAttrProvider() {
    List<Object[]> dataList = Lists.newArrayList();
    for (AttributeType attrType : AttributeType.values()) {
      Attribute attr = mock(Attribute.class);
      when(attr.getDataType()).thenReturn(attrType);
      when(attr.toString()).thenReturn("attr_" + attrType.toString());

      boolean isRefAttr =
          attrType == CATEGORICAL
              || attrType == CATEGORICAL_MREF
              || attrType == FILE
              || attrType == MREF
              || attrType == ONE_TO_MANY
              || attrType == XREF;
      dataList.add(new Object[] {attr, isRefAttr});
    }
    return dataList.iterator();
  }

  @ParameterizedTest
  @MethodSource("isReferenceTypeAttrProvider")
  void isReferenceTypeAttr(Attribute attr, boolean isRefAttr) {
    assertEquals(EntityTypeUtils.isReferenceType(attr), isRefAttr);
  }

  static Iterator<Object[]> isReferenceTypeAttrTypeProvider() {
    List<Object[]> dataList = Lists.newArrayList();
    for (AttributeType attrType : AttributeType.values()) {
      boolean isRefAttr =
          attrType == CATEGORICAL
              || attrType == CATEGORICAL_MREF
              || attrType == FILE
              || attrType == MREF
              || attrType == ONE_TO_MANY
              || attrType == XREF;
      dataList.add(new Object[] {attrType, isRefAttr});
    }
    return dataList.iterator();
  }

  @ParameterizedTest
  @MethodSource("isReferenceTypeAttrTypeProvider")
  void isReferenceTypeAttrType(AttributeType attrType, boolean isRefAttrType) {
    assertEquals(EntityTypeUtils.isReferenceType(attrType), isRefAttrType);
  }

  static Iterator<Object[]> isMultipleReferenceTypeProvider() {
    List<Object[]> dataList = Lists.newArrayList();
    for (AttributeType attrType : AttributeType.values()) {
      Attribute attr = mock(Attribute.class);
      when(attr.getDataType()).thenReturn(attrType);
      when(attr.toString()).thenReturn("attr_" + attrType.toString());

      boolean isMultipleRefAttr =
          attrType == CATEGORICAL_MREF || attrType == MREF || attrType == ONE_TO_MANY;
      dataList.add(new Object[] {attr, isMultipleRefAttr});
    }
    return dataList.iterator();
  }

  @ParameterizedTest
  @MethodSource("isMultipleReferenceTypeProvider")
  void isMultipleReferenceType(Attribute attr, boolean isMultipleRefAttr) {
    assertEquals(EntityTypeUtils.isMultipleReferenceType(attr), isMultipleRefAttr);
  }

  @Test
  void getAttributeNames() {
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn("attr0").getMock();
    Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn("attr1").getMock();
    assertEquals(
        newArrayList(EntityTypeUtils.getAttributeNames(asList(attr0, attr1))),
        asList("attr0", "attr1"));
  }

  @Test
  void buildFullNamePackage() {
    Package package_ = when(mock(Package.class).getId()).thenReturn("my_first_package").getMock();
    assertEquals(
        EntityTypeUtils.buildFullName(package_, "simpleName"), "my_first_package_simpleName");
  }

  @Test
  void buildFullNameNoPackage() {
    assertEquals(EntityTypeUtils.buildFullName(null, "simpleName"), "simpleName");
  }

  @Test
  void isSystemEntityIfInSystemPackage() {
    EntityType entity = mock(EntityType.class);
    Package entityPackage = mock(Package.class);
    when(entity.getPackage()).thenReturn(entityPackage);
    when(entityPackage.getId()).thenReturn("sys");
    assertTrue(EntityTypeUtils.isSystemEntity(entity));
  }

  @Test
  void isSystemEntityIfInSystemSubPackage() {
    EntityType entity = mock(EntityType.class);
    Package entityPackage = mock(Package.class);
    when(entityPackage.getId()).thenReturn("foo");

    Package sysPackage = mock(Package.class);
    when(sysPackage.getId()).thenReturn("sys");

    when(entityPackage.getRootPackage()).thenReturn(sysPackage);
    when(entity.getPackage()).thenReturn(entityPackage);
    when(entity.getId()).thenReturn("sys_foo_Entity");

    assertTrue(EntityTypeUtils.isSystemEntity(entity));
  }

  @Test
  void isSystemEntityNotASystemIfNotInSystemPackage() {
    EntityType entity = mock(EntityType.class);
    Package entityPackage = mock(Package.class);
    when(entity.getPackage()).thenReturn(entityPackage);
    when(entityPackage.getId()).thenReturn("foo_bar");
    when(entity.getId()).thenReturn("foo_bar_Entity");
    assertFalse(EntityTypeUtils.isSystemEntity(entity));
  }

  @Test
  void isSystemEntityNotASystemEntityIfNotInPackage() {
    EntityType entity = mock(EntityType.class);
    when(entity.getPackage()).thenReturn(null);
    assertFalse(EntityTypeUtils.isSystemEntity(entity));
  }

  @Test
  void testCreateFetchForReindexingIndexingDepth0() {
    EntityType entityType = createMockEntityType();
    when(entityType.getIndexingDepth()).thenReturn(0);
    Fetch expectedFetch = new Fetch().field("MyEntityTypeAttr").field("MyEntityTypeRefAttr");
    assertEquals(EntityTypeUtils.createFetchForReindexing(entityType), expectedFetch);
  }

  @Test
  void testCreateFetchForReindexingIndexingDepth1() {
    EntityType entityType = createMockEntityType();
    when(entityType.getIndexingDepth()).thenReturn(1);
    Fetch expectedFetch =
        new Fetch()
            .field("MyEntityTypeAttr")
            .field(
                "MyEntityTypeRefAttr",
                new Fetch().field("MyRefEntityTypeAttr").field("MyRefEntityTypeRefAttr"));
    assertEquals(EntityTypeUtils.createFetchForReindexing(entityType), expectedFetch);
  }

  @Test
  void testCreateFetchForReindexingIndexingDepth2() {
    EntityType entityType = createMockEntityType();
    when(entityType.getIndexingDepth()).thenReturn(2);
    Fetch expectedFetch =
        new Fetch()
            .field("MyEntityTypeAttr")
            .field(
                "MyEntityTypeRefAttr",
                new Fetch()
                    .field("MyRefEntityTypeAttr")
                    .field(
                        "MyRefEntityTypeRefAttr",
                        new Fetch()
                            .field("MyRefRefEntityTypeAttr")
                            .field("MyRefRefEntityTypeRefAttr")));
    assertEquals(EntityTypeUtils.createFetchForReindexing(entityType), expectedFetch);
  }

  @Test
  void testGetEntityTypeFetch() {
    Fetch fetch = EntityTypeUtils.getEntityTypeFetch();

    assertTrue(fetch.hasField(EntityTypeMetadata.ID));
    assertTrue(fetch.hasField(EntityTypeMetadata.PACKAGE));
    assertTrue(fetch.hasField(EntityTypeMetadata.LABEL));
    assertTrue(fetch.hasField(EntityTypeMetadata.DESCRIPTION));
    assertTrue(fetch.hasField(EntityTypeMetadata.ATTRIBUTES));
    assertTrue(fetch.hasField(EntityTypeMetadata.IS_ABSTRACT));
    assertTrue(fetch.hasField(EntityTypeMetadata.EXTENDS));
    assertTrue(fetch.hasField(EntityTypeMetadata.TAGS));
    assertTrue(fetch.hasField(EntityTypeMetadata.BACKEND));
  }

  private EntityType createMockEntityType() {
    EntityType refRefEntityType = mock(EntityType.class);
    Attribute refRefEntityTypeAttr =
        when(mock(Attribute.class).getName()).thenReturn("MyRefRefEntityTypeAttr").getMock();
    Attribute refRefEntityTypeRefAttr =
        when(mock(Attribute.class).getName()).thenReturn("MyRefRefEntityTypeRefAttr").getMock();
    when(refRefEntityTypeRefAttr.hasRefEntity()).thenReturn(true);
    when(refRefEntityTypeRefAttr.getRefEntity()).thenReturn(refRefEntityType);
    when(refRefEntityType.getAtomicAttributes())
        .thenReturn(asList(refRefEntityTypeAttr, refRefEntityTypeRefAttr));

    EntityType refEntityType = mock(EntityType.class);
    Attribute refEntityTypeAttr =
        when(mock(Attribute.class).getName()).thenReturn("MyRefEntityTypeAttr").getMock();
    Attribute refEntityTypeRefAttr =
        when(mock(Attribute.class).getName()).thenReturn("MyRefEntityTypeRefAttr").getMock();
    when(refEntityTypeRefAttr.hasRefEntity()).thenReturn(true);
    when(refEntityTypeRefAttr.getRefEntity()).thenReturn(refRefEntityType);
    when(refEntityType.getAtomicAttributes())
        .thenReturn(asList(refEntityTypeAttr, refEntityTypeRefAttr));

    EntityType entityType = mock(EntityType.class);
    Attribute attr = when(mock(Attribute.class).getName()).thenReturn("MyEntityTypeAttr").getMock();
    Attribute refAttr =
        when(mock(Attribute.class).getName()).thenReturn("MyEntityTypeRefAttr").getMock();
    when(refAttr.hasRefEntity()).thenReturn(true);
    when(refAttr.getRefEntity()).thenReturn(refEntityType);
    when(entityType.getAtomicAttributes()).thenReturn(asList(attr, refAttr));

    return entityType;
  }
}
