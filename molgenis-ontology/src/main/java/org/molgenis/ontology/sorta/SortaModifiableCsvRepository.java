package org.molgenis.ontology.sorta;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

public class SortaModifiableCsvRepository extends AbstractRepository
{
	private DefaultEntityMetaData entityMetaData = null;
	private final CsvRepository csvRepository;
	private final String entityName;
	public static final String ALLOWED_IDENTIFIER = "Identifier";

	public SortaModifiableCsvRepository(String entityName, CsvRepository csvRepository)
	{
		this.csvRepository = csvRepository;
		this.entityName = entityName;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (entityMetaData == null)
		{
			entityMetaData = new DefaultEntityMetaData(entityName, csvRepository.getEntityMetaData());
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ALLOWED_IDENTIFIER).setIdAttribute(true)
					.setNillable(false));
		}
		return entityMetaData;
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
				if (StringUtils.isEmpty(entity.getString(ALLOWED_IDENTIFIER)))
				{
					entity = new MapEntity(entity);
					entity.set(ALLOWED_IDENTIFIER, count.incrementAndGet());
				}
				return entity;
			}
		};
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Collections.emptySet();
	}

	@Override
	public long count()
	{
		return csvRepository.count();
	}
}