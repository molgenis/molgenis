package org.molgenis.security.twofactor.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class UserSecretFactory
    extends AbstractSystemEntityFactory<UserSecret, UserSecretMetadata, String> {
  UserSecretFactory(UserSecretMetadata userSecretMetaData, EntityPopulator entityPopulator) {
    super(UserSecret.class, userSecretMetaData, entityPopulator);
  }
}
