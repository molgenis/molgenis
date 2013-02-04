package org.molgenis.framework.tupletable.view.renderers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.model.elements.Field;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;
import org.testng.annotations.Test;

public class ExcelExporterTest
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ExcelExporter()
	{
		new ExcelExporter(null);
	}

	@Test
	public void export() throws TableException, IOException
	{
		TupleTable tableModel = mock(TupleTable.class);
		Field field1 = when(mock(Field.class).getName()).thenReturn("col1").getMock();
		Field field2 = when(mock(Field.class).getName()).thenReturn("col2").getMock();
		Field field3 = when(mock(Field.class).getName()).thenReturn("col3").getMock();
		when(tableModel.getColumns()).thenReturn(Arrays.asList(field1, field2, field3));
		WritableTuple row1 = new KeyValueTuple();
		row1.set("col1", "val1.1");
		row1.set("col2", "val1.2");
		row1.set("col3", "val1.3");
		WritableTuple row2 = new KeyValueTuple();
		row2.set("col1", "val2.1");
		row2.set("col2", "val2.2");
		row2.set("col3", "val2.3");
		when(tableModel.iterator()).thenReturn(Arrays.<Tuple> asList(row1, row2).iterator());

		ExcelExporter excelExporter = new ExcelExporter(tableModel);
		File xlsFile = File.createTempFile("table", ".xls");
		try
		{
			excelExporter.export(new FileOutputStream(xlsFile));

			// check output
			ExcelReader excelReader = new ExcelReader(xlsFile);
			ExcelSheetReader sheetReader = excelReader.getSheet("Sheet1");
			try
			{
				Iterator<Tuple> it = sheetReader.iterator();
				assertTrue(it.hasNext());
				Tuple tuple1 = it.next();
				assertEquals(tuple1.getString("col1"), row1.getString("col1"));
				assertEquals(tuple1.getString("col2"), row1.getString("col2"));
				assertEquals(tuple1.getString("col3"), row1.getString("col3"));
				assertTrue(it.hasNext());
				Tuple tuple2 = it.next();
				assertEquals(tuple2.getString("col1"), row2.getString("col1"));
				assertEquals(tuple2.getString("col2"), row2.getString("col2"));
				assertEquals(tuple2.getString("col3"), row2.getString("col3"));
				assertFalse(it.hasNext());
			}
			finally
			{
				IOUtils.closeQuietly(sheetReader);
				IOUtils.closeQuietly(excelReader);
			}
		}
		finally
		{
			xlsFile.delete();
		}
	}

	@Test
	public void export_offsetsLimits() throws TableException, IOException
	{
		TupleTable tableModel = mock(TupleTable.class);
		when(tableModel.getColOffset()).thenReturn(2);
		when(tableModel.getColLimit()).thenReturn(3);
		when(tableModel.getOffset()).thenReturn(1);
		when(tableModel.getLimit()).thenReturn(2);

		// verify that offsets and limits are ignored during export
		Field field1 = when(mock(Field.class).getName()).thenReturn("col1").getMock();
		Field field2 = when(mock(Field.class).getName()).thenReturn("col2").getMock();
		Field field3 = when(mock(Field.class).getName()).thenReturn("col3").getMock();
		when(tableModel.getColumns()).thenReturn(Arrays.asList(field1, field2, field3));
		WritableTuple row1 = new KeyValueTuple();
		row1.set("col1", "val1.1");
		row1.set("col2", "val1.2");
		row1.set("col3", "val1.3");
		WritableTuple row2 = new KeyValueTuple();
		row2.set("col1", "val2.1");
		row2.set("col2", "val2.2");
		row2.set("col3", "val2.3");
		when(tableModel.iterator()).thenReturn(Arrays.<Tuple> asList(row1, row2).iterator());

		ExcelExporter excelExporter = new ExcelExporter(tableModel);
		File xlsFile = File.createTempFile("table", ".xls");
		try
		{
			excelExporter.export(new FileOutputStream(xlsFile));

			// check output
			ExcelReader excelReader = new ExcelReader(xlsFile);
			ExcelSheetReader sheetReader = excelReader.getSheet("Sheet1");
			try
			{
				Iterator<Tuple> it = sheetReader.iterator();
				assertTrue(it.hasNext());
				Tuple tuple1 = it.next();
				assertEquals(tuple1.getString("col1"), row1.getString("col1"));
				assertEquals(tuple1.getString("col2"), row1.getString("col2"));
				assertEquals(tuple1.getString("col3"), row1.getString("col3"));
				assertTrue(it.hasNext());
				Tuple tuple2 = it.next();
				assertEquals(tuple2.getString("col1"), row2.getString("col1"));
				assertEquals(tuple2.getString("col2"), row2.getString("col2"));
				assertEquals(tuple2.getString("col3"), row2.getString("col3"));
				assertFalse(it.hasNext());
			}
			finally
			{
				IOUtils.closeQuietly(sheetReader);
				IOUtils.closeQuietly(excelReader);
			}
		}
		finally
		{
			xlsFile.delete();
		}
	}
}
