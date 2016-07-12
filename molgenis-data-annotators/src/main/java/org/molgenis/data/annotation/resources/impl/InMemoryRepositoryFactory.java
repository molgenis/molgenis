package org.molgenis.data.annotation.resources.impl;

import org.molgenis.data.Repository;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.importer.MetaDataParser;
import org.molgenis.data.mem.InMemoryRepository;
import org.molgenis.data.meta.DefaultPackage;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;

import java.io.File;
import java.io.IOException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * Factory that can instantiate a InMemoryRepository.
 */
public class InMemoryRepositoryFactory implements RepositoryFactory
{
	private final AttributeMetaDataFactory attributeMetaDataFactory;
	private final EntityMetaDataFactory entityMetaDataFactory;

	private final String name;

	private ExcelRepositoryCollection repositoryCollection = null;
	private final MetaDataParser parser;

	public InMemoryRepositoryFactory(String name, MetaDataParser parser, EntityMetaDataFactory entityMetaDataFactory,
			AttributeMetaDataFactory attributeMetaDataFactory)
	{
		this.name = name;
		this.parser = parser;
		this.attributeMetaDataFactory = attributeMetaDataFactory;
		this.entityMetaDataFactory = entityMetaDataFactory;
	}

	@Override
	public Repository createRepository(File file) throws IOException
	{
		try
		{
			repositoryCollection = new ExcelRepositoryCollection(file);
			repositoryCollection.setAttributeMetaDataFactory(attributeMetaDataFactory);
			repositoryCollection.setEntityMetaDataFactory(entityMetaDataFactory);
		}
		catch (Exception e)
		{
			throw new RuntimeException(
					"Unable to create ExcelRepositoryCollection for file:" + file.getName() + " exception: " + e);
		}
		// FIXME nullpointers
		EntityMetaData metaData = parser.parse(repositoryCollection, DefaultPackage.PACKAGE_DEFAULT).getEntityMap()
				.get(name);
		InMemoryRepository inMemoryRepository = new InMemoryRepository(metaData);
		inMemoryRepository.add(StreamSupport.stream(Spliterators
						.spliteratorUnknownSize(repositoryCollection.getRepository(name).iterator(), Spliterator.ORDERED),
				false));
		return inMemoryRepository;
	}

}
