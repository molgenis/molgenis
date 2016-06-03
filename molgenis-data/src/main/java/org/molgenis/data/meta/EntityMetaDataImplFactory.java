package org.molgenis.data.meta;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityMetaDataImplFactory extends AbstractEntityFactory<EntityMetaDataImpl, EntityMetaDataMetaData, String>
{
	@Autowired
	EntityMetaDataImplFactory(EntityMetaDataMetaData entityMetaMeta)
	{
		super(EntityMetaDataImpl.class, entityMetaMeta, String.class);
	}
}
