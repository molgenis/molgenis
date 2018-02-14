package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.postgresql.PostgreSqlRepositoryCollection;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.integrationtest.platform.TestPermissionPopulator;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.molgenis.data.EntityManager.CreationMode.NO_POPULATE;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.security.EntityTypePermission.*;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractAttributeTypeUpdateIT extends AbstractTestNGSpringContextTests
{
	private final Logger LOG = getLogger(AbstractAttributeTypeUpdateIT.class);

	private static final List<AttributeType> referencingTypes = newArrayList(MREF, XREF, CATEGORICAL, CATEGORICAL_MREF,
			FILE);
	private static final List<String> enumOptions = newArrayList("1", "2b", "abc");
	private static final String MAIN_ENTITY = "MAINENTITY";
	private static final String REFERENCE_ENTITY = "REFERENCEENTITY";
	private static final String MAIN_ENTITY_ID_VALUE = "1";

	@Autowired
	IndexJobScheduler indexService;

	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	DataService dataService;

	@Autowired
	EntityManager entityManager;

	@Autowired
	MetaDataService metaDataService;

	@Autowired
	private TestPermissionPopulator testPermissionPopulator;

	private EntityType entityType;
	private String mainId = "id";
	private String mainAttribute = "mainAttribute";

	private EntityType referenceEntityType;
	private String refId = "id";
	private String refLabel = "label";

	private void populateUserPermissions()
	{
		Map<String, EntityTypePermission> entityTypePermissionMap = new HashMap<>();
		entityTypePermissionMap.put("sys_md_Package", WRITE);
		entityTypePermissionMap.put("sys_md_EntityType", WRITE);
		entityTypePermissionMap.put("sys_md_Attribute", WRITE);
		entityTypePermissionMap.put("sys_dec_DecoratorConfiguration", READ);
		entityTypePermissionMap.put(MAIN_ENTITY, WRITEMETA);
		entityTypePermissionMap.put(REFERENCE_ENTITY, WRITEMETA);

		testPermissionPopulator.populate(entityTypePermissionMap);
	}

	/**
	 * setup method to configure the entities for AttributeType editing
	 *
	 * @param type      The main AttributeType, which will be converted to all other types
	 * @param refIdType The type of the reference ID attribute, used for testing valid and invalid XREF conversions
	 */
	void setup(AttributeType type, AttributeType refIdType)
	{
		entityType = entityTypeFactory.create(MAIN_ENTITY);
		entityType.setLabel(MAIN_ENTITY);
		entityType.setBackend(PostgreSqlRepositoryCollection.POSTGRESQL);

		referenceEntityType = entityTypeFactory.create(REFERENCE_ENTITY);
		referenceEntityType.setLabel(REFERENCE_ENTITY);
		referenceEntityType.setBackend(PostgreSqlRepositoryCollection.POSTGRESQL);

		Attribute mainIdAttribute = attributeFactory.create().setName(mainId).setIdAttribute(true);
		Attribute mainAttributeAttribute = attributeFactory.create()
														   .setDataType(type)
														   .setName(mainAttribute)
														   .setNillable(false);

		if (referencingTypes.contains(type))
		{
			mainAttributeAttribute.setRefEntity(referenceEntityType);
		}
		else if (type.equals(ENUM))
		{
			mainAttributeAttribute.setEnumOptions(enumOptions);
		}

		Attribute refIdAttribute = attributeFactory.create().setName(refId).setDataType(refIdType).setIdAttribute(true);
		Attribute refLabelAttribute = attributeFactory.create()
													  .setName(refLabel)
													  .setLabelAttribute(true)
													  .setNillable(false);

		entityType.addAttributes(newArrayList(mainIdAttribute, mainAttributeAttribute));
		referenceEntityType.addAttributes(newArrayList(refIdAttribute, refLabelAttribute));

		Entity refEntity_1 = entityManager.create(referenceEntityType, NO_POPULATE);
		Entity refEntity_2 = entityManager.create(referenceEntityType, NO_POPULATE);
		Entity refEntity_3 = entityManager.create(referenceEntityType, NO_POPULATE);

		if (refIdType == INT)
		{
			refEntity_1.set(refId, 1);
			refEntity_2.set(refId, 23);
			refEntity_3.set(refId, 42);
		}
		else if (refIdType == LONG)
		{
			refEntity_1.set(refId, 1L);
			refEntity_2.set(refId, 3432453425L);
			refEntity_3.set(refId, 42L);
		}
		else
		{
			refEntity_1.set(refId, "1");
			refEntity_2.set(refId, "molgenis@test.org");
			refEntity_3.set(refId, "https://www.google.com");
		}
		refEntity_1.set(refLabel, "label1");
		refEntity_2.set(refLabel, "email label");
		refEntity_3.set(refLabel, "hyperlink label");

		runAsSystem(() ->
		{
			metaDataService.upsertEntityTypes(newArrayList(entityType, referenceEntityType));
			dataService.add(REFERENCE_ENTITY, Stream.of(refEntity_1, refEntity_2, refEntity_3));
		});
	}

	void testTypeConversion(Object valueToConvert, AttributeType typeToConvertTo) throws MolgenisValidationException
	{
		populateUserPermissions();

		// Add a data row to the EntityType
		Entity entity = entityManager.create(entityType, NO_POPULATE);
		entity.set(mainId, MAIN_ENTITY_ID_VALUE);
		entity.set(mainAttribute, valueToConvert);

		// Add one entity row
		dataService.add(MAIN_ENTITY, entity);

		// Update EntityType
		convert(typeToConvertTo);

		metaDataService.updateEntityType(entityType);
	}

	private void convert(AttributeType typeToConvertTo) throws MolgenisValidationException
	{
		Attribute attribute = entityType.getAttribute(mainAttribute);
		if (referencingTypes.contains(typeToConvertTo)) attribute.setRefEntity(referenceEntityType);
		else attribute.setRefEntity(null);

		if (typeToConvertTo.equals(AttributeType.ENUM)) attribute.setEnumOptions(enumOptions);
		else attribute.setEnumOptions(emptyList());

		attribute.setDataType(typeToConvertTo);
		metaDataService.updateEntityType(entityType);
	}

	Object getActualValue()
	{
		Object actualValue = dataService.findOneById(MAIN_ENTITY, MAIN_ENTITY_ID_VALUE).get(mainAttribute);
		if (actualValue instanceof Entity) return ((Entity) actualValue).getLabelValue();
		return actualValue;
	}

	AttributeType getActualDataType()
	{
		return metaDataService.getEntityType(MAIN_ENTITY).getAttribute(mainAttribute).getDataType();
	}

	void afterMethod(AttributeType type)
	{
		runAsSystem(() ->
		{
			// Delete rows of data
			dataService.deleteAll(MAIN_ENTITY);

			// Remove attribute, some conversions back to the main attribute are not allowed
			Attribute attribute = entityType.getAttribute(mainAttribute);
			entityType.removeAttribute(attribute);
			metaDataService.updateEntityType(entityType);

			// Add the main attribute again, with the original type
			Attribute mainAttributeAttribute = attributeFactory.create().setDataType(type).setName(mainAttribute);
			if (referencingTypes.contains(type)) mainAttributeAttribute.setRefEntity(referenceEntityType);
			else if (type.equals(ENUM)) mainAttributeAttribute.setEnumOptions(enumOptions);

			entityType.addAttribute(mainAttributeAttribute);

			metaDataService.updateEntityType(entityType);
		});

	}

	void afterClass()
	{
		runAsSystem(() ->
		{
			dataService.deleteAll(MAIN_ENTITY);
			dataService.deleteAll(REFERENCE_ENTITY);
			metaDataService.deleteEntityType(MAIN_ENTITY);
			metaDataService.deleteEntityType(REFERENCE_ENTITY);
		});
	}
}
