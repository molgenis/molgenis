package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class TokenFactory extends AbstractSystemEntityFactory<Token, TokenMetadata, String> {

  TokenFactory(TokenMetadata tokenMetaData, EntityPopulator entityPopulator) {
    super(Token.class, tokenMetaData, entityPopulator);
  }
}
