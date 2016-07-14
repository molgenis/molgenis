package org.molgenis.data.cache.l2;

import com.google.common.util.concurrent.UncheckedExecutionException;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.data.cache.utils.EntityHydration;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.test.data.EntityTestHarness;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = L2CacheTest.Config.class)
public class L2CacheTest extends AbstractMolgenisSpringTest
{
	private L2Cache l2Cache;

	@Autowired
	private EntityHydration entityHydration;

	@Autowired
	private EntityTestHarness entityTestHarness;

	@Autowired
	private EntityManager entityManager;

	@Mock
	private Repository<Entity> repository;
	@Mock
	private TransactionInformation transactionInformation;
	private List<Entity> testEntities;
	private EntityMetaData emd;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
		EntityMetaData refEntityMetaData = entityTestHarness.createDynamicRefEntityMetaData();
		emd = entityTestHarness.createDynamicTestEntityMetaData();
		List<Entity> refEntities = entityTestHarness.createTestRefEntities(refEntityMetaData, 2);
		testEntities = entityTestHarness.createTestEntities(emd, 4, refEntities).collect(toList());

		when(entityManager.create(emd)).thenReturn(new DynamicEntity(emd));
		when(entityManager.getReference(any(EntityMetaData.class), eq("0"))).thenReturn(refEntities.get(0));
		when(entityManager.getReference(any(EntityMetaData.class), eq("1"))).thenReturn(refEntities.get(1));
		when(entityManager.getReferences(any(EntityMetaData.class), eq(newArrayList("0"))))
				.thenReturn(newArrayList(refEntities.get(0)));
		when(entityManager.getReferences(any(EntityMetaData.class), eq(newArrayList("1"))))
				.thenReturn(newArrayList(refEntities.get(1)));
	}

	@BeforeMethod
	public void beforeMethod()
	{
		reset(repository,transactionInformation);
		when(repository.getEntityMetaData()).thenReturn(emd);
		when(repository.getName()).thenReturn(emd.getName());

		l2Cache = new L2Cache(entityHydration, transactionInformation);
	}

	@Test
	public void testGetStringIdCachesLoadedData()
	{
		Entity entity2 = testEntities.get(2);
		when(repository.findOneById("2")).thenReturn(entity2);

		Entity result = l2Cache.get(repository, "2");
		assertTrue(EntityUtils.equals(result, entity2));

		result = l2Cache.get(repository, "2");
		assertTrue(EntityUtils.equals(result, entity2));

		verify(repository, atMost(1)).findOneById("2");
	}

	@Test(expectedExceptions = UncheckedExecutionException.class)
	public void testGetStringIdLoaderThrowsException()
	{
		when(repository.findOneById("2"))
				.thenThrow(new MolgenisDataException("Table is missing for entity TestEntity"));
		l2Cache.get(repository, "2");
	}

	@Test(expectedExceptions = UncheckedExecutionException.class)
	public void testGetBatchIdLoaderThrowsException()
	{
		when(repository.findAll(any(Stream.class)))
				.thenThrow(new MolgenisDataException("Table is missing for entity TestEntity"));
		l2Cache.getBatch(repository, newArrayList("1", "2"));
	}

	@Test
	public void testFindAll()
	{
		when(repository.findAll(any(Stream.class))).thenReturn(testEntities.stream());
		List<Entity> result = l2Cache.getBatch(repository, newArrayList("1", "2", "3", "4"));
		Map<Object, Entity> retrievedEntities = result.stream().collect(toMap(Entity::getIdValue, e -> e));
		assertEquals(retrievedEntities.size(), 4);
		assertTrue(EntityUtils.equals(retrievedEntities.get("1"), testEntities.get(1)));
	}

	@Configuration
	@Import({ EntityTestHarness.class, EntityHydration.class })
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
