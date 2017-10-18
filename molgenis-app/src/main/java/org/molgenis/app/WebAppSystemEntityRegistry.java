package org.molgenis.app;

import org.molgenis.bootstrap.populate.SystemEntityRegistry;
import org.molgenis.data.Entity;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

/**
 * Registry of application system entities to be added to an empty database.
 */
@Component
public class WebAppSystemEntityRegistry implements SystemEntityRegistry
{
	public WebAppSystemEntityRegistry()
	{
	}

	@Override
	public Collection<Entity> getEntities()
	{
		// This was created for security system to fill anonymous user and all users authorities upon startup.
		// Leaving it in place in case other webapps use it for something else.
		return Collections.emptyList();
	}
}
