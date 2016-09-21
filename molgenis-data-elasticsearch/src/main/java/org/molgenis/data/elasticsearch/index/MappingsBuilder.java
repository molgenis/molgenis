package org.molgenis.data.elasticsearch.index;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.elasticsearch.util.MapperTypeSanitizer;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;

import java.io.IOException;

import static java.lang.String.format;

/**
 * Builds mappings for a documentType. For each column a multi_field is created, one analyzed for searching and one
 * not_analyzed for sorting
 *
 * @author erwin
 */
public class MappingsBuilder
{
	public static final String FIELD_NOT_ANALYZED = "raw";
	public static final String FIELD_NGRAM_ANALYZED = "ngram";

	private MappingsBuilder()
	{
	}

	/**
	 * Creates a Elasticsearch mapping for the given entity meta data
	 *
	 * @param jsonBuilder    {@link XContentBuilder} to write the mapping to
	 * @param entityMetaData {@link EntityMetaData} for the entity to map
	 * @throws IOException writing to JSON builder
	 */
	public static void buildMapping(XContentBuilder jsonBuilder, EntityMetaData entityMetaData, boolean enableNorms,
			boolean createAllIndex) throws IOException
	{
		String documentType = MapperTypeSanitizer.sanitizeMapperType(entityMetaData.getName());
		jsonBuilder.startObject().startObject(documentType);

		jsonBuilder.startObject("_source").field("enabled", false).endObject();

		jsonBuilder.startObject("properties");

		for (AttributeMetaData attr : entityMetaData.getAtomicAttributes())
		{
			createAttributeMapping(attr, enableNorms, createAllIndex, true, true, jsonBuilder);
		}
		jsonBuilder.endObject();

		jsonBuilder.endObject().endObject();
	}

	// TODO discuss: use null_value for nillable attributes?
	private static void createAttributeMapping(AttributeMetaData attr, boolean enableNorms, boolean createAllIndex,
			boolean nestRefs, boolean enableNgramAnalyzer, XContentBuilder jsonBuilder) throws IOException
	{
		String attrName = attr.getName();
		jsonBuilder.startObject(attrName);
		createAttributeMappingContents(attr, enableNorms, createAllIndex, nestRefs, enableNgramAnalyzer, jsonBuilder);
		jsonBuilder.endObject();
	}

	private static void createAttributeMappingContents(AttributeMetaData attr, boolean enableNorms,
			boolean createAllIndex, boolean nestRefs, boolean enableNgramAnalyzer, XContentBuilder jsonBuilder)
			throws IOException
	{
		AttributeType dataType = attr.getDataType();
		switch (dataType)
		{
			case BOOL:
				jsonBuilder.field("type", "boolean");
				// disable norms for numeric fields
				// note: https://github.com/elasticsearch/elasticsearch/issues/5502
				jsonBuilder.field("norms").startObject().field("enabled", false).endObject();
				break;
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case FILE:
			case MREF:
			case ONE_TO_MANY:
			case XREF:
				EntityMetaData refEntity = attr.getRefEntity();
				if (nestRefs)
				{
					jsonBuilder.field("type", "nested");
					jsonBuilder.field("norms").startObject().field("enabled", enableNorms).endObject();
					jsonBuilder.startObject("properties");
					for (AttributeMetaData refAttr : refEntity.getAtomicAttributes())
					{
						createAttributeMapping(refAttr, enableNorms, createAllIndex, false, true, jsonBuilder);
					}
					jsonBuilder.endObject();
				}
				else
				{
					createAttributeMappingContents(refEntity.getLabelAttribute(), enableNorms, createAllIndex, false,
							enableNgramAnalyzer, jsonBuilder);
				}
				break;
			case COMPOUND:
				throw new UnsupportedOperationException();
			case DATE:
				jsonBuilder.field("type", "date").field("format", "date");
				// disable norms for numeric fields
				jsonBuilder.field("norms").startObject().field("enabled", false).endObject();
				// not-analyzed field for aggregation
				// note: the include_in_all setting is ignored on any field that is defined in the fields options
				// note: the norms settings defaults to false for not_analyzed fields
				jsonBuilder.startObject("fields").startObject(FIELD_NOT_ANALYZED).field("type", "string")
						.field("index", "not_analyzed").endObject().endObject();
				break;
			case DATE_TIME:
				jsonBuilder.field("type", "date").field("format", "date_time_no_millis");
				// disable norms for numeric fields
				jsonBuilder.field("norms").startObject().field("enabled", false).endObject();
				// not-analyzed field for aggregation
				// note: the include_in_all setting is ignored on any field that is defined in the fields options
				// note: the norms settings defaults to false for not_analyzed fields
				jsonBuilder.startObject("fields").startObject(FIELD_NOT_ANALYZED).field("type", "string")
						.field("index", "not_analyzed").endObject().endObject();
				break;
			case DECIMAL:
				jsonBuilder.field("type", "double");
				// disable norms for numeric fields
				jsonBuilder.field("norms").startObject().field("enabled", false).endObject();
				break;
			case INT:
				jsonBuilder.field("type", "integer");
				// Fix sorting by using disk-based "fielddata" instead of in-memory "fielddata"
				jsonBuilder.field("doc_values", true);
				// disable norms for numeric fields
				jsonBuilder.field("norms").startObject().field("enabled", false).endObject();
				break;
			case LONG:
				jsonBuilder.field("type", "long");
				// disable norms for numeric fields
				jsonBuilder.field("norms").startObject().field("enabled", false).endObject();
				break;
			case EMAIL:
			case ENUM:
			case HYPERLINK:
			case STRING:
			case TEXT:
				// enable/disable norms based on given value
				jsonBuilder.field("type", "string");
				jsonBuilder.field("norms").startObject().field("enabled", enableNorms).endObject();
				// not-analyzed field for sorting and wildcard queries
				// note: the include_in_all setting is ignored on any field that is defined in the fields options
				// note: the norms settings defaults to false for not_analyzed fields
				XContentBuilder fieldsObject = jsonBuilder.startObject("fields").startObject(FIELD_NOT_ANALYZED)
						.field("type", "string").field("index", "not_analyzed").endObject();
				if (enableNgramAnalyzer)
				{
					// add ngram analyzer (not applied to nested documents)
					fieldsObject.startObject(FIELD_NGRAM_ANALYZED).field("type", "string")
							.field("analyzer", ElasticsearchIndexCreator.NGRAM_ANALYZER).endObject();
				}
				fieldsObject.endObject();
				break;
			case HTML:
			case SCRIPT:
				// enable/disable norms based on given value
				jsonBuilder.field("type", "string");
				jsonBuilder.field("norms").startObject().field("enabled", enableNorms).endObject();
				// not-analyzed field for sorting and wildcard queries
				// note: the include_in_all setting is ignored on any field that is defined in the fields options
				// note: the norms settings defaults to false for not_analyzed fields
				jsonBuilder.startObject("fields").startObject(FIELD_NOT_ANALYZED).field("type", "string")
						.field("index", "not_analyzed").endObject().endObject();
				break;
			default:
				throw new RuntimeException(format("Unknown data type [%s]", dataType.toString()));
		}

		jsonBuilder.field("include_in_all", createAllIndex && attr.isVisible());
	}

}
