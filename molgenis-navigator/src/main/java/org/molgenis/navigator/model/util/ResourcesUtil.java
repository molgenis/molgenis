package org.molgenis.navigator.model.util;

import static com.google.common.collect.Lists.newArrayList;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import org.molgenis.navigator.model.Resource;
import org.molgenis.navigator.model.ResourceType;

public class ResourcesUtil {
  public static List<Resource> getResourcesFromJson(String resourceJson) {
    Gson gson = new Gson();
    List<Resource> resources = newArrayList();
    List<Map<String, String>> jsonList = gson.fromJson(resourceJson, List.class);
    for (Map<String, String> jsonResource : jsonList) {
      resources.add(
          Resource.create(
              ResourceType.valueOf(jsonResource.get("type")),
              jsonResource.get("id"),
              jsonResource.get("label"),
              jsonResource.get("description"),
              Boolean.valueOf(jsonResource.get("hidden")),
              Boolean.valueOf(jsonResource.get("readonly"))));
    }
    return resources;
  }
}
