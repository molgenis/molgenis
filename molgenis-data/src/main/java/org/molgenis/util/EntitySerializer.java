package org.molgenis.util;

import com.google.gson.*;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;

import java.lang.reflect.Type;
import java.util.Date;

import static java.lang.String.format;
import static org.molgenis.MolgenisFieldTypes.AttributeType;

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
		result.add("__labelValue", context.serialize(entity.getLabelValue()));
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
				AttributeType attrType = attr.getDataType();
				switch (attrType)
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
					case ONE_TO_MANY:
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
					case COMPOUND:
						throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
					default:
						throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
				}
			}
		}
		return result;
	}
}
