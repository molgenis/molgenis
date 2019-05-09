package org.molgenis.data.export;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.testng.Assert.*;

import java.util.List;
import org.molgenis.data.export.mapper.AttributeMapper;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.Test;

public class AttributeMapperTest extends AbstractMockitoTest {

  @Test
  public void testMapAttribute() {
    Attribute attr = mock(Attribute.class);
    Attribute parent = mock(Attribute.class);
    EntityType entityType = mock(EntityType.class);
    EntityType refEntityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("entityId");
    when(refEntityType.getId()).thenReturn("refEntityId");
    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);

    when(tag1.getId()).thenReturn("tag1");
    when(tag2.getId()).thenReturn("tag2");

    when(attr.getEntity()).thenReturn(entityType);
    when(attr.hasRefEntity()).thenReturn(true);
    when(attr.getRefEntity()).thenReturn(refEntityType);
    when(attr.getLookupAttributeIndex()).thenReturn(1);
    when(attr.getTags()).thenReturn(newArrayList(tag1, tag2));
    when(attr.getVisibleExpression()).thenReturn(null);
    when(attr.isVisible()).thenReturn(true);
    when(attr.getNullableExpression()).thenReturn(null);
    when(attr.isNillable()).thenReturn(true);
    when(attr.getParent()).thenReturn(parent);
    when(parent.getName()).thenReturn("compound");
    when(attr.isIdAttribute()).thenReturn(true);

    doReturn("attr1").when(attr).get(AttributeMetadata.NAME);
    doReturn("label1").when(attr).get(AttributeMetadata.LABEL);
    doReturn("this is attr 1").when(attr).get(AttributeMetadata.DESCRIPTION);
    doReturn(STRING).when(attr).get(AttributeMetadata.TYPE);
    doReturn(true).when(attr).get(AttributeMetadata.IS_UNIQUE);
    doReturn(true).when(attr).get(AttributeMetadata.IS_LABEL_ATTRIBUTE);
    doReturn(false).when(attr).get(AttributeMetadata.IS_READ_ONLY);
    doReturn(false).when(attr).get(AttributeMetadata.IS_AGGREGATABLE);
    doReturn(null).when(attr).get(AttributeMetadata.RANGE_MAX);
    doReturn(null).when(attr).get(AttributeMetadata.RANGE_MIN);
    doReturn("express").when(attr).get(AttributeMetadata.EXPRESSION);
    doReturn("validate").when(attr).get(AttributeMetadata.VALIDATION_EXPRESSION);
    doReturn("molgenis").when(attr).get(AttributeMetadata.DEFAULT_VALUE);
    doReturn(true).when(attr).get(AttributeMetadata.IS_AUTO);

    List<Object> expected =
        newArrayList(
            "attr1",
            "label1",
            "this is attr 1",
            "entityId",
            "STRING",
            "refEntityId",
            "true",
            "true",
            "true",
            "true",
            "true",
            "false",
            "false",
            "true",
            null,
            "compound",
            null,
            null,
            null,
            "express",
            "validate",
            "molgenis",
            "tag1,tag2",
            "true",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    List<Object> actual = AttributeMapper.map(attr);
    assertEquals(actual, expected);
  }

  @Test
  public void testMapAttributeExpressionsAndRange() {
    Attribute attr = mock(Attribute.class);
    Attribute parent = mock(Attribute.class);
    EntityType entityType = mock(EntityType.class);
    EntityType refEntityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("entityId");
    when(refEntityType.getId()).thenReturn("refEntityId");
    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);

    when(tag1.getId()).thenReturn("tag1");
    when(tag2.getId()).thenReturn("tag2");

