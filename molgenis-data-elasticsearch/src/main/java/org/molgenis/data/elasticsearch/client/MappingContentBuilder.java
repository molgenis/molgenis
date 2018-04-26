package org.molgenis.data.elasticsearch.client;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.molgenis.data.elasticsearch.generator.model.FieldMapping;
import org.molgenis.data.elasticsearch.generator.model.Mapping;
import org.molgenis.util.UnexpectedEnumException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.elasticsearch.FieldConstants.FIELD_NOT_ANALYZED;

/**
 * Creates Elasticsearch transport client content for mappings.
 */
class MappingContentBuilder
{
	private final XContentType xContentType;

	MappingContentBuilder()
	{
		this(XContentType.JSON);
	}

	MappingContentBuilder(XContentType xContentType)
	{
		this.xContentType = requireNonNull(xContentType);
	}

	XContentBuilder createMapping(Mapping mapping)
	{
		try (XContentBuilder contentBuilder = XContentFactory.contentBuilder(xContentType))
		{
			createMapping(mapping, contentBuilder);
			return contentBuilder;
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private void createMapping(Mapping mapping, XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.startObject();
		contentBuilder.startObject("_source").field("enabled", false).endObject();
		createFieldMappings(mapping.getFieldMappings(), contentBuilder);
		contentBuilder.endObject();
	}

	private void createFieldMappings(List<FieldMapping> fieldMappings, XContentBuilder contentBuilder)
			throws IOException
	{
		contentBuilder.startObject("properties");
		fieldMappings.forEach(fieldMapping ->
		{
			try
			{
				createFieldMapping(fieldMapping, contentBuilder);
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		});
		contentBuilder.endObject();
	}

	private void createFieldMapping(FieldMapping fieldMapping, XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.startObject(fieldMapping.getName());
		switch (fieldMapping.getType())
		{
			case BOOLEAN:
				createFieldMapping("boolean", contentBuilder);
				break;
			case DATE:
				createFieldMappingDate("date", contentBuilder);
				break;
			case DATE_TIME:
				createFieldMappingDate("date_time_no_millis", contentBuilder);
				break;
			case DOUBLE:
				createFieldMapping("double", contentBuilder);
				break;
			case INTEGER:
				createFieldMappingInteger(contentBuilder);
				break;
			case LONG:
				createFieldMapping("long", contentBuilder);
				break;
			case NESTED:
				createFieldMappingNested(fieldMapping.getNestedFieldMappings(), contentBuilder);
				break;
			case TEXT:
				createFieldMappingText(contentBuilder);
				break;
			default:
				throw new UnexpectedEnumException(fieldMapping.getType());
		}
		contentBuilder.endObject();
	}

	private void createFieldMapping(String type, XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.field("type", type);
	}

	private void createFieldMappingDate(String dateFormat, XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.field("type", "date").field("format", dateFormat);
		// not-analyzed field for aggregation
		// note: the norms settings defaults to false for not_analyzed fields
		contentBuilder.startObject("fields")
					  .startObject(FIELD_NOT_ANALYZED)
					  .field("type", "keyword")
					  .field("index", true)
					  .endObject()
					  .endObject();
	}

	private void createFieldMappingInteger(XContentBuilder contentBuilder) throws IOException
	{
		contentBuilder.field("type", "integer");
		// Fix sorting by using disk-based "fielddata" instead of in-memory "fielddata"
		contentBuilder.field("doc_values", true);
	}

	private void createFieldMappingNested(List<FieldMapping> nestedFieldMappings, XContentBuilder contentBuilder)
			throws IOException
	{
		contentBuilder.field("type", "nested");
		createFieldMappings(nestedFieldMappings, contentBuilder);
	}

	private void createFieldMappingText(XContentBuilder contentBuilder) throws IOException
	{
		// enable/disable norms based on given value
		contentBuilder.field("type", "text");
		contentBuilder.field("norms", true);
		// not-analyzed field for sorting and wildcard queries
		// note: the norms settings defaults to false for not_analyzed fields
		XContentBuilder fieldsObject = contentBuilder.startObject("fields")
													 .startObject(FIELD_NOT_ANALYZED)
													 .field("type", "keyword")
													 .field("index", true)
													 .endObject();
		fieldsObject.endObject();
	}
}
