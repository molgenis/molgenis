package org.molgenis.data.annotation.resources.impl;

import java.io.File;
import java.io.IOException;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotator.tabix.TabixRepository;
import org.molgenis.data.meta.EntityMetaDataImpl;

/**
 * Factory that can instantiate a TabixRepository. The metadata for the repository are configured in the factory.
 */
public class TabixRepositoryFactory implements RepositoryFactory
{
	private EntityMetaDataImpl emd;

	public TabixRepositoryFactory(EntityMetaDataImpl emd)
	{
		this.emd = emd;
	}

	@Override
	public Repository<Entity> createRepository(File file) throws IOException
	{
		return new TabixRepository(file, emd);
	}

}
