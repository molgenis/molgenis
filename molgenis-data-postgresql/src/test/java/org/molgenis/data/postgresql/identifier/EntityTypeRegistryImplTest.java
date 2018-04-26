package org.molgenis.data.postgresql.identifier;

import com.google.common.collect.ImmutableMap;
import org.mockito.Mock;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.transaction.TransactionManager.TRANSACTION_ID_RESOURCE_NAME;
import static org.springframework.transaction.support.TransactionSynchronizationManager.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class EntityTypeRegistryImplTest extends AbstractMockitoTest
{
	@Mock
	private Attribute stringAttr;
	@Mock
	private Attribute xrefAttr;
	@Mock
	private Attribute mrefAttr;
	@Mock
	private Attribute attr;
	@Mock
	private EntityType entityType;
	@Mock
	private TransactionManager transactionManager;

	private EntityTypeRegistryImpl entityTypeRegistryImpl;
	private EntityTypeDescription entityTypeDescription;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityTypeRegistryImpl = new EntityTypeRegistryImpl(transactionManager);

		when(stringAttr.getDataType()).thenReturn(STRING);
		when(stringAttr.getName()).thenReturn("stringAttribute");

		when(xrefAttr.getDataType()).thenReturn(XREF);
		when(xrefAttr.getName()).thenReturn("xref_attribute_with_a_long_name");

		when(mrefAttr.getDataType()).thenReturn(MREF);
		when(mrefAttr.getName()).thenReturn("mref");

		when(entityType.getId()).thenReturn("org_molgenis_test_many_packages_deep");
		when(entityType.getAtomicAttributes()).thenReturn(asList(stringAttr, xrefAttr, mrefAttr));

		entityTypeDescription = EntityTypeDescription.create("org_molgenis_test_many_packages_deep",
				ImmutableMap.of("stringAttribute", AttributeDescription.create("stringAttribute"),
						"xref_attribute_with_a_long_name",
						AttributeDescription.create("xref_attribute_with_a_long_name")));

		entityTypeRegistryImpl.registerEntityType(entityType);
	}

	@AfterMethod
	public void afterMethod()
	{
		if (getResource(TRANSACTION_ID_RESOURCE_NAME) != null)
		{
			unbindResource(TRANSACTION_ID_RESOURCE_NAME);
		}
	}

	@Test
	public void testGetEntityTypeDescription()
	{
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"),
				entityTypeDescription);
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"),
				entityTypeDescription);
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_stringAttribute"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription(
				"org_molgenis_test_many#2409e1c6_xref_attribute_with_a_long_name"));
	}

	@Test
	public void testGetEntityTypeDescriptionWithinNewTransactionFallbackToCommitted()
	{
		bindResource(TRANSACTION_ID_RESOURCE_NAME, "1");
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"),
				entityTypeDescription);
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"),
				entityTypeDescription);
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_stringAttribute"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription(
				"org_molgenis_test_many#2409e1c6_xref_attribute_with_a_long_name"));
	}

	@Test
	public void testGetEntityTypeDescriptionWithinNewTransactionOverrideWithRegistered()
	{
		bindResource(TRANSACTION_ID_RESOURCE_NAME, "1");
		entityTypeRegistryImpl.unregisterEntityType(entityType);
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_stringAttribute"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription(
				"org_molgenis_test_many#2409e1c6_xref_attribute_with_a_long_name"));
	}

	@Test
	public void testAddMrefAttribute()
	{
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getName()).thenReturn("mref2");

		entityTypeRegistryImpl.addAttribute(entityType, attr);

		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"),
				entityTypeDescription);
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"),
				entityTypeDescription);
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref2"),
				entityTypeDescription);
	}

	@Test
	public void testAddIntAttribute()
	{
		when(attr.getDataType()).thenReturn(INT);
		when(attr.getName()).thenReturn("xint");

		entityTypeRegistryImpl.addAttribute(entityType, attr);

		entityTypeDescription = EntityTypeDescription.create("org_molgenis_test_many_packages_deep",
				ImmutableMap.of("stringAttribute", AttributeDescription.create("stringAttribute"),
						"xref_attribute_with_a_long_name",
						AttributeDescription.create("xref_attribute_with_a_long_name"), "xint",
						AttributeDescription.create("xint")));

		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"),
				entityTypeDescription);
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"),
				entityTypeDescription);
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_xint"));
	}

	@Test
	public void testUpdateMrefToIntAttribute()
	{
		when(attr.getDataType()).thenReturn(INT);
		when(attr.getName()).thenReturn("xint");

		entityTypeRegistryImpl.updateAttribute(entityType, mrefAttr, attr);

		entityTypeDescription = EntityTypeDescription.create("org_molgenis_test_many_packages_deep",
				ImmutableMap.of("stringAttribute", AttributeDescription.create("stringAttribute"),
						"xref_attribute_with_a_long_name",
						AttributeDescription.create("xref_attribute_with_a_long_name"), "xint",
						AttributeDescription.create("xint")));

		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"),
				entityTypeDescription);
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_xint"));
	}

	@Test
	public void testUpdateMrefAttributeName()
	{
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getName()).thenReturn("newName");

		entityTypeRegistryImpl.updateAttribute(entityType, mrefAttr, attr);

		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"),
				entityTypeDescription);
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_newName"),
				entityTypeDescription);
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"));
	}

	@Test
	public void testUpdateStringToMrefAttribute()
	{
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getName()).thenReturn("stringAttribute");

		entityTypeDescription = EntityTypeDescription.create("org_molgenis_test_many_packages_deep",
				ImmutableMap.of("xref_attribute_with_a_long_name",
						AttributeDescription.create("xref_attribute_with_a_long_name")));

		entityTypeRegistryImpl.updateAttribute(entityType, stringAttr, attr);

		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"),
				entityTypeDescription);
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_stringAttribute"),
				entityTypeDescription);
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"),
				entityTypeDescription);
	}

	@Test
	public void testDeleteMrefAttr()
	{
		entityTypeRegistryImpl.deleteAttribute(entityType, mrefAttr);

		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"),
				entityTypeDescription);
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"));
	}

	@Test
	public void testDeleteStringAttr()
	{
		entityTypeRegistryImpl.deleteAttribute(entityType, stringAttr);

		entityTypeDescription = EntityTypeDescription.create("org_molgenis_test_many_packages_deep",
				ImmutableMap.of("xref_attribute_with_a_long_name",
						AttributeDescription.create("xref_attribute_with_a_long_name")));

		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"),
				entityTypeDescription);
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"),
				entityTypeDescription);
	}

	@Test
	public void testUnregisterEntityType()
	{
		entityTypeRegistryImpl.unregisterEntityType(entityType);

		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription(
				"org_molgenis_test_many#2409e1c6_xref_attribute_with_a_long_name"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"));
	}

	@Test
	public void testRollbackTransaction()
	{
		bindResource(TRANSACTION_ID_RESOURCE_NAME, "1");

		entityTypeRegistryImpl.unregisterEntityType(entityType);

		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_stringAttribute"));
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription(
				"org_molgenis_test_many#2409e1c6_xref_attribute_with_a_long_name"));

		entityTypeRegistryImpl.rollbackTransaction("1");

		testGetEntityTypeDescription();
	}

	@Test
	public void testAfterCommitTransaction()
	{
		entityTypeRegistryImpl.afterCommitTransaction("1");
		entityTypeRegistryImpl.rollbackTransaction("1");
		testGetEntityTypeDescription();
	}

	@Test
	public void testDeleteMrefAttrInTransaction()
	{
		// commit metadata
		entityTypeRegistryImpl.afterCommitTransaction("1");

		// delete mref attr in second transaction
		bindResource(TRANSACTION_ID_RESOURCE_NAME, "2");
		entityTypeRegistryImpl.deleteAttribute(entityType, mrefAttr);
		unbindResource(TRANSACTION_ID_RESOURCE_NAME);

		// other checks still see old metadata
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"),
				entityTypeDescription);
		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"),
				entityTypeDescription, "Other threads should still see old metadata");

		// commit new transaction
		entityTypeRegistryImpl.afterCommitTransaction("2");

		assertEquals(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many_packages_deep#2409e1c6"),
				entityTypeDescription);
		assertNull(entityTypeRegistryImpl.getEntityTypeDescription("org_molgenis_test_many#2409e1c6_mref"),
				"After committing the junction table description should be removed for all threads");
	}

}