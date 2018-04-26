package org.molgenis.data.cache.l1;

import com.google.common.collect.Sets;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.quality.Strictness;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.LazyEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.EntityKey.create;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNull;

@ContextConfiguration(classes = L1CacheRepositoryDecoratorTest.Config.class)
public class L1CacheRepositoryDecoratorTest extends AbstractMolgenisSpringTest
{
	private L1CacheRepositoryDecorator l1CacheRepositoryDecorator;

	private EntityType authorMetaData;
	private EntityType bookMetaData;

	private final String authorEntityName = "Author";
	private final String bookEntityName = "Book";
	private final String authorID = "1";
	private final String authorID2 = "2";
	private final String bookID = "b1";
	private final String bookID2 = "b2";
	private Entity author;
	private Entity author2;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attributeFactory;

	@Autowired
	private DataService dataService;

	@Mock
	private L1Cache l1Cache;

	@Mock
	private Repository<Entity> authorRepository;

	@Mock
	private Repository<Entity> bookRepository;

	@Captor
	private ArgumentCaptor<Stream<Entity>> entitiesCaptor;

	@Captor
	private ArgumentCaptor<Stream<Object>> entityIdsCaptor;

	@Captor
	private ArgumentCaptor<Stream<EntityKey>> entityKeysCaptor;

	public L1CacheRepositoryDecoratorTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		authorMetaData = entityTypeFactory.create(authorEntityName);
		bookMetaData = entityTypeFactory.create(bookEntityName);

		authorMetaData.addAttribute(attributeFactory.create().setName("ID"), ROLE_ID);
		authorMetaData.addAttribute(attributeFactory.create().setName("name"));
		Attribute authorAttribute = attributeFactory.create()
													.setName("author")
													.setDataType(XREF)
													.setRefEntity(authorMetaData);
		authorMetaData.addAttribute(attributeFactory.create()
													.setName("books")
													.setDataType(ONE_TO_MANY)
													.setMappedBy(authorAttribute)
													.setRefEntity(bookMetaData));
		bookMetaData.addAttribute(attributeFactory.create().setName("ID"), ROLE_ID);
		bookMetaData.addAttribute(attributeFactory.create().setName("title"));
		bookMetaData.addAttribute(authorAttribute);

		author = new DynamicEntity(authorMetaData);
		author.set("ID", authorID);
		author.set("name", "Terry Pratchett");
		author.set("books", Arrays.asList(new LazyEntity(bookMetaData, dataService, bookID),
				new LazyEntity(bookMetaData, dataService, bookID2)));

		author2 = new DynamicEntity(authorMetaData);
		author2.set("ID", authorID2);
		author2.set("name", "Stephen King");

		when(authorRepository.getCapabilities()).thenReturn(Sets.newHashSet(CACHEABLE, WRITABLE));
		when(authorRepository.getName()).thenReturn(authorEntityName);
		when(authorRepository.getEntityType()).thenReturn(authorMetaData);

