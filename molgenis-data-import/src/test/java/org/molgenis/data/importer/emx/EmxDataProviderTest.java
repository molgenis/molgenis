package org.molgenis.data.importer.emx;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.*;
import org.molgenis.data.importer.ParsedMetaData;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class EmxDataProviderTest extends AbstractMockitoTest
{
	@Mock
	private EmxImportJob emxImportJob;

	@Mock
	private EntityManager entityManager;

	private EmxDataProvider emxDataProvider;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		emxDataProvider = new EmxDataProvider(emxImportJob, entityManager);
	}

	public EmxDataProviderTest()
	{
		super(Strictness.WARN);
	}

	@Test
	public void testGetEntityTypes() throws Exception
	{
		ImmutableCollection<EntityType> entityTypes = ImmutableList.of();
		ParsedMetaData parsedMetaData = mock(ParsedMetaData.class);
		when(parsedMetaData.getEntities()).thenReturn(entityTypes).getMock();
		when(emxImportJob.getParsedMetaData()).thenReturn(parsedMetaData);

		assertEquals(emxDataProvider.getEntityTypes().collect(toList()), emptyList());
	}

	@Test
	public void testHasEntitiesTrue() throws Exception
	{
		String entityTypeId = "EntityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();

		RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
		when(repositoryCollection.hasRepository(entityType)).thenReturn(true);
		when(emxImportJob.getSource()).thenReturn(repositoryCollection);

		assertTrue(emxDataProvider.hasEntities(entityType));
	}

	@Test
	public void testHasEntitiesFalse() throws Exception
	{
		String entityTypeId = "EntityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();

		RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
		when(repositoryCollection.hasRepository(entityType)).thenReturn(false);
		when(emxImportJob.getSource()).thenReturn(repositoryCollection);

		assertFalse(emxDataProvider.hasEntities(entityType));
	}

	@Test
	public void testHasEntitiesAlternativeEntityTypeIdTrue() throws Exception
	{
		String entityTypeId = "base_EntityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();

		RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
		when(repositoryCollection.hasRepository("EntityTypeId")).thenReturn(true);
		when(emxImportJob.getSource()).thenReturn(repositoryCollection);

		assertTrue(emxDataProvider.hasEntities(entityType));
	}

	@Test
	public void testHasEntitiesAlternativeEntityTypeIdFalse() throws Exception
	{
		String entityTypeId = "base_EntityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();

		RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
		when(repositoryCollection.hasRepository("EntityTypeId")).thenReturn(false);
		when(emxImportJob.getSource()).thenReturn(repositoryCollection);

		assertFalse(emxDataProvider.hasEntities(entityType));
	}

	@Test
	public void testGetEntities() throws Exception
	{
		EntityType entityType = mock(EntityType.class);
		RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
		@SuppressWarnings("unchecked")
		Repository<Entity> repository = mock(Repository.class);
		when(repository.spliterator()).thenReturn(Collections.<Entity>emptyList().spliterator());
		when(repositoryCollection.getRepository(entityType)).thenReturn(repository);
		when(emxImportJob.getSource()).thenReturn(repositoryCollection);
		assertEquals(emxDataProvider.getEntities(entityType).collect(toList()), emptyList());
	}

	@Test
	public void testGetEntitiesAlternativeEntityTypeId() throws Exception
	{
		String entityTypeId = "base_EntityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();

		RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
		@SuppressWarnings("unchecked")
		Repository<Entity> repository = mock(Repository.class);
		when(repository.spliterator()).thenReturn(Collections.<Entity>emptyList().spliterator());
		when(repositoryCollection.getRepository("EntityTypeId")).thenReturn(repository);
		when(emxImportJob.getSource()).thenReturn(repositoryCollection);
		assertEquals(emxDataProvider.getEntities(entityType).collect(toList()), emptyList());

	}

	@Test(expectedExceptions = UnknownRepositoryException.class)
	public void testGetEntitiesUnknownRepository() throws Exception
	{
		String entityTypeId = "EntityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();

		RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
		doThrow(new UnknownRepositoryException(entityTypeId)).when(repositoryCollection).getRepository(entityType);
		when(emxImportJob.getSource()).thenReturn(repositoryCollection);

		assertEquals(emxDataProvider.getEntities(entityType).collect(toList()), emptyList());
	}

	@Test(expectedExceptions = UnknownRepositoryException.class)
	public void testGetEntitiesAlternativeEntityTypeIdUnknownRepository() throws Exception
	{
		String entityTypeId = "base_EntityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();

		RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
		doThrow(new UnknownRepositoryException("EntityTypeId")).when(repositoryCollection)
															   .getRepository("EntityTypeId");
		when(emxImportJob.getSource()).thenReturn(repositoryCollection);
		assertEquals(emxDataProvider.getEntities(entityType).collect(toList()), emptyList());
	}
}