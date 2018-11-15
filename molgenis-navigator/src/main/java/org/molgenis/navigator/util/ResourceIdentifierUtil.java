package org.molgenis.navigator.util;

import static com.google.common.collect.Lists.newArrayList;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.navigator.model.ResourceType;

public class ResourceIdentifierUtil {

  private ResourceIdentifierUtil() {}

  public static List<ResourceIdentifier> getResourcesFromJson(String resourceJson) {
    Gson gson = new Gson();
    List<ResourceIdentifier> resources = newArrayList();

    @SuppressWarnings("unchecked")
    List<Map<String, String>> jsonList = gson.fromJson(resourceJson, List.class);
    for (Map<String, String> jsonResource : jsonList) {
      resources.add(
          ResourceIdentifier.create(
              ResourceType.valueOf(jsonResource.get("type")), jsonResource.get("id")));
    }
    return resources;
  }
}
