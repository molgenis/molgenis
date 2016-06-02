package org.molgenis.auth;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MolgenisGroupFactory extends AbstractEntityFactory<MolgenisGroup, MolgenisGroupMetaData, String>
{
	@Autowired
	MolgenisGroupFactory(MolgenisGroupMetaData molgenisGroupMetaData)
	{
		super(MolgenisGroup.class, molgenisGroupMetaData, String.class);
	}
}
