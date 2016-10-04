package org.molgenis.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MolgenisGroupFactory extends AbstractSystemEntityFactory<MolgenisGroup, MolgenisGroupMetaData, String>
{
	@Autowired
	MolgenisGroupFactory(MolgenisGroupMetaData molgenisGroupMetaData, EntityPopulator entityPopulator)
	{
		super(MolgenisGroup.class, molgenisGroupMetaData, entityPopulator);
	}
}
