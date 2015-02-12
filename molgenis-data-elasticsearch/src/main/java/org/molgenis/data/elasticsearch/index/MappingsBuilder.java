package org.molgenis.data.elasticsearch.index;

import static org.molgenis.data.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Range;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.ElasticsearchEntity;
import org.molgenis.data.elasticsearch.util.MapperTypeSanitizer;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

/**
 * Builds mappings for a documentType. For each column a multi_field is created, one analyzed for searching and one
 * not_analyzed for sorting
 * 
 * @author erwin
 * 
 */
public class MappingsBuilder
{
	private static final String ENTITY_NAME = "name";
	private static final String ENTITY_LABEL = "label";
	private static final String ENTITY_DESCRIPTION = "description";
	private static final String ENTITY_EXTENDS = "extends";
	private static final String ENTITY_ATTRIBUTES = "attributes";
	private static final String ENTITY_ABSTRACT = "abstract";
	private static final String ENTITY_ENTITY_CLASS = "entityClass";

	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_LABEL = "label";
	private static final String ATTRIBUTE_DESCRIPTION = "description";
	private static final String ATTRIBUTE_RANGE_MAX = "max";
	private static final String ATTRIBUTE_RANGE_MIN = "min";
	private static final String ATTRIBUTE_RANGE = "range";
	private static final String ATTRIBUTE_AGGREGATEABLE = "aggregateable";
	private static final String ATTRIBUTE_ATTRIBUTE_PARTS = "attributeParts";
	private static final String ATTRIBUTE_REF_ENTITY = "refEntity";
	private static final String ATTRIBUTE_AUTO = "auto";
	private static final String ATTRIBUTE_LOOKUP_ATTRIBUTE = "lookupAttribute";
	private static final String ATTRIBUTE_LABEL_ATTRIBUTE = "labelAttribute";
	private static final String ATTRIBUTE_ID_ATTRIBUTE = "idAttribute";
	private static final String ATTRIBUTE_DEFAULT_VALUE = "defaultValue";
	private static final String ATTRIBUTE_VISIBLE = "visible";
	private static final String ATTRIBUTE_UNIQUE = "unique";
	private static final String ATTRIBUTE_READONLY = "readonly";
	private static final String ATTRIBUTE_NILLABLE = "nillable";
	private static final String ATTRIBUTE_DATA_TYPE = "dataType";

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
		return buildMapping(repository.getEntityMetaData(), storeSource, enableNorms, createAllIndex, false);
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
		return buildMapping(entityMetaData, true, true, true, false);
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
			boolean createAllIndex, boolean storeFullMetadata) throws IOException
	{
		String documentType = MapperTypeSanitizer.sanitizeMapperType(entityMetaData.getName());
		XContentBuilder jsonBuilder = XContentFactory.jsonBuilder().startObject().startObject(documentType)
				.startObject("_source").field("enabled", storeSource).endObject().startObject("properties");

		for (AttributeMetaData attr : entityMetaData.getAtomicAttributes())
		{
			createAttributeMapping(attr, enableNorms, createAllIndex, true, jsonBuilder);
		}

		jsonBuilder.endObject();
		// create custom meta data
		if (storeFullMetadata)
		{
			jsonBuilder.startObject("_meta");
			serializeEntityMeta(entityMetaData, jsonBuilder);
			jsonBuilder.endObject();
		}

		jsonBuilder.endObject().endObject();

		return jsonBuilder;
	}

	// TODO discuss: use null_value for nillable attributes?
	private static void createAttributeMapping(AttributeMetaData attr, boolean enableNorms, boolean createAllIndex,
			boolean nestRefs, XContentBuilder jsonBuilder) throws IOException
	{
		String attrName = attr.getName();
		jsonBuilder.startObject(attrName);
		createAttributeMappingContents(attr, enableNorms, createAllIndex, nestRefs, jsonBuilder);
		jsonBuilder.endObject();
	}

	private static void createAttributeMappingContents(AttributeMetaData attr, boolean enableNorms,
			boolean createAllIndex, boolean nestRefs, XContentBuilder jsonBuilder) throws IOException
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
			case MREF:
			case XREF:
				EntityMetaData refEntity = attr.getRefEntity();
				if (nestRefs)
				{
					jsonBuilder.field("type", "nested");
					jsonBuilder.field("norms").startObject().field("enabled", enableNorms).endObject();
					jsonBuilder.startObject("properties");
					for (AttributeMetaData refAttr : refEntity.getAtomicAttributes())
					{
						createAttributeMapping(refAttr, enableNorms, createAllIndex, false, jsonBuilder);
					}
					jsonBuilder.endObject();
				}
				else
				{
					createAttributeMappingContents(refEntity.getLabelAttribute(), enableNorms, createAllIndex, false,
							jsonBuilder);
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
			case FILE:
			case IMAGE:
				throw new MolgenisDataException("Unsupported data type [" + dataType + "]");
			case INT:
				jsonBuilder.field("type", "integer");
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
				jsonBuilder.startObject("fields").startObject(FIELD_NOT_ANALYZED).field("type", "string")
						.field("index", "not_analyzed").endObject().startObject(FIELD_NGRAM_ANALYZED)
						.field("type", "string").field("analyzer", ElasticsearchIndexCreator.NGRAM_ANALYZER)
						.endObject().endObject();
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

	public static void serializeEntityMeta(EntityMetaData entityMetaData, XContentBuilder jsonBuilder)
			throws IOException
	{
		if (entityMetaData.getName() != null) jsonBuilder.field(ENTITY_NAME, entityMetaData.getName());
		jsonBuilder.field(ENTITY_ABSTRACT, entityMetaData.isAbstract());
		if (entityMetaData.getLabel() != null) jsonBuilder.field(ENTITY_LABEL, entityMetaData.getLabel());
		if (entityMetaData.getDescription() != null) jsonBuilder.field(ENTITY_DESCRIPTION,
				entityMetaData.getDescription());
		jsonBuilder.startArray(ENTITY_ATTRIBUTES);
		for (AttributeMetaData attr : entityMetaData.getAttributes())
		{
			serializeAttribute(attr, jsonBuilder);
		}
		jsonBuilder.endArray(); // attributes
		if (entityMetaData.getExtends() != null)
		{
			jsonBuilder.field(ENTITY_EXTENDS, entityMetaData.getExtends().getName());
		}
		if (entityMetaData.getEntityClass() != null)
		{
			jsonBuilder.field(ENTITY_ENTITY_CLASS, entityMetaData.getEntityClass().getName());
		}
	}

	private static void serializeAttribute(AttributeMetaData attr, XContentBuilder jsonBuilder) throws IOException
	{
		jsonBuilder.startObject();
		if (attr.getName() != null) jsonBuilder.field(ATTRIBUTE_NAME, attr.getName());
		if (attr.getLabel() != null) jsonBuilder.field(ATTRIBUTE_LABEL, attr.getLabel());
		if (attr.getDescription() != null) jsonBuilder.field(ATTRIBUTE_DESCRIPTION, attr.getDescription());
		if (attr.getDataType() != null && attr.getDataType().getEnumType() != null)
		{
			jsonBuilder.field(ATTRIBUTE_DATA_TYPE, attr.getDataType().getEnumType());
		}
		jsonBuilder.field(ATTRIBUTE_NILLABLE, attr.isNillable());
		jsonBuilder.field(ATTRIBUTE_READONLY, attr.isReadonly());
		jsonBuilder.field(ATTRIBUTE_UNIQUE, attr.isUnique());
		jsonBuilder.field(ATTRIBUTE_VISIBLE, attr.isVisible());
		// TODO better solution
		if (attr.getDefaultValue() != null) jsonBuilder.field(ATTRIBUTE_DEFAULT_VALUE, attr.getDefaultValue());
		jsonBuilder.field(ATTRIBUTE_ID_ATTRIBUTE, attr.isIdAtrribute());
		jsonBuilder.field(ATTRIBUTE_LABEL_ATTRIBUTE, attr.isLabelAttribute());
		jsonBuilder.field(ATTRIBUTE_LOOKUP_ATTRIBUTE, attr.isLookupAttribute());
		jsonBuilder.field(ATTRIBUTE_AUTO, attr.isAuto());
		if (attr.getRefEntity() != null && attr.getRefEntity().getName() != null)
		{
			jsonBuilder.field(ATTRIBUTE_REF_ENTITY, attr.getRefEntity().getName());
		}
		if (attr.getAttributeParts() != null)
		{
			jsonBuilder.startArray(ATTRIBUTE_ATTRIBUTE_PARTS);
			for (AttributeMetaData attrPart : attr.getAttributeParts())
			{
				serializeAttribute(attrPart, jsonBuilder);
			}
			jsonBuilder.endArray();
		}
		jsonBuilder.field(ATTRIBUTE_AGGREGATEABLE, attr.isAggregateable());
		if (attr.getRange() != null)
		{
			jsonBuilder.startObject(ATTRIBUTE_RANGE);
			if (attr.getRange().getMin() != null) jsonBuilder.field(ATTRIBUTE_RANGE_MIN, attr.getRange().getMin());
			if (attr.getRange().getMax() != null) jsonBuilder.field(ATTRIBUTE_RANGE_MAX, attr.getRange().getMax());
			jsonBuilder.endObject(); // range
		}

		jsonBuilder.endObject();
	}

	@SuppressWarnings("unchecked")
	public static EntityMetaData deserializeEntityMeta(Client client, String entityName) throws IOException
	{
		String docType = sanitizeMapperType(entityName);

		GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings("molgenis").execute()
				.actionGet();
		ImmutableOpenMap<String, MappingMetaData> indexMappings = getMappingsResponse.getMappings().get("molgenis");
		MappingMetaData mappingMetaData = indexMappings.get(docType);
		Map<String, Object> metaMap = null;
		// get full entitymetadata stored in elastic search
		metaMap = (Map<String, Object>) mappingMetaData.sourceAsMap().get("_meta");
		// get properties if full entitymetadata is not stored in elastic search
		if (metaMap == null || metaMap.isEmpty()) metaMap = (Map<String, Object>) mappingMetaData.sourceAsMap().get(
				"properties");
		// create entity meta
		String name = (String) metaMap.get(ENTITY_NAME);

		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(name, ElasticsearchEntity.class);

		// deserialize entity meta
		deserializeEntityMeta(metaMap, entityMetaData, client);
		return entityMetaData;
	}

	@SuppressWarnings("unchecked")
	private static void deserializeEntityMeta(Map<String, Object> entityMetaMap, DefaultEntityMetaData entityMetaData,
			Client client) throws IOException
	{
		boolean abstract_ = (boolean) entityMetaMap.get(ENTITY_ABSTRACT);
		entityMetaData.setAbstract(abstract_);

		if (entityMetaMap.containsKey(ENTITY_LABEL))
		{
			String label = (String) entityMetaMap.get(ENTITY_LABEL);
			entityMetaData.setLabel(label);
		}
		if (entityMetaMap.containsKey(ENTITY_DESCRIPTION))
		{
			String description = (String) entityMetaMap.get(ENTITY_DESCRIPTION);
			entityMetaData.setDescription(description);
		}
		if (entityMetaMap.containsKey(ENTITY_ATTRIBUTES))
		{
			List<Map<String, Object>> attributes = (List<Map<String, Object>>) entityMetaMap.get(ENTITY_ATTRIBUTES);
			for (Map<String, Object> attribute : attributes)
			{
				AttributeMetaData attributeMetaData = deserializeAttribute(attribute, entityMetaData, client);
				entityMetaData.addAttributeMetaData(attributeMetaData);
			}
		}
		if (entityMetaMap.containsKey(ENTITY_EXTENDS))
		{
			String extendsEntityName = (String) entityMetaMap.get(ENTITY_EXTENDS);
			EntityMetaData extends_ = deserializeEntityMeta(client, extendsEntityName);
			entityMetaData.setExtends(extends_);
		}
	}

	@SuppressWarnings("unchecked")
	private static AttributeMetaData deserializeAttribute(Map<String, Object> attributeMap,
			DefaultEntityMetaData entityMetaData, Client client) throws IOException
	{
		String name = (String) attributeMap.get(ATTRIBUTE_NAME);
		DefaultAttributeMetaData attribute = new DefaultAttributeMetaData(name);

		if (attributeMap.containsKey(ATTRIBUTE_LABEL))
		{
			attribute.setLabel((String) attributeMap.get(ATTRIBUTE_LABEL));
		}
		if (attributeMap.containsKey(ATTRIBUTE_DESCRIPTION))
		{
			attribute.setDescription((String) attributeMap.get(ATTRIBUTE_DESCRIPTION));
		}
		if (attributeMap.containsKey(ATTRIBUTE_DATA_TYPE))
		{
			String dataType = (String) attributeMap.get(ATTRIBUTE_DATA_TYPE);
			attribute.setDataType(MolgenisFieldTypes.getType(dataType.toLowerCase()));
		}
		attribute.setNillable((boolean) attributeMap.get(ATTRIBUTE_NILLABLE));
		attribute.setReadOnly((boolean) attributeMap.get(ATTRIBUTE_READONLY));
		attribute.setUnique((boolean) attributeMap.get(ATTRIBUTE_UNIQUE));
		attribute.setVisible((boolean) attributeMap.get(ATTRIBUTE_VISIBLE));
		if (attributeMap.containsKey(ATTRIBUTE_DEFAULT_VALUE))
		{
			// TODO convert default value to correct molgenis type
			attribute.setDefaultValue(attributeMap.get(ATTRIBUTE_DEFAULT_VALUE));
		}
		attribute.setIdAttribute((boolean) attributeMap.get(ATTRIBUTE_ID_ATTRIBUTE));
		attribute.setLabelAttribute((boolean) attributeMap.get(ATTRIBUTE_LABEL_ATTRIBUTE));
		attribute.setLookupAttribute((boolean) attributeMap.get(ATTRIBUTE_LOOKUP_ATTRIBUTE));
		attribute.setAuto((boolean) attributeMap.get(ATTRIBUTE_AUTO));
		if (attributeMap.containsKey(ATTRIBUTE_REF_ENTITY))
		{
			// TODO use dataservice to retrieve ref entity meta data instead of this repo
			String refEntityName = (String) attributeMap.get(ATTRIBUTE_REF_ENTITY);
			EntityMetaData refEntityMeta = deserializeEntityMeta(client, refEntityName);
			attribute.setRefEntity(refEntityMeta);
		}
		if (attributeMap.containsKey(ATTRIBUTE_ATTRIBUTE_PARTS))
		{
			List<Map<String, Object>> attributeParts = (List<Map<String, Object>>) attributeMap
					.get(ATTRIBUTE_ATTRIBUTE_PARTS);

			List<AttributeMetaData> attributeMetaDataParts = new ArrayList<AttributeMetaData>();
			for (Map<String, Object> attributePart : attributeParts)
			{
				AttributeMetaData attributeMetaDataPart = deserializeAttribute(attributePart, entityMetaData, client);
				attributeMetaDataParts.add(attributeMetaDataPart);
			}
			attribute.setAttributesMetaData(attributeMetaDataParts);
		}
		attribute.setAggregateable((boolean) attributeMap.get(ATTRIBUTE_AGGREGATEABLE));
		if (attributeMap.containsKey(ATTRIBUTE_RANGE))
		{
			Map<String, Object> range = (Map<String, Object>) attributeMap.get(ATTRIBUTE_RANGE);
			Long min = range.containsKey(ATTRIBUTE_RANGE_MIN) ? (Long) range.get(ATTRIBUTE_RANGE_MIN) : null;
			Long max = range.containsKey(ATTRIBUTE_RANGE_MAX) ? (Long) range.get(ATTRIBUTE_RANGE_MAX) : null;
			attribute.setRange(new Range(min, max));
		}
		return attribute;

	}
}
