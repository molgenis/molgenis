package org.molgenis.model.elements;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Vector;

import org.molgenis.fieldtypes.CharField;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.model.MolgenisModelException;
import org.testng.annotations.Test;

public class FieldTest
{
	@Test
	public void testGetFormatString()
	{
		FieldType fieldType = when(mock(FieldType.class).getFormatString()).thenReturn("%d%s").getMock();
		Field field = new Field(mock(Entity.class), "field", fieldType);
		assertEquals(field.getFormatString(), "%d%s");
	}

	@Test
	public void testGetFormatString_xrefField() throws MolgenisModelException
	{
		Entity xrefEntity = mock(Entity.class);
		Field xrefField = when(mock(Field.class).getFormatString()).thenReturn("%d%s").getMock();
		when(xrefEntity.getAllField("xrefField")).thenReturn(xrefField);

		Model model = when(mock(Model.class).getEntity("xrefEntity")).thenReturn(xrefEntity).getMock();
		Entity entity = when(mock(Entity.class).getModel()).thenReturn(model).getMock();

		Field field = new Field(entity, "field", new XrefField());
		field.setXRefEntity("xrefEntity");
		field.setXrefField("xrefField");
		assertEquals(field.getFormatString(), "%d%s");
	}

	@Test
	public void testGetLabel_hasLabel()
	{
		Field field = new Field(mock(Entity.class), "field", mock(FieldType.class));
		field.setLabel("label");
		field.setName(null);
		assertEquals(field.getLabel(), "label");
	}

	@Test
	public void testGetLabel_hasName()
	{
		Field field = new Field(mock(Entity.class), "field", mock(FieldType.class));
		field.setLabel(null);
		field.setName("name");
		assertEquals(field.getLabel(), "name");
	}

	@Test
	public void testGetLabel_hasLabelhasName()
	{
		Field field = new Field(mock(Entity.class), "field", mock(FieldType.class));
		field.setLabel("label");
		field.setName("name");
		assertEquals(field.getLabel(), "label");
	}

	@Test
	public void testIsLocal()
	{
		assertFalse(new Field(mock(Entity.class), "field", mock(FieldType.class)).isLocal());
		assertFalse(new Field(mock(Entity.class), "field", new XrefField()).isLocal());
		assertTrue(new Field(mock(Entity.class), "field", new MrefField()).isLocal());
	}

	@Test
	public void testIsCyclic_noXrefField() throws MolgenisModelException
	{
		assertFalse(new Field(mock(Entity.class), "field", mock(FieldType.class)).isCyclic());
	}

	@Test
	public void testIsCyclic_selfXref() throws MolgenisModelException
	{
		Field field = new Field(mock(Entity.class), "field", new XrefField());
		field.setXRefEntity("field");
		assertTrue(field.isCyclic());
	}

	@Test
	public void testIsCyclic_cyclicXref() throws MolgenisModelException
	{
		Vector<Field> fields = new Vector<Field>();
		Field field1 = new Field(mock(Entity.class), "field1", mock(FieldType.class));
		fields.add(field1);
		Field field2 = new Field(mock(Entity.class), "field2", new XrefField());
		field2.setXRefEntity("cyclic_field");
		fields.add(field2);
		Field field3 = new Field(mock(Entity.class), "field3", new MrefField());
		fields.add(field3);

		Entity xrefEntity = mock(Entity.class);
		when(xrefEntity.getAllFields()).thenReturn(fields);

		Entity rootEntity = mock(Entity.class);
		when(rootEntity.get("xrefEntity")).thenReturn(xrefEntity);

		Entity entity = mock(Entity.class);
		when(entity.getRoot()).thenReturn(rootEntity);

		Field field = new Field(entity, "cyclic_field", new XrefField());
		field.setXRefEntity("xrefEntity");
		assertTrue(field.isCyclic());
	}

