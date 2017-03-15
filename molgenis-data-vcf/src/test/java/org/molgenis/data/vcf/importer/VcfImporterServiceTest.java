package org.molgenis.data.vcf.importer;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.*;
import org.molgenis.data.importer.EntitiesValidationReport;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.testng.Assert.*;

public class VcfImporterServiceTest extends AbstractMockitoTest
{
	private VcfImporterService vcfImporterService;
	@Mock
	private DataService dataService;
	@Mock
	private PermissionSystemService permissionSystemService;
	@Mock
	private MetaDataService metaDataService;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private RepositoryCollection repositoryCollection;

	@Captor
	private ArgumentCaptor<Consumer<List<Entity>>> consumerArgumentCaptor;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		vcfImporterService = new VcfImporterService(dataService, permissionSystemService, metaDataService);
		when(dataService.getMeta()).thenReturn(metaDataService);
		SecurityContextHolder.setContext(securityContext);
		when(metaDataService.getDefaultBackend()).thenReturn(repositoryCollection);
		when(repositoryCollection.getName()).thenReturn("default");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void doImportVcfWithoutSamples()
	{
		// Test with multiple input repositories not possible due to
		// https://github.com/molgenis/molgenis/issues/4544

		String entityName0 = "entity0";
		List<String> entityNames = Arrays.asList(entityName0);

		EntityType entityType0 = mock(EntityType.class);
		when(entityType0.getFullyQualifiedName()).thenReturn(entityName0);
		when(entityType0.getName()).thenReturn(entityName0);
		when(entityType0.getOwnAttributes()).thenReturn(emptyList());
		when(entityType0.getOwnLookupAttributes()).thenReturn(emptyList());
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		List<Entity> entities = Arrays.asList(entity0, entity1);
		Repository<Entity> repo0 = Mockito.spy(new AbstractRepository()
		{
			@Override
			public Set<RepositoryCapability> getCapabilities()
			{
				return null;
			}

			public EntityType getEntityType()
			{
				return entityType0;
			}

			@Override
			public Iterator<Entity> iterator()
			{
				return entities.iterator();
			}

			@Override
			public Spliterator<Entity> spliterator()
			{
				return entities.spliterator();
			}

			@Override
			public String getName()
			{
				return entityName0;
			}

			@Override
			public void forEachBatched(Consumer<List<Entity>> consumer, int batchSize)
			{
				this.forEachBatched(null, consumer, batchSize);
			}
		});
		when(dataService.hasRepository(entityName0)).thenReturn(false);
		Repository<Entity> outRepo0 = mock(Repository.class);
		when(metaDataService.createRepository(argThat(eqName(entityType0)))).thenReturn(outRepo0);
		when(outRepo0.add(any(Stream.class))).thenAnswer(new Answer<Integer>()
		{
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable
			{
				Stream<Entity> entities = (Stream<Entity>) invocation.getArguments()[0];
				List<Entity> entityList = entities.collect(Collectors.toList());
				return entityList.size();
			}
		});
		RepositoryCollection source = mock(RepositoryCollection.class);
		when(source.getEntityIds()).thenReturn(entityNames);
		when(source.getRepository(entityName0)).thenReturn(repo0);
		String defaultPackage = "package";
		EntityImportReport entityImportReport = vcfImporterService.doImport(source, DatabaseAction.ADD, defaultPackage);
		EntityImportReport expectedEntityImportReport = new EntityImportReport();
		expectedEntityImportReport.addEntityCount(entityName0, entities.size());
		expectedEntityImportReport.addNewEntity(entityName0);
		assertEquals(entityImportReport, expectedEntityImportReport);

		verify(metaDataService, times(1)).createRepository(argThat(eqName(entityType0)));
		verify(permissionSystemService, times(1)).giveUserWriteMetaPermissions(entityType0);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void doImportVcfWithSamples()
	{
		// Test with multiple input repositories not possible due to
		// https://github.com/molgenis/molgenis/issues/4544

		String entityName0 = "entity0";
		List<String> entityNames = Arrays.asList(entityName0);

		String sampleEntityName0 = "entity0sample";
		EntityType sampleEntityType0 = mock(EntityType.class);
		when(sampleEntityType0.getFullyQualifiedName()).thenReturn(sampleEntityName0);
		when(sampleEntityType0.getName()).thenReturn(sampleEntityName0);
		when(sampleEntityType0.getOwnAttributes()).thenReturn(emptyList());
		when(sampleEntityType0.getOwnLookupAttributes()).thenReturn(emptyList());
		Repository<Entity> outSampleRepo0 = mock(Repository.class);
		when(outSampleRepo0.getName()).thenReturn(sampleEntityName0);
		when(metaDataService.createRepository(argThat(eqName(sampleEntityType0)))).thenReturn(outSampleRepo0);

		Attribute sampleAttr = mock(Attribute.class);
		when(sampleAttr.getName()).thenReturn(VcfAttributes.SAMPLES);
		when(sampleAttr.getRefEntity()).thenReturn(sampleEntityType0);
		when(sampleAttr.getDataType()).thenReturn(MREF);
		EntityType entityType0 = mock(EntityType.class);
		when(entityType0.getFullyQualifiedName()).thenReturn(entityName0);
		when(entityType0.getName()).thenReturn(entityName0);
		when(entityType0.getAttribute(VcfAttributes.SAMPLES)).thenReturn(sampleAttr);
		when(entityType0.getOwnAttributes()).thenReturn(singletonList(sampleAttr));
		when(entityType0.getOwnLookupAttributes()).thenReturn(emptyList());
		Entity entity0Sample0 = mock(Entity.class);
		Entity entity0Sample1 = mock(Entity.class);
		Entity entity1Sample0 = mock(Entity.class);
		Entity entity1Sample1 = mock(Entity.class);
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntities(VcfAttributes.SAMPLES)).thenReturn(Arrays.asList(entity0Sample0, entity0Sample1));
		Entity entity1 = mock(Entity.class);
		when(entity1.getEntities(VcfAttributes.SAMPLES)).thenReturn(Arrays.asList(entity1Sample0, entity1Sample1));
		List<Entity> entities = Arrays.asList(entity0, entity1);
		Repository<Entity> repo0 = Mockito.spy(new AbstractRepository()
		{
			@Override
			public Set<RepositoryCapability> getCapabilities()
			{
				return null;
			}

			public EntityType getEntityType()
			{
				return entityType0;
			}

			@Override
			public Iterator<Entity> iterator()
			{
				return entities.iterator();
			}

			@Override
			public Spliterator<Entity> spliterator()
			{
				return entities.spliterator();
			}

			@Override
			public String getName()
			{
				return entityName0;
			}

			@Override
			public void forEachBatched(Consumer<List<Entity>> consumer, int batchSize)
			{
				this.forEachBatched(null, consumer, batchSize);
			}
		});
		when(dataService.hasRepository(entityName0)).thenReturn(false);
		Repository<Entity> outRepo0 = mock(Repository.class);
		when(metaDataService.createRepository(argThat(eqName(entityType0)))).thenReturn(outRepo0);
		when(outRepo0.add(any(Stream.class))).thenAnswer(new Answer<Integer>()
		{
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable
			{
				Stream<Entity> entities = (Stream<Entity>) invocation.getArguments()[0];
				List<Entity> entityList = entities.collect(Collectors.toList());
				return entityList.size();
			}
		});
		RepositoryCollection source = mock(RepositoryCollection.class);
		when(source.getEntityIds()).thenReturn(entityNames);
		when(source.getRepository(entityName0)).thenReturn(repo0);
		String defaultPackage = "package";
		EntityImportReport entityImportReport = vcfImporterService.doImport(source, DatabaseAction.ADD, defaultPackage);
		EntityImportReport expectedEntityImportReport = new EntityImportReport();
		expectedEntityImportReport.addNewEntity(sampleEntityName0);
		expectedEntityImportReport.addEntityCount(sampleEntityName0, 4);
		expectedEntityImportReport.addNewEntity(entityName0);
		expectedEntityImportReport.addEntityCount(entityName0, entities.size());
		assertEquals(entityImportReport, expectedEntityImportReport);

		verify(metaDataService).createRepository(argThat(eqName(sampleEntityType0)));
		verify(metaDataService).createRepository(argThat(eqName(entityType0)));
		verify(permissionSystemService).giveUserWriteMetaPermissions(entityType0);
		verify(permissionSystemService).giveUserWriteMetaPermissions(sampleEntityType0);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void doImportAlreadyExists()
	{
		String entityName0 = "entity0";
		List<String> entityNames = Arrays.asList(entityName0);

		@SuppressWarnings("unchecked")
		Repository<Entity> repo0 = mock(Repository.class);
		when(repo0.getName()).thenReturn(entityName0);
		RepositoryCollection source = mock(RepositoryCollection.class);
		when(source.getEntityIds()).thenReturn(entityNames);
		when(source.getRepository(entityName0)).thenReturn(repo0);
		when(dataService.hasRepository(entityName0)).thenReturn(true);
		String defaultPackage = "package";
		vcfImporterService.doImport(source, DatabaseAction.ADD, defaultPackage);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void doImportAddIgnoreExisting()
	{
		RepositoryCollection source = mock(RepositoryCollection.class);
		String defaultPackage = "package";
		vcfImporterService.doImport(source, DatabaseAction.ADD_IGNORE_EXISTING, defaultPackage);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void doImportAddUpdateExisting()
	{
		RepositoryCollection source = mock(RepositoryCollection.class);
		String defaultPackage = "package";
		vcfImporterService.doImport(source, DatabaseAction.ADD_UPDATE_EXISTING, defaultPackage);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void doImportUpdate()
	{
		RepositoryCollection source = mock(RepositoryCollection.class);
		String defaultPackage = "package";
		vcfImporterService.doImport(source, DatabaseAction.UPDATE, defaultPackage);
	}

	@Test
	public void validateImportWithoutSamples()
	{
		// Test with multiple input repositories not possible due to
		// https://github.com/molgenis/molgenis/issues/4544

		File file = mock(File.class);

		String entityName0 = "entity0";
		List<String> entityNames = Arrays.asList(entityName0);

		String attrName0 = "attr0";
		Attribute attr0 = mock(Attribute.class);
		when(attr0.getName()).thenReturn(attrName0);

		EntityType entityType0 = mock(EntityType.class);
		when(entityType0.getFullyQualifiedName()).thenReturn(entityName0);
		when(entityType0.getName()).thenReturn(entityName0);
		when(entityType0.getOwnAttributes()).thenReturn(singletonList(attr0));
		when(entityType0.getAtomicAttributes()).thenReturn(singletonList(attr0));
		when(entityType0.getOwnLookupAttributes()).thenReturn(emptyList());

		@SuppressWarnings("unchecked")
		Repository<Entity> repo0 = mock(Repository.class);
		when(repo0.getName()).thenReturn(entityName0);
		when(repo0.getEntityType()).thenReturn(entityType0);

		RepositoryCollection source = mock(RepositoryCollection.class);
		when(source.getEntityIds()).thenReturn(entityNames);
		when(source.getRepository(entityName0)).thenReturn(repo0);
		EntitiesValidationReport entitiesValidationReport = vcfImporterService.validateImport(file, source);
		assertTrue(entitiesValidationReport.valid());
		assertEquals(entitiesValidationReport.getFieldsAvailable(), emptyMap());
		assertEquals(entitiesValidationReport.getFieldsImportable(),
				singletonMap(entityName0, singletonList(attrName0)));
		assertEquals(entitiesValidationReport.getFieldsRequired(), emptyMap());
		assertEquals(entitiesValidationReport.getFieldsUnknown(), emptyMap());
		assertEquals(entitiesValidationReport.getImportOrder(), emptyList());
		assertEquals(entitiesValidationReport.getPackages(), emptyList());
		assertEquals(entitiesValidationReport.getSheetsImportable(), singletonMap(entityName0, Boolean.TRUE));
	}

	@Test
	public void validateImportWithoutSamplesAlreadyExists()
	{
		// Test with multiple input repositories not possible due to
		// https://github.com/molgenis/molgenis/issues/4544

		File file = mock(File.class);

		String entityName0 = "entity0";
		List<String> entityNames = Arrays.asList(entityName0);

		String attrName0 = "attr0";
		Attribute attr0 = mock(Attribute.class);
		when(attr0.getName()).thenReturn(attrName0);

		EntityType entityType0 = mock(EntityType.class);
		when(entityType0.getFullyQualifiedName()).thenReturn(entityName0);
		when(entityType0.getName()).thenReturn(entityName0);
		when(entityType0.getOwnAttributes()).thenReturn(singletonList(attr0));
		when(entityType0.getAtomicAttributes()).thenReturn(singletonList(attr0));
		when(entityType0.getOwnLookupAttributes()).thenReturn(emptyList());

		@SuppressWarnings("unchecked")
		Repository<Entity> repo0 = mock(Repository.class);
		when(repo0.getName()).thenReturn(entityName0);
		when(repo0.getEntityType()).thenReturn(entityType0);

		RepositoryCollection source = mock(RepositoryCollection.class);
		when(source.getEntityIds()).thenReturn(entityNames);
		when(source.getRepository(entityName0)).thenReturn(repo0);
		when(dataService.hasRepository(entityName0)).thenReturn(true);
		EntitiesValidationReport entitiesValidationReport = vcfImporterService.validateImport(file, source);
		assertFalse(entitiesValidationReport.valid());
		assertEquals(entitiesValidationReport.getFieldsAvailable(), emptyMap());
		assertEquals(entitiesValidationReport.getFieldsImportable(),
				singletonMap(entityName0, singletonList(attrName0)));
		assertEquals(entitiesValidationReport.getFieldsRequired(), emptyMap());
		assertEquals(entitiesValidationReport.getFieldsUnknown(), emptyMap());
		assertEquals(entitiesValidationReport.getImportOrder(), emptyList());
		assertEquals(entitiesValidationReport.getPackages(), emptyList());
		assertEquals(entitiesValidationReport.getSheetsImportable(), singletonMap(entityName0, Boolean.FALSE));
	}

	@Test
	public void validateImportWithSamples()
	{
		// Test with multiple input repositories not possible due to
		// https://github.com/molgenis/molgenis/issues/4544

		File file = mock(File.class);

		String entityName0 = "entity0";
		List<String> entityNames = Arrays.asList(entityName0);

		String attrName0 = "attr0";
		Attribute attr0 = mock(Attribute.class);
		when(attr0.getName()).thenReturn(attrName0);

		String sampleAttrName0 = "sampleAttr0";
		Attribute sampleAttr0 = mock(Attribute.class);
		when(sampleAttr0.getName()).thenReturn(sampleAttrName0);

		String sampleEntityName0 = "entity0sample";
		EntityType sampleEntityType0 = mock(EntityType.class);
		when(sampleEntityType0.getFullyQualifiedName()).thenReturn(sampleEntityName0);
		when(sampleEntityType0.getName()).thenReturn(sampleEntityName0);
		when(sampleEntityType0.getOwnAttributes()).thenReturn(emptyList());
		when(sampleEntityType0.getAtomicAttributes()).thenReturn(singleton(sampleAttr0));
		when(sampleEntityType0.getOwnLookupAttributes()).thenReturn(emptyList());

		Attribute sampleAttr = mock(Attribute.class);
		when(sampleAttr.getName()).thenReturn(VcfAttributes.SAMPLES);
		when(sampleAttr.getRefEntity()).thenReturn(sampleEntityType0);
		when(sampleAttr.getDataType()).thenReturn(MREF);

		EntityType entityType0 = mock(EntityType.class);
		when(entityType0.getFullyQualifiedName()).thenReturn(entityName0);
		when(entityType0.getName()).thenReturn(entityName0);
		when(entityType0.getAttribute(VcfAttributes.SAMPLES)).thenReturn(sampleAttr);
		when(entityType0.getOwnAttributes()).thenReturn(singletonList(sampleAttr));
		when(entityType0.getAtomicAttributes()).thenReturn(singletonList(sampleAttr));
		when(entityType0.getOwnLookupAttributes()).thenReturn(emptyList());

		@SuppressWarnings("unchecked")
		Repository<Entity> repo0 = mock(Repository.class);
		when(repo0.getName()).thenReturn(entityName0);
		when(repo0.getEntityType()).thenReturn(entityType0);

		RepositoryCollection source = mock(RepositoryCollection.class);
		when(source.getEntityIds()).thenReturn(entityNames);
		when(source.getRepository(entityName0)).thenReturn(repo0);
		EntitiesValidationReport entitiesValidationReport = vcfImporterService.validateImport(file, source);
		assertTrue(entitiesValidationReport.valid());
		assertEquals(entitiesValidationReport.getFieldsAvailable(), emptyMap());
		Map<String, List<String>> importableFields = new HashMap<String, List<String>>();
		importableFields.put(entityName0, singletonList(VcfAttributes.SAMPLES));
		importableFields.put(sampleEntityName0, singletonList(sampleAttrName0));
		assertEquals(entitiesValidationReport.getFieldsImportable(), importableFields);
		assertEquals(entitiesValidationReport.getFieldsRequired(), emptyMap());
		assertEquals(entitiesValidationReport.getFieldsUnknown(), emptyMap());
		assertEquals(entitiesValidationReport.getImportOrder(), emptyList());
		assertEquals(entitiesValidationReport.getPackages(), emptyList());
		Map<String, Boolean> sheetsImportable = new HashMap<>();
		sheetsImportable.put(entityName0, Boolean.TRUE);
		sheetsImportable.put(sampleEntityName0, Boolean.TRUE);
		assertEquals(entitiesValidationReport.getSheetsImportable(), sheetsImportable);
	}

	@Test
	public void validateImportWithSamplesAlreadyExists()
	{
		// Test with multiple input repositories not possible due to
		// https://github.com/molgenis/molgenis/issues/4544

		File file = mock(File.class);

		String entityName0 = "entity0";
		List<String> entityNames = Arrays.asList(entityName0);

		String attrName0 = "attr0";
		Attribute attr0 = mock(Attribute.class);
		when(attr0.getName()).thenReturn(attrName0);

		String sampleAttrName0 = "sampleAttr0";
		Attribute sampleAttr0 = mock(Attribute.class);
		when(sampleAttr0.getName()).thenReturn(sampleAttrName0);

		String sampleEntityName0 = "entity0sample";
		EntityType sampleEntityType0 = mock(EntityType.class);
		when(sampleEntityType0.getFullyQualifiedName()).thenReturn(sampleEntityName0);
		when(sampleEntityType0.getName()).thenReturn(sampleEntityName0);
		when(sampleEntityType0.getOwnAttributes()).thenReturn(emptyList());
		when(sampleEntityType0.getAtomicAttributes()).thenReturn(singleton(sampleAttr0));
		when(sampleEntityType0.getOwnLookupAttributes()).thenReturn(emptyList());

		Attribute sampleAttr = mock(Attribute.class);
		when(sampleAttr.getName()).thenReturn(VcfAttributes.SAMPLES);
		when(sampleAttr.getRefEntity()).thenReturn(sampleEntityType0);
		when(sampleAttr.getDataType()).thenReturn(MREF);

		EntityType entityType0 = mock(EntityType.class);
		when(entityType0.getFullyQualifiedName()).thenReturn(entityName0);
		when(entityType0.getName()).thenReturn(entityName0);
		when(entityType0.getAttribute(VcfAttributes.SAMPLES)).thenReturn(sampleAttr);
		when(entityType0.getOwnAttributes()).thenReturn(singletonList(sampleAttr));
		when(entityType0.getAtomicAttributes()).thenReturn(singletonList(sampleAttr));
		when(entityType0.getOwnLookupAttributes()).thenReturn(emptyList());

		@SuppressWarnings("unchecked")
		Repository<Entity> repo0 = mock(Repository.class);
		when(repo0.getName()).thenReturn(entityName0);
		when(repo0.getEntityType()).thenReturn(entityType0);

		RepositoryCollection source = mock(RepositoryCollection.class);
		when(source.getEntityIds()).thenReturn(entityNames);
		when(source.getRepository(entityName0)).thenReturn(repo0);
		when(dataService.hasRepository(entityName0)).thenReturn(true);
		when(dataService.hasRepository(sampleEntityName0)).thenReturn(true);
		EntitiesValidationReport entitiesValidationReport = vcfImporterService.validateImport(file, source);
		assertFalse(entitiesValidationReport.valid());
		assertEquals(entitiesValidationReport.getFieldsAvailable(), emptyMap());
		Map<String, List<String>> importableFields = new HashMap<String, List<String>>();
		importableFields.put(entityName0, singletonList(VcfAttributes.SAMPLES));
		importableFields.put(sampleEntityName0, singletonList(sampleAttrName0));
		assertEquals(entitiesValidationReport.getFieldsImportable(), importableFields);
		assertEquals(entitiesValidationReport.getFieldsRequired(), emptyMap());
		assertEquals(entitiesValidationReport.getFieldsUnknown(), emptyMap());
		assertEquals(entitiesValidationReport.getImportOrder(), emptyList());
		assertEquals(entitiesValidationReport.getPackages(), emptyList());
		Map<String, Boolean> sheetsImportable = new HashMap<>();
		sheetsImportable.put(entityName0, Boolean.FALSE);
		sheetsImportable.put(sampleEntityName0, Boolean.FALSE);
		assertEquals(entitiesValidationReport.getSheetsImportable(), sheetsImportable);
	}

	@Test
	public void canImportVcf()
	{
		RepositoryCollection source = mock(RepositoryCollection.class);
		assertTrue(vcfImporterService.canImport(new File("file.vcf"), source));
	}

	@Test
	public void canImportVcfGz()
	{
		RepositoryCollection source = mock(RepositoryCollection.class);
		assertTrue(vcfImporterService.canImport(new File("file.vcf.gz"), source));
	}

	@Test
	public void canImportXls()
	{
		RepositoryCollection source = mock(RepositoryCollection.class);
		assertFalse(vcfImporterService.canImport(new File("file.xls"), source));
	}

	private static Matcher<EntityType> eqName(EntityType expectedEntityType)
	{
		return new BaseMatcher<EntityType>()
		{
			@Override
			public boolean matches(Object item)
			{
				if (!(item instanceof EntityType))
				{
					return false;
				}
				return ((EntityType) item).getFullyQualifiedName().equals(expectedEntityType.getFullyQualifiedName());
			}

			@Override
			public void describeTo(Description description)
			{
				description.appendText("is EntityType with same name");
			}
		};
	}
}
