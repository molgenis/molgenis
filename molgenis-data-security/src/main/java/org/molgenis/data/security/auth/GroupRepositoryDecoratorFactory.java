package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Component;

@Component
public class GroupRepositoryDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<Group, GroupMetadata> {

  private final DataService dataService;
  private final MutableAclService mutableAclService;

  public GroupRepositoryDecoratorFactory(
      GroupMetadata groupMetadata, DataService dataService, MutableAclService mutableAclService) {
    super(groupMetadata);
    this.dataService = requireNonNull(dataService);
    this.mutableAclService = requireNonNull(mutableAclService);
  }

  @Override
  public Repository<Group> createDecoratedRepository(Repository<Group> repository) {
    return new GroupRepositoryDecorator(repository, dataService, mutableAclService);
  }
}