    when(attr.getEntity()).thenReturn(entityType);
    when(attr.hasRefEntity()).thenReturn(true);
    when(attr.getRefEntity()).thenReturn(refEntityType);
    when(attr.getLookupAttributeIndex()).thenReturn(1);
    when(attr.getTags()).thenReturn(newArrayList(tag1, tag2));
    when(attr.getVisibleExpression()).thenReturn("isThisVisible");
    when(attr.getNullableExpression()).thenReturn("isThisNillable");
    when(attr.getParent()).thenReturn(parent);
    when(parent.getName()).thenReturn("compound");
    when(attr.isIdAttribute()).thenReturn(true);
    when(attr.getEnumOptions()).thenReturn(null);

    doReturn("attr1").when(attr).get(AttributeMetadata.NAME);
    doReturn("label1").when(attr).get(AttributeMetadata.LABEL);
    doReturn("this is attr 1").when(attr).get(AttributeMetadata.DESCRIPTION);
    doReturn(INT).when(attr).get(AttributeMetadata.TYPE);
    doReturn(true).when(attr).get(AttributeMetadata.IS_UNIQUE);
    doReturn(true).when(attr).get(AttributeMetadata.IS_LABEL_ATTRIBUTE);
    doReturn(false).when(attr).get(AttributeMetadata.IS_READ_ONLY);
    doReturn(false).when(attr).get(AttributeMetadata.IS_AGGREGATABLE);
    doReturn(1).when(attr).get(AttributeMetadata.RANGE_MAX);
    doReturn(2).when(attr).get(AttributeMetadata.RANGE_MIN);
    doReturn("express").when(attr).get(AttributeMetadata.EXPRESSION);
    doReturn("validate").when(attr).get(AttributeMetadata.VALIDATION_EXPRESSION);
    doReturn("molgenis").when(attr).get(AttributeMetadata.DEFAULT_VALUE);

    List<Object> expected =
        newArrayList(
            "attr1",
            "label1",
            "this is attr 1",
            "entityId",
            "INT",
            "refEntityId",
            "isThisNillable",
            "true",
            "isThisVisible",
            "true",
            "true",
            "false",
            "false",
            "true",
            null,
            "compound",
            "1",
            "2",
            null,
            "express",
            "validate",
            "molgenis",
            "tag1,tag2",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    List<Object> actual = AttributeMapper.map(attr);
    assertEquals(actual, expected);
  }

  @Test
  public void testMapAttributeOneToMany() {
    Attribute attr = mock(Attribute.class);
    EntityType entityType = mock(EntityType.class);
    EntityType refEntityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("entityId");
    when(refEntityType.getId()).thenReturn("refEntityId");
    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);

    when(tag1.getId()).thenReturn("tag1");
    when(tag2.getId()).thenReturn("tag2");

    when(attr.getEntity()).thenReturn(entityType);
    when(attr.hasRefEntity()).thenReturn(true);
    when(attr.getRefEntity()).thenReturn(refEntityType);
    when(attr.getLookupAttributeIndex()).thenReturn(1);
    when(attr.getTags()).thenReturn(newArrayList(tag1, tag2));
    when(attr.getVisibleExpression()).thenReturn(null);
    when(attr.isVisible()).thenReturn(true);
    when(attr.getNullableExpression()).thenReturn(null);
    when(attr.isNillable()).thenReturn(true);
    when(attr.getParent()).thenReturn(null);
    when(attr.isIdAttribute()).thenReturn(true);
    when(attr.getEnumOptions()).thenReturn(null);

    doReturn("attr1").when(attr).get(AttributeMetadata.NAME);
    doReturn("label1").when(attr).get(AttributeMetadata.LABEL);
    doReturn("this is attr 1").when(attr).get(AttributeMetadata.DESCRIPTION);
    doReturn(ONE_TO_MANY).when(attr).get(AttributeMetadata.TYPE);
    doReturn(true).when(attr).get(AttributeMetadata.IS_UNIQUE);
    doReturn(true).when(attr).get(AttributeMetadata.IS_LABEL_ATTRIBUTE);
    doReturn(false).when(attr).get(AttributeMetadata.IS_READ_ONLY);
    doReturn(false).when(attr).get(AttributeMetadata.IS_AGGREGATABLE);
    doReturn(null).when(attr).get(AttributeMetadata.RANGE_MAX);
    doReturn(null).when(attr).get(AttributeMetadata.RANGE_MIN);
    doReturn("express").when(attr).get(AttributeMetadata.EXPRESSION);
    doReturn("validate").when(attr).get(AttributeMetadata.VALIDATION_EXPRESSION);
    doReturn("molgenis").when(attr).get(AttributeMetadata.DEFAULT_VALUE);

