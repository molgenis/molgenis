package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class VOGroupFactory extends AbstractSystemEntityFactory<VOGroup, VOGroupMetadata, String> {
  VOGroupFactory(VOGroupMetadata voGroupMetadata, EntityPopulator entityPopulator) {
    super(VOGroup.class, voGroupMetadata, entityPopulator);
  }

  public VOGroup withName(String name) {
    VOGroup group = create();
    group.setName(name);
    return group;
  }
}
