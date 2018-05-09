package org.molgenis.integrationtest.platform;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.exception.NullPackageNotSuException;
import org.molgenis.data.security.exception.PackagePermissionDeniedException;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.EntityWithComputedAttributes;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.data.util.MolgenisDateFormat;
import org.molgenis.security.core.PermissionSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.ObjectIdentity;
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
	private static final String PACK_NO_WRITEMETA_PERMISSION = "packageNoWriteMeta";
	private static final String PACK_PERMISSION = "packageWriteMeta";
	public static final String ENTITY_TYPE_3 = "entityType3";
	public static final String ENTITY_TYPE_2 = "entityType2";
	public static final String ENTITY_TYPE_1 = "entityType1";
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
	@Autowired
	private EntityTypeFactory entityTypeFactory;
	@Autowired
	private PackageFactory packageFactory;

	private Package packNoPermission;
	private Package packPermission;

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

	@SuppressWarnings("deprecation")
	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = PackagePermissionDeniedException.class)
	public void testCreateNoPackagePermission()
	{
		EntityType entityType = entityTypeFactory.create(ENTITY_TYPE_1)
												 .setLabel("label")
												 .setBackend("PostgreSQL")
												 .setPackage(packNoPermission);
		entityType.addAttribute(attributeFactory.create()
												.setIdentifier(ATTR_REF_ID)
												.setName("attr")
												.setDataType(STRING)
												.setIdAttribute(true)
												.setNillable(false));
		metaDataService.createRepository(entityType);
	}

	@SuppressWarnings("deprecation")
	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = NullPackageNotSuException.class)
	public void testCreateNullPermission()
	{
		EntityType entityType = entityTypeFactory.create(ENTITY_TYPE_2).setLabel("label").setBackend("PostgreSQL");
		entityType.addAttribute(attributeFactory.create()
												.setIdentifier(ATTR_REF_ID)
												.setName("attr")
												.setDataType(STRING)
												.setIdAttribute(true)
												.setNillable(false));
		metaDataService.createRepository(entityType);
	}

	@SuppressWarnings("deprecation")
	@WithMockUser(username = USERNAME)
	@Test
	public void testCreatePermission()
	{
		EntityType entityType = entityTypeFactory.create(ENTITY_TYPE_3)
												 .setLabel("label")
												 .setBackend("PostgreSQL")
												 .setPackage(packPermission);
		entityType.addAttribute(attributeFactory.create()
												.setIdentifier(ATTR_REF_ID)
												.setName("attr")
												.setDataType(STRING)
												.setIdAttribute(true)
												.setNillable(false));
		metaDataService.createRepository(entityType);
		assertTrue(dataService.hasRepository(ENTITY_TYPE_3));
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

		packNoPermission = packageFactory.create(PACK_NO_WRITEMETA_PERMISSION);
		packPermission = packageFactory.create(PACK_PERMISSION);

		metaDataService.addPackage(packNoPermission);
		metaDataService.addPackage(packPermission);
	}

	private void populateDataPermissions()
	{
		Map<ObjectIdentity, PermissionSet> permissionMap = new HashMap<>();
		permissionMap.put(new EntityTypeIdentity("sys_md_Package"), PermissionSet.READ);
		permissionMap.put(new EntityTypeIdentity("sys_md_EntityType"), PermissionSet.WRITEMETA);
		permissionMap.put(new EntityTypeIdentity("sys_md_Attribute"), PermissionSet.WRITEMETA);
		permissionMap.put(new EntityTypeIdentity("sys_dec_DecoratorConfiguration"), PermissionSet.READ);
		permissionMap.put(new EntityTypeIdentity(ENTITY_TYPE_ID), PermissionSet.WRITEMETA);
		permissionMap.put(new EntityTypeIdentity(REF_ENTITY_TYPE_ID), PermissionSet.READ);
		permissionMap.put(new PackageIdentity(PACK_PERMISSION), PermissionSet.WRITEMETA);
		permissionMap.put(new PackageIdentity(PACK_NO_WRITEMETA_PERMISSION), PermissionSet.WRITE);
		testPermissionPopulator.populate(permissionMap, USERNAME);
	}

	private void depopulate()
	{
		List<EntityType> entityTypes = dataService.findAll(ENTITY_TYPE_META_DATA,
				Stream.of(ENTITY_TYPE_ID, REF_ENTITY_TYPE_ID, ENTITY_TYPE_1, ENTITY_TYPE_2, ENTITY_TYPE_3),
				EntityType.class).collect(toList());
		dataService.getMeta().deleteEntityType(entityTypes);
		dataService.delete(PackageMetadata.PACKAGE, Stream.of(packNoPermission, packPermission));

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
