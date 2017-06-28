package org.molgenis.data.security;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AclTestFactory extends AbstractSystemEntityFactory<AclTest, AclTestMetadata, Long>
{
	@Autowired
	AclTestFactory(AclTestMetadata aclTestMetadata, EntityPopulator entityPopulator)
	{
		super(AclTest.class, aclTestMetadata, entityPopulator);
	}
}