package org.molgenis.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MolgenisUserFactory extends AbstractSystemEntityFactory<MolgenisUser, MolgenisUserMetaData, String>
{
	@Autowired
	MolgenisUserFactory(MolgenisUserMetaData molgenisUserMetaData, EntityPopulator entityPopulator)
	{
		super(MolgenisUser.class, molgenisUserMetaData, entityPopulator);
	}
}
