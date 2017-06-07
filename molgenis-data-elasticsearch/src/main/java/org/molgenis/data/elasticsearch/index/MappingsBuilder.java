package org.molgenis.data.elasticsearch.index;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.molgenis.data.elasticsearch.util.DocumentIdGenerator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

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
	 * @param jsonBuilder {@link XContentBuilder} to write the mapping to
	 * @param entityType  {@link EntityType} for the entity to map
	 * @param documentIdGenerator document id generator
	 * @throws IOException writing to JSON builder
	 */
	public static void buildMapping(XContentBuilder jsonBuilder, EntityType entityType,
			DocumentIdGenerator documentIdGenerator, boolean enableNorms,
			boolean createAllIndex) throws IOException
	{
		String docType = documentIdGenerator.generateId(entityType);
		jsonBuilder.startObject().startObject(docType);

		jsonBuilder.startObject("_source").field("enabled", false).endObject();

		jsonBuilder.startObject("properties");

		for (Attribute attr : entityType.getAtomicAttributes())
		{
			createAttributeMapping(attr, documentIdGenerator, enableNorms, createAllIndex, true, true, jsonBuilder);
		}
		jsonBuilder.endObject();

		jsonBuilder.endObject().endObject();
	}

	// TODO discuss: use null_value for nillable attributes?
	private static void createAttributeMapping(Attribute attr, DocumentIdGenerator documentIdGenerator,
			boolean enableNorms, boolean createAllIndex,
			boolean nestRefs, boolean enableNgramAnalyzer, XContentBuilder jsonBuilder) throws IOException
	{
		String attrName = documentIdGenerator.generateId(attr);
		jsonBuilder.startObject(attrName);
		createAttributeMappingContents(attr, documentIdGenerator, enableNorms, createAllIndex, nestRefs,
				enableNgramAnalyzer, jsonBuilder);
		jsonBuilder.endObject();
	}

	private static void createAttributeMappingContents(Attribute attr, DocumentIdGenerator documentIdGenerator,
			boolean enableNorms, boolean createAllIndex,
			boolean nestRefs, boolean enableNgramAnalyzer, XContentBuilder jsonBuilder) throws IOException
	{
		AttributeType dataType = attr.getDataType();
		switch (dataType)
		{
			case BOOL:
				jsonBuilder.field("type", "boolean");
				break;
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case FILE:
			case MREF:
			case ONE_TO_MANY:
			case XREF:
				EntityType refEntity = attr.getRefEntity();
				if (nestRefs)
				{
					jsonBuilder.field("type", "nested");
					jsonBuilder.startObject("properties");
					for (Attribute refAttr : refEntity.getAtomicAttributes())
					{
						createAttributeMapping(refAttr, documentIdGenerator, enableNorms, createAllIndex, false, true,
								jsonBuilder);
					}
					jsonBuilder.endObject();
				}
				else
				{
					createAttributeMappingContents(refEntity.getLabelAttribute(), documentIdGenerator, enableNorms,
							createAllIndex, false,
							enableNgramAnalyzer, jsonBuilder);
				}
				break;
			case COMPOUND:
				throw new UnsupportedOperationException();
			case DATE:
				jsonBuilder.field("type", "date").field("format", "date");
				// not-analyzed field for aggregation
				// note: the include_in_all setting is ignored on any field that is defined in the fields options
				// note: the norms settings defaults to false for not_analyzed fields
				jsonBuilder.startObject("fields").startObject(FIELD_NOT_ANALYZED).field("type", "string")
						.field("index", "not_analyzed").endObject().endObject();
				break;
			case DATE_TIME:
				jsonBuilder.field("type", "date").field("format", "date_time_no_millis");
				// not-analyzed field for aggregation
				// note: the include_in_all setting is ignored on any field that is defined in the fields options
				// note: the norms settings defaults to false for not_analyzed fields
				jsonBuilder.startObject("fields").startObject(FIELD_NOT_ANALYZED).field("type", "string")
						.field("index", "not_analyzed").endObject().endObject();
				break;
			case DECIMAL:
				jsonBuilder.field("type", "double");
				break;
			case INT:
				jsonBuilder.field("type", "integer");
				// Fix sorting by using disk-based "fielddata" instead of in-memory "fielddata"
				jsonBuilder.field("doc_values", true);
				break;
			case LONG:
				jsonBuilder.field("type", "long");
				break;
			case EMAIL:
			case ENUM:
			case HYPERLINK:
			case STRING:
			case TEXT:
				// enable/disable norms based on given value
				jsonBuilder.field("type", "string");
				jsonBuilder.field("norms", enableNorms);
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
				jsonBuilder.field("norms", enableNorms);
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
