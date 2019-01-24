package org.molgenis.bootstrap.populate;

import static com.google.common.collect.Multimaps.filterEntries;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Multimap;
import java.util.Collection;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.util.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Discovers {@link PermissionRegistry application system permission registries} and populates
 * permissions.
 */
@Component
public class PermissionPopulator {
  private final PermissionService permissionService;

  PermissionPopulator(PermissionService permissionService) {
    this.permissionService = requireNonNull(permissionService);
  }

  @Transactional
  public void populate(ApplicationContext applicationContext) {
    Collection<PermissionRegistry> registries =
        applicationContext.getBeansOfType(PermissionRegistry.class).values();
    registries.forEach(this::populate);
  }

  private void populate(PermissionRegistry systemPermissionRegistry) {
    Multimap<ObjectIdentity, Pair<PermissionSet, Sid>> systemPermissions =
        systemPermissionRegistry.getPermissions();

    Multimap<ObjectIdentity, Pair<PermissionSet, Sid>> newSystemPermissions =
        filterEntries(
            systemPermissions,
            entry -> entry != null && isNewPermission(entry.getKey(), entry.getValue()));

    newSystemPermissions.asMap().forEach(this::populate);
  }

  private boolean isNewPermission(
      ObjectIdentity objectIdentity, Pair<PermissionSet, Sid> permission) {
    return !permissionService.exists(objectIdentity, permission.getB());
  }

  private void populate(ObjectIdentity objectIdentity, Collection<Pair<PermissionSet, Sid>> pairs) {
    pairs.forEach(pair -> permissionService.grant(objectIdentity, pair.getA(), pair.getB()));
  }
}
