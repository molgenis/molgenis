package org.molgenis.data.meta.model;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.EntityTypeMetadata.LABEL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.config.MetadataTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      EntityTypeMetadata.class,
      EntityTypeFactory.class,
      MetadataTestConfig.class
    })
public class EntityTypeTest extends AbstractSystemEntityTest {

  @Autowired EntityTypeMetadata metadata;
  @Autowired EntityTypeFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, EntityType.class, factory, getOverriddenReturnTypes(), getExcludedAttrs(), true);
  }

  @Test
  public void addSequenceNumberNull() {
    Attribute attr1 = mock(Attribute.class);
    Attribute attr2 = mock(Attribute.class);
    Attribute attr3 = mock(Attribute.class);
    Attribute attribute = mock(Attribute.class);

    when(attr1.getSequenceNumber()).thenReturn(0);
    when(attr2.getSequenceNumber()).thenReturn(1);
    when(attr3.getSequenceNumber()).thenReturn(2);
    when(attribute.getSequenceNumber()).thenReturn(null);

    EntityType.addSequenceNumber(attribute, Lists.newArrayList(attr1, attr2, attr3));
    verify(attribute).setSequenceNumber(3);
  }

  @Test
  public void addSequenceNumberNullAndOtherNull() {
    Attribute attr1 = mock(Attribute.class);
    Attribute attr2 = mock(Attribute.class);
    Attribute attr3 = mock(Attribute.class);
    Attribute attribute = mock(Attribute.class);

    when(attr1.getSequenceNumber()).thenReturn(null);
    when(attr2.getSequenceNumber()).thenReturn(null);
    when(attr3.getSequenceNumber()).thenReturn(null);
    when(attribute.getSequenceNumber()).thenReturn(null);

    EntityType.addSequenceNumber(attribute, Lists.newArrayList(attr1, attr2, attr3));
    verify(attribute).setSequenceNumber(0);
  }

  @Test
  public void setLabel() {
    EntityType entityTypeMeta = mockEntityTypeMeta();
    Attribute strAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    doReturn(strAttr).when(entityTypeMeta).getAttribute(LABEL);
    EntityType entityType = new EntityType(entityTypeMeta);
    String label = "label";
    entityType.setLabel(label);
    assertEquals(entityType.getLabel(), label);
    assertEquals(entityType.getString(LABEL), label);
  }

  @Test
  public void setLabelNull() {
    EntityType entityTypeMeta = mockEntityTypeMeta();
    EntityType entityType = new EntityType(entityTypeMeta);
    assertNull(entityType.getLabel());
    assertNull(entityType.getString(LABEL));
  }

  @Test
  public void newInstanceShallowCopy() {
    EntityType entityTypeMeta = mockEntityTypeMeta();
    Attribute strAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute xrefAttr = when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
    Attribute mrefAttr = when(mock(Attribute.class).getDataType()).thenReturn(MREF).getMock();
    doReturn(strAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.ID);
    doReturn(xrefAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.PACKAGE);
    doReturn(strAttr).when(entityTypeMeta).getAttribute(LABEL);
    doReturn(strAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.DESCRIPTION);
    doReturn(mrefAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.ATTRIBUTES);
    doReturn(xrefAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.EXTENDS);
    doReturn(mrefAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.TAGS);
    doReturn(strAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.BACKEND);

    Package package_ = mock(Package.class);

    EntityType extendsEntityType = mock(EntityType.class);

    Attribute attrId = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
    when(attrId.isIdAttribute()).thenReturn(true);
    when(attrId.getLookupAttributeIndex()).thenReturn(0);
    Attribute attrLabel = when(mock(Attribute.class).getName()).thenReturn("label").getMock();
    when(attrLabel.isLabelAttribute()).thenReturn(true);
    when(attrLabel.getLookupAttributeIndex()).thenReturn(1);
    Attribute attrCompoundPart =
        when(mock(Attribute.class).getName()).thenReturn("compoundPart").getMock();
    when(attrCompoundPart.getLookupAttributeIndex()).thenReturn(null);
    Attribute attrCompound = when(mock(Attribute.class).getName()).thenReturn("compound").getMock();
    when(attrCompound.getLookupAttributeIndex()).thenReturn(null);
    Tag tag0 = mock(Tag.class);
    Tag tag1 = mock(Tag.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getEntityType()).thenReturn(entityTypeMeta);
    when(entityType.getId()).thenReturn("myPackage_myEntity");
    when(entityType.getPackage()).thenReturn(package_);
    when(entityType.getLabel()).thenReturn("label");
    when(entityType.getDescription()).thenReturn("description");

    when(entityType.getOwnAllAttributes())
        .thenReturn(asList(attrId, attrLabel, attrCompound, attrCompoundPart));
    when(entityType.isAbstract()).thenReturn(false);
    when(entityType.getExtends()).thenReturn(extendsEntityType);
    when(entityType.getTags()).thenReturn(asList(tag0, tag1));
    when(entityType.getBackend()).thenReturn("backend");
    when(entityType.getIndexingDepth()).thenReturn(3);

    EntityType entityTypeCopy = EntityType.newInstance(entityType);
    assertSame(entityTypeCopy.getEntityType(), entityTypeMeta);
    assertEquals(entityTypeCopy.getId(), "myPackage_myEntity");
    assertSame(entityTypeCopy.getPackage(), package_);
    assertEquals(entityTypeCopy.getLabel(), "label");
    assertEquals(entityTypeCopy.getDescription(), "description");

    List<Attribute> ownAttrsCopy = newArrayList(entityTypeCopy.getOwnAllAttributes());
    assertEquals(ownAttrsCopy.size(), 4);
    assertSame(ownAttrsCopy.get(0), attrId);
    assertSame(ownAttrsCopy.get(1), attrLabel);
    assertSame(ownAttrsCopy.get(2), attrCompound);
    assertSame(ownAttrsCopy.get(3), attrCompoundPart);
    assertSame(entityTypeCopy.getIdAttribute(), attrId);
    assertSame(entityTypeCopy.getLabelAttribute(), attrLabel);

    List<Attribute> ownLookAttrsCopy = newArrayList(entityTypeCopy.getOwnLookupAttributes());

    assertEquals(ownLookAttrsCopy.size(), 2);
    assertSame(ownLookAttrsCopy.get(0), attrId);
    assertSame(ownLookAttrsCopy.get(1), attrLabel);

    assertFalse(entityTypeCopy.isAbstract());
    assertSame(entityTypeCopy.getExtends(), extendsEntityType);

    List<Tag> tagsCopy = newArrayList(entityTypeCopy.getTags());
    assertEquals(tagsCopy.size(), 2);
    assertSame(tagsCopy.get(0), tag0);
    assertSame(tagsCopy.get(1), tag1);
    assertEquals(entityTypeCopy.getBackend(), "backend");
    assertEquals(entityTypeCopy.getIndexingDepth(), 3);
  }

  @Test(
      expectedExceptions = MolgenisDataException.class,
      expectedExceptionsMessageRegExp =
          "Entity \\[myEntity\\] already contains attribute with name \\[attrName\\], duplicate attribute names are not allowed")
  public void addAttributeWithDuplicateName() {
    EntityType entityTypeMeta = mockEntityTypeMeta();
    Attribute strAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute mrefAttr = when(mock(Attribute.class).getDataType()).thenReturn(MREF).getMock();
    doReturn(strAttr).when(entityTypeMeta).getAttribute(LABEL);
    doReturn(mrefAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.ATTRIBUTES);
    EntityType entityType = new EntityType(entityTypeMeta);
    entityType.setLabel("myEntity");
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn("attrName").getMock();
    Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn("attrName").getMock();
    entityType.addAttribute(attr0);
    entityType.addAttribute(attr1);
  }

  @Test
  public void addLabelAttributeSetsNillableFalse() {
    EntityType entityTypeMeta = mockEntityTypeMeta();
    EntityType entityType = new EntityType(entityTypeMeta);
    Attribute attr = mock(Attribute.class);

    entityType.setAttributeRoles(attr, ROLE_LABEL);

    verify(attr).setNillable(false);
  }

  @Test
  public void testRemoveAttribute() {
    EntityType entityTypeMeta = mockEntityTypeMeta();
    EntityType entityType = new EntityType(entityTypeMeta);
    Attribute mrefAttr = when(mock(Attribute.class).getDataType()).thenReturn(MREF).getMock();
    doReturn(mrefAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.ATTRIBUTES);

    Attribute normalAttr = mock(Attribute.class);
    when(normalAttr.getName()).thenReturn("normalAttr");
    Attribute compoundAttr =
        when(mock(Attribute.class).getDataType()).thenReturn(COMPOUND).getMock();
    when(compoundAttr.getName()).thenReturn("compoundAttr");
    Attribute childAttr1 = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    when(compoundAttr.getName()).thenReturn("childAttr1");
    Attribute childAttr2 = when(mock(Attribute.class).getDataType()).thenReturn(COMPOUND).getMock();
    when(compoundAttr.getName()).thenReturn("childAttr2");
    Attribute childAttr2a = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    when(compoundAttr.getName()).thenReturn("childAttr2a");
    when(compoundAttr.getChildren()).thenReturn(asList(childAttr1, childAttr2));
    when(childAttr2.getChildren()).thenReturn(singletonList(childAttr2a));
    entityType.setOwnAllAttributes(
        asList(normalAttr, compoundAttr, childAttr1, childAttr2, childAttr2a));

    entityType.removeAttribute(compoundAttr);

    assertEquals(Iterables.size(entityType.getAllAttributes()), 1);
    assertEquals(entityType.getAllAttributes().iterator().next(), normalAttr);
  }

  private EntityType mockEntityTypeMeta() {
    EntityType entityTypeMeta = mock(EntityType.class);
    Attribute intAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    Attribute boolAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    doReturn(boolAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.IS_ABSTRACT);
    doReturn(intAttr).when(entityTypeMeta).getAttribute(EntityTypeMetadata.INDEXING_DEPTH);
    return entityTypeMeta;
  }
}
