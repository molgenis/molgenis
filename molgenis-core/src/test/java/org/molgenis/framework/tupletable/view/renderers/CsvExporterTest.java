package org.molgenis.framework.tupletable.view.renderers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.model.elements.Field;
import org.molgenis.util.tuple.Tuple;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CsvExporterTest
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void CsvExporter()
	{
		new CsvExporter(null);
	}

	@Test
	public void export() throws IOException, TableException
	{
		TupleTable tupleTable = mock(TupleTable.class);
		Field field1 = when(mock(Field.class).getSqlName()).thenReturn("col1").getMock();
		Field field2 = when(mock(Field.class).getSqlName()).thenReturn("col2").getMock();
		when(tupleTable.getColumns()).thenReturn(Arrays.asList(field1, field2));
		Tuple row1 = mock(Tuple.class);
		when(row1.getNrCols()).thenReturn(2);
		when(row1.get("col1")).thenReturn("val1");
		when(row1.get("col2")).thenReturn("val2");
		when(row1.hasColNames()).thenReturn(true);
		when(row1.getColNames()).thenReturn(Arrays.asList("col1", "col2"));

		Tuple row2 = mock(Tuple.class);
		when(row2.getNrCols()).thenReturn(2);
		when(row2.get("col1")).thenReturn("val3");
		when(row2.get("col2")).thenReturn("val4");
		when(row2.hasColNames()).thenReturn(true);
		when(row2.getColNames()).thenReturn(Arrays.asList("col1", "col2"));
		when(tupleTable.iterator()).thenReturn(Arrays.asList(row1, row2).iterator());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		CsvExporter csvExporter = new CsvExporter(tupleTable);
		csvExporter.export(bos);
		String csvString = new String(bos.toByteArray(), Charset.forName("UTF-8"));
		Assert.assertEquals(csvString, "\"col1\",\"col2\"\n\"val1\",\"val2\"\n\"val3\",\"val4\"\n");
	}

	@Test
	public void export_offsetsLimits() throws TableException, IOException
	{
		TupleTable tupleTable = mock(TupleTable.class);
		when(tupleTable.getColOffset()).thenReturn(2);
		when(tupleTable.getColLimit()).thenReturn(3);
		when(tupleTable.getOffset()).thenReturn(1);
		when(tupleTable.getLimit()).thenReturn(2);

		Field field1 = when(mock(Field.class).getSqlName()).thenReturn("col1").getMock();
		Field field2 = when(mock(Field.class).getSqlName()).thenReturn("col2").getMock();
		when(tupleTable.getColumns()).thenReturn(Arrays.asList(field1, field2));
		Tuple row1 = mock(Tuple.class);
		when(row1.getNrCols()).thenReturn(2);
		when(row1.get("col1")).thenReturn("val1");
		when(row1.get("col2")).thenReturn("val2");
		when(row1.hasColNames()).thenReturn(true);
		when(row1.getColNames()).thenReturn(Arrays.asList("col1", "col2"));

		Tuple row2 = mock(Tuple.class);
		when(row2.getNrCols()).thenReturn(2);
		when(row2.get("col1")).thenReturn("val3");
		when(row2.get("col2")).thenReturn("val4");
		when(row2.hasColNames()).thenReturn(true);
		when(row2.getColNames()).thenReturn(Arrays.asList("col1", "col2"));
		when(tupleTable.iterator()).thenReturn(Arrays.asList(row1, row2).iterator());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		CsvExporter csvExporter = new CsvExporter(tupleTable);
		csvExporter.export(bos);
		String csvString = new String(bos.toByteArray(), Charset.forName("UTF-8"));
		Assert.assertEquals(csvString, "\"col1\",\"col2\"\n\"val1\",\"val2\"\n\"val3\",\"val4\"\n");
	}
}