	@Test
	public void testIsCyclic_nonCyclicXref() throws MolgenisModelException
	{
		Vector<Field> fields = new Vector<Field>();
		Field field1 = new Field(mock(Entity.class), "field1", mock(FieldType.class));
		fields.add(field1);
		Field field2 = new Field(mock(Entity.class), "field2", new XrefField());
		field2.setXRefEntity("field4");
		fields.add(field2);
		Field field3 = new Field(mock(Entity.class), "field3", new MrefField());
		field3.setXRefEntity("field5");
		fields.add(field3);

		Entity xrefEntity = mock(Entity.class);
		when(xrefEntity.getAllFields()).thenReturn(fields);

		Entity rootEntity = mock(Entity.class);
		when(rootEntity.get("xrefEntity")).thenReturn(xrefEntity);

		Entity entity = mock(Entity.class);
		when(entity.getRoot()).thenReturn(rootEntity);

		Field field = new Field(entity, "field", new XrefField());
		field.setXRefEntity("xrefEntity");
		assertFalse(field.isCyclic());
	}

	@Test
	public void testIsXRef()
	{
		assertFalse(new Field(mock(Entity.class), "field", mock(FieldType.class)).isXRef());
		assertTrue(new Field(mock(Entity.class), "field", new XrefField()).isXRef());
		assertTrue(new Field(mock(Entity.class), "field", new MrefField()).isXRef());
	}

	@Test
	public void testIsMRef()
	{
		assertFalse(new Field(mock(Entity.class), "field", mock(FieldType.class)).isMRef());
		assertFalse(new Field(mock(Entity.class), "field", new XrefField()).isMRef());
		assertTrue(new Field(mock(Entity.class), "field", new MrefField()).isMRef());
	}

	@Test
	public void testGetSetEnumOptions() throws MolgenisModelException
	{
		Vector<String> enumOptions = new Vector<String>();
		enumOptions.add("option1");
		enumOptions.add("option2");

		Field field = new Field(mock(Entity.class), "field", new EnumField());
		field.setEnumOptions(enumOptions);
		assertEquals(field.getEnumOptions(), enumOptions);
	}

	@Test(expectedExceptions = MolgenisModelException.class)
	public void testGetSetEnumOptions_noEnumField() throws MolgenisModelException
	{
		Vector<String> enumOptions = new Vector<String>();
		enumOptions.add("option1");
		enumOptions.add("option2");

		Field field = new Field(mock(Entity.class), "field", mock(FieldType.class));
		field.setEnumOptions(enumOptions);
		field.getEnumOptions();
	}

	@Test
	public void testGetSetVarCharLength() throws MolgenisModelException
	{
		{
			Field field = new Field(mock(Entity.class), "field", new CharField());
			field.setVarCharLength(10);
			assertEquals(field.getVarCharLength(), 10);
		}
		{
			Field field = new Field(mock(Entity.class), "field", new StringField());
			field.setVarCharLength(10);
			assertEquals(field.getVarCharLength(), 10);
		}
	}

	@Test(expectedExceptions = MolgenisModelException.class)
	public void testGetSetVarCharLength_noCharOrStringField() throws MolgenisModelException
	{
		Field field = new Field(mock(Entity.class), "field", mock(FieldType.class));
		field.setVarCharLength(10);
		field.getVarCharLength();
	}

	@Test
	public void testGetSetXrefEntity() throws MolgenisModelException
	{
		Entity xrefEntity = mock(Entity.class);
		Model model = when(mock(Model.class).getEntity("xrefEntity")).thenReturn(xrefEntity).getMock();
		Entity entity = when(mock(Entity.class).getModel()).thenReturn(model).getMock();

		Field field = new Field(entity, "field", new XrefField());
		field.setXRefEntity("xrefEntity");
		assertEquals(field.getXrefEntity(), xrefEntity);
	}

	@Test(expectedExceptions = MolgenisModelException.class)
	public void testGetSetXrefEntity_noXrefEntity() throws MolgenisModelException
	{
		Entity xrefEntity = mock(Entity.class);

		Model model = mock(Model.class);
		Entity entity = when(mock(Entity.class).getModel()).thenReturn(model).getMock();

		Field field = new Field(entity, "field", new XrefField());
		field.setXRefEntity("xrefEntity");
		assertEquals(field.getXrefEntity(), xrefEntity);
	}

	@Test(expectedExceptions = MolgenisModelException.class)
	public void testGetSetXRefEntity_noXrefField() throws MolgenisModelException
	{
		Field field = new Field(mock(Entity.class), "field", mock(FieldType.class));
		field.getXrefEntity();
	}

