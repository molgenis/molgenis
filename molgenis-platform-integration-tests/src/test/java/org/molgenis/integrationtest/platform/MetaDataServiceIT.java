package org.molgenis.integrationtest.platform;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.*;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.EntityWithComputedAttributes;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.data.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.time.ZoneId.systemDefault;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.EntityTestHarness.*;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.security.EntityTypePermission.READ;
import static org.molgenis.data.security.EntityTypePermission.WRITE;
import static org.molgenis.data.security.EntityTypePermission.WRITEMETA;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { PlatformITConfig.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
@Transactional
public class MetaDataServiceIT extends AbstractTestNGSpringContextTests
{
	private static final String USERNAME = "metaDataService-user";

	private static final String ENTITY_TYPE_ID = "metaDataServiceEntityType";
	private static final String REF_ENTITY_TYPE_ID = "metaDataServiceRefEntityType";
	private static List<Entity> refEntities;

	@Autowired
	private IndexJobScheduler indexJobScheduler;
	@Autowired
	private TestPermissionPopulator testPermissionPopulator;
	@Autowired
	private EntityTestHarness entityTestHarness;
	@Autowired
	private MetaDataService metaDataService;
	@Autowired
	private DataService dataService;
	@Autowired
	private AttributeFactory attributeFactory;

	@BeforeClass
	public void setUpBeforeClass() throws InterruptedException
	{
		// bootstrapper has finished but indexing of bootstrapped data might be in progress
		indexJobScheduler.waitForAllIndicesStable();

		runAsSystem(this::populate);
	}

	@AfterClass
	public void tearDownAfterClass()
	{
		runAsSystem(this::depopulate);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testUpdateEntityType()
	{
		EntityType updatedEntityType = metaDataService.getEntityType(ENTITY_TYPE_ID);
		updatedEntityType.getAttribute(ATTR_STRING).setDataType(ENUM).setEnumOptions(asList("string0", "string1"));
		updatedEntityType.getAttribute(ATTR_BOOL).setDataType(STRING);
		updatedEntityType.getAttribute(ATTR_CATEGORICAL).setDataType(LONG).setRefEntity(null);
		updatedEntityType.getAttribute(ATTR_CATEGORICAL_MREF).setDataType(MREF);
		updatedEntityType.getAttribute(ATTR_DATE).setDataType(DATE_TIME);
		updatedEntityType.getAttribute(ATTR_DATETIME).setDataType(DATE);
		updatedEntityType.getAttribute(ATTR_EMAIL).setDataType(STRING);
		updatedEntityType.getAttribute(ATTR_DECIMAL).setDataType(INT);
		updatedEntityType.getAttribute(ATTR_HTML).setDataType(TEXT);
		updatedEntityType.getAttribute(ATTR_HYPERLINK).setDataType(STRING);
		updatedEntityType.getAttribute(ATTR_LONG).setDataType(DECIMAL);
		updatedEntityType.getAttribute(ATTR_INT).setDataType(LONG);
		updatedEntityType.getAttribute(ATTR_SCRIPT).setDataType(TEXT);
		updatedEntityType.getAttribute(ATTR_XREF).setDataType(CATEGORICAL);
		updatedEntityType.getAttribute(ATTR_MREF).setDataType(CATEGORICAL_MREF);
		updatedEntityType.getAttribute(ATTR_ENUM).setDataType(STRING).setEnumOptions(emptyList());

		metaDataService.updateEntityType(updatedEntityType);

		Entity expectedEntity = new DynamicEntity(updatedEntityType);
		expectedEntity.set(ATTR_ID, "0");
		expectedEntity.set(ATTR_STRING, "string1");
		expectedEntity.set(ATTR_BOOL, "true");
		expectedEntity.set(ATTR_CATEGORICAL, 0L);
		expectedEntity.set(ATTR_CATEGORICAL_MREF, singletonList(refEntities.get(0)));
		expectedEntity.set(ATTR_DATE, LocalDate.parse("2012-12-21").atStartOfDay(systemDefault()).toInstant());
		expectedEntity.set(ATTR_DATETIME, MolgenisDateFormat.parseLocalDate("1985-08-12T06:12:13Z"));
		expectedEntity.set(ATTR_EMAIL, "this.is@mail.address");
		expectedEntity.set(ATTR_DECIMAL, 0); // before update: 0.123
		expectedEntity.set(ATTR_HTML, null);
		expectedEntity.set(ATTR_HYPERLINK, "http://www.molgenis.org");
		expectedEntity.set(ATTR_LONG, 0.0);
		expectedEntity.set(ATTR_INT, 10L);
		expectedEntity.set(ATTR_SCRIPT, "/bin/blaat/script.sh");
		expectedEntity.set(ATTR_XREF, refEntities.get(0));
		expectedEntity.set(ATTR_MREF, singletonList(refEntities.get(0)));
		expectedEntity.set(ATTR_COMPOUND_CHILD_INT, 10);
		expectedEntity.set(ATTR_ENUM, "option1");
		expectedEntity = new EntityWithComputedAttributes(expectedEntity);

		assertTrue(EntityUtils.equals(dataService.findOneById(ENTITY_TYPE_ID, "0"), expectedEntity));
	}

	@SuppressWarnings("deprecation")
	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Attribute data type update from \\[INT\\] to \\[DATE_TIME\\] not allowed, allowed types are \\[BOOL, CATEGORICAL, DECIMAL, ENUM, LONG, STRING, TEXT, XREF\\]")
	public void testUpdateEntityTypeNotAllowed()
	{
		EntityType updatedEntityType = metaDataService.getEntityType(ENTITY_TYPE_ID);
		updatedEntityType.getAttribute(ATTR_COMPOUND_CHILD_INT).setDataType(DATE_TIME);
		metaDataService.updateEntityType(updatedEntityType);
	}

	@WithMockUser(username = USERNAME)
	@Test(dependsOnMethods = { "testUpdateEntityType", "testUpdateEntityTypeNotAllowed" })
	public void testAddAttribute()
	{
		EntityType entityType = metaDataService.getEntityType(ENTITY_TYPE_ID);
		Attribute attribute = attributeFactory.create().setName("newAttribute");
		attribute.setEntity(entityType);
		metaDataService.addAttribute(attribute);

		EntityType updatedEntityType = metaDataService.getEntityType(ENTITY_TYPE_ID);
		assertTrue(EntityUtils.equals(attribute, updatedEntityType.getAttribute("newAttribute")));
	}

	@WithMockUser(username = USERNAME)
	@Test(dependsOnMethods = { "testAddAttribute" })
	public void testUpdateAttribute()
	{
		EntityType entityType = metaDataService.getEntityType(ENTITY_TYPE_ID);
		Attribute attribute = entityType.getAttribute("newAttribute");
		attribute.setLabel("updated-label");
		attribute.setEntity(entityType);
		dataService.update(ATTRIBUTE_META_DATA, attribute);

		EntityType updatedEntityType = metaDataService.getEntityType(ENTITY_TYPE_ID);
		assertTrue(EntityUtils.equals(attribute, updatedEntityType.getAttribute("newAttribute")));
	}

	@WithMockUser(username = USERNAME)
	@Test(dependsOnMethods = { "testUpdateAttribute" })
	public void testDeleteAttribute()
	{
		EntityType entityType = metaDataService.getEntityType(ENTITY_TYPE_ID);
		String attributeId = entityType.getAttribute("newAttribute").getIdentifier();
		metaDataService.deleteAttributeById(attributeId);

		EntityType updatedEntityType = metaDataService.getEntityType(ENTITY_TYPE_ID);
		assertNull(updatedEntityType.getAttribute("newAttribute"));
	}

	private void populate()
	{
		populateData();
		populateDataPermissions();

		try
		{
			indexJobScheduler.waitForAllIndicesStable();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void populateData()
	{
		EntityType refEntityType = entityTestHarness.createDynamicRefEntityType("metaDataServiceRefEntityType");
		metaDataService.createRepository(refEntityType);
		refEntities = entityTestHarness.createTestRefEntities(refEntityType, 3);
		dataService.add(refEntityType.getId(), refEntities.stream());

		EntityType entityType = entityTestHarness.createDynamicTestEntityType(refEntityType,
				"metaDataServiceEntityType");
		metaDataService.createRepository(entityType);
		List<Entity> entities = entityTestHarness.createTestEntities(entityType, 1, refEntities).collect(toList());
		dataService.add(entityType.getId(), entities.stream());
	}

	private void populateDataPermissions()
	{
		// define cumulative permissions
		CumulativePermission readEntityType = EntityTypePermissionUtils.getCumulativePermission(
				EntityTypePermission.READ);
		CumulativePermission writeEntityType = EntityTypePermissionUtils.getCumulativePermission(WRITE);
		CumulativePermission writeMetaEntityType = EntityTypePermissionUtils.getCumulativePermission(WRITEMETA);
		CumulativePermission readEntity = EntityPermissionUtils.getCumulativePermission(EntityPermission.READ);
		CumulativePermission writeEntity = EntityPermissionUtils.getCumulativePermission(EntityPermission.WRITE);

		Map<ObjectIdentity, Permission> permissionMap = new HashMap<>();
		permissionMap.put(new EntityTypeIdentity("sys_md_Package"), readEntityType);
		permissionMap.put(new EntityTypeIdentity("sys_md_EntityType"), writeMetaEntityType);
		permissionMap.put(new EntityTypeIdentity("sys_md_Attribute"), writeMetaEntityType);
		permissionMap.put(new EntityTypeIdentity("sys_dec_DecoratorConfiguration"), readEntityType);
		permissionMap.put(new EntityTypeIdentity(ENTITY_TYPE_ID), writeMetaEntityType);
		permissionMap.put(new EntityTypeIdentity(REF_ENTITY_TYPE_ID), readEntityType);
		testPermissionPopulator.populate(permissionMap, USERNAME);
	}

	private void depopulate()
	{
		List<EntityType> entityTypes = dataService.findAll(ENTITY_TYPE_META_DATA,
				Stream.of(ENTITY_TYPE_ID, REF_ENTITY_TYPE_ID), EntityType.class).collect(toList());
		dataService.getMeta().deleteEntityType(entityTypes);

		try
		{
			indexJobScheduler.waitForAllIndicesStable();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
}
