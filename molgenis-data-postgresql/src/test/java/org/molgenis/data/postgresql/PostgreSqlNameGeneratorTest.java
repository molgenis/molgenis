package org.molgenis.data.postgresql;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.postgresql.PostgreSqlNameGenerator.JUNCTION_TABLE_ORDER_ATTR_NAME;
import static org.testng.Assert.assertEquals;

public class PostgreSqlNameGeneratorTest
{
	@DataProvider(name = "getTableNameEntityTypeProvider")
	public static Iterator<Object[]> getTableNameEntityTypeProvider()
	{
		return newArrayList(new Object[] { "entityTypeId", "\"entityTypeId#c34894ba\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3",
						"\"thisisa_Very_very_very_very_very_very_very_long_s1mpl3#d9b1efe8\"" }).iterator();
	}

	@Test(dataProvider = "getTableNameEntityTypeProvider")
	public void testGetTableNameEntityType(String entityTypeId, String expectedTableName)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		assertEquals(PostgreSqlNameGenerator.getTableName(entityType), expectedTableName);
	}

	@DataProvider(name = "getTableNameEntityTypeBooleanProvider")
	public static Iterator<Object[]> getTableNameEntityTypeBooleanProvider()
	{
		return newArrayList(new Object[] { "entityTypeId", true, "\"entityTypeId#c34894ba\"" },
				new Object[] { "entityTypeId", false, "entityTypeId#c34894ba" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3", true,
						"\"thisisa_Very_very_very_very_very_very_very_long_s1mpl3#d9b1efe8\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3", false,
						"thisisa_Very_very_very_very_very_very_very_long_s1mpl3#d9b1efe8" }).iterator();
	}

	@Test(dataProvider = "getTableNameEntityTypeBooleanProvider")
	public void testGetTableNameEntityTypeBoolean(String entityTypeId, boolean quoteIdentifiers,
			String expectedTableName)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		assertEquals(PostgreSqlNameGenerator.getTableName(entityType, quoteIdentifiers), expectedTableName);
	}

	@DataProvider(name = "getJunctionTableNameProvider")
	public static Iterator<Object[]> getJunctionTableNameProvider()
	{
		return newArrayList(new Object[] { "entity", "attr", true, "\"entity#6844280e_attr\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3", "attr", true,
						"\"thisisa_Very_very_very#d9b1efe8_attr\"" },
				new Object[] { "entity", "this$is-a_Very_very_very_very_very_very_very_long_attr_nam3", true,
						"\"entity#6844280e_thisisa_Very_very_very#69363cb7\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3",
						"this$is-a_Very_very_very_very_very_very_very_long_attr_nam3", true,
						"\"thisisa_Very_very_very#d9b1efe8_thisisa_Very_very_very#69363cb7\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3",
						"this$is-a_Very_very_very_very_very_very_very_long_attr_nam3", false,
						"thisisa_Very_very_very#d9b1efe8_thisisa_Very_very_very#69363cb7" }).iterator();
	}

	@Test(dataProvider = "getJunctionTableNameProvider")
	public void testGetJunctionTableName(String entityTypeId, String attrName, boolean quotedIdentifiers,
			String expectedJunctionTableName)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		Attribute attr = mock(Attribute.class);
		when(attr.getIdentifier()).thenReturn("9876543210-9876543210-9876543210");
		when(attr.getName()).thenReturn(attrName);
		assertEquals(PostgreSqlNameGenerator.getJunctionTableName(entityType, attr, quotedIdentifiers),
				expectedJunctionTableName);
	}

	@DataProvider(name = "getJunctionTableIndexNameProvider")
	public static Iterator<Object[]> getJunctionTableIndexNameProvider()
	{
		return newArrayList(new Object[] { "entity", "attr", "idxAttr", "\"entity#6844280e_attr_idxAttr_idx\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3", "attr", "idxAttr",
						"\"thisisa_Ve#d9b1efe8_attr_idxAttr_idx\"" },
				new Object[] { "entity", "this$is-a_Very_very_very_very_very_very_very_long_attr_nam3", "idxAttr",
						"\"entity#6844280e_thisisa_Ve#69363cb7_idxAttr_idx\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3",
						"this$is-a_Very_very_very_very_very_very_very_long_attr_nam3", "idxAttr",
						"\"thisisa_Ve#d9b1efe8_thisisa_Ve#69363cb7_idxAttr_idx\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3",
						"this$is-a_Very_very_very_very_very_very_very_long_attr_nam3",
						"this$is-a_Very_very_very_very_very_very_very_long_idxAttr_nam3",
						"\"thisisa_Ve#d9b1efe8_thisisa_Ve#69363cb7_thisisa_Ve#65070eab_idx\"" }).iterator();
	}

	@Test(dataProvider = "getJunctionTableIndexNameProvider")
	public void testGetJunctionTableIndexName(String entityTypeId, String attrName, String indexAttrName,
			String expectedJunctionTableName)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		Attribute attr = mock(Attribute.class);
		when(attr.getIdentifier()).thenReturn("9876543210-9876543210-9876543210");
		when(attr.getName()).thenReturn(attrName);
		Attribute idxAttr = mock(Attribute.class);
		when(idxAttr.getIdentifier()).thenReturn("9876543210-0123456789-9876543210");
		when(idxAttr.getName()).thenReturn(indexAttrName);
		assertEquals(PostgreSqlNameGenerator.getJunctionTableIndexName(entityType, attr, idxAttr),
				expectedJunctionTableName);
	}

	@DataProvider(name = "getColumnNameProvider")
	public static Iterator<Object[]> getColumnNameProvider()
	{
		return newArrayList(new Object[] { "attrName", "\"attrName\"" },
				new Object[] { "attr-name", "\"attrname#69363cb7\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_attr_nam3",
						"\"thisisa_Very_very_very_very_very_very_very_long_attr_n#69363cb7\"" }).iterator();
	}

	@Test(dataProvider = "getColumnNameProvider")
	public void testGetColumnName(String attrName, String expectedColumnName)
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getIdentifier()).thenReturn("9876543210-9876543210-9876543210");
		when(attr.getName()).thenReturn(attrName);
		assertEquals(PostgreSqlNameGenerator.getColumnName(attr), expectedColumnName);
	}

	@DataProvider(name = "getColumnNameBooleanProvider")
	public static Iterator<Object[]> getColumnNameBooleanProvider()
	{
		return newArrayList(new Object[] { "attrName", false, "attrName" },
				new Object[] { "attr-name", false, "attrname#69363cb7" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_attr_nam3", false,
						"thisisa_Very_very_very_very_very_very_very_long_attr_n#69363cb7" },
				new Object[] { "attrName", true, "\"attrName\"" },
				new Object[] { "attr-name", true, "\"attrname#69363cb7\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_attr_nam3", true,
						"\"thisisa_Very_very_very_very_very_very_very_long_attr_n#69363cb7\"" }).iterator();
	}

	@Test(dataProvider = "getColumnNameBooleanProvider")
	public void testGetColumnName(String attrName, boolean quoteIdentifiers, String expectedColumnName)
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getIdentifier()).thenReturn("9876543210-9876543210-9876543210");
		when(attr.getName()).thenReturn(attrName);
		assertEquals(PostgreSqlNameGenerator.getColumnName(attr, quoteIdentifiers), expectedColumnName);
	}

	@Test
	public void testGetJunctionTableOrderColumnName()
	{
		assertEquals(PostgreSqlNameGenerator.getJunctionTableOrderColumnName(),
				'"' + JUNCTION_TABLE_ORDER_ATTR_NAME + '"');
	}

	@DataProvider(name = "getFilterColumnNameProvider")
	public static Iterator<Object[]> getFilterColumnNameProvider()
	{
		return newArrayList(new Object[] { "attrName", 0, "\"attrName_filter0\"" },
				new Object[] { "attr-name", 1, "\"attrname#69363cb7_filter1\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_attr_nam3", 0,
						"\"thisisa_Very_very_very_very_very_very_very_lon#69363cb7_filter0\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_attr_nam3", 123,
						"\"thisisa_Very_very_very_very_very_very_very_l#69363cb7_filter123\"" }).iterator();
	}

	@Test(dataProvider = "getFilterColumnNameProvider")
	public void testGetFilterColumnName(String attrName, int filterIndex, String expectedColumnName)
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getIdentifier()).thenReturn("9876543210-9876543210-9876543210");
		when(attr.getName()).thenReturn(attrName);
		assertEquals(PostgreSqlNameGenerator.getFilterColumnName(attr, filterIndex), expectedColumnName);
	}

	@DataProvider(name = "getPrimaryKeyNameProvider")
	public static Iterator<Object[]> getPrimaryKeyNameProvider()
	{
		return newArrayList(new Object[] { "entity", "attr", "\"entity#6844280e_attr_pkey\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3", "attr",
						"\"thisisa_Very_very_v#d9b1efe8_attr_pkey\"" },
				new Object[] { "entity", "this$is-a_Very_very_very_very_very_very_very_long_attr_nam3",
						"\"entity#6844280e_thisisa_Very_very_v#69363cb7_pkey\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3",
						"this$is-a_Very_very_very_very_very_very_very_long_attr_nam3",
						"\"thisisa_Very_very_v#d9b1efe8_thisisa_Very_very_v#69363cb7_pkey\"" }).iterator();
	}

	@Test(dataProvider = "getPrimaryKeyNameProvider")
	public void testGetPrimaryKeyName(String entityTypeId, String attrName, String expectedPrimaryKeyName)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		Attribute attr = mock(Attribute.class);
		when(attr.getIdentifier()).thenReturn("9876543210-9876543210-9876543210");
		when(attr.getName()).thenReturn(attrName);
		assertEquals(PostgreSqlNameGenerator.getPrimaryKeyName(entityType, attr), expectedPrimaryKeyName);
	}

	@DataProvider(name = "getForeignKeyNameProvider")
	public static Iterator<Object[]> getForeignKeyNameProvider()
	{
		return newArrayList(new Object[] { "entity", "attr", "\"entity#6844280e_attr_fkey\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3", "attr",
						"\"thisisa_Very_very_v#d9b1efe8_attr_fkey\"" },
				new Object[] { "entity", "this$is-a_Very_very_very_very_very_very_very_long_attr_nam3",
						"\"entity#6844280e_thisisa_Very_very_v#69363cb7_fkey\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3",
						"this$is-a_Very_very_very_very_very_very_very_long_attr_nam3",
						"\"thisisa_Very_very_v#d9b1efe8_thisisa_Very_very_v#69363cb7_fkey\"" }).iterator();
	}

	@Test(dataProvider = "getForeignKeyNameProvider")
	public void testGetForeignKeyName(String entityTypeId, String attrName, String expectedForeignKeyName)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		Attribute attr = mock(Attribute.class);
		when(attr.getIdentifier()).thenReturn("9876543210-9876543210-9876543210");
		when(attr.getName()).thenReturn(attrName);
		assertEquals(PostgreSqlNameGenerator.getForeignKeyName(entityType, attr), expectedForeignKeyName);
	}

	@DataProvider(name = "getUniqueKeyNameProvider")
	public static Iterator<Object[]> getUniqueKeyNameProvider()
	{
		return newArrayList(new Object[] { "entity", "attr", "\"entity#6844280e_attr_key\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3", "attr",
						"\"thisisa_Very_very_ve#d9b1efe8_attr_key\"" },
				new Object[] { "entity", "this$is-a_Very_very_very_very_very_very_very_long_attr_nam3",
						"\"entity#6844280e_thisisa_Very_very_ve#69363cb7_key\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3",
						"this$is-a_Very_very_very_very_very_very_very_long_attr_nam3",
						"\"thisisa_Very_very_ve#d9b1efe8_thisisa_Very_very_ve#69363cb7_key\"" }).iterator();
	}

	@Test(dataProvider = "getUniqueKeyNameProvider")
	public void testGetUniqueKeyName(String entityTypeId, String attrName, String expectedUniqueKeyName)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		Attribute attr = mock(Attribute.class);
		when(attr.getIdentifier()).thenReturn("9876543210-9876543210-9876543210");
		when(attr.getName()).thenReturn(attrName);
		assertEquals(PostgreSqlNameGenerator.getUniqueKeyName(entityType, attr), expectedUniqueKeyName);
	}

	@DataProvider(name = "getCheckConstraintNameProvider")
	public static Iterator<Object[]> getCheckConstraintNameProvider()
	{
		return newArrayList(new Object[] { "entity", "attr", "\"entity#6844280e_attr_chk\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3", "attr",
						"\"thisisa_Very_very_ve#d9b1efe8_attr_chk\"" },
				new Object[] { "entity", "this$is-a_Very_very_very_very_very_very_very_long_attr_nam3",
						"\"entity#6844280e_thisisa_Very_very_ve#69363cb7_chk\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3",
						"this$is-a_Very_very_very_very_very_very_very_long_attr_nam3",
						"\"thisisa_Very_very_ve#d9b1efe8_thisisa_Very_very_ve#69363cb7_chk\"" }).iterator();
	}

	@Test(dataProvider = "getCheckConstraintNameProvider")
	public void testGetCheckConstraintName(String entityTypeId, String attrName, String expectedCheckConstraintName)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		Attribute attr = mock(Attribute.class);
		when(attr.getIdentifier()).thenReturn("9876543210-9876543210-9876543210");
		when(attr.getName()).thenReturn(attrName);
		assertEquals(PostgreSqlNameGenerator.getCheckConstraintName(entityType, attr), expectedCheckConstraintName);
	}

	@DataProvider(name = "getFunctionValidateUpdateNameProvider")
	public static Iterator<Object[]> getFunctionValidateUpdateNameProvider()
	{
		return newArrayList(new Object[] { "entityTypeId", "\"validate_update_entityTypeId#c34894ba\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3",
						"\"validate_update_thisisa_Very_very_very_very_very_very_#d9b1efe8\"" }).iterator();
	}

	@Test(dataProvider = "getFunctionValidateUpdateNameProvider")
	public void testGetFunctionValidateUpdateName(String entityTypeId, String expectedFunctionValidateUpdateName)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		assertEquals(PostgreSqlNameGenerator.getFunctionValidateUpdateName(entityType),
				expectedFunctionValidateUpdateName);
	}

	@DataProvider(name = "getUpdateTriggerNameProvider")
	public static Iterator<Object[]> getUpdateTriggerNameProvider()
	{
		return newArrayList(new Object[] { "entityTypeId", "\"update_trigger_entityTypeId#c34894ba\"" },
				new Object[] { "this$is-a_Very_very_very_very_very_very_very_long_s1mpl3_nam3",
						"\"update_trigger_thisisa_Very_very_very_very_very_very_v#d9b1efe8\"" }).iterator();
	}

	@Test(dataProvider = "getUpdateTriggerNameProvider")
	public void testGetUpdateTriggerName(String entityTypeId, String expectedUpdateTriggerName)
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		assertEquals(PostgreSqlNameGenerator.getUpdateTriggerName(entityType), expectedUpdateTriggerName);
	}
}