	@Test
	public void testGetSetXRefVariables() throws MolgenisModelException
	{
		Field field = new Field(mock(Entity.class), "field", new XrefField());
		field.setXRefVariables("a", "b", Arrays.asList("c", "d"));
		assertEquals(field.getXrefEntityName(), "a");
		assertEquals(field.getXrefFieldName(), "b");
		assertEquals(field.getXrefLabelNames(), Arrays.asList("c", "d"));
	}

	@Test(expectedExceptions = MolgenisModelException.class)
	public void testGetSetXRefVariables_noXrefField() throws MolgenisModelException
	{
		Field field = new Field(mock(Entity.class), "field", mock(FieldType.class));
		field.setXRefVariables("a", "b", Arrays.asList("c", "d"));
		field.getXrefEntityName();
	}

	@Test
	public void testGetSetXrefEntityName() throws MolgenisModelException
	{
		{
			Field field = new Field(mock(Entity.class), "field", new XrefField());
			field.setXRefEntity("entity");
			assertEquals(field.getXrefEntityName(), "entity");
		}
		{
			Field field = new Field(mock(Entity.class), "field", new MrefField());
			field.setXRefEntity("entity");
			assertEquals(field.getXrefEntityName(), "entity");
		}
	}

	@Test(expectedExceptions = MolgenisModelException.class)
	public void testGetSetXrefEntityName_noXrefField() throws MolgenisModelException
	{
		Field field = new Field(mock(Entity.class), "field", mock(FieldType.class));
		field.setXRefEntity("entity");
		field.getXrefEntityName();
	}

	@Test
	public void testGetSetXrefField() throws MolgenisModelException
	{
		Entity xrefEntity = mock(Entity.class);
		Model model = when(mock(Model.class).getEntity("xrefEntity")).thenReturn(xrefEntity).getMock();
		Entity entity = when(mock(Entity.class).getModel()).thenReturn(model).getMock();

		Field field = new Field(entity, "field", new XrefField());
		field.setXRefEntity("xrefEntity");
		assertEquals(field.getXrefEntity(), xrefEntity);

	}

	@Test(expectedExceptions = MolgenisModelException.class)
	public void testGetSetXrefField_unknownXrefEntity() throws MolgenisModelException
	{
		Entity xrefEntity = mock(Entity.class);
		Model model = when(mock(Model.class).getEntity("xrefEntity")).thenReturn(xrefEntity).getMock();
		Entity entity = when(mock(Entity.class).getModel()).thenReturn(model).getMock();

		Field field = new Field(entity, "field", new XrefField());
		field.setXRefEntity("unknownXrefEntity");
		assertEquals(field.getXrefEntity(), xrefEntity);
	}

	@Test(expectedExceptions = MolgenisModelException.class)
	public void testGetSetXrefField_noXrefField() throws MolgenisModelException
	{
		Field field = new Field(mock(Entity.class), "field", mock(FieldType.class));
		field.setXrefField("xrefField");
		field.getXrefField();
	}

	@Test
	public void testGetSetXrefLabelNames() throws MolgenisModelException
	{
		Entity xrefEntity = mock(Entity.class);
		Model model = when(mock(Model.class).getEntity("xrefEntity")).thenReturn(xrefEntity).getMock();
		Entity entity = when(mock(Entity.class).getModel()).thenReturn(model).getMock();

		Field field = new Field(entity, "field", new XrefField());
		field.setXRefEntity("xrefEntity");
		field.setXrefLabelNames(Arrays.asList("a", "b", "c"));
		assertEquals(field.getXrefLabelNames(), Arrays.asList("a", "b", "c"));
	}

	@Test
	public void testGetSetXrefLabelNames_removeXrefEntityName() throws MolgenisModelException
	{
		Entity xrefEntity = mock(Entity.class);
		Model model = when(mock(Model.class).getEntity("xrefEntity")).thenReturn(xrefEntity).getMock();
		Entity entity = when(mock(Entity.class).getModel()).thenReturn(model).getMock();

		Field field = new Field(entity, "field", new XrefField());
		field.setXRefEntity("xrefEntity");
		field.setXrefLabelNames(Arrays.asList("xrefEntity.1", "xrefEntity.2", "xrefEntity.3"));
		assertEquals(field.getXrefLabelNames(), Arrays.asList("1", "2", "3"));
	}

