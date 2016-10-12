package org.molgenis.data.mapper.service.impl;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserFactory;
import org.molgenis.data.*;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.meta.MappingTargetMetaData;
import org.molgenis.data.mapper.repository.MappingProjectRepository;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.populate.UuidGenerator;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.mapper.meta.MappingProjectMetaData.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { MappingServiceImplTest.Config.class })
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
	private IdGenerator idGenerator;

	@Autowired
	private MappingProjectRepository mappingProjectRepo;

	@Autowired
	private PermissionSystemService permissionSystemService;

	@Autowired
	private AttributeFactory attrMetaFactory;

	@Autowired
	private MappingService mappingService;

	@Autowired
	private MolgenisUserFactory molgenisUserFactory;

	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private PackageFactory packageFactory;

	@Mock
	private Repository<Entity> hopRepo;

	@Mock
	private Repository<Entity> geneRepo;

	@Mock
	private Repository<Entity> exonRepo;

	private MetaDataService metaDataService;

	private MolgenisUser user;
	private EntityMetaData hopMetaData;
	private EntityMetaData geneMetaData;
	private Package package_;
	private final UuidGenerator uuidGenerator = new UuidGenerator();

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		reset(dataService);
		reset(idGenerator);
		reset(mappingProjectRepo);
		reset(permissionSystemService);
		reset(hopRepo);
		reset(geneRepo);
		reset(exonRepo);

		user = molgenisUserFactory.create();
		user.setUsername(USERNAME);

		package_ = packageFactory.create("package");

		hopMetaData = entityMetaFactory.create(TARGET_HOP_ENTITY).setPackage(package_);
		hopMetaData.addAttribute(attrMetaFactory.create().setName("identifier"), ROLE_ID);
		hopMetaData.addAttribute(attrMetaFactory.create().setName("height").setDataType(DECIMAL).setNillable(false));

		geneMetaData = entityMetaFactory.create(SOURCE_GENE_ENTITY).setPackage(package_);
		geneMetaData.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);
		geneMetaData.addAttribute(attrMetaFactory.create().setName("length").setDataType(DECIMAL).setNillable(false));

		EntityMetaData exonMetaData = entityMetaFactory.create(SOURCE_EXON_ENTITY).setPackage(package_);
		exonMetaData.addAttribute(attrMetaFactory.create().setName("id"), ROLE_ID);
		exonMetaData
				.addAttribute(attrMetaFactory.create().setName("basepairs").setDataType(DECIMAL).setNillable(false));

		metaDataService = mock(MetaDataService.class);
		when(metaDataService.createRepository(argThat(new ArgumentMatcher<EntityMetaData>()
		{
			@Override
			public boolean matches(Object obj)
			{
				return obj instanceof EntityMetaData && ((EntityMetaData) obj).getName().equals(TARGET_HOP_ENTITY);
			}
		}))).thenReturn(hopRepo);
		when(metaDataService.createRepository(argThat(new ArgumentMatcher<EntityMetaData>()
		{
			@Override
			public boolean matches(Object obj)
			{
				return obj instanceof EntityMetaData && ((EntityMetaData) obj).getName().equals(SOURCE_GENE_ENTITY);
			}
		}))).thenReturn(geneRepo);
		when(metaDataService.createRepository(argThat(new ArgumentMatcher<EntityMetaData>()
		{
			@Override
			public boolean matches(Object obj)
			{
				return obj instanceof EntityMetaData && ((EntityMetaData) obj).getName().equals(SOURCE_EXON_ENTITY);
			}
		}))).thenReturn(exonRepo);

		when(hopRepo.getName()).thenReturn(TARGET_HOP_ENTITY);
		when(hopRepo.getEntityMetaData()).thenReturn(hopMetaData);
		when(geneRepo.getName()).thenReturn(SOURCE_GENE_ENTITY);
		when(geneRepo.iterator()).thenReturn(Collections.<Entity>emptyList().iterator());
		when(geneRepo.getEntityMetaData()).thenReturn(geneMetaData);
		when(exonRepo.getName()).thenReturn(SOURCE_EXON_ENTITY);
		when(exonRepo.iterator()).thenReturn(Collections.<Entity>emptyList().iterator());
		when(exonRepo.getEntityMetaData()).thenReturn(exonMetaData);
		when(dataService.getMeta()).thenReturn(metaDataService);
		when(dataService.getEntityMetaData(TARGET_HOP_ENTITY)).thenReturn(hopMetaData);
		when(dataService.getRepository(TARGET_HOP_ENTITY)).thenReturn(hopRepo);
		Query<Entity> hopQ = mock(Query.class);
		when(hopRepo.query()).thenReturn(hopQ);
		when(hopQ.eq("source", SOURCE_GENE_ENTITY)).thenReturn(hopQ);
		when(hopQ.eq("source", SOURCE_EXON_ENTITY)).thenReturn(hopQ);
		when(hopQ.findAll()).thenAnswer(new Answer<Stream>()
		{
			@Override
			public Stream answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.empty();
			}
		});

		when(dataService.getEntityMetaData(SOURCE_GENE_ENTITY)).thenReturn(geneMetaData);
		when(dataService.getRepository(SOURCE_GENE_ENTITY)).thenReturn(geneRepo);
		when(dataService.getEntityMetaData(SOURCE_EXON_ENTITY)).thenReturn(exonMetaData);
		when(dataService.getRepository(SOURCE_EXON_ENTITY)).thenReturn(exonRepo);
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
	}

	@Test
	public void testAddMappingProject()
	{
		String projectName = "test_project";
		MappingProject mappingProject = mappingService.addMappingProject(projectName, user, TARGET_HOP_ENTITY);
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

		MappingProject actualAddedMappingProject = mappingService
				.addMappingProject(projectName, user, TARGET_HOP_ENTITY);

		String mappingTargetIdentifier = actualAddedMappingProject.getMappingTarget(TARGET_HOP_ENTITY).getIdentifier();
		MappingTarget expectedMappingTarget = getManualMappingTarget(mappingTargetIdentifier, emptyList());

		String mappingProjectIdentifier = actualAddedMappingProject.getIdentifier();
		MappingProject expectedMappingProject = getManualMappingProject(mappingProjectIdentifier, projectName,
				expectedMappingTarget);

		Entity mappingTargetEntity = mock(Entity.class);
		when(mappingTargetEntity.getString(MappingTargetMetaData.IDENTIFIER)).thenReturn(mappingTargetIdentifier);
		when(mappingTargetEntity.getString(MappingTargetMetaData.TARGET)).thenReturn(TARGET_HOP_ENTITY);
		when(mappingTargetEntity.getEntities(MappingTargetMetaData.ENTITY_MAPPINGS)).thenReturn(null);
		when(dataService.getEntityMetaData(mappingTargetEntity.getString(MappingTargetMetaData.TARGET)))
				.thenReturn(hopMetaData);

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
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		MappingProject mappingProject = mappingService.addMappingProject("Test123", user, TARGET_HOP_ENTITY);
		mappingProject.addTarget(hopMetaData);
	}

	/**
	 * New entities in the source should be added to the target when a new mapping to the same target is performed.
	 */
	@Test
	public void testApplyMappingsAdd()
	{
		String entityName = "addEntity";
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
		Repository<Entity> addEntityRepo = mock(Repository.class);
		when(addEntityRepo.getName()).thenReturn(entityName);
		EntityMetaData targetMeta = entityMetaFactory.create(TARGET_HOP_ENTITY).setPackage(package_);
		targetMeta.addAttribute(attrMetaFactory.create().setName("identifier"), ROLE_ID);
		targetMeta.addAttribute(attrMetaFactory.create().setName("height").setDataType(DECIMAL).setNillable(false));
		targetMeta.addAttribute(attrMetaFactory.create().setName("source"));

		when(addEntityRepo.getEntityMetaData()).thenReturn(targetMeta);
		when(metaDataService.createRepository(argThat(new ArgumentMatcher<EntityMetaData>()
		{
			@Override
			public boolean matches(Object obj)
			{
				return obj instanceof EntityMetaData && ((EntityMetaData) obj).getName().equals(entityName);
			}
		}))).thenReturn(addEntityRepo);

		// add an entity to the source
		List<Entity> sourceGeneEntities = newArrayList();
		List<Entity> expectedEntities = newArrayList();
		createEntities(targetMeta, sourceGeneEntities, expectedEntities);

		Query<Entity> addEntityQ = mock(Query.class);
		when(addEntityRepo.query()).thenReturn(addEntityQ);
		when(addEntityQ.eq("source", SOURCE_GENE_ENTITY)).thenReturn(addEntityQ);
		when(addEntityQ.findAll()).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.empty();
			}
		});

		when(geneRepo.iterator()).thenAnswer(new Answer<Iterator<Entity>>()
		{
			@Override
			public Iterator<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return sourceGeneEntities.iterator();
			}
		});

		// make project and apply mappings once
		MappingProject project = createMappingProjectWithMappings();

		// apply mapping again
		String generatedEntityName = mappingService
				.applyMappings(project.getMappingTarget(TARGET_HOP_ENTITY), entityName, true);
		assertEquals(generatedEntityName, entityName);

		ArgumentCaptor<Entity> entityCaptor = forClass((Class) Entity.class);
		verify(addEntityRepo, times(4)).add(entityCaptor.capture());

		assertTrue(EntityUtils.entitiesEquals(entityCaptor.getAllValues(), expectedEntities));

		verify(permissionSystemService)
				.giveUserEntityPermissions(SecurityContextHolder.getContext(), singletonList(entityName));
	}

	/**
	 * Applying a mapping multiple times to the same target should update the existing entities.
	 */
	@Test
	public void testApplyMappingsUpdate()
	{
		String entityName = "updateEntity";

		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());
		Repository<Entity> updateEntityRepo = mock(Repository.class);
		when(updateEntityRepo.getName()).thenReturn(entityName);
		EntityMetaData targetMeta = entityMetaFactory.create(TARGET_HOP_ENTITY).setPackage(package_);
		targetMeta.addAttribute(attrMetaFactory.create().setName("identifier"), ROLE_ID);
		targetMeta.addAttribute(attrMetaFactory.create().setName("height").setDataType(DECIMAL).setNillable(false));
		targetMeta.addAttribute(attrMetaFactory.create().setName("source"));
		when(dataService.hasRepository(entityName)).thenReturn(true);
		when(dataService.getRepository(entityName)).thenReturn(updateEntityRepo);

		when(updateEntityRepo.getEntityMetaData()).thenReturn(targetMeta);
		when(metaDataService.createRepository(argThat(new ArgumentMatcher<EntityMetaData>()
		{
			@Override
			public boolean matches(Object obj)
			{
				return obj instanceof EntityMetaData && ((EntityMetaData) obj).getName().equals(entityName);
			}
		}))).thenReturn(updateEntityRepo);

		// add an entity to the source
		List<Entity> sourceGeneEntities = newArrayList();
		List<Entity> expectedEntities = newArrayList();
		createEntities(targetMeta, sourceGeneEntities, expectedEntities);

		Query<Entity> addEntityQ = mock(Query.class);
		when(updateEntityRepo.query()).thenReturn(addEntityQ);
		when(addEntityQ.eq("source", SOURCE_GENE_ENTITY)).thenReturn(addEntityQ);
		when(addEntityQ.findAll()).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return expectedEntities.stream();
			}
		});

		when(geneRepo.iterator()).thenAnswer(new Answer<Iterator<Entity>>()
		{
			@Override
			public Iterator<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return sourceGeneEntities.iterator();
			}
		});

		// make project and apply mappings once
		MappingProject project = createMappingProjectWithMappings();

		// apply mapping again
		String generatedEntityName = mappingService
				.applyMappings(project.getMappingTarget(TARGET_HOP_ENTITY), entityName);
		assertEquals(generatedEntityName, entityName);

		ArgumentCaptor<Entity> entityCaptor = forClass((Class) Entity.class);
		verify(updateEntityRepo, times(4)).add(entityCaptor.capture());

		assertTrue(EntityUtils.entitiesEquals(entityCaptor.getAllValues(), expectedEntities));

		verifyZeroInteractions(permissionSystemService);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Target repository does not contain the following attribute: COUNTRY_1")
	public void testIncompatibleMetaDataUnknownAttribute()
	{
		String targetRepositoryName = "target_repository";

		Repository<Entity> targetRepository = mock(Repository.class);
		EntityMetaData targetRepositoryMetaData = entityMetaFactory.create(targetRepositoryName);
		targetRepositoryMetaData.addAttribute(attrMetaFactory.create().setName("ID").setDataType(STRING), ROLE_ID);
		targetRepositoryMetaData.addAttribute(attrMetaFactory.create().setName("COUNTRY").setDataType(STRING));

		when(dataService.hasRepository(targetRepositoryName)).thenReturn(true);
		when(dataService.getRepository(targetRepositoryName)).thenReturn(targetRepository);
		when(targetRepository.getEntityMetaData()).thenReturn(targetRepositoryMetaData);

		EntityMetaData mappingTargetMetaData = entityMetaFactory.create("mapping_target");
		mappingTargetMetaData.addAttribute(attrMetaFactory.create().setName("ID").setDataType(STRING), ROLE_ID);
		mappingTargetMetaData.addAttribute(attrMetaFactory.create().setName("COUNTRY_1").setDataType(STRING));

		MappingTarget mappingTarget = new MappingTarget(mappingTargetMetaData);

		mappingService.applyMappings(mappingTarget, targetRepositoryName, false);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp =
			"attribute COUNTRY in the mapping target is type INT while attribute "
					+ "COUNTRY in the target repository is type STRING. Please make sure the types are the same")
	public void testIncompatibleMetaDataDifferentType()
	{
		String targetRepositoryName = "target_repository";

		Repository<Entity> targetRepository = mock(Repository.class);
		EntityMetaData targetRepositoryMetaData = entityMetaFactory.create(targetRepositoryName);
		targetRepositoryMetaData.addAttribute(attrMetaFactory.create().setName("ID").setDataType(STRING), ROLE_ID);
		targetRepositoryMetaData.addAttribute(attrMetaFactory.create().setName("COUNTRY").setDataType(STRING));

		when(dataService.hasRepository(targetRepositoryName)).thenReturn(true);
		when(dataService.getRepository(targetRepositoryName)).thenReturn(targetRepository);
		when(targetRepository.getEntityMetaData()).thenReturn(targetRepositoryMetaData);

		EntityMetaData mappingTargetMetaData = entityMetaFactory.create("mapping_target");
		mappingTargetMetaData.addAttribute(attrMetaFactory.create().setName("ID").setDataType(STRING), ROLE_ID);
		mappingTargetMetaData.addAttribute(attrMetaFactory.create().setName("COUNTRY").setDataType(INT));

		MappingTarget mappingTarget = new MappingTarget(mappingTargetMetaData);

		mappingService.applyMappings(mappingTarget, targetRepositoryName, false);
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

		EntityMetaData targetRefEntity = entityMetaFactory.create(targetRepositoryRefEntityName);

		Repository<Entity> targetRepository = mock(Repository.class);
		EntityMetaData targetRepositoryMetaData = entityMetaFactory.create(targetRepositoryName);
		targetRepositoryMetaData.addAttribute(attrMetaFactory.create().setName("ID").setDataType(STRING), ROLE_ID);
		targetRepositoryMetaData.addAttribute(
				attrMetaFactory.create().setName("COUNTRY").setDataType(XREF).setRefEntity(targetRefEntity));

		when(dataService.hasRepository(targetRepositoryName)).thenReturn(true);
		when(dataService.getRepository(targetRepositoryName)).thenReturn(targetRepository);
		when(targetRepository.getEntityMetaData()).thenReturn(targetRepositoryMetaData);

		EntityMetaData mappingTargetRefEntity = entityMetaFactory.create(mappingTargetRefEntityName);

		EntityMetaData mappingTargetMetaData = entityMetaFactory.create("mapping_target");
		mappingTargetMetaData.addAttribute(attrMetaFactory.create().setName("ID").setDataType(STRING), ROLE_ID);
		mappingTargetMetaData.addAttribute(
				attrMetaFactory.create().setName("COUNTRY").setDataType(XREF).setRefEntity(mappingTargetRefEntity));

		MappingTarget mappingTarget = new MappingTarget(mappingTargetMetaData);

		mappingService.applyMappings(mappingTarget, targetRepositoryName, false);
	}

	private void createEntities(EntityMetaData targetMeta, List<Entity> sourceGeneEntities,
			List<Entity> expectedEntities)
	{
		for (int i = 0; i < 4; ++i)
		{
			Entity geneEntity = new DynamicEntity(geneMetaData);
			geneEntity.set("id", String.valueOf(i));
			geneEntity.set("length", i * 2d);
			sourceGeneEntities.add(geneEntity);

			when(algorithmService.apply(argThat(new ArgumentMatcher<AttributeMapping>()
			{
				@Override
				public boolean matches(Object obj)
				{
					return obj instanceof AttributeMapping && ((AttributeMapping) obj).getAlgorithm()
							.equals("$('id').value()");
				}
			}), eq(geneEntity), eq(geneMetaData))).thenReturn(geneEntity.getString("id"));

			when(algorithmService.apply(argThat(new ArgumentMatcher<AttributeMapping>()
			{
				@Override
				public boolean matches(Object obj)
				{
					return obj instanceof AttributeMapping && ((AttributeMapping) obj).getAlgorithm()
							.equals("$('length').value()");
				}
			}), eq(geneEntity), eq(geneMetaData))).thenReturn(geneEntity.getDouble("length"));

			Entity expectedEntity = new DynamicEntity(targetMeta);
			expectedEntity.set("identifier", String.valueOf(i));
			expectedEntity.set("height", i * 2d);
			expectedEntity.set("source", SOURCE_GENE_ENTITY);
			expectedEntities.add(expectedEntity);
		}
	}

	private MappingProject createMappingProjectWithMappings()
	{
		MappingProject mappingProject = mappingService.addMappingProject("TestRun", user, TARGET_HOP_ENTITY);
		MappingTarget target = mappingProject.getMappingTarget(TARGET_HOP_ENTITY);

		EntityMapping mapping = target.addSource(geneMetaData);

		AttributeMapping idMapping = mapping.addAttributeMapping("identifier");
		idMapping.setAlgorithm("$('id').value()");
		AttributeMapping attrMapping = mapping.addAttributeMapping("height");
		attrMapping.setAlgorithm("$('length').value()");

		return mappingProject;
	}

	@Test
	public void testNumericId()
	{
		assertEquals(mappingService.generateId(INT, 1L), "2");
		assertEquals(mappingService.generateId(DECIMAL, 2L), "3");
		assertEquals(mappingService.generateId(LONG, 3L), "4");
	}

	@Test
	public void testStringId()
	{
		reset(idGenerator);
		when(idGenerator.generateId()).thenReturn(uuidGenerator.generateId());

		mappingService.generateId(STRING, 1L);
		verify(idGenerator).generateId();
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
	@ComponentScan({ "org.molgenis.data.mapper.meta", "org.molgenis.auth", "org.molgenis.data.index.meta" })
	static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public AlgorithmService algorithmService()
		{
			return mock(AlgorithmService.class);
		}

		@Bean
		public IdGenerator idGenerator()
		{
			return mock(IdGenerator.class);
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

		@Bean
		public AttributeFactory attributeFactory()
		{
			return new AttributeFactory(mock(EntityPopulator.class));
		}

		@Bean
		public MappingService mappingService()
		{
			return new MappingServiceImpl(dataService(), algorithmService(), idGenerator(), mappingProjectRepository(),
					permissionSystemService(), attributeFactory());
		}
	}
}
