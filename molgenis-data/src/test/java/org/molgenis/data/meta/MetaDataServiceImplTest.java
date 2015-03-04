//package org.molgenis.data.meta;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.reset;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.testng.Assert.assertEquals;
//
//import java.util.Arrays;
//import java.util.Collections;
//
//import org.molgenis.data.Entity;
//import org.molgenis.data.ManageableCrudRepositoryCollection;
//import org.molgenis.data.Package;
//import org.molgenis.data.QueryableCrudRepository;
//import org.molgenis.data.support.DataServiceImpl;
//import org.molgenis.data.support.DefaultEntityMetaData;
//import org.molgenis.data.support.MapEntity;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
//import org.springframework.test.context.web.WebAppConfiguration;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Test;
//
//@WebAppConfiguration
//@ContextConfiguration(classes = MetaDataServiceImplTest.Config.class)
//public class MetaDataServiceImplTest extends AbstractTestNGSpringContextTests
//{
//	private MetaDataServiceImpl metaDataServiceImpl;
//
//	@Autowired
//	private ManageableCrudRepositoryCollection manageableCrudRepositoryCollection;
//
//	@Autowired
//	private QueryableCrudRepository attributeRepository;
//
//	@Autowired
//	private QueryableCrudRepository entityRepository;
//
//	@Autowired
//	private QueryableCrudRepository packageRepository;
//
//	@Autowired
//	private QueryableCrudRepository tagRepository;
//
//	private Package defaultPackage;
//
//	private PackageImpl molgenis;
//
//	@SuppressWarnings("unchecked")
//	@BeforeMethod
//	public void readPackageTree()
//	{
//		reset(manageableCrudRepositoryCollection, attributeRepository, packageRepository, entityRepository);
//		when(manageableCrudRepositoryCollection.addEntityMeta(new AttributeMetaDataMetaData())).thenReturn(
//				attributeRepository);
//		when(manageableCrudRepositoryCollection.addEntityMeta(new EntityMetaDataMetaData())).thenReturn(
//				entityRepository);
//		when(manageableCrudRepositoryCollection.addEntityMeta(new PackageMetaData())).thenReturn(packageRepository);
//		when(manageableCrudRepositoryCollection.addEntityMeta(new TagMetaData())).thenReturn(tagRepository);
//		when(tagRepository.getName()).thenReturn("Tag");
//		when(packageRepository.getName()).thenReturn("Package");
//		when(attributeRepository.getName()).thenReturn("attributes");
//		when(entityRepository.getName()).thenReturn("entities");
//
//		defaultPackage = PackageImpl.defaultPackage;
//		PackageImpl org = new PackageImpl("org", "the org package", null);
//		molgenis = new PackageImpl("molgenis", "the molgenis package", org);
//		org.addSubPackage(molgenis);
//		PackageImpl molgenis2 = new PackageImpl("molgenis2", "the molgenis2 package", org);
//		org.addSubPackage(molgenis2);
//
//		DefaultEntityMetaData personMetaData = new DefaultEntityMetaData("Person");
//		personMetaData.setDescription("A person");
//		personMetaData.setPackage(molgenis);
//		molgenis.addEntity(personMetaData);
//
//		MapEntity personEntity = new MapEntity();
//		personEntity.set(EntityMetaDataMetaData.PACKAGE, molgenis.toEntity());
//		personEntity.set(EntityMetaDataMetaData.DESCRIPTION, "A person");
//		personEntity.set(EntityMetaDataMetaData.FULL_NAME, "org_molgenis_Person");
//		personEntity.set(EntityMetaDataMetaData.SIMPLE_NAME, "Person");
//		personEntity.set(EntityMetaDataMetaData.ABSTRACT, true);
//
//		when(packageRepository.iterator()).thenReturn(
//				Arrays.asList(org.toEntity(), molgenis.toEntity(), molgenis2.toEntity(), defaultPackage.toEntity())
//						.iterator());
//
//		when(entityRepository.iterator()).thenReturn(Arrays.asList((Entity) personEntity).iterator());
//		when(attributeRepository.iterator()).thenReturn(Collections.<Entity> emptyList().iterator());
//
//		metaDataServiceImpl = new MetaDataServiceImpl(new DataServiceImpl());
//		metaDataServiceImpl.setDefaultBackend(manageableCrudRepositoryCollection);
//
//		assertEquals(metaDataServiceImpl.getRootPackages(), Arrays.asList(defaultPackage, org));
//	}
//
//	@Test
//	public void testAddAndGetEntity()
//	{
//		PackageImpl defaultPackage = (PackageImpl) PackageImpl.defaultPackage;
//		DefaultEntityMetaData coderMetaData = new DefaultEntityMetaData("Coder");
//		coderMetaData.setDescription("A coder");
//		coderMetaData.setExtends(metaDataServiceImpl.getEntityMetaData("org_molgenis_Person"));
//		coderMetaData.setPackage(defaultPackage);
//
//		MapEntity orgPackageEntity = new MapEntity();
//		orgPackageEntity.set(PackageMetaData.SIMPLE_NAME, "org");
//
//		MapEntity personEntity = new MapEntity();
//		personEntity.set(EntityMetaDataMetaData.FULL_NAME, "org_molgenis_Person");
//		personEntity.set(EntityMetaDataMetaData.SIMPLE_NAME, "Person");
//		personEntity.set(EntityMetaDataMetaData.PACKAGE, molgenis.toEntity());
//		personEntity.set(EntityMetaDataMetaData.DESCRIPTION, "A person");
//		personEntity.set(EntityMetaDataMetaData.ABSTRACT, true);
//		personEntity.set(EntityMetaDataMetaData.LABEL, "Person");
//
//		MapEntity coderEntity = new MapEntity();
//		coderEntity.set(EntityMetaDataMetaData.FULL_NAME, "Coder");
//		coderEntity.set(EntityMetaDataMetaData.SIMPLE_NAME, "Coder");
//		coderEntity.set(EntityMetaDataMetaData.PACKAGE, defaultPackage.toEntity());
//		coderEntity.set(EntityMetaDataMetaData.DESCRIPTION, "A coder");
//		coderEntity.set(EntityMetaDataMetaData.ABSTRACT, false);
//		coderEntity.set(EntityMetaDataMetaData.LABEL, "Coder");
//		coderEntity.set(EntityMetaDataMetaData.EXTENDS, personEntity);
//
//		metaDataServiceImpl.addEntityMeta(coderMetaData);
//
//		verify(entityRepository, times(1)).add(coderEntity);
//		assertEquals(coderMetaData, metaDataServiceImpl.getEntityMetaData("Coder"));
//	}
//
//	@Configuration
//	public static class Config
//	{
//		@Bean
//		ManageableCrudRepositoryCollection manageableCrudRepositoryCollection()
//		{
//			return mock(ManageableCrudRepositoryCollection.class);
//		}
//
//		@Bean
//		QueryableCrudRepository packageRepository()
//		{
//			return mock(QueryableCrudRepository.class);
//		}
//
//		@Bean
//		QueryableCrudRepository entityRepository()
//		{
//			return mock(QueryableCrudRepository.class);
//		}
//
//		@Bean
//		QueryableCrudRepository attributeRepository()
//		{
//			return mock(QueryableCrudRepository.class);
//		}
//
//		@Bean
//		QueryableCrudRepository tagRepository()
//		{
//			return mock(QueryableCrudRepository.class);
//		}
//
//	}
// }