    List<Object> expected =
        newArrayList(
            "attr1",
            "label1",
            "this is attr 1",
            "entityId",
            "ONE_TO_MANY",
            "refEntityId",
            "true",
            "true",
            "true",
            "true",
            "true",
            "false",
            "false",
            "true",
            null,
            null,
            null,
            null,
            null,
            "express",
            "validate",
            "molgenis",
            "tag1,tag2",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    List<Object> actual = AttributeMapper.map(attr);
    assertEquals(actual, expected);
  }

  @Test
  public void testMapAttributeEnum() {
    Attribute attr = mock(Attribute.class);
    EntityType entityType = mock(EntityType.class);
    EntityType refEntityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("entityId");
    when(refEntityType.getId()).thenReturn("refEntityId");
    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);

    when(tag1.getId()).thenReturn("tag1");
    when(tag2.getId()).thenReturn("tag2");

    when(attr.getEntity()).thenReturn(entityType);
    when(attr.hasRefEntity()).thenReturn(true);
    when(attr.getRefEntity()).thenReturn(refEntityType);
    when(attr.getLookupAttributeIndex()).thenReturn(1);
    when(attr.getTags()).thenReturn(newArrayList(tag1, tag2));
    when(attr.getVisibleExpression()).thenReturn(null);
    when(attr.isVisible()).thenReturn(true);
    when(attr.getNullableExpression()).thenReturn(null);
    when(attr.isNillable()).thenReturn(true);
    when(attr.getParent()).thenReturn(null);
    when(attr.isIdAttribute()).thenReturn(true);
    when(attr.getEnumOptions()).thenReturn(newArrayList("enum1", "enum2"));

    doReturn("attr1").when(attr).get(AttributeMetadata.NAME);
    doReturn("label1").when(attr).get(AttributeMetadata.LABEL);
    doReturn("this is attr 1").when(attr).get(AttributeMetadata.DESCRIPTION);
    doReturn(ENUM).when(attr).get(AttributeMetadata.TYPE);
    doReturn(true).when(attr).get(AttributeMetadata.IS_UNIQUE);
    doReturn(true).when(attr).get(AttributeMetadata.IS_LABEL_ATTRIBUTE);
    doReturn(false).when(attr).get(AttributeMetadata.IS_READ_ONLY);
    doReturn(false).when(attr).get(AttributeMetadata.IS_AGGREGATABLE);
    doReturn(null).when(attr).get(AttributeMetadata.RANGE_MAX);
    doReturn(null).when(attr).get(AttributeMetadata.RANGE_MIN);
    doReturn(null).when(attr).get(AttributeMetadata.EXPRESSION);
    doReturn(null).when(attr).get(AttributeMetadata.VALIDATION_EXPRESSION);
    doReturn(null).when(attr).get(AttributeMetadata.DEFAULT_VALUE);

    List<Object> expected =
        newArrayList(
            "attr1",
            "label1",
            "this is attr 1",
            "entityId",
            "ENUM",
            "refEntityId",
            "true",
            "true",
            "true",
            "true",
            "true",
            "false",
            "false",
            "true",
            "enum1,enum2",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "tag1,tag2",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    List<Object> actual = AttributeMapper.map(attr);
    assertEquals(actual, expected);
  }

  @Test
  public void testMapAttributeI18N() {
    Attribute attr = mock(Attribute.class);
    EntityType entityType = mock(EntityType.class);
    EntityType refEntityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("entityId");
    when(refEntityType.getId()).thenReturn("refEntityId");
    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);

