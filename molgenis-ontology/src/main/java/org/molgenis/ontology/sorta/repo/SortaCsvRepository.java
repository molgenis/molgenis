package org.molgenis.ontology.sorta.repo;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.LowerCaseProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.ontology.sorta.service.impl.SortaServiceImpl;

public class SortaCsvRepository extends AbstractRepository
{
	private EntityMetaData entityMetaData = null;
	private final CsvRepository csvRepository;
	private final String entityName;
	private final String entityLabel;
	public final static String ALLOWED_IDENTIFIER = "Identifier";
	private final static List<CellProcessor> LOWERCASE_AND_TRIM = Arrays.asList(new LowerCaseProcessor(),
			new TrimProcessor());

	public SortaCsvRepository(File file, EntityMetaDataFactory entityMetaFactory,
			AttributeMetaDataFactory attrMetaFactory)
	{
		this.csvRepository = new CsvRepository(file, entityMetaFactory, attrMetaFactory, LOWERCASE_AND_TRIM,
				SortaServiceImpl.DEFAULT_SEPARATOR);
		this.entityName = file.getName();
		this.entityLabel = file.getName();
	}

	public SortaCsvRepository(String entityName, String entityLabel, File uploadedFile,
			EntityMetaDataFactory entityMetaFactory, AttributeMetaDataFactory attrMetaFactory)
	{
		this.csvRepository = new CsvRepository(uploadedFile, entityMetaFactory, attrMetaFactory, LOWERCASE_AND_TRIM,
				SortaServiceImpl.DEFAULT_SEPARATOR);
		this.entityName = entityName;
		this.entityLabel = entityLabel;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (entityMetaData == null)
		{
			AttributeMetaDataFactory attrMetaFactory = getApplicationContext().getBean(AttributeMetaDataFactory.class);

			entityMetaData = EntityMetaData.newInstance(csvRepository.getEntityMetaData());
			entityMetaData.setName(entityName);
			entityMetaData.setLabel(entityLabel);
			entityMetaData
					.addAttribute(attrMetaFactory.create().setName(ALLOWED_IDENTIFIER).setNillable(false), ROLE_ID);
			AttributeMetaData nameAttribute = entityMetaData.getAttribute(SortaServiceImpl.DEFAULT_MATCHING_NAME_FIELD);
			if (nameAttribute != null)
			{
				entityMetaData.setLabelAttribute(nameAttribute);
			}
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
				if (isEmpty(entity.getString(ALLOWED_IDENTIFIER)))
				{
					DynamicEntity dynamicEntity = new DynamicEntity(
							null); // FIXME pass entity meta data instead of null
					dynamicEntity.set(entity);
					entity = dynamicEntity;
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