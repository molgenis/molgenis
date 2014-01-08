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
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.util.ServiceException;

public class GoogleSpreadsheetRepositoryTest
{
	private GoogleSpreadsheetRepository spreadsheetRepository;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp() throws IOException, ServiceException
	{
		SpreadsheetService spreadsheetService = mock(SpreadsheetService.class);
		ListFeed listFeed = mock(ListFeed.class);
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
		when(spreadsheetService.getFeed(any(URL.class), (Class<ListFeed>) any(Class.class))).thenReturn(listFeed);
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

	@Test
	public void iterator()
	{
		Iterator<Entity> it = spreadsheetRepository.iterator();
		assertTrue(it.hasNext());
		Entity entity = it.next();
		assertEquals(entity.getString("col1"), "val1");
		assertEquals(entity.getString("col2"), "val2");
		assertEquals(entity.getString("col3"), "val3");
		assertFalse(it.hasNext());
	}

	@Test
	public void getEntityMetaData()
	{
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
