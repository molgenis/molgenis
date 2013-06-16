package org.molgenis.elasticsearch.index;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Field;

/**
 * Builds mappings for a documentType. For each column a multi_field is created, one analyzed for searching and one
 * not_analyzed for sorting
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
			String esType = getType(field);
			if (esType.equals("string"))
			{

				jsonBuilder.startObject(field.getName()).field("type", "multi_field").startObject("fields")
						.startObject(field.getName()).field("type", "string").endObject().startObject("sort")
						.field("type", "string").field("index", "not_analyzed").endObject().endObject().endObject();

			}
			else if (esType.equals("date"))
			{
				String dateFormat;
				if (field.getType().getEnumType() == FieldTypeEnum.DATE) dateFormat = "date"; // yyyy-MM-dd
				else if (field.getType().getEnumType() == FieldTypeEnum.DATE_TIME) dateFormat = "date_time_no_millis"; // yyyy-MM-dd’T’HH:mm:ssZZ
				else
				{
					throw new TableException("invalid molgenis field type for elasticsearch date format ["
							+ field.getType().getEnumType() + "]");
				}

				jsonBuilder.startObject(field.getName()).field("type", "multi_field").startObject("fields")
						.startObject(field.getName()).field("type", "date").endObject().startObject("sort")
						.field("type", "date").field("format", dateFormat).endObject().endObject().endObject();
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

	/**
	 * Gets the elasticsearch field type for a molgenis field type
	 * 
	 * @throws TableException
	 */
	private static String getType(Field field) throws TableException
	{
		FieldTypeEnum enumType = field.getType().getEnumType();
		switch (enumType)
		{
			case BOOL:
				return "boolean";
			case DATE:
			case DATE_TIME:
				return "date";
			case DECIMAL:
				return "double";
			case INT:
				return "integer";
			case LONG:
				return "long";
			case CATEGORICAL:
			case EMAIL:
			case ENUM:
			case HYPERLINK:
			case STRING:
			case TEXT:
				return "string";
			case MREF:
			case XREF:
			{
				try
				{
					// return type of referenced field
					return getType(field.getXrefField());
				}
				catch (MolgenisModelException e)
				{
					throw new RuntimeException(e);
				}
			}
			case FILE:
			case IMAGE:
				throw new TableException("indexing of molgenis field type [" + enumType + "] not supported");
			default:
				return "string";
		}
	}
}
