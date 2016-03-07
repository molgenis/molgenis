package org.molgenis.util;

import java.lang.reflect.Type;
import java.util.Date;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
	private JsonElement serializeReference(Entity entity, JsonSerializationContext context)
	{
		JsonObject result = new JsonObject();
		result.addProperty("__entityName", entity.getEntityMetaData().getName());
		result.add("__idValue", context.serialize(entity.getIdValue()));
		result.addProperty("__labelValue", entity.getLabelValue());
		return result;
	}

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
						Entity refEntity = entity.getEntity(attributeName);
						result.add(attributeName, serializeReference(refEntity, context));
						break;
					case CATEGORICAL_MREF:
					case MREF:
						JsonArray jsonArray = new JsonArray();
						entity.getEntities(attributeName).forEach(e -> jsonArray.add(serializeReference(e, context)));
						result.add(attributeName, jsonArray);
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