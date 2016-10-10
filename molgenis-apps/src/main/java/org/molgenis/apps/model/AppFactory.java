package org.molgenis.apps.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppFactory extends AbstractSystemEntityFactory<App, AppMetaData, String>
{
	@Autowired
	AppFactory(AppMetaData appMetaData, EntityPopulator entityPopulator)
	{
		super(App.class, appMetaData, entityPopulator);
	}
}

