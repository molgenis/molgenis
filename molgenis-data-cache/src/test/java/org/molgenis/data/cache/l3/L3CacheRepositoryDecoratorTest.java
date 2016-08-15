package org.molgenis.data.cache.l3;

import com.google.common.collect.Sets;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.MolgenisFieldTypes.AttributeType.INT;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = L3CacheRepositoryDecoratorTest.Config.class)
public class L3CacheRepositoryDecoratorTest extends AbstractMolgenisSpringTest
{
	private L3CacheRepositoryDecorator l3CacheRepositoryDecorator;
	private EntityMetaData entityMetaData;

	private Entity mockEntity1;
	private Entity mockEntity2;
	private Entity mockEntity3;

	private final String repository = "TestRepository";
	private static final String COUNTRY = "Country";
	private static final String ID = "ID";

	@Mock
	private L3Cache l3Cache;

	@Mock
	private Repository<Entity> decoratedRepository;

	@Mock
	private TransactionInformation transactionInformation;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	private EntityManager entityManager;

	@Captor
	private ArgumentCaptor<Stream<Object>> entityIdCaptor;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);

		entityMetaData = entityMetaDataFactory.create(repository);
		entityMetaData.addAttribute(attributeMetaDataFactory.create().setDataType(INT).setName(ID), ROLE_ID);
		entityMetaData.addAttribute(attributeMetaDataFactory.create().setName(COUNTRY));

		when(entityManager.create(entityMetaData)).thenReturn(new DynamicEntity(entityMetaData));

		mockEntity1 = entityManager.create(entityMetaData);
		mockEntity1.set(ID, 1);
		mockEntity1.set(COUNTRY, "NL");

		mockEntity2 = entityManager.create(entityMetaData);
		mockEntity2.set(ID, 2);
		mockEntity2.set(COUNTRY, "NL");

		mockEntity3 = entityManager.create(entityMetaData);
		mockEntity3.set(ID, 3);
		mockEntity3.set(COUNTRY, "GB");

		when(decoratedRepository.getCapabilities()).thenReturn(Sets.newHashSet(CACHEABLE));
		l3CacheRepositoryDecorator = new L3CacheRepositoryDecorator(decoratedRepository, l3Cache,
				transactionInformation);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		reset(l3Cache, transactionInformation, decoratedRepository);
		when(decoratedRepository.getEntityMetaData()).thenReturn(entityMetaData);
		when(decoratedRepository.getName()).thenReturn(entityMetaData.getName());
	}

	@Test
	public void testFindOne()
	{
		when(transactionInformation.isRepositoryCompletelyClean(entityMetaData.getName())).thenReturn(true);
		Query<Entity> query = new QueryImpl<>().eq(COUNTRY, "GB");
		query.pageSize(1);
		query.sort(new Sort());

		when(l3Cache.get(decoratedRepository, query)).thenReturn(singletonList(3));
		when(decoratedRepository.findOneById(3, query.getFetch())).thenReturn(mockEntity3);

		Entity actualEntity = l3CacheRepositoryDecorator.findOne(query);
		assertEquals(actualEntity, mockEntity3);
	}

	@Test
	public void testFindAll()
	{
		when(transactionInformation.isRepositoryCompletelyClean(entityMetaData.getName())).thenReturn(true);
		Query<Entity> query = new QueryImpl<>().eq(COUNTRY, "NL");
		query.pageSize(10);
		query.sort(new Sort());
		query.fetch(new Fetch());

		List<Object> ids = asList(1, 2);
		List<Entity> expectedEntities = newArrayList(mockEntity1, mockEntity2);

		when(l3Cache.get(decoratedRepository, query)).thenReturn(ids);
		when(decoratedRepository.findAll(entityIdCaptor.capture(), eq(query.getFetch())))
				.thenReturn(expectedEntities.stream());

		Stream<Entity> actualEntities = l3CacheRepositoryDecorator.findAll(query);

		assertEquals(actualEntities.collect(toList()), expectedEntities);
		assertEquals(entityIdCaptor.getValue().collect(toList()), ids);
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
