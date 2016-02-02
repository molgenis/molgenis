package org.molgenis.ontology.matching;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

public class AdaptedCsvRepository extends AbstractRepository
{
	private final CsvRepository csvRepository;
	private final String entityName;
	public static final String ALLOWED_IDENTIFIER = "Identifier";

	public AdaptedCsvRepository(String entityName, CsvRepository csvRepository)
	{
		this.csvRepository = csvRepository;
		this.entityName = entityName;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		DefaultEntityMetaData defaultEntityMetaData = new DefaultEntityMetaData(entityName);
		for (AttributeMetaData attributeMetaData : csvRepository.getEntityMetaData().getAttributes())
		{
			if (!ALLOWED_IDENTIFIER.equalsIgnoreCase(attributeMetaData.getName()))
			{
				defaultEntityMetaData.addAttributeMetaData(attributeMetaData);
			}
		}

		DefaultAttributeMetaData idAttribute = new DefaultAttributeMetaData(ALLOWED_IDENTIFIER);
		defaultEntityMetaData.addAttributeMetaData(idAttribute, ROLE_ID);

		return defaultEntityMetaData;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		final AtomicInteger count = new AtomicInteger(0);
		final Iterator<Entity> iterator = csvRepository.iterator();
		return new Iterator<Entity>()
		{
			@Override
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			@Override
			public Entity next()
			{
				Entity entity = iterator.next();
				if (entity.get(ALLOWED_IDENTIFIER) == null)
				{
					MapEntity mapEntity = new MapEntity();
					mapEntity.set(ALLOWED_IDENTIFIER, count.incrementAndGet());
					for (String attributeName : entity.getAttributeNames())
					{
						mapEntity.set(attributeName, entity.get(attributeName));
					}
					return mapEntity;
				}
				return entity;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public void close() throws IOException
	{
		csvRepository.close();
	}

	@Override
	public String getName()
	{
		return entityName;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Collections.emptySet();
	}

}
