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
	@Override
	public Collection<Entity> getEntities()
	{
		return Collections.emptyList();
	}
}
