package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.security.core.model.GroupValue;
import org.springframework.stereotype.Component;

@Component
public class GroupFactory extends AbstractSystemEntityFactory<Group, GroupMetadata, String> {
  GroupFactory(GroupMetadata groupMetadata, EntityPopulator entityPopulator) {
    super(Group.class, groupMetadata, entityPopulator);
  }

  public Group create(GroupValue groupValue) {
    Group group = create();
    group.setName(groupValue.getName());
    group.setLabel(groupValue.getLabel());
    group.setDescription(groupValue.getDescription());
    group.setPublic(groupValue.isPublic());
    return group;
  }
}
