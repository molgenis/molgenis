package org.molgenis.data.support;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.convert.DateToStringConverter;
import org.springframework.beans.BeanUtils;

public abstract class AbstractEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getLabelValue()
	{
		AttributeMetaData labelAttribute = getEntityMetaData().getLabelAttribute();
		String labelAttributeName = labelAttribute.getName();
		FieldTypeEnum dataType = labelAttribute.getDataType().getEnumType();
		switch (dataType)
		{
			case BOOL:
			case DECIMAL:
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case INT:
			case LONG:
			case SCRIPT:
			case STRING:
			case TEXT:
				Object obj = get(labelAttributeName);
				return obj != null ? obj.toString() : null;
			case DATE:
			case DATE_TIME:
				Date date = getUtilDate(labelAttributeName);
				return new DateToStringConverter().convert(date);
			case CATEGORICAL:
			case XREF:
			case FILE:
				Entity refEntity = getEntity(labelAttributeName);
				return refEntity != null ? refEntity.getLabelValue() : null;
			case CATEGORICAL_MREF:
			case MREF:
				Iterable<Entity> refEntities = getEntities(labelAttributeName);
				if (refEntities != null)
				{
					StringBuilder strBuilder = new StringBuilder();
					for (Entity mrefEntity : refEntities)
					{
						if (strBuilder.length() > 0) strBuilder.append(',');
						strBuilder.append(mrefEntity.getLabelValue());
					}
					return strBuilder.toString();
				}
				return null;
			case COMPOUND:
			case IMAGE:
				throw new RuntimeException("invalid label data type " + dataType);
			default:
				throw new RuntimeException("unsupported label data type " + dataType);
		}
	}

	@Override
	public String getString(String attributeName)
	{
		return DataConverter.toString(get(attributeName));
	}

	@Override
	public Integer getInt(String attributeName)
	{
		return DataConverter.toInt(get(attributeName));
	}

	@Override
	public Long getLong(String attributeName)
	{
		return DataConverter.toLong(get(attributeName));
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return DataConverter.toBoolean(get(attributeName));
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return DataConverter.toDouble(get(attributeName));
	}

	@Override
	public java.sql.Date getDate(String attributeName)
	{
		return DataConverter.toDate(get(attributeName));
	}

	@Override
	public java.util.Date getUtilDate(String attributeName)
	{
		return DataConverter.toUtilDate(get(attributeName));
	}

	@Override
	public Timestamp getTimestamp(String attributeName)
	{
		return DataConverter.toTimestamp(get(attributeName));
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		return DataConverter.toEntity(get(attributeName));
	}

	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		Entity entity = getEntity(attributeName);
		return entity != null ? new ConvertingIterable<E>(clazz, Arrays.asList(entity), null).iterator().next() : null;
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		return DataConverter.toEntities(get(attributeName));
	}

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		Iterable<Entity> entities = getEntities(attributeName);
		return entities != null ? new ConvertingIterable<E>(clazz, entities, null) : null;
	}

	@Override
	public List<String> getList(String attributeName)
	{
		return DataConverter.toList(get(attributeName));
	}

	@Override
	public List<Integer> getIntList(String attributeName)
	{
		return DataConverter.toIntList(get(attributeName));
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName() + "{");
		for (String attrName : this.getAttributeNames())
		{
			sb.append(attrName + "='" + this.get(attrName) + "', ");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append("}");
		return sb.toString();
	}

	public static boolean isObjectRepresentation(String objStr)
	{
		int left = objStr.indexOf('(');
		int right = objStr.lastIndexOf(')');
		return (left == -1 || right == -1) ? false : true;
	}

	public static <T extends Entity> T setValuesFromString(String objStr, Class<T> klass)
	{
		T result = BeanUtils.instantiateClass(klass);

		int left = objStr.indexOf('(');
		int right = objStr.lastIndexOf(')');

		String content = objStr.substring(left + 1, right);

		String[] attrValues = content.split(" ");
		for (String attrValue : attrValues)
		{
			String[] av = attrValue.split("=");
			String attr = av[0];
			String value = av[1];
			if (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'')
			{
				value = value.substring(1, value.length() - 1);
			}
			result.set(attr, value);
		}
		return result;
	}
}
