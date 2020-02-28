package org.molgenis.data.security.auth;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.security.exception.CircularRoleHierarchyException;

public class RoleRepositoryDecorator extends AbstractRepositoryDecorator<Role> {

  private final CachedRoleHierarchy cachedRoleHierarchy;

  RoleRepositoryDecorator(
      Repository<Role> delegateRepository, CachedRoleHierarchy cachedRoleHierarchy) {
    super(delegateRepository);
    this.cachedRoleHierarchy = requireNonNull(cachedRoleHierarchy);
  }

  @Override
  public void add(Role role) {
    validateInclusion(role);
    clearRoleHierarchyCache();
    super.add(role);
  }

  @Override
  public Integer add(Stream<Role> roleStream) {
    roleStream = validateInclusion(roleStream);
    clearRoleHierarchyCache();
    return super.add(roleStream);
  }

  @Override
  public void update(Role role) {
    validateInclusion(role);
    clearRoleHierarchyCache();
    super.update(role);
  }

  @Override
  public void update(Stream<Role> roleStream) {
    roleStream = validateInclusion(roleStream);
    clearRoleHierarchyCache();
    super.update(roleStream);
  }

  @Override
  public void deleteAll() {
    clearRoleHierarchyCache();
    super.deleteAll();
  }

  @Override
  public void deleteById(Object id) {
    clearRoleHierarchyCache();
    super.deleteById(id);
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    clearRoleHierarchyCache();
    super.deleteAll(ids);
  }

  @Override
  public void delete(Role role) {
    clearRoleHierarchyCache();
    super.delete(role);
  }

  @Override
  public void delete(Stream<Role> roleStream) {
    clearRoleHierarchyCache();
    super.delete(roleStream);
  }

  private void clearRoleHierarchyCache() {
    cachedRoleHierarchy.markRoleHierarchyCacheDirty();
  }

  private Stream<Role> validateInclusion(Stream<Role> roles) {
    return roles.filter(
        role -> {
          validateInclusion(role);
          return true;
        });
  }

  private void validateInclusion(Role role) {
    role.getIncludes()
        .forEach(
            includedRole -> {
              if (hasCircularHierarchy(role, includedRole)) {
                throw new CircularRoleHierarchyException(role);
              }
            });
  }

  private boolean hasCircularHierarchy(Role originalRole, Role includedRole) {
    if (originalRole.getId().equals(includedRole.getId())) {
      return true;
    } else {
      return stream(includedRole.getIncludes())
          .anyMatch(role -> hasCircularHierarchy(originalRole, role));
    }
  }
}
