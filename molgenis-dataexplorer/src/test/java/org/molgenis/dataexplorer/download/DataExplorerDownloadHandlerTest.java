package org.molgenis.dataexplorer.download;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.controller.DataRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataExplorerDownloadHandlerTest
{
	DataService dataService;
	AttributeFactory attributeFactory;

	private DataExplorerDownloadHandler dataExplorerDownloadHandler;

	@BeforeMethod
	public void beforeTest() throws IOException
	{
		dataService = mock(DataService.class);
		attributeFactory = mock(AttributeFactory.class);
		dataExplorerDownloadHandler = new DataExplorerDownloadHandler(dataService, attributeFactory);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Total number of cells for this download exceeds the maximum of 500000 for .xlsx downloads, please use .csv instead")
	public void testWriteToExcel() throws Exception
	{
		EntityType entityType = mock(EntityType.class);
		Attribute attribute = mock(Attribute.class);
		MetaDataService metaDataService = mock(MetaDataService.class);

		when(dataService.getMeta()).thenReturn(metaDataService);
		when(dataService.count("sys_set_thousandgenomes", new QueryImpl<>())).thenReturn(500001L);
		when(metaDataService.getEntityTypeById("sys_set_thousandgenomes")).thenReturn(entityType);
		when(entityType.getAllAttributes()).thenReturn(Collections.singletonList(attribute));

		DataRequest dataRequest = mock(DataRequest.class);
		when(dataRequest.getEntityName()).thenReturn("sys_set_thousandgenomes");
		when(dataRequest.getQuery()).thenReturn(new QueryImpl<>());

		dataExplorerDownloadHandler.writeToExcel(dataRequest, mock(OutputStream.class));
	}

}