package org.molgenis.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MolgenisTokenFactory extends AbstractSystemEntityFactory<MolgenisToken, MolgenisTokenMetaData, String>
{
	@Autowired
	MolgenisTokenFactory(MolgenisTokenMetaData molgenisTokenMetaData, EntityPopulator entityPopulator)
	{
		super(MolgenisToken.class, molgenisTokenMetaData, entityPopulator);
	}
}
