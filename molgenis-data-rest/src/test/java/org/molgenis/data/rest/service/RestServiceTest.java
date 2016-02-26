package org.molgenis.data.rest.service;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.IdGenerator;
import org.molgenis.file.FileStore;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RestServiceTest
{
	private RestService restService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		DataService dataService = mock(DataService.class);
		IdGenerator idGenerator = mock(IdGenerator.class);
		FileStore fileStore = mock(FileStore.class);
		this.restService = new RestService(dataService, idGenerator, fileStore);
	}

	@Test
	public void toEntityValue()
	{
		AttributeMetaData attr = mock(AttributeMetaData.class);
		when(attr.getDataType()).thenReturn(MREF);
		assertEquals(restService.toEntityValue(attr, null), emptyList());
	}
}
