package org.molgenis.data.annotation.resources.impl;

import java.io.File;
import java.io.IOException;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.annotator.tabix.TabixRepository;

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
	public Repository createRepository(File file) throws IOException
	{
		return new TabixRepository(file, emd);
	}

}
