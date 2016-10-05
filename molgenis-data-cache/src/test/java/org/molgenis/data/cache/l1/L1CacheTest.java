package org.molgenis.data.cache.l1;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.EntityManager;
import org.molgenis.data.cache.utils.EntityHydration;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.util.EntityUtils;
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
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = L1CacheTest.Config.class)
public class L1CacheTest extends AbstractMolgenisSpringTest
{
	private L1Cache l1Cache;
	private EntityMetaData entityMetaData;
	private Entity entity1;
	private Entity entity2;

	private final String transactionID = "__TRANSACTION__";
	private final String repository = "TestRepository";
	private final String entityID1 = "1";
	private final String entityID2 = "2";

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private EntityHydration entityHydration;

	@Mock
	private MolgenisTransactionManager molgenisTransactionManager;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);

		entityMetaData = entityMetaDataFactory.create(repository);
		entityMetaData.addAttribute(attributeMetaDataFactory.create().setName("ID"), ROLE_ID);
		entityMetaData.addAttribute(attributeMetaDataFactory.create().setName("ATTRIBUTE_1"));

		Mockito.when(entityManager.create(entityMetaData, NO_POPULATE)).thenReturn(new DynamicEntity(entityMetaData));

		mockEntity = entityManager.create(entityMetaData, NO_POPULATE);
		mockEntity.set("ID", entityID);
		mockEntity.set("ATTRIBUTE_1", "test_value_1");
	}

	@BeforeMethod
	public void beforeMethod()
	{
		l1Cache = new L1Cache(molgenisTransactionManager, entityHydration);
	}

	@Test
	public void testWhenNotInTransaction()
	{
		// Without a transactionStarted() call the cache does not exist, return null
		l1Cache.put(repository, entity1);
		Optional<Entity> actualEntity = l1Cache.get(repository, entityID1, entityMetaData);
		assertEquals(actualEntity, null);
	}

	@Test
	public void testPutAndGetWhenInTransaction()
	{
		// Start transaction
		l1Cache.transactionStarted(transactionID);

		// Entity has not been added to cache, return null
		Optional<Entity> actualEntity = l1Cache.get(repository, entityID1, entityMetaData);
		assertEquals(actualEntity, null);

		// Entity has been added to cache, return entity
		l1Cache.put(repository, entity1);
		Entity result = l1Cache.get(repository, entityID1, entityMetaData).get();
		assertTrue(EntityUtils.equals(result, entity1));

		// Cleanup after transaction and expect the cache to be cleared, return null
		l1Cache.doCleanupAfterCompletion(transactionID);
		actualEntity = l1Cache.get(repository, entityID1, entityMetaData);
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
		Entity actualEntity = l1Cache.get(repository, entityID1, entityMetaData).get();
		assertTrue(EntityUtils.equals(actualEntity, entity1));
		actualEntity = l1Cache.get(repository, entityID2, entityMetaData).get();
		assertTrue(EntityUtils.equals(actualEntity, entity2));

		l1Cache.evict(Stream.of(EntityKey.create(entity1), EntityKey.create(entity2)));

		Optional<Entity> result = l1Cache.get(repository, entityID1, entityMetaData);
		assertEquals(result, null);
		result = l1Cache.get(repository, entityID2, entityMetaData);
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
		Entity actualEntity = l1Cache.get(repository, entityID1, entityMetaData).get();
		assertTrue(EntityUtils.equals(actualEntity, entity1));
		actualEntity = l1Cache.get(repository, entityID2, entityMetaData).get();
		assertTrue(EntityUtils.equals(actualEntity, entity2));

		l1Cache.evict(Stream.of(EntityKey.create(entity2)));

		actualEntity = l1Cache.get(repository, entityID1, entityMetaData).get();
		assertTrue(EntityUtils.equals(actualEntity, entity1));
		Optional<Entity> result = l1Cache.get(repository, entityID2, entityMetaData);
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
		Entity actualEntity = l1Cache.get(repository, entityID1, entityMetaData).get();
		assertTrue(EntityUtils.equals(actualEntity, entity1));
		actualEntity = l1Cache.get(repository, entityID2, entityMetaData).get();
		assertTrue(EntityUtils.equals(actualEntity, entity2));

		l1Cache.evictAll(entityMetaData.getName());

		Optional<Entity> result = l1Cache.get(repository, entityID1, entityMetaData);
		assertEquals(result, null);
		result = l1Cache.get(repository, entityID2, entityMetaData);
		assertEquals(result, null);
	}

	@Test
	public void testPutDeletionWhenInTransaction()
	{
		// Start transaction
		l1Cache.transactionStarted(transactionID);

		// Entity has been deleted once, return empty
		l1Cache.putDeletion(create(entity1));
		Optional<Entity> actualEntity = l1Cache.get(repository, entityID1, entityMetaData);
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
		l1Cache.evictAll(repository);
		Optional<Entity> actualEntity = l1Cache.get(repository, entityID1, entityMetaData);
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
