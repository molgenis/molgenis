package org.molgenis.data.cache.l3;

import com.google.common.collect.Sets;
import org.mockito.Mock;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.MolgenisFieldTypes.AttributeType.INT;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = L3CacheTest.Config.class)
public class L3CacheTest extends AbstractMolgenisSpringTest
{
	private L3Cache l3Cache;

	private EntityMetaData entityMetaData;

	private Entity entity1;
	private Entity entity2;
	private Entity entity3;

	private final String repositoryName = "TestRepository";
	private static final String COUNTRY = "Country";
	private static final String ID = "ID";

	@Mock
	private Repository<Entity> decoratedRepository;

	@Mock
	private TransactionInformation transactionInformation;

	@Mock
	private MolgenisTransactionManager molgenisTransactionManager;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	private EntityManager entityManager;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);

		entityMetaData = entityMetaDataFactory.create(repositoryName);
		entityMetaData.addAttribute(attributeMetaDataFactory.create().setDataType(INT).setName(ID), ROLE_ID);
		entityMetaData.addAttribute(attributeMetaDataFactory.create().setName(COUNTRY));

		//		when(entityManager.create(entityMetaData)).thenReturn(new DynamicEntity(entityMetaData));

		entity1 = new DynamicEntity(entityMetaData);
		entity1.set(ID, 1);
		entity1.set(COUNTRY, "NL");

		entity2 = new DynamicEntity(entityMetaData);
		entity2.set(ID, 2);
		entity2.set(COUNTRY, "NL");

		entity3 = new DynamicEntity(entityMetaData);
		entity3.set(ID, 3);
		entity3.set(COUNTRY, "GB");

		when(decoratedRepository.getCapabilities()).thenReturn(Sets.newHashSet(CACHEABLE));
		when(decoratedRepository.getName()).thenReturn(repositoryName);
		when(decoratedRepository.getEntityMetaData()).thenReturn(entityMetaData);
		l3Cache = new L3Cache(molgenisTransactionManager, transactionInformation);
	}

	@BeforeMethod
	public void beforeMethod()
	{

	}

	@Test
	public void testGetIgnoresFetch()
	{
		Fetch idAttributeFetch = new Fetch().field(entityMetaData.getIdAttribute().getName());
		Query<Entity> fetchLessQuery = new QueryImpl<>().eq(COUNTRY, "NL").fetch(idAttributeFetch);

		when(decoratedRepository.findAll(fetchLessQuery)).thenReturn(Stream.of(entity1, entity2));

		Fetch fetch = mock(Fetch.class);
		Query<Entity> query = new QueryImpl<>().eq(COUNTRY, "NL").fetch(fetch);

		assertEquals(l3Cache.get(decoratedRepository, query), Arrays.asList(1, 2));
		assertEquals(l3Cache.get(decoratedRepository, query), Arrays.asList(1, 2));

		verify(decoratedRepository, atMost(1)).findAll(fetchLessQuery);
		verify(decoratedRepository, atLeast(0)).getName();
		verify(decoratedRepository, atLeast(0)).getEntityMetaData();
		verifyNoMoreInteractions(decoratedRepository);
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
