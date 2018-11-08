package org.molgenis.navigator.resource;

import static com.google.common.collect.Lists.newArrayList;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import org.molgenis.navigator.resource.Resource.Type;

public class ResourcesUtil {
  public static List<Resource> getResourcesFromJson(String resourceJson) {
    Gson gson = new Gson();
    List<Resource> resources = newArrayList();
    List<Map<String, String>> jsonList = gson.fromJson(resourceJson, List.class);
    for (Map<String, String> jsonResource : jsonList) {
      resources.add(
          Resource.create(
              Type.valueOf(jsonResource.get("type")), jsonResource.get("id"), null, null));
    }
    return resources;
  }
}
