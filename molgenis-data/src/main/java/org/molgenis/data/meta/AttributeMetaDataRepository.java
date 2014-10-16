package org.molgenis.data.meta;

import static org.molgenis.data.meta.AttributeMetaDataMetaData.AGGREGATEABLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.AUTO;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DATA_TYPE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ENTITY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ENUM_OPTIONS;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.IDENTIFIER;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LOOKUP_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.NAME;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.NILLABLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MAX;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MIN;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.READ_ONLY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.REF_ENTITY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.UNIQUE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VISIBLE;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.Query;
import org.molgenis.data.Range;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.EnumField;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

class AttributeMetaDataRepository
{
	public static final AttributeMetaDataMetaData META_DATA = new AttributeMetaDataMetaData();

	private AtomicInteger idCounter = new AtomicInteger();

	private CrudRepository repository;

	private EntityMetaDataRepository entityMetaDataRepository;

	public AttributeMetaDataRepository(ManageableCrudRepositoryCollection collection,
			EntityMetaDataRepository entityMetaDataRepository)
	{
		this.entityMetaDataRepository = entityMetaDataRepository;
		this.repository = collection.add(META_DATA);
	}

	public Iterable<AttributeMetaData> findForEntity(String entityName)
	{
		List<AttributeMetaData> attributes = Lists.newArrayList();
		for (Entity entity : repository.findAll(new QueryImpl().eq(ENTITY, entityName)))
		{
			attributes.add(toAttributeMetaData(entity));
		}

		return attributes;
	}

	public void add(Entity entity, AttributeMetaData att)
	{
		Entity attributeMetaDataEntity = new MapEntity();
		// autoid
		attributeMetaDataEntity.set(IDENTIFIER, idCounter.incrementAndGet());
		attributeMetaDataEntity.set(ENTITY, entity);
		attributeMetaDataEntity.set(NAME, att.getName());
		attributeMetaDataEntity.set(DATA_TYPE, att.getDataType());
		attributeMetaDataEntity.set(ID_ATTRIBUTE, att.isIdAtrribute());
		attributeMetaDataEntity.set(NILLABLE, att.isNillable());
		attributeMetaDataEntity.set(AUTO, att.isAuto());
		attributeMetaDataEntity.set(VISIBLE, att.isVisible());
		attributeMetaDataEntity.set(LABEL, att.getLabel());
		attributeMetaDataEntity.set(DESCRIPTION, att.getDescription());
		attributeMetaDataEntity.set(AGGREGATEABLE, att.isAggregateable());
		attributeMetaDataEntity.set(LOOKUP_ATTRIBUTE, att.isLookupAttribute());
		attributeMetaDataEntity.set(LABEL_ATTRIBUTE, att.isLabelAttribute());
		attributeMetaDataEntity.set(READ_ONLY, att.isReadonly());
		attributeMetaDataEntity.set(UNIQUE, att.isUnique());

		if (att.getDataType() instanceof EnumField)
		{
			attributeMetaDataEntity.set(ENUM_OPTIONS, Joiner.on(",").join(att.getEnumOptions()));
		}

		if (att.getRange() != null)
		{
			attributeMetaDataEntity.set(RANGE_MIN, att.getRange().getMin());
			attributeMetaDataEntity.set(RANGE_MAX, att.getRange().getMax());
		}

		Entity refEntity = entityMetaDataRepository.getEntity(att.getRefEntity().getName());
		if (att.getRefEntity() != null) attributeMetaDataEntity.set(REF_ENTITY, refEntity);

		repository.add(attributeMetaDataEntity);
	}

	public void remove(String entityName, String attributeName)
	{
		Query q = new QueryImpl().eq(AttributeMetaDataMetaData.ENTITY, entityName).and()
				.eq(AttributeMetaDataMetaData.NAME, attributeName);
		Entity entity = repository.findOne(q);
		if (entity != null)
		{
			repository.delete(entity);
		}
	}

	public void deleteAllAttributes(String entityName)
	{
		repository.delete(repository.findAll(new QueryImpl().eq(AttributeMetaDataMetaData.ENTITY, entityName)));
	}

	public void deleteAll()
	{
		repository.deleteAll();
	}

	private DefaultAttributeMetaData toAttributeMetaData(Entity entity)
	{
		DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData(entity.getString(NAME));
		attributeMetaData.setDataType(MolgenisFieldTypes.getType(entity.getString(DATA_TYPE)));
		attributeMetaData.setNillable(entity.getBoolean(NILLABLE));
		attributeMetaData.setAuto(entity.getBoolean(AUTO));
		attributeMetaData.setIdAttribute(entity.getBoolean(ID_ATTRIBUTE));
		attributeMetaData.setLookupAttribute(entity.getBoolean(LOOKUP_ATTRIBUTE));
		attributeMetaData.setVisible(entity.getBoolean(VISIBLE));
		attributeMetaData.setLabel(entity.getString(LABEL));
		attributeMetaData.setDescription(entity.getString(DESCRIPTION));
		attributeMetaData.setAggregateable(entity.getBoolean(AGGREGATEABLE) == null ? false : entity
				.getBoolean(AGGREGATEABLE));
		attributeMetaData.setEnumOptions(entity.getList(ENUM_OPTIONS));
		attributeMetaData.setLabelAttribute(entity.getBoolean(LABEL_ATTRIBUTE) == null ? false : entity
				.getBoolean(LABEL_ATTRIBUTE));
		attributeMetaData.setReadOnly(entity.getBoolean(READ_ONLY) == null ? false : entity.getBoolean(READ_ONLY));
		attributeMetaData.setUnique(entity.getBoolean(UNIQUE) == null ? false : entity.getBoolean(UNIQUE));

		Long rangeMin = entity.getLong(RANGE_MIN);
		Long rangeMax = entity.getLong(RANGE_MAX);
		if ((rangeMin != null) || (rangeMax != null))
		{
			attributeMetaData.setRange(new Range(rangeMin, rangeMax));
		}

		return attributeMetaData;
	}

	Iterable<Entity> getAttributeEntities()
	{
		return repository;
	}
}