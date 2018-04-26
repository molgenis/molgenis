package org.molgenis.data.index;

import com.google.common.collect.Lists;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.Query;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionFactory;
import org.molgenis.data.index.meta.IndexActionGroup;
import org.molgenis.data.index.meta.IndexActionGroupFactory;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;
import static org.molgenis.data.index.meta.IndexActionGroupMetaData.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetaData.INDEX_ACTION;
import static org.molgenis.data.index.meta.IndexActionMetaData.IndexStatus.PENDING;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.testng.Assert.*;

public class IndexActionRegisterServiceTest extends AbstractMockitoTest
{
	private IndexActionRegisterServiceImpl indexActionRegisterServiceImpl;
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
		TransactionSynchronizationManager.bindResource(TransactionManager.TRANSACTION_ID_RESOURCE_NAME, "1");
		indexActionRegisterServiceImpl = new IndexActionRegisterServiceImpl(dataService, indexActionFactory,
				indexActionGroupFactory, new IndexingStrategy());
	}

	@AfterMethod
	public void afterMethod()
	{
		TransactionSynchronizationManager.unbindResource(TransactionManager.TRANSACTION_ID_RESOURCE_NAME);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRegisterCreateSingleEntityNoReferences()
	{
		when(indexActionGroupFactory.create("1")).thenReturn(indexActionGroup);
		when(indexActionGroup.setCount(1)).thenReturn(indexActionGroup);

		when(indexActionFactory.create()).thenReturn(indexAction);
		when(indexAction.setIndexActionGroup(indexActionGroup)).thenReturn(indexAction);
		when(indexAction.setEntityTypeId("entityTypeId")).thenReturn(indexAction);
		when(indexAction.setEntityId("123")).thenReturn(indexAction);
		when(indexAction.setActionOrder(0)).thenReturn(indexAction);
		when(indexAction.setIndexStatus(PENDING)).thenReturn(indexAction);
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId");

		indexActionRegisterServiceImpl.register(entityType, 123);

		verifyZeroInteractions(dataService);

		Query<Entity> refEntityQuery = mock(Query.class);
		when(refEntityQuery.count()).thenReturn(0L);
		when(refEntityQuery.in(AttributeMetadata.REF_ENTITY_TYPE, singleton("entityTypeId"))).thenReturn(
				refEntityQuery);
		doReturn(refEntityQuery).when(dataService).query(ATTRIBUTE_META_DATA);
		indexActionRegisterServiceImpl.storeIndexActions("1");

		verify(dataService).add(INDEX_ACTION_GROUP, indexActionGroup);
		verify(dataService).add(eq(INDEX_ACTION), indexActionStreamCaptor.capture());
		assertEquals(indexActionStreamCaptor.getValue().collect(Collectors.toList()), Lists.newArrayList(indexAction));
	}

	@Test
	public void testRegisterAndForget()
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId");
		indexActionRegisterServiceImpl.register(entityType, 123);

		verifyZeroInteractions(dataService);

		indexActionRegisterServiceImpl.forgetIndexActions("1");

		verifyZeroInteractions(dataService);

		indexActionRegisterServiceImpl.storeIndexActions("1");

		verifyZeroInteractions(dataService);
	}

	@Test
	public void testRegisterExcludedEntities()
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId");
		indexActionRegisterServiceImpl.addExcludedEntity("ABC");

		indexActionRegisterServiceImpl.register(entityType, 123);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void isEntityDirtyTrue()
	{
		String entityTypeId = "myEntityTypeId";
		int entityId = 123;
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		indexActionRegisterServiceImpl.register(entityType, entityId);
		EntityKey entityKey = EntityKey.create(entityTypeId, entityId);
		assertTrue(indexActionRegisterServiceImpl.isEntityDirty(entityKey));
	}

	@Test
	public void isEntityDirtyFalse()
	{
		String entityTypeId = "myEntityTypeId";
		String entityId = "id";
		String otherId = "otherID";
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		indexActionRegisterServiceImpl.register(entityType, entityId);
		EntityKey entityKey = EntityKey.create(entityTypeId, otherId);
		assertFalse(indexActionRegisterServiceImpl.isEntityDirty(entityKey));
	}
}
