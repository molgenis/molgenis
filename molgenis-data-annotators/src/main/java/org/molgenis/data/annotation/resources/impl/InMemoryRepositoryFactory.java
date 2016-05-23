package org.molgenis.data.annotation.resources.impl;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.Repository;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.importer.MetaDataParser;
import org.molgenis.data.mem.InMemoryRepository;

import java.io.File;
import java.io.IOException;

/**
 * Factory that can instantiate a InMemoryRepository.
 */
public class InMemoryRepositoryFactory implements RepositoryFactory
{
	private final String name;

	private ExcelRepositoryCollection repositoryCollection = null;
	private final MetaDataParser parser;

	public InMemoryRepositoryFactory(String name, MetaDataParser parser)
	{
		this.name = name;
		this.parser = parser;
	}

	@Override
	public Repository createRepository(File file) throws IOException
	{
		try
		{
			repositoryCollection = new ExcelRepositoryCollection(file);
		}
		catch (Exception e)
		{
			// FIXME better message
			throw new RuntimeException("file is no good");
		}
		// FIXME nullpointers
		EntityMetaData metaData = parser.parse(repositoryCollection, Package.DEFAULT_PACKAGE_NAME).getEntityMap()
				.get(name);
		InMemoryRepository inMemoryRepository = new InMemoryRepository(metaData);
		inMemoryRepository.add(repositoryCollection.getRepository(name).stream());
		return inMemoryRepository;
	}

}
