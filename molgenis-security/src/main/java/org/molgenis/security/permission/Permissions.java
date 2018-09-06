package org.molgenis.security.permission;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@AutoValue
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Permissions {
  public abstract Set<String> getIds();

  public abstract Map<String, Collection<String>> getPermissions();

  public static Permissions create(Set<String> ids, Multimap<String, String> permissions) {
    return new AutoValue_Permissions(ids, ImmutableMultimap.copyOf(permissions).asMap());
  }
}
