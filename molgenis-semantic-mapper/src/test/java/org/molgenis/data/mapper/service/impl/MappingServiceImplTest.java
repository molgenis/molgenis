package org.molgenis.data.mapper.service.impl;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.auth.User;
import org.molgenis.auth.UserFactory;
import org.molgenis.data.*;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.config.UserTestConfig;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.meta.MappingTargetMetaData;
import org.molgenis.data.mapper.repository.MappingProjectRepository;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.meta.DefaultPackage;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.*;
import static org.molgenis.data.mapper.meta.MappingProjectMetaData.*;
import static org.molgenis.data.mapper.service.impl.MappingServiceImpl.MAPPING_BATCH_SIZE;
import static org.molgenis.data.mapper.service.impl.MappingServiceImpl.SOURCE;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { MappingServiceImplTest.Config.class, MappingServiceImpl.class,
		EntityBaseTestConfig.class, DefaultPackage.class })
public class MappingServiceImplTest extends AbstractMolgenisSpringTest
{
	private static final String TARGET_HOP_ENTITY = "HopEntity";
	private static final String USERNAME = "admin";
	private static final String SOURCE_GENE_ENTITY = "Gene";
	private static final String SOURCE_EXON_ENTITY = "Exon";

	@Autowired
	private DataService dataService;

	@Autowired
	private AlgorithmService algorithmService;

	@Autowired
	private MappingProjectRepository mappingProjectRepo;

	@Autowired
	private PermissionSystemService permissionSystemService;

	@Autowired
	private AttributeFactory attrMetaFactory;

	@Autowired
	private MappingServiceImpl mappingService;

	@Autowired
	private UserFactory userFactory;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private PackageFactory packageFactory;

	@Mock
	private Repository<Entity> hopRepo;

	@Mock
	private Repository<Entity> geneRepo;

	@Mock
	private Repository<Entity> exonRepo;

	@Mock
	private Progress progress;

	@Captor
	private ArgumentCaptor<List<Entity>> batchCaptor;

	private MetaDataService metaDataService;

	private User user;
	private EntityType hopMetaData;
	private EntityType geneMetaData;

	private Package package_;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeMethod()
	{
		reset(dataService);
		reset(mappingProjectRepo);
		reset(permissionSystemService);
		reset(hopRepo);
		reset(geneRepo);
		reset(exonRepo);
		reset(progress);

		user = userFactory.create();
		user.setUsername(USERNAME);

		package_ = packageFactory.create("package");

		hopMetaData = entityTypeFactory.create(TARGET_HOP_ENTITY).setPackage(package_);
		hopMetaData.addAttribute(attrMetaFactory.create().setName("identifier"), ROLE_ID);
		hopMetaData.addAttribute(attrMetaFactory.create().setName("height").setDataType(DECIMAL).setNillable(false));

		geneMetaData = entityTypeFactory.create(SOURCE_GENE_ENTITY).setPackage(package_);
		geneMetaData.setLabel("Genes");
		geneMetaData.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);
		geneMetaData.addAttribute(attrMetaFactory.create().setName("length").setDataType(DECIMAL).setNillable(false));

