package org.molgenis.data.index;

import com.google.common.collect.Lists;
import org.mockito.*;
import org.molgenis.data.DataService;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionFactory;
import org.molgenis.data.index.meta.IndexActionGroup;
import org.molgenis.data.index.meta.IndexActionGroupFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.molgenis.data.index.meta.IndexActionGroupMetaData.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetaData.INDEX_ACTION;
import static org.molgenis.data.index.meta.IndexActionMetaData.IndexStatus.PENDING;
import static org.testng.Assert.assertEquals;

public class IndexActionRegisterServiceTest
{
	@InjectMocks
	private IndexActionRegisterService indexActionRegisterService = new IndexActionRegisterServiceImpl();
	@Mock
	private IndexActionGroupFactory indexActionGroupFactory;
	@Mock
	private IndexActionGroup indexActionGroup;
	@Mock
	private IndexActionFactory indexActionFactory;
	@Mock
	private IndexAction indexAction;
	@Mock
	private DataService dataService;
	@Captor
	private ArgumentCaptor<Stream<IndexAction>> indexActionStreamCaptor;

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
		when(indexActionGroupFactory.create("1")).thenReturn(indexActionGroup);
		when(indexActionGroup.setCount(1)).thenReturn(indexActionGroup);

		when(indexActionFactory.create()).thenReturn(indexAction);
		when(indexAction.setIndexActionGroup(indexActionGroup)).thenReturn(indexAction);
		when(indexAction.setEntityFullName("TestEntityName")).thenReturn(indexAction);
		when(indexAction.getEntityFullName()).thenReturn("TestEntityName");
		when(indexAction.setEntityId("123")).thenReturn(indexAction);
		when(indexAction.setActionOrder(0)).thenReturn(indexAction);
		when(indexAction.setIndexStatus(PENDING)).thenReturn(indexAction);
		EntityMetaData emd = mock(EntityMetaData.class);

		// To avoid addReferencingEntities(IndexAction indexAction)
		when(dataService.getEntityMetaData(indexAction.getEntityFullName())).thenReturn(emd);
		when(dataService.getEntityNames()).thenReturn(Stream.empty());

		indexActionRegisterService.register("TestEntityName", "123");

		verifyZeroInteractions(dataService);

		indexActionRegisterService.storeIndexActions("1");

		verify(dataService).add(INDEX_ACTION_GROUP, indexActionGroup);
		verify(dataService).add(eq(INDEX_ACTION), indexActionStreamCaptor.capture());
		assertEquals(indexActionStreamCaptor.getValue().collect(Collectors.toList()), Lists.newArrayList(indexAction));
	}

	@Test
	public void testRegisterAndForget()
	{
		when(indexActionGroupFactory.create("1")).thenReturn(indexActionGroup);
		when(indexActionGroup.setCount(1)).thenReturn(indexActionGroup);

		when(indexActionFactory.create()).thenReturn(indexAction);
		when(indexAction.setIndexActionGroup(indexActionGroup)).thenReturn(indexAction);
		when(indexAction.setEntityFullName("TestEntityName")).thenReturn(indexAction);
		when(indexAction.getEntityFullName()).thenReturn("TestEntityName");
		when(indexAction.setEntityId("123")).thenReturn(indexAction);
		when(indexAction.setActionOrder(0)).thenReturn(indexAction);
		when(indexAction.setIndexStatus(PENDING)).thenReturn(indexAction);

		indexActionRegisterService.register("TestEntityName", "123");

		verifyZeroInteractions(dataService);

		indexActionRegisterService.forgetIndexActions("1");

		verifyZeroInteractions(dataService);

		indexActionRegisterService.storeIndexActions("1");

		verifyZeroInteractions(dataService);
	}

	@Test
	public void testRegisterExcludedEntities()
	{
		when(indexActionGroupFactory.create("1")).thenReturn(indexActionGroup);
		when(indexActionGroup.setCount(1)).thenReturn(indexActionGroup);
		when(indexActionFactory.create()).thenReturn(indexAction);
		when(indexAction.setIndexActionGroup(indexActionGroup)).thenReturn(indexAction);
		when(indexAction.setEntityFullName("ABC")).thenReturn(indexAction);
		when(indexAction.getEntityFullName()).thenReturn("ABC");
		when(indexAction.setEntityId("123")).thenReturn(indexAction);
		when(indexAction.setActionOrder(0)).thenReturn(indexAction);
		when(indexAction.setIndexStatus(PENDING)).thenReturn(indexAction);

		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getName()).thenReturn("ABC");
		indexActionRegisterService.addExcludedEntity("ABC");

		indexActionRegisterService.register("ABC", "123");
		verifyNoMoreInteractions(dataService);
	}
}
