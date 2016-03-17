package org.molgenis.data.vcf.importer;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.permission.PermissionSystemService;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class VcfImporterServiceTest
{
	private VcfImporterService vcfImporterService;
	private DataService dataService;
	private PermissionSystemService permissionSystemService;
	private MetaDataService metaDataService;
	private SecurityContext securityContext;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
		permissionSystemService = mock(PermissionSystemService.class);
		vcfImporterService = new VcfImporterService(dataService, permissionSystemService);

		metaDataService = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(metaDataService);

		securityContext = mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void doImportVcfWithoutSamples()
	{
		// Test with multiple input repositories not possible due to
		// https://github.com/molgenis/molgenis/issues/4544

		String entityName0 = "entity0";
		List<String> entityNames = Arrays.asList(entityName0);

		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		when(entityMeta0.getName()).thenReturn(entityName0);
		when(entityMeta0.getSimpleName()).thenReturn(entityName0);
		when(entityMeta0.getOwnAttributes()).thenReturn(emptyList());
		when(entityMeta0.getOwnLookupAttributes()).thenReturn(emptyList());
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		List<Entity> entities = Arrays.asList(entity0, entity1);
		Repository repo0 = mock(Repository.class);
		when(repo0.getName()).thenReturn(entityName0);
		when(repo0.getEntityMetaData()).thenReturn(entityMeta0);
		when(repo0.stream()).thenReturn(entities.stream());
		when(repo0.iterator()).thenReturn(entities.iterator());
		when(dataService.hasRepository(entityName0)).thenReturn(false);
		Repository outRepo0 = mock(Repository.class);
		when(metaDataService.addEntityMeta(argThat(eqName(entityMeta0)))).thenReturn(outRepo0);
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
		when(source.getEntityNames()).thenReturn(entityNames);
		when(source.getRepository(entityName0)).thenReturn(repo0);
		String defaultPackage = "package";
		EntityImportReport entityImportReport = vcfImporterService.doImport(source, DatabaseAction.ADD, defaultPackage);
		EntityImportReport expectedEntityImportReport = new EntityImportReport();
		expectedEntityImportReport.addEntityCount(entityName0, entities.size());
		expectedEntityImportReport.addNewEntity(entityName0);
		assertEquals(entityImportReport, expectedEntityImportReport);

		verify(metaDataService, times(1)).addEntityMeta(argThat(eqName(entityMeta0)));
		verify(permissionSystemService, times(1)).giveUserEntityPermissions(securityContext,
				singletonList(entityName0));
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
		EntityMetaData sampleEntityMeta0 = mock(EntityMetaData.class);
		when(sampleEntityMeta0.getName()).thenReturn(sampleEntityName0);
		when(sampleEntityMeta0.getSimpleName()).thenReturn(sampleEntityName0);
		when(sampleEntityMeta0.getOwnAttributes()).thenReturn(emptyList());
		when(sampleEntityMeta0.getOwnLookupAttributes()).thenReturn(emptyList());
		Repository outSampleRepo0 = mock(Repository.class);
		when(outSampleRepo0.getName()).thenReturn(sampleEntityName0);
		when(metaDataService.addEntityMeta(argThat(eqName(sampleEntityMeta0)))).thenReturn(outSampleRepo0);

		AttributeMetaData sampleAttr = mock(AttributeMetaData.class);
		when(sampleAttr.getName()).thenReturn(VcfRepository.SAMPLES);
		when(sampleAttr.getRefEntity()).thenReturn(sampleEntityMeta0);
		when(sampleAttr.getDataType()).thenReturn(MREF);
		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		when(entityMeta0.getName()).thenReturn(entityName0);
		when(entityMeta0.getSimpleName()).thenReturn(entityName0);
		when(entityMeta0.getAttribute(VcfRepository.SAMPLES)).thenReturn(sampleAttr);
		when(entityMeta0.getOwnAttributes()).thenReturn(singletonList(sampleAttr));
		when(entityMeta0.getOwnLookupAttributes()).thenReturn(emptyList());
		Entity entity0Sample0 = mock(Entity.class);
		Entity entity0Sample1 = mock(Entity.class);
		Entity entity1Sample0 = mock(Entity.class);
		Entity entity1Sample1 = mock(Entity.class);
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntities(VcfRepository.SAMPLES)).thenReturn(Arrays.asList(entity0Sample0, entity0Sample1));
		Entity entity1 = mock(Entity.class);
		when(entity1.getEntities(VcfRepository.SAMPLES)).thenReturn(Arrays.asList(entity1Sample0, entity1Sample1));
		List<Entity> entities = Arrays.asList(entity0, entity1);
		Repository repo0 = mock(Repository.class);
		when(repo0.getName()).thenReturn(entityName0);
		when(repo0.getEntityMetaData()).thenReturn(entityMeta0);
		when(repo0.stream()).thenReturn(entities.stream());
		when(repo0.iterator()).thenReturn(entities.iterator());
		when(dataService.hasRepository(entityName0)).thenReturn(false);
		Repository outRepo0 = mock(Repository.class);
		when(metaDataService.addEntityMeta(argThat(eqName(entityMeta0)))).thenReturn(outRepo0);
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
		when(source.getEntityNames()).thenReturn(entityNames);
		when(source.getRepository(entityName0)).thenReturn(repo0);
		String defaultPackage = "package";
		EntityImportReport entityImportReport = vcfImporterService.doImport(source, DatabaseAction.ADD, defaultPackage);
		EntityImportReport expectedEntityImportReport = new EntityImportReport();
		expectedEntityImportReport.addNewEntity(sampleEntityName0);
		expectedEntityImportReport.addEntityCount(sampleEntityName0, 4);
		expectedEntityImportReport.addNewEntity(entityName0);
		expectedEntityImportReport.addEntityCount(entityName0, entities.size());
		assertEquals(entityImportReport, expectedEntityImportReport);

		verify(metaDataService, times(1)).addEntityMeta(argThat(eqName(sampleEntityMeta0)));
		verify(metaDataService, times(1)).addEntityMeta(argThat(eqName(entityMeta0)));
		verify(permissionSystemService, times(1)).giveUserEntityPermissions(securityContext,
				singletonList(entityName0));
		verify(permissionSystemService, times(1)).giveUserEntityPermissions(securityContext,
				singletonList(sampleEntityName0));
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void doImportAlreadyExists()
	{
		String entityName0 = "entity0";
		List<String> entityNames = Arrays.asList(entityName0);

		Repository repo0 = mock(Repository.class);
		when(repo0.getName()).thenReturn(entityName0);
		RepositoryCollection source = mock(RepositoryCollection.class);
		when(source.getEntityNames()).thenReturn(entityNames);
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
		AttributeMetaData attr0 = mock(AttributeMetaData.class);
		when(attr0.getName()).thenReturn(attrName0);

		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		when(entityMeta0.getName()).thenReturn(entityName0);
		when(entityMeta0.getSimpleName()).thenReturn(entityName0);
		when(entityMeta0.getOwnAttributes()).thenReturn(singletonList(attr0));
		when(entityMeta0.getAtomicAttributes()).thenReturn(singletonList(attr0));
		when(entityMeta0.getOwnLookupAttributes()).thenReturn(emptyList());

		Repository repo0 = mock(Repository.class);
		when(repo0.getName()).thenReturn(entityName0);
		when(repo0.getEntityMetaData()).thenReturn(entityMeta0);

		RepositoryCollection source = mock(RepositoryCollection.class);
		when(source.getEntityNames()).thenReturn(entityNames);
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
		AttributeMetaData attr0 = mock(AttributeMetaData.class);
		when(attr0.getName()).thenReturn(attrName0);

		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		when(entityMeta0.getName()).thenReturn(entityName0);
		when(entityMeta0.getSimpleName()).thenReturn(entityName0);
		when(entityMeta0.getOwnAttributes()).thenReturn(singletonList(attr0));
		when(entityMeta0.getAtomicAttributes()).thenReturn(singletonList(attr0));
		when(entityMeta0.getOwnLookupAttributes()).thenReturn(emptyList());

		Repository repo0 = mock(Repository.class);
		when(repo0.getName()).thenReturn(entityName0);
		when(repo0.getEntityMetaData()).thenReturn(entityMeta0);

		RepositoryCollection source = mock(RepositoryCollection.class);
		when(source.getEntityNames()).thenReturn(entityNames);
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
		AttributeMetaData attr0 = mock(AttributeMetaData.class);
		when(attr0.getName()).thenReturn(attrName0);

		String sampleAttrName0 = "sampleAttr0";
		AttributeMetaData sampleAttr0 = mock(AttributeMetaData.class);
		when(sampleAttr0.getName()).thenReturn(sampleAttrName0);

		String sampleEntityName0 = "entity0sample";
		EntityMetaData sampleEntityMeta0 = mock(EntityMetaData.class);
		when(sampleEntityMeta0.getName()).thenReturn(sampleEntityName0);
		when(sampleEntityMeta0.getSimpleName()).thenReturn(sampleEntityName0);
		when(sampleEntityMeta0.getOwnAttributes()).thenReturn(emptyList());
		when(sampleEntityMeta0.getAtomicAttributes()).thenReturn(singleton(sampleAttr0));
		when(sampleEntityMeta0.getOwnLookupAttributes()).thenReturn(emptyList());

		AttributeMetaData sampleAttr = mock(AttributeMetaData.class);
		when(sampleAttr.getName()).thenReturn(VcfRepository.SAMPLES);
		when(sampleAttr.getRefEntity()).thenReturn(sampleEntityMeta0);
		when(sampleAttr.getDataType()).thenReturn(MREF);

		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		when(entityMeta0.getName()).thenReturn(entityName0);
		when(entityMeta0.getSimpleName()).thenReturn(entityName0);
		when(entityMeta0.getAttribute(VcfRepository.SAMPLES)).thenReturn(sampleAttr);
		when(entityMeta0.getOwnAttributes()).thenReturn(singletonList(sampleAttr));
		when(entityMeta0.getAtomicAttributes()).thenReturn(singletonList(sampleAttr));
		when(entityMeta0.getOwnLookupAttributes()).thenReturn(emptyList());

		Repository repo0 = mock(Repository.class);
		when(repo0.getName()).thenReturn(entityName0);
		when(repo0.getEntityMetaData()).thenReturn(entityMeta0);

		RepositoryCollection source = mock(RepositoryCollection.class);
		when(source.getEntityNames()).thenReturn(entityNames);
		when(source.getRepository(entityName0)).thenReturn(repo0);
		EntitiesValidationReport entitiesValidationReport = vcfImporterService.validateImport(file, source);
		assertTrue(entitiesValidationReport.valid());
		assertEquals(entitiesValidationReport.getFieldsAvailable(), emptyMap());
		Map<String, List<String>> importableFields = new HashMap<String, List<String>>();
		importableFields.put(entityName0, singletonList(VcfRepository.SAMPLES));
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
		AttributeMetaData attr0 = mock(AttributeMetaData.class);
		when(attr0.getName()).thenReturn(attrName0);

		String sampleAttrName0 = "sampleAttr0";
		AttributeMetaData sampleAttr0 = mock(AttributeMetaData.class);
		when(sampleAttr0.getName()).thenReturn(sampleAttrName0);

		String sampleEntityName0 = "entity0sample";
		EntityMetaData sampleEntityMeta0 = mock(EntityMetaData.class);
		when(sampleEntityMeta0.getName()).thenReturn(sampleEntityName0);
		when(sampleEntityMeta0.getSimpleName()).thenReturn(sampleEntityName0);
		when(sampleEntityMeta0.getOwnAttributes()).thenReturn(emptyList());
		when(sampleEntityMeta0.getAtomicAttributes()).thenReturn(singleton(sampleAttr0));
		when(sampleEntityMeta0.getOwnLookupAttributes()).thenReturn(emptyList());

		AttributeMetaData sampleAttr = mock(AttributeMetaData.class);
		when(sampleAttr.getName()).thenReturn(VcfRepository.SAMPLES);
		when(sampleAttr.getRefEntity()).thenReturn(sampleEntityMeta0);
		when(sampleAttr.getDataType()).thenReturn(MREF);

		EntityMetaData entityMeta0 = mock(EntityMetaData.class);
		when(entityMeta0.getName()).thenReturn(entityName0);
		when(entityMeta0.getSimpleName()).thenReturn(entityName0);
		when(entityMeta0.getAttribute(VcfRepository.SAMPLES)).thenReturn(sampleAttr);
		when(entityMeta0.getOwnAttributes()).thenReturn(singletonList(sampleAttr));
		when(entityMeta0.getAtomicAttributes()).thenReturn(singletonList(sampleAttr));
		when(entityMeta0.getOwnLookupAttributes()).thenReturn(emptyList());

		Repository repo0 = mock(Repository.class);
		when(repo0.getName()).thenReturn(entityName0);
		when(repo0.getEntityMetaData()).thenReturn(entityMeta0);

		RepositoryCollection source = mock(RepositoryCollection.class);
		when(source.getEntityNames()).thenReturn(entityNames);
		when(source.getRepository(entityName0)).thenReturn(repo0);
		when(dataService.hasRepository(entityName0)).thenReturn(true);
		when(dataService.hasRepository(sampleEntityName0)).thenReturn(true);
		EntitiesValidationReport entitiesValidationReport = vcfImporterService.validateImport(file, source);
		assertFalse(entitiesValidationReport.valid());
		assertEquals(entitiesValidationReport.getFieldsAvailable(), emptyMap());
		Map<String, List<String>> importableFields = new HashMap<String, List<String>>();
		importableFields.put(entityName0, singletonList(VcfRepository.SAMPLES));
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

	private static Matcher<EntityMetaData> eqName(EntityMetaData expectedEntityMeta)
	{
		return new BaseMatcher<EntityMetaData>()
		{
			@Override
			public boolean matches(Object item)
			{
				if (!(item instanceof EntityMetaData))
				{
					return false;
				}
				return ((EntityMetaData) item).getName().equals(expectedEntityMeta.getName());
			}

			@Override
			public void describeTo(Description description)
			{
				description.appendText("is EntityMetaData with same name");
			}
		};
	}
}
