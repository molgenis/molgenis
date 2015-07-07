package org.molgenis.data.annotation.resources.impl;

import java.io.File;
import java.io.IOException;

import org.molgenis.data.Repository;
import org.molgenis.data.annotation.resources.RepositoryFactory;
import org.molgenis.data.annotator.tabix.TabixVcfRepository;

/**
 * Factory that can create a {@link TabixVcfRepository}. The name of the repository is configured in the factory.
 */
public class TabixVcfRepositoryFactory implements RepositoryFactory
{
	private final String name;

	public TabixVcfRepositoryFactory(String name)
	{
		this.name = name;
	}

	@Override
	public Repository createRepository(File file) throws IOException
	{
		return new TabixVcfRepository(file, name);
	}

}
