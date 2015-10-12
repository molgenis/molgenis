package org.molgenis.data.meta;

import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.mockito.Mockito;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Package;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.mem.InMemoryRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@WebAppConfiguration
@ContextConfiguration(classes = MetaDataServiceImplTest.Config.class)
public class MetaDataServiceImplTest extends AbstractTestNGSpringContextTests
{
	private MetaDataServiceImpl metaDataServiceImpl;

	@Autowired
	private ManageableRepositoryCollection manageableCrudRepositoryCollection;

	private Package defaultPackage;

	private PackageImpl molgenis;

	@BeforeMethod
	public void readPackageTree()
	{
		defaultPackage = PackageImpl.defaultPackage;
		PackageImpl org = new PackageImpl("org", "the org package", null);
		molgenis = new PackageImpl("molgenis", "the molgenis package", org);
		org.addSubPackage(molgenis);
		PackageImpl molgenis2 = new PackageImpl("molgenis2", "the molgenis2 package", org);
		org.addSubPackage(molgenis2);

		DefaultEntityMetaData personMetaData = new DefaultEntityMetaData("Person");
		personMetaData.setDescription("A person");
		personMetaData.setPackage(molgenis);
		molgenis.addEntity(personMetaData);

		MapEntity personEntity = new MapEntity();
		personEntity.set(EntityMetaDataMetaData.PACKAGE, molgenis.toEntity());
		personEntity.set(EntityMetaDataMetaData.DESCRIPTION, "A person");
		personEntity.set(EntityMetaDataMetaData.FULL_NAME, "org_molgenis_Person");
		personEntity.set(EntityMetaDataMetaData.SIMPLE_NAME, "Person");
		personEntity.set(EntityMetaDataMetaData.ABSTRACT, true);

		metaDataServiceImpl = new MetaDataServiceImpl(new DataServiceImpl());
		metaDataServiceImpl.setDefaultBackend(manageableCrudRepositoryCollection);

		assertEquals(metaDataServiceImpl.getRootPackages(), Arrays.asList(defaultPackage));
	}

	@Test
	public void testAddAndGetEntity()
	{
		readPackageTree();

		PackageImpl defaultPackage = (PackageImpl) PackageImpl.defaultPackage;
		DefaultEntityMetaData coderMetaData = new DefaultEntityMetaData("Coder");
		coderMetaData.setDescription("A coder");
		coderMetaData.setExtends(metaDataServiceImpl.getEntityMetaData("org_molgenis_Person"));
		coderMetaData.setPackage(defaultPackage);
		coderMetaData.addAttribute("ID").setIdAttribute(true);
		coderMetaData.addAttribute("simple");
		DefaultAttributeMetaData compoundAttribute = new DefaultAttributeMetaData("compound", COMPOUND);
		coderMetaData.addAttributeMetaData(compoundAttribute);
		compoundAttribute.addAttributePart(new DefaultAttributeMetaData("c1"));
		compoundAttribute.addAttributePart(new DefaultAttributeMetaData("c2"));

		assertEquals(coderMetaData.getIdAttribute().getName(), "ID");
		metaDataServiceImpl.addEntityMeta(coderMetaData);
		DefaultEntityMetaData retrieved = metaDataServiceImpl.getEntityMetaData("Coder");
		assertEquals(retrieved, coderMetaData);
		assertEquals(retrieved.getIdAttribute().getName(), "ID");

		// reboot

		metaDataServiceImpl = new MetaDataServiceImpl(new DataServiceImpl());
		metaDataServiceImpl.setDefaultBackend(manageableCrudRepositoryCollection);
		retrieved = metaDataServiceImpl.getEntityMetaData("Coder");
		assertEquals(retrieved.getIdAttribute().getName(), "ID");
		assertTrue(Iterables.elementsEqual(retrieved.getAtomicAttributes(), coderMetaData.getAtomicAttributes()));
	}

	/**
	 * Test that a new entity has at least all attributes as of the existing entity. If not it results false.
	 */
	@Test
	public void canIntegrateEntityMetadataCheckTrue()
	{
		String entityName = "testEntity";
		DataServiceImpl dataServiceImpl = Mockito.mock(DataServiceImpl.class);
		when(dataServiceImpl.hasRepository(entityName)).thenReturn(Boolean.TRUE);

		DefaultEntityMetaData existingEntityMetaData = new DefaultEntityMetaData(entityName);
		existingEntityMetaData.addAttribute("ID").setIdAttribute(true);

		DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData(entityName);
		newEntityMetaData.addAttribute("ID").setIdAttribute(true);

		when(dataServiceImpl.getEntityMetaData(entityName)).thenReturn(existingEntityMetaData);
		MetaDataServiceImpl metaDataService = new MetaDataServiceImpl(dataServiceImpl);

		assertTrue(metaDataService.canIntegrateEntityMetadataCheck(newEntityMetaData));
	}

