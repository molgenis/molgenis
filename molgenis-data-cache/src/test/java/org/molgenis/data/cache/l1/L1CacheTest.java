package org.molgenis.data.cache.l1;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.EntityManager;
import org.molgenis.data.cache.utils.EntityHydration;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.data.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.EntityKey.create;
import static org.molgenis.data.EntityManager.CreationMode.NO_POPULATE;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = L1CacheTest.Config.class)
public class L1CacheTest extends AbstractMolgenisSpringTest
{
	private L1Cache l1Cache;
	private EntityType entityType;
	private Entity entity1;
	private Entity entity2;

	private final String transactionID = "__TRANSACTION__";
	private final String repository = "TestRepository";
	private final String entityID1 = "1";
	private final String entityID2 = "2";

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attributeFactory;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private EntityHydration entityHydration;

	@Mock
	private TransactionManager transactionManager;

	@BeforeClass
	public void beforeClass()
	{
		entityType = entityTypeFactory.create(repository);
		entityType.addAttribute(attributeFactory.create().setName("ID"), ROLE_ID);
		entityType.addAttribute(attributeFactory.create().setName("ATTRIBUTE_1"));

		Mockito.when(entityManager.create(entityType, NO_POPULATE)).thenReturn(new DynamicEntity(entityType));

		entity1 = new DynamicEntity(entityType);
		entity1.set("ID", entityID1);
		entity1.set("ATTRIBUTE_1", "test_value_1");

		entity2 = new DynamicEntity(entityType);
		entity2.set("ID", entityID2);
		entity2.set("ATTRIBUTE_1", "test_value_2");
	}

	@BeforeMethod
	public void beforeMethod()
	{
		l1Cache = new L1Cache(transactionManager, entityHydration);
	}

	@Test
	public void testWhenNotInTransaction()
	{
		// Without a transactionStarted() call the cache does not exist, return null
		l1Cache.put(repository, entity1);
		Optional<Entity> actualEntity = l1Cache.get(repository, entityID1, entityType);
		assertEquals(actualEntity, null);
	}

	@Test
	public void testPutAndGetWhenInTransaction()
	{
		// Start transaction
		l1Cache.transactionStarted(transactionID);

		// Entity has not been added to cache, return null
		Optional<Entity> actualEntity = l1Cache.get(repository, entityID1, entityType);
		assertEquals(actualEntity, null);

		// Entity has been added to cache, return entity
		l1Cache.put(repository, entity1);
		Entity result = l1Cache.get(repository, entityID1, entityType).get();
		assertTrue(EntityUtils.equals(result, entity1));

		// Cleanup after transaction and expect the cache to be cleared, return null
		l1Cache.doCleanupAfterCompletion(transactionID);
		actualEntity = l1Cache.get(repository, entityID1, entityType);
		assertEquals(actualEntity, null);
	}

	@Test
	public void testEvictStream()
	{
		// Start transaction
		l1Cache.transactionStarted(transactionID);

		// Entity has been added to cache, return entity
		l1Cache.put(repository, entity1);
		l1Cache.put(repository, entity2);
		Entity actualEntity = l1Cache.get(repository, entityID1, entityType).get();
		assertTrue(EntityUtils.equals(actualEntity, entity1));
		actualEntity = l1Cache.get(repository, entityID2, entityType).get();
		assertTrue(EntityUtils.equals(actualEntity, entity2));

		l1Cache.evict(Stream.of(EntityKey.create(entity1), EntityKey.create(entity2)));

		Optional<Entity> result = l1Cache.get(repository, entityID1, entityType);
		assertEquals(result, null);
		result = l1Cache.get(repository, entityID2, entityType);
		assertEquals(result, null);
	}

	@Test
	public void testEvictStreamOfOneEntity()
	{
		// Start transaction
		l1Cache.transactionStarted(transactionID);

		// Entity has been added to cache, return entity
		l1Cache.put(repository, entity1);
		l1Cache.put(repository, entity2);
		Entity actualEntity = l1Cache.get(repository, entityID1, entityType).get();
		assertTrue(EntityUtils.equals(actualEntity, entity1));
		actualEntity = l1Cache.get(repository, entityID2, entityType).get();
		assertTrue(EntityUtils.equals(actualEntity, entity2));

		l1Cache.evict(Stream.of(EntityKey.create(entity2)));

		actualEntity = l1Cache.get(repository, entityID1, entityType).get();
		assertTrue(EntityUtils.equals(actualEntity, entity1));
		Optional<Entity> result = l1Cache.get(repository, entityID2, entityType);
		assertEquals(result, null);
	}

	@Test
	public void testEvictAll()
	{
		// Start transaction
		l1Cache.transactionStarted(transactionID);

		// Entity has been added to cache, return entity
		l1Cache.put(repository, entity1);
		l1Cache.put(repository, entity2);
		Entity actualEntity = l1Cache.get(repository, entityID1, entityType).get();
		assertTrue(EntityUtils.equals(actualEntity, entity1));
		actualEntity = l1Cache.get(repository, entityID2, entityType).get();
		assertTrue(EntityUtils.equals(actualEntity, entity2));

		l1Cache.evictAll(entityType);

		Optional<Entity> result = l1Cache.get(repository, entityID1, entityType);
		assertEquals(result, null);
		result = l1Cache.get(repository, entityID2, entityType);
		assertEquals(result, null);
	}

	@Test
	public void testPutDeletionWhenInTransaction()
	{
		// Start transaction
		l1Cache.transactionStarted(transactionID);

		// Entity has been deleted once, return empty
		l1Cache.putDeletion(create(entity1));
		Optional<Entity> actualEntity = l1Cache.get(repository, entityID1, entityType);
		assertEquals(actualEntity, empty());

		// Cleanup transaction
		l1Cache.doCleanupAfterCompletion(transactionID);
	}

	@Test
	public void testEvictAllWhenInTransaction()
	{
		// Start transaction
		l1Cache.transactionStarted(transactionID);

		// Evict entity, return null
		l1Cache.put(repository, entity1);
		l1Cache.evictAll(entityType);
		Optional<Entity> actualEntity = l1Cache.get(repository, entityID1, entityType);
		assertEquals(actualEntity, null);

		// Cleanup transaction
		l1Cache.doCleanupAfterCompletion(transactionID);
	}

	@Configuration
	@Import({ EntityHydration.class })
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
