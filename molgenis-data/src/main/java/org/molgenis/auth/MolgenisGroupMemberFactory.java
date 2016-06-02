package org.molgenis.auth;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MolgenisGroupMemberFactory
		extends AbstractEntityFactory<MolgenisGroupMember, MolgenisGroupMemberMetaData, String>
{
	@Autowired
	MolgenisGroupMemberFactory(MolgenisGroupMemberMetaData molgenisGroupMemberMetaData)
	{
		super(MolgenisGroupMember.class, molgenisGroupMemberMetaData, String.class);
	}
}
