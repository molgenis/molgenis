package org.molgenis.data.postgresql.identifier;

import com.google.common.collect.ImmutableMap;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;
import static org.springframework.transaction.support.TransactionSynchronizationManager.bindResource;
import static org.springframework.transaction.support.TransactionSynchronizationManager.unbindResource;
import static org.testng.Assert.assertEquals;

public class EntityTypeRegistryImplTest
{
	private EntityTypeRegistryImpl entityTypeRegistryImpl;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		MolgenisTransactionManager molgenisTransactionManager = mock(MolgenisTransactionManager.class);
		entityTypeRegistryImpl = new EntityTypeRegistryImpl(molgenisTransactionManager);
		bindResource(TRANSACTION_ID_RESOURCE_NAME, "1");
	}

	@AfterMethod
	public void afterMethod()
	{
		unbindResource(TRANSACTION_ID_RESOURCE_NAME);
	}

	@Test
	public void testGetEntityTypeDescription() throws Exception
	{
		String entityTypeName = "entityTypeName";
		EntityType entityType = mock(EntityType.class);
		when(entityType.getAllAttributes()).thenReturn(emptyList());
		when(entityType.getName()).thenReturn(entityTypeName);
		when(entityType.getSimpleName()).thenReturn("entityTypeSimpleName");
		entityTypeRegistryImpl.registerEntityType(entityType);
		String tableName = "entityTypeSimpleName#2b9f9eaf";
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription(tableName),
				EntityTypeDescription.create(entityTypeName, ImmutableMap.of()));
	}

	@Test
	public void testGetEntityTypeDescriptionJunctionTable() throws Exception
	{
		String entityTypeName = "entityTypeName";
		EntityType entityType = mock(EntityType.class);
		when(entityType.getAllAttributes()).thenReturn(emptyList());
		when(entityType.getName()).thenReturn(entityTypeName);
		when(entityType.getSimpleName()).thenReturn("entityTypeSimpleName");
		entityTypeRegistryImpl.registerEntityType(entityType);
		String tableName = "entityTypeSimpleName#2b9f9eaf_mrefattr";
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription(tableName),
				EntityTypeDescription.create(entityTypeName, ImmutableMap.of()));
	}
}