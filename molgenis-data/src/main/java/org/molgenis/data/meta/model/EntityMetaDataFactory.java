package org.molgenis.data.meta.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityMetaDataFactory extends AbstractSystemEntityFactory<EntityMetaData, EntityMetaDataMetaData, String>
{
	@Autowired
	EntityMetaDataFactory(EntityMetaDataMetaData entityMetaMeta, EntityPopulator entityPopulator)
	{
		super(EntityMetaData.class, entityMetaMeta, entityPopulator);
	}
}
