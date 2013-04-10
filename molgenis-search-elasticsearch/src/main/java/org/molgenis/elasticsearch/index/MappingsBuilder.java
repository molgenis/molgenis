package org.molgenis.elasticsearch.index;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.molgenis.util.tuple.Tuple;

/**
 * Builds mappings for a documentType. For each column a multi_field is created,
 * one analyzed for searching and one not_analyzed for sorting
 * 
 * @author erwin
 * 
 */
public class MappingsBuilder
{
	public static XContentBuilder buildMapping(String documentType, Tuple tuple) throws IOException
	{
		XContentBuilder jsonBuilder = XContentFactory.jsonBuilder().startObject().startObject(documentType)
				.startObject("properties");

		for (String columnName : tuple.getColNames())
		{
			Object value = tuple.get(columnName);

			if (value instanceof String)
			{
				jsonBuilder.startObject(columnName).field("type", "multi_field").startObject("fields")
						.startObject(columnName).field("type", "string").endObject().startObject("sort")
						.field("type", "string").field("index", "not_analyzed").endObject().endObject().endObject();

			}
			else
			{
				String type = getType(value);
				jsonBuilder.startObject(columnName).field("type", "multi_field").startObject("fields")
						.startObject(columnName).field("type", type).endObject().startObject("sort")
						.field("type", type).endObject().endObject().endObject();

			}

		}

		jsonBuilder.endObject().endObject().endObject();

		return jsonBuilder;
	}

	/*
	 * Gets the elasticsearch field type
	 */
	private static String getType(Object value)
	{
		if (value == null)
		{
			return "string";
		}

		if (value instanceof Integer)
		{
			return "integer";
		}

		if (value instanceof Long)
		{
			return "long";
		}

		if (value instanceof Float)
		{
			return "float";
		}

		if (value instanceof Double)
		{
			return "double";
		}

		if (value instanceof Boolean)
		{
			return "boolean";
		}

		return "string";
	}

}
