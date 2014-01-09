package org.molgenis.elasticsearch.index;

import java.io.IOException;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.elasticsearch.util.MapperTypeSanitizer;

/**
 * Builds mappings for a documentType. For each column a multi_field is created, one analyzed for searching and one
 * not_analyzed for sorting
 * 
 * @author erwin
 * 
 */
public class MappingsBuilder
{
	public static XContentBuilder buildMapping(Repository<? extends Entity> repository) throws IOException
	{
		String documentType = MapperTypeSanitizer.sanitizeMapperType(repository.getName());
		XContentBuilder jsonBuilder = XContentFactory.jsonBuilder().startObject().startObject(documentType)
				.startObject("properties");

		for (AttributeMetaData attr : repository.getAttributes())
		{
			String esType = getType(attr);
			if (esType.equals("string"))
			{

				jsonBuilder.startObject(attr.getName()).field("type", "multi_field").startObject("fields")
						.startObject(attr.getName()).field("type", "string").endObject().startObject("sort")
						.field("type", "string").field("index", "not_analyzed").endObject().endObject().endObject();

			}
			else if (esType.equals("date"))
			{
				String dateFormat;
				if (attr.getDataType().getEnumType() == FieldTypeEnum.DATE) dateFormat = "date"; // yyyy-MM-dd
				else if (attr.getDataType().getEnumType() == FieldTypeEnum.DATE_TIME) dateFormat = "date_time_no_millis"; // yyyy-MM-dd’T’HH:mm:ssZZ
				else
				{
					throw new MolgenisDataException("invalid molgenis field type for elasticsearch date format ["
							+ attr.getDataType().getEnumType() + "]");
				}

				jsonBuilder.startObject(attr.getName()).field("type", "multi_field").startObject("fields")
						.startObject(attr.getName()).field("type", "date").endObject().startObject("sort")
						.field("type", "date").field("format", dateFormat).endObject().endObject().endObject();
			}
			else
			{
				jsonBuilder.startObject(attr.getName()).field("type", "multi_field").startObject("fields")
						.startObject(attr.getName()).field("type", esType).endObject().startObject("sort")
						.field("type", esType).endObject().endObject().endObject();

			}
		}

		jsonBuilder.endObject().endObject().endObject();

		return jsonBuilder;
	}

	private static String getType(AttributeMetaData attr)
	{
		FieldTypeEnum enumType = attr.getDataType().getEnumType();
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
			case HTML:
			case HYPERLINK:
			case STRING:
			case TEXT:
				return "string";
			case MREF:
			case XREF:
			{
				// return type of referenced label field
				return getType(attr.getRefEntity().getLabelAttribute());
			}
			case FILE:
			case IMAGE:
				throw new ElasticSearchException("indexing of molgenis field type [" + enumType + "] not supported");
			default:
				return "string";
		}
	}
}
