package org.molgenis.data.cache.l3;

import com.google.common.collect.Sets;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.transaction.TransactionInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.EntityManager.CreationMode.NO_POPULATE;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = L3CacheRepositoryDecoratorTest.Config.class)
public class L3CacheRepositoryDecoratorTest extends AbstractMolgenisSpringTest
{
	private L3CacheRepositoryDecorator l3CacheRepositoryDecorator;
	private EntityType entityType;

	private Entity entity1;
	private Entity entity2;
	private Entity entity3;

	private final String repositoryName = "TestRepository";
	private static final String COUNTRY = "Country";
	private static final String ID = "ID";

	@Mock
	private L3Cache l3Cache;

	@Mock
	private Repository<Entity> decoratedRepository;

	@Mock
	private TransactionInformation transactionInformation;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attributeFactory;

	@Autowired
	private EntityManager entityManager;

	@Captor
	private ArgumentCaptor<Stream<Object>> entityIdCaptor;

	private Query<Entity> query;

	@Mock
	private Fetch fetch;

	@BeforeMethod
	public void beforeMethod()
	{
		entityType = entityTypeFactory.create(repositoryName);
		entityType.addAttribute(attributeFactory.create().setDataType(INT).setName(ID), ROLE_ID);
		entityType.addAttribute(attributeFactory.create().setName(COUNTRY));

		when(entityManager.create(entityType, NO_POPULATE)).thenReturn(new DynamicEntity(entityType));

		entity1 = entityManager.create(entityType, NO_POPULATE);
		entity1.set(ID, 1);
		entity1.set(COUNTRY, "NL");

		entity2 = entityManager.create(entityType, NO_POPULATE);
		entity2.set(ID, 2);
		entity2.set(COUNTRY, "NL");

		entity3 = entityManager.create(entityType, NO_POPULATE);
		entity3.set(ID, 3);
		entity3.set(COUNTRY, "GB");

		when(decoratedRepository.getCapabilities()).thenReturn(Sets.newHashSet(CACHEABLE));
		l3CacheRepositoryDecorator = new L3CacheRepositoryDecorator(decoratedRepository, l3Cache,
				transactionInformation);
		verify(decoratedRepository).getCapabilities();
		query = new QueryImpl<>().eq(COUNTRY, "GB");
		query.pageSize(10);
		query.sort(new Sort().on(COUNTRY));
		query.setFetch(fetch);
		when(decoratedRepository.getEntityType()).thenReturn(entityType);
		when(decoratedRepository.getName()).thenReturn(repositoryName);
	}

	@Test
	public void testFindOneRepositoryClean()
	{
		when(transactionInformation.isRepositoryCompletelyClean(entityType)).thenReturn(true);
		Query<Entity> queryWithPageSizeOne = new QueryImpl<>(query).pageSize(1);
		when(l3Cache.get(decoratedRepository, queryWithPageSizeOne)).thenReturn(singletonList(3));
		when(decoratedRepository.findOneById(3, fetch)).thenReturn(entity3);

		assertEquals(l3CacheRepositoryDecorator.findOne(queryWithPageSizeOne), entity3);
		verify(decoratedRepository, times(1)).findOneById(3, fetch);
		verify(decoratedRepository, atLeast(0)).getEntityType();
		verifyNoMoreInteractions(decoratedRepository);
	}

	@Test
	public void testFindOneRepositoryDirty()
	{
		when(transactionInformation.isRepositoryCompletelyClean(entityType)).thenReturn(false);
		when(decoratedRepository.findOne(query)).thenReturn(entity3);

		assertEquals(l3CacheRepositoryDecorator.findOne(query), entity3);
		verifyNoMoreInteractions(l3Cache);
	}

	@Test
	public void testFindAllRepositoryClean()
	{
		when(transactionInformation.isRepositoryCompletelyClean(entityType)).thenReturn(true);

		List<Object> ids = asList(1, 2);
		List<Entity> expectedEntities = newArrayList(entity1, entity2);

		when(l3Cache.get(decoratedRepository, query)).thenReturn(ids);
		when(decoratedRepository.findAll(entityIdCaptor.capture(), eq(query.getFetch()))).thenReturn(
				expectedEntities.stream());

		Stream<Entity> actualEntities = l3CacheRepositoryDecorator.findAll(query);

		assertEquals(actualEntities.collect(toList()), expectedEntities);
		assertEquals(entityIdCaptor.getValue().collect(toList()), ids);
	}

	@Test
	public void testFindAllVeryLargePageSize()
	{
		when(transactionInformation.isRepositoryCompletelyClean(entityType)).thenReturn(true);
		Query<Entity> largeQuery = new QueryImpl<>(query).setPageSize(10000);

		List<Entity> expectedEntities = newArrayList(entity1, entity2);

		when(decoratedRepository.findAll(largeQuery)).thenReturn(expectedEntities.stream());

		List<Entity> actualEntities = l3CacheRepositoryDecorator.findAll(largeQuery).collect(toList());

		assertEquals(actualEntities, expectedEntities);
		verifyNoMoreInteractions(l3Cache);
	}

	@Test
	public void testFindAllZeroPageSize()
	{
		when(transactionInformation.isRepositoryCompletelyClean(entityType)).thenReturn(true);
		Query<Entity> largeQuery = new QueryImpl<>(query).setPageSize(0);

		List<Entity> expectedEntities = newArrayList(entity1, entity2);

		when(decoratedRepository.findAll(largeQuery)).thenReturn(expectedEntities.stream());

		List<Entity> actualEntities = l3CacheRepositoryDecorator.findAll(largeQuery).collect(toList());

		assertEquals(actualEntities, expectedEntities);
		verifyNoMoreInteractions(l3Cache);
	}

	@Test
	public void testFindAllRepositoryDirty()
	{
		when(transactionInformation.isRepositoryCompletelyClean(entityType)).thenReturn(false);
		Query<Entity> query = new QueryImpl<>().eq(COUNTRY, "NL");
		query.pageSize(10);
		query.sort(new Sort());
		query.fetch(new Fetch());

		List<Entity> expectedEntities = newArrayList(entity1, entity2);

		when(decoratedRepository.findAll(query)).thenReturn(expectedEntities.stream());

		List<Entity> actualEntities = l3CacheRepositoryDecorator.findAll(query).collect(toList());

		assertEquals(actualEntities, expectedEntities);
		verifyNoMoreInteractions(l3Cache);
	}

	@Configuration
	public static class Config
	{
		@Bean
		public EntityManager entityManager()
		{
			return mock(EntityManager.class);
		}
	}
}
