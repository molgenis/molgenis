package org.molgenis.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenFactory extends AbstractSystemEntityFactory<Token, TokenMetaData, String>
{
	@Autowired
	TokenFactory(TokenMetaData tokenMetaData, EntityPopulator entityPopulator)
	{
		super(Token.class, tokenMetaData, entityPopulator);
	}
}
