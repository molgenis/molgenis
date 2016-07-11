package org.molgenis.auth;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MolgenisGroupMemberFactory
		extends AbstractSystemEntityFactory<MolgenisGroupMember, MolgenisGroupMemberMetaData, String>
{
	@Autowired
	MolgenisGroupMemberFactory(MolgenisGroupMemberMetaData molgenisGroupMemberMetaData)
	{
		super(MolgenisGroupMember.class, molgenisGroupMemberMetaData);
	}
}
