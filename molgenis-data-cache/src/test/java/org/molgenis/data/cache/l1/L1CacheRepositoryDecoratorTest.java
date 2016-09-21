package org.molgenis.data.cache.l1;

import com.google.common.collect.Sets;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.EntityKey.create;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = L1CacheRepositoryDecoratorTest.Config.class)
public class L1CacheRepositoryDecoratorTest extends AbstractMolgenisSpringTest
{
	private L1CacheRepositoryDecorator l1CacheRepositoryDecorator;
	private EntityMetaData entityMetaData;

	private final String entityName = "TestRepository";
	private final String entityID = "1";
	private final String entityID2 = "2";
	private Entity entity;
	private Entity entity2;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	@Mock
	private L1Cache l1Cache;

	@Mock
	private Repository<Entity> decoratedRepository;

	@Captor
	private ArgumentCaptor<Stream<Entity>> entitiesCaptor;

	@Captor
	private ArgumentCaptor<Stream<Object>> entityIdsCaptor;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);

		entityMetaData = entityMetaDataFactory.create(entityName);
		entityMetaData.addAttribute(attributeMetaDataFactory.create().setName("ID"), ROLE_ID);
		entityMetaData.addAttribute(attributeMetaDataFactory.create().setName("ATTRIBUTE_1"));

		entity = new DynamicEntity(entityMetaData);
		entity.set("ID", entityID);
		entity.set("ATTRIBUTE_1", "test_value_1");

		entity2 = new DynamicEntity(entityMetaData);
		entity2.set("ID", entityID2);
		entity2.set("ATTRIBUTE_1", "test_value_2");

		when(decoratedRepository.getCapabilities()).thenReturn(Sets.newHashSet(CACHEABLE, WRITABLE));
		when(decoratedRepository.getName()).thenReturn(entityName);
		when(decoratedRepository.getEntityMetaData()).thenReturn(entityMetaData);

		l1CacheRepositoryDecorator = new L1CacheRepositoryDecorator(decoratedRepository, l1Cache);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		reset(l1Cache);
	}

	@Test
	public void testAdd()
	{
		l1CacheRepositoryDecorator.add(entity);
		verify(l1Cache).put(entityName, entity);
	}

	@Test
	public void testAddWithStreamOfEntities()
	{
		l1CacheRepositoryDecorator.add(Stream.of(entity));

		verify(decoratedRepository.add(entitiesCaptor.capture()));
		entitiesCaptor.getValue().collect(Collectors.toList());
		verify(l1Cache).put(entityName, entity);
	}

	@Test
	public void testDelete()
	{
		l1CacheRepositoryDecorator.delete(entity);
		verify(l1Cache).putDeletion(create(entity));
	}

	@Test
	public void testDeleteById()
	{
		l1CacheRepositoryDecorator.deleteById(entityID);
		verify(l1Cache).putDeletion(create(entity));
	}

	@Test
	public void testDeleteByStreamOfEntities()
	{
		l1CacheRepositoryDecorator.delete(Stream.of(entity));

		verify(decoratedRepository).delete(entitiesCaptor.capture());
		entitiesCaptor.getValue().collect(Collectors.toList());
		verify(l1Cache).putDeletion(EntityKey.create(entity));
	}

	@Test
	public void testDeleteAll()
	{
		l1CacheRepositoryDecorator.deleteAll();
		verify(l1Cache).evictAll(entityName);
	}

	@Test
	public void testDeleteAllByStreamOfIds()
	{
		l1CacheRepositoryDecorator.deleteAll(Stream.of(entityID));

		verify(decoratedRepository).deleteAll(entityIdsCaptor.capture());
		entityIdsCaptor.getValue().collect(Collectors.toList());
		verify(l1Cache).putDeletion(EntityKey.create(entityName, entityID));
	}

	@Test
	public void testUpdate()
	{
		l1CacheRepositoryDecorator.update(entity);
		verify(l1Cache).put(entityName, entity);
		verify(decoratedRepository).update(entity);
	}

	@Test
	public void testUpdateWithStreamOfEntities()
	{
		l1CacheRepositoryDecorator.update(Stream.of(entity));

		verify(decoratedRepository).update(entitiesCaptor.capture());
		entitiesCaptor.getValue().collect(Collectors.toList());
		verify(l1Cache).put(entityName, entity);
	}

	@Test
	public void testFindOneByIdReturnsEntity()
	{
		when(l1Cache.get(entityName, entityID, entityMetaData)).thenReturn(of(entity));
		Entity actualEntity = l1CacheRepositoryDecorator.findOneById(entityID);
		assertEquals(actualEntity, entity);

		verify(decoratedRepository, never()).findOneById(Mockito.any());
	}

	@Test
	public void testFindOneByIdReturnsEmpty()
	{
		when(l1Cache.get(entityName, entityID, entityMetaData)).thenReturn(empty());
		Entity actualEntity = l1CacheRepositoryDecorator.findOneById(entityID);
		assertNull(actualEntity);

		verify(decoratedRepository, never()).findOneById(Mockito.any());
	}

	@Test
	public void testFindOneByIdReturnsNull()
	{
		when(l1Cache.get(entityName, entityID, entityMetaData)).thenReturn(null);
		Entity actualEntity = l1CacheRepositoryDecorator.findOneById(entityID);
		assertNull(actualEntity);

		verify(decoratedRepository).findOneById(entityID);

	}

	@Test
	public void testFindAllByStreamOfIdsEntityNotPresentInCache()
	{
		when(decoratedRepository.findAll(entityIdsCaptor.capture())).thenReturn(Stream.of(entity, entity2));

		List<Entity> actual = l1CacheRepositoryDecorator.findAll(Stream.of(entityID, entityID2))
				.collect(Collectors.toList());
		assertEquals(asList(entity, entity2), actual);

		List<Object> ids = entityIdsCaptor.getValue().collect(Collectors.toList());
		assertEquals(ids, asList(entityID, entityID2));
	}

	@Test
	public void testFindAllByStreamOfIdsOneCachedOneMissing()
	{
		when(l1Cache.get(entityName, entityID, entityMetaData)).thenReturn(of(entity));

		when(decoratedRepository.findAll(entityIdsCaptor.capture())).thenReturn(Stream.of(entity2));

		List<Entity> actual = l1CacheRepositoryDecorator.findAll(Stream.of(entityID, entityID2))
				.collect(Collectors.toList());
		assertEquals(asList(entity, entity2), actual);

		List<Object> ids = entityIdsCaptor.getValue().collect(Collectors.toList());
		assertEquals(ids, Collections.singletonList(entityID2));
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
