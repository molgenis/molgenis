package org.molgenis.security.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class ActionFactory extends AbstractSystemEntityFactory<Action, ActionMetadata, String>
{
	ActionFactory(ActionMetadata actionMetadata, EntityPopulator entityPopulator)
	{
		super(Action.class, actionMetadata, entityPopulator);
	}
}
