package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class RoleRepositoryDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<Role, RoleMetadata> {

  private final CachedRoleHierarchy cachedRoleHierarchy;

  public RoleRepositoryDecoratorFactory(
      RoleMetadata roleMetadata, CachedRoleHierarchy cachedRoleHierarchy) {
    super(roleMetadata);
    this.cachedRoleHierarchy = requireNonNull(cachedRoleHierarchy);
  }

  @Override
  public Repository<Role> createDecoratedRepository(Repository<Role> repository) {
    return new RoleRepositoryDecorator(repository, cachedRoleHierarchy);
  }
}
