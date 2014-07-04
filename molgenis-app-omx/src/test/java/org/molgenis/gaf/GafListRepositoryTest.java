package org.molgenis.gaf;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListFeed;

//TODO JJ
public class GafListRepositoryTest
{
	private GafListFileRepository gafListFileRepository;
	private GafListValidationReport report;
	private SpreadsheetService spreadsheetService;
	private ListFeed listFeed;
	private CellFeed cellFeed;
	//
	// @BeforeMethod
	// public void setUp() throws IOException, ServiceException
	// {
	// spreadsheetService = mock(SpreadsheetService.class);
	// listFeed = mock(ListFeed.class);
	// TextConstruct textConstruct = when(mock(TextConstruct.class).getPlainText()).thenReturn("name").getMock();
	// when(listFeed.getTitle()).thenReturn(textConstruct);
	// List<ListEntry> entries = new ArrayList<ListEntry>();
	// ListEntry entry = mock(ListEntry.class);
	// CustomElementCollection customElements = mock(CustomElementCollection.class);
	// when(customElements.getTags()).thenReturn(new LinkedHashSet<String>(Arrays.asList("col1", "col2", "col3")));
	// when(customElements.getValue(GafListValidator.COL_RUN)).thenReturn("1");
	// when(customElements.getValue("col2")).thenReturn("val2");
	// when(customElements.getValue("col3")).thenReturn("val3");
	// when(entry.getCustomElements()).thenReturn(customElements);
	// entries.add(entry);
	// when(listFeed.getEntries()).thenReturn(entries);
	// cellFeed = mock(CellFeed.class);
	// List<CellEntry> cells = new ArrayList<CellEntry>();
	//
	// Cell cell1 = mock(Cell.class);
	// when(cell1.getRow()).thenReturn(1);
	// when(cell1.getValue()).thenReturn(GafListValidator.COL_RUN);
	// Cell cell2 = mock(Cell.class);
	// when(cell2.getRow()).thenReturn(1);
	// when(cell2.getValue()).thenReturn("col2");
	// Cell cell3 = mock(Cell.class);
	// when(cell3.getRow()).thenReturn(1);
	// when(cell3.getValue()).thenReturn("col3");
	// CellEntry entry1 = when(mock(CellEntry.class).getCell()).thenReturn(cell1).getMock();
	// CellEntry entry2 = when(mock(CellEntry.class).getCell()).thenReturn(cell2).getMock();
	// CellEntry entry3 = when(mock(CellEntry.class).getCell()).thenReturn(cell3).getMock();
	// cells.add(entry1);
	// cells.add(entry2);
	// cells.add(entry3);
	// when(cellFeed.getEntries()).thenReturn(cells);
	// when(cellFeed.getTitle()).thenReturn(textConstruct);
	// report = mock(GafListValidationReport.class);
	// gafListFileRepository = new GafListRepository(spreadsheetService, "key", "id", Visibility.PUBLIC, report);
	// }
	//
	// @Test(expectedExceptions = IllegalArgumentException.class)
	// public void GafListRepository() throws IOException, ServiceException
	// {
	// GafListRepository repo = null;
	// try
	// {
	// repo = new GafListRepository(null, null, null, null, null);
	// }
	// finally
	// {
	// if (repo != null) repo.close();
	// }
	// }
	//
	// @SuppressWarnings("unchecked")
	// @Test
	// public void iterator() throws IOException, ServiceException
	// {
	// when(report.hasErrors("1")).thenReturn(false);
	// when(spreadsheetService.getFeed(any(URL.class), any(Class.class))).thenReturn(cellFeed).thenReturn(listFeed);
	// Iterator<Entity> it = spreadsheetRepository.iterator();
	// assertTrue(it.hasNext());
	// it.next();
	// assertFalse(it.hasNext());
	// }
	//
	// @SuppressWarnings("unchecked")
	// @Test
	// public void iterator_errors() throws IOException, ServiceException
	// {
	// when(report.hasErrors("1")).thenReturn(true);
	// when(spreadsheetService.getFeed(any(URL.class), any(Class.class))).thenReturn(cellFeed).thenReturn(listFeed);
	// Iterator<Entity> it = spreadsheetRepository.iterator();
	// assertFalse(it.hasNext());
	// }
}
