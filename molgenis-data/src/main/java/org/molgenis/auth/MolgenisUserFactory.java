package org.molgenis.auth;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MolgenisUserFactory extends AbstractEntityFactory<MolgenisUser, MolgenisUserMetaData, String>
{
	@Autowired
	MolgenisUserFactory(MolgenisUserMetaData molgenisUserMetaData)
	{
		super(MolgenisUser.class, molgenisUserMetaData, String.class);
	}
}
