package org.molgenis.util;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Serializer for concrete Entity subclasses. This allows you to return Entities in your Controllers, without having to
 * create DTO's with explicit fields for {@link Gson} serialization.
 */
public class EntitySerializer implements JsonSerializer<Entity>
{
	@Override
	public JsonElement serialize(Entity entity, Type type, JsonSerializationContext context)
	{
		JsonObject result = new JsonObject();
		result.addProperty("__entityName", entity.getEntityMetaData().getName());
		for (AttributeMetaData attr : entity.getEntityMetaData().getAtomicAttributes())
		{
			String attributeName = attr.getName();
			Object value = entity.get(attributeName);
			if (value != null)
			{
				switch (attr.getDataType().getEnumType())
				{
					case BOOL:
						result.addProperty(attributeName, entity.getBoolean(attributeName));
						break;
					case CATEGORICAL:
					case XREF:
					case FILE:
						// TODO: or just refuse it for now?
						Entity refEntity = entity.getEntity(attributeName);
						result.add(attributeName, context.serialize(refEntity));
						break;
					case CATEGORICAL_MREF:
					case MREF:
						List<Entity> refEntities = Lists.newArrayList(entity.getEntities(attributeName));
						result.add(attributeName, context.serialize(refEntities));
						break;
					case DATE:
					case DATE_TIME:
						Date dateValue = entity.getUtilDate(attributeName);
						result.add(attributeName, context.serialize(dateValue));
						break;
					case DECIMAL:
						result.addProperty(attributeName, entity.getDouble(attributeName));
						break;
					case INT:
						result.addProperty(attributeName, entity.getInt(attributeName));
						break;
					case LONG:
						result.addProperty(attributeName, entity.getLong(attributeName));
						break;
					case ENUM:
					case HTML:
					case HYPERLINK:
					case SCRIPT:
					case EMAIL:
					case STRING:
					case TEXT:
						result.addProperty(attributeName, value.toString());
						break;
					default:
						throw new IllegalArgumentException("Unknown datatype!");
				}
			}
		}
		return result;
	}
}