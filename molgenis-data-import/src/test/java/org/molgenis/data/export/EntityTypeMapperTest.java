package org.molgenis.data.export;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.List;
import org.molgenis.data.export.mapper.EntityTypeMapper;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.Test;

public class EntityTypeMapperTest extends AbstractMockitoTest {

  @Test
  public void testMapEntityType() {
    EntityType entityType = mock(EntityType.class);
    EntityType parentType = mock(EntityType.class);
    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);
    when(tag1.getId()).thenReturn("tag1");
    when(tag2.getId()).thenReturn("tag2");
    Package pack = mock(Package.class);
    when(pack.getId()).thenReturn("packageId");

    when(parentType.getId()).thenReturn("parentId");
    when(entityType.getTags()).thenReturn(newArrayList(tag1, tag2));
    doReturn("Elastic").when(entityType).get(EntityTypeMetadata.BACKEND);
    doReturn("Human Readable").when(entityType).get(EntityTypeMetadata.LABEL);
    doReturn("Description").when(entityType).get(EntityTypeMetadata.DESCRIPTION);
    doReturn(false).when(entityType).get(EntityTypeMetadata.IS_ABSTRACT);
    when(entityType.getExtends()).thenReturn(parentType);
    when(entityType.getPackage()).thenReturn(pack);
    when(entityType.getId()).thenReturn("packageId_ID");

    List<Object> expected =
        newArrayList(
            "ID",
            "packageId",
            "Human Readable",
            "Description",
            "false",
            "parentId",
            "Elastic",
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
            null);
    List<Object> actual = EntityTypeMapper.map(entityType);
    assertEquals(actual, expected);
  }

  @Test
  public void testMapEntityTypeNotExtending() {
    EntityType entityType = mock(EntityType.class);
    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);
    when(tag1.getId()).thenReturn("tag1");
    when(tag2.getId()).thenReturn("tag2");
    Package pack = mock(Package.class);
    when(pack.getId()).thenReturn("packageId");

    when(entityType.getTags()).thenReturn(newArrayList(tag1, tag2));
    doReturn("Elastic").when(entityType).get(EntityTypeMetadata.BACKEND);
    doReturn("Human Readable").when(entityType).get(EntityTypeMetadata.LABEL);
    doReturn(null).when(entityType).get(EntityTypeMetadata.DESCRIPTION);
    doReturn(false).when(entityType).get(EntityTypeMetadata.IS_ABSTRACT);
    when(entityType.getExtends()).thenReturn(null);
    when(entityType.getPackage()).thenReturn(pack);
    when(entityType.getId()).thenReturn("packageId_ID");

    List<Object> expected =
        newArrayList(
            "ID",
            "packageId",
            "Human Readable",
            null,
            "false",
            null,
            "Elastic",
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
            null);
    List<Object> actual = EntityTypeMapper.map(entityType);
    assertEquals(actual, expected);
  }

  @Test
  public void testMapEntityTypeI18N() {
    EntityType entityType = mock(EntityType.class);
    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);
    when(tag1.getId()).thenReturn("tag1");
    when(tag2.getId()).thenReturn("tag2");
    Package pack = mock(Package.class);
    when(pack.getId()).thenReturn("packageId");

    when(entityType.getTags()).thenReturn(newArrayList(tag1, tag2));
    doReturn("Elastic").when(entityType).get(EntityTypeMetadata.BACKEND);
    doReturn("Human Readable").when(entityType).get(EntityTypeMetadata.LABEL);
    doReturn("Dutch Label").when(entityType).get("labelNl");
    doReturn("Dutch description").when(entityType).get("descriptionNl");
    doReturn("English Label").when(entityType).get("labelEn");
    doReturn("English description").when(entityType).get("descriptionEn");
    doReturn("German Label").when(entityType).get("labelDe");
    doReturn("German description").when(entityType).get("descriptionDe");
    doReturn("Italian Label").when(entityType).get("labelIt");
    doReturn("Italian description").when(entityType).get("descriptionIt");
    doReturn("Portugese Label").when(entityType).get("labelPt");
    doReturn("Portugese description").when(entityType).get("descriptionPt");
    doReturn("Spanish Label").when(entityType).get("labelEs");
    doReturn("Spanish description").when(entityType).get("descriptionEs");
    doReturn("French Label").when(entityType).get("labelFr");
    doReturn("French description").when(entityType).get("descriptionFr");
    doReturn("xx Label").when(entityType).get("labelXx");
    doReturn("xx description").when(entityType).get("descriptionXx");
    doReturn(null).when(entityType).get(EntityTypeMetadata.DESCRIPTION);
    doReturn(false).when(entityType).get(EntityTypeMetadata.IS_ABSTRACT);
    when(entityType.getExtends()).thenReturn(null);
    when(entityType.getPackage()).thenReturn(pack);
    when(entityType.getId()).thenReturn("packageId_ID");

    List<Object> expected =
        newArrayList(
            "ID",
            "packageId",
            "Human Readable",
            null,
            "false",
            null,
            "Elastic",
            "tag1,tag2",
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
    List<Object> actual = EntityTypeMapper.map(entityType);
    assertEquals(actual, expected);
  }

  @Test
  public void testGet() {
    EntityType entityType = mock(EntityType.class);
    Attribute attr1 = mock(Attribute.class);
    when(attr1.getName()).thenReturn("attribute 1");
    when(attr1.getDataType()).thenReturn(AttributeType.STRING);
    Attribute attr2 = mock(Attribute.class);
    when(attr2.getDataType()).thenReturn(AttributeType.COMPOUND);
    Attribute attr3 = mock(Attribute.class);
    when(attr3.getName()).thenReturn("attribute 3");
    when(attr3.getDataType()).thenReturn(AttributeType.STRING);
    when(entityType.getAttributes()).thenReturn(Arrays.asList(attr1, attr2, attr3));
    assertEquals(
        EntityTypeMapper.getHeaders(entityType), Arrays.asList("attribute 1", "attribute 3"));
  }
}
