package org.molgenis.auth;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MolgenisTokenFactory extends AbstractEntityFactory<MolgenisToken, MolgenisTokenMetaData, String>
{
	@Autowired
	MolgenisTokenFactory(MolgenisTokenMetaData molgenisTokenMetaData)
	{
		super(MolgenisToken.class, molgenisTokenMetaData, String.class);
	}
}
