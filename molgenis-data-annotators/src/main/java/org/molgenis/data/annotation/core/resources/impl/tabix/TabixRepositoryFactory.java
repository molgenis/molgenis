package org.molgenis.data.annotation.core.resources.impl.tabix;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.meta.model.EntityMetaData;

import java.io.File;
import java.io.IOException;

/**
 * Factory that can instantiate a TabixRepository. The metadata for the repository are configured in the factory.
 */
public class TabixRepositoryFactory implements RepositoryFactory
{
	private EntityMetaData emd;

	public TabixRepositoryFactory(EntityMetaData emd)
	{
		this.emd = emd;
	}

	@Override
	public Repository<Entity> createRepository(File file) throws IOException
	{
		return new TabixRepository(file, emd);
	}

}
