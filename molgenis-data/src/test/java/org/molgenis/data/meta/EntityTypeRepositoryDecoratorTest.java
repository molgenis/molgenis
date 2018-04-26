package org.molgenis.data.meta;

import com.google.common.collect.Lists;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.testng.Assert.assertEquals;

@SuppressWarnings("deprecation")
public class EntityTypeRepositoryDecoratorTest extends AbstractMockitoTest
{
	private final String entityTypeId1 = "EntityType1";
	private final String entityTypeId2 = "EntityType2";

	private EntityTypeRepositoryDecorator repo;
	@Mock
	private Repository<EntityType> delegateRepository;
	@Mock
	private DataService dataService;
	@Mock
	private MetaDataService metaDataService;
	@Mock
	private EntityType entityType1;
	@Mock
	private EntityType entityType2;
	@Mock
	private EntityType entityType3;
	@Mock
	private EntityTypeDependencyResolver entityTypeDependencyResolver;
	@Mock
	private RepositoryCollection repositoryCollection;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		repo = new EntityTypeRepositoryDecorator(delegateRepository, dataService, entityTypeDependencyResolver);
	}

	@Test
	public void addWithKnownBackend()
	{
		when(entityType1.getId()).thenReturn(entityTypeId1);
		when(dataService.getMeta()).thenReturn(metaDataService);
		when(metaDataService.getBackend(entityType1)).thenReturn(repositoryCollection);

		repo.add(entityType1);
		verify(delegateRepository).add(entityType1);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Unknown backend \\[backend\\]")
	public void addWithUnknownBackend()
	{
		when(entityType1.getId()).thenReturn(entityTypeId1);
		String backend = "backend";
		when(entityType1.getBackend()).thenReturn(backend);
		when(dataService.getMeta()).thenReturn(metaDataService);

		repo.add(entityType1);
		verify(delegateRepository).add(entityType1);
	}

	@Test
	public void delete()
	{
		String backend = "backend";
		when(entityType1.getBackend()).thenReturn(backend);
		when(dataService.getMeta()).thenReturn(metaDataService);
		when(metaDataService.getBackend(backend)).thenReturn(repositoryCollection);

		Attribute attr0 = mock(Attribute.class);
		Attribute attrCompound = mock(Attribute.class);
		Attribute attr1a = mock(Attribute.class);
		Attribute attr1b = mock(Attribute.class);
		when(attrCompound.getChildren()).thenReturn(newArrayList(attr1a, attr1b));
		when(entityType1.getOwnAttributes()).thenReturn(newArrayList(attr0, attrCompound));

		repo.delete(entityType1);

		verify(delegateRepository).delete(entityType1);
		verify(repositoryCollection).deleteRepository(entityType1);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Attribute>> attrCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(dataService).delete(eq(ATTRIBUTE_META_DATA), attrCaptor.capture());
		assertEquals(attrCaptor.getValue().collect(toList()), newArrayList(attr0, attrCompound, attr1a, attr1b));
	}

	@Test
	public void deleteAbstract()
	{
		when(entityType1.isAbstract()).thenReturn(true);
		Attribute attr0 = mock(Attribute.class);
		when(attr0.getChildren()).thenReturn(emptyList());
		when(entityType1.getOwnAttributes()).thenReturn(singletonList(attr0));

		repo.delete(entityType1);

		verify(delegateRepository).delete(entityType1);
		verify(repositoryCollection, times(0)).deleteRepository(entityType1); // entity is abstract

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Attribute>> attrCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(dataService).delete(eq(ATTRIBUTE_META_DATA), attrCaptor.capture());
		assertEquals(attrCaptor.getValue().collect(toList()), singletonList(attr0));
	}

	@Test
	public void addRemoveAttributeAbstractEntityType()
	{
		when(entityType1.getId()).thenReturn(entityTypeId1);
		when(entityType2.getId()).thenReturn(entityTypeId2);
		String entityTypeId3 = "EntityType3";
		when(entityType3.getId()).thenReturn(entityTypeId3);
		when(dataService.getMeta()).thenReturn(metaDataService);

		EntityType currentEntityType = mock(EntityType.class);
		EntityType currentEntityType2 = mock(EntityType.class);
		EntityType currentEntityType3 = mock(EntityType.class);
		doReturn(currentEntityType).when(delegateRepository).findOneById(entityTypeId1);
		doReturn(currentEntityType2).when(delegateRepository).findOneById(entityTypeId2);
		doReturn(currentEntityType3).when(delegateRepository).findOneById(entityTypeId3);

		Attribute attributeStays = mock(Attribute.class);
		when(attributeStays.getName()).thenReturn("attributeStays");
		Attribute attributeRemoved = mock(Attribute.class);
		when(attributeRemoved.getName()).thenReturn("attributeRemoved");
		Attribute attributeAdded = mock(Attribute.class);
		when(attributeAdded.getName()).thenReturn("attributeAdded");

		when(currentEntityType.isAbstract()).thenReturn(true);
		when(currentEntityType.getOwnAllAttributes()).thenReturn(Lists.newArrayList(attributeStays, attributeRemoved));
		when(entityType1.getOwnAllAttributes()).thenReturn(Lists.newArrayList(attributeStays, attributeAdded));
		when(metaDataService.getConcreteChildren(entityType1)).thenReturn(Stream.of(entityType2, entityType3));
		RepositoryCollection backend2 = mock(RepositoryCollection.class);
		RepositoryCollection backend3 = mock(RepositoryCollection.class);
		doReturn(backend2).when(metaDataService).getBackend(entityType2);
		doReturn(backend3).when(metaDataService).getBackend(entityType3);

		repo.update(entityType1);

		// verify that attributes got added and deleted in concrete extending entities
		verify(backend2).addAttribute(currentEntityType2, attributeAdded);
		verify(backend2).deleteAttribute(currentEntityType2, attributeRemoved);
		verify(backend3).addAttribute(currentEntityType3, attributeAdded);
		verify(backend3).deleteAttribute(currentEntityType3, attributeRemoved);
		verify(backend2, never()).updateRepository(any(), any());
		verify(backend3, never()).updateRepository(any(), any());
	}

	@Test
	public void updateConcreteEntityType()
	{
		when(dataService.getMeta()).thenReturn(metaDataService);
		when(metaDataService.getBackend(entityType1)).thenReturn(repositoryCollection);

		when(delegateRepository.findOneById(entityTypeId1)).thenReturn(entityType1);
		when(entityType1.getOwnAllAttributes()).thenReturn(emptyList());

		EntityType updatedEntityType1 = mock(EntityType.class);
		when(updatedEntityType1.getId()).thenReturn(entityTypeId1);
		when(updatedEntityType1.getOwnAllAttributes()).thenReturn(emptyList());

		repo.update(updatedEntityType1);

		verify(repositoryCollection).updateRepository(entityType1, updatedEntityType1);
	}

	@Test
	public void deleteEntityTypesWithOneToMany()
	{
		String backend = "backend";
		when(entityType1.getId()).thenReturn(entityTypeId1);
		when(entityType1.getBackend()).thenReturn(backend);
		when(entityType2.getId()).thenReturn(entityTypeId2);
		when(entityType2.getBackend()).thenReturn(backend);
		when(dataService.getMeta()).thenReturn(metaDataService);
		when(metaDataService.getBackend(backend)).thenReturn(repositoryCollection);

		Attribute mappedByAttribute = mock(Attribute.class);
		when(mappedByAttribute.getEntity()).thenReturn(entityType2);
		when(entityType1.getMappedByAttributes()).thenReturn(Stream.of(mappedByAttribute));
		when(entityType2.getMappedByAttributes()).thenReturn(Stream.empty());
		when(entityTypeDependencyResolver.resolve(any())).thenReturn(asList(entityType1, entityType2));
		InOrder repositoryCollectionInOrder = inOrder(repositoryCollection);
		InOrder decoratedRepoInOrder = inOrder(delegateRepository);

		repo.delete(Stream.of(entityType1, entityType2));

		verify(dataService).delete(ATTRIBUTE_META_DATA, mappedByAttribute);
		repositoryCollectionInOrder.verify(repositoryCollection).deleteRepository(entityType2);
		repositoryCollectionInOrder.verify(repositoryCollection).deleteRepository(entityType1);
		decoratedRepoInOrder.verify(delegateRepository).delete(entityType2);
		decoratedRepoInOrder.verify(delegateRepository).delete(entityType1);
	}
}