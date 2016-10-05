package org.molgenis.data.annotation.core.resources.impl.emx;

import com.google.common.collect.ImmutableMap;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.importer.MetaDataParser;
import org.molgenis.data.mem.InMemoryRepository;
import org.molgenis.data.meta.DefaultPackage;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;

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
	private final EntityTypeFactory entityTypeFactory;

	private final String name;

	private ExcelRepositoryCollection repositoryCollection = null;
	private final MetaDataParser parser;

	public InMemoryRepositoryFactory(String name, MetaDataParser parser, EntityTypeFactory entityTypeFactory,
			AttributeMetaDataFactory attributeMetaDataFactory)
	{
		this.name = name;
		this.parser = parser;
		this.attributeMetaDataFactory = attributeMetaDataFactory;
		this.entityTypeFactory = entityTypeFactory;
	}

	@Override
	public Repository createRepository(File file) throws IOException
	{
		try
		{
			repositoryCollection = new ExcelRepositoryCollection(file);
			repositoryCollection.setAttributeMetaDataFactory(attributeMetaDataFactory);
			repositoryCollection.setEntityTypeFactory(entityTypeFactory);
		}
		catch (Exception e)
		{
			throw new RuntimeException(
					"Unable to create ExcelRepositoryCollection for file:" + file.getName() + " exception: " + e);
		}

		ImmutableMap<String, EntityType> entityMap = parser
				.parse(repositoryCollection, DefaultPackage.PACKAGE_DEFAULT).getEntityMap();
		if (!entityMap.containsKey(name))
		{
			throw new RuntimeException("Entity [" + name + "] is not found. Entities found: " + entityMap.keySet());
		}

		EntityType metaData = entityMap.get(name);
		InMemoryRepository inMemoryRepository = new InMemoryRepository(metaData);
		inMemoryRepository.add(StreamSupport.stream(Spliterators
						.spliteratorUnknownSize(repositoryCollection.getRepository(name).iterator(), Spliterator.ORDERED),
				false));
		return inMemoryRepository;
	}
}
