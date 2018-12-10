package org.molgenis.data.export;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

import java.util.List;
import org.molgenis.data.export.mapper.PackageMapper;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.Test;

public class PackageMapperTest extends AbstractMockitoTest {

  @Test
  public void testMapPackage() {
    Package pack = mock(Package.class);
    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);
    doReturn("tag1").when(tag1).getId();
    doReturn(("tag2")).when(tag2).getId();

    Package parent = mock(Package.class);
    doReturn("parentId").when(parent).getId();
    doReturn(newArrayList(tag1, tag2)).when(pack).getTags();
    doReturn(parent).when(pack).getParent();
    doReturn("packId").when(pack).get(PackageMetadata.ID);
    doReturn("Description").when(pack).get(PackageMetadata.DESCRIPTION);
    doReturn("Label").when(pack).get(PackageMetadata.LABEL);
    List<Object> expected = newArrayList("packId", "Label", "Description", "parentId", "tag1,tag2");
    List<Object> actual = PackageMapper.map(pack);
    assertEquals(actual, expected);
  }
}
