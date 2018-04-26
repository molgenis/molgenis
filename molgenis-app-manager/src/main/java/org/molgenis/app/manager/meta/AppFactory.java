package org.molgenis.app.manager.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class AppFactory extends AbstractSystemEntityFactory<App, AppMetadata, String>
{
	AppFactory(AppMetadata appMetadata, EntityPopulator entityPopulator)
	{
		super(App.class, appMetadata, entityPopulator);
	}
}

