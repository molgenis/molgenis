package org.molgenis.data.elasticsearch.index;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.elasticsearch.util.MapperTypeSanitizer;

/**
 * Builds mappings for a documentType. For each column a multi_field is created, one analyzed for searching and one
 * not_analyzed for sorting
 * 
 * @author erwin
 * 
 */
public class MappingsBuilder
{
	public static final String FIELD_NOT_ANALYZED = "raw";
	public static final String FIELD_NGRAM_ANALYZED = "ngram";

	/**
	 * Creates entity meta data for the given repository, documents are stored in the index
	 * 
	 * @param repository
	 * @return
	 * @throws IOException
	 */
	public static XContentBuilder buildMapping(Repository repository) throws IOException
	{
		return buildMapping(repository.getEntityMetaData());
	}

	/**
	 * Creates entity meta data for the given repository
	 * 
	 * @deprecated see buildMapping(EntityMetaData)
	 * 
	 * @param repository
	 * @param storeSource
	 *            whether or not documents are stored in the index
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static XContentBuilder buildMapping(Repository repository, boolean storeSource, boolean enableNorms,
			boolean createAllIndex) throws IOException
	{
		return buildMapping(repository.getEntityMetaData(), storeSource, enableNorms, createAllIndex);
	}

	/**
	 * Creates a Elasticsearch mapping for the given entity meta data, documents are stored in the index
	 * 
	 * @param entityMetaData
	 * @return
	 * @throws IOException
	 */
	public static XContentBuilder buildMapping(EntityMetaData entityMetaData) throws IOException
	{
		return buildMapping(entityMetaData, true, true, true);
	}

	/**
	 * Creates a Elasticsearch mapping for the given entity meta data
	 * 
	 * @param entityMetaData
	 * @param storeSource
	 *            whether or not documents are stored in the index
	 * @return
	 * @throws IOException
	 */
	public static XContentBuilder buildMapping(EntityMetaData entityMetaData, boolean storeSource, boolean enableNorms,
			boolean createAllIndex) throws IOException
	{
		String documentType = MapperTypeSanitizer.sanitizeMapperType(entityMetaData.getName());
		XContentBuilder jsonBuilder = XContentFactory.jsonBuilder().startObject().startObject(documentType);

		jsonBuilder.startObject("_source").field("enabled", storeSource).endObject();

		jsonBuilder.startObject("properties");

		jsonBuilder.startObject(ElasticsearchService.CRUD_TYPE_FIELD_NAME);
		jsonBuilder.field("type", "string").field("index", "not_analyzed");
		jsonBuilder.endObject();

		for (AttributeMetaData attr : entityMetaData.getAtomicAttributes())
		{
			createAttributeMapping(attr, enableNorms, createAllIndex, true, true, jsonBuilder);
		}
		jsonBuilder.endObject();

		jsonBuilder.endObject().endObject();

		return jsonBuilder;
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
		FieldTypeEnum dataType = attr.getDataType().getEnumType();
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
			case MREF:
			case XREF:
			case FILE:
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
							false, jsonBuilder);
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
			case TEXT:
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
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}

		jsonBuilder.field("include_in_all", createAllIndex && attr.isVisible());
	}

}
