package org.molgenis.data.transaction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class TransactionLogRepositoryDecoratorTest
{
	private Repository decoratedRepo;
	private EntityMetaData entityMeta;
	private TransactionLogRepositoryDecorator transactionLogRepositoryDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		entityMeta = mock(EntityMetaData.class);
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);
		TransactionLogService transactionLogService = mock(TransactionLogService.class);
		transactionLogRepositoryDecorator = new TransactionLogRepositoryDecorator(decoratedRepo, transactionLogService);
	}

	@Test
	public void findAllIterableFetch()
	{
		Iterable<Object> ids = Arrays.<Object> asList(Integer.valueOf(0), Integer.valueOf(1));
		Fetch fetch = new Fetch();
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Iterable<Entity> entities = Arrays.asList(entity0, entity1);
		when(decoratedRepo.findAll(ids, fetch)).thenReturn(entities);
		assertEquals(Arrays.asList(entity0, entity1),
				Lists.newArrayList(transactionLogRepositoryDecorator.findAll(ids, fetch)));
		verify(decoratedRepo, times(1)).findAll(ids, fetch);
	}

	@Test
	public void findOne()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		when(decoratedRepo.findOne(id, fetch)).thenReturn(entity);
		assertEquals(entity, transactionLogRepositoryDecorator.findOne(id, fetch));
		verify(decoratedRepo, times(1)).findOne(id, fetch);
	}
}
