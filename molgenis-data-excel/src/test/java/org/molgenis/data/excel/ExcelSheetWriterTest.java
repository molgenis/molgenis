package org.molgenis.data.excel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.molgenis.data.Entity;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExcelSheetWriterTest
{
	private ExcelWriter excelWriter;
	private ByteArrayOutputStream bos;
	private ExcelSheetWriter excelSheetWriter;

	@BeforeMethod
	public void setUp() throws IOException
	{
		bos = new ByteArrayOutputStream();
		excelWriter = new ExcelWriter(bos);
		excelSheetWriter = excelWriter.createWritable("sheet", Arrays.asList("col1", "col2"));
	}

	@AfterMethod
	public void tearDown() throws IOException
	{
		excelWriter.close();
	}

	@Test
	public void addCellProcessor() throws IOException
	{
		CellProcessor processor = when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();

		Entity entity = new MapEntity();
		entity.set("col1", "val1");
		entity.set("col2", "val2");

		excelSheetWriter.addCellProcessor(processor);
		excelSheetWriter.add(entity);

		verify(processor).process("val1");
		verify(processor).process("val2");
	}

	@Test
	public void write() throws IOException
	{
		Entity entity1 = new MapEntity();
		entity1.set("col1", "val1");
		entity1.set("col2", "val2");
		excelSheetWriter.add(entity1);

		Entity entity2 = new MapEntity();
		entity2.set("col1", "val3");
		entity2.set("col2", "val4");
		excelSheetWriter.add(entity2);

		excelWriter.close();

	}

}
