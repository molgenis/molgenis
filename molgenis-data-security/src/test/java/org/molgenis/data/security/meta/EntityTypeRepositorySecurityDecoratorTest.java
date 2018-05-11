package org.molgenis.data.security.meta;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.PackagePermission;
import org.molgenis.data.security.exception.EntityTypePermissionDeniedException;
import org.molgenis.data.security.exception.NullPackageNotSuException;
import org.molgenis.data.security.exception.PackagePermissionDeniedException;
import org.molgenis.data.security.exception.SystemMetadataModificationException;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.molgenis.data.security.EntityTypePermission.UPDATE_METADATA;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@ContextConfiguration(classes = { EntityTypeRepositorySecurityDecoratorTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class EntityTypeRepositorySecurityDecoratorTest extends AbstractMockitoTestNGSpringContextTests
{
	private static final String USERNAME = "user";

	@Mock
	private Repository delegateRepository;
	@Mock
	private SystemEntityTypeRegistry systemEntityTypeRegistry;
	@Mock
	private UserPermissionEvaluator userPermissionEvaluator;
	@Mock
	private MutableAclService mutableAclService;
	@Mock
	private MutableAclClassService mutableAclClassService;
	@Mock
	private EntityTypeRepositorySecurityDecorator repo;
	@Mock
	private DataService dataService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		repo = new EntityTypeRepositorySecurityDecorator(delegateRepository, systemEntityTypeRegistry,
				userPermissionEvaluator, mutableAclService, mutableAclClassService, dataService);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void count()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		when(delegateRepository.findAll(new QueryImpl<>().setOffset(0).setPageSize(Integer.MAX_VALUE))).thenReturn(
				asList(entityType0, entityType1).stream());
		doReturn(false).when(userPermissionEvaluator)
					   .hasPermission(new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA);
		doReturn(true).when(userPermissionEvaluator)
					  .hasPermission(new EntityTypeIdentity(entityType1Name), EntityTypePermission.READ_METADATA);
		assertEquals(repo.count(), 1L);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void add()
	{
		String entityTypeId = "entityTypeId";
		EntityType entityType = mock(EntityType.class);
		Package pack = mock(Package.class);
		MutableAcl mutableAcl = mock(MutableAcl.class);
		Acl acl = mock(Acl.class);

		when(pack.getId()).thenReturn("test");
		when(entityType.getId()).thenReturn(entityTypeId);
		when(entityType.getPackage()).thenReturn(pack);
		when(userPermissionEvaluator.hasPermission(new PackageIdentity("test"),
				PackagePermission.ADD_ENTITY_TYPE)).thenReturn(true);
		when(mutableAclService.createAcl(new EntityTypeIdentity(entityType.getId()))).thenReturn(mutableAcl);
		when(mutableAclService.readAclById(new PackageIdentity("test"))).thenReturn(acl);

		repo.add(entityType);

		verify(delegateRepository).add(entityType);
		verify(mutableAclService).createAcl(new EntityTypeIdentity(entityTypeId));
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = PackagePermissionDeniedException.class)
	public void addNoPermissionOnPack()
	{
		EntityType entityType = mock(EntityType.class);
		Package pack = mock(Package.class);

		when(pack.getId()).thenReturn("test");
		when(entityType.getPackage()).thenReturn(pack);
		when(userPermissionEvaluator.hasPermission(new PackageIdentity("test"),
				PackagePermission.ADD_ENTITY_TYPE)).thenReturn(false);

		repo.add(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = NullPackageNotSuException.class)
	public void addNullPackage()
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getPackage()).thenReturn(null);

		repo.add(entityType);
	}

	@Test
	public void query()
	{
		assertEquals(repo.query().getRepository(), repo);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void countQuery()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		Query q = new QueryImpl<>();
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Query> queryCaptor = forClass(Query.class);
		when(delegateRepository.findAll(queryCaptor.capture())).thenReturn(Stream.of(entityType0, entityType1));
		doReturn(false).when(userPermissionEvaluator)
					   .hasPermission(new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA);
		doReturn(true).when(userPermissionEvaluator)
					  .hasPermission(new EntityTypeIdentity(entityType1Name), EntityTypePermission.READ_METADATA);
		assertEquals(repo.count(q), 1L);
		assertEquals(queryCaptor.getValue().getOffset(), 0);
		assertEquals(queryCaptor.getValue().getPageSize(), Integer.MAX_VALUE);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findAllQueryUser()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getId()).thenReturn(entityType2Name).getMock();
		@SuppressWarnings("unchecked")
		Query q = mock(Query.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Query> queryCaptor = forClass(Query.class);
		when(delegateRepository.findAll(queryCaptor.capture())).thenReturn(
				Stream.of(entityType0, entityType1, entityType2));
		doReturn(true).when(userPermissionEvaluator)
					  .hasPermission(new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA);
		doReturn(false).when(userPermissionEvaluator)
					   .hasPermission(new EntityTypeIdentity(entityType1Name), EntityTypePermission.READ_METADATA);
		doReturn(true).when(userPermissionEvaluator)
					  .hasPermission(new EntityTypeIdentity(entityType2Name), EntityTypePermission.READ_METADATA);
		assertEquals(repo.findAll(q).collect(toList()), asList(entityType0, entityType2));
		Query<EntityType> decoratedQ = queryCaptor.getValue();
		assertEquals(decoratedQ.getOffset(), 0);
		assertEquals(decoratedQ.getPageSize(), Integer.MAX_VALUE);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findAllQueryUserOffsetLimit()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getId()).thenReturn(entityType2Name).getMock();
		@SuppressWarnings("unchecked")
		Query q = mock(Query.class);
		when(q.getOffset()).thenReturn(1);
		when(q.getPageSize()).thenReturn(1);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Query<EntityType>> queryCaptor = forClass(Query.class);
		when(delegateRepository.findAll(queryCaptor.capture())).thenReturn(
				Stream.of(entityType0, entityType1, entityType2));
		doReturn(true).when(userPermissionEvaluator)
					  .hasPermission(new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA);
		doReturn(false).when(userPermissionEvaluator)
					   .hasPermission(new EntityTypeIdentity(entityType1Name), EntityTypePermission.READ_METADATA);
		doReturn(true).when(userPermissionEvaluator)
					  .hasPermission(new EntityTypeIdentity(entityType2Name), EntityTypePermission.READ_METADATA);
		assertEquals(repo.findAll(q).collect(toList()), singletonList(entityType2));
		Query<EntityType> decoratedQ = queryCaptor.getValue();
		assertEquals(decoratedQ.getOffset(), 0);
		assertEquals(decoratedQ.getPageSize(), Integer.MAX_VALUE);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void iteratorUser()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getId()).thenReturn(entityType2Name).getMock();
		when(delegateRepository.iterator()).thenReturn(asList(entityType0, entityType1, entityType2).iterator());
		doReturn(true).when(userPermissionEvaluator)
					  .hasPermission(new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA);
		doReturn(false).when(userPermissionEvaluator)
					   .hasPermission(new EntityTypeIdentity(entityType1Name), EntityTypePermission.READ_METADATA);
		doReturn(true).when(userPermissionEvaluator)
					  .hasPermission(new EntityTypeIdentity(entityType2Name), EntityTypePermission.READ_METADATA);
		assertEquals(newArrayList(repo.iterator()), asList(entityType0, entityType2));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneQueryUserPermissionAllowed()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		@SuppressWarnings("unchecked")
		Query q = mock(Query.class);
		when(delegateRepository.findAll(any(Query.class))).thenReturn(Stream.of(entityType0));
		when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(entityType0Name),
				EntityTypePermission.READ_METADATA)).thenReturn(true);
		assertEquals(repo.findOne(q), entityType0);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneQueryUserPermissionDenied()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		@SuppressWarnings("unchecked")
		Query q = mock(Query.class);
		when(delegateRepository.findAll(any(Query.class))).thenReturn(Stream.of(entityType0));
		when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(entityType0Name),
				EntityTypePermission.READ_METADATA)).thenReturn(false);
		assertNull(repo.findOne(q));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdUserPermissionAllowed()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = mock(EntityType.class);
		when(delegateRepository.findOneById("entity0")).thenReturn(entityType0);
		when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(entityType0Name),
				EntityTypePermission.READ_METADATA)).thenReturn(true);
		assertEquals(repo.findOneById("entity0"), entityType0);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdUserPermissionDenied()
	{
		String entityType0Name = "entity0";
		when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(entityType0Name),
				EntityTypePermission.READ_METADATA)).thenReturn(false);
		assertNull(repo.findOneById(entityType0Name));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdFetchUserPermissionAllowed()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = mock(EntityType.class);
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findOneById(entityType0Name, fetch)).thenReturn(entityType0);
		when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(entityType0Name),
				EntityTypePermission.READ_METADATA)).thenReturn(true);
		assertEquals(repo.findOneById(entityType0Name, fetch), entityType0);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findOneByIdFetchUserPermissionDenied()
	{
		String entityType0Name = "entity0";
		Fetch fetch = mock(Fetch.class);
		when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(entityType0Name),
				EntityTypePermission.READ_METADATA)).thenReturn(false);
		assertNull(repo.findOneById(entityType0Name, fetch));
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void findAllIdsFetchUser()
	{
		String entityType0Name = "entity0";
		EntityType entityType0 = when(mock(EntityType.class).getId()).thenReturn(entityType0Name).getMock();
		String entityType1Name = "entity1";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityType1Name).getMock();
		String entityType2Name = "entity2";
		EntityType entityType2 = when(mock(EntityType.class).getId()).thenReturn(entityType2Name).getMock();
		Stream<Object> ids = Stream.of("entity0", "entity1");
		Fetch fetch = mock(Fetch.class);
		when(delegateRepository.findAll(ids, fetch)).thenReturn(Stream.of(entityType0, entityType1, entityType2));
		doReturn(true).when(userPermissionEvaluator)
					  .hasPermission(new EntityTypeIdentity(entityType0Name), EntityTypePermission.READ_METADATA);
		doReturn(false).when(userPermissionEvaluator)
					   .hasPermission(new EntityTypeIdentity(entityType1Name), EntityTypePermission.READ_METADATA);
		doReturn(true).when(userPermissionEvaluator)
					  .hasPermission(new EntityTypeIdentity(entityType2Name), EntityTypePermission.READ_METADATA);
		assertEquals(repo.findAll(ids, fetch).collect(toList()), asList(entityType0, entityType2));
	}

	@WithMockUser(username = USERNAME, authorities = "ROLE_SU")
	@Test
	public void aggregateSu()
	{
		aggregateSuOrSystem();
	}

	@WithMockUser(username = USERNAME, authorities = "ROLE_SYSTEM")
	@Test
	public void aggregateSystem()
	{
		aggregateSuOrSystem();
	}

	private void aggregateSuOrSystem()
	{
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		AggregateResult aggregateResult = mock(AggregateResult.class);
		when(delegateRepository.aggregate(aggregateQuery)).thenReturn(aggregateResult);
		assertEquals(repo.aggregate(aggregateQuery), aggregateResult);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void aggregateUser()
	{
		AggregateQuery aggregateQuery = mock(AggregateQuery.class);
		repo.aggregate(aggregateQuery);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void delete()
	{
		String entityTypeId = "entityTypeId";
		when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(entityTypeId),
				EntityTypePermission.DELETE_METADATA)).thenReturn(true);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId").getMock();
		repo.delete(entityType);
		verify(mutableAclService).deleteAcl(new EntityTypeIdentity(entityTypeId), true);
		verify(mutableAclClassService).deleteAclClass("entity-" + entityTypeId);
		verify(delegateRepository).delete(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:DELETE_METADATA entityTypeId:entityTypeId")
	public void deleteNotAllowed()
	{
		String entityTypeId = "entityTypeId";
		when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(entityTypeId),
				EntityTypePermission.DELETE_METADATA)).thenReturn(false);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId");

		repo.delete(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = SystemMetadataModificationException.class)
	public void deleteSystemEntityType()
	{
		String entityTypeId = "entityTypeId";
		when(systemEntityTypeRegistry.hasSystemEntityType("entityTypeId")).thenReturn(true);
		when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(entityTypeId),
				EntityTypePermission.DELETE_METADATA)).thenReturn(true);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId").getMock();

		repo.delete(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void update()
	{
		String entityTypeId = "entityTypeId";

		EntityType entityType = mock(EntityType.class);
		Package pack = mock(Package.class);
		MutableAcl packageAcl = mock(MutableAcl.class);
		MutableAcl acl = mock(MutableAcl.class);

		when(pack.getId()).thenReturn("test");
		when(entityType.getId()).thenReturn(entityTypeId).getMock();
		when(entityType.getPackage()).thenReturn(pack);
		doReturn(packageAcl).when(mutableAclService).readAclById(new PackageIdentity("test"));
		doReturn(acl).when(mutableAclService).readAclById(new EntityTypeIdentity(entityTypeId));

		when(dataService.findOneById(EntityTypeMetadata.ENTITY_TYPE_META_DATA, entityType.getId(),
				EntityType.class)).thenReturn(entityType);

		when(acl.getParentAcl()).thenReturn(packageAcl);

		doReturn(true).when(userPermissionEvaluator)
					  .hasPermission(new EntityTypeIdentity(entityTypeId), UPDATE_METADATA);

		repo.update(entityType);
		verify(delegateRepository).update(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = PackagePermissionDeniedException.class)
	public void updateNoPermissionOnPack()
	{
		String entityTypeId = "entityTypeId";

		EntityType entityType = mock(EntityType.class);
		Package pack = mock(Package.class);
		EntityType oldEntityType = mock(EntityType.class);

		when(pack.getId()).thenReturn("test");
		when(entityType.getId()).thenReturn(entityTypeId).getMock();
		when(entityType.getPackage()).thenReturn(pack);

		when(dataService.findOneById(EntityTypeMetadata.ENTITY_TYPE_META_DATA, entityType.getId(),
				EntityType.class)).thenReturn(oldEntityType);

		doReturn(false).when(userPermissionEvaluator)
					   .hasPermission(new PackageIdentity("test"), PackagePermission.ADD_ENTITY_TYPE);

		repo.update(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = NullPackageNotSuException.class)
	public void updateToNullPackage()
	{
		String entityTypeId = "entityTypeId";

		EntityType entityType = mock(EntityType.class);
		Package pack = mock(Package.class);
		EntityType oldEntityType = mock(EntityType.class);

		when(entityType.getId()).thenReturn(entityTypeId).getMock();
		when(oldEntityType.getPackage()).thenReturn(pack);

		when(dataService.findOneById(EntityTypeMetadata.ENTITY_TYPE_META_DATA, entityType.getId(),
				EntityType.class)).thenReturn(oldEntityType);

		repo.update(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void updateFromNullToNullPackage()
	{
		String entityTypeId = "entityTypeId";

		EntityType entityType = mock(EntityType.class);
		EntityType oldEntityType = mock(EntityType.class);
		MutableAcl acl = mock(MutableAcl.class);

		when(entityType.getId()).thenReturn(entityTypeId).getMock();
		doReturn(acl).when(mutableAclService).readAclById(new EntityTypeIdentity(entityTypeId));

		when(dataService.findOneById(EntityTypeMetadata.ENTITY_TYPE_META_DATA, entityType.getId(),
				EntityType.class)).thenReturn(oldEntityType);

		doReturn(true).when(userPermissionEvaluator)
					  .hasPermission(new EntityTypeIdentity(entityTypeId), UPDATE_METADATA);

		repo.update(entityType);
		verify(delegateRepository).update(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "permission:UPDATE_METADATA entityTypeId:entityTypeId")
	public void updateNotAllowed()
	{
		String entityTypeId = "entityTypeId";
		when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(entityTypeId), UPDATE_METADATA)).thenReturn(
				false);

		EntityType entityType = mock(EntityType.class);
		Package pack = mock(Package.class);

		when(entityType.getId()).thenReturn(entityTypeId).getMock();
		when(entityType.getPackage()).thenReturn(pack);
		when(pack.getId()).thenReturn("id");

		when(dataService.findOneById(EntityTypeMetadata.ENTITY_TYPE_META_DATA, entityType.getId(),
				EntityType.class)).thenReturn(entityType);

		repo.update(entityType);
	}

	@WithMockUser(username = USERNAME)
	@Test(expectedExceptions = SystemMetadataModificationException.class)
	public void updateSystemEntityType()
	{
		String entityTypeId = "entityTypeId";
		when(systemEntityTypeRegistry.hasSystemEntityType(entityTypeId)).thenReturn(true);
		when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(entityTypeId), UPDATE_METADATA)).thenReturn(
				true);

		EntityType entityType = mock(EntityType.class);
		Package pack = mock(Package.class);
		when(pack.getId()).thenReturn("test");

		when(entityType.getId()).thenReturn(entityTypeId).getMock();
		when(entityType.getPackage()).thenReturn(pack);

		when(dataService.findOneById(EntityTypeMetadata.ENTITY_TYPE_META_DATA, entityType.getId(),
				EntityType.class)).thenReturn(entityType);

		repo.update(entityType);
	}

	static class Config
	{

	}
}