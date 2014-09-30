package org.molgenis.data.mysql;

import static org.molgenis.data.mysql.AttributeMetaDataMetaData.AGGREGATEABLE;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.AUTO;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.DATA_TYPE;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.ENTITY;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.ENUM_OPTIONS;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.LABEL;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.LABEL_ATTRIBUTE;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.LOOKUP_ATTRIBUTE;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.NAME;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.NILLABLE;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.RANGE_MAX;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.RANGE_MIN;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.READ_ONLY;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.REF_ENTITY;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.UNIQUE;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.VISIBLE;

import java.util.List;

import javax.sql.DataSource;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Range;
import org.molgenis.data.meta.AttributeMetaDataRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.EnumField;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class MysqlAttributeMetaDataRepository extends MysqlRepository implements AttributeMetaDataRepository
{
	public static final AttributeMetaDataMetaData META_DATA = new AttributeMetaDataMetaData();

	public MysqlAttributeMetaDataRepository(DataSource dataSource)
	{
		super(dataSource);
		setMetaData(META_DATA);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.mysql.AttributeMetaDataRepository#getEntityAttributeMetaData(java.lang.String)
	 */
	@Override
	public Iterable<AttributeMetaData> getEntityAttributeMetaData(String entityName)
	{
		List<AttributeMetaData> attributes = Lists.newArrayList();
		for (Entity entity : findAll(new QueryImpl().eq(ENTITY, entityName)))
		{
			attributes.add(toAttributeMetaData(entity));
		}

		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.mysql.AttributeMetaDataRepository#addAttributeMetaData(java.lang.String,
	 * org.molgenis.data.AttributeMetaData)
	 */
	@Override
	public void addAttributeMetaData(String entityName, AttributeMetaData att)
	{
		Entity attributeMetaDataEntity = new MapEntity();
		attributeMetaDataEntity.set(ENTITY, entityName);
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

		if (att.getRefEntity() != null) attributeMetaDataEntity.set(REF_ENTITY, att.getRefEntity().getName());

		add(attributeMetaDataEntity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.data.mysql.AttributeMetaDataRepository#removeAttributeMetaData(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void removeAttributeMetaData(String entityName, String attributeName)
	{
		Query q = new QueryImpl().eq(AttributeMetaDataMetaData.ENTITY, entityName).and()
				.eq(AttributeMetaDataMetaData.NAME, attributeName);
		Entity entity = findOne(q);
		if (entity != null)
		{
			delete(entity);
		}
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
		attributeMetaData.setReadOnly(entity.getBoolean(UNIQUE) == null ? false : entity.getBoolean(UNIQUE));

		Long rangeMin = entity.getLong(RANGE_MIN);
		Long rangeMax = entity.getLong(RANGE_MAX);
		if ((rangeMin != null) || (rangeMax != null))
		{
			attributeMetaData.setRange(new Range(rangeMin, rangeMax));
		}

		return attributeMetaData;
	}
}