		EntityType exonMetaData = entityTypeFactory.create(SOURCE_EXON_ENTITY).setPackage(package_);
		exonMetaData.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);
		exonMetaData.addAttribute(
				attrMetaFactory.create().setName("basepairs").setDataType(DECIMAL).setNillable(false));

		metaDataService = mock(MetaDataService.class);
		when(metaDataService.createRepository(argThat(obj -> obj != null && obj.getId().equals(hopMetaData.getId()))))
				.thenReturn(hopRepo);
		when(metaDataService.createRepository(argThat(obj -> obj != null && obj.getId().equals(geneMetaData.getId()))))
				.thenReturn(geneRepo);
		when(metaDataService.createRepository(argThat(obj -> obj != null && obj.getId().equals(SOURCE_EXON_ENTITY))))
				.thenReturn(exonRepo);

		when(hopRepo.getName()).thenReturn(TARGET_HOP_ENTITY);
		when(hopRepo.getEntityType()).thenReturn(hopMetaData);
		when(geneRepo.getName()).thenReturn(geneMetaData.getId());
		when(geneRepo.iterator()).thenReturn(Collections.<Entity>emptyList().iterator());
		when(geneRepo.getEntityType()).thenReturn(geneMetaData);
		when(exonRepo.getName()).thenReturn(SOURCE_EXON_ENTITY);
		when(exonRepo.iterator()).thenReturn(Collections.<Entity>emptyList().iterator());
		when(exonRepo.getEntityType()).thenReturn(exonMetaData);
		when(dataService.getMeta()).thenReturn(metaDataService);
		when(dataService.getEntityType(hopMetaData.getId())).thenReturn(hopMetaData);
		when(dataService.getRepository(TARGET_HOP_ENTITY)).thenReturn(hopRepo);
		Query<Entity> hopQ = mock(Query.class);
		when(hopRepo.query()).thenReturn(hopQ);
		when(hopQ.eq("source", geneMetaData.getId())).thenReturn(hopQ);
		when(hopQ.eq("source", SOURCE_EXON_ENTITY)).thenReturn(hopQ);
		when(hopQ.findAll()).thenAnswer(invocation -> Stream.empty());

		when(dataService.getEntityType(geneMetaData.getId())).thenReturn(geneMetaData);
		when(dataService.getRepository(geneMetaData.getId())).thenReturn(geneRepo);
		when(dataService.getEntityType(SOURCE_EXON_ENTITY)).thenReturn(exonMetaData);
		when(dataService.getRepository(SOURCE_EXON_ENTITY)).thenReturn(exonRepo);
	}

	@Test
	public void testAddMappingProject()
	{
		String projectName = "test_project";
		MappingProject mappingProject = mappingService.addMappingProject(projectName, user, hopMetaData.getId());
		verify(mappingProjectRepo, times(1)).add(mappingProject);
		assertEquals(mappingProject.getName(), projectName);
		assertEquals(mappingProject.getOwner(), user);
		List<MappingTarget> mappingTargets = mappingProject.getMappingTargets();
		assertEquals(mappingTargets.size(), 1);
		assertEquals(mappingTargets.get(0).getTarget(), hopMetaData);
	}

	@Test
	public void testGetMappingProject()
	{
		String projectName = "test_project";

		MappingProject actualAddedMappingProject = mappingService.addMappingProject(projectName, user,
				hopMetaData.getId());

		String mappingTargetIdentifier = actualAddedMappingProject.getMappingTarget(hopMetaData.getId())
																  .getIdentifier();
		MappingTarget expectedMappingTarget = getManualMappingTarget(mappingTargetIdentifier, emptyList());

		String mappingProjectIdentifier = actualAddedMappingProject.getIdentifier();
		MappingProject expectedMappingProject = getManualMappingProject(mappingProjectIdentifier, projectName,
				expectedMappingTarget);

		Entity mappingTargetEntity = mock(Entity.class);
		when(mappingTargetEntity.getString(MappingTargetMetaData.IDENTIFIER)).thenReturn(mappingTargetIdentifier);
		when(mappingTargetEntity.getString(MappingTargetMetaData.TARGET)).thenReturn(hopMetaData.getId());
		when(mappingTargetEntity.getEntities(MappingTargetMetaData.ENTITY_MAPPINGS)).thenReturn(null);
		when(dataService.getEntityType(mappingTargetEntity.getString(MappingTargetMetaData.TARGET))).thenReturn(
				hopMetaData);

		Entity mappingProjectEntity = mock(Entity.class);
		when(mappingProjectEntity.get(IDENTIFIER)).thenReturn(mappingProjectIdentifier);
		when(mappingProjectEntity.get(NAME)).thenReturn(projectName);
		when(mappingProjectEntity.get(OWNER)).thenReturn(user);
		when(mappingProjectEntity.getEntities(MAPPING_TARGETS)).thenReturn(singletonList(mappingTargetEntity));
		when(mappingProjectEntity.get(MAPPING_TARGETS)).thenReturn(singletonList(expectedMappingTarget));

		when(dataService.findOneById(MAPPING_PROJECT, mappingProjectIdentifier)).thenReturn(mappingProjectEntity);
		when(mappingProjectRepo.getMappingProject(mappingProjectIdentifier)).thenReturn(actualAddedMappingProject);
		MappingProject retrievedMappingProject = mappingService.getMappingProject(mappingProjectIdentifier);

		assertEquals(retrievedMappingProject, expectedMappingProject);
	}

	@Test
	public void testCloneMappingProjectString()
	{
		MappingProject mappingProject = mock(MappingProject.class);
		when(mappingProjectRepo.getMappingProject("0")).thenReturn(mappingProject);
		mappingService.cloneMappingProject("0");
		verify(mappingProject).removeIdentifiers();
		verify(mappingProjectRepo).add(mappingProject);
	}

	@Test
	public void testCloneMappingProjectStringString()
	{
		MappingProject mappingProject = mock(MappingProject.class);
		when(mappingProjectRepo.getMappingProject("0")).thenReturn(mappingProject);
		mappingService.cloneMappingProject("0", "newName");
		verify(mappingProject).removeIdentifiers();
		verify(mappingProject).setName("newName");
		verify(mappingProjectRepo).add(mappingProject);
	}

	@Test
	public void testUpdateMappingProject()
	{
		MappingProject mappingProject = mock(MappingProject.class);
		mappingService.updateMappingProject(mappingProject);
		verifyZeroInteractions(mappingProject);
		verify(mappingProjectRepo).update(mappingProject);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testAddExistingTarget()
	{
		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, hopMetaData.getId());
		mappingProject.addTarget(hopMetaData);
	}

	@Test
	public void createMetaWithNullParameters()
	{
		MappingTarget mappingTarget = mock(MappingTarget.class);
		when(mappingTarget.getTarget()).thenReturn(hopMetaData);
		EntityType targetMetadata = mappingService.createTargetMetadata(mappingTarget, "target id", null, null, null);
		assertEquals(targetMetadata.getId(), "target id");
		assertEquals(targetMetadata.getLabel(), "target id");
		assertEquals(targetMetadata.getPackage().getId(), "base");
		assertNull(targetMetadata.getAttribute(SOURCE));
	}

	@Test
	public void createMetaWithNonNullParameters()
	{
		MappingTarget mappingTarget = mock(MappingTarget.class);
		when(mappingTarget.getTarget()).thenReturn(hopMetaData);

		Package targetPackage = mock(Package.class);
		when(metaDataService.getPackage("targetPackage")).thenReturn(targetPackage);

		EntityType targetMetadata = mappingService.createTargetMetadata(mappingTarget, "test", "targetPackage",
				"target label", true);
		assertEquals(targetMetadata.getId(), "test");
		assertEquals(targetMetadata.getLabel(), "target label");
		assertEquals(targetMetadata.getPackage(), targetPackage);
		assertNotNull(targetMetadata.getAttribute(SOURCE));
	}

	/**
	 * New entities in the source should be added to the target when a new mapping to the same target is performed.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testApplyMappingsAdd()
	{
		String entityTypeId = "addEntity";
		@SuppressWarnings("unchecked")
		Repository<Entity> addEntityRepo = mock(Repository.class);
		when(addEntityRepo.getName()).thenReturn(entityTypeId);
		EntityType targetMeta = entityTypeFactory.create(TARGET_HOP_ENTITY).setPackage(package_);
		targetMeta.addAttribute(attrMetaFactory.create().setName("identifier"), ROLE_ID);
		targetMeta.addAttribute(attrMetaFactory.create().setName("height").setDataType(DECIMAL).setNillable(false));
		targetMeta.addAttribute(attrMetaFactory.create().setName("source"));

		when(addEntityRepo.getEntityType()).thenReturn(targetMeta);
		when(metaDataService.createRepository(argThat(obj -> obj.getId().equals(entityTypeId))))
				.thenReturn(addEntityRepo);

		// add an entity to the source
		List<Entity> sourceGeneEntities = newArrayList();
		List<Entity> expectedEntities = newArrayList();
		createEntities(targetMeta, sourceGeneEntities, expectedEntities);

		doAnswer(invocationOnMock ->
		{
			@SuppressWarnings("unchecked")
			Consumer<List<Entity>> consumer = (Consumer<List<Entity>>) invocationOnMock.<Consumer>getArgument(0);

			consumer.accept(sourceGeneEntities);
			return null;
		}).when(geneRepo).forEachBatched(any(Consumer.class), eq(MAPPING_BATCH_SIZE));

		// make project and apply mappings once
		MappingProject project = createMappingProjectWithMappings();

		// apply mapping again
		assertEquals(mappingService.applyMappings("TestRun", entityTypeId, true, "packageId", "label", progress), 4);

		verify(geneRepo).forEachBatched(any(Consumer.class), any(Integer.class));

		ArgumentCaptor<EntityType> entityTypeCaptor = ArgumentCaptor.forClass(EntityType.class);
		verify(permissionSystemService).giveUserWriteMetaPermissions(entityTypeCaptor.capture());
		assertEquals(entityTypeCaptor.getValue().getId(), entityTypeId);
		verify(progress).setProgressMax(anyInt());
		verify(progress).progress(0, "Checking target repository [addEntity]...");
		verify(progress).status("Applying mappings to repository [HopEntity]");
		verify(progress).status("Mapping source [Genes]...");
		verify(progress).increment(1);
		verify(progress).status("Mapped 4 [Genes] entities.");
		verify(progress).status("Done applying mappings to repository [HopEntity]");
		verifyNoMoreInteractions(progress);
	}

	/**
	 * Applying a mapping multiple times to the same target should update the existing entities.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testApplyMappingsUpdate()
	{
		String entityTypeId = "updateEntity";

		@SuppressWarnings("unchecked")
		Repository<Entity> updateEntityRepo = mock(Repository.class);
		when(updateEntityRepo.getName()).thenReturn(entityTypeId);
		EntityType targetMeta = entityTypeFactory.create(TARGET_HOP_ENTITY).setPackage(package_);
		targetMeta.addAttribute(attrMetaFactory.create().setName("identifier"), ROLE_ID);
		targetMeta.addAttribute(attrMetaFactory.create().setName("height").setDataType(DECIMAL).setNillable(false));
		targetMeta.addAttribute(attrMetaFactory.create().setName("source"));
		when(dataService.hasRepository(entityTypeId)).thenReturn(true);
		when(dataService.getRepository(entityTypeId)).thenReturn(updateEntityRepo);

		when(updateEntityRepo.getEntityType()).thenReturn(targetMeta);
		when(metaDataService.createRepository(argThat(obj -> obj.getLabel().equals(entityTypeId))))
				.thenReturn(updateEntityRepo);

		// add an entity to the source
		List<Entity> sourceGeneEntities = newArrayList();
		List<Entity> expectedEntities = newArrayList();
		createEntities(targetMeta, sourceGeneEntities, expectedEntities);

		when(updateEntityRepo.count()).thenReturn(4L);
		when(updateEntityRepo.findAll(any(), any())).thenReturn(expectedEntities.stream());

		doAnswer(invocationOnMock ->
		{
			@SuppressWarnings("unchecked")
			Consumer<List<Entity>> consumer = (Consumer<List<Entity>>) invocationOnMock.<Consumer>getArgument(0);
			consumer.accept(sourceGeneEntities);
			return null;
		}).when(geneRepo).forEachBatched(any(Consumer.class), eq(MAPPING_BATCH_SIZE));

		// make project and apply mappings once
		MappingProject project = createMappingProjectWithMappings();

		// apply mapping again
		assertEquals(mappingService.applyMappings("TestRun", entityTypeId, false, "packageId", "label", progress), 4);

		verify(geneRepo).forEachBatched(any(Consumer.class), any(Integer.class));

		verify(updateEntityRepo).upsertBatch(batchCaptor.capture());
		assertTrue(EntityUtils.equalsEntities(batchCaptor.getValue(), expectedEntities));

		verify(progress).status("Applying mappings to repository [HopEntity]");
		verify(progress).status("Mapping source [Genes]...");
		verify(progress).increment(1);
		verify(progress).status("Mapped 4 [Genes] entities.");
		verify(progress).status("Done applying mappings to repository [HopEntity]");
		verifyZeroInteractions(permissionSystemService);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testApplyMappingsToRepoAdd()
	{
		Repository<Entity> targetRepo = mock(Repository.class);
		Repository<Entity> sourceRepo = mock(Repository.class);
		EntityMapping sourceMapping = mock(EntityMapping.class);
		when(sourceMapping.getLabel()).thenReturn("sourceMappingLabel");
		when(sourceMapping.getName()).thenReturn("sourceMappingID");

		EntityType sourceEntityType = mock(EntityType.class);
		when(sourceEntityType.getLabel()).thenReturn("test");
		when(sourceRepo.getEntityType()).thenReturn(sourceEntityType);
		when(dataService.getRepository("sourceMappingID")).thenReturn(sourceRepo);
		when(targetRepo.count()).thenReturn(0L);

		EntityType targetEntityType = mock(EntityType.class);
		when(targetEntityType.getId()).thenReturn("targetEntityType");
		when(targetEntityType.getAtomicAttributes()).thenReturn(newArrayList());
		Attribute targetID = mock(Attribute.class);
		when(targetID.getName()).thenReturn("targetID");
		when(targetEntityType.getIdAttribute()).thenReturn(targetID);
		when(targetRepo.getEntityType()).thenReturn(targetEntityType);

		List<Entity> batch = newArrayList(mock(Entity.class));
		doAnswer(invocationOnMock ->
		{
			Consumer<List<Entity>> consumer = (Consumer<List<Entity>>) invocationOnMock.<Consumer>getArgument(0);

			consumer.accept(batch);
			consumer.accept(batch);
			consumer.accept(batch);
			return null;
		}).when(sourceRepo).forEachBatched(any(Consumer.class), eq(MAPPING_BATCH_SIZE));

		mappingService.applyMappingToRepo(sourceMapping, targetRepo, progress);

		verify(targetRepo, times(3)).add(any(Stream.class));
		verify(progress, times(3)).increment(1);
		verify(progress).status("Mapping source [sourceMappingLabel]...");
		verify(progress).status("Mapped 3 [sourceMappingLabel] entities.");
		verifyNoMoreInteractions(progress);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testApplyMappingsToRepoUpsert()
	{
		Repository<Entity> targetRepo = mock(Repository.class);
		Repository<Entity> sourceRepo = mock(Repository.class);
		EntityMapping sourceMapping = mock(EntityMapping.class);
		when(sourceMapping.getLabel()).thenReturn("sourceMappingLabel");
		when(sourceMapping.getName()).thenReturn("sourceMappingID");

		when(dataService.getRepository("sourceMappingID")).thenReturn(sourceRepo);
		when(targetRepo.count()).thenReturn(3L);

		EntityType targetEntityType = mock(EntityType.class);
		when(targetEntityType.getId()).thenReturn("targetEntityType");
		when(targetEntityType.getAtomicAttributes()).thenReturn(newArrayList());
		Attribute targetID = mock(Attribute.class);
		when(targetID.getName()).thenReturn("targetID");
		when(targetEntityType.getIdAttribute()).thenReturn(targetID);
		when(targetRepo.getEntityType()).thenReturn(targetEntityType);

		List<Entity> batch = newArrayList(mock(Entity.class), mock(Entity.class));

		doAnswer(invocationOnMock ->
		{
			Consumer<List<Entity>> consumer = (Consumer<List<Entity>>) invocationOnMock.<Consumer>getArgument(0);
			consumer.accept(batch);
			consumer.accept(batch);
			return null;
		}).when(sourceRepo).forEachBatched(any(Consumer.class), eq(MAPPING_BATCH_SIZE));

		mappingService.applyMappingToRepo(sourceMapping, targetRepo, progress);

		verify(targetRepo, times(2)).upsertBatch(any(List.class));
		verify(progress, times(2)).increment(1);
		verify(progress).status("Mapping source [sourceMappingLabel]...");
		verify(progress).status("Mapped 4 [sourceMappingLabel] entities.");
		verifyNoMoreInteractions(progress);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Target repository does not contain the following attribute: COUNTRY_1")
	public void testIncompatibleMetaDataUnknownAttribute()
	{
		String targetRepositoryName = "targetRepository";

		@SuppressWarnings("unchecked")
		Repository<Entity> targetRepository = mock(Repository.class);
		EntityType targetRepositoryMetaData = entityTypeFactory.create(targetRepositoryName);
		targetRepositoryMetaData.addAttribute(attrMetaFactory.create().setName("ID").setDataType(STRING), ROLE_ID);
		targetRepositoryMetaData.addAttribute(attrMetaFactory.create().setName("COUNTRY").setDataType(STRING));
		targetRepositoryMetaData.setPackage(package_);

		when(dataService.hasRepository(targetRepositoryName)).thenReturn(true);
		when(dataService.getRepository(targetRepositoryName)).thenReturn(targetRepository);
		when(targetRepository.getEntityType()).thenReturn(targetRepositoryMetaData);

		EntityType mappingTargetMetaData = entityTypeFactory.create("mapping_target");
		mappingTargetMetaData.addAttribute(attrMetaFactory.create().setName("ID").setDataType(STRING), ROLE_ID);
		mappingTargetMetaData.addAttribute(attrMetaFactory.create().setName("COUNTRY_1").setDataType(STRING));
		mappingTargetMetaData.setPackage(package_);

		MappingTarget mappingTarget = new MappingTarget(mappingTargetMetaData);
		MappingProject mappingProject = mock(MappingProject.class);
		when(mappingProject.getMappingTargets()).thenReturn(newArrayList(mappingTarget));
		when(mappingProjectRepo.getMappingProject("TestRun")).thenReturn(mappingProject);
		mappingService.applyMappings("TestRun", targetRepositoryName, false, "packageId", "label", progress);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp =
			"attribute COUNTRY in the mapping target is type INT while attribute "
					+ "COUNTRY in the target repository is type STRING. Please make sure the types are the same")
	public void testIncompatibleMetaDataDifferentType()
	{
		String targetRepositoryName = "target_repository";

		@SuppressWarnings("unchecked")
		Repository<Entity> targetRepository = mock(Repository.class);
		EntityType targetRepositoryMetaData = entityTypeFactory.create(targetRepositoryName);
		targetRepositoryMetaData.addAttribute(attrMetaFactory.create().setName("ID").setDataType(STRING), ROLE_ID);
		targetRepositoryMetaData.addAttribute(attrMetaFactory.create().setName("COUNTRY").setDataType(STRING));
		targetRepositoryMetaData.setPackage(package_);

		when(dataService.hasRepository(targetRepositoryName)).thenReturn(true);
		when(dataService.getRepository(targetRepositoryName)).thenReturn(targetRepository);
		when(targetRepository.getEntityType()).thenReturn(targetRepositoryMetaData);

		EntityType mappingTargetMetaData = entityTypeFactory.create("mapping_target");
		mappingTargetMetaData.addAttribute(attrMetaFactory.create().setName("ID").setDataType(STRING), ROLE_ID);
		mappingTargetMetaData.addAttribute(attrMetaFactory.create().setName("COUNTRY").setDataType(INT));
		mappingTargetMetaData.setPackage(package_);

		MappingTarget mappingTarget = new MappingTarget(mappingTargetMetaData);
		MappingProject mappingProject = mock(MappingProject.class);
		when(mappingProject.getMappingTargets()).thenReturn(newArrayList(mappingTarget));
		when(mappingProjectRepo.getMappingProject("TestRun")).thenReturn(mappingProject);
		mappingService.applyMappings("TestRun", targetRepositoryName, false, "packageId", "label", progress);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp =
			"In the mapping target, attribute COUNTRY of type XREF has "
					+ "reference entity mapping_target_ref while in the target repository attribute COUNTRY of type XREF has reference entity target_repository_ref. "
					+ "Please make sure the reference entities of your mapping target are pointing towards the same reference entities as your target repository")
	public void testIncompatibleMetaDataDifferentRefEntity()
	{
		String targetRepositoryName = "target_repository";
		String targetRepositoryRefEntityName = "target_repository_ref";
		String mappingTargetRefEntityName = "mapping_target_ref";

		EntityType targetRefEntity = entityTypeFactory.create(targetRepositoryRefEntityName);

		@SuppressWarnings("unchecked")
		Repository<Entity> targetRepository = mock(Repository.class);
		EntityType targetRepositoryMetaData = entityTypeFactory.create(targetRepositoryName);
		targetRepositoryMetaData.addAttribute(attrMetaFactory.create().setName("ID").setDataType(STRING), ROLE_ID);
		targetRepositoryMetaData.addAttribute(
				attrMetaFactory.create().setName("COUNTRY").setDataType(XREF).setRefEntity(targetRefEntity));
		targetRepositoryMetaData.setPackage(package_);

		when(dataService.hasRepository(targetRepositoryName)).thenReturn(true);
		when(dataService.getRepository(targetRepositoryName)).thenReturn(targetRepository);
		when(targetRepository.getEntityType()).thenReturn(targetRepositoryMetaData);

		EntityType mappingTargetRefEntity = entityTypeFactory.create(mappingTargetRefEntityName);

		EntityType mappingTargetMetaData = entityTypeFactory.create("mapping_target");
		mappingTargetMetaData.addAttribute(attrMetaFactory.create().setName("ID").setDataType(STRING), ROLE_ID);
		mappingTargetMetaData.addAttribute(
				attrMetaFactory.create().setName("COUNTRY").setDataType(XREF).setRefEntity(mappingTargetRefEntity));
		mappingTargetMetaData.setPackage(package_);

		when(metaDataService.createRepository(mappingTargetMetaData)).thenReturn(targetRepository);

		MappingTarget mappingTarget = new MappingTarget(mappingTargetMetaData);

		MappingProject mappingProject = mock(MappingProject.class);
		when(mappingProject.getMappingTargets()).thenReturn(newArrayList(mappingTarget));
		when(mappingProjectRepo.getMappingProject("TestRun")).thenReturn(mappingProject);
		mappingService.applyMappings("TestRun", targetRepositoryName, false, "packageId", "label", progress);
	}

	@Test
	public void testGetCompatibleEntityTypes()
	{
		when(metaDataService.getEntityTypes()).thenReturn(Stream.of(hopMetaData, geneMetaData));
		Set<Entity> compatibleEntityTypes = mappingService.getCompatibleEntityTypes(hopMetaData).collect(toSet());
		assertEquals(compatibleEntityTypes, newHashSet(hopMetaData));
	}

	@Test
	public void testMaxProgressOneSourceOneBatch()
	{
		MappingTarget mappingTarget = mock(MappingTarget.class);
		EntityMapping entityMapping = getMockEntityMapping("a", MAPPING_BATCH_SIZE - 1);
		List<EntityMapping> mappings = singletonList(entityMapping);
		when(mappingTarget.getEntityMappings()).thenReturn(mappings);

		assertEquals(mappingService.calculateMaxProgress(mappingTarget), 1);
	}

	@Test
	public void testMaxProgressOneSourceMultipleBatches()
	{
		MappingTarget mappingTarget = mock(MappingTarget.class);
		EntityMapping entityMapping = getMockEntityMapping("a", (3 * MAPPING_BATCH_SIZE) + 1);
		List<EntityMapping> mappings = singletonList(entityMapping);
		when(mappingTarget.getEntityMappings()).thenReturn(mappings);

		assertEquals(mappingService.calculateMaxProgress(mappingTarget), 4);
	}

	@Test
	public void testMaxProgressOneSourceMultipleBatchesSelfReferencing()
	{
		MappingTarget mappingTarget = mock(MappingTarget.class);
		EntityMapping entityMapping = getMockEntityMapping("a", (3 * MAPPING_BATCH_SIZE) + 1);
		when(mappingTarget.hasSelfReferences()).thenReturn(true);
		List<EntityMapping> mappings = singletonList(entityMapping);
		when(mappingTarget.getEntityMappings()).thenReturn(mappings);

		assertEquals(mappingService.calculateMaxProgress(mappingTarget), 8);
	}

	@Test
	public void testMaxProgressMultipleSourcesSelfReferencing()
	{
		MappingTarget mappingTarget = mock(MappingTarget.class);
		EntityMapping mapping1 = getMockEntityMapping("a", MAPPING_BATCH_SIZE);
		EntityMapping mapping2 = getMockEntityMapping("b", MAPPING_BATCH_SIZE + 1);
		List<EntityMapping> mappings = newArrayList(mapping1, mapping2);
		when(mappingTarget.hasSelfReferences()).thenReturn(true);
		when(mappingTarget.getEntityMappings()).thenReturn(mappings);

		assertEquals(mappingService.calculateMaxProgress(mappingTarget), 6);
	}

	@Test
	public void testMaxProgressMultipleSources()
	{
		MappingTarget mappingTarget = mock(MappingTarget.class);
		EntityMapping mapping1 = getMockEntityMapping("a", MAPPING_BATCH_SIZE);
		EntityMapping mapping2 = getMockEntityMapping("b", MAPPING_BATCH_SIZE + 1);
		List<EntityMapping> mappings = newArrayList(mapping1, mapping2);
		when(mappingTarget.getEntityMappings()).thenReturn(mappings);

		assertEquals(mappingService.calculateMaxProgress(mappingTarget), 3);
	}

	private EntityMapping getMockEntityMapping(String id, long sourceRows)
	{
		EntityMapping entityMapping = mock(EntityMapping.class);
		EntityType sourceEntityType = mock(EntityType.class);
		when(entityMapping.getSourceEntityType()).thenReturn(sourceEntityType);
		when(sourceEntityType.getId()).thenReturn(id);
		when(dataService.count(id)).thenReturn(sourceRows);
		return entityMapping;
	}

	private void createEntities(EntityType targetMeta, List<Entity> sourceGeneEntities, List<Entity> expectedEntities)
	{
		for (int i = 0; i < 4; ++i)
		{
			Entity geneEntity = new DynamicEntity(geneMetaData);
			geneEntity.set("id", String.valueOf(i));
			geneEntity.set("length", i * 2d);
			sourceGeneEntities.add(geneEntity);

			when(algorithmService.apply(argThat(obj -> obj != null && obj.getAlgorithm().equals("$('id').value()")),
					eq(geneEntity), eq(geneMetaData))).thenReturn(geneEntity.getString("id"));

			when(algorithmService.apply(argThat(obj -> obj != null && obj.getAlgorithm().equals("$('length').value()")),
					eq(geneEntity), eq(geneMetaData))).thenReturn(geneEntity.getDouble("length"));

			Entity expectedEntity = new DynamicEntity(targetMeta);
			expectedEntity.set("identifier", String.valueOf(i));
			expectedEntity.set("height", i * 2d);
			expectedEntity.set("source", geneMetaData.getId());
			expectedEntities.add(expectedEntity);
		}
	}

	private MappingProject createMappingProjectWithMappings()
	{
		MappingProject mappingProject = mappingService.addMappingProject("TestRun", user, hopMetaData.getId());
		MappingTarget target = mappingProject.getMappingTarget(hopMetaData.getId());

		when(mappingProjectRepo.getMappingProject("TestRun")).thenReturn(mappingProject);

		EntityMapping mapping = target.addSource(geneMetaData);

		AttributeMapping idMapping = mapping.addAttributeMapping("identifier");
		idMapping.setAlgorithm("$('id').value()");
		AttributeMapping attrMapping = mapping.addAttributeMapping("height");
		attrMapping.setAlgorithm("$('length').value()");

		return mappingProject;
	}

	private MappingTarget getManualMappingTarget(String identifier, Collection<EntityMapping> entityMappings)
	{
		return new MappingTarget(identifier, hopMetaData, entityMappings);
	}

	private MappingProject getManualMappingProject(String identifier, String projectName, MappingTarget mappingTarget)
	{
		return new MappingProject(identifier, projectName, user, newArrayList(mappingTarget));
	}

	@Configuration
	@Import(UserTestConfig.class)
	static class Config
	{

		@Bean
		public AlgorithmService algorithmService()
		{
			return mock(AlgorithmService.class);
		}

		@Bean
		JsMagmaScriptEvaluator jsMagmaScriptEvaluator()
		{
			return mock(JsMagmaScriptEvaluator.class);
		}

		@Bean
		public MappingProjectRepository mappingProjectRepository()
		{
			return mock(MappingProjectRepository.class);
		}

		@Bean
		public PermissionSystemService permissionSystemService()
		{
			return mock(PermissionSystemService.class);
		}

	}
}