	/**
	 * Test that a new entity has at least all attributes as of the existing entity. It is allowed to the new entity to
	 * have an extra attribute.
	 */
	@Test
	public void canIntegrateEntityMetadataCheckTrue2()
	{
		String entityName = "testEntity";
		DataServiceImpl dataServiceImpl = Mockito.mock(DataServiceImpl.class);
		when(dataServiceImpl.hasRepository(entityName)).thenReturn(Boolean.TRUE);

		DefaultEntityMetaData existingEntityMetaData = new DefaultEntityMetaData(entityName);
		existingEntityMetaData.addAttribute("ID").setIdAttribute(true);

		DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData(entityName);
		newEntityMetaData.addAttribute("ID").setIdAttribute(true);
		newEntityMetaData.addAttributeMetaData(new DefaultAttributeMetaData("newAtribute"));

		when(dataServiceImpl.getEntityMetaData(entityName)).thenReturn(existingEntityMetaData);
		MetaDataServiceImpl metaDataService = new MetaDataServiceImpl(dataServiceImpl);

		assertTrue(metaDataService.canIntegrateEntityMetadataCheck(newEntityMetaData));
	}

	/**
	 * Test that a new entity has at least all attributes as of the existing entity.
	 */
	@Test
	public void canIntegrateEntityMetadataCheckFalse()
	{
		String entityName = "testEntity";
		DataServiceImpl dataServiceImpl = Mockito.mock(DataServiceImpl.class);
		when(dataServiceImpl.hasRepository(entityName)).thenReturn(Boolean.TRUE);

		DefaultEntityMetaData existingEntityMetaData = new DefaultEntityMetaData(entityName);
		existingEntityMetaData.addAttribute("ID").setIdAttribute(true);
		existingEntityMetaData.addAttributeMetaData(new DefaultAttributeMetaData("existingAttribute"));

		DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData(entityName);
		newEntityMetaData.addAttribute("ID").setIdAttribute(true);

		when(dataServiceImpl.getEntityMetaData(entityName)).thenReturn(existingEntityMetaData);
		MetaDataServiceImpl metaDataService = new MetaDataServiceImpl(dataServiceImpl);

		assertFalse(metaDataService.canIntegrateEntityMetadataCheck(newEntityMetaData));
	}

	/**
	 * Test that the same attribute name but different attribute settings are resulting in false
	 */
	@Test
	public void canIntegrateEntityMetadataCheckFalse2()
	{
		String entityName = "testEntity";
		DataServiceImpl dataServiceImpl = Mockito.mock(DataServiceImpl.class);
		when(dataServiceImpl.hasRepository(entityName)).thenReturn(Boolean.TRUE);

		DefaultEntityMetaData existingEntityMetaData = new DefaultEntityMetaData(entityName);
		existingEntityMetaData.addAttribute("ID").setIdAttribute(true);

		DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData(entityName);
		newEntityMetaData.addAttribute("ID").setIdAttribute(false);

		when(dataServiceImpl.getEntityMetaData(entityName)).thenReturn(existingEntityMetaData);
		MetaDataServiceImpl metaDataService = new MetaDataServiceImpl(dataServiceImpl);

		assertFalse(metaDataService.canIntegrateEntityMetadataCheck(newEntityMetaData));
	}

	@Test
	public void integrationTestMetaData()
	{
		DataServiceImpl dataServiceImpl = Mockito.mock(DataServiceImpl.class);
		when(dataServiceImpl.hasRepository("attributes")).thenReturn(Boolean.FALSE); // To skip the
																						// canIntegrateEntityMetadataCheck
																						// test
		MetaDataServiceImpl metaDataService = new MetaDataServiceImpl(dataServiceImpl);
		RepositoryCollection repositoryCollection = Mockito.mock(RepositoryCollection.class);
		
		when(repositoryCollection.getEntityNames()).thenReturn(Lists.newArrayList("attributes"));

		DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData("attributes");
		newEntityMetaData.addAttribute("ID").setIdAttribute(false);
		Repository repo1 = Mockito.mock(Repository.class);
		when(repositoryCollection.getRepository("attributes")).thenReturn(repo1);
		when(repo1.getEntityMetaData()).thenReturn(newEntityMetaData);

		LinkedHashMap<String, Boolean> entitiesImportable = new LinkedHashMap<String, Boolean>();
		entitiesImportable.put("attributes", true);

		assertEquals(entitiesImportable, metaDataService.integrationTestMetaData(repositoryCollection));
	}

	@Test
	public void integrationTestMetaData2()
	{
		DataServiceImpl dataServiceImpl = Mockito.mock(DataServiceImpl.class);
		MetaDataServiceImpl metaDataService = new MetaDataServiceImpl(dataServiceImpl);

		DefaultEntityMetaData newEntityMetaData = new DefaultEntityMetaData("attributes");
		newEntityMetaData.addAttribute("ID").setIdAttribute(false);

		List<String> skipEntities = Lists.<String> newArrayList("attributes");
		String defaultPackage = "base";
		ImmutableMap<String, EntityMetaData> newEntitiesMetaDataMap = Mockito.mock(ImmutableMap.class);
		when(newEntitiesMetaDataMap.get("attributes")).thenReturn(newEntityMetaData);
		when(newEntitiesMetaDataMap.keySet()).thenReturn(ImmutableSet.of("attributes"));

		LinkedHashMap<String, Boolean> entitiesImportable = new LinkedHashMap<String, Boolean>();
		entitiesImportable.put("attributes", true);
		assertEquals(metaDataService.integrationTestMetaData(newEntitiesMetaDataMap, skipEntities, defaultPackage),
				entitiesImportable);
	}

	@Configuration
	public static class Config
	{
		@Bean
		ManageableRepositoryCollection manageableCrudRepositoryCollection()
		{
			return new InMemoryRepositoryCollection("mem");
		}
	}
}
