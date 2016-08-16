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

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.MolgenisFieldTypes.AttributeType.INT;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;

@ContextConfiguration(classes = L3CacheTest.Config.class)
public class L3CacheTest extends AbstractMolgenisSpringTest
{
	private L3Cache l3Cache;

	private EntityMetaData entityMetaData;

	private Entity mockEntity1;
	private Entity mockEntity2;
	private Entity mockEntity3;

	private final String repository = "TestRepository";
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
		l3Cache = new L3Cache(molgenisTransactionManager, transactionInformation);
	}

	@BeforeMethod
	public void beforeMethod()
	{

	}

	@Test
	public void getTest()
	{
		// repository.findAll(new QueryImpl<>(query).fetch(idAttributeFetch)).map(Entity::getIdValue)

		Fetch idAttributeFetch = new Fetch().field(entityMetaData.getIdAttribute().getName());
		Query<Entity> query = new QueryImpl<>().eq(COUNTRY, "NL");
		query.fetch(idAttributeFetch);

		when(decoratedRepository.findAll(query)).thenReturn(Stream.of(mockEntity1, mockEntity2));

		List<Object> actualIdentifiers = l3Cache.get(decoratedRepository, query);
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
