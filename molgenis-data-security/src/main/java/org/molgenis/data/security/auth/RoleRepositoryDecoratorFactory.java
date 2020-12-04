package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.molgenis.security.acl.MutableSidService;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class RoleRepositoryDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<Role, RoleMetadata> {

  private final CachedRoleHierarchy cachedRoleHierarchy;
  private final MutableSidService mutableSidService;

  public RoleRepositoryDecoratorFactory(
      RoleMetadata roleMetadata,
      CachedRoleHierarchy cachedRoleHierarchy,
      MutableSidService mutableSidService) {
    super(roleMetadata);
    this.cachedRoleHierarchy = requireNonNull(cachedRoleHierarchy);
    this.mutableSidService = requireNonNull(mutableSidService);
  }

  @Override
  public Repository<Role> createDecoratedRepository(Repository<Role> repository) {
    return new RoleRepositoryDecorator(repository, cachedRoleHierarchy, mutableSidService);
  }
}
