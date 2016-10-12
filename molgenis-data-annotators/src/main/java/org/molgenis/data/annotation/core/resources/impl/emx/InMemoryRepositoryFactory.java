package org.molgenis.data.annotation.core.resources.impl.emx;

import com.google.common.collect.ImmutableMap;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.importer.MetaDataParser;
import org.molgenis.data.mem.InMemoryRepository;
import org.molgenis.data.meta.DefaultPackage;
import org.molgenis.data.meta.model.AttributeFactory;
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
	private final AttributeFactory attributeFactory;
	private final EntityMetaDataFactory entityMetaDataFactory;

	private final String name;

	private ExcelRepositoryCollection repositoryCollection = null;
	private final MetaDataParser parser;

	public InMemoryRepositoryFactory(String name, MetaDataParser parser, EntityMetaDataFactory entityMetaDataFactory,
			AttributeFactory attributeFactory)
	{
		this.name = name;
		this.parser = parser;
		this.attributeFactory = attributeFactory;
		this.entityMetaDataFactory = entityMetaDataFactory;
	}

	@Override
	public Repository createRepository(File file) throws IOException
	{
		try
		{
			repositoryCollection = new ExcelRepositoryCollection(file);
			repositoryCollection.setAttributeMetaDataFactory(attributeFactory);
			repositoryCollection.setEntityMetaDataFactory(entityMetaDataFactory);
		}
		catch (Exception e)
		{
			throw new RuntimeException(
					"Unable to create ExcelRepositoryCollection for file:" + file.getName() + " exception: " + e);
		}

		ImmutableMap<String, EntityMetaData> entityMap = parser
				.parse(repositoryCollection, DefaultPackage.PACKAGE_DEFAULT).getEntityMap();
		if (!entityMap.containsKey(name))
		{
			throw new RuntimeException("Entity [" + name + "] is not found. Entities found: " + entityMap.keySet());
		}

		EntityMetaData metaData = entityMap.get(name);
		InMemoryRepository inMemoryRepository = new InMemoryRepository(metaData);
		inMemoryRepository.add(StreamSupport.stream(Spliterators
						.spliteratorUnknownSize(repositoryCollection.getRepository(name).iterator(), Spliterator.ORDERED),
				false));
		return inMemoryRepository;
	}
}
