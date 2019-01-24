package org.molgenis.navigator.copy.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.testng.annotations.Test;

public class RelationTransformerTest {

  @Test
  public void testTransformPackageEmptyMap() {
    EntityType entityType = mock(EntityType.class);

    RelationTransformer.transformPackage(entityType, emptyMap());

    verify(entityType, never()).setPackage(any(Package.class));
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void testTransformPackage() {
    EntityType entityType = mock(EntityType.class, RETURNS_DEEP_STUBS);
    when(entityType.getPackage().getId()).thenReturn("oldPackage");
    Package newPackage = mock(Package.class);
    Map<String, Package> newPackages = ImmutableMap.of("oldPackage", newPackage);

    RelationTransformer.transformPackage(entityType, newPackages);

    verify(entityType).setPackage(newPackage);
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void testTransformPackageNoChange() {
    EntityType entityType = mock(EntityType.class, RETURNS_DEEP_STUBS);
    when(entityType.getPackage().getId()).thenReturn("oldPackage");
    Package newPackage = mock(Package.class);
    Map<String, Package> newPackages = ImmutableMap.of("otherOldPackage", newPackage);

    RelationTransformer.transformPackage(entityType, newPackages);

    verify(entityType, never()).setPackage(newPackage);
  }

  @Test
  public void testTransformExtendsEmptyMap() {
    EntityType entityType = mock(EntityType.class);

    RelationTransformer.transformExtends(entityType, emptyMap());

    verify(entityType, never()).setExtends(any(EntityType.class));
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void testTransformExtends() {
    EntityType entityType = mock(EntityType.class, RETURNS_DEEP_STUBS);
    when(entityType.getExtends().getId()).thenReturn("oldExtends");
    EntityType newExtends = mock(EntityType.class);
    Map<String, EntityType> newEntityTypes = ImmutableMap.of("oldExtends", newExtends);

    RelationTransformer.transformExtends(entityType, newEntityTypes);

    verify(entityType).setExtends(newExtends);
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void testTransformExtendsNoChange() {
    EntityType entityType = mock(EntityType.class, RETURNS_DEEP_STUBS);
    when(entityType.getExtends().getId()).thenReturn("oldExtends");
    EntityType newExtends = mock(EntityType.class);
    Map<String, EntityType> newEntityTypes = ImmutableMap.of("otherOldExtends", newExtends);

    RelationTransformer.transformExtends(entityType, newEntityTypes);

    verify(entityType, never()).setExtends(newExtends);
  }

  @Test
  public void testTransformRefEntitiesEmptyMap() {
    EntityType entityType = mock(EntityType.class);

    RelationTransformer.transformRefEntities(entityType, emptyMap());

    verify(entityType, never()).getAtomicAttributes();
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void testTransformRefEntity() {
    EntityType entityType = mock(EntityType.class);
    Attribute refAttr1 = mock(Attribute.class, RETURNS_DEEP_STUBS);
    Attribute refAttr2 = mock(Attribute.class, RETURNS_DEEP_STUBS);
    Attribute normalAttr = mock(Attribute.class);
    when(refAttr1.getIdentifier()).thenReturn("refAttr1");
    when(refAttr1.getDataType()).thenReturn(MREF);
    when(refAttr1.hasRefEntity()).thenReturn(true);
    when(refAttr1.getRefEntity().getId()).thenReturn("ref1");
    when(refAttr2.getIdentifier()).thenReturn("refAttr2");
    when(refAttr2.getDataType()).thenReturn(XREF);
    when(refAttr2.hasRefEntity()).thenReturn(true);
    when(refAttr2.getRefEntity().getId()).thenReturn("ref2");
    when(normalAttr.getIdentifier()).thenReturn("normalAttr");
    when(normalAttr.getDataType()).thenReturn(STRING);
    EntityType newRef1 = mock(EntityType.class);
    EntityType newRef3 = mock(EntityType.class);
    when(entityType.getAtomicAttributes()).thenReturn(asList(refAttr1, refAttr2, normalAttr));
    Map<String, EntityType> newEntityTypes = ImmutableMap.of("ref1", newRef1, "ref3", newRef3);

    RelationTransformer.transformRefEntities(entityType, newEntityTypes);

    verify(refAttr1).setRefEntity(newRef1);
    verify(refAttr2, never()).setRefEntity(any(EntityType.class));
    verify(normalAttr, never()).setRefEntity(any(EntityType.class));
  }

  @Test
  public void testTransformMappedBysEmptyMap() {
    EntityType entityType = mock(EntityType.class);

    RelationTransformer.transformMappedBys(entityType, emptyMap());

    verify(entityType, never()).getAtomicAttributes();
  }

  @SuppressWarnings({"ConstantConditions", "unchecked"})
  @Test
  public void testTransformMappedBy() {
    EntityType entityType = mock(EntityType.class);
    Attribute refAttr1 = mock(Attribute.class, RETURNS_DEEP_STUBS);
    Attribute refAttr2 = mock(Attribute.class, RETURNS_DEEP_STUBS);
    Attribute normalAttr = mock(Attribute.class);
    when(refAttr1.isMappedBy()).thenReturn(true);
    when(refAttr1.getMappedBy().getIdentifier()).thenReturn("mappedByAttr1");
    when(refAttr1.getDataType()).thenReturn(ONE_TO_MANY);
    when(refAttr2.isMappedBy()).thenReturn(true);
    when(refAttr2.getMappedBy().getIdentifier()).thenReturn("mappedByAttr2");
    when(refAttr2.getDataType()).thenReturn(ONE_TO_MANY);
    when(normalAttr.getIdentifier()).thenReturn("normalAttr");
    when(normalAttr.getDataType()).thenReturn(STRING);
    Attribute newMappedByAttr1 = mock(Attribute.class);
    Attribute newMappedByAttr3 = mock(Attribute.class);
    when(entityType.getAtomicAttributes()).thenReturn(asList(refAttr1, refAttr2, normalAttr));
    Map<String, Attribute> newAttributes =
        ImmutableMap.of("mappedByAttr1", newMappedByAttr1, "mappedByAttr3", newMappedByAttr3);

    RelationTransformer.transformMappedBys(entityType, newAttributes);

    verify(refAttr1).setMappedBy(newMappedByAttr1);
    verify(refAttr2, never()).setMappedBy(any(Attribute.class));
    verify(normalAttr, never()).setMappedBy(any(Attribute.class));
  }
}
