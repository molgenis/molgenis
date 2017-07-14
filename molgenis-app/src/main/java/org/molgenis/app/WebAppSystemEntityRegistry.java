package org.molgenis.app;

import org.molgenis.auth.GroupAuthorityFactory;
import org.molgenis.auth.UserAuthorityFactory;
import org.molgenis.bootstrap.populate.SystemEntityRegistry;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

import static java.util.Objects.requireNonNull;

/**
 * Registry of application system entities to be added to an empty database.
 */
@Component
public class WebAppSystemEntityRegistry implements SystemEntityRegistry
{
	private final DataService dataService;
	private final UserAuthorityFactory userAuthorityFactory;
	private final GroupAuthorityFactory groupAuthorityFactory;

	@Autowired
	public WebAppSystemEntityRegistry(DataService dataService, UserAuthorityFactory userAuthorityFactory,
			GroupAuthorityFactory groupAuthorityFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.groupAuthorityFactory = requireNonNull(groupAuthorityFactory);
	}

	@Override
	public Collection<Entity> getEntities()
	{
		// FIXME use EntityAclService
		return Collections.emptyList();
	}
}
