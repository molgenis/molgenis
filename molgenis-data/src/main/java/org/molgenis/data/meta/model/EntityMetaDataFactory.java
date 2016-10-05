package org.molgenis.data.meta.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityMetaDataFactory extends AbstractSystemEntityFactory<EntityMetaData, EntityTypeMetadata, String>
{
	@Autowired
	EntityMetaDataFactory(EntityTypeMetadata entityMetaMeta, EntityPopulator entityPopulator)
	{
		super(EntityMetaData.class, entityMetaMeta, entityPopulator);
	}
}
