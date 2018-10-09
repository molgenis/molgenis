package org.molgenis.data.export;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import java.util.List;
import org.molgenis.data.export.mapper.EntityTypeMapper;
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
            "tag1,tag2");
    List<Object> actual = EntityTypeMapper.map(entityType);
    assertEquals(actual, expected);
  }
}
