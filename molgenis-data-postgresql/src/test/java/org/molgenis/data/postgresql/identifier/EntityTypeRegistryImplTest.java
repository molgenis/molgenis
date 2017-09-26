package org.molgenis.data.postgresql.identifier;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.molgenis.data.transaction.TransactionManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.molgenis.data.transaction.TransactionManager.TRANSACTION_ID_RESOURCE_NAME;
import static org.springframework.transaction.support.TransactionSynchronizationManager.bindResource;
import static org.springframework.transaction.support.TransactionSynchronizationManager.unbindResource;
import static org.testng.Assert.*;

public class EntityTypeRegistryImplTest
{
	private EntityTypeRegistryImpl entityTypeRegistryImpl;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		TransactionManager transactionManager = mock(TransactionManager.class);
		entityTypeRegistryImpl = new EntityTypeRegistryImpl(transactionManager);
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
		String entityTypeId = "entityTypeId";
		entityTypeRegistryImpl.registerEntityType(entityTypeId, emptyList());
		String tableName = "entityTypeId#c34894ba";
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription(tableName),
				EntityTypeDescription.create(entityTypeId, ImmutableMap.of()));
	}

	@Test
	public void testUnregisterEntityType()
	{
		String entityTypeId = "org_molgenis_test_many_packages_deep";
		List<Identifiable> referenceTypeAttributes = Lists.newArrayList(
				Identifiable.create("xref_attribute_with_a_long_name", "0"), Identifiable.create("mref", "1"));

		entityTypeRegistryImpl.registerEntityType(entityTypeId, referenceTypeAttributes);

		assertNotNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"));
		assertNotNull(entityTypeRegistryImpl.getEntityTypeDescription(
				"org_molgenis_test_many#2409e1c6_xref_attribute_with_a_long_name"));
		assertNotNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_stringAttribute"));

		entityTypeRegistryImpl.unregisterEntityType(entityTypeId, referenceTypeAttributes);

		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription(
				"org_molgenis_test_many#2409e1c6_xref_attribute_with_a_long_name"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"));
	}
}