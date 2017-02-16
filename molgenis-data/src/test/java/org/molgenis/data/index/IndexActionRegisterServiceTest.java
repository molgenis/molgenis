package org.molgenis.data.index;

import com.google.common.collect.Lists;
import org.mockito.*;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityKey;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionFactory;
import org.molgenis.data.index.meta.IndexActionGroup;
import org.molgenis.data.index.meta.IndexActionGroupFactory;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
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
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.AttributeMetadata.REF_ENTITY_TYPE;
import static org.testng.Assert.*;

public class IndexActionRegisterServiceTest
{
	@InjectMocks
	private IndexActionRegisterServiceImpl indexActionRegisterServiceImpl = new IndexActionRegisterServiceImpl();
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
		when(indexAction.setEntityTypeName("entityTypeName")).thenReturn(indexAction);
		when(indexAction.getEntityTypeName()).thenReturn("entityTypeName");
		when(indexAction.setEntityTypeId("entityTypeId")).thenReturn(indexAction);
		when(indexAction.getEntityTypeId()).thenReturn("entityTypeId");
		when(indexAction.setEntityFullName("TestEntityName")).thenReturn(indexAction);
		when(indexAction.getEntityFullName()).thenReturn("TestEntityName");
		when(indexAction.setEntityId("123")).thenReturn(indexAction);
		when(indexAction.getEntityTypeId()).thenReturn("123");
		when(indexAction.setActionOrder(0)).thenReturn(indexAction);
		when(indexAction.setIndexStatus(PENDING)).thenReturn(indexAction);
		EntityType entityType = mock(EntityType.class);
		when(entityType.getName()).thenReturn("entityTypeName");
		when(entityType.getId()).thenReturn("entityTypeId");
		when(entityType.getFullyQualifiedName()).thenReturn("TestEntityName");

		when(dataService.getEntityType(indexAction.getEntityFullName())).thenReturn(entityType);
		@SuppressWarnings("unchecked")
		Query<Attribute> query = mock(Query.class);
		when(query.fetch(any(Fetch.class))).thenReturn(query);
		when(query.eq(REF_ENTITY_TYPE, entityType)).thenReturn(query);
		when(query.findAll()).thenReturn(Stream.empty());
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(query);

		indexActionRegisterServiceImpl.register(entityType, "123");

		verifyZeroInteractions(dataService);

