package org.molgenis.navigator.util;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.navigator.util.ResourceIdentifierUtil.getResourcesFromJson;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.ResourceType;

class ResourceIdentifierUtilTest {

  @Test
  void testGetResourcesFromJson() {
    String json =
        "[{'id':'idValue1','type':'ENTITY_TYPE_ABSTRACT'},{'id':'idValue2','type':'PACKAGE'}]";
    ResourceIdentifier expected1 =
        ResourceIdentifier.create(ResourceType.ENTITY_TYPE_ABSTRACT, "idValue1");
    ResourceIdentifier expected2 = ResourceIdentifier.create(ResourceType.PACKAGE, "idValue2");
    List<ResourceIdentifier> expected = newArrayList(expected1, expected2);
    assertEquals(expected, getResourcesFromJson(json));
  }
}
