package org.molgenis.elasticsearch.index;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.model.elements.Field;

/**
 * Builds mappings for a documentType. For each column a multi_field is created,
 * one analyzed for searching and one not_analyzed for sorting
 * 
 * @author erwin
 * 
 */
public class MappingsBuilder
{
	public static XContentBuilder buildMapping(String documentType, TupleTable tupleTable) throws IOException,
			TableException
	{
		XContentBuilder jsonBuilder = XContentFactory.jsonBuilder().startObject().startObject(documentType)
				.startObject("properties");

		for (Field field : tupleTable.getAllColumns())
		{
			String esType = getType(field.getType());
			if (esType.equals("string"))
			{

				jsonBuilder.startObject(field.getName()).field("type", "multi_field").startObject("fields")
						.startObject(field.getName()).field("type", "string").endObject().startObject("sort")
						.field("type", "string").field("index", "not_analyzed").endObject().endObject().endObject();

			}
			else
			{
				jsonBuilder.startObject(field.getName()).field("type", "multi_field").startObject("fields")
						.startObject(field.getName()).field("type", esType).endObject().startObject("sort")
						.field("type", esType).endObject().endObject().endObject();

			}
		}

		jsonBuilder.endObject().endObject().endObject();

		return jsonBuilder;
	}

	/*
	 * Gets the elasticsearch field type
	 * 
	 * Posible FieldTypes: BOOL, CHAR, DATE, DATE_TIME, DECIMAL, ENUM, EMAIL,
	 * FILE, FREEMARKER, HEXA, HYPERLINK, IMAGE, INT, LIST, LONG, MREF, STRING,
	 * TEXT, LONGTEXT, XREF, CATEGORICAL, UNKNOWN, RICHTEXT
	 */
	private static String getType(FieldType fieldType)
	{
		switch (fieldType.getEnumType())
		{
			case BOOL:
				return "boolean";
			case INT:
				return "integer";
			case LONG:
				return "long";
			case DECIMAL:
				return "double";
			default:
				return "string";

		}
	}
}
