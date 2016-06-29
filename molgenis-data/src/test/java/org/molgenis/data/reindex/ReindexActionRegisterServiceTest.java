package org.molgenis.data.reindex;

import autovalue.shaded.com.google.common.common.collect.Lists;
import org.mockito.*;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.reindex.meta.ReindexAction;
import org.molgenis.data.reindex.meta.ReindexActionFactory;
import org.molgenis.data.reindex.meta.ReindexActionGroup;
import org.molgenis.data.reindex.meta.ReindexActionGroupFactory;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.molgenis.data.reindex.meta.ReindexActionGroupMetaData.REINDEX_ACTION_GROUP;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.REINDEX_ACTION;
import static org.molgenis.data.reindex.meta.ReindexActionMetaData.ReindexStatus.PENDING;
import static org.testng.Assert.assertEquals;

public class ReindexActionRegisterServiceTest
{
	@InjectMocks
	private ReindexActionRegisterService reindexActionRegisterService = new ReindexActionRegisterService();
	@Mock
	private ReindexActionGroupFactory reindexActionGroupFactory;
	@Mock
	private ReindexActionGroup reindexActionGroup;
	@Mock
	private ReindexActionFactory reindexActionFactory;
	@Mock
	private ReindexAction reindexAction;
	@Mock
	private DataService dataService;
	@Captor
	private ArgumentCaptor<Stream<ReindexAction>> reindexActionStreamCaptor;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		TransactionSynchronizationManager.bindResource(MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME, "1");
	}

	@AfterMethod
	public void afterMethod()
	{
		TransactionSynchronizationManager.unbindResource(MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME);
	}

	@Test
	public void testRegisterCreateSingleEntity()
	{
		when(reindexActionGroupFactory.create("1")).thenReturn(reindexActionGroup);
		when(reindexActionGroup.setCount(1)).thenReturn(reindexActionGroup);

		when(reindexActionFactory.create()).thenReturn(reindexAction);
		when(reindexAction.setReindexActionGroup(reindexActionGroup)).thenReturn(reindexAction);
		when(reindexAction.setEntityFullName("TestEntityName")).thenReturn(reindexAction);
		when(reindexAction.setCudType(CudType.CREATE)).thenReturn(reindexAction);
		when(reindexAction.setDataType(DataType.DATA)).thenReturn(reindexAction);
		when(reindexAction.setEntityId("123")).thenReturn(reindexAction);
		when(reindexAction.setActionOrder(0)).thenReturn(reindexAction);
		when(reindexAction.setReindexStatus(PENDING)).thenReturn(reindexAction);

		reindexActionRegisterService.register("TestEntityName", CudType.CREATE, DataType.DATA, "123");

		verifyZeroInteractions(dataService);

		reindexActionRegisterService.storeReindexActions("1");

		verify(dataService).add(REINDEX_ACTION_GROUP, reindexActionGroup);
		verify(dataService).add(eq(REINDEX_ACTION), reindexActionStreamCaptor.capture());
		assertEquals(reindexActionStreamCaptor.getValue().collect(Collectors.toList()),
				Lists.newArrayList(reindexAction));
	}

	@Test
	public void testRegisterAndForget()
	{
		when(reindexActionGroupFactory.create("1")).thenReturn(reindexActionGroup);
		when(reindexActionGroup.setCount(1)).thenReturn(reindexActionGroup);

		when(reindexActionFactory.create()).thenReturn(reindexAction);
		when(reindexAction.setReindexActionGroup(reindexActionGroup)).thenReturn(reindexAction);
		when(reindexAction.setEntityFullName("TestEntityName")).thenReturn(reindexAction);
		when(reindexAction.setCudType(CudType.CREATE)).thenReturn(reindexAction);
		when(reindexAction.setDataType(DataType.DATA)).thenReturn(reindexAction);
		when(reindexAction.setEntityId("123")).thenReturn(reindexAction);
		when(reindexAction.setActionOrder(0)).thenReturn(reindexAction);
		when(reindexAction.setReindexStatus(PENDING)).thenReturn(reindexAction);

		reindexActionRegisterService.register("TestEntityName", CudType.CREATE, DataType.DATA, "123");

		verifyZeroInteractions(dataService);

		reindexActionRegisterService.forgetReindexActions("1");

		verifyZeroInteractions(dataService);

		reindexActionRegisterService.storeReindexActions("1");

		verifyZeroInteractions(dataService);
	}

	@Test
	public void testRegisterExcludedEntities()
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getName()).thenReturn("ABC");
		reindexActionRegisterService.addExcludedEntity("ABC");

		reindexActionRegisterService.register("ABC", CudType.CREATE, DataType.DATA, "123");
		verifyNoMoreInteractions(dataService);
	}
}