		l1CacheRepositoryDecorator = new L1CacheRepositoryDecorator(authorRepository, l1Cache);
	}

	@Test
	public void testAdd()
	{
		l1CacheRepositoryDecorator.add(author);
		verify(l1Cache).put(authorEntityName, author);
		verify(l1Cache).evict(entityKeysCaptor.capture());
		assertEquals(entityKeysCaptor.getValue().collect(Collectors.toList()),
				Arrays.asList(EntityKey.create(bookEntityName, bookID), EntityKey.create(bookEntityName, bookID2)));
		verifyNoMoreInteractions(l1Cache);
	}

	@Test
	public void testAddWithStreamOfEntities()
	{
		l1CacheRepositoryDecorator.add(Stream.of(author));

		verify(authorRepository).add(entitiesCaptor.capture());
		entitiesCaptor.getValue().collect(Collectors.toList());
		verify(l1Cache).put(authorEntityName, author);
		verify(l1Cache).evictAll(bookMetaData);
		verifyNoMoreInteractions(l1Cache);
	}

	@Test
	public void testDelete()
	{
		l1CacheRepositoryDecorator.delete(author);
		verify(l1Cache).putDeletion(create(author));
		verify(l1Cache).evict(entityKeysCaptor.capture());
		assertEquals(entityKeysCaptor.getValue().collect(Collectors.toList()),
				Arrays.asList(EntityKey.create(bookEntityName, bookID), EntityKey.create(bookEntityName, bookID2)));

		verifyNoMoreInteractions(l1Cache);
	}

	@Test
	public void testDeleteById()
	{
		l1CacheRepositoryDecorator.deleteById(authorID);
		verify(l1Cache).putDeletion(create(author));
		verify(l1Cache).evictAll(bookMetaData);
		verifyNoMoreInteractions(l1Cache);
	}

	@Test
	public void testDeleteByStreamOfEntities()
	{
		l1CacheRepositoryDecorator.delete(Stream.of(author));

		verify(authorRepository).delete(entitiesCaptor.capture());
		entitiesCaptor.getValue().collect(Collectors.toList());
		verify(l1Cache).putDeletion(EntityKey.create(author));
		verify(l1Cache).evictAll(bookMetaData);
		verifyNoMoreInteractions(l1Cache);
	}

	@Test
	public void testDeleteAll()
	{
		l1CacheRepositoryDecorator.deleteAll();
		verify(l1Cache).evictAll(authorMetaData);
		verify(l1Cache).evictAll(bookMetaData);
		verifyNoMoreInteractions(l1Cache);
	}

	@Test
	public void testDeleteAllByStreamOfIds()
	{
		l1CacheRepositoryDecorator.deleteAll(Stream.of(authorID));

		verify(authorRepository).deleteAll(entityIdsCaptor.capture());
		entityIdsCaptor.getValue().collect(Collectors.toList());
		verify(l1Cache).putDeletion(EntityKey.create(authorEntityName, authorID));
		verify(l1Cache).evictAll(bookMetaData);
		verifyNoMoreInteractions(l1Cache);
	}

	@Test
	public void testUpdate()
	{
		l1CacheRepositoryDecorator.update(author);
		verify(l1Cache).put(authorEntityName, author);
		verify(authorRepository).update(author);
		verify(l1Cache).evictAll(bookMetaData);
		verifyNoMoreInteractions(l1Cache);
	}

	@Test
	public void testUpdateWithStreamOfEntities()
	{
		l1CacheRepositoryDecorator.update(Stream.of(author));

		verify(authorRepository).update(entitiesCaptor.capture());
		entitiesCaptor.getValue().collect(Collectors.toList());
		verify(l1Cache).put(authorEntityName, author);
		verify(l1Cache).evictAll(bookMetaData);
		verifyNoMoreInteractions(l1Cache);
	}

	@Test
	public void testFindOneByIdReturnsEntity()
	{
		when(l1Cache.get(authorEntityName, authorID, authorMetaData)).thenReturn(of(author));
		Entity actualEntity = l1CacheRepositoryDecorator.findOneById(authorID);
		assertEquals(actualEntity, author);

		verify(authorRepository, never()).findOneById(Mockito.any());
	}

	@Test
	public void testFindOneByIdReturnsEmpty()
	{
		when(l1Cache.get(authorEntityName, authorID, authorMetaData)).thenReturn(empty());
		Entity actualEntity = l1CacheRepositoryDecorator.findOneById(authorID);
		assertNull(actualEntity);

		verify(authorRepository, never()).findOneById(Mockito.any());
	}

	@Test
	public void testFindOneByIdReturnsNull()
	{
		when(l1Cache.get(authorEntityName, authorID, authorMetaData)).thenReturn(null);
		Entity actualEntity = l1CacheRepositoryDecorator.findOneById(authorID);
		assertNull(actualEntity);

		verify(authorRepository).findOneById(authorID);
	}

	@Test
	public void testFindAllByStreamOfIdsEntityNotPresentInCache()
	{
		when(l1Cache.get(authorEntityName, authorID, authorMetaData)).thenReturn(null);
		when(l1Cache.get(authorEntityName, authorID2, authorMetaData)).thenReturn(null);
		when(authorRepository.findAll(entityIdsCaptor.capture())).thenReturn(Stream.of(author, author2));

		List<Entity> actual = l1CacheRepositoryDecorator.findAll(Stream.of(authorID, authorID2))
														.collect(Collectors.toList());
		assertEquals(actual, asList(author, author2));

		List<Object> ids = entityIdsCaptor.getValue().collect(Collectors.toList());
		assertEquals(ids, asList(authorID, authorID2));
	}

	@Test
	public void testFindAllByStreamOfIdsOneCachedOneMissing()
	{
		when(l1Cache.get(authorEntityName, authorID, authorMetaData)).thenReturn(of(author));
		when(l1Cache.get(authorEntityName, authorID2, authorMetaData)).thenReturn(null);

		when(authorRepository.findAll(entityIdsCaptor.capture())).thenReturn(Stream.of(author2));

		List<Entity> actual = l1CacheRepositoryDecorator.findAll(Stream.of(authorID, authorID2))
														.collect(Collectors.toList());

		List<Object> ids = entityIdsCaptor.getValue().collect(Collectors.toList());
		assertEquals(ids, Collections.singletonList(authorID2));
		assertEquals(actual, asList(author, author2));
	}

	@Configuration
	public static class Config
	{
		@Mock
		private EntityManager entityManager;

		public Config()
		{
			initMocks(this);
		}

		@Bean
		public EntityManager entityManager()
		{
			return entityManager;
		}
	}
}
