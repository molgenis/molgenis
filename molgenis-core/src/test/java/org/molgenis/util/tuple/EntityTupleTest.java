package org.molgenis.util.tuple;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.Iterator;
import java.util.Vector;

import org.molgenis.util.Entity;
import org.testng.annotations.Test;

public class EntityTupleTest
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void EntityTuple()
	{
		new EntityTuple(null);
	}

	@Test
	public void getString()
	{
		Entity entity = when(mock(Entity.class).get("col1")).thenReturn("val1").getMock();
		assertEquals(new EntityTuple(entity).get("col1"), "val1");
	}

	@Test
	public void getint()
	{
		Vector<String> fields = new Vector<String>();
		fields.add("col1");
		fields.add("col2");

		Entity entity = mock(Entity.class);
		when(entity.get("col1")).thenReturn("val1").getMock();
		when(entity.get("col2")).thenReturn("val2").getMock();
		when(entity.getFields()).thenReturn(fields);

		EntityTuple entityTuple = new EntityTuple(entity);
		assertEquals(entityTuple.get("col1"), "val1");
		assertEquals(entityTuple.get("col2"), "val2");
	}

	@Test
	public void getColNames()
	{
		Vector<String> fields = new Vector<String>();
		fields.add("col1");
		fields.add("col2");

		Entity entity = when(mock(Entity.class).getFields()).thenReturn(fields).getMock();

		EntityTuple entityTuple = new EntityTuple(entity);
		Iterator<String> colNames = entityTuple.getColNames().iterator();
		assertEquals(colNames.next(), "col1");
		assertEquals(colNames.next(), "col2");
		assertFalse(colNames.hasNext());
	}

	@Test
	public void getNrCols()
	{
		Vector<String> fields = new Vector<String>();
		fields.add("col1");
		fields.add("col2");

		Entity entity = when(mock(Entity.class).getFields()).thenReturn(fields).getMock();
		assertEquals(new EntityTuple(entity).getNrCols(), 2);
	}
}
