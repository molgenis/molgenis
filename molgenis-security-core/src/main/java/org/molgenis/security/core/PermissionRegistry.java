package org.molgenis.security.core;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PermissionRegistry {
  private SetMultimap<Permission, PermissionSet> mapping = HashMultimap.create();

  public void addMapping(Permission permission, PermissionSet... permissionSet) {
    mapping.putAll(permission, asList(permissionSet));
  }

  public Set<PermissionSet> getPermissions(Permission permission) {
    return mapping.get(permission);
  }

  public Map<PermissionSet, Set<Permission>> getPermissionSets() {
    return mapping
        .entries()
        .stream()
        .collect(groupingBy(Entry::getValue, LinkedHashMap::new, mapping(Entry::getKey, toSet())));
  }
}
