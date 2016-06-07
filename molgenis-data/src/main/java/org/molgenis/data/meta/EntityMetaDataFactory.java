package org.molgenis.data.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityMetaDataFactory extends AbstractSystemEntityFactory<EntityMetaData, EntityMetaDataMetaData, String>
{
	@Autowired
	EntityMetaDataFactory(EntityMetaDataMetaData entityMetaMeta)
	{
		super(EntityMetaData.class, entityMetaMeta, String.class);
	}
}