	@Test(expectedExceptions = MolgenisModelException.class)
	public void testGetSetXrefLabelNames_noXrefField() throws MolgenisModelException
	{
		Field field = new Field(mock(Entity.class), "field", mock(FieldType.class));
		field.setXrefLabelNames(Arrays.asList("a", "b", "c"));
		field.getXrefLabelNames();
	}

	// TODO implement
	// @Test
	// public void testGetXrefLabelTree()
	// {
	// Assert.fail("Not yet implemented");
	// }

	// TODO implement
	// @Test
	// public void testGetXrefLabelTreeListOfStringSimpleTreeOfQ()
	// {
	// Assert.fail("Not yet implemented");
	// }

	// TODO implement
	// @Test
	// public void testGetXrefLabelPath()
	// {
	// Assert.fail("Not yet implemented");
	// }

	// TODO implement
	// @Test
	// public void testGetXrefLabels() throws DatabaseException,
	// MolgenisModelException
	// {
	// }

	@Test(expectedExceptions = MolgenisModelException.class)
	public void testGetXrefLabels_noXrefField() throws DatabaseException, MolgenisModelException
	{
		Field field = new Field(mock(Entity.class), "field", mock(FieldType.class));
		field.setXRefEntity("xrefEntity");
		field.setXrefLabelNames(Arrays.asList("a.1", "b.2", "c.3"));
		field.getXrefLabels();
	}

	// TODO implement
	// @Test
	// public void testGetXrefLabelsTemp()
	// {
	// Assert.fail("Not yet implemented");
	// }

	// TODO enable test after getXrefLabelsTemp is fixed (see fixme)
	// @Test(expectedExceptions = MolgenisModelException.class)
	// public void testGetXrefLabelsTemp_noXrefField() throws DatabaseException,
	// MolgenisModelException
	// {
	// Field field = new Field(mock(Entity.class), "field",
	// mock(FieldType.class));
	// field.setXrefLabelNames(Arrays.asList("a.1", "b.2", "c.3"));
	// field.getXrefLabelsTemp();
	// }

	// TODO implement
	// @Test
	// public void testAllPossibleXrefLabels() throws MolgenisModelException,
	// DatabaseException
	// {
	// }

	@Test
	public void testAllPossibleXrefLabels_noXrefKeys() throws MolgenisModelException, DatabaseException
	{
		Vector<Unique> keys = new Vector<Unique>();

		Entity xrefEntity = when(mock(Entity.class).getAllKeys()).thenReturn(keys).getMock();
		Model model = when(mock(Model.class).getEntity("xrefEntity")).thenReturn(xrefEntity).getMock();
		Entity entity = when(mock(Entity.class).getModel()).thenReturn(model).getMock();

		Field field = new Field(entity, "field", new XrefField());
		field.setXRefEntity("xrefEntity");
		assertTrue(field.allPossibleXrefLabels().isEmpty());
	}

	@Test(expectedExceptions = MolgenisModelException.class)
	public void testAllPossibleXrefLabels_noXrefField() throws DatabaseException, MolgenisModelException
	{
		Field field = new Field(mock(Entity.class), "field", mock(FieldType.class));
		field.setXRefEntity("xrefEntity");
		field.allPossibleXrefLabels();
	}

	@Test
	public void testGetLength() throws MolgenisModelException
	{
		Field field = new Field(mock(Entity.class), "field", new StringField());
		field.setVarCharLength(10);
		assertEquals(field.getLength(), Integer.valueOf(10));
	}

	@Test
	public void testGetLength_noStringField() throws MolgenisModelException
	{
		Field field = new Field(mock(Entity.class), "field", mock(FieldType.class));
		field.setVarCharLength(10);
		assertNull(field.getLength());
	}

	@Test
	public void testGetSqlName()
	{
		Field field = new Field(mock(Entity.class), "field", mock(FieldType.class));
		field.setTableName("Table");
		assertEquals(field.getSqlName(), "Table.field");
	}

	@Test
	public void testGetSqlName_capitalize()
	{
		Field field = new Field(mock(Entity.class), "Field", mock(FieldType.class));
		field.setTableName("table");
		assertEquals(field.getSqlName(), "Table.Field");
	}

	// TODO implement
	// @Test
	// public void testEqualsObject()
	// {
	// Assert.fail("Not yet implemented");
	// }
}
