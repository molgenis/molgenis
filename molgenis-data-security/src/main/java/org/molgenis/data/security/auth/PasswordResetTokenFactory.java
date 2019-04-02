package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenFactory
    extends AbstractSystemEntityFactory<PasswordResetToken, PasswordResetTokenMetadata, String> {

  PasswordResetTokenFactory(
      PasswordResetTokenMetadata passwordResetTokenMetadata, EntityPopulator entityPopulator) {
    super(PasswordResetToken.class, passwordResetTokenMetadata, entityPopulator);
  }
}
