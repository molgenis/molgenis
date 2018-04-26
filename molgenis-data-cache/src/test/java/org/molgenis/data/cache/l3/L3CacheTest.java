package org.molgenis.data.cache.l3;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.data.transaction.TransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class L3CacheTest extends AbstractMolgenisSpringTest
{
	private L3Cache l3Cache;

	private EntityType entityType;

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
	private TransactionManager transactionManager;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attributeFactory;

	public L3CacheTest()
	{
		super(Strictness.WARN);
	}

	@BeforeClass
	public void beforeClass()
	{
		entityType = entityTypeFactory.create(repositoryName);
		entityType.addAttribute(attributeFactory.create().setDataType(INT).setName(ID), ROLE_ID);
		entityType.addAttribute(attributeFactory.create().setName(COUNTRY));

		entity1 = new DynamicEntity(entityType);
		entity1.set(ID, 1);
		entity1.set(COUNTRY, "NL");

		entity2 = new DynamicEntity(entityType);
		entity2.set(ID, 2);
		entity2.set(COUNTRY, "NL");

		entity3 = new DynamicEntity(entityType);
		entity3.set(ID, 3);
		entity3.set(COUNTRY, "GB");
	}

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeMethod()
	{
		reset(decoratedRepository);

		when(decoratedRepository.getCapabilities()).thenReturn(Sets.newHashSet(CACHEABLE));
		when(decoratedRepository.getName()).thenReturn(repositoryName);
		when(decoratedRepository.getEntityType()).thenReturn(entityType);

		l3Cache = new L3Cache(transactionManager, transactionInformation);
	}

	@Test
	public void testGet()
	{
		Fetch idAttributeFetch = new Fetch().field(entityType.getIdAttribute().getName());
		Query<Entity> fetchLessQuery = new QueryImpl<>().eq(COUNTRY, "NL").fetch(idAttributeFetch);

		when(decoratedRepository.findAll(fetchLessQuery)).thenReturn(Stream.of(entity1, entity2));

		Fetch fetch = mock(Fetch.class);
		Query<Entity> query = new QueryImpl<>().eq(COUNTRY, "NL").fetch(fetch);

		assertEquals(l3Cache.get(decoratedRepository, query), Arrays.asList(1, 2));
		assertEquals(l3Cache.get(decoratedRepository, query), Arrays.asList(1, 2));

		verify(decoratedRepository, times(1)).findAll(fetchLessQuery);
		verify(decoratedRepository, atLeast(0)).getName();
		verify(decoratedRepository, atLeast(0)).getEntityType();
		verifyNoMoreInteractions(decoratedRepository);
	}

	@Test
	public void testGetThrowsException()
	{
		Fetch idAttributeFetch = new Fetch().field(entityType.getIdAttribute().getName());
		Query<Entity> fetchLessQuery = new QueryImpl<>().eq(COUNTRY, "NL").fetch(idAttributeFetch);

		when(decoratedRepository.findAll(fetchLessQuery)).thenThrow(new MolgenisDataException("What table?"));

		Fetch fetch = mock(Fetch.class);
		Query<Entity> query = new QueryImpl<>().eq(COUNTRY, "NL").fetch(fetch);

		try
		{
			l3Cache.get(decoratedRepository, query);
			// check that exception is thrown by the get method
			fail("Get should throw exception");
		}
		catch (UncheckedExecutionException expected)
		{

		}
		// Check that exception isn't cached.
		try
		{
			l3Cache.get(decoratedRepository, query);
			fail("Get should throw exception");
		}
		catch (UncheckedExecutionException expected)
		{

		}
		verify(decoratedRepository, times(2)).findAll(fetchLessQuery);
	}

	@Test
	public void testAfterCommitTransactionDirtyRepository()
	{
		Fetch idAttributeFetch = new Fetch().field(entityType.getIdAttribute().getName());
		Query<Entity> fetchLessQuery = new QueryImpl<>().eq(COUNTRY, "NL").fetch(idAttributeFetch);

		when(decoratedRepository.findAll(fetchLessQuery)).thenReturn(Stream.of(entity1, entity2));

		Fetch fetch = mock(Fetch.class);
		Query<Entity> query = new QueryImpl<>().eq(COUNTRY, "NL").fetch(fetch);

		assertEquals(l3Cache.get(decoratedRepository, query), Arrays.asList(1, 2));
		assertEquals(l3Cache.get(decoratedRepository, query), Arrays.asList(1, 2));

		when(transactionInformation.getDirtyRepositories()).thenReturn(Collections.singleton(repositoryName));
		l3Cache.afterCommitTransaction("ABCDE");

		when(decoratedRepository.findAll(fetchLessQuery)).thenReturn(Stream.of(entity3, entity2));

		assertEquals(l3Cache.get(decoratedRepository, query), Arrays.asList(3, 2));
		assertEquals(l3Cache.get(decoratedRepository, query), Arrays.asList(3, 2));

		verify(decoratedRepository, times(2)).findAll(fetchLessQuery);
	}

	@Test
	public void testAfterCommitTransactionCleanRepository()
	{
		Fetch idAttributeFetch = new Fetch().field(entityType.getIdAttribute().getName());
		Query<Entity> fetchLessQuery = new QueryImpl<>().eq(COUNTRY, "NL").fetch(idAttributeFetch);

		when(decoratedRepository.findAll(fetchLessQuery)).thenReturn(Stream.of(entity1, entity2));

		Fetch fetch = mock(Fetch.class);
		Query<Entity> query = new QueryImpl<>().eq(COUNTRY, "NL").fetch(fetch);

		assertEquals(l3Cache.get(decoratedRepository, query), Arrays.asList(1, 2));
		assertEquals(l3Cache.get(decoratedRepository, query), Arrays.asList(1, 2));

		when(transactionInformation.getDirtyRepositories()).thenReturn(Collections.singleton("blah"));
		l3Cache.afterCommitTransaction("ABCDE");

		assertEquals(l3Cache.get(decoratedRepository, query), Arrays.asList(1, 2));
		assertEquals(l3Cache.get(decoratedRepository, query), Arrays.asList(1, 2));

		verify(decoratedRepository, times(1)).findAll(fetchLessQuery);
		verify(decoratedRepository, atLeast(0)).getName();
		verify(decoratedRepository, atLeast(0)).getEntityType();
		verifyNoMoreInteractions(decoratedRepository);
	}
}
