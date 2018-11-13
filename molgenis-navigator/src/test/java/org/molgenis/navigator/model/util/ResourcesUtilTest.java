package org.molgenis.navigator.model.util;

import static com.google.common.collect.Lists.newArrayList;
import static org.testng.Assert.*;

import java.util.List;
import org.molgenis.navigator.model.Resource;
import org.molgenis.navigator.model.ResourceType;
import org.molgenis.navigator.model.ResourcesUtil;
import org.testng.annotations.Test;

public class ResourcesUtilTest {

  @Test
  public void testGetResourcesFromJson() {
    String json =
        "[{'id':'idValue1','type':'ENTITY_TYPE_ABSTRACT','label':'labelValue1','description':'descriptionValue1','hidden':'false','readonly':'false'},{'id':'idValue2','type':'PACKAGE','label':'labelValue2','hidden':'true','readonly':'true'}]";
    Resource expected1 =
        Resource.create(
            ResourceType.ENTITY_TYPE_ABSTRACT,
            "idValue1",
            "labelValue1",
            "descriptionValue1",
            false,
            false);
    Resource expected2 =
        Resource.create(ResourceType.PACKAGE, "idValue2", "labelValue2", null, true, true);
    List<Resource> expected = newArrayList(expected1, expected2);
    assertEquals(ResourcesUtil.getResourcesFromJson(json), expected);
  }
}
