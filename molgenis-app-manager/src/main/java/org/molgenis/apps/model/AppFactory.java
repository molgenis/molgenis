package org.molgenis.apps.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class AppFactory extends AbstractSystemEntityFactory<App, AppMetaData, String>
{
	AppFactory(AppMetaData appMetaData, EntityPopulator entityPopulator)
	{
		super(App.class, appMetaData, entityPopulator);
	}
}