    when(tag1.getId()).thenReturn("tag1");
    when(tag2.getId()).thenReturn("tag2");

    when(attr.getEntity()).thenReturn(entityType);
    when(attr.hasRefEntity()).thenReturn(true);
    when(attr.getRefEntity()).thenReturn(refEntityType);
    when(attr.getLookupAttributeIndex()).thenReturn(1);
    when(attr.getTags()).thenReturn(newArrayList(tag1, tag2));
    when(attr.getVisibleExpression()).thenReturn(null);
    when(attr.isVisible()).thenReturn(true);
    when(attr.getNullableExpression()).thenReturn(null);
    when(attr.isNillable()).thenReturn(true);
    when(attr.getParent()).thenReturn(null);
    when(attr.isIdAttribute()).thenReturn(true);
    when(attr.getEnumOptions()).thenReturn(newArrayList("enum1", "enum2"));

    doReturn("attr1").when(attr).get(AttributeMetadata.NAME);
    doReturn("label1").when(attr).get(AttributeMetadata.LABEL);
    doReturn("this is attr 1").when(attr).get(AttributeMetadata.DESCRIPTION);
    doReturn(ENUM).when(attr).get(AttributeMetadata.TYPE);
    doReturn(true).when(attr).get(AttributeMetadata.IS_UNIQUE);
    doReturn(true).when(attr).get(AttributeMetadata.IS_LABEL_ATTRIBUTE);
    doReturn(false).when(attr).get(AttributeMetadata.IS_READ_ONLY);
    doReturn(false).when(attr).get(AttributeMetadata.IS_AGGREGATABLE);
    doReturn(null).when(attr).get(AttributeMetadata.RANGE_MAX);
    doReturn(null).when(attr).get(AttributeMetadata.RANGE_MIN);
    doReturn(null).when(attr).get(AttributeMetadata.EXPRESSION);
    doReturn(null).when(attr).get(AttributeMetadata.VALIDATION_EXPRESSION);
    doReturn(null).when(attr).get(AttributeMetadata.DEFAULT_VALUE);
    doReturn(null).when(attr).get("isAuto");
    doReturn("Dutch Label").when(attr).get("labelNl");
    doReturn("Dutch description").when(attr).get("descriptionNl");
    doReturn("English Label").when(attr).get("labelEn");
    doReturn("English description").when(attr).get("descriptionEn");
    doReturn("German Label").when(attr).get("labelDe");
    doReturn("German description").when(attr).get("descriptionDe");
    doReturn("Italian Label").when(attr).get("labelIt");
    doReturn("Italian description").when(attr).get("descriptionIt");
    doReturn("Portugese Label").when(attr).get("labelPt");
    doReturn("Portugese description").when(attr).get("descriptionPt");
    doReturn("Spanish Label").when(attr).get("labelEs");
    doReturn("Spanish description").when(attr).get("descriptionEs");
    doReturn("French Label").when(attr).get("labelFr");
    doReturn("French description").when(attr).get("descriptionFr");
    doReturn("xx Label").when(attr).get("labelXx");
    doReturn("xx description").when(attr).get("descriptionXx");

    List<Object> expected =
        newArrayList(
            "attr1",
            "label1",
            "this is attr 1",
            "entityId",
            "ENUM",
            "refEntityId",
            "true",
            "true",
            "true",
            "true",
            "true",
            "false",
            "false",
            "true",
            "enum1,enum2",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "tag1,tag2",
            null,
            "English Label",
            "English description",
            "Dutch Label",
            "Dutch description",
            "German Label",
            "German description",
            "Spanish Label",
            "Spanish description",
            "Italian Label",
            "Italian description",
            "Portugese Label",
            "Portugese description",
            "French Label",
            "French description",
            "xx Label",
            "xx description");
    List<Object> actual = AttributeMapper.map(attr);
    assertEquals(actual, expected);
  }
}
