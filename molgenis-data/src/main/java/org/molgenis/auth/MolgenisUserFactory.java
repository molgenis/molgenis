package org.molgenis.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MolgenisUserFactory extends AbstractSystemEntityFactory<MolgenisUser, MolgenisUserMetaData, String>
{
	@Autowired
	MolgenisUserFactory(MolgenisUserMetaData molgenisUserMetaData)
	{
		super(MolgenisUser.class, molgenisUserMetaData);
	}
}
