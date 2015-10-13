package org.molgenis.migrate.version.v1_5;

import static org.molgenis.data.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IndexedRepository;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Range;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.util.DependencyResolver;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Upgrade class for upgrading from metadata version 0 (molgenis 1.4.4) to 1 (molgenis 1.5.0)
 */
public class Step2 extends MolgenisUpgrade
{
	private static final String ENTITY_NAME = "name";
	private static final String ENTITY_LABEL = "label";
	private static final String ENTITY_DESCRIPTION = "description";
	private static final String ENTITY_EXTENDS = "extends";
	private static final String ENTITY_ATTRIBUTES = "attributes";
	private static final String ENTITY_ABSTRACT = "abstract";

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

	private final DataService dataService;
	private final RepositoryCollection jpaBackend;
	private final JdbcTemplate jdbcTemplate;
	private final ElasticsearchService searchService;

	public Step2(DataService dataService, RepositoryCollection jpaBackend, DataSource dataSource,
			SearchService searchService)
	{
		super(1, 2);
		this.dataService = dataService;
		this.jpaBackend = jpaBackend;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.searchService = (ElasticsearchService) searchService;
	}

	@Override
	public void upgrade()
	{
		MetaDataServiceImpl metaDataService = (MetaDataServiceImpl) dataService.getMeta();

		// Register ES repos
		Set<EntityMetaData> esRepos = new HashSet<>();
		for (String entityName : searchService.getTypes())
		{
			try
			{
				EntityMetaData emd = deserializeEntityMeta(entityName);
				if (emd != null) esRepos.add(emd);
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}

		for (EntityMetaData emd : DependencyResolver.resolve(esRepos))
		{
			metaDataService.addToEntityMetaDataRepository(emd);
		}

		// We got no mrefs in JPA in the standard molgenis -> not supported in upgrade
		// JPA ids from int to string
		List<String> statements = new ArrayList<>();

		// Drop foreign keys
		for (Repository repo : jpaBackend)
		{
			EntityMetaData emd = repo.getEntityMetaData();
			for (AttributeMetaData attr : emd.getAtomicAttributes())
			{
				if (attr.getDataType() instanceof MrefField) throw new MolgenisDataException(
						"Mref not supported in upgrade");

				if (attr.getDataType() instanceof XrefField)
				{
					statements.add(String.format("ALTER TABLE %s DROP FOREIGN KEY FK_%s_%s", emd.getName(),
							emd.getName(), attr.getName()));
				}
			}
		}
		jdbcTemplate.batchUpdate(statements.toArray(new String[statements.size()]));
		statements.clear();

		// Update key from int to varchar
		for (Repository repo : jpaBackend)
		{
			EntityMetaData emd = repo.getEntityMetaData();
			for (AttributeMetaData attr : emd.getAtomicAttributes())
			{
				if (attr.isIdAtrribute() || (attr.getDataType() instanceof XrefField))
				{
					statements.add(String.format("ALTER TABLE %s MODIFY COLUMN %s VARCHAR(255)", emd.getName(),
							attr.getName()));
				}
			}
		}
		jdbcTemplate.batchUpdate(statements.toArray(new String[statements.size()]));

		// Reanable foreing keys
		statements.clear();
		for (Repository repo : jpaBackend)
		{
			EntityMetaData emd = repo.getEntityMetaData();
			for (AttributeMetaData attr : repo.getEntityMetaData().getAtomicAttributes())
			{
				if (attr.getDataType() instanceof XrefField)
				{
					statements.add(String.format("ALTER TABLE %s ADD FOREIGN KEY (FK_%s_%s) REFERENCES %s",
							emd.getName(), emd.getName(), attr.getName(), attr.getRefEntity().getName()));
				}
			}
		}

		for (Repository repo : jpaBackend)
		{
			((IndexedRepository) repo).rebuildIndex();
		}
	}

	@SuppressWarnings("unchecked")
	public EntityMetaData deserializeEntityMeta(String entityName) throws IOException
	{
		String docType = sanitizeMapperType(entityName);

		GetMappingsResponse getMappingsResponse = searchService.getMappings();

		ImmutableOpenMap<String, MappingMetaData> indexMappings = getMappingsResponse.getMappings().get("molgenis");
		MappingMetaData mappingMetaData = indexMappings.get(docType);
		Map<String, Object> metaMap = (Map<String, Object>) mappingMetaData.sourceAsMap().get("_meta");
		if (metaMap == null) return null;

		// create entity meta
		String name = (String) metaMap.get(ENTITY_NAME);

		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(name);
		entityMetaData.setBackend(ElasticsearchRepositoryCollection.NAME);

		// deserialize entity meta
		deserializeEntityMeta(metaMap, entityMetaData);
		return entityMetaData;
	}

	@SuppressWarnings("unchecked")
	private void deserializeEntityMeta(Map<String, Object> entityMetaMap, DefaultEntityMetaData entityMetaData)
			throws IOException
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
				AttributeMetaData attributeMetaData = deserializeAttribute(attribute, entityMetaData);
				entityMetaData.addAttributeMetaData(attributeMetaData);
			}
		}
		if (entityMetaMap.containsKey(ENTITY_EXTENDS))
		{
			String extendsEntityName = (String) entityMetaMap.get(ENTITY_EXTENDS);
			EntityMetaData extends_ = deserializeEntityMeta(extendsEntityName);
			entityMetaData.setExtends(extends_);
		}
	}

	@SuppressWarnings("unchecked")
	private AttributeMetaData deserializeAttribute(Map<String, Object> attributeMap,
			DefaultEntityMetaData entityMetaData) throws IOException
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
			attribute.setDefaultValue((String) attributeMap.get(ATTRIBUTE_DEFAULT_VALUE));
		}
		attribute.setIdAttribute((boolean) attributeMap.get(ATTRIBUTE_ID_ATTRIBUTE));
		attribute.setLabelAttribute((boolean) attributeMap.get(ATTRIBUTE_LABEL_ATTRIBUTE));
		attribute.setLookupAttribute((boolean) attributeMap.get(ATTRIBUTE_LOOKUP_ATTRIBUTE));
		attribute.setAuto((boolean) attributeMap.get(ATTRIBUTE_AUTO));
		if (attributeMap.containsKey(ATTRIBUTE_REF_ENTITY))
		{
			String refEntityName = (String) attributeMap.get(ATTRIBUTE_REF_ENTITY);
			EntityMetaData refEntityMeta = deserializeEntityMeta(refEntityName);
			attribute.setRefEntity(refEntityMeta);
		}
		if (attributeMap.containsKey(ATTRIBUTE_ATTRIBUTE_PARTS))
		{
			List<Map<String, Object>> attributeParts = (List<Map<String, Object>>) attributeMap
					.get(ATTRIBUTE_ATTRIBUTE_PARTS);

			List<AttributeMetaData> attributeMetaDataParts = new ArrayList<AttributeMetaData>();
			for (Map<String, Object> attributePart : attributeParts)
			{
				AttributeMetaData attributeMetaDataPart = deserializeAttribute(attributePart, entityMetaData);
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
