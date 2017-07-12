package org.molgenis.data.postgresql.identifier;

import com.google.common.collect.ImmutableMap;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.transaction.TransactionManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
		EntityType entityType = mock(EntityType.class);
		when(entityType.getAllAttributes()).thenReturn(emptyList());
		when(entityType.getId()).thenReturn(entityTypeId);
		entityTypeRegistryImpl.registerEntityType(entityType);
		String tableName = "entityTypeId#c34894ba";
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription(tableName),
				EntityTypeDescription.create(entityTypeId, ImmutableMap.of()));
	}

	@Test
	public void testUnregisterEntityType()
	{
		EntityType entityType = prepareMockEntityTypeForRegistering();

		entityTypeRegistryImpl.registerEntityType(entityType);

		assertNotNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"));
		assertNotNull(entityTypeRegistryImpl.getEntityTypeDescription(
				"org_molgenis_test_many#2409e1c6_xref_attribute_with_a_long_name"));
		assertNotNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_stringAttribute"));

		entityTypeRegistryImpl.unregisterEntityType(entityType);

		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription(
				"org_molgenis_test_many#2409e1c6_xref_attribute_with_a_long_name"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"));
	}

	private EntityType prepareMockEntityTypeForRegistering()
	{
		Attribute stringAttr = mock(Attribute.class);
		when(stringAttr.getDataType()).thenReturn(AttributeType.STRING);
		when(stringAttr.getName()).thenReturn("stringAttribute");

		Attribute xrefAttr = mock(Attribute.class);
		when(xrefAttr.getDataType()).thenReturn(AttributeType.XREF);
		when(xrefAttr.getName()).thenReturn("xref_attribute_with_a_long_name");
		when(xrefAttr.getIdentifier()).thenReturn("0");

		Attribute mrefAttr = mock(Attribute.class);
		when(mrefAttr.getDataType()).thenReturn(AttributeType.MREF);
		when(mrefAttr.getName()).thenReturn("mref");
		when(xrefAttr.getIdentifier()).thenReturn("1");

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("org_molgenis_test_many_packages_deep");
		when(entityType.getAllAttributes()).thenReturn(asList(stringAttr, xrefAttr, mrefAttr));

		return entityType;
	}
}