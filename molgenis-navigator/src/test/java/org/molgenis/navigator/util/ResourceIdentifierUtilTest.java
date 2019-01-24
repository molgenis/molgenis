package org.molgenis.navigator.util;

import static com.google.common.collect.Lists.newArrayList;
import static org.testng.Assert.assertEquals;

import java.util.List;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.ResourceType;
import org.testng.annotations.Test;

public class ResourceIdentifierUtilTest {

  @Test
  public void testGetResourcesFromJson() {
    String json =
        "[{'id':'idValue1','type':'ENTITY_TYPE_ABSTRACT'},{'id':'idValue2','type':'PACKAGE'}]";
    ResourceIdentifier expected1 =
        ResourceIdentifier.create(ResourceType.ENTITY_TYPE_ABSTRACT, "idValue1");
    ResourceIdentifier expected2 = ResourceIdentifier.create(ResourceType.PACKAGE, "idValue2");
    List<ResourceIdentifier> expected = newArrayList(expected1, expected2);
    assertEquals(ResourceIdentifierUtil.getResourcesFromJson(json), expected);
  }
}
