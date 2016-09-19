package org.molgenis.data.cache.l1;

import com.google.common.collect.Sets;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.Entity;
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

import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.EntityKey.create;
import static org.molgenis.data.EntityManager.CreationMode.NO_POPULATE;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = L1CacheRepositoryDecoratorTest.Config.class)
public class L1CacheRepositoryDecoratorTest extends AbstractMolgenisSpringTest
{
	private L1CacheRepositoryDecorator l1CacheRepositoryDecorator;
	private EntityMetaData entityMetaData;
	private Entity mockEntity;

	private final String repository = "TestRepository";
	private final String entityID = "1";

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	private EntityManager entityManager;

	@Mock
	private L1Cache l1Cache;

	@Mock
	private Repository<Entity> decoratedRepository;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);

		entityMetaData = entityMetaDataFactory.create(repository);
		entityMetaData.addAttribute(attributeMetaDataFactory.create().setName("ID"), ROLE_ID);
		entityMetaData.addAttribute(attributeMetaDataFactory.create().setName("ATTRIBUTE_1"));

		when(entityManager.create(entityMetaData, NO_POPULATE)).thenReturn(new DynamicEntity(entityMetaData));

		mockEntity = entityManager.create(entityMetaData, NO_POPULATE);
		mockEntity.set("ID", entityID);
		mockEntity.set("ATTRIBUTE_1", "test_value_1");

		when(decoratedRepository.getCapabilities()).thenReturn(Sets.newHashSet(CACHEABLE, WRITABLE));
		when(decoratedRepository.getName()).thenReturn(repository);
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
		l1CacheRepositoryDecorator.add(mockEntity);
		verify(l1Cache).put(repository, mockEntity);
	}

	@Test //FIXME l1Cache is used but not?
	public void testAddWithStreamOfEntities()
	{
		l1CacheRepositoryDecorator.add(Stream.of(mockEntity));
		// verify(l1Cache).put(repository, mockEntity);
	}

	@Test
	public void testDelete()
	{
		l1CacheRepositoryDecorator.delete(mockEntity);
		verify(l1Cache).putDeletion(create(mockEntity));
	}

	@Test
	public void testDeleteById()
	{
		l1CacheRepositoryDecorator.deleteById(entityID);
		verify(l1Cache).putDeletion(create(mockEntity));
	}

	@Test //FIXME l1Cache is used but not?
	public void testDeleteByStreamOfEntities()
	{
		l1CacheRepositoryDecorator.delete(Stream.of(mockEntity));
		// verify(l1Cache).putDeletion(EntityKey.create(mockEntity));
	}

	@Test
	public void testDeleteAll()
	{
		l1CacheRepositoryDecorator.deleteAll();
		verify(l1Cache).evictAll(repository);
	}

	@Test //FIXME l1Cache is used but not?
	public void testDeleteAllByStreamOfIds()
	{
		l1CacheRepositoryDecorator.deleteAll(Stream.of(entityID));
		// verify(l1Cache).putDeletion(EntityKey.create(repository, entityID));
	}

	@Test
	public void testUpdate()
	{
		l1CacheRepositoryDecorator.update(mockEntity);
		verify(l1Cache).put(repository, mockEntity);
	}

	@Test //FIXME l1Cache is used but not?
	public void testUpdateWithStreamOfEntities()
	{
		l1CacheRepositoryDecorator.update(Stream.of(mockEntity));
		// verify(l1Cache).put(repository, mockEntity);
	}

	@Test
	public void testFindOneByIdReturnsEntity()
	{
		when(l1Cache.get(repository, entityID, entityMetaData)).thenReturn(of(mockEntity));
		Entity actualEntity = l1CacheRepositoryDecorator.findOneById(entityID);
		assertEquals(actualEntity, mockEntity);

		verify(decoratedRepository, never()).findOneById(Mockito.any());
	}

	@Test
	public void testFindOneByIdReturnsEmpty()
	{
		when(l1Cache.get(repository, entityID, entityMetaData)).thenReturn(empty());
		Entity actualEntity = l1CacheRepositoryDecorator.findOneById(entityID);
		assertNull(actualEntity);

		verify(decoratedRepository, never()).findOneById(Mockito.any());
	}

	@Test
	public void testFindOneByIdReturnsNull()
	{
		when(l1Cache.get(repository, entityID, entityMetaData)).thenReturn(null);
		Entity actualEntity = l1CacheRepositoryDecorator.findOneById(entityID);
		assertNull(actualEntity);

		verify(decoratedRepository).findOneById(entityID);

	}

	@Test
	public void testFindAllByStreamOfIds()
	{
		l1CacheRepositoryDecorator.findAll(Stream.of(entityID));
		// TODO Hard test
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
