package org.molgenis.oneclickimporter.service.Impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.DefaultPackage;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.oneclickimporter.model.Column;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.AttributeTypeService;
import org.molgenis.oneclickimporter.service.EntityService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.EntityManager.CreationMode.NO_POPULATE;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;

@Component
public class EntityServiceImpl implements EntityService
{
	private static final String ID_ATTR_NAME = "auto_identifier";

	private final DefaultPackage defaultPackage;

	private final EntityTypeFactory entityTypeFactory;

	private final AttributeFactory attributeFactory;

	private final IdGenerator idGenerator;

	private final DataService dataService;

	private final MetaDataService metaDataService;

	private final EntityManager entityManager;

	private final AttributeTypeService attributeTypeService;

	public EntityServiceImpl(DefaultPackage defaultPackage, EntityTypeFactory entityTypeFactory,
			AttributeFactory attributeFactory, IdGenerator idGenerator, DataService dataService,
			MetaDataService metaDataService, EntityManager entityManager, AttributeTypeService attributeTypeService)
	{
		this.defaultPackage = requireNonNull(defaultPackage);
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
		this.attributeFactory = requireNonNull(attributeFactory);
		this.idGenerator = requireNonNull(idGenerator);
		this.dataService = requireNonNull(dataService);
		this.metaDataService = requireNonNull(metaDataService);
		this.entityManager = requireNonNull(entityManager);
		this.attributeTypeService = requireNonNull(attributeTypeService);
	}

	@Override
	public EntityType createEntityType(DataCollection dataCollection)
	{
		String entityTypeId = idGenerator.generateId();

		// Create a dataTable
		EntityType entityType = entityTypeFactory.create();
		entityType.setPackage(defaultPackage);
		entityType.setId(entityTypeId);
		entityType.setLabel(dataCollection.getName());

		// Create and add an auto id column to the dataTable
		entityType.addAttribute(createIdAttribute(), ROLE_ID);

		// Add all other columns the dataTable
		dataCollection.getColumns().forEach(column ->
		{
			Attribute attribute = createAttribute(column);
			entityType.addAttribute(attribute);
		});

		// Store the dataTable (metadata only)
		metaDataService.addEntityType(entityType);

		AtomicInteger rowIndex = new AtomicInteger(0);
		List<Entity> rows = newArrayList();
		while (rowIndex.get() < dataCollection.getColumns().get(0).getDataValues().size())
		{
			Entity row = entityManager.create(entityType, NO_POPULATE);
			row.setIdValue(idGenerator.generateId());
			int index = rowIndex.getAndIncrement();
			dataCollection.getColumns().forEach(column -> setRowValueForAttribute(row, index, column));

			rows.add(row);
		}
		dataService.add(entityType.getId(), rows.stream());

		return entityType;
	}

	private void setRowValueForAttribute(Entity row, int index, Column column)
	{
		String attributeName = asValidAttributeName(column.getName());
		Object dataValue = column.getDataValues().get(index);

		EntityType rowType = row.getEntityType();
		Attribute attribute = rowType.getAttribute(attributeName);

		row.set(attributeName, castValueAsAttributeType(dataValue, attribute.getDataType()));
	}

	private Object castValueAsAttributeType(Object value, AttributeType type)
	{
		if (value == null)
		{
			return value;
		}

		switch (type)
		{
			case DATE:
				if (!(value instanceof LocalDate))
				{
					value = LocalDate.parse(value.toString());
				}
				break;
			default:
				break;

		}
		return value;
	}

	private Attribute createIdAttribute()
	{
		Attribute idAttribute = attributeFactory.create();
		idAttribute.setName(ID_ATTR_NAME);
		idAttribute.setVisible(false);
		idAttribute.setAuto(true);
		idAttribute.setIdAttribute(true);
		return idAttribute;
	}

	private Attribute createAttribute(Column column)
	{
		Attribute attribute = attributeFactory.create();
		attribute.setName(asValidAttributeName(column.getName()));
		attribute.setLabel(column.getName());
		attribute.setDataType(attributeTypeService.guessAttributeType(column.getDataValues()));
		return attribute;
	}

	private String asValidAttributeName(String columnName)
	{
		return columnName.replace(" ", "_");
	}
}
