package org.molgenis.googlespreadsheet;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.TextConstruct;
import com.google.gdata.data.spreadsheet.Cell;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.util.ServiceException;

public class GoogleSpreadsheetRepositoryTest
{
	private GoogleSpreadsheetRepository spreadsheetRepository;
	private SpreadsheetService spreadsheetService;
	private ListFeed listFeed;
	private CellFeed cellFeed;

	@BeforeMethod
	public void setUp() throws IOException, ServiceException
	{
		spreadsheetService = mock(SpreadsheetService.class);
		listFeed = mock(ListFeed.class);
		TextConstruct textConstruct = when(mock(TextConstruct.class).getPlainText()).thenReturn("name").getMock();
		when(listFeed.getTitle()).thenReturn(textConstruct);
		List<ListEntry> entries = new ArrayList<ListEntry>();
		ListEntry entry = mock(ListEntry.class);
		CustomElementCollection customElements = mock(CustomElementCollection.class);
		when(customElements.getTags()).thenReturn(new LinkedHashSet<String>(Arrays.asList("col1", "col2", "col3")));
		when(customElements.getValue("col1")).thenReturn("val1");
		when(customElements.getValue("col2")).thenReturn("val2");
		when(customElements.getValue("col3")).thenReturn("val3");
		when(entry.getCustomElements()).thenReturn(customElements);
		entries.add(entry);
		when(listFeed.getEntries()).thenReturn(entries);
		cellFeed = mock(CellFeed.class);
		List<CellEntry> cells = new ArrayList<CellEntry>();

		Cell cell1 = mock(Cell.class);
		when(cell1.getRow()).thenReturn(1);
		when(cell1.getValue()).thenReturn("col1");
		Cell cell2 = mock(Cell.class);
		when(cell2.getRow()).thenReturn(1);
		when(cell2.getValue()).thenReturn("col2");
		Cell cell3 = mock(Cell.class);
		when(cell3.getRow()).thenReturn(1);
		when(cell3.getValue()).thenReturn("col3");
		CellEntry entry1 = when(mock(CellEntry.class).getCell()).thenReturn(cell1).getMock();
		CellEntry entry2 = when(mock(CellEntry.class).getCell()).thenReturn(cell2).getMock();
		CellEntry entry3 = when(mock(CellEntry.class).getCell()).thenReturn(cell3).getMock();
		cells.add(entry1);
		cells.add(entry2);
		cells.add(entry3);
		when(cellFeed.getEntries()).thenReturn(cells);
		when(cellFeed.getTitle()).thenReturn(textConstruct);
		spreadsheetRepository = new GoogleSpreadsheetRepository(spreadsheetService, "key", "id");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void GoogleSpreadsheetRepository() throws IOException, ServiceException
	{
		new GoogleSpreadsheetRepository(null, null, null);
	}

	@Test
	public void getEntityClass() throws IOException, ServiceException
	{
		assertEquals(MapEntity.class, spreadsheetRepository.getEntityClass());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void iterator() throws IOException, ServiceException
	{
		when(spreadsheetService.getFeed(any(URL.class), (Class<ListFeed>) any(Class.class))).thenReturn(listFeed);
		Iterator<Entity> it = spreadsheetRepository.iterator();
		assertTrue(it.hasNext());
		Entity entity = it.next();
		assertEquals(entity.getString("col1"), "val1");
		assertEquals(entity.getString("col2"), "val2");
		assertEquals(entity.getString("col3"), "val3");
		assertFalse(it.hasNext());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getEntityMetaData() throws IOException, ServiceException
	{
		when(spreadsheetService.getFeed(any(URL.class), (Class<CellFeed>) any(Class.class))).thenReturn(cellFeed);
		EntityMetaData entityMetaData = spreadsheetRepository.getEntityMetaData();
		assertEquals(entityMetaData.getName(), "name");
		Iterator<AttributeMetaData> it = entityMetaData.getAttributes().iterator();
		assertTrue(it.hasNext());
		assertEquals(it.next().getName(), "col1");
		assertTrue(it.hasNext());
		assertEquals(it.next().getName(), "col2");
		assertTrue(it.hasNext());
		assertEquals(it.next().getName(), "col3");
		assertFalse(it.hasNext());
	}
}