		indexActionRegisterServiceImpl.storeIndexActions("1");

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
		when(indexAction.setEntityTypeName("entityTypeName")).thenReturn(indexAction);
		when(indexAction.getEntityTypeName()).thenReturn("entityTypeName");
		when(indexAction.setEntityTypeId("entityTypeId")).thenReturn(indexAction);
		when(indexAction.getEntityTypeId()).thenReturn("entityTypeId");
		when(indexAction.getEntityFullName()).thenReturn("");
		when(indexAction.setEntityFullName("TestEntityName")).thenReturn(indexAction);
		when(indexAction.getEntityFullName()).thenReturn("");
		when(indexAction.setEntityId("123")).thenReturn(indexAction);
		when(indexAction.setActionOrder(0)).thenReturn(indexAction);
		when(indexAction.setIndexStatus(PENDING)).thenReturn(indexAction);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getName()).thenReturn("entityTypeName");
		when(entityType.getId()).thenReturn("entityTypeId");
		when(entityType.getFullyQualifiedName()).thenReturn("TestEntityName");
		indexActionRegisterServiceImpl.register(entityType, "123");

		verifyZeroInteractions(dataService);

		indexActionRegisterServiceImpl.forgetIndexActions("1");

		verifyZeroInteractions(dataService);

		indexActionRegisterServiceImpl.storeIndexActions("1");

		verifyZeroInteractions(dataService);
	}

	@Test
	public void testRegisterExcludedEntities()
	{
		when(indexActionGroupFactory.create("1")).thenReturn(indexActionGroup);
		when(indexActionGroup.setCount(1)).thenReturn(indexActionGroup);
		when(indexActionFactory.create()).thenReturn(indexAction);
		when(indexAction.setIndexActionGroup(indexActionGroup)).thenReturn(indexAction);
		when(indexAction.setEntityTypeName("entityTypeName")).thenReturn(indexAction);
		when(indexAction.getEntityTypeName()).thenReturn("entityTypeName");
		when(indexAction.setEntityTypeId("entityTypeId")).thenReturn(indexAction);
		when(indexAction.getEntityTypeId()).thenReturn("entityTypeId");
		when(indexAction.setEntityFullName("ABC")).thenReturn(indexAction);
		when(indexAction.getEntityFullName()).thenReturn("ABC");
		when(indexAction.setEntityId("123")).thenReturn(indexAction);
		when(indexAction.getEntityId()).thenReturn("123");
		when(indexAction.setActionOrder(0)).thenReturn(indexAction);
		when(indexAction.setIndexStatus(PENDING)).thenReturn(indexAction);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getName()).thenReturn("entityTypeName");
		when(entityType.getId()).thenReturn("entityTypeId");
		when(entityType.getFullyQualifiedName()).thenReturn("ABC");
		indexActionRegisterServiceImpl.addExcludedEntity("ABC");

		indexActionRegisterServiceImpl.register(entityType, "123");
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void isEntityDirtyTrue()
	{
		String entityTypeId = "myEntityTypeId";
		String entityTypeName = "myEntityTypeName";
		String entityName = "org_test_Test";
		String entityIdString = "123";
		Integer entityIdInteger = Integer.valueOf("123");

		when(indexActionGroupFactory.create("1")).thenReturn(indexActionGroup);
		when(indexActionGroup.setCount(1)).thenReturn(indexActionGroup);
		when(indexActionFactory.create()).thenReturn(indexAction);
		when(indexAction.setIndexActionGroup(indexActionGroup)).thenReturn(indexAction);
		when(indexAction.setEntityTypeName(entityTypeName)).thenReturn(indexAction);
		when(indexAction.getEntityTypeName()).thenReturn(entityTypeName);
		when(indexAction.setEntityTypeId(entityTypeId)).thenReturn(indexAction);
		when(indexAction.getEntityTypeId()).thenReturn(entityTypeId);
		when(indexAction.setEntityFullName(entityName)).thenReturn(indexAction);
		when(indexAction.getEntityFullName()).thenReturn(entityName);
		when(indexAction.setEntityId(entityIdString)).thenReturn(indexAction);
		when(indexAction.getEntityId()).thenReturn(entityIdString);
		when(indexAction.setActionOrder(0)).thenReturn(indexAction);
		when(indexAction.setIndexStatus(PENDING)).thenReturn(indexAction);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getName()).thenReturn(entityTypeName);
		when(entityType.getId()).thenReturn(entityTypeId);
		when(entityType.getFullyQualifiedName()).thenReturn(entityName);
		indexActionRegisterServiceImpl.register(entityType, entityIdString);
		EntityKey entityKey = EntityKey.create(entityName, entityIdInteger);
		assertTrue(indexActionRegisterServiceImpl.isEntityDirty(entityKey));
	}

	@Test
	public void isEntityDirtyFalse()
	{
		String entityTypeId = "myEntityTypeId";
		String entityTypeName = "myEntityTypeName";
		String entityName = "org_test_Test";
		String entityId1 = "123";
		String entityId2 = "false";

		when(indexActionGroupFactory.create("1")).thenReturn(indexActionGroup);
		when(indexActionGroup.setCount(1)).thenReturn(indexActionGroup);
		when(indexActionFactory.create()).thenReturn(indexAction);
		when(indexAction.setIndexActionGroup(indexActionGroup)).thenReturn(indexAction);
		when(indexAction.setEntityTypeName(entityTypeName)).thenReturn(indexAction);
		when(indexAction.getEntityTypeName()).thenReturn(entityTypeName);
		when(indexAction.setEntityTypeId(entityTypeId)).thenReturn(indexAction);
		when(indexAction.getEntityTypeId()).thenReturn(entityTypeName);
		when(indexAction.setEntityFullName(entityName)).thenReturn(indexAction);
		when(indexAction.getEntityFullName()).thenReturn(entityName);
		when(indexAction.setEntityId(entityId1)).thenReturn(indexAction);
		when(indexAction.getEntityId()).thenReturn(entityId1);
		when(indexAction.setActionOrder(0)).thenReturn(indexAction);
		when(indexAction.setIndexStatus(PENDING)).thenReturn(indexAction);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getName()).thenReturn(entityTypeName);
		when(entityType.getId()).thenReturn(entityTypeId);
		when(entityType.getFullyQualifiedName()).thenReturn(entityName);
		indexActionRegisterServiceImpl.register(entityType, entityId1);
		EntityKey entityKey = EntityKey.create(entityName, entityId2);
		assertFalse(indexActionRegisterServiceImpl.isEntityDirty(entityKey));
	}
